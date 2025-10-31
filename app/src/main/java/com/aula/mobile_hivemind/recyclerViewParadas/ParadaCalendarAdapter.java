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

        // ✅ CORRIGIDO: Usando os novos getters
        holder.tvTitulo.setText("Máquina ID: " + maquina.getId_maquina());

        // ✅ CORRIGIDO: Formatando a data
        String dataFormatada = "Data não informada";
        if (maquina.getDt_parada() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            dataFormatada = sdf.format(maquina.getDt_parada());
        }
        holder.tvHora.setText(dataFormatada);

        holder.itemView.setOnClickListener(v -> {
            // ✅ CORRIGIDO: Usando os novos getters no detalhe
            String dataDetalhe = "Data não informada";
            String horaInicio = "Não informada";
            String horaFim = "Não informada";

            if (maquina.getDt_parada() != null) {
                SimpleDateFormat sdfDetalhe = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                dataDetalhe = sdfDetalhe.format(maquina.getDt_parada());
            }

            if (maquina.getHora_Inicio() != null) {
                SimpleDateFormat sdfHora = new SimpleDateFormat("HH:mm", Locale.getDefault());
                horaInicio = sdfHora.format(maquina.getHora_Inicio());
            }

            if (maquina.getHora_Fim() != null) {
                SimpleDateFormat sdfHora = new SimpleDateFormat("HH:mm", Locale.getDefault());
                horaFim = sdfHora.format(maquina.getHora_Fim());
            }

            String detalhes = "ID: " + (maquina.getId() != null ? maquina.getId() : "-") +
                    "\nID da máquina: " + maquina.getId_maquina() +
                    "\nID do usuário: " + maquina.getId_usuario() +
                    "\nSetor: " + (maquina.getDes_setor() != null ? maquina.getDes_setor() : "-") +
                    "\nDescrição: " + (maquina.getDes_parada() != null ? maquina.getDes_parada() : "-") +
                    "\nData: " + dataDetalhe +
                    "\nHora Início: " + horaInicio +
                    "\nHora Fim: " + horaFim;

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