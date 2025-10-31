package com.aula.mobile_hivemind.ui.home.paradas;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.aula.mobile_hivemind.MainActivity;
import com.aula.mobile_hivemind.R;
import com.aula.mobile_hivemind.api.RetrofitClient;
import com.aula.mobile_hivemind.api.SqlApiService;
import com.aula.mobile_hivemind.dto.MaquinaResponseDTO;
import com.aula.mobile_hivemind.dto.RegistroParadaRequestDTO;
import com.aula.mobile_hivemind.dto.TrabalhadorResponseDTO;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import java.sql.Time;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddParadaFragment extends Fragment {
    private EditText editIdMaquina, editNomeMaquina, editIdUsuario, textSetor, editDescricaoParada;

    private TextInputEditText editTextDATAPARADA, editTextHoraInicio, editTextHoraFim;

    private MaterialButton btnAdicionarParada, btnBuscarMaquina, btnValidarColaborador;
    private ImageButton btnVoltar;
    private MaterialTextView textResumoInfo;
    private Calendar calendar;
    private SimpleDateFormat dateFormatter, timeFormatter;
    private com.aula.mobile_hivemind.api.ApiService apiService;
    private SqlApiService sqlApiService;
    private FirebaseFirestore db;

    // Flags para validação
    private boolean maquinaValida = false;
    private boolean colaboradorValido = false;
    private String nomeMaquinaEncontrada = "";
    private String setorMaquinaEncontrado = "";

    public AddParadaFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_parada, container, false);

        // Inicializar API Services
        apiService = RetrofitClient.getApiService();
        sqlApiService = RetrofitClient.getSqlApiService();
        db = FirebaseFirestore.getInstance();

        // Inicializar formatadores de data e hora
        calendar = Calendar.getInstance();
        dateFormatter = new SimpleDateFormat("dd, MMM yyyy", Locale.getDefault());
        timeFormatter = new SimpleDateFormat("HH:mm", Locale.getDefault());

        // Inicializar views
        initViews(view);

        // Configurar listeners
        setupListeners(view);

        hideFab();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        btnVoltar = view.findViewById(R.id.btnVoltar);
        btnVoltar.setOnClickListener(v -> navigateBack());
    }

    private void initViews(View view) {
        Bundle bundle = getArguments();

        editIdMaquina = view.findViewById(R.id.editIdMaquina);
        editNomeMaquina = view.findViewById(R.id.editNomeMaquina);
        editIdUsuario = view.findViewById(R.id.editCodigoColaborador);
        textSetor = view.findViewById(R.id.textSetor);
        editDescricaoParada = view.findViewById(R.id.editDescricaoParada);

        // Para os campos dentro do TextInputLayout, use findViewById no TextInputEditText
        TextInputLayout textInputLayoutData = view.findViewById(R.id.textInputLayoutDATAPARADA);
        if (textInputLayoutData != null) {
            editTextDATAPARADA = textInputLayoutData.findViewById(R.id.editTextDATAPARADA);
        }

        TextInputLayout textInputLayoutHoraInicio = view.findViewById(R.id.textInputLayoutHoraInicio);
        if (textInputLayoutHoraInicio != null) {
            editTextHoraInicio = textInputLayoutHoraInicio.findViewById(R.id.editTextHoraInicio);
        }

        TextInputLayout textInputLayoutHoraFim = view.findViewById(R.id.textInputLayoutHoraFim);
        if (textInputLayoutHoraFim != null) {
            editTextHoraFim = textInputLayoutHoraFim.findViewById(R.id.editTextHoraFim);
        }

        if (bundle != null && bundle.containsKey("userId")) {
            int userId = bundle.getInt("userId", 0);
            editIdUsuario.setText(String.valueOf(userId));
        }


        btnAdicionarParada = view.findViewById(R.id.btnAdicionarParada);
        btnBuscarMaquina = view.findViewById(R.id.btnBuscarMaquina);
        btnValidarColaborador = view.findViewById(R.id.btnValidarColaborador);
        textResumoInfo = view.findViewById(R.id.textResumoInfo);

        // Tornar campo nome da máquina não editável
        editNomeMaquina.setFocusable(false);
        editNomeMaquina.setClickable(false);
        editNomeMaquina.setCursorVisible(false);

        // Configurar data atual como padrão
        if (editTextDATAPARADA != null) {
            editTextDATAPARADA.setText(dateFormatter.format(calendar.getTime()));
        }
    }

    private void setupListeners(View view) {
        // Listener para data
        if (editTextDATAPARADA != null) {
            editTextDATAPARADA.setOnClickListener(v -> showDatePicker());
        }

        // Listener para hora de início
        if (editTextHoraInicio != null) {
            editTextHoraInicio.setOnClickListener(v -> showTimePicker(editTextHoraInicio, "Início"));
        }

        // Listener para hora de fim
        if (editTextHoraFim != null) {
            editTextHoraFim.setOnClickListener(v -> showTimePicker(editTextHoraFim, "Fim"));
        }

        // Listener para buscar máquina
        if (btnBuscarMaquina != null) {
            btnBuscarMaquina.setOnClickListener(v -> buscarMaquinaPorId());
        }

        // Listener para validar colaborador
        if (btnValidarColaborador != null) {
            btnValidarColaborador.setOnClickListener(v -> validarColaboradorPorId());
        }

        // Listener para o botão de adicionar parada
        if (btnAdicionarParada != null) {
            btnAdicionarParada.setOnClickListener(v -> adicionarParada());
        }

        if (btnVoltar != null) {
            btnVoltar.setOnClickListener(v -> navigateBack());
        }

        // Listeners para resetar validações quando os IDs mudarem
        if (editIdMaquina != null) {
            editIdMaquina.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    maquinaValida = false;
                    nomeMaquinaEncontrada = "";
                    setorMaquinaEncontrado = "";
                    if (editNomeMaquina != null) editNomeMaquina.setText("");
                    if (textSetor != null) textSetor.setText("");
                    atualizarResumo();
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        if (editIdUsuario != null) {
            editIdUsuario.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    colaboradorValido = false;
                    atualizarResumo();
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void buscarMaquinaPorId() {
        String idMaquinaStr = editIdMaquina.getText().toString().trim();

        if (idMaquinaStr.isEmpty()) {
            Toast.makeText(requireContext(), "Digite o ID da máquina", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Long idMaquina = Long.parseLong(idMaquinaStr);

            // Buscar todas as máquinas e filtrar pelo ID
            Call<List<MaquinaResponseDTO>> call = sqlApiService.listarMaquinas();
            call.enqueue(new Callback<List<MaquinaResponseDTO>>() {
                @Override
                public void onResponse(Call<List<MaquinaResponseDTO>> call, Response<List<MaquinaResponseDTO>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        List<MaquinaResponseDTO> maquinas = response.body();
                        MaquinaResponseDTO maquinaEncontrada = null;

                        // Buscar máquina pelo ID
                        for (MaquinaResponseDTO maquina : maquinas) {
                            if (maquina.getId().equals(idMaquina)) {
                                maquinaEncontrada = maquina;
                                break;
                            }
                        }

                        if (maquinaEncontrada != null) {
                            nomeMaquinaEncontrada = maquinaEncontrada.getNome();
                            setorMaquinaEncontrado = maquinaEncontrada.getSetor();

                            editNomeMaquina.setText(nomeMaquinaEncontrada);
                            textSetor.setText(setorMaquinaEncontrado);
                            maquinaValida = true;

                            Toast.makeText(requireContext(), "Máquina encontrada!", Toast.LENGTH_SHORT).show();
                            atualizarResumo();
                        } else {
                            limparCamposMaquina();
                            Toast.makeText(requireContext(), "Máquina não encontrada", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        limparCamposMaquina();
                        Toast.makeText(requireContext(), "Erro ao buscar máquinas", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<List<MaquinaResponseDTO>> call, Throwable t) {
                    limparCamposMaquina();
                    Toast.makeText(requireContext(), "Erro de conexão: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), "ID da máquina inválido", Toast.LENGTH_SHORT).show();
        }
    }

    private void validarColaboradorPorId() {
        String idColaboradorStr = editIdUsuario.getText().toString().trim();

        if (idColaboradorStr.isEmpty()) {
            Toast.makeText(requireContext(), "Digite o código do colaborador", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Long idColaborador = Long.parseLong(idColaboradorStr);

            // Buscar todos os trabalhadores e filtrar pelo ID
            Call<List<TrabalhadorResponseDTO>> call = sqlApiService.listarTrabalhadores();
            call.enqueue(new Callback<List<TrabalhadorResponseDTO>>() {
                @Override
                public void onResponse(Call<List<TrabalhadorResponseDTO>> call, Response<List<TrabalhadorResponseDTO>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        List<TrabalhadorResponseDTO> trabalhadores = response.body();
                        boolean encontrado = false;

                        // Buscar trabalhador pelo ID
                        for (TrabalhadorResponseDTO trabalhador : trabalhadores) {
                            if (trabalhador.getId().equals(idColaborador)) {
                                encontrado = true;
                                break;
                            }
                        }

                        if (encontrado) {
                            colaboradorValido = true;
                            Toast.makeText(requireContext(), "Colaborador válido!", Toast.LENGTH_SHORT).show();
                            atualizarResumo();
                        } else {
                            colaboradorValido = false;
                            Toast.makeText(requireContext(), "Colaborador não encontrado", Toast.LENGTH_SHORT).show();
                            atualizarResumo();
                        }
                    } else {
                        colaboradorValido = false;
                        Toast.makeText(requireContext(), "Erro ao buscar colaboradores", Toast.LENGTH_SHORT).show();
                        atualizarResumo();
                    }
                }

                @Override
                public void onFailure(Call<List<TrabalhadorResponseDTO>> call, Throwable t) {
                    colaboradorValido = false;
                    Toast.makeText(requireContext(), "Erro de conexão: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    atualizarResumo();
                }
            });

        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), "Código do colaborador inválido", Toast.LENGTH_SHORT).show();
        }
    }

    private void limparCamposMaquina() {
        maquinaValida = false;
        nomeMaquinaEncontrada = "";
        setorMaquinaEncontrado = "";
        editNomeMaquina.setText("");
        textSetor.setText("");
        atualizarResumo();
    }

    private void atualizarResumo() {
        StringBuilder resumo = new StringBuilder();

        if (maquinaValida) {
            resumo.append("✓ Máquina válida: ").append(nomeMaquinaEncontrada).append("\n");
        } else {
            resumo.append("✗ Máquina não validada\n");
        }

        if (colaboradorValido) {
            resumo.append("✓ Colaborador válido\n");
        } else {
            resumo.append("✗ Colaborador não validado\n");
        }

        if (editDescricaoParada.getText().toString().trim().isEmpty()) {
            resumo.append("✗ Descrição pendente");
        } else {
            resumo.append("✓ Descrição preenchida");
        }

        textResumoInfo.setText(resumo.toString());
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    String selectedDate = dateFormatter.format(calendar.getTime());
                    editTextDATAPARADA.setText(selectedDate);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void showTimePicker(TextInputEditText editText, String tipo) {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                requireContext(),
                (view, hourOfDay, minute) -> {
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.MINUTE, minute);

                    String selectedTime = timeFormatter.format(calendar.getTime());
                    editText.setText(selectedTime);
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
        );
        timePickerDialog.setTitle("Hora de " + tipo);
        timePickerDialog.show();
    }

    private void adicionarParada() {
        if (!validarCampos()) {
            return;
        }

        if (!maquinaValida) {
            Toast.makeText(requireContext(), "Valide a máquina antes de continuar", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!colaboradorValido) {
            Toast.makeText(requireContext(), "Valide o colaborador antes de continuar", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            RegistroParadaRequestDTO paradaRequest = new RegistroParadaRequestDTO();

            // Campos básicos - usando os novos nomes da API
            paradaRequest.setId_maquina(Integer.parseInt(editIdMaquina.getText().toString()));
            paradaRequest.setId_usuario(Integer.parseInt(editIdUsuario.getText().toString()));
            paradaRequest.setDes_parada(editDescricaoParada.getText().toString());
            paradaRequest.setDes_setor(textSetor.getText().toString());

            // Processar data e horas
            processarDataEHora(paradaRequest);

            // Enviar para a API MongoDB
            enviarParadaParaMongoDB(paradaRequest);

        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), "IDs devem ser números válidos", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Erro: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validarCampos() {
        if (editIdMaquina.getText().toString().trim().isEmpty()) {
            Toast.makeText(requireContext(), "ID da máquina é obrigatório", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (editIdUsuario.getText().toString().trim().isEmpty()) {
            Toast.makeText(requireContext(), "Código do colaborador é obrigatório", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (textSetor.getText().toString().trim().isEmpty()) {
            Toast.makeText(requireContext(), "Setor é obrigatório", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (editDescricaoParada.getText().toString().trim().isEmpty()) {
            Toast.makeText(requireContext(), "Descrição da parada é obrigatória", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (editTextDATAPARADA.getText().toString().trim().isEmpty()) {
            Toast.makeText(requireContext(), "Data da parada é obrigatória", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (editTextHoraInicio.getText().toString().trim().isEmpty()) {
            Toast.makeText(requireContext(), "Hora de início é obrigatória", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (editTextHoraFim.getText().toString().trim().isEmpty()) {
            Toast.makeText(requireContext(), "Hora de término é obrigatória", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Validar se hora de fim é depois da hora de início
        try {
            String horaInicio = editTextHoraInicio.getText().toString();
            String horaFim = editTextHoraFim.getText().toString();

            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            Date inicio = timeFormat.parse(horaInicio);
            Date fim = timeFormat.parse(horaFim);

            if (fim.before(inicio)) {
                Toast.makeText(requireContext(), "Hora de término deve ser depois da hora de início", Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Erro ao validar horários", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void processarDataEHora(RegistroParadaRequestDTO paradaRequest) throws Exception {
        String dataStr = editTextDATAPARADA.getText().toString();
        String horaInicioStr = editTextHoraInicio.getText().toString();
        String horaFimStr = editTextHoraFim.getText().toString();

        SimpleDateFormat combinedFormatter = new SimpleDateFormat("dd, MMM yyyy HH:mm", Locale.getDefault());
        combinedFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));

        // dt_parada (data + hora de início)
        String dataHoraInicioStr = dataStr + " " + horaInicioStr;
        Date dtParada = combinedFormatter.parse(dataHoraInicioStr);

        // hora_Inicio (data + hora de início)
        Date horaInicio = combinedFormatter.parse(dataHoraInicioStr);

        // hora_Fim (data + hora de fim)
        String dataHoraFimStr = dataStr + " " + horaFimStr;
        Date horaFim = combinedFormatter.parse(dataHoraFimStr);

        paradaRequest.setDt_parada(dtParada);
        paradaRequest.setHora_Inicio(horaInicio);
        paradaRequest.setHora_Fim(horaFim);
    }

    private void enviarParadaParaMongoDB(RegistroParadaRequestDTO paradaRequest) {
        btnAdicionarParada.setEnabled(false);
        btnAdicionarParada.setText("Salvando...");

        Call<ResponseBody> call = apiService.criarRegistro(paradaRequest);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                btnAdicionarParada.setEnabled(true);
                btnAdicionarParada.setText("Adicionar Parada");

                if (response.isSuccessful()) {
                    limparCampos();
                    Toast.makeText(requireContext(), "Parada salva com sucesso!", Toast.LENGTH_SHORT).show();
                    navigateBack();
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Erro desconhecido";
                        Log.e("AddParadaFragment", "Erro API: " + errorBody);
                        Toast.makeText(requireContext(), "Erro ao salvar parada: " + response.code(), Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(requireContext(), "Erro ao salvar parada", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                btnAdicionarParada.setEnabled(true);
                btnAdicionarParada.setText("Adicionar Parada");
                Log.e("AddParadaFragment", "Falha na conexão: " + t.getMessage());
                Toast.makeText(requireContext(), "Falha na conexão: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void limparCampos() {
        editIdMaquina.setText("");
        editNomeMaquina.setText("");
        editIdUsuario.setText("");
        editDescricaoParada.setText("");
        editTextHoraInicio.setText("");
        editTextHoraFim.setText("");
        maquinaValida = false;
        colaboradorValido = false;
        atualizarResumo();

        // Manter a data atual
        editTextDATAPARADA.setText(dateFormatter.format(calendar.getTime()));
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
}