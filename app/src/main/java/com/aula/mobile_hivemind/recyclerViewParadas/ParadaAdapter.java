package com.aula.mobile_hivemind.recyclerViewParadas;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aula.mobile_hivemind.R;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ParadaAdapter extends RecyclerView.Adapter<ParadaAdapter.ViewHolder> {
    private List<Parada> listParadas;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(Parada parada);
    }

    public ParadaAdapter(List<Parada> listParadas) {
        this.listParadas = listParadas;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_parada, parent, false);
        return new ViewHolder(view);
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
        private TextView textIdMaquina;
        private TextView textDataParada;
        private TextView textIdUsuario;
        private TextView textSetor;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textIdMaquina = itemView.findViewById(R.id.textIdMaquina);
            textDataParada = itemView.findViewById(R.id.textDataParada);
            textIdUsuario = itemView.findViewById(R.id.textIdUsuario);
            textSetor = itemView.findViewById(R.id.textSetor);
        }

        public void bind(Parada parada) {
            // Formatando a data
            String dataFormatada = "Data: N/A";
            if (parada.getDt_parada() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                dataFormatada = "Data: " + sdf.format(parada.getDt_parada());
            }

            textIdMaquina.setText("ID Máquina: " + parada.getId_maquina());
            textDataParada.setText(dataFormatada);
            textIdUsuario.setText("ID Usuário: " + parada.getId_usuario());
            textSetor.setText("Setor: " + parada.getDes_setor());
        }
    }
}