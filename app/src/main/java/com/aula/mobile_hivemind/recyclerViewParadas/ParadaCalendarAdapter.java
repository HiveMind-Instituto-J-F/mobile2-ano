package com.aula.mobile_hivemind.recyclerViewParadas;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.aula.mobile_hivemind.R;
import com.aula.mobile_hivemind.api.SqlApiService;
import com.aula.mobile_hivemind.dto.MaquinaResponseDTO;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ParadaCalendarAdapter extends RecyclerView.Adapter<ParadaCalendarAdapter.ParadaViewHolder> {

    private List<Parada> listaParadas;
    private Context context;
    private SqlApiService sqlApiService;
    private Map<Long, String> cacheNomesMaquinas = new HashMap<>();
    private boolean cacheCarregado = false;

    public ParadaCalendarAdapter(Context context, List<Parada> listaParadas, SqlApiService sqlApiService) {
        this.context = context;
        this.listaParadas = listaParadas;
        this.sqlApiService = sqlApiService;
        carregarCacheMaquinas();
    }

    private void carregarCacheMaquinas() {
        if (sqlApiService == null) return;

        Call<List<MaquinaResponseDTO>> call = sqlApiService.listarMaquinas();
        call.enqueue(new Callback<List<MaquinaResponseDTO>>() {
            @Override
            public void onResponse(Call<List<MaquinaResponseDTO>> call, Response<List<MaquinaResponseDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (MaquinaResponseDTO maquina : response.body()) {
                        cacheNomesMaquinas.put(maquina.getId(), maquina.getNome());
                    }
                    cacheCarregado = true;
                    notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<MaquinaResponseDTO>> call, Throwable t) {
                // Mesmo com falha, a lista será exibida
            }
        });
    }

    public void updateData(List<Parada> newData) {
        this.listaParadas = newData;
        notifyDataSetChanged();
    }

    public static class ParadaViewHolder extends RecyclerView.ViewHolder {
        TextView tvDescricao, tvSetor, tvData, tvStatus;
        View indicator;

        public ParadaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDescricao = itemView.findViewById(R.id.textDescricao);
            tvSetor = itemView.findViewById(R.id.textSetor);
            tvData = itemView.findViewById(R.id.textData);
            tvStatus = itemView.findViewById(R.id.textStatus);
            indicator = itemView.findViewById(R.id.indicator);
        }

        public void bind(Parada parada, Context context) {
            // Descrição da parada
            tvDescricao.setText(parada.getDes_parada() != null ? parada.getDes_parada() : "Sem descrição");

            // Setor
            tvSetor.setText(parada.getDes_setor() != null ? parada.getDes_setor() : "Setor não informado");

            // Data formatada
            if (parada.getDt_parada() != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                tvData.setText(dateFormat.format(parada.getDt_parada()));
            } else {
                tvData.setText("Data não informada");
            }

            // Status (Em andamento ou Finalizada)
            if ("EM_ANDAMENTO".equals(parada.getTipo())) {
                tvStatus.setText("Em Andamento");
                tvStatus.setBackgroundColor(ContextCompat.getColor(context, android.R.color.holo_orange_light));
                indicator.setBackgroundColor(ContextCompat.getColor(context, android.R.color.holo_orange_light));
            } else {
                tvStatus.setText("Finalizada");
                tvStatus.setBackgroundColor(ContextCompat.getColor(context, android.R.color.holo_green_light));
                indicator.setBackgroundColor(ContextCompat.getColor(context, android.R.color.holo_green_light));
            }
        }
    }

    @NonNull
    @Override
    public ParadaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.parada_item_recycleview_calendar, parent, false);
        return new ParadaViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ParadaViewHolder holder, int position) {
        Parada parada = listaParadas.get(position);
        holder.bind(parada, context);

        holder.itemView.setOnClickListener(v -> {
            // Buscar nome da máquina para o modal
            String nomeMaquinaModal = "Carregando...";
            if (cacheCarregado && parada.getId_maquina() != null) {
                nomeMaquinaModal = cacheNomesMaquinas.getOrDefault(parada.getId_maquina().longValue(), "Máquina não encontrada");
            }

            // Formatar data para exibição
            String dataFormatada = "Não informada";
            if (parada.getDt_parada() != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                dataFormatada = dateFormat.format(parada.getDt_parada());
            }

            // Formatar horas
            String horaInicio = "Não informada";
            String horaFim = "Não informada";
            if (parada.getHora_Inicio() != null) {
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                horaInicio = timeFormat.format(parada.getHora_Inicio());
            }
            if (parada.getHora_Fim() != null) {
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                horaFim = timeFormat.format(parada.getHora_Fim());
            }

            // Calcular duração
            String duracao = "Não calculável";
            if (parada.getHora_Inicio() != null && parada.getHora_Fim() != null) {
                long diff = parada.getHora_Fim().getTime() - parada.getHora_Inicio().getTime();
                long diffMinutes = diff / (60 * 1000);
                long diffHours = diffMinutes / 60;
                long remainingMinutes = diffMinutes % 60;

                if (diffHours > 0) {
                    duracao = String.format(Locale.getDefault(), "%dh %02dmin", diffHours, remainingMinutes);
                } else {
                    duracao = String.format(Locale.getDefault(), "%dmin", diffMinutes);
                }
            }

            String status = "EM_ANDAMENTO".equals(parada.getTipo()) ? "Em Andamento" : "Finalizada";

            String detalhes = "Status: " + status +
                    "\nID da máquina: " + (parada.getId_maquina() != null ? parada.getId_maquina() : "-") +
                    "\nNome da máquina: " + nomeMaquinaModal +
                    "\nCódigo Colaborador: " + (parada.getId_usuario() != null ? parada.getId_usuario() : "-") +
                    "\nSetor: " + (parada.getDes_setor() != null ? parada.getDes_setor() : "-") +
                    "\nDescrição: " + (parada.getDes_parada() != null ? parada.getDes_parada() : "-") +
                    "\nData: " + dataFormatada +
                    "\nHora Início: " + horaInicio +
                    "\nHora Término: " + horaFim +
                    "\nDuração: " + duracao;

            new AlertDialog.Builder(context)
                    .setTitle("Detalhes da Parada")
                    .setMessage(detalhes)
                    .setPositiveButton("OK", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return listaParadas.size();
    }
}