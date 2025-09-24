package com.aula.mobile_hivemind.ui.calendar;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.aula.mobile_hivemind.databinding.FragmentCalendarBinding;
import com.aula.mobile_hivemind.recyclerViewParadas.adapter.ParadaCalendarAdapter;
import com.aula.mobile_hivemind.recyclerViewParadas.ParadaModel;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;

public class CalendarFragment extends Fragment {

    private FragmentCalendarBinding binding;
    private ParadaCalendarAdapter paradaCalendarAdapter;
    private List<ParadaModel> todasParadas;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentCalendarBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        MaterialCalendarView calendarView = binding.calendarView;

        // Título do mês em português
        calendarView.setTitleFormatter(day -> {
            SimpleDateFormat format = new SimpleDateFormat("MMMM yyyy", new Locale("pt", "BR"));
            return "     " + format.format(day.getDate());
        });

        // Remove setas
        calendarView.setLeftArrowMask(null);
        calendarView.setRightArrowMask(null);

        // Decora os dias do mês atual
        calendarView.addDecorator(new DayViewDecorator() {
            @Override
            public boolean shouldDecorate(CalendarDay day) {
                Calendar systemCalendar = Calendar.getInstance();
                int currentMonth = systemCalendar.get(Calendar.MONTH);
                int currentYear = systemCalendar.get(Calendar.YEAR);

                Calendar dayCalendar = day.getCalendar();
                int dayMonth = dayCalendar.get(Calendar.MONTH);
                int dayYear = dayCalendar.get(Calendar.YEAR);

                return dayMonth == currentMonth && dayYear == currentYear;
            }

            @Override
            public void decorate(DayViewFacade view) {
                view.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                view.addSpan(new ForegroundColorSpan(Color.BLACK)); // Branco
            }
        });

        // Decoradores visuais
        decorateOtherMonthDays(calendarView);

        // Lista completa de paradas
        todasParadas = getAllParadasFromDatabase();

        // Marcar datas no calendário
        marcarParadasNoCalendario(calendarView, todasParadas);

        // Configuração do RecyclerView
        paradaCalendarAdapter = new ParadaCalendarAdapter(getContext(), todasParadas);
        binding.recyclerViewCalendar.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewCalendar.setAdapter(paradaCalendarAdapter);

        // Listener de clique no calendário
        calendarView.setOnDateChangedListener((widget, date, selected) -> {
            filtrarParadasPorData(date);
        });

        return root;
    }

    private void decorateOtherMonthDays(MaterialCalendarView calendarView) {
        Calendar systemCalendar = Calendar.getInstance();
        int currentMonth = systemCalendar.get(Calendar.MONTH);
        int currentYear = systemCalendar.get(Calendar.YEAR);

        calendarView.addDecorator(new DayViewDecorator() {
            @Override
            public boolean shouldDecorate(CalendarDay day) {
                Calendar dayCalendar = day.getCalendar();
                int dayMonth = dayCalendar.get(Calendar.MONTH);
                int dayYear = dayCalendar.get(Calendar.YEAR);
                return dayMonth != currentMonth || dayYear != currentYear;
            }

            @Override
            public void decorate(DayViewFacade view) {
                view.addSpan(new ForegroundColorSpan(Color.GRAY));
            }
        });
    }


    private Map<CalendarDay, Integer> marcarParadasNoCalendario(MaterialCalendarView calendarView, List<ParadaModel> paradas) {
        Map<CalendarDay, Integer> mapaCores = new HashMap<>();

        for (ParadaModel parada : paradas) {
            if (parada.getData() != null) {
                CalendarDay dia = CalendarDay.from(parada.getData());
                if (!mapaCores.containsKey(dia)) {
                    int cor = Color.rgb(0, 0, 126);
                    mapaCores.put(dia, cor);
                }
            }
        }

        for (Map.Entry<CalendarDay, Integer> entry : mapaCores.entrySet()) {
            List<CalendarDay> unicaData = new ArrayList<>();
            unicaData.add(entry.getKey());

            Drawable fundo = new ColorDrawable(entry.getValue());
            calendarView.addDecorator(new EventoDecorador(unicaData, fundo));
        }

        return mapaCores;
    }

    // Filtra o RecyclerView para mostrar apenas itens do dia selecionado
    private void filtrarParadasPorData(CalendarDay dataSelecionada) {
        List<ParadaModel> filtradas = new ArrayList<>();

        for (ParadaModel parada : todasParadas) {
            if (parada.getData() != null) {
                Calendar diaParada = parada.getData();
                if (diaParada.get(Calendar.YEAR) == dataSelecionada.getYear() &&
                        diaParada.get(Calendar.MONTH) == dataSelecionada.getMonth() &&
                        diaParada.get(Calendar.DAY_OF_MONTH) == dataSelecionada.getDay()) {
                    filtradas.add(parada);
                }
            }
        }

        paradaCalendarAdapter.updateData(filtradas);
    }

    private List<ParadaModel> getAllParadasFromDatabase() {
        List<ParadaModel> paradas = new ArrayList<>();
        Calendar hoje = Calendar.getInstance();

        paradas.add(new ParadaModel("Parada Estação Central", "Setor A", "08:00", hoje, "Ronaldo"));

        return paradas;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
