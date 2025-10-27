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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ParadaCalendarAdapter extends RecyclerView.Adapter<ParadaCalendarAdapter.MaquinaViewHolder> {

    private List<Parada> listaMaquinas;
    private Context context;

    public ParadaCalendarAdapter(Context context, List<Parada> listaMaquinas) {
        this.context = context;
        this.listaMaquinas = listaMaquinas;
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

        holder.tvTitulo.setText(maquina.getNomeMaquina());

        // Se não tiver hora, mostrar a data ou deixar vazio
        if (maquina.getDataParada() != null) {
            holder.tvHora.setText(maquina.getDataParada());
        } else {
            holder.tvHora.setText("Data não informada");
        }

        holder.itemView.setOnClickListener(v -> {
            String detalhes = "ID da máquina: " + maquina.getIdMaquina() +
                    "\nNome da máquina: " + maquina.getNomeMaquina() +
                    "\nCódigo Colaborador: " + (maquina.getCodigoColaborador() != null ? maquina.getCodigoColaborador() : "-") +
                    "\nSetor: " + (maquina.getSetor() != null ? maquina.getSetor() : "-") +
                    "\nDescrição: " + (maquina.getDescricaoParada() != null ? maquina.getDescricaoParada() : "-") +
                    "\nData: " + (maquina.getDataParada() != null ? maquina.getDataParada() : "-");

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