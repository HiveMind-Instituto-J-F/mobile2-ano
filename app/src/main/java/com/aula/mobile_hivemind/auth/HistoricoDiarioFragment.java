package com.aula.mobile_hivemind.auth;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aula.mobile_hivemind.R;
import com.aula.mobile_hivemind.api.RetrofitClient;
import com.aula.mobile_hivemind.api.SqlApiService;
import com.aula.mobile_hivemind.dto.MaquinaResponseDTO;
import com.aula.mobile_hivemind.dto.RegistroParadaResponseDTO;
import com.aula.mobile_hivemind.recyclerViewParadas.Parada;
import com.aula.mobile_hivemind.recyclerViewParadas.ParadaAdapter;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HistoricoDiarioFragment extends Fragment {

    private RecyclerView recyclerViewHistorico;
    private TextView txtTitulo, txtSemHistorico;
    private ImageButton btnVoltar;
    private com.aula.mobile_hivemind.api.ApiService apiService;
    private ParadaAdapter historicoAdapter;
    private List<Parada> historicoParadas;
    private SqlApiService sqlApiService;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_historico_diario, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerViewHistorico = view.findViewById(R.id.recyclerViewHistorico);
        txtTitulo = view.findViewById(R.id.txtTitulo);
        txtSemHistorico = view.findViewById(R.id.txtSemHistorico);
        btnVoltar = view.findViewById(R.id.btnVoltar);

        apiService = RetrofitClient.getApiService();
        sqlApiService = RetrofitClient.getSqlApiService();
        historicoParadas = new ArrayList<>();

        setupRecyclerView();
        carregarHistoricoDiario();

        btnVoltar.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.popBackStack();
        });
    }

    private void setupRecyclerView() {
        historicoAdapter = new ParadaAdapter(historicoParadas, sqlApiService);
        recyclerViewHistorico.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewHistorico.setAdapter(historicoAdapter);

        historicoAdapter.setOnItemClickListener(parada -> {
            abrirModalParada(parada);
        });
    }

    private void abrirModalParada(Parada parada) {
        View modalView = LayoutInflater.from(requireContext()).inflate(R.layout.modal_parada, null);

        // Buscar todas as TextViews
        TextView txtIdMaquina = modalView.findViewById(R.id.txtIdMaquina);
        TextView txtCodigoColaborador = modalView.findViewById(R.id.txtCodigoColaborador);
        TextView txtNomeMaquina = modalView.findViewById(R.id.txtNomeMaquina);
        TextView txtSetor = modalView.findViewById(R.id.txtSetor);
        TextView txtDataParada = modalView.findViewById(R.id.txtData);
        TextView txtHoraInicio = modalView.findViewById(R.id.txtHoraInicio);
        TextView txtHoraFim = modalView.findViewById(R.id.txtHoraFim);
        TextView txtDuracao = modalView.findViewById(R.id.txtDuracao);
        TextView txtDescricaoParada = modalView.findViewById(R.id.txtDescricao);

        // Define os valores básicos
        txtIdMaquina.setText(parada.getId_maquina() != null ? String.valueOf(parada.getId_maquina()) : "Não informado");
        txtCodigoColaborador.setText(parada.getId_usuario() != null ? String.valueOf(parada.getId_usuario()) : "Não informado");
        txtSetor.setText(parada.getDes_setor() != null ? parada.getDes_setor() : "Não informado");
        if (parada.getDt_parada() != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd, MMM yyyy", Locale.getDefault());
            String dataFormatada = dateFormat.format(parada.getDt_parada());
            txtDataParada.setText(dataFormatada);
        } else {
            txtDataParada.setText("Não informado");
        }
        txtDescricaoParada.setText(parada.getDes_parada() != null ? parada.getDes_parada() : "Não informado");

        // Processar horas e duração
        processarHorasEDuracao(parada, txtHoraInicio, txtHoraFim, txtDuracao);

        // Buscar nome da máquina pelo ID
        buscarNomeMaquinaPorId(parada.getId_maquina(), txtNomeMaquina);

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
        bottomSheetDialog.setContentView(modalView);
        bottomSheetDialog.show();

        ImageButton btnFechar = modalView.findViewById(R.id.btnFechar);
        if (btnFechar != null) {
            btnFechar.setOnClickListener(v -> bottomSheetDialog.dismiss());
        }
    }

    private void processarHorasEDuracao(Parada parada, TextView txtHoraInicio, TextView txtHoraFim, TextView txtDuracao) {
        try {
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

            if (parada.getHora_Inicio() != null) {
                String horaInicio = timeFormat.format(parada.getHora_Inicio());
                txtHoraInicio.setText(horaInicio);
            } else {
                txtHoraInicio.setText("Não informado");
            }

            if (parada.getHora_Fim() != null) {
                String horaFim = timeFormat.format(parada.getHora_Fim());
                txtHoraFim.setText(horaFim);
            } else {
                txtHoraFim.setText("Não informado");
            }

            if (parada.getHora_Inicio() != null && parada.getHora_Fim() != null) {
                long diff = parada.getHora_Fim().getTime() - parada.getHora_Inicio().getTime();
                long diffMinutes = diff / (60 * 1000);
                long diffHours = diffMinutes / 60;
                long remainingMinutes = diffMinutes % 60;

                String duracao;
                if (diffHours > 0) {
                    duracao = String.format(Locale.getDefault(), "%dh %02dmin", diffHours, remainingMinutes);
                } else {
                    duracao = String.format(Locale.getDefault(), "%dmin", diffMinutes);
                }
                txtDuracao.setText(duracao);
            } else {
                txtDuracao.setText("Não calculável");
            }

        } catch (Exception e) {
            txtHoraInicio.setText("Erro");
            txtHoraFim.setText("Erro");
            txtDuracao.setText("Erro");
            Log.e("ModalParada", "Erro ao processar horas: " + e.getMessage());
        }
    }

    private void buscarNomeMaquinaPorId(Integer idMaquina, TextView txtNomeMaquina) {
        if (idMaquina == null) {
            txtNomeMaquina.setText("ID não informado");
            return;
        }

        txtNomeMaquina.setText("Carregando...");

        // Buscar todas as máquinas e filtrar pelo ID
        Call<List<MaquinaResponseDTO>> call = sqlApiService.listarMaquinas();
        call.enqueue(new Callback<List<MaquinaResponseDTO>>() {
            @Override
            public void onResponse(Call<List<MaquinaResponseDTO>> call, Response<List<MaquinaResponseDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<MaquinaResponseDTO> maquinas = response.body();
                    String nomeMaquinaEncontrada = "Máquina não encontrada";

                    // Buscar máquina pelo ID
                    for (MaquinaResponseDTO maquina : maquinas) {
                        if (maquina.getId().equals(idMaquina.longValue())) {
                            nomeMaquinaEncontrada = maquina.getNome();
                            break;
                        }
                    }

                    txtNomeMaquina.setText(nomeMaquinaEncontrada);
                } else {
                    txtNomeMaquina.setText("Erro ao buscar máquina");
                }
            }

            @Override
            public void onFailure(Call<List<MaquinaResponseDTO>> call, Throwable t) {
                txtNomeMaquina.setText("Falha na conexão");
            }
        });
    }

    private void carregarHistoricoDiario() {
        Call<List<RegistroParadaResponseDTO>> call = apiService.getAllRegistros();
        call.enqueue(new Callback<List<RegistroParadaResponseDTO>>() {
            @Override
            public void onResponse(Call<List<RegistroParadaResponseDTO>> call, Response<List<RegistroParadaResponseDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    processarParadasDoDia(response.body());
                } else {
                    mostrarHistoricoVazio();
                }
            }

            @Override
            public void onFailure(Call<List<RegistroParadaResponseDTO>> call, Throwable t) {
                mostrarHistoricoVazio();
            }
        });
    }

    private void processarParadasDoDia(List<RegistroParadaResponseDTO> todasParadas) {
        historicoParadas.clear();

        // Obter data atual (início do dia)
        Calendar hoje = Calendar.getInstance();
        hoje.set(Calendar.HOUR_OF_DAY, 0);
        hoje.set(Calendar.MINUTE, 0);
        hoje.set(Calendar.SECOND, 0);
        hoje.set(Calendar.MILLISECOND, 0);

        Calendar amanha = (Calendar) hoje.clone();
        amanha.add(Calendar.DAY_OF_MONTH, 1);

        for (RegistroParadaResponseDTO registro : todasParadas) {
            try {
                if (registro.getDt_parada() != null) {
                    Date dataParada = registro.getDt_parada();

                    // Verificar se a data da parada é hoje
                    if (dataParada.after(hoje.getTime()) && dataParada.before(amanha.getTime())) {
                        Parada parada = converterParaParada(registro);
                        historicoParadas.add(parada);
                    }
                }
            } catch (Exception e) {
                Log.e("HistoricoDiario", "Erro ao processar parada: " + e.getMessage());
                e.printStackTrace();
            }
        }

        ordenarParadasPorHora(historicoParadas);
        atualizarUIHistorico();
    }

    private void ordenarParadasPorHora(List<Parada> paradas) {
        paradas.sort((p1, p2) -> {
            try {
                Date d1 = p1.getDt_parada(); // Agora também é Date
                Date d2 = p2.getDt_parada();

                // Ordena do mais recente para o mais antigo
                return d2.compareTo(d1);

            } catch (Exception e) {
                return 0;
            }
        });
    }

    private void atualizarUIHistorico() {
        if (historicoParadas.isEmpty()) {
            recyclerViewHistorico.setVisibility(View.GONE);
            txtSemHistorico.setVisibility(View.VISIBLE);
            txtTitulo.setText("Histórico Diário - 0 paradas");
        } else {
            recyclerViewHistorico.setVisibility(View.VISIBLE);
            txtSemHistorico.setVisibility(View.GONE);
            txtTitulo.setText("Histórico Diário: " + historicoParadas.size() + " paradas");
            historicoAdapter.notifyDataSetChanged();
        }
    }

    private void mostrarHistoricoVazio() {
        historicoParadas.clear();
        atualizarUIHistorico();
    }

    private Parada converterParaParada(RegistroParadaResponseDTO registro) {
        // Formatar a data para exibição
        SimpleDateFormat displayFormat = new SimpleDateFormat("dd, MMM yyyy HH:mm", Locale.getDefault());
        String dataFormatada = "";

        if (registro.getDt_parada() != null) {
            dataFormatada = displayFormat.format(registro.getDt_parada());
        }

        return new Parada(
                registro.getId(),
                registro.getId_maquina(),
                registro.getId_usuario(),
                registro.getDes_parada(),
                registro.getDes_setor(),
                registro.getDt_parada(),
                registro.getHora_Fim(),
                registro.getHora_Inicio()
        );
    }

    @Override
    public void onResume() {
        super.onResume();
        carregarHistoricoDiario();
    }
}