package com.aula.mobile_hivemind.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.aula.mobile_hivemind.R;
import com.aula.mobile_hivemind.notification.Notification;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private List<Notification> notifications;
    private OnNotificationClickListener listener;

    public interface OnNotificationClickListener {
        void onNotificationClick(Notification notification);
        void onNotificationLongClick(Notification notification);
    }

    public NotificationAdapter(List<Notification> notifications, OnNotificationClickListener listener) {
        this.notifications = notifications;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notifications.get(position);
        holder.bind(notification);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNotificationClick(notification);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onNotificationLongClick(notification);
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    public void updateData(List<Notification> newNotifications) {
        this.notifications = newNotifications;
        notifyDataSetChanged();
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        private TextView title, message, date, time, badge;
        private ImageView icon;
        private View indicator;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            message = itemView.findViewById(R.id.message);
            date = itemView.findViewById(R.id.date);
            time = itemView.findViewById(R.id.time);
            badge = itemView.findViewById(R.id.badge);
            icon = itemView.findViewById(R.id.icon);
            indicator = itemView.findViewById(R.id.indicator);
        }

        public void bind(Notification notification) {
            title.setText(notification.getTitle());
            message.setText(notification.getMessage());

            // Formatar data e hora
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

            date.setText(dateFormat.format(notification.getDate()));
            time.setText(timeFormat.format(notification.getDate()));

            // Configurar aparência baseada no tipo e status
            setupAppearance(notification);
        }

        private void setupAppearance(Notification notification) {
            // Configurar cores baseadas no tipo e status de leitura
            if (!notification.isRead()) {
                // Não lida - destaque
                indicator.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), android.R.color.holo_blue_light));
                badge.setVisibility(View.VISIBLE);
                title.setAlpha(1.0f);
                message.setAlpha(1.0f);
            } else {
                // Lida - opaca
                indicator.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), android.R.color.darker_gray));
                badge.setVisibility(View.GONE);
                title.setAlpha(0.7f);
                message.setAlpha(0.7f);
            }

            // Configurar ícone baseado no tipo - USANDO ÍCONES DO ANDROID
            switch (notification.getType()) {
                case "parada_finalizada":
                    // Ícone de check/sucesso
                    icon.setImageResource(android.R.drawable.ic_input_add);
                    icon.setColorFilter(ContextCompat.getColor(itemView.getContext(), android.R.color.holo_green_dark));
                    break;
                case "nova_parada":
                    // Ícone de alerta
                    icon.setImageResource(android.R.drawable.ic_dialog_alert);
                    icon.setColorFilter(ContextCompat.getColor(itemView.getContext(), android.R.color.holo_orange_dark));
                    break;
                case "alerta_sistema":
                    // Ícone de informação
                    icon.setImageResource(android.R.drawable.ic_dialog_info);
                    icon.setColorFilter(ContextCompat.getColor(itemView.getContext(), android.R.color.holo_blue_dark));
                    break;
                default:
                    // Ícone padrão
                    icon.setImageResource(android.R.drawable.ic_popup_reminder);
                    icon.setColorFilter(ContextCompat.getColor(itemView.getContext(), android.R.color.darker_gray));
                    break;
            }
        }
    }
}