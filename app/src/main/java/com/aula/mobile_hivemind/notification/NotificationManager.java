package com.aula.mobile_hivemind.notification;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class NotificationManager {
    private static final String NOTIFICATIONS_KEY = "notifications_history";
    private static final String PREFS_NAME = "NotificationPrefs";

    public static void addNotification(Context context, Notification notification) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Carrega notificações existentes
        List<Notification> notifications = getNotifications(context);

        // Adiciona a nova notificação no início
        notifications.add(0, notification);

        // Salva de volta
        saveNotifications(context, notifications);
    }

    public static List<Notification> getNotifications(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String notificationsJson = sharedPreferences.getString(NOTIFICATIONS_KEY, null);

        if (notificationsJson != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<Notification>>() {}.getType();
            List<Notification> notifications = gson.fromJson(notificationsJson, type);
            if (notifications != null) {
                return notifications;
            }
        }
        return new ArrayList<>();
    }

    private static void saveNotifications(Context context, List<Notification> notifications) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String notificationsJson = gson.toJson(notifications);
        sharedPreferences.edit().putString(NOTIFICATIONS_KEY, notificationsJson).apply();
    }

    public static int getUnreadCount(Context context) {
        List<Notification> notifications = getNotifications(context);
        int count = 0;
        for (Notification notification : notifications) {
            if (!notification.isRead()) {
                count++;
            }
        }
        return count;
    }
}