package com.aula.mobile_hivemind.notification;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aula.mobile_hivemind.R;
import com.aula.mobile_hivemind.adapters.NotificationAdapter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NotificationHistoryFragment extends Fragment {

    private RecyclerView recyclerViewNotifications;
    private NotificationAdapter notificationAdapter;
    private List<Notification> notificationList;
    private List<Notification> allNotifications;
    private LinearLayout emptyState;
    private Spinner spinnerFilter;
    private Button btnClearFilter;
    private ImageButton btnBack;
    private SharedPreferences sharedPreferences;

    // TextViews para contadores
    private TextView txtTotalNotificacoes;
    private TextView txtNaoLidas;

    private static final String NOTIFICATIONS_KEY = "notifications_history";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notification_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sharedPreferences = requireContext().getSharedPreferences("NotificationPrefs", 0);

        initViews(view);
        setupRecyclerView();
        setupFilterSpinner();
        loadNotifications();
        setupClickListeners();
        updateCounters(); // Atualiza os contadores
    }

    private void initViews(View view) {
        recyclerViewNotifications = view.findViewById(R.id.recyclerViewNotifications);
        emptyState = view.findViewById(R.id.emptyState);
        spinnerFilter = view.findViewById(R.id.spinnerFilter);
        btnClearFilter = view.findViewById(R.id.btnClearFilter);
        btnBack = view.findViewById(R.id.btnBack);
        txtTotalNotificacoes = view.findViewById(R.id.txtTotalNotificacoes);
        txtNaoLidas = view.findViewById(R.id.txtNaoLidas);
    }

    private void setupRecyclerView() {
        notificationList = new ArrayList<>();
        allNotifications = new ArrayList<>();

        notificationAdapter = new NotificationAdapter(notificationList, new NotificationAdapter.OnNotificationClickListener() {
            @Override
            public void onNotificationClick(Notification notification) {
                markNotificationAsRead(notification);
            }

            @Override
            public void onNotificationLongClick(Notification notification) {
                showDeleteConfirmation(notification);
            }
        });

        recyclerViewNotifications.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewNotifications.setAdapter(notificationAdapter);
    }

    private void setupFilterSpinner() {
        String[] filterOptions = {
                "Todas as notificações",
                "Não lidas",
                "Paradas finalizadas",
                "Alertas do sistema",
                "Últimos 7 dias",
                "Últimos 30 dias"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                filterOptions
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilter.setAdapter(adapter);

        spinnerFilter.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                applyFilter(position);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> {
            Navigation.findNavController(v).popBackStack();
        });

        btnClearFilter.setOnClickListener(v -> {
            spinnerFilter.setSelection(0);
            applyFilter(0);
        });
    }

    private void loadNotifications() {
        // Usa o NotificationManager para carregar
        allNotifications = NotificationManager.getNotifications(requireContext());

        // Ordenar por timestamp (mais recente primeiro)
        Collections.sort(allNotifications, (n1, n2) -> Long.compare(n2.getTimestamp(), n1.getTimestamp()));

        notificationList.clear();
        notificationList.addAll(allNotifications);
        notificationAdapter.notifyDataSetChanged();

        updateEmptyState();
        updateCounters();
    }

    private List<Notification> createSampleNotifications() {
        List<Notification> sampleNotifications = new ArrayList<>();

        long currentTime = System.currentTimeMillis();

        sampleNotifications.add(new Notification(
                "Parada Finalizada",
                "A parada da máquina Prensa Hidráulica foi finalizada pelo engenheiro João Silva",
                "parada_finalizada",
                currentTime - (2 * 60 * 60 * 1000), // 2 horas atrás
                false
        ));

        sampleNotifications.add(new Notification(
                "Nova Parada Registrada",
                "Operador Carlos registrou uma nova parada na máquina Torno CNC",
                "nova_parada",
                currentTime - (5 * 60 * 60 * 1000), // 5 horas atrás
                true
        ));

        sampleNotifications.add(new Notification(
                "Alerta de Manutenção",
                "Máquina Injetora necessita de manutenção preventiva",
                "alerta_sistema",
                currentTime - (24 * 60 * 60 * 1000), // 1 dia atrás
                false
        ));

        return sampleNotifications;
    }

    private void applyFilter(int filterPosition) {
        notificationList.clear();

        long currentTime = System.currentTimeMillis();
        long sevenDaysAgo = currentTime - (7 * 24 * 60 * 60 * 1000);
        long thirtyDaysAgo = currentTime - (30 * 24 * 60 * 60 * 1000);

        switch (filterPosition) {
            case 0: // Todas
                notificationList.addAll(allNotifications);
                break;

            case 1: // Não lidas
                for (Notification notification : allNotifications) {
                    if (!notification.isRead()) {
                        notificationList.add(notification);
                    }
                }
                break;

            case 2: // Paradas finalizadas
                for (Notification notification : allNotifications) {
                    if ("parada_finalizada".equals(notification.getType())) {
                        notificationList.add(notification);
                    }
                }
                break;

            case 3: // Alertas do sistema
                for (Notification notification : allNotifications) {
                    if ("alerta_sistema".equals(notification.getType())) {
                        notificationList.add(notification);
                    }
                }
                break;

            case 4: // Últimos 7 dias
                for (Notification notification : allNotifications) {
                    if (notification.getTimestamp() >= sevenDaysAgo) {
                        notificationList.add(notification);
                    }
                }
                break;

            case 5: // Últimos 30 dias
                for (Notification notification : allNotifications) {
                    if (notification.getTimestamp() >= thirtyDaysAgo) {
                        notificationList.add(notification);
                    }
                }
                break;
        }

        notificationAdapter.notifyDataSetChanged();
        updateEmptyState();
        updateCounters(); // Atualiza contadores após filtrar
    }

    private void markNotificationAsRead(Notification notification) {
        notification.setRead(true);
        saveNotifications();

        // Encontra a notificação na lista e atualiza
        int position = notificationList.indexOf(notification);
        if (position != -1) {
            notificationAdapter.notifyItemChanged(position);
        }

        updateCounters(); // Atualiza contadores após marcar como lida
    }

    private void showDeleteConfirmation(Notification notification) {
        new android.app.AlertDialog.Builder(requireContext())
                .setTitle("Excluir Notificação")
                .setMessage("Deseja excluir esta notificação?")
                .setPositiveButton("Excluir", (dialog, which) -> {
                    deleteNotification(notification);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void deleteNotification(Notification notification) {
        // Remove de ambas as listas
        allNotifications.remove(notification);
        notificationList.remove(notification);

        saveNotifications();
        notificationAdapter.notifyDataSetChanged();
        updateEmptyState();
        updateCounters();
    }

    private void saveNotifications() {
        Gson gson = new Gson();
        String notificationsJson = gson.toJson(allNotifications);
        sharedPreferences.edit()
                .putString(NOTIFICATIONS_KEY, notificationsJson)
                .apply();
    }

    private void updateEmptyState() {
        if (notificationList.isEmpty()) {
            recyclerViewNotifications.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
        } else {
            recyclerViewNotifications.setVisibility(View.VISIBLE);
            emptyState.setVisibility(View.GONE);
        }
    }

    private void updateCounters() {
        // Atualiza o contador total
        txtTotalNotificacoes.setText(String.valueOf(allNotifications.size()));

        // Atualiza o contador de não lidas
        int unreadCount = 0;
        for (Notification notification : allNotifications) {
            if (!notification.isRead()) {
                unreadCount++;
            }
        }
        txtNaoLidas.setText(String.valueOf(unreadCount));
    }

    public void addNewNotification(Notification notification) {
        allNotifications.add(0, notification); // Adiciona no início
        saveNotifications();
        loadNotifications(); // Recarrega para atualizar a lista
    }

    public int getUnreadCount() {
        int count = 0;
        for (Notification notification : allNotifications) {
            if (!notification.isRead()) {
                count++;
            }
        }
        return count;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadNotifications(); // Recarrega quando o fragment for retomado
    }
}