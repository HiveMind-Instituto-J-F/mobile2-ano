package com.aula.mobile_hivemind.ui.home.paradas;

import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.aula.mobile_hivemind.MainActivity;
import com.aula.mobile_hivemind.R;
import com.aula.mobile_hivemind.api.RetrofitClient;
import com.aula.mobile_hivemind.dto.ManutencaoRequestDTO;
import com.aula.mobile_hivemind.dto.ManutencaoResponseDTO;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MaintenanceFragment extends Fragment {

    private ImageButton btnBack;
    private Button btnConcluir;
    private EditText editTextDATAMANUTENCAO, editTextHoraInicio, editTextHoraFim, editRelatorio, editIdFunc;
    private TextView txtIdMaquina;

    private com.aula.mobile_hivemind.api.SqlApiService apiManutencaoService;

    // Dados necessários
    private int idMaquina, idUsuario, idManutencista;
    private String setor;

    public MaintenanceFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_maintenance, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        apiManutencaoService = RetrofitClient.getSqlApiService();
        hideFab();
        inicializarViews(view);
        preencherDadosDaParada();
        configurarDatePicker();
        configurarTimePickers();
        configurarBotoes();
    }

    private void inicializarViews(View view) {
        btnBack = view.findViewById(R.id.btnBack);
        btnConcluir = view.findViewById(R.id.button_concluir_relatorio);
        editIdFunc = view.findViewById(R.id.editIdFunc);

        // Campos editáveis (manutenção)
        editTextDATAMANUTENCAO = view.findViewById(R.id.editTextDATAMANUTENCAO);
        editTextHoraInicio = view.findViewById(R.id.editTextHoraInicio);
        editTextHoraFim = view.findViewById(R.id.editTextHoraFim);
        editRelatorio = view.findViewById(R.id.editRelatorio);

        txtIdMaquina = view.findViewById(R.id.editNumeroSerie);
    }

    private void preencherDadosDaParada() {
        Bundle bundle = getArguments();
        if (bundle == null) return;

        idMaquina = bundle.getInt("idMaquina", 0);
        idUsuario = bundle.getInt("codigoColaborador", 0);
        setor = bundle.getString("setor", "");
        idManutencista = bundle.getInt("userId", 0);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String dataAtual = sdf.format(new Date());

        txtIdMaquina.setText(String.valueOf(idMaquina));
        editTextDATAMANUTENCAO.setText(dataAtual);

        // ✅ PREENCHER AUTOMATICAMENTE O ID DO FUNCIONÁRIO LOGADO
        editIdFunc.setText(String.valueOf(idManutencista));
        editIdFunc.setEnabled(false); // Torna o campo não editável
        editIdFunc.setFocusable(false); // Impede foco no campo
        txtIdMaquina.setEnabled(false);
        txtIdMaquina.setFocusable(false);


        Log.d("MaintenanceFragment", "Dados recebidos - ID Máquina: " + idMaquina +
                ", ID Usuário: " + idUsuario + ", Setor: " + setor);
    }

    private void configurarDatePicker() {
        editTextDATAMANUTENCAO.setFocusable(false);
        editTextDATAMANUTENCAO.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                mostrarDatePicker();
                return true;
            }
            return false;
        });
    }

    private void configurarTimePickers() {
        editTextHoraInicio.setOnClickListener(v -> mostrarTimePicker(editTextHoraInicio));
        editTextHoraFim.setOnClickListener(v -> mostrarTimePicker(editTextHoraFim));
    }

    private void mostrarDatePicker() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        long today = calendar.getTimeInMillis();

        CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder();
        constraintsBuilder.setEnd(today);

        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Selecione a data do reparo")
                .setCalendarConstraints(constraintsBuilder.build())
                .build();

        datePicker.show(getParentFragmentManager(), "DATE_PICKER");

        datePicker.addOnPositiveButtonClickListener(selection -> {
            if (selection > today) {
                Toast.makeText(requireContext(), "Não é possível adicionar datas futuras", Toast.LENGTH_SHORT).show();
                return;
            }

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            String selectedDate = sdf.format(new Date(selection));
            editTextDATAMANUTENCAO.setText(selectedDate);
        });
    }

    private void mostrarTimePicker(EditText targetEditText) {
        final Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePicker = new TimePickerDialog(getContext(),
                (view, selectedHour, selectedMinute) -> {
                    String horaFormatada = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute);
                    targetEditText.setText(horaFormatada);
                }, hour, minute, true);

        timePicker.show();
    }

    private void configurarBotoes() {
        btnBack.setOnClickListener(v -> navigateBack());
        btnConcluir.setOnClickListener(v -> salvarManutencao());
    }

    private void salvarManutencao() {
        if (!validarCampos()) return;

        String dataStr = editTextDATAMANUTENCAO.getText().toString().trim();
        String horaInicioStr = editTextHoraInicio.getText().toString().trim();
        String horaFimStr = editTextHoraFim.getText().toString().trim();
        String relatorio = editRelatorio.getText().toString().trim();

        if (!isValidTimeFormat(horaInicioStr) || !isValidTimeFormat(horaFimStr)) {
            Toast.makeText(requireContext(), "Formato de hora inválido. Use HH:mm", Toast.LENGTH_SHORT).show();
            return;
        }

        if (idManutencista == 0) {
            Toast.makeText(requireContext(), "ID do usuário inválido!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Converter para os tipos corretos
        java.sql.Date data = parseSqlDate(dataStr);
        Time horaInicio = parseTime(horaInicioStr);
        Time horaFim = parseTime(horaFimStr);

        // Criar DTO para manutenção
        ManutencaoRequestDTO manutencaoRequest = new ManutencaoRequestDTO(
                idManutencista,    // id_usuario (agora é o ID do usuário logado)
                idMaquina,         // id_maquina
                data,              // data
                setor,             // setor
                horaInicio,        // hora_inicio
                horaFim,           // hora_fim
                relatorio          // descricao
        );

        Gson gson = new Gson();
        String jsonRequest = gson.toJson(manutencaoRequest);
        Log.d("MaintenanceFragment", "JSON enviado: " + jsonRequest);

        btnConcluir.setEnabled(false);
        btnConcluir.setText("Salvando...");

        apiManutencaoService.inserirManutencao(manutencaoRequest)
                .enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                        btnConcluir.setEnabled(true);
                        btnConcluir.setText("CONCLUIR RELATÓRIO");

                        if (response.isSuccessful() && response.body() != null) {
                            Log.d("MaintenanceFragment", "Resposta body: " + response.body());

                            // ✅ EXTRAIR O ID DA MANUTENÇÃO
                            int idManutencao = extrairIdManutencao(response.body());

                            if (idManutencao != -1) {
                                // ✅ CHAMAR FINALIZAÇÃO DA PARADA APÓS MANUTENÇÃO SER INSERIDA
                                finalizarParadaAposManutencao(idManutencao);
                            } else {
                                Toast.makeText(requireContext(), "Manutenção salva, mas não foi possível obter o ID", Toast.LENGTH_LONG).show();
                                navigateToConfirmation();
                            }

                        } else {
                            String errorMessage = "Erro ao salvar manutenção: " + response.code();
                            if (response.errorBody() != null) {
                                try {
                                    String errorBody = response.errorBody().string();
                                    errorMessage += " - " + errorBody;
                                } catch (Exception e) {
                                    Log.e("MaintenanceFragment", "Erro ao ler errorBody", e);
                                }
                            }
                            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                        btnConcluir.setEnabled(true);
                        btnConcluir.setText("CONCLUIR RELATÓRIO");
                        Toast.makeText(requireContext(), "Falha na conexão: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    // ✅ MÉTODO PARA FINALIZAR PARADA APÓS MANUTENÇÃO
    private void finalizarParadaAposManutencao(int idManutencao) {
        String idMongo = getArguments().getString("idMongo", "");

        if (idMongo.isEmpty()) {
            Log.e("MaintenanceFragment", "ID MongoDB não encontrado");
            Toast.makeText(requireContext(), "Manutenção salva, mas parada não foi finalizada (ID não encontrado)", Toast.LENGTH_LONG).show();
            navigateToConfirmation();
            return;
        }

        ProgressDialog progressDialog = new ProgressDialog(requireContext());
        progressDialog.setMessage("Finalizando parada...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // ✅ CHAMAR API PARA EXCLUIR PARADA DO MONGODB
        Call<ResponseBody> call = RetrofitClient.getApiService().excluirRegistro(idMongo);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                progressDialog.dismiss();

                if (response.isSuccessful()) {
                    Log.d("MaintenanceFragment", "Parada finalizada com sucesso! ID Manutenção: " + idManutencao);
                    Toast.makeText(requireContext(),
                            "Manutenção registrada e parada finalizada! ID: " + idManutencao,
                            Toast.LENGTH_LONG).show();

                    // ✅ NAVEGAR PARA CONFIRMAÇÃO COM OS DADOS
                    navigateToConfirmationWithData(idManutencao);

                } else {
                    Log.e("MaintenanceFragment", "Erro ao finalizar parada: " + response.code());
                    Toast.makeText(requireContext(),
                            "Manutenção salva (ID: " + idManutencao + "), mas erro ao finalizar parada: " + response.code(),
                            Toast.LENGTH_LONG).show();
                    navigateToConfirmationWithData(idManutencao);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                progressDialog.dismiss();
                Log.e("MaintenanceFragment", "Falha ao finalizar parada: " + t.getMessage());
                Toast.makeText(requireContext(),
                        "Manutenção salva (ID: " + idManutencao + "), mas falha ao finalizar parada: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
                navigateToConfirmationWithData(idManutencao);
            }
        });
    }

    // ✅ NAVEGAÇÃO PARA CONFIRMAÇÃO COM DADOS
    private void navigateToConfirmationWithData(int idManutencao) {
        Bundle bundle = new Bundle();
        bundle.putInt("idManutencao", idManutencao);
        bundle.putString("mensagem", "Manutenção registrada com sucesso! ID: " + idManutencao);

        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
        navController.navigate(R.id.confirmationFragment, bundle);
    }

    private void navigateToConfirmation() {
        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
        navController.navigate(R.id.confirmationFragment);
    }

    private int extrairIdManutencao(String responseBody) {
        try {
            Log.d("MaintenanceFragment", "Tentando extrair ID de: " + responseBody);

            // Tentar parsear como JSON primeiro
            try {
                Gson gson = new Gson();
                JsonObject jsonObject = gson.fromJson(responseBody, JsonObject.class);
                if (jsonObject != null) {
                    if (jsonObject.has("id")) {
                        return jsonObject.get("id").getAsInt();
                    }
                    if (jsonObject.has("idManutencao")) {
                        return jsonObject.get("idManutencao").getAsInt();
                    }
                }
            } catch (Exception e) {
                Log.d("MaintenanceFragment", "Resposta não é JSON válido, tentando extrair de texto");
            }

            // ✅ CORREÇÃO: Melhorar a extração do ID do texto
            // Para resposta: "A manutenção foi inserida com sucesso! ID: 1251"

            // Método 1: Buscar por padrão "ID: número"
            if (responseBody.contains("ID:") || responseBody.contains("Id:") || responseBody.contains("id:")) {
                String[] parts = responseBody.split("ID:|Id:|id:");
                if (parts.length > 1) {
                    // Pegar apenas números da segunda parte
                    String idStr = parts[1].replaceAll("[^0-9]", "").trim();
                    if (!idStr.isEmpty()) {
                        return Integer.parseInt(idStr);
                    }
                }
            }

            // Método 2: Buscar o último número na string (mais robusto)
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\d+");
            java.util.regex.Matcher matcher = pattern.matcher(responseBody);

            // Buscar todos os números e pegar o último (que deve ser o ID)
            int lastId = -1;
            while (matcher.find()) {
                try {
                    lastId = Integer.parseInt(matcher.group());
                } catch (NumberFormatException e) {
                    // Ignorar se não for número válido
                }
            }

            if (lastId != -1) {
                Log.d("MaintenanceFragment", "ID extraído: " + lastId);
                return lastId;
            }

            Log.w("MaintenanceFragment", "Não foi possível extrair ID da resposta: " + responseBody);
            return -1;

        } catch (Exception e) {
            Log.e("MaintenanceFragment", "Erro ao extrair ID da manutenção", e);
            return -1;
        }
    }

    private boolean isValidTimeFormat(String timeString) {
        return timeString.matches("^([01]?[0-9]|2[0-3]):[0-5][0-9]$");
    }

    private java.sql.Date parseSqlDate(String dateString) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date utilDate = sdf.parse(dateString);
            return new java.sql.Date(utilDate.getTime());
        } catch (Exception e) {
            Log.e("MaintenanceFragment", "Erro ao converter data SQL: " + dateString, e);
            return new java.sql.Date(System.currentTimeMillis());
        }
    }

    private Time parseTime(String timeString) {
        try {
            String cleanTime = timeString.trim().replaceAll("(?i)\\s*[AP]M$", "").trim();
            if (cleanTime.length() == 5) cleanTime += ":00";
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
            sdf.setLenient(false);
            java.util.Date utilDate = sdf.parse(cleanTime);
            return new Time(utilDate.getTime());
        } catch (Exception e) {
            Log.e("MaintenanceFragment", "Erro ao converter hora SQL: " + timeString, e);
            return new Time(System.currentTimeMillis());
        }
    }

    private void hideFab() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setFabVisibility(false);
            ((MainActivity) getActivity()).setBottomNavigationVisibility(false);
        }
    }

    private boolean validarCampos() {
        return !editTextDATAMANUTENCAO.getText().toString().trim().isEmpty()
                && !editTextHoraInicio.getText().toString().trim().isEmpty()
                && !editTextHoraFim.getText().toString().trim().isEmpty()
                && !editRelatorio.getText().toString().trim().isEmpty();
    }

    private void navigateBack() {
        if (getActivity() != null) {
            getActivity().onBackPressed();
        }
    }
}