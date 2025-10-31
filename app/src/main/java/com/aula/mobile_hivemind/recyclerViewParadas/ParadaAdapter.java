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

    public ParadaAdapter(List<Parada> listParadas, SqlApiService sqlApiService) {
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
                .inflate(R.layout.item_parada, parent, false);
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
        private SqlApiService sqlApiService;

        public ViewHolder(@NonNull View itemView, SqlApiService sqlApiService) {
            super(itemView);
            this.sqlApiService = sqlApiService;
            textNomeMaquina = itemView.findViewById(R.id.textNomeMaquina);
            textDataParada = itemView.findViewById(R.id.textDataParada);
            textCodigoColaborador = itemView.findViewById(R.id.textCodigoColaborador);
        }

        public void bind(Parada parada) {
            if (parada.getDt_parada() != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd, MMM yyyy", Locale.getDefault());
                String dataFormatada = dateFormat.format(parada.getDt_parada());
                textDataParada.setText("Data: " + dataFormatada);
            } else {
                textDataParada.setText("Data: Não informada");
            }

            textCodigoColaborador.setText("Cód. Colaborador: " +
                    (parada.getId_usuario() != null ? String.valueOf(parada.getId_usuario()) : "Não informado"));

            buscarNomeMaquina(parada.getId_maquina(), textNomeMaquina);
        }

        private void buscarNomeMaquina(Integer idMaquina, TextView txtNomeMaquina) {
            if (idMaquina == null) {
                txtNomeMaquina.setText("ID não informado");
                return;
            }

            Call<List<MaquinaResponseDTO>> call = sqlApiService.listarMaquinas();
            call.enqueue(new Callback<List<MaquinaResponseDTO>>() {
                @Override
                public void onResponse(Call<List<MaquinaResponseDTO>> call, Response<List<MaquinaResponseDTO>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        List<MaquinaResponseDTO> maquinas = response.body();
                        String nomeMaquinaEncontrada = "Máquina não encontrada";

                        for (MaquinaResponseDTO maquina : maquinas) {
                            if (maquina.getId() != null && maquina.getId().equals(idMaquina.longValue())) {
                                nomeMaquinaEncontrada = maquina.getNome() != null ? maquina.getNome() : "Nome não disponível";
                                break;
                            }
                        }

                        txtNomeMaquina.setText(nomeMaquinaEncontrada);
                    } else {
                        txtNomeMaquina.setText("Erro ao buscar");
                    }
                }

                @Override
                public void onFailure(Call<List<MaquinaResponseDTO>> call, Throwable t) {
                    txtNomeMaquina.setText("Falha na conexão");
                }
            });
        }
    }
}
