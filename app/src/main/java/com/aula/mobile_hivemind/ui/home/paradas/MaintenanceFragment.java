package com.aula.mobile_hivemind.ui.home.paradas;

import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.aula.mobile_hivemind.MainActivity;
import com.aula.mobile_hivemind.R;
import com.aula.mobile_hivemind.api.RetrofitClient;
import com.aula.mobile_hivemind.dto.ManutencaoRequestDTO;
import com.aula.mobile_hivemind.dto.ParadaSQLRequestDTO;
import com.aula.mobile_hivemind.utils.CustomToast;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
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

    private ImageButton btnVoltar;
    private Button btnConcluir;

    private TextInputEditText editTextDATAMANUTENCAO, editTextHoraInicio, editTextHoraFim, editRelatorio, editIdFunc;
    private TextInputEditText editNumeroSerie;

    private com.aula.mobile_hivemind.api.SqlApiService apiSQLService;

    // Dados necessários
    private int idMaquina, idUsuario, idManutencista;
    private String setor;

    private String dataParada;
    private String horaInicio, horaFim;

    public MaintenanceFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_maintenance, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        apiSQLService = RetrofitClient.getSqlApiService();
        hideFab();
        inicializarViews(view);
        preencherDadosDaParada();
        configurarDatePicker();
        configurarTimePickers();
        configurarBotoes();
    }

    private void inicializarViews(View view) {
        btnVoltar = view.findViewById(R.id.btnVoltar);
        btnConcluir = view.findViewById(R.id.button_concluir_relatorio);

        editIdFunc = view.findViewById(R.id.editIdFunc);
        editTextDATAMANUTENCAO = view.findViewById(R.id.editTextDATAMANUTENCAO);
        editTextHoraInicio = view.findViewById(R.id.editTextHoraInicio);
        editTextHoraFim = view.findViewById(R.id.editTextHoraFim);
        editRelatorio = view.findViewById(R.id.editRelatorio);
        editNumeroSerie = view.findViewById(R.id.editNumeroSerie);
    }

    private void preencherDadosDaParada() {
        Bundle bundle = getArguments();
        if (bundle == null) return;

        idMaquina = bundle.getInt("idMaquina", 0);
        idUsuario = bundle.getInt("codigoColaborador", 0);
        setor = bundle.getString("setor", "");
        idManutencista = bundle.getInt("userId", 0);
        dataParada = bundle.getString("dataParada", "");
        horaInicio = bundle.getString("horaInicio", "");
        horaFim = bundle.getString("horaFim", "");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String dataAtual = sdf.format(new Date());

        editNumeroSerie.setText(String.valueOf(idMaquina));
        editTextDATAMANUTENCAO.setText(dataAtual);

        editIdFunc.setText(String.valueOf(idManutencista));
        editIdFunc.setEnabled(false);
        editIdFunc.setFocusable(false);
        editNumeroSerie.setEnabled(false);
        editNumeroSerie.setFocusable(false);
    }

    private void configurarDatePicker() {
        if (editTextDATAMANUTENCAO != null) {
            editTextDATAMANUTENCAO.setFocusable(false);
            editTextDATAMANUTENCAO.setOnClickListener(v -> mostrarDatePicker());
        }
    }

    private void configurarTimePickers() {
        if (editTextHoraInicio != null) {
            editTextHoraInicio.setOnClickListener(v -> mostrarTimePicker(editTextHoraInicio));
        }

        if (editTextHoraFim != null) {
            editTextHoraFim.setOnClickListener(v -> mostrarTimePicker(editTextHoraFim));
        }
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
                CustomToast.showWarning(requireContext(), "Não é possível adicionar datas futuras");
                return;
            }

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            String selectedDate = sdf.format(new Date(selection));
            editTextDATAMANUTENCAO.setText(selectedDate);
        });
    }

    private void mostrarTimePicker(TextInputEditText targetEditText) {
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
        btnVoltar.setOnClickListener(v -> navigateBack());
        btnConcluir.setOnClickListener(v -> salvarManutencao());
    }

    private void salvarManutencao() {
        if (!validarCampos()) return;

        String dataStr = editTextDATAMANUTENCAO.getText().toString().trim();
        String horaInicioStr = editTextHoraInicio.getText().toString().trim();
        String horaFimStr = editTextHoraFim.getText().toString().trim();
        String relatorio = editRelatorio.getText().toString().trim();

        if (!isValidTimeFormat(horaInicioStr) || !isValidTimeFormat(horaFimStr)) {
            CustomToast.showError(requireContext(), "Formato de hora inválido. Use HH:mm");
            return;
        }

        if (idManutencista == 0) {
            CustomToast.showError(requireContext(), "ID do usuário inválido!");
            return;
        }

        java.sql.Date data = parseSqlDate(dataStr);
        Time horaInicio = parseTime(horaInicioStr);
        Time horaFim = parseTime(horaFimStr);

        ManutencaoRequestDTO manutencaoRequest = new ManutencaoRequestDTO(
                idManutencista,
                idMaquina,
                data,
                setor,
                horaInicio,
                horaFim,
                relatorio
        );

        btnConcluir.setEnabled(false);
        btnConcluir.setText("Salvando...");

        apiSQLService.inserirManutencao(manutencaoRequest)
                .enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                        btnConcluir.setEnabled(true);
                        btnConcluir.setText("CONCLUIR RELATÓRIO");

                        if (response.isSuccessful() && response.body() != null) {
                            int idManutencao = extrairIdManutencao(response.body());
                            if (idManutencao != -1) {
                                inserirParadaComProcedure(idManutencao);
                            } else {
                                CustomToast.showWarning(requireContext(), "Manutenção salva, mas não foi possível obter o ID");
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
                            CustomToast.showError(requireContext(), errorMessage);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                        btnConcluir.setEnabled(true);
                        btnConcluir.setText("CONCLUIR RELATÓRIO");
                        CustomToast.showError(requireContext(), "Falha na conexão: " + t.getMessage());
                    }
                });
    }

    private void inserirParadaComProcedure(int idManutencao) {
        String descricaoParada = "Parada para manutenção - ID: " + idManutencao;

        Time horaInicioTime = parseTimeFromString(horaInicio);
        Time horaFimTime = parseTimeFromString(horaFim);
        Date dateParada = parseDateFromString(dataParada);

        ParadaSQLRequestDTO paradaProcedure = new ParadaSQLRequestDTO(
                idManutencao,
                idMaquina,
                idUsuario,
                descricaoParada,
                setor,
                dateParada,
                horaInicioTime,
                horaFimTime
        );

        apiSQLService.inserirParada(paradaProcedure)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            finalizarParadaAposManutencao(idManutencao);
                        } else {
                            CustomToast.showWarning(requireContext(),
                                    "Manutenção salva (ID: " + idManutencao + "), mas erro ao registrar parada: " + response.code());
                            finalizarParadaAposManutencao(idManutencao);
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Log.e("MaintenanceFragment", "Falha na procedure: " + t.getMessage());
                        CustomToast.showError(requireContext(),
                                "Manutenção salva (ID: " + idManutencao + "), mas falha ao registrar parada: " + t.getMessage());
                        finalizarParadaAposManutencao(idManutencao);
                    }
                });
    }

    private void finalizarParadaAposManutencao(int idManutencao) {
        String idMongo = getArguments().getString("idMongo", "");

        if (idMongo.isEmpty()) {
            Log.e("MaintenanceFragment", "ID MongoDB não encontrado");
            CustomToast.showWarning(requireContext(), "Manutenção salva, mas parada não foi finalizada (ID não encontrado)");
            navigateToConfirmation();
            return;
        }

        ProgressDialog progressDialog = new ProgressDialog(requireContext());
        progressDialog.setMessage("Finalizando parada...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        Call<ResponseBody> call = RetrofitClient.getApiService().excluirRegistro(idMongo);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                progressDialog.dismiss();

                if (response.isSuccessful()) {
                    CustomToast.showSuccess(requireContext(),
                            "Manutenção registrada e parada finalizada! ID: " + idManutencao);
                    navigateToConfirmation();
                } else {
                    CustomToast.showWarning(requireContext(),
                            "Manutenção salva (ID: " + idManutencao + "), mas erro ao finalizar parada: " + response.code());
                    navigateToConfirmation();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                progressDialog.dismiss();
                CustomToast.showError(requireContext(),
                        "Manutenção salva (ID: " + idManutencao + "), mas falha ao finalizar parada: " + t.getMessage());
                navigateToConfirmation();
            }
        });
    }

    private Time parseTimeFromString(String timeString) {
        if (timeString == null || timeString.isEmpty()) {
            return new Time(0);
        }

        try {
            if (timeString.length() == 8 && timeString.matches("\\d{2}:\\d{2}:\\d{2}")) {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
                java.util.Date utilDate = sdf.parse(timeString);
                return new Time(utilDate.getTime());
            }

            if (timeString.length() == 5 && timeString.matches("\\d{2}:\\d{2}")) {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                java.util.Date utilDate = sdf.parse(timeString);
                return new Time(utilDate.getTime());
            }

            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
            java.util.Date utilDate = sdf.parse(timeString);
            return new Time(utilDate.getTime());

        } catch (Exception e) {
            Log.e("MaintenanceFragment", "Erro ao converter hora: " + timeString, e);
            return new Time(0);
        }
    }

    private java.sql.Date parseDateFromString(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            Log.w("MaintenanceFragment", "Data nula ou vazia, usando data atual");
            return new java.sql.Date(System.currentTimeMillis());
        }

        try {
            if (dateString.length() == 10 && dateString.matches("\\d{4}-\\d{2}-\\d{2}")) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                java.util.Date utilDate = sdf.parse(dateString);
                return new java.sql.Date(utilDate.getTime());
            }

            if (dateString.length() == 10 && dateString.matches("\\d{2}/\\d{2}/\\d{4}")) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                java.util.Date utilDate = sdf.parse(dateString);
                return new java.sql.Date(utilDate.getTime());
            }

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            java.util.Date utilDate = sdf.parse(dateString);
            return new java.sql.Date(utilDate.getTime());

        } catch (Exception e) {
            Log.e("MaintenanceFragment", "Erro ao converter data: " + dateString, e);
            return new java.sql.Date(System.currentTimeMillis());
        }
    }

    private void navigateToConfirmation() {
        try {
            Bundle bundle = new Bundle();

            // Passar dados para a notificação
            bundle.putString("nomeEngenheiro", "Engenheiro");
            bundle.putString("nomeMaquina", "Máquina " + idMaquina);
            bundle.putString("nomeUsuarioCriador", "Usuário");
            bundle.putString("dataParada", dataParada);
            bundle.putString("horaInicio", horaInicio);
            bundle.putString("horaFim", editTextHoraFim.getText().toString().trim());

            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
            navController.navigate(R.id.confirmationMaintenceFragment, bundle);

        } catch (Exception e) {
            // Fallback em caso de erro
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
            navController.navigate(R.id.confirmationMaintenceFragment);
        }
    }

    private int extrairIdManutencao(String responseBody) {
        try {
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

            if (responseBody.contains("ID:") || responseBody.contains("Id:") || responseBody.contains("id:")) {
                String[] parts = responseBody.split("ID:|Id:|id:");
                if (parts.length > 1) {
                    String idStr = parts[1].replaceAll("[^0-9]", "").trim();
                    if (!idStr.isEmpty()) {
                        return Integer.parseInt(idStr);
                    }
                }
            }

            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\d+");
            java.util.regex.Matcher matcher = pattern.matcher(responseBody);

            int lastId = -1;
            while (matcher.find()) {
                try {
                    lastId = Integer.parseInt(matcher.group());
                } catch (NumberFormatException e) {
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
        if (editTextDATAMANUTENCAO.getText().toString().trim().isEmpty()) {
            CustomToast.showWarning(requireContext(), "Data da manutenção é obrigatória");
            return false;
        }
        if (editTextHoraInicio.getText().toString().trim().isEmpty()) {
            CustomToast.showWarning(requireContext(), "Hora de início é obrigatória");
            return false;
        }
        if (editTextHoraFim.getText().toString().trim().isEmpty()) {
            CustomToast.showWarning(requireContext(), "Hora de término é obrigatória");
            return false;
        }
        if (editRelatorio.getText().toString().trim().isEmpty()) {
            CustomToast.showWarning(requireContext(), "Relatório da manutenção é obrigatório");
            return false;
        }
        return true;
    }

    private void navigateBack() {
        if (getActivity() != null) {
            getActivity().onBackPressed();
        }
    }
}