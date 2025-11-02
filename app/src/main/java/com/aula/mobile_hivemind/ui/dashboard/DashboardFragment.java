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

import com.aula.mobile_hivemind.api.RetrofitClient;
import com.aula.mobile_hivemind.dto.RegistroParadaResponseDTO;
import com.aula.mobile_hivemind.databinding.FragmentDashboardBinding;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private com.aula.mobile_hivemind.api.ApiService apiService;

    // TextViews do novo layout
    private TextView textResumoNumero, textData, txtPorcent;
    private TextView txtSetorA, txtSetorB, txtSetorC;
    private TextView txtTempoPeriodo, txtTempoHorario;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Inicializar as views
        inicializarViews();

        // Inicializar API service
        apiService = RetrofitClient.getApiService();

        // Atualizar dados
        atualizarDataAtual();
        atualizarResumo();
        atualizarDadosSetores();
        atualizarHorasPerdidas();

        return root;
    }

    private void inicializarViews() {
        textResumoNumero = binding.textResumoNumero;
        textData = binding.textData;
        txtPorcent = binding.txtPorcent;

        // Buscar as views dos setores - você precisará adicionar esses IDs no XML
        // txtSetorA = binding.txtSetorA;
        // txtSetorB = binding.txtSetorB;
        // txtSetorC = binding.txtSetorC;

        // txtTempoPeriodo = binding.txtTempoPeriodo;
        // txtTempoHorario = binding.txtTempoHorario;
    }

    private void atualizarDataAtual() {
        LocalDate hoje = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("'Hoje dia' dd/MM/yyyy", new Locale("pt", "BR"));
        String dataFormatada = hoje.format(formatter);
        textData.setText(dataFormatada);
    }

    private void atualizarResumo() {
        buscarParadasDoMesAtual(new ParadasCallback() {
            @Override
            public void onParadasLoaded(int paradasMesAtual, int paradasMesAnterior, Map<String, Integer> paradasPorSetor) {
                // Atualizar número principal
                textResumoNumero.setText(String.valueOf(paradasMesAtual));

                // Calcular porcentagem
                double porcentagem;
                if (paradasMesAnterior != 0) {
                    porcentagem = ((double)(paradasMesAtual - paradasMesAnterior) / paradasMesAnterior) * 100;
                } else {
                    porcentagem = paradasMesAtual > 0 ? 100 : 0;
                }

                // Formatar porcentagem conforme a imagem
                String textoPorcentagem = String.format(Locale.getDefault(), "%.2f%%", Math.abs(porcentagem));
                txtPorcent.setText(textoPorcentagem);

                // Atualizar dados dos setores
                atualizarDadosSetoresComMapa(paradasPorSetor);
            }

            @Override
            public void onError(String error) {
                Log.e("DashboardFragment", "Erro ao carregar paradas: " + error);
                textResumoNumero.setText("0");
                txtPorcent.setText("0.00%");
            }
        });
    }

    private void atualizarDadosSetores() {
        // Dados estáticos iniciais - serão substituídos pelos dados reais
        if (txtSetorA != null) txtSetorA.setText("0.75% - 100%");
        if (txtSetorB != null) txtSetorB.setText("85 - 70%");
        if (txtSetorC != null) txtSetorC.setText("--");
    }

    private void atualizarDadosSetoresComMapa(Map<String, Integer> paradasPorSetor) {
        // Calcular totais e porcentagens para cada setor
        int totalParadas = 0;
        for (int quantidade : paradasPorSetor.values()) {
            totalParadas += quantidade;
        }

        // Atualizar cada setor (exemplo com 3 setores principais)
        if (txtSetorA != null && paradasPorSetor.containsKey("Setor A")) {
            int quantidadeA = paradasPorSetor.get("Setor A");
            double porcentagemA = totalParadas > 0 ? (quantidadeA * 100.0) / totalParadas : 0;
            txtSetorA.setText(String.format(Locale.getDefault(), "%.2f%% - %d%%", porcentagemA, (int)(porcentagemA)));
        }

        if (txtSetorB != null && paradasPorSetor.containsKey("Setor B")) {
            int quantidadeB = paradasPorSetor.get("Setor B");
            double porcentagemB = totalParadas > 0 ? (quantidadeB * 100.0) / totalParadas : 0;
            txtSetorB.setText(String.format(Locale.getDefault(), "%d - %.0f%%", quantidadeB, porcentagemB));
        }

        if (txtSetorC != null && paradasPorSetor.containsKey("Setor C")) {
            int quantidadeC = paradasPorSetor.get("Setor C");
            if (quantidadeC > 0) {
                double porcentagemC = totalParadas > 0 ? (quantidadeC * 100.0) / totalParadas : 0;
                txtSetorC.setText(String.format(Locale.getDefault(), "%.2f%%", porcentagemC));
            } else {
                txtSetorC.setText("--");
            }
        }
    }

    private void atualizarHorasPerdidas() {
        // Buscar dados de horas perdidas
        buscarHorasPerdidas(new HorasPerdidasCallback() {
            @Override
            public void onHorasCarregadas(double tempoPeriodo, double tempoHorario) {
                if (txtTempoPeriodo != null) {
                    txtTempoPeriodo.setText(String.format(Locale.getDefault(), "%.1f horas", tempoPeriodo));
                }
                if (txtTempoHorario != null) {
                    txtTempoHorario.setText(String.format(Locale.getDefault(), "%.1f horas", tempoHorario));
                }
            }

            @Override
            public void onError(String error) {
                Log.e("DashboardFragment", "Erro ao carregar horas perdidas: " + error);
                if (txtTempoPeriodo != null) txtTempoPeriodo.setText("--");
                if (txtTempoHorario != null) txtTempoHorario.setText("--");
            }
        });
    }

    interface ParadasCallback {
        void onParadasLoaded(int paradasMesAtual, int paradasMesAnterior, Map<String, Integer> paradasPorSetor);
        void onError(String error);
    }

    interface HorasPerdidasCallback {
        void onHorasCarregadas(double tempoPeriodo, double tempoHorario);
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
                    Map<String, Integer> paradasPorSetor = new HashMap<>();

                    for (RegistroParadaResponseDTO parada : todasParadas) {
                        try {
                            if (parada.getDt_parada() != null) {
                                Date dataParadaDate = parada.getDt_parada();
                                Calendar dataParadaCal = Calendar.getInstance();
                                dataParadaCal.setTime(dataParadaDate);

                                int mesParada = dataParadaCal.get(Calendar.MONTH);
                                int anoParada = dataParadaCal.get(Calendar.YEAR);

                                String setor = parada.getDes_setor();
                                if (setor == null) setor = "Sem Setor";

                                // Contar por setor
                                paradasPorSetor.put(setor, paradasPorSetor.getOrDefault(setor, 0) + 1);

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
                    callback.onParadasLoaded(contadorMesAtual, contadorMesAnterior, paradasPorSetor);

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

    private void buscarHorasPerdidas(HorasPerdidasCallback callback) {
        // Implementar a lógica para buscar horas perdidas
        // Por enquanto, retornar valores fictícios baseados na imagem
        callback.onHorasCarregadas(900.0, 85.0);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Atualizar dados quando o fragment for retomado
        atualizarResumo();
        atualizarHorasPerdidas();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}