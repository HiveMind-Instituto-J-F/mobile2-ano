package com.aula.mobile_hivemind.recyclerViewParadas;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aula.mobile_hivemind.R;
import com.aula.mobile_hivemind.api.SqlApiService;
import com.aula.mobile_hivemind.dto.MaquinaResponseDTO;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ParadaAdapter extends RecyclerView.Adapter<ParadaAdapter.ViewHolder> {
    private List<Parada> listParadas;
    private OnItemClickListener onItemClickListener;
    private SqlApiService sqlApiService;


    public interface OnItemClickListener {
        void onItemClick(Parada parada);
    }

    // Construtor original (para compatibilidade)
    public ParadaAdapter(List<Parada> listParadas, SqlApiService sqlApiService) {
        this.listParadas = listParadas;
        this.sqlApiService = sqlApiService;
    }

    // Novo construtor com suporte a mostrarAcoes
    public ParadaAdapter(List<Parada> listParadas, SqlApiService sqlApiService, boolean mostrarAcoes) {
        this.listParadas = listParadas;
        this.sqlApiService = sqlApiService;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.parada_item_recycleview_principal, parent, false);
        return new ViewHolder(view, sqlApiService);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Parada parada = listParadas.get(position);
        holder.bind(parada);

        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(parada);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listParadas.size();
    }

    public void setParadas(List<Parada> paradas) {
        this.listParadas = paradas;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView textNomeMaquina;
        private TextView textDataParada;
        private TextView textCodigoColaborador;
        private TextView textSetor;
        private TextView textHoraInicio;
        private TextView textHoraFim;
        private TextView textDuracao;
        private TextView textDescricaoResumida;
        private TextView textStatusParada;
        private SqlApiService sqlApiService;

        public ViewHolder(@NonNull View itemView, SqlApiService sqlApiService) {
            super(itemView);
            this.sqlApiService = sqlApiService;

            // Inicializar todas as views do layout completo
            textNomeMaquina = itemView.findViewById(R.id.nomeMaq);
            textDataParada = itemView.findViewById(R.id.dtParada);
            textCodigoColaborador = itemView.findViewById(R.id.codColaborador);
            textSetor = itemView.findViewById(R.id.setorMaq);
            textHoraInicio = itemView.findViewById(R.id.horaInicio);
            textHoraFim = itemView.findViewById(R.id.horaFim);
            textDuracao = itemView.findViewById(R.id.duracaoParada);
            textDescricaoResumida = itemView.findViewById(R.id.descricaoResumida);
            textStatusParada = itemView.findViewById(R.id.statusParada);
        }

        public void bind(Parada parada) {
            // DEBUG: Log para verificar os dados da parada
            System.out.println("DEBUG Parada - ID: " + parada.getId() +
                    ", ID Manutenção: " + parada.getId_manutencao() +
                    ", Hora Início: " + parada.getHora_Inicio() +
                    ", Hora Fim: " + parada.getHora_Fim());

            // Nome da máquina
            buscarNomeMaquina(parada.getId_maquina());

            // Data formatada
            if (parada.getDt_parada() != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                String dataFormatada = dateFormat.format(parada.getDt_parada());
                textDataParada.setText(dataFormatada);
            } else {
                textDataParada.setText("Data não informada");
            }

            // Código do colaborador
            if (parada.getId_usuario() != null) {
                textCodigoColaborador.setText("ID: " + parada.getId_usuario());
            } else {
                textCodigoColaborador.setText("ID: Não informado");
            }

            // Setor
            if (parada.getDes_setor() != null) {
                textSetor.setText(parada.getDes_setor());
            } else {
                textSetor.setText("Setor não informado");
            }

            // Horários
            if (parada.getHora_Inicio() != null) {
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                textHoraInicio.setText(timeFormat.format(parada.getHora_Inicio()));
            } else {
                textHoraInicio.setText("--:--");
            }

            if (parada.getHora_Fim() != null) {
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                textHoraFim.setText(timeFormat.format(parada.getHora_Fim()));
            } else {
                textHoraFim.setText("--:--");
            }

            // Duração calculada
            textDuracao.setText(calcularDuracao(parada.getHora_Inicio(), parada.getHora_Fim()));

            // Descrição resumida
            if (parada.getDes_parada() != null) {
                String descricao = parada.getDes_parada();
                if (descricao.length() > 60) {
                    descricao = descricao.substring(0, 57) + "...";
                }
                textDescricaoResumida.setText(descricao);
            } else {
                textDescricaoResumida.setText("Descrição não disponível");
            }

            // Status da parada - CHAMADA CORRIGIDA
            configurarStatus(parada);
        }

        private String calcularDuracao(java.util.Date horaInicio, java.util.Date horaFim) {
            if (horaInicio != null && horaFim != null) {
                try {
                    long diff = horaFim.getTime() - horaInicio.getTime();
                    long diffMinutes = diff / (60 * 1000);
                    long diffHours = diffMinutes / 60;
                    long remainingMinutes = diffMinutes % 60;

                    if (diffHours > 0) {
                        return String.format(Locale.getDefault(), "%dh %02dmin", diffHours, remainingMinutes);
                    } else {
                        return String.format(Locale.getDefault(), "%dmin", diffMinutes);
                    }
                } catch (Exception e) {
                    return "Erro cálculo";
                }
            } else if (horaInicio != null) {
                return "Em andamento";
            }
            return "Não calculável";
        }

        private void configurarStatus(Parada parada) {
            if (textStatusParada != null) {
                System.out.println("DEBUG Status - ID: " + parada.getId());

                // Lógica simplificada baseada no ID
                if (parada.getId() != null && parada.getId().matches("\\d+")) {
                    // ID numérico = SQL = FINALIZADA
                    textStatusParada.setText("Finalizada");
                    textStatusParada.setBackgroundResource(R.drawable.bg_status_concluido);
                    System.out.println("DEBUG Status: Finalizada (SQL - ID numérico)");
                } else {
                    // ID não numérico ou null = MongoDB = EM ANDAMENTO
                    textStatusParada.setText("Em Andamento");
                    textStatusParada.setBackgroundResource(R.drawable.bg_status_em_andamento);
                    System.out.println("DEBUG Status: Em Andamento (Mongo - ID não numérico)");
                }
            }
        }

        private void buscarNomeMaquina(Integer idMaquina) {
            if (idMaquina == null) {
                textNomeMaquina.setText("ID não informado");
                return;
            }

            textNomeMaquina.setText("Carregando...");

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

                        textNomeMaquina.setText(nomeMaquinaEncontrada);
                    } else {
                        textNomeMaquina.setText("Erro ao buscar");
                    }
                }

                @Override
                public void onFailure(Call<List<MaquinaResponseDTO>> call, Throwable t) {
                    textNomeMaquina.setText("Falha na conexão");
                }
            });
        }
    }
}