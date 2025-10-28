package com.aula.mobile_hivemind.ui.home.paradas;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.aula.mobile_hivemind.MainActivity;
import com.aula.mobile_hivemind.R;
import com.aula.mobile_hivemind.api.mongo.RetrofitClient;
import com.aula.mobile_hivemind.api.mongo.ApiServiceMongo;
import com.aula.mobile_hivemind.dto.mongo.RegistroParadaRequestDTO;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddParadaFragment extends Fragment {

    private ImageButton btnBack;
    private MaterialButton btnAdicionarParada;
    private EditText idMaquina;
    private EditText nomeMaquina;
    private EditText codigoColaborador;
    private TextView setor;
    private EditText descricaoParada;
    private EditText editTextDATAPARADA;
    private ApiServiceMongo apiServiceMongo;

    private FirebaseFirestore db;
    private String userSetor;

    public AddParadaFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_parada, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        apiServiceMongo = RetrofitClient.getApiService();
        db = FirebaseFirestore.getInstance();

        hideFab();
        inicializarViews(view);
        carregarSetorUsuario(); // 🔧 CARREGA O SETOR DO USUÁRIO
        configurarDatePicker();
        configurarBotoes();
    }

    private void inicializarViews(View view) {
        idMaquina = view.findViewById(R.id.editIdMaquina);
        nomeMaquina = view.findViewById(R.id.editNomeMaquina);
        codigoColaborador = view.findViewById(R.id.editCodigoColaborador);
        setor = view.findViewById(R.id.textSetor);
        descricaoParada = view.findViewById(R.id.editDescricaoParada);
        editTextDATAPARADA = view.findViewById(R.id.editTextDATAPARADA);
        btnBack = view.findViewById(R.id.btnBack);
        btnAdicionarParada = view.findViewById(R.id.btnAdicionarParada);

        setor.setText("Carregando setor...");
    }

    // 🔧 MÉTODO SIMPLES: CARREGA O SETOR DO USUÁRIO LOGADO
    private void carregarSetorUsuario() {
        // Pega o email do usuário logado do SharedPreferences
        SharedPreferences prefs = requireContext().getSharedPreferences("ProfilePrefs", 0);
        String userEmail = prefs.getString("user_email", "");

        if (userEmail.isEmpty()) {
            setor.setText("Usuário não logado");
            return;
        }

        // Busca o usuário no Firestore
        db.collection("trabalhadores")
                .whereEqualTo("login", userEmail)
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        userSetor = task.getResult().getDocuments().get(0).getString("setor");

                        if (userSetor != null && !userSetor.isEmpty()) {
                            setor.setText(userSetor);
                            Log.d("AddParadaFragment", "Setor carregado: " + userSetor);
                        } else {
                            setor.setText("Setor não definido");
                        }
                    } else {
                        setor.setText("Usuário não encontrado");
                    }
                });
    }

    private void configurarDatePicker() {
        editTextDATAPARADA.setFocusable(false);
        editTextDATAPARADA.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                mostrarDatePicker();
                return true;
            }
            return false;
        });
    }

    private void configurarBotoes() {
        btnBack.setOnClickListener(v -> navigateBack());
        btnAdicionarParada.setOnClickListener(v -> salvarParada());
    }

    @Override
    public void onResume() {
        super.onResume();
        hideFab();
    }

    private void hideFab() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setFabVisibility(false);
            ((MainActivity) getActivity()).setBottomNavigationVisibility(false);
        }
    }

    private void navigateBack() {
        showFab();
        try {
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
            navController.popBackStack();
        } catch (Exception e) {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        }
    }

    private void showFab() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setFabVisibility(true);
            ((MainActivity) getActivity()).setBottomNavigationVisibility(true);
        }
    }

    private void mostrarDatePicker() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long today = calendar.getTimeInMillis();

        CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder();
        constraintsBuilder.setEnd(today);

        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Selecione a data")
                .setCalendarConstraints(constraintsBuilder.build())
                .build();

        datePicker.show(getParentFragmentManager(), "DATE_PICKER");

        datePicker.addOnPositiveButtonClickListener(selection -> {
            if (selection > today) {
                Toast.makeText(requireContext(), "Não é possível adicionar paradas futuras", Toast.LENGTH_SHORT).show();
                return;
            }

            SimpleDateFormat sdf = new SimpleDateFormat("dd, MMM yyyy", Locale.getDefault());
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            String selectedDate = sdf.format(new Date(selection));
            editTextDATAPARADA.setText(selectedDate);
        });
    }

    private void salvarParada() {
        try {
            if (!validarCampos()) return;

            String setorText = setor.getText().toString().trim();

            // Só permite salvar se o setor foi carregado corretamente
            if (setorText.equals("Carregando setor...") ||
                    setorText.equals("Usuário não logado") ||
                    setorText.equals("Usuário não encontrado") ||
                    setorText.equals("Setor não definido")) {
                Toast.makeText(requireContext(), "Aguarde o setor ser carregado", Toast.LENGTH_LONG).show();
                return;
            }

            RegistroParadaRequestDTO requestDTO = new RegistroParadaRequestDTO(
                    Integer.parseInt(idMaquina.getText().toString().trim()),
                    nomeMaquina.getText().toString().trim(),
                    Integer.parseInt(codigoColaborador.getText().toString().trim()),
                    setorText, // 🔧 SETOR DO USUÁRIO (automático)
                    descricaoParada.getText().toString().trim(),
                    editTextDATAPARADA.getText().toString().trim()
            );

            salvarNoMongoDB(requestDTO);
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), "ID Máquina e Código Colaborador devem ser números", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Erro ao salvar parada", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validarCampos() {
        if (idMaquina.getText().toString().trim().isEmpty()) {
            Toast.makeText(requireContext(), "ID da Máquina é obrigatório", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (nomeMaquina.getText().toString().trim().isEmpty()) {
            Toast.makeText(requireContext(), "Nome da Máquina é obrigatório", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (codigoColaborador.getText().toString().trim().isEmpty()) {
            Toast.makeText(requireContext(), "Código do Colaborador é obrigatório", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (descricaoParada.getText().toString().trim().isEmpty()) {
            Toast.makeText(requireContext(), "Descrição da Parada é obrigatória", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (editTextDATAPARADA.getText().toString().trim().isEmpty()) {
            Toast.makeText(requireContext(), "Data da Parada é obrigatória", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void salvarNoMongoDB(RegistroParadaRequestDTO requestDTO) {
        btnAdicionarParada.setEnabled(false);
        btnAdicionarParada.setText("Salvando...");

        Call<ResponseBody> call = apiServiceMongo.criarRegistro(requestDTO);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                btnAdicionarParada.setEnabled(true);
                btnAdicionarParada.setText("Adicionar Parada");

                if (response.isSuccessful()) {
                    limparCampos();
                    Toast.makeText(requireContext(), "Parada salva com sucesso!", Toast.LENGTH_SHORT).show();

                    try {
                        NavHostFragment.findNavController(AddParadaFragment.this).navigate(R.id.confirmationFragment);
                    } catch (Exception e) {
                        navigateBack();
                    }
                } else {
                    Toast.makeText(requireContext(), "Erro ao salvar parada", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                btnAdicionarParada.setEnabled(true);
                btnAdicionarParada.setText("Adicionar Parada");
                Toast.makeText(requireContext(), "Falha na conexão", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void limparCampos() {
        idMaquina.setText("");
        nomeMaquina.setText("");
        codigoColaborador.setText("");
        descricaoParada.setText("");
        editTextDATAPARADA.setText("");
    }
}