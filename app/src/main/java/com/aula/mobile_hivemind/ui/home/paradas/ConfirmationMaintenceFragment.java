package com.aula.mobile_hivemind.ui.home.paradas;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.aula.mobile_hivemind.MainActivity;
import com.aula.mobile_hivemind.R;
import com.aula.mobile_hivemind.notification.ParadaNotificationManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ConfirmationMaintenceFragment extends Fragment {
    private Button btnVoltar;

    // Dados para a notificação
    private String nomeEngenheiro, nomeMaquina, nomeUsuarioCriador, dataParada, horaInicio, horaFim;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Recuperar dados do Bundle
        Bundle bundle = getArguments();
        if (bundle != null) {
            nomeEngenheiro = bundle.getString("nomeEngenheiro", "Técnico");
            nomeMaquina = bundle.getString("nomeMaquina", "Máquina");
            nomeUsuarioCriador = bundle.getString("nomeUsuarioCriador", "Usuário");
            dataParada = bundle.getString("dataParada", "");
            horaInicio = bundle.getString("horaInicio", "");
            horaFim = bundle.getString("horaFim", "");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_confirmation, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnVoltar = view.findViewById(R.id.btnVoltar);

        enviarNotificacaoParadaConcluida();

        btnVoltar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFabAndBottomNav();
                NavController navController = Navigation.findNavController(v);
                navController.navigate(R.id.navigation_home);
            }
        });
    }

    private void enviarNotificacaoParadaConcluida() {
        try {
            // Calcular tempo de duração
            String tempoDuracao = calcularTempoDuracao();

            // Formatar data para exibição
            String dataFormatada = formatarDataParaExibicao(dataParada);

            // Chamar o método de notificação
            ParadaNotificationManager.enviarNotificacaoParadaFinalizada(
                    requireContext(),
                    nomeEngenheiro,
                    nomeMaquina,
                    tempoDuracao,
                    dataFormatada,
                    nomeUsuarioCriador
            );

        } catch (Exception e) {
            // Não mostrar erro para o usuário, pois a funcionalidade principal já foi concluída
            e.printStackTrace();
        }
    }

    private String calcularTempoDuracao() {
        try {
            if (horaInicio == null || horaInicio.isEmpty() || horaFim == null || horaFim.isEmpty()) {
                return "Tempo não calculado";
            }

            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            Date inicio = sdf.parse(horaInicio);
            Date fim = sdf.parse(horaFim);

            if (inicio == null || fim == null) {
                return "Tempo não calculado";
            }

            long diff = fim.getTime() - inicio.getTime();

            // Se a diferença for negativa, assumir que passou para o dia seguinte
            if (diff < 0) {
                diff += 24 * 60 * 60 * 1000; // Adicionar 24 horas
            }

            long horas = diff / (60 * 60 * 1000);
            long minutos = (diff % (60 * 60 * 1000)) / (60 * 1000);

            if (horas > 0) {
                return String.format(Locale.getDefault(), "%dh %dm", horas, minutos);
            } else {
                return String.format(Locale.getDefault(), "%dm", minutos);
            }

        } catch (Exception e) {
            return "Tempo calculado";
        }
    }

    private String formatarDataParaExibicao(String data) {
        try {
            if (data == null || data.isEmpty()) {
                // Se não tiver data, usar data atual
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                return sdf.format(new Date());
            }

            // Tentar diferentes formatos de data
            SimpleDateFormat sdfEntrada;
            if (data.matches("\\d{4}-\\d{2}-\\d{2}")) {
                sdfEntrada = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            } else if (data.matches("\\d{2}/\\d{2}/\\d{4}")) {
                sdfEntrada = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            } else {
                return data; // Retornar original se não reconhecer o formato
            }

            SimpleDateFormat sdfSaida = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date dataObj = sdfEntrada.parse(data);
            return sdfSaida.format(dataObj);

        } catch (Exception e) {
            return data; // Retorna original se não conseguir formatar
        }
    }

    private void hideFabAndBottomNav() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setFabVisibility(false);
            ((MainActivity) getActivity()).setBottomNavigationVisibility(false);
        }
    }

    private void showFabAndBottomNav() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setFabVisibility(true);
            ((MainActivity) getActivity()).setBottomNavigationVisibility(true);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        showFabAndBottomNav();
    }
}