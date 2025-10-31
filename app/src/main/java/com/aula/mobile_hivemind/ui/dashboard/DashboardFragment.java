package com.aula.mobile_hivemind.ui.dashboard;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aula.mobile_hivemind.R;
import com.aula.mobile_hivemind.api.RetrofitClient;
import com.aula.mobile_hivemind.dto.RegistroParadaResponseDTO;
import com.aula.mobile_hivemind.databinding.FragmentDashboardBinding;
import com.aula.mobile_hivemind.ui.dashboard.itens.ProgressBarAdapter;
import com.aula.mobile_hivemind.ui.dashboard.itens.ProgressItem;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardFragment extends Fragment {

    TextView textResumoTitulo, textResumoNumero, textData, txtPorcent;
    private FragmentDashboardBinding binding;
    private PieChart pieChart;
    private RecyclerView recyclerViewProgressBars;
    private com.aula.mobile_hivemind.api.ApiService apiService;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);

        textResumoNumero = binding.textResumoNumero;
        textResumoTitulo = binding.textResumoTitulo;
        textData = binding.textData;
        txtPorcent = binding.txtPorcent;

        apiService = RetrofitClient.getApiService();

        pieChart = binding.pieChart;
        recyclerViewProgressBars = binding.recyclerViewProgressBars;

        atualizarDataAtual();

        atualizarResumo();

        setupPieChart();
        setupProgressBars();

        return binding.getRoot();
    }

    private void atualizarDataAtual() {
        LocalDate hoje = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM 'de' yyyy", new Locale("pt", "BR"));
        String mesAnoFormatado = hoje.format(formatter);

        mesAnoFormatado = mesAnoFormatado.substring(0, 1).toUpperCase() + mesAnoFormatado.substring(1);

        textData.setText(mesAnoFormatado);
    }

    private void atualizarResumo() {
        buscarParadasDoMesAtual(new ParadasCallback() {
            @Override
            public void onParadasLoaded(int paradasMesAtual, int paradasMesAnterior) {
                textResumoNumero.setText(String.valueOf(paradasMesAtual));

                double porcentagem;
                if (paradasMesAnterior != 0) {
                    porcentagem = ((double)(paradasMesAtual - paradasMesAnterior) / paradasMesAnterior) * 100;
                } else {
                    porcentagem = paradasMesAtual > 0 ? 100 : 0;
                }

                if (porcentagem >= 0) {
                    txtPorcent.setText("cerca de " + String.format("%.1f%%", porcentagem) + " a mais em relação ao mês anterior");
                } else {
                    txtPorcent.setText("cerca de " + String.format("%.1f%%", Math.abs(porcentagem)) + " a menos em relação ao mês anterior");
                }
            }

            @Override
            public void onError(String error) {
                Log.e("DashboardFragment", "Erro ao carregar paradas: " + error);
                textResumoNumero.setText("0");
                txtPorcent.setText("Erro ao carregar dados");
            }
        });
    }

    interface ParadasCallback {
        void onParadasLoaded(int paradasMesAtual, int paradasMesAnterior);
        void onError(String error);
    }

    private void buscarParadasDoMesAtual(ParadasCallback callback) {
        Call<List<RegistroParadaResponseDTO>> call = apiService.getAllRegistros();
        call.enqueue(new Callback<List<RegistroParadaResponseDTO>>() {
            @Override
            public void onResponse(Call<List<RegistroParadaResponseDTO>> call, Response<List<RegistroParadaResponseDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<RegistroParadaResponseDTO> todasParadas = response.body();

                    Calendar cal = Calendar.getInstance();
                    int mesAtual = cal.get(Calendar.MONTH);
                    int anoAtual = cal.get(Calendar.YEAR);

                    cal.add(Calendar.MONTH, -1);
                    int mesAnterior = cal.get(Calendar.MONTH);
                    int anoAnterior = cal.get(Calendar.YEAR);

                    int contadorMesAtual = 0;
                    int contadorMesAnterior = 0;

                    for (RegistroParadaResponseDTO parada : todasParadas) {
                        try {
                            if (parada.getDt_parada() != null) {
                                Date dataParadaDate = parada.getDt_parada();
                                Calendar dataParadaCal = Calendar.getInstance();
                                dataParadaCal.setTime(dataParadaDate);

                                int mesParada = dataParadaCal.get(Calendar.MONTH);
                                int anoParada = dataParadaCal.get(Calendar.YEAR);

                                // Verificar se é do mês atual
                                if (mesParada == mesAtual && anoParada == anoAtual) {
                                    contadorMesAtual++;
                                }
                                // Verificar se é do mês anterior
                                else if (mesParada == mesAnterior && anoParada == anoAnterior) {
                                    contadorMesAnterior++;
                                }
                            }
                        } catch (Exception e) {
                            Log.e("DashboardFragment", "Erro ao processar data da parada ID: " + parada.getId(), e);
                        }
                    }

                    Log.d("DashboardFragment", "Paradas mês atual: " + contadorMesAtual + ", mês anterior: " + contadorMesAnterior);
                    callback.onParadasLoaded(contadorMesAtual, contadorMesAnterior);

                } else {
                    callback.onError("Erro na resposta: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<RegistroParadaResponseDTO>> call, Throwable t) {
                callback.onError("Falha na conexão: " + t.getMessage());
            }
        });
    }

    private void setupPieChart() {
        // Dados fictícios baseados na imagem
        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(20f, "Setor A"));
        entries.add(new PieEntry(18f, "Setor B"));
        entries.add(new PieEntry(16f, "Setor C"));
        entries.add(new PieEntry(14f, "Setor D"));
        entries.add(new PieEntry(12f, "Setor E"));

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);

        // Cores personalizadas
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.parseColor("#EF4444")); // Vermelho
        colors.add(Color.parseColor("#EC2B7B")); // Rosa
        colors.add(Color.parseColor("#B68DF6")); // Roxo
        colors.add(Color.parseColor("#8059E7")); // Azul escuro
        colors.add(Color.parseColor("#4C65F1")); // Azul
        colors.add(Color.parseColor("#26C8D8")); // Ciano
        colors.add(Color.parseColor("#43D8B0")); // Verde claro
        colors.add(Color.parseColor("#82E762")); // Verde
        colors.add(Color.parseColor("#FFD95D")); // Amarelo

        dataSet.setColors(colors);

        PieData pieData = new PieData(dataSet);
        pieData.setValueFormatter(new PercentFormatter(pieChart));
        pieData.setValueTextSize(11f);
        pieData.setValueTextColor(Color.WHITE);

        // Configurações do gráfico
        pieChart.setData(pieData);
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setHoleColor(Color.TRANSPARENT);
        pieChart.setHoleRadius(58f); // Raio interno para o buraco (donut)
        pieChart.setTransparentCircleRadius(61f);
        pieChart.setDrawCenterText(true);
        pieChart.setCenterText("$2 570");
        pieChart.setCenterTextColor(Color.WHITE);
        pieChart.setCenterTextSize(24f);

        // Remover a legenda padrão
        Legend legend = pieChart.getLegend();
        legend.setEnabled(false);

        pieChart.invalidate(); // Atualiza o gráfico
    }

    private void setupProgressBars() {
        List<ProgressItem> progressItems = new ArrayList<>();
        // Dados fictícios baseados na imagem
        progressItems.add(new ProgressItem("Setor A", 25, R.color.red_500));
        progressItems.add(new ProgressItem("Setor B", 22, R.color.pink_500));
        progressItems.add(new ProgressItem("Setor C", 20, R.color.purple_500));
        progressItems.add(new ProgressItem("Setor D", 17, R.color.indigo_500));
        progressItems.add(new ProgressItem("Setor E", 15, R.color.blue_500));

        recyclerViewProgressBars.setLayoutManager(new LinearLayoutManager(getContext()));

        // Define o adapter com os dados fictícios
        ProgressBarAdapter adapter = new ProgressBarAdapter(progressItems);
        recyclerViewProgressBars.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        atualizarResumo();
    }
}
