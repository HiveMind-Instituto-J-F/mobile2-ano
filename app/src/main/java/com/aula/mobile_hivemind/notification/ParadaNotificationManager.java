package com.aula.mobile_hivemind.notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.aula.mobile_hivemind.MainActivity;
import com.aula.mobile_hivemind.R;

public class ParadaNotificationManager {

    private static final String CHANNEL_ID = "parada_finalizada_channel";
    private static final int NOTIFICATION_ID = 1001;

    public static void enviarNotificacaoParadaFinalizada(
            Context context,
            String nomeEngenheiro,
            String nomeMaquina,
            String tempoDuracao,
            String dataParada,
            String nomeUsuarioCriador) {

        criarCanalNotificacao(context);

        // Intent para abrir o app quando clicar na notificação
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Criar a notificação visual
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("✅ Parada Finalizada")
                .setContentText("Parada da máquina " + nomeMaquina + " foi solucionada")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("A parada adicionada pelo usuário " + nomeUsuarioCriador +
                                " no dia " + dataParada + " da máquina " + nomeMaquina +
                                " foi solucionada pelo Engenheiro " + nomeEngenheiro +
                                ".\n\nTempo de duração: " + tempoDuracao +
                                "\n\nParada resolvida com sucesso! ✅"))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_STATUS)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setDefaults(NotificationCompat.DEFAULT_ALL);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        // Verificar permissão
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "Permissão para notificações necessária", Toast.LENGTH_SHORT).show();
            return;
        }

        notificationManager.notify(NOTIFICATION_ID, builder.build());

        // 🔹 Adicionar automaticamente ao histórico local
        com.aula.mobile_hivemind.notification.NotificationManager.addNotification(
                context,
                new com.aula.mobile_hivemind.notification.Notification(
                        "Parada Finalizada",
                        "A parada da máquina " + nomeMaquina +
                                " foi solucionada pelo engenheiro " + nomeEngenheiro +
                                ". Duração: " + tempoDuracao,
                        "parada_finalizada",
                        System.currentTimeMillis(),
                        false
                )
        );
    }

    private static void criarCanalNotificacao(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Finalização de Paradas",
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Notificações quando paradas são finalizadas pelos engenheiros");
            channel.enableVibration(true);
            channel.setShowBadge(true);
            channel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }
}
