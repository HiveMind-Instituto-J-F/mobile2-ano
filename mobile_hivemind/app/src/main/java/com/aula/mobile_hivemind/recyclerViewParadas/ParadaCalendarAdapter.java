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
import java.util.List;
import java.util.Locale;

public class ParadaCalendarAdapter extends RecyclerView.Adapter<ParadaCalendarAdapter.MaquinaViewHolder> {

    private List<ParadaModel> listaMaquinas;
    private Context context;

    public ParadaCalendarAdapter(Context context, List<ParadaModel> listaMaquinas) {
        this.context = context;
        this.listaMaquinas = listaMaquinas;
    }

    public void updateData(List<ParadaModel> newData) {
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
        ParadaModel maquina = listaMaquinas.get(position);

        holder.tvTitulo.setText(maquina.getNome());
        holder.tvHora.setText(maquina.getHora());

        holder.itemView.setOnClickListener(v -> {
            String dataFormatada = "";
            if (maquina.getData() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", new Locale("pt", "BR"));
                dataFormatada = sdf.format(maquina.getData().getTime());
            }

            String detalhes = "Nome: " + maquina.getNome() +
                    "\nSetor: " + (maquina.getSetor() != null ? maquina.getSetor() : "-") +
                    "\nHora: " + (maquina.getHora() != null ? maquina.getHora() : "-") +
                    "\nData: " + (dataFormatada.isEmpty() ? "-" : dataFormatada) +
                    "\nDescrição: " + (maquina.getDescricao() != null ? maquina.getDescricao() : "-");

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
