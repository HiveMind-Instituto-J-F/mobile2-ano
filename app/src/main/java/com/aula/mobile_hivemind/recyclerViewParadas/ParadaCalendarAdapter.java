package com.aula.mobile_hivemind.recyclerViewParadas;

import android.app.AlertDialog;
import android.content.Context;
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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ParadaCalendarAdapter extends RecyclerView.Adapter<ParadaCalendarAdapter.MaquinaViewHolder> {

    private List<Parada> listaMaquinas;
    private Context context;
    private SqlApiService sqlApiService;
    private Map<Long, String> cacheNomesMaquinas = new HashMap<>();
    private boolean cacheCarregado = false;

    public ParadaCalendarAdapter(Context context, List<Parada> listaMaquinas, SqlApiService sqlApiService) {
        this.context = context;
        this.listaMaquinas = listaMaquinas;
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
                    notifyDataSetChanged(); // Atualiza a lista quando o cache estiver pronto
                }
            }

            @Override
            public void onFailure(Call<List<MaquinaResponseDTO>> call, Throwable t) {
                // Mesmo com falha, a lista será exibida
            }
        });
    }

    public void updateData(List<Parada> newData) {
        this.listaMaquinas = newData;
        notifyDataSetChanged();
    }

    public static class MaquinaViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitulo, tvHora;

        public MaquinaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.tvTitle);
            tvHora = itemView.findViewById(R.id.tvHorario);
        }

        public void bind(Parada maquina, Map<Long, String> cacheNomesMaquinas, boolean cacheCarregado) {
            // Buscar nome da máquina do cache
            if (cacheCarregado && maquina.getId_maquina() != null) {
                String nomeMaquina = cacheNomesMaquinas.get(maquina.getId_maquina().longValue());
                tvTitulo.setText(nomeMaquina != null ? nomeMaquina : "Máquina não encontrada");
            } else {
                tvTitulo.setText(maquina.getId_maquina() != null ? "Carregando..." : "ID não informado");
            }

            // Formatar data e hora
            if (maquina.getDt_parada() != null) {
                SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                String dataHoraFormatada = dateTimeFormat.format(maquina.getDt_parada());
                tvHora.setText(dataHoraFormatada);
            } else {
                tvHora.setText("Data não informada");
            }
        }
    }

    @NonNull
    @Override
    public MaquinaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.parada_item_recycleview_calendar, parent, false);
        return new MaquinaViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MaquinaViewHolder holder, int position) {
        Parada maquina = listaMaquinas.get(position);
        holder.bind(maquina, cacheNomesMaquinas, cacheCarregado);

        holder.itemView.setOnClickListener(v -> {
            // Buscar nome da máquina para o modal (pode ser diferente do cache se ainda não carregou)
            String nomeMaquinaModal = "Carregando...";
            if (cacheCarregado && maquina.getId_maquina() != null) {
                nomeMaquinaModal = cacheNomesMaquinas.getOrDefault(maquina.getId_maquina().longValue(), "Máquina não encontrada");
            }

            // Formatar data para exibição
            String dataFormatada = "Não informada";
            if (maquina.getDt_parada() != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                dataFormatada = dateFormat.format(maquina.getDt_parada());
            }

            // Formatar horas
            String horaInicio = "Não informada";
            String horaFim = "Não informada";
            if (maquina.getHora_Inicio() != null) {
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                horaInicio = timeFormat.format(maquina.getHora_Inicio());
            }
            if (maquina.getHora_Fim() != null) {
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                horaFim = timeFormat.format(maquina.getHora_Fim());
            }

            // Calcular duração
            String duracao = "Não calculável";
            if (maquina.getHora_Inicio() != null && maquina.getHora_Fim() != null) {
                long diff = maquina.getHora_Fim().getTime() - maquina.getHora_Inicio().getTime();
                long diffMinutes = diff / (60 * 1000);
                long diffHours = diffMinutes / 60;
                long remainingMinutes = diffMinutes % 60;

                if (diffHours > 0) {
                    duracao = String.format(Locale.getDefault(), "%dh %02dmin", diffHours, remainingMinutes);
                } else {
                    duracao = String.format(Locale.getDefault(), "%dmin", diffMinutes);
                }
            }

            String detalhes = "ID da máquina: " + (maquina.getId_maquina() != null ? maquina.getId_maquina() : "-") +
                    "\nNome da máquina: " + nomeMaquinaModal +
                    "\nCódigo Colaborador: " + (maquina.getId_usuario() != null ? maquina.getId_usuario() : "-") +
                    "\nSetor: " + (maquina.getDes_setor() != null ? maquina.getDes_setor() : "-") +
                    "\nDescrição: " + (maquina.getDes_parada() != null ? maquina.getDes_parada() : "-") +
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
        return listaMaquinas.size();
    }
}