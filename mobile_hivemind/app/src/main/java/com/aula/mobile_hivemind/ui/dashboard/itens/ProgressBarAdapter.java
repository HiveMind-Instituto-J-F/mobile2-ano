package com.aula.mobile_hivemind.ui.dashboard.itens;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.aula.mobile_hivemind.R;

import java.util.List;

public class ProgressBarAdapter extends RecyclerView.Adapter<ProgressBarAdapter.ViewHolder> {

    private List<ProgressItem> progressItems;

    public ProgressBarAdapter(List<ProgressItem> progressItems) {
        this.progressItems = progressItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_progress_bar, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ProgressItem item = progressItems.get(position);
        holder.tvLabel.setText(item.getLabel());
        holder.progressBar.setProgress(item.getProgress());
        holder.progressBar.setProgressTintList(ContextCompat.getColorStateList(holder.itemView.getContext(), item.getColor()));
    }

    @Override
    public int getItemCount() {
        return progressItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvLabel;
        ProgressBar progressBar;

        public ViewHolder(View itemView) {
            super(itemView);
            tvLabel = itemView.findViewById(R.id.tv_label);
            progressBar = itemView.findViewById(R.id.progress_bar);
        }
    }
}
