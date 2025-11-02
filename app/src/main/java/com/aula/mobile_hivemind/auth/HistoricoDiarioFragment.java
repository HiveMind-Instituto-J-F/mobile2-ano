package com.aula.mobile_hivemind.auth;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
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
import com.aula.mobile_hivemind.dto.ParadaSQLResponseDTO;
import com.aula.mobile_hivemind.dto.RegistroParadaResponseDTO;
import com.aula.mobile_hivemind.recyclerViewParadas.Parada;
import com.aula.mobile_hivemind.recyclerViewParadas.ParadaAdapter;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.text.ParseException;
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
    private TextView txtTitulo;
    private ImageButton btnVoltar;
    private com.aula.mobile_hivemind.api.ApiService apiService;
    private ParadaAdapter historicoAdapter; // Use ParadaAdapter em vez de HistoricoAdapter
    private List<Parada> historicoParadas;
    private SqlApiService sqlApiService;
    private LinearLayout emptyState;
    private TextView txtTotalHoje, txtDataAtual;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_historico_diario, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicializar views com verificações de null
        emptyState = view.findViewById(R.id.emptyState);
        txtTotalHoje = view.findViewById(R.id.txtTotalHoje);
        txtDataAtual = view.findViewById(R.id.txtDataAtual);
        recyclerViewHistorico = view.findViewById(R.id.recyclerViewHistorico);
        txtTitulo = view.findViewById(R.id.txtTitulo);
        btnVoltar = view.findViewById(R.id.btnVoltar);

        // Verificar se todas as views foram encontradas
        if (emptyState == null || txtTotalHoje == null || txtDataAtual == null ||
                recyclerViewHistorico == null) {
            Log.e("HistoricoDiario", "Alguma view não foi encontrada no layout");
            return;
        }

        // Configurar data atual
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String dataAtual = sdf.format(new Date());
        txtDataAtual.setText(dataAtual);

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
        if (!isAdded() || getContext() == null) return;

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
                        if (maquina.getId() != null && maquina.getId().equals(idMaquina.longValue())) {
                            nomeMaquinaEncontrada = maquina.getNome() != null ? maquina.getNome() : "Máquina " + idMaquina;
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
        if (!isAdded() || getContext() == null) return;

        Call<List<RegistroParadaResponseDTO>> call = apiService.getAllRegistros();
        call.enqueue(new Callback<List<RegistroParadaResponseDTO>>() {
            @Override
            public void onResponse(Call<List<RegistroParadaResponseDTO>> call, Response<List<RegistroParadaResponseDTO>> response) {
                if (!isAdded() || getContext() == null) return;

                if (response.isSuccessful() && response.body() != null) {
                    processarParadasDoDiaMongoDB(response.body()); // Método corrigido
                } else {
                    mostrarHistoricoVazio();
                }
            }

            @Override
            public void onFailure(Call<List<RegistroParadaResponseDTO>> call, Throwable t) {
                if (!isAdded() || getContext() == null) return;
                mostrarHistoricoVazio();
            }
        });
    }

    private void processarParadasDoDiaMongoDB(List<RegistroParadaResponseDTO> paradasMongo) {
        if (!isAdded() || getContext() == null) return;

        List<Parada> paradasDoDia = new ArrayList<>();

        // Obter data atual
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String dataAtual = sdf.format(new Date());

        for (RegistroParadaResponseDTO paradaMongo : paradasMongo) {
            try {
                if (paradaMongo.getDt_parada() != null) {
                    String dataParada = sdf.format(paradaMongo.getDt_parada());
                    if (dataParada.equals(dataAtual)) {
                        // Converter para objeto Parada
                        Parada parada = converterParaParada(paradaMongo);
                        paradasDoDia.add(parada);
                    }
                }
            } catch (Exception e) {
                Log.e("HistoricoDiario", "Erro ao processar parada MongoDB: " + e.getMessage());
            }
        }

        // Atualizar UI na thread principal
        requireActivity().runOnUiThread(() -> {
            atualizarUIHistorico(paradasDoDia);
        });
    }

    private void processarParadasDoDiaSQL(List<ParadaSQLResponseDTO> todasParadas) {
        if (!isAdded() || getContext() == null) return;

        List<Parada> paradasDoDia = new ArrayList<>();

        // Obter data atual
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String dataAtual = sdf.format(new Date());

        for (ParadaSQLResponseDTO paradaSQL : todasParadas) {
            try {
                if (paradaSQL.getDt_parada() != null && paradaSQL.getDt_parada().equals(dataAtual)) {
                    // Converter para objeto Parada
                    Parada parada = converterParadaSQLParaParada(paradaSQL);
                    paradasDoDia.add(parada);
                }
            } catch (Exception e) {
                Log.e("HistoricoDiario", "Erro ao processar parada SQL: " + e.getMessage());
            }
        }

        // Atualizar UI na thread principal
        requireActivity().runOnUiThread(() -> {
            atualizarUIHistorico(paradasDoDia);
        });
    }

    private void mostrarHistoricoVazio() {
        if (!isAdded() || getContext() == null) return;

        // Atualizar UI com lista vazia
        requireActivity().runOnUiThread(() -> {
            atualizarUIHistorico(new ArrayList<>());
        });
    }

    private void atualizarUIHistorico(List<Parada> paradasDoDia) {
        // Verificar se as views foram inicializadas
        if (emptyState == null || recyclerViewHistorico == null || txtTotalHoje == null) {
            Log.e("HistoricoDiario", "Views não inicializadas corretamente");
            return;
        }

        if (paradasDoDia.isEmpty()) {
            // Mostrar estado vazio
            emptyState.setVisibility(View.VISIBLE);
            recyclerViewHistorico.setVisibility(View.GONE);

            // Atualizar estatísticas para zero
            txtTotalHoje.setText("0");

            // Limpar adapter
            historicoParadas.clear();
            if (historicoAdapter != null) {
                historicoAdapter.notifyDataSetChanged();
            }
        } else {
            // Mostrar lista
            emptyState.setVisibility(View.GONE);
            recyclerViewHistorico.setVisibility(View.VISIBLE);

            // Calcular estatísticas
            int totalParadas = paradasDoDia.size();

            // Atualizar estatísticas
            txtTotalHoje.setText(String.valueOf(totalParadas));

            // Atualizar adapter existente (não criar novo)
            historicoParadas.clear();
            historicoParadas.addAll(paradasDoDia);
            if (historicoAdapter != null) {
                historicoAdapter.notifyDataSetChanged();
            }
        }
    }

    private Parada converterParadaSQLParaParada(ParadaSQLResponseDTO paradaSQL) {
        Date dataParada = null;
        Date horaInicio = null;
        Date horaFim = null;

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

            if (paradaSQL.getDt_parada() != null) {
                dataParada = dateFormat.parse(paradaSQL.getDt_parada());
            }
            if (paradaSQL.getHora_inicio() != null) {
                horaInicio = timeFormat.parse(paradaSQL.getHora_inicio());
            }
            if (paradaSQL.getHora_fim() != null) {
                horaFim = timeFormat.parse(paradaSQL.getHora_fim());
            }
        } catch (ParseException e) {
            Log.e("HistoricoDiario", "Erro ao converter datas SQL", e);
        }

        return new Parada(
                null,
                paradaSQL.getId_maquina(),
                paradaSQL.getId_usuario(),
                paradaSQL.getDes_parada(),
                paradaSQL.getDes_setor(),
                dataParada,
                horaFim,
                horaInicio
        );
    }

    private Parada converterParaParada(RegistroParadaResponseDTO registro) {
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