package com.aula.mobile_hivemind.recyclerViewParadas.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aula.mobile_hivemind.R;
import com.aula.mobile_hivemind.recyclerViewParadas.ParadaModel;

import java.util.List;

public class ParadaMOPAdapter extends RecyclerView.Adapter<ParadaMOPAdapter.MaquinaViewHolder> {

    private List<ParadaModel> listaMaquinas;

    public ParadaMOPAdapter(List<ParadaModel> listaMaquinas) {
        this.listaMaquinas = listaMaquinas;
    }

    // *** NOVO MÉTODO ***
    // Este método permite atualizar os dados no adapter e notificar o RecyclerView
    public void updateData(List<ParadaModel> newData) {
        this.listaMaquinas = newData;
        notifyDataSetChanged(); // Notifica o RecyclerView que os dados foram alterados
    }

    public static class MaquinaViewHolder extends RecyclerView.ViewHolder {
        TextView tvNome_IdMaq, tvSetprMaq, tvTempoParada, tvIdManutencao;

        public MaquinaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNome_IdMaq = itemView.findViewById(R.id.tvNome_IdMaq);
            tvSetprMaq = itemView.findViewById(R.id.tvSetorMaq);
            tvTempoParada = itemView.findViewById(R.id.tvTempoParada);
            tvIdManutencao = itemView.findViewById(R.id.tvIdManutencao);
        }
    }

    @NonNull
    @Override
    public ParadaMOPAdapter.MaquinaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.parada_item_recycleview_principal, parent, false);
        return new ParadaMOPAdapter.MaquinaViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ParadaMOPAdapter.MaquinaViewHolder holder, int position) {
        ParadaModel maquina = listaMaquinas.get(position);
        holder.tvNome_IdMaq.setText(maquina.getNome());
        holder.tvSetprMaq.setText(maquina.getSetor());
        holder.tvTempoParada.setText(maquina.getHora());
        holder.tvIdManutencao.setText(maquina.getIdManutencao());
    }

    @Override
    public int getItemCount() {
        return listaMaquinas.size();
    }
}
