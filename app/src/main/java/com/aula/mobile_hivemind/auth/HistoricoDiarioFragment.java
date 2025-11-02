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
import com.aula.mobile_hivemind.dto.ManutencaoResponseDTO;
import com.aula.mobile_hivemind.dto.MaquinaResponseDTO;
import com.aula.mobile_hivemind.dto.ParadaSQLResponseDTO;
import com.aula.mobile_hivemind.dto.RegistroParadaResponseDTO;
import com.aula.mobile_hivemind.recyclerViewParadas.Parada;
import com.aula.mobile_hivemind.recyclerViewParadas.ParadaAdapter;
import com.aula.mobile_hivemind.utils.SharedPreferencesManager;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HistoricoDiarioFragment extends Fragment {

    private RecyclerView recyclerViewHistorico;
    private TextView txtTitulo, txtParada;
    private ImageButton btnVoltar;
    private com.aula.mobile_hivemind.api.ApiService apiService;
    private ParadaAdapter historicoAdapter;
    private List<Parada> historicoParadas;
    private SqlApiService sqlApiService;
    private LinearLayout emptyState;
    private TextView txtTotalHoje, txtDataAtual;

    // Tipos de usuário como strings
    private static final String TIPO_USUARIO_COMUM = "regular";
    private static final String TIPO_USUARIO_MANUTENCAO = "man";

    private String tipoUsuario;
    private int usuarioId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_historico_diario, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Obter tipo de usuário como string
        tipoUsuario = getTipoUsuarioComoString();
        usuarioId = SharedPreferencesManager.getInstance(requireContext()).getUserId();

        Log.d("HistoricoDiario", "Tipo usuário: " + tipoUsuario + ", ID: " + usuarioId);

        // Inicializar views
        emptyState = view.findViewById(R.id.emptyState);
        txtTotalHoje = view.findViewById(R.id.txtTotalHoje);
        txtDataAtual = view.findViewById(R.id.txtDataAtual);
        recyclerViewHistorico = view.findViewById(R.id.recyclerViewHistorico);
        txtTitulo = view.findViewById(R.id.txtTitulo);
        txtParada = view.findViewById(R.id.txtParada);
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

        // Configurar interface baseada no tipo de usuário
        configurarInterfacePorTipoUsuario();

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

    private String getTipoUsuarioComoString() {
        // Obter do SharedPreferencesManager (que retorna int)
        int tipoInt = SharedPreferencesManager.getInstance(requireContext()).getUserType();

        Log.d("HistoricoDebug", "Tipo int do SharedPrefs: " + tipoInt);

        // Converter int para string conforme o mapeamento do LoginActivity
        switch (tipoInt) {
            case 1:
                return "regular";
            case 2:
                return "man";
            case 3:
                return "RH";
            default:
                return "regular";
        }
    }

    private void configurarInterfacePorTipoUsuario() {
        if (txtTitulo != null) {
            if (TIPO_USUARIO_MANUTENCAO.equals(tipoUsuario)) {
                txtTitulo.setText("Histórico de Manutenções");
                if (txtParada != null) {
                    txtParada.setText("Manutenções de hoje");
                }
            } else {
                txtTitulo.setText("Minhas Paradas");
                if (txtParada != null) {
                    txtParada.setText("Paradas de hoje");
                }
            }
        }
    }

    private void setupRecyclerView() {
        boolean mostrarAcoes = TIPO_USUARIO_MANUTENCAO.equals(tipoUsuario);

        historicoAdapter = new ParadaAdapter(historicoParadas, sqlApiService, mostrarAcoes);
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

        Call<List<MaquinaResponseDTO>> call = sqlApiService.listarMaquinas();
        call.enqueue(new Callback<List<MaquinaResponseDTO>>() {
            @Override
            public void onResponse(Call<List<MaquinaResponseDTO>> call, Response<List<MaquinaResponseDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<MaquinaResponseDTO> maquinas = response.body();
                    String nomeMaquinaEncontrada = "Máquina não encontrada";

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

        if (TIPO_USUARIO_MANUTENCAO.equals(tipoUsuario)) {
            carregarManutencoesDoDia();
        } else {
            carregarParadasDoDia();
        }
    }

    private void carregarParadasDoDia() {
        Call<List<RegistroParadaResponseDTO>> callMongo = apiService.getAllRegistros();
        callMongo.enqueue(new Callback<List<RegistroParadaResponseDTO>>() {
            @Override
            public void onResponse(Call<List<RegistroParadaResponseDTO>> call, Response<List<RegistroParadaResponseDTO>> response) {
                if (!isAdded() || getContext() == null) return;

                if (response.isSuccessful() && response.body() != null) {
                    List<Parada> paradasDoDia = filtrarParadasDoDia(response.body());

                    if (paradasDoDia.isEmpty()) {
                        carregarParadasSQL();
                    } else {
                        processarParadasDoDia(paradasDoDia);
                    }
                } else {
                    carregarParadasSQL();
                }
            }

            @Override
            public void onFailure(Call<List<RegistroParadaResponseDTO>> call, Throwable t) {
                if (!isAdded() || getContext() == null) return;
                carregarParadasSQL();
            }
        });
    }

    private void carregarParadasSQL() {
        Call<List<ParadaSQLResponseDTO>> callSQL = sqlApiService.listarTodasParadas();
        callSQL.enqueue(new Callback<List<ParadaSQLResponseDTO>>() {
            @Override
            public void onResponse(Call<List<ParadaSQLResponseDTO>> call, Response<List<ParadaSQLResponseDTO>> response) {
                if (!isAdded() || getContext() == null) return;

                if (response.isSuccessful() && response.body() != null) {
                    List<Parada> paradasDoDia = filtrarParadasSQLDoDia(response.body());
                    processarParadasDoDia(paradasDoDia);
                } else {
                    mostrarHistoricoVazio();
                }
            }

            @Override
            public void onFailure(Call<List<ParadaSQLResponseDTO>> call, Throwable t) {
                if (!isAdded() || getContext() == null) return;
                mostrarHistoricoVazio();
            }
        });
    }

    private void carregarManutencoesDoDia() {
        Call<List<ManutencaoResponseDTO>> call = sqlApiService.listarManutencoes();
        call.enqueue(new Callback<List<ManutencaoResponseDTO>>() {
            @Override
            public void onResponse(Call<List<ManutencaoResponseDTO>> call, Response<List<ManutencaoResponseDTO>> response) {
                if (!isAdded() || getContext() == null) return;

                if (response.isSuccessful() && response.body() != null) {
                    List<Parada> manutencoesDoDia = filtrarManutencoesDoDia(response.body());
                    processarParadasDoDia(manutencoesDoDia);
                } else {
                    mostrarHistoricoVazio();
                }
            }

            @Override
            public void onFailure(Call<List<ManutencaoResponseDTO>> call, Throwable t) {
                if (!isAdded() || getContext() == null) return;
                mostrarHistoricoVazio();
            }
        });
    }

    private List<Parada> filtrarParadasDoDia(List<RegistroParadaResponseDTO> todasParadas) {
        List<Parada> paradasDoDia = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String dataAtual = sdf.format(new Date());

        for (RegistroParadaResponseDTO parada : todasParadas) {
            try {
                if (parada.getDt_parada() != null) {
                    String dataParada = sdf.format(parada.getDt_parada());
                    if (dataParada.equals(dataAtual)) {
                        paradasDoDia.add(converterParaParada(parada));
                    }
                }
            } catch (Exception e) {
                Log.e("HistoricoDiario", "Erro ao filtrar parada MongoDB: " + e.getMessage());
            }
        }

        return paradasDoDia;
    }

    private List<Parada> filtrarParadasSQLDoDia(List<ParadaSQLResponseDTO> todasParadas) {
        List<Parada> paradasDoDia = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String dataAtual = sdf.format(new Date());

        for (ParadaSQLResponseDTO parada : todasParadas) {
            try {
                if (parada.getDt_parada() != null && parada.getDt_parada().equals(dataAtual)) {
                    paradasDoDia.add(converterParadaSQLParaParada(parada));
                }
            } catch (Exception e) {
                Log.e("HistoricoDiario", "Erro ao filtrar parada SQL: " + e.getMessage());
            }
        }

        return paradasDoDia;
    }

    private List<Parada> filtrarManutencoesDoDia(List<ManutencaoResponseDTO> todasManutencoes) {
        List<Parada> manutencoesDoDia = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String dataAtual = sdf.format(new Date());

        for (ManutencaoResponseDTO manutencao : todasManutencoes) {
            try {
                if (manutencao.getDate() != null) {
                    String dataManutencao = sdf.format(manutencao.getDate());
                    if (dataManutencao.equals(dataAtual)) {
                        manutencoesDoDia.add(converterManutencaoParaParada(manutencao));
                    }
                }
            } catch (Exception e) {
                Log.e("HistoricoDiario", "Erro ao filtrar manutenção: " + e.getMessage());
            }
        }

        return manutencoesDoDia;
    }

    private void processarParadasDoDia(List<Parada> paradasDoDia) {
        requireActivity().runOnUiThread(() -> {
            atualizarUIHistorico(paradasDoDia);
        });
    }

    private void mostrarHistoricoVazio() {
        if (!isAdded() || getContext() == null) return;

        requireActivity().runOnUiThread(() -> {
            atualizarUIHistorico(new ArrayList<>());
        });
    }

    private void atualizarUIHistorico(List<Parada> paradasDoDia) {
        if (emptyState == null || recyclerViewHistorico == null || txtTotalHoje == null) {
            Log.e("HistoricoDiario", "Views não inicializadas corretamente");
            return;
        }

        if (paradasDoDia.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            recyclerViewHistorico.setVisibility(View.GONE);
            txtTotalHoje.setText("0");
            historicoParadas.clear();
            if (historicoAdapter != null) {
                historicoAdapter.notifyDataSetChanged();
            }
        } else {
            emptyState.setVisibility(View.GONE);
            recyclerViewHistorico.setVisibility(View.VISIBLE);
            txtTotalHoje.setText(String.valueOf(paradasDoDia.size()));
            historicoParadas.clear();
            historicoParadas.addAll(paradasDoDia);
            if (historicoAdapter != null) {
                historicoAdapter.notifyDataSetChanged();
            }
        }
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

    private Parada converterParadaSQLParaParada(ParadaSQLResponseDTO paradaSQL) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

            Date dataParada = null;
            Date horaInicio = null;
            Date horaFim = null;

            if (paradaSQL.getDt_parada() != null) {
                dataParada = dateFormat.parse(paradaSQL.getDt_parada());
            }
            if (paradaSQL.getHora_inicio() != null) {
                horaInicio = timeFormat.parse(paradaSQL.getHora_inicio());
            }
            if (paradaSQL.getHora_fim() != null) {
                horaFim = timeFormat.parse(paradaSQL.getHora_fim());
            }

            return new Parada(
                    null,
                    paradaSQL.getId_manutencao(),
                    paradaSQL.getId_maquina(),
                    paradaSQL.getId_usuario(),
                    paradaSQL.getDes_parada(),
                    paradaSQL.getDes_setor(),
                    dataParada,
                    horaFim,
                    horaInicio
            );
        } catch (Exception e) {
            Log.e("HistoricoDiario", "Erro ao converter parada SQL: " + e.getMessage());
            return new Parada(
                    null,
                    paradaSQL.getId_manutencao(),
                    paradaSQL.getId_maquina(),
                    paradaSQL.getId_usuario(),
                    paradaSQL.getDes_parada(),
                    paradaSQL.getDes_setor(),
                    null,
                    null,
                    null
            );
        }
    }

    private Parada converterManutencaoParaParada(ManutencaoResponseDTO manutencao) {
        return new Parada(
                String.valueOf(manutencao.getId()),
                manutencao.getId_maquina(),
                manutencao.getId_usuario(),
                manutencao.getDescricao(),
                manutencao.getSetor(),
                manutencao.getDate(),
                manutencao.getHora_fim(),
                manutencao.getHora_inicio()
        );
    }

    @Override
    public void onResume() {
        super.onResume();
        carregarHistoricoDiario();
    }
}