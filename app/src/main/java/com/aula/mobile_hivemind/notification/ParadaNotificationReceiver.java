package com.aula.mobile_hivemind.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.core.app.NotificationManagerCompat;

import com.aula.mobile_hivemind.MainActivity;
import com.aula.mobile_hivemind.utils.CustomToast;

public class ParadaNotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String acao = intent.getStringExtra("acao");

        if ("visualizar_detalhes".equals(acao)) {
            CustomToast.showInfo(context, "Abrindo detalhes da parada...");

            // Intent para abrir a tela de detalhes da parada
            Intent detalhesIntent = new Intent(context, MainActivity.class);
            detalhesIntent.putExtra("abrir_paradas", true);
            detalhesIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(detalhesIntent);
        }

        // Fechar a notificação
        NotificationManagerCompat.from(context).cancel(1001);
    }
}