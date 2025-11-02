package com.aula.mobile_hivemind.utils;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.aula.mobile_hivemind.R;

public class CustomToast {

    public static final int TYPE_SUCCESS = 1;
    public static final int TYPE_ERROR = 2;
    public static final int TYPE_WARNING = 3;
    public static final int TYPE_INFO = 4;

    public static void show(Context context, String message, int type) {
        show(context, message, type, Toast.LENGTH_SHORT);
    }

    public static void show(Context context, String message, int type, int duration) {
        // Inflar o layout customizado
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.custom_toast, null);

        // Configurar elementos do Toast
        ImageView icon = layout.findViewById(R.id.toast_icon);
        TextView text = layout.findViewById(R.id.toast_message);

        // Configurar baseado no tipo
        switch (type) {
            case TYPE_SUCCESS:
                icon.setImageResource(R.drawable.ic_success);
                layout.setBackground(context.getDrawable(R.drawable.toast_success));
                break;
            case TYPE_ERROR:
                icon.setImageResource(R.drawable.ic_error);
                layout.setBackground(context.getDrawable(R.drawable.toast_error));
                break;
            case TYPE_WARNING:
                icon.setImageResource(R.drawable.ic_warning);
                layout.setBackground(context.getDrawable(R.drawable.toast_warning));
                break;
            case TYPE_INFO:
            default:
                icon.setImageResource(R.drawable.ic_info);
                layout.setBackground(context.getDrawable(R.drawable.toast_info));
                break;
        }

        text.setText(message);

        // Criar e configurar o Toast
        Toast toast = new Toast(context);
        toast.setDuration(duration);
        toast.setView(layout);
        toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 100);
        toast.show();
    }

    // Métodos específicos para cada tipo
    public static void showSuccess(Context context, String message) {
        show(context, message, TYPE_SUCCESS);
    }

    public static void showError(Context context, String message) {
        show(context, message, TYPE_ERROR);
    }

    public static void showWarning(Context context, String message) {
        show(context, message, TYPE_WARNING);
    }

    public static void showInfo(Context context, String message) {
        show(context, message, TYPE_INFO);
    }

    // Toast com posição personalizada
    public static void showBottom(Context context, String message, int type) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.custom_toast, null);

        ImageView icon = layout.findViewById(R.id.toast_icon);
        TextView text = layout.findViewById(R.id.toast_message);

        switch (type) {
            case TYPE_SUCCESS:
                icon.setImageResource(R.drawable.ic_success);
                layout.setBackground(context.getDrawable(R.drawable.toast_success));
                break;
            case TYPE_ERROR:
                icon.setImageResource(R.drawable.ic_error);
                layout.setBackground(context.getDrawable(R.drawable.toast_error));
                break;
            case TYPE_WARNING:
                icon.setImageResource(R.drawable.ic_warning);
                layout.setBackground(context.getDrawable(R.drawable.toast_warning));
                break;
            default:
                icon.setImageResource(R.drawable.ic_info);
                layout.setBackground(context.getDrawable(R.drawable.toast_info));
                break;
        }

        text.setText(message);

        Toast toast = new Toast(context);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 100);
        toast.show();
    }
}