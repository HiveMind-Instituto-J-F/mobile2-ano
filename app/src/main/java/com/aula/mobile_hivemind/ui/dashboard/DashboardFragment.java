package com.aula.mobile_hivemind.ui.dashboard;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.aula.mobile_hivemind.R;
import com.aula.mobile_hivemind.api.RetrofitClient;
import com.aula.mobile_hivemind.dto.MaquinaResponseDTO;
import com.aula.mobile_hivemind.dto.MetricasParadaDTO;
import com.aula.mobile_hivemind.dto.ParadaSQLResponseDTO;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardFragment extends Fragment {

    private ProgressBar progressBar;
    private TextView textTotalParadas, textTempoMedio, textMaquinaProblema, textSetorProblema;
    private LineChart lineChartEvolucao;
    private TextView textTituloDashboard;

    private com.aula.mobile_hivemind.api.SqlApiService sqlApiService;
    private SharedPreferences sharedPreferences;
    private String userType;
    private String userSetor;
    private List<MaquinaResponseDTO> listaMaquinas;

    public DashboardFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        sqlApiService = RetrofitClient.getSqlApiService();
        sharedPreferences = requireContext().getSharedPreferences("ProfilePrefs", 0);
        userType = sharedPreferences.getString("user_type", "regular");
        userSetor = sharedPreferences.getString("user_setor", "");
        listaMaquinas = new ArrayList<>();

        initViews(view);
        carregarMaquinasEDadosDashboard();

        return view;
    }

    private void initViews(View view) {
        progressBar = view.findViewById(R.id.progressBar);
        textTotalParadas = view.findViewById(R.id.textTotalParadas);
        textTempoMedio = view.findViewById(R.id.textTempoMedio);
        textMaquinaProblema = view.findViewById(R.id.textMaquinaProblema);
        textSetorProblema = view.findViewById(R.id.textSetorProblema);

        lineChartEvolucao = view.findViewById(R.id.lineChartEvolucao);
        textTituloDashboard = view.findViewById(R.id.textTituloDashboard);

        configurarGrafico();

        // Configurar título baseado no tipo de usuário
        if (textTituloDashboard != null) {
            if ("regular".equals(userType)) {
                textTituloDashboard.setText("Dashboard - Setor " + userSetor);
            } else if ("MOP".equals(userType)) {
                textTituloDashboard.setText("Dashboard - Todos os Setores");
            }
        }
    }

    private void configurarGrafico() {
        configurarLineChart(lineChartEvolucao);
    }

    private void carregarMaquinasEDadosDashboard() {
        progressBar.setVisibility(View.VISIBLE);

        // Primeiro carrega as máquinas para depois carregar as paradas
        Call<List<MaquinaResponseDTO>> callMaquinas = sqlApiService.listarMaquinas();
        callMaquinas.enqueue(new Callback<List<MaquinaResponseDTO>>() {
            @Override
            public void onResponse(Call<List<MaquinaResponseDTO>> call, Response<List<MaquinaResponseDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaMaquinas = response.body();
                    Log.d("Dashboard", "Máquinas carregadas: " + listaMaquinas.size());
                }
                // Agora carrega as paradas
                carregarDadosDashboard();
            }

            @Override
            public void onFailure(Call<List<MaquinaResponseDTO>> call, Throwable t) {
                Log.e("Dashboard", "Falha ao carregar máquinas: " + t.getMessage());
                // Mesmo assim tenta carregar as paradas
                carregarDadosDashboard();
            }
        });
    }

    private void carregarDadosDashboard() {
        Log.d("Dashboard", "Iniciando carregamento de dados do SQL...");

        Call<List<ParadaSQLResponseDTO>> call = sqlApiService.listarTodasParadas();
        call.enqueue(new Callback<List<ParadaSQLResponseDTO>>() {
            @Override
            public void onResponse(Call<List<ParadaSQLResponseDTO>> call, Response<List<ParadaSQLResponseDTO>> response) {
                progressBar.setVisibility(View.GONE);
                Log.d("Dashboard", "Resposta recebida do SQL. Sucesso: " + response.isSuccessful());

                if (response.isSuccessful() && response.body() != null) {
                    List<ParadaSQLResponseDTO> paradas = response.body();
                    Log.d("Dashboard", "Total de paradas finalizadas recebidas: " + paradas.size());

                    // Filtrar paradas baseado no tipo de usuário
                    List<ParadaSQLResponseDTO> paradasFiltradas = filtrarParadasPorUsuario(paradas);
                    Log.d("Dashboard", "Paradas após filtro: " + paradasFiltradas.size());

                    if (paradasFiltradas.isEmpty()) {
                        Log.w("Dashboard", "Nenhuma parada finalizada encontrada após filtro");
                        mostrarDadosDemo(); // Mostrar dados de demonstração
                        return;
                    }

                    MetricasParadaDTO metricas = calcularMetricas(paradasFiltradas);
                    atualizarUI(metricas);
                } else {
                    mostrarDadosDemo();
                }
            }

            @Override
            public void onFailure(Call<List<ParadaSQLResponseDTO>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e("Dashboard", "Falha na requisição do SQL: " + t.getMessage());
                mostrarDadosDemo(); // Mostrar dados de demonstração em caso de falha
            }
        });
    }

    private void mostrarDadosDemo() {
        // Dados de demonstração para testes
        Log.d("Dashboard", "Mostrando dados de demonstração");

        // Atualizar UI com dados demo
        textTotalParadas.setText("15");
        textTempoMedio.setText("32.5 min");
        textMaquinaProblema.setText("Prensa Hidráulica");
        textSetorProblema.setText("Produção");

        // Dados demo para gráfico de evolução
        Map<String, Integer> evolucaoDemo = new LinkedHashMap<>();
        evolucaoDemo.put("Sem 1", 3);
        evolucaoDemo.put("Sem 2", 5);
        evolucaoDemo.put("Sem 3", 4);
        evolucaoDemo.put("Sem 4", 3);

        atualizarGraficoEvolucao(evolucaoDemo);
    }

    private List<ParadaSQLResponseDTO> filtrarParadasPorUsuario(List<ParadaSQLResponseDTO> paradas) {
        List<ParadaSQLResponseDTO> paradasFiltradas = new ArrayList<>();

        if ("regular".equals(userType)) {
            // Operador: apenas paradas do próprio setor
            for (ParadaSQLResponseDTO parada : paradas) {
                if (parada.getDes_setor() != null && parada.getDes_setor().equals(userSetor)) {
                    paradasFiltradas.add(parada);
                }
            }
        } else {
            // Engenheiro: todas as paradas
            paradasFiltradas.addAll(paradas);
        }

        return paradasFiltradas;
    }

    private MetricasParadaDTO calcularMetricas(List<ParadaSQLResponseDTO> paradas) {
        MetricasParadaDTO metricas = new MetricasParadaDTO();

        // Filtra paradas do mês atual
        List<ParadaSQLResponseDTO> paradasMes = filtrarParadasDoMes(paradas);

        metricas.setTotalParadasMes(paradasMes.size());
        metricas.setTempoMedioParadaMinutos(calcularTempoMedio(paradasMes));
        metricas.setMaquinaComMaisParadas(identificarMaquinaComMaisParadas(paradasMes));
        metricas.setSetorComMaisParadas(identificarSetorComMaisParadas(paradasMes));
        metricas.setEvolucaoMensal(calcularEvolucaoMensal(paradasMes));

        return metricas;
    }

    private List<ParadaSQLResponseDTO> filtrarParadasDoMes(List<ParadaSQLResponseDTO> paradas) {
        List<ParadaSQLResponseDTO> paradasMes = new ArrayList<>();

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        long inicioMesAtual = cal.getTimeInMillis();
        long dataAtual = System.currentTimeMillis();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        for (ParadaSQLResponseDTO parada : paradas) {
            try {
                if (parada.getDt_parada() != null) {
                    Date dataParada = dateFormat.parse(parada.getDt_parada());
                    if (dataParada != null && dataParada.getTime() >= inicioMesAtual && dataParada.getTime() <= dataAtual) {
                        paradasMes.add(parada);
                    }
                }
            } catch (ParseException e) {
                Log.e("Dashboard", "Erro ao parsear data: " + parada.getDt_parada(), e);
            }
        }

        Log.d("Dashboard", "Paradas do mês atual: " + paradasMes.size() +
                " (de " + new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date(inicioMesAtual)) +
                " até hoje)");

        return paradasMes;
    }

    private String calcularTempoMedio(List<ParadaSQLResponseDTO> paradas) {
        if (paradas.isEmpty()) return "0 min";

        long totalTempoMinutos = 0;
        int paradasComTempo = 0;

        for (ParadaSQLResponseDTO parada : paradas) {
            if (parada.getHora_inicio() != null && parada.getHora_fim() != null) {
                long duracaoMinutos = calcularDuracaoMinutos(parada.getHora_inicio(), parada.getHora_fim());
                if (duracaoMinutos > 0) {
                    totalTempoMinutos += duracaoMinutos;
                    paradasComTempo++;
                }
            }
        }

        if (paradasComTempo == 0) return "0 min";

        double tempoMedioMinutos = (double) totalTempoMinutos / paradasComTempo;

        return formatarTempoMedio(tempoMedioMinutos);
    }

    private String formatarTempoMedio(double tempoMedioMinutos) {
        if (tempoMedioMinutos < 60) {
            // Menos de 1 hora - mostra apenas minutos
            return String.format(Locale.getDefault(), "%.0f min", tempoMedioMinutos);
        } else {
            // 1 hora ou mais - converte para horas e minutos
            int horas = (int) (tempoMedioMinutos / 60);
            int minutos = (int) (tempoMedioMinutos % 60);

            if (minutos == 0) {
                return String.format(Locale.getDefault(), "%d h", horas);
            } else {
                return String.format(Locale.getDefault(), "%d h %02d min", horas, minutos);
            }
        }
    }

    private long calcularDuracaoMinutos(String horaInicio, String horaFim) {
        try {
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

            Date inicio = timeFormat.parse(horaInicio);
            Date fim = timeFormat.parse(horaFim);

            if (inicio != null && fim != null) {
                long diff = fim.getTime() - inicio.getTime();
                return diff / (60 * 1000); // Converter para minutos
            }
        } catch (ParseException e) {
            Log.e("Dashboard", "Erro ao calcular duração entre " + horaInicio + " e " + horaFim, e);
        }
        return 0;
    }

    private String identificarMaquinaComMaisParadas(List<ParadaSQLResponseDTO> paradas) {
        Map<Integer, Integer> contagemMaquinas = new HashMap<>();

        for (ParadaSQLResponseDTO parada : paradas) {
            Integer idMaquina = parada.getId_maquina();
            if (idMaquina != null) {
                contagemMaquinas.put(idMaquina, contagemMaquinas.getOrDefault(idMaquina, 0) + 1);
            }
        }

        return contagemMaquinas.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(entry -> {
                    Integer idMaquina = entry.getKey();
                    // Busca o nome da máquina na lista carregada
                    for (MaquinaResponseDTO maquina : listaMaquinas) {
                        if (maquina.getId() != null && maquina.getId().equals(idMaquina.longValue())) {
                            return maquina.getNome() != null ? maquina.getNome() : "Máquina " + idMaquina;
                        }
                    }
                    return "Máquina " + idMaquina; // Fallback se não encontrar
                })
                .orElse("Nenhuma");
    }

    private String identificarSetorComMaisParadas(List<ParadaSQLResponseDTO> paradas) {
        Map<String, Integer> setores = calcularParadasPorSetor(paradas);
        return setores.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Nenhum");
    }

    private Map<String, Integer> calcularParadasPorSetor(List<ParadaSQLResponseDTO> paradas) {
        Map<String, Integer> setores = new HashMap<>();
        for (ParadaSQLResponseDTO parada : paradas) {
            String setor = parada.getDes_setor();
            if (setor != null) {
                setores.put(setor, setores.getOrDefault(setor, 0) + 1);
            }
        }
        return setores;
    }

    private Map<String, Integer> calcularEvolucaoMensal(List<ParadaSQLResponseDTO> paradas) {
        // Usando LinkedHashMap para manter a ordem de inserção
        Map<String, Integer> evolucao = new LinkedHashMap<>();
        evolucao.put("Sem 1", 0);
        evolucao.put("Sem 2", 0);
        evolucao.put("Sem 3", 0);
        evolucao.put("Sem 4", 0);
        evolucao.put("Sem 5", 0); // Para meses com 5 semanas

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        for (ParadaSQLResponseDTO parada : paradas) {
            try {
                if (parada.getDt_parada() != null) {
                    Date dataParada = dateFormat.parse(parada.getDt_parada());
                    if (dataParada != null) {
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(dataParada);
                        int semanaMes = cal.get(Calendar.WEEK_OF_MONTH);

                        String semanaKey = "Sem " + Math.min(semanaMes, 5);
                        evolucao.put(semanaKey, evolucao.getOrDefault(semanaKey, 0) + 1);
                    }
                }
            } catch (ParseException e) {
                Log.e("Dashboard", "Erro ao parsear data para evolução: " + parada.getDt_parada(), e);
            }
        }

        Map<String, Integer> evolucaoFiltrada = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : evolucao.entrySet()) {
            if (entry.getValue() > 0) {
                evolucaoFiltrada.put(entry.getKey(), entry.getValue());
            }
        }

        // Se todas as semanas estão zeradas, mantém pelo menos as 4 primeiras
        if (evolucaoFiltrada.isEmpty()) {
            evolucaoFiltrada.put("Sem 1", 0);
            evolucaoFiltrada.put("Sem 2", 0);
            evolucaoFiltrada.put("Sem 3", 0);
            evolucaoFiltrada.put("Sem 4", 0);
        }

        return evolucaoFiltrada;
    }

    private void atualizarUI(MetricasParadaDTO metricas) {
        if (textTotalParadas != null) {
            textTotalParadas.setText(String.valueOf(metricas.getTotalParadasMes()));
        }
        if (textTempoMedio != null) {
            // CORREÇÃO: Formatar o double para String com horas e minutos
            String tempoFormatado = formatarTempoMedio(metricas.getTempoMedioParadaMinutos());
            textTempoMedio.setText(tempoFormatado);
        }
        if (textMaquinaProblema != null) {
            textMaquinaProblema.setText(metricas.getMaquinaComMaisParadas());
        }
        if (textSetorProblema != null) {
            textSetorProblema.setText(metricas.getSetorComMaisParadas());
        }

        atualizarGraficoEvolucao(metricas.getEvolucaoMensal());
    }

    private void atualizarGraficoEvolucao(Map<String, Integer> evolucaoMensal) {
        if (lineChartEvolucao == null) return;

        List<Entry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        int i = 0;
        for (Map.Entry<String, Integer> entry : evolucaoMensal.entrySet()) {
            entries.add(new Entry(i, entry.getValue()));
            labels.add(entry.getKey());
            i++;
        }

        LineDataSet dataSet = new LineDataSet(entries, "Paradas por Semana");
        dataSet.setColor(Color.BLUE);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(12f);
        dataSet.setCircleColor(Color.BLUE);
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawCircleHole(true);
        dataSet.setCircleHoleColor(Color.WHITE);

        LineData lineData = new LineData(dataSet);
        lineChartEvolucao.setData(lineData);
        lineChartEvolucao.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        lineChartEvolucao.invalidate();
    }

    private void configurarLineChart(LineChart chart) {
        if (chart == null) return;

        chart.getDescription().setText("Evolução Mensal de Paradas");
        chart.getDescription().setTextSize(12f);
        chart.setDrawGridBackground(false);
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setPinchZoom(true);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        xAxis.setTextSize(10f);

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setTextSize(10f);

        chart.getAxisRight().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.animateY(1000);
    }
}