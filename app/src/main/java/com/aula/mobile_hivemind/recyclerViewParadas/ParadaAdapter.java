package com.aula.mobile_hivemind.recyclerViewParadas;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aula.mobile_hivemind.R;

import java.util.List;

public class ParadaAdapter extends RecyclerView.Adapter<com.aula.mobile_hivemind.recyclerViewParadas.ParadaAdapter.ViewHolder> {
    private List<com.aula.mobile_hivemind.recyclerViewParadas.Parada> listParadas;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(com.aula.mobile_hivemind.recyclerViewParadas.Parada parada);
    }

    public ParadaAdapter(List<com.aula.mobile_hivemind.recyclerViewParadas.Parada> listParadas) {
        this.listParadas = listParadas;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    @NonNull
    @Override
    public com.aula.mobile_hivemind.recyclerViewParadas.ParadaAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_parada, parent, false);
        return new com.aula.mobile_hivemind.recyclerViewParadas.ParadaAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull com.aula.mobile_hivemind.recyclerViewParadas.ParadaAdapter.ViewHolder holder, int position) {
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

    public void setParadas(List<com.aula.mobile_hivemind.recyclerViewParadas.Parada> paradas) {
        this.listParadas = paradas;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView textNomeMaquina;
        private TextView textDataParada;
        private TextView textCodigoColaborador;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textNomeMaquina = itemView.findViewById(R.id.textNomeMaquina);
            textDataParada = itemView.findViewById(R.id.textDataParada);
            textCodigoColaborador = itemView.findViewById(R.id.textCodigoColaborador);
        }

        public void bind(Parada parada) {
            textNomeMaquina.setText(parada.getNomeMaquina());
            textDataParada.setText("Data: " + parada.getDataParada());
            textCodigoColaborador.setText("Cód. Colaborador: " + parada.getCodigoColaborador());
        }
    }
}
