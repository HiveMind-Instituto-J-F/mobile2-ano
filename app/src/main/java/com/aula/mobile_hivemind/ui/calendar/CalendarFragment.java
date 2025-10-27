package com.aula.mobile_hivemind.ui.calendar;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.aula.mobile_hivemind.databinding.FragmentCalendarBinding;
import com.aula.mobile_hivemind.recyclerViewParadas.Parada;
import com.aula.mobile_hivemind.recyclerViewParadas.ParadaCalendarAdapter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;

public class CalendarFragment extends Fragment {

    private FragmentCalendarBinding binding;
    private ParadaCalendarAdapter paradaCalendarAdapter;
    private List<Parada> todasParadas;
    private FirebaseFirestore db;
    private SimpleDateFormat dateFormat;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentCalendarBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Inicializar Firebase
        db = FirebaseFirestore.getInstance();
        dateFormat = new SimpleDateFormat("dd, MMM yyyy", Locale.getDefault());

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
        calendarView.addDecorator(new CurrentMonthDecorator());

        // Decoradores visuais para outros meses
        decorateOtherMonthDays(calendarView);

        // Inicializar lista
        todasParadas = new ArrayList<>();

        // Configuração do RecyclerView
        paradaCalendarAdapter = new ParadaCalendarAdapter(getContext(), todasParadas);
        binding.recyclerViewCalendar.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewCalendar.setAdapter(paradaCalendarAdapter);

        // Carregar paradas do Firebase
        carregarParadasDoFirebase(calendarView);

        // Listener de clique no calendário
        calendarView.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                filtrarParadasPorData(date);
            }
        });

        return root;
    }

    private void carregarParadasDoFirebase(MaterialCalendarView calendarView) {
        db.collection("paradas")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        todasParadas.clear();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Parada parada = document.toObject(Parada.class);
                            parada.setId(document.getId());
                            todasParadas.add(parada);
                        }

                        // Atualizar adapter e marcar datas no calendário
                        paradaCalendarAdapter.updateData(todasParadas);
                        marcarParadasNoCalendario(calendarView, todasParadas);

                    } else {
                        Toast.makeText(getContext(), "Erro ao carregar paradas", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void decorateOtherMonthDays(MaterialCalendarView calendarView) {
        calendarView.addDecorator(new DayViewDecorator() {
            @Override
            public boolean shouldDecorate(CalendarDay day) {
                Calendar currentCalendar = Calendar.getInstance();
                Calendar dayCalendar = day.getCalendar();
                return dayCalendar.get(Calendar.MONTH) != currentCalendar.get(Calendar.MONTH) ||
                        dayCalendar.get(Calendar.YEAR) != currentCalendar.get(Calendar.YEAR);
            }

            @Override
            public void decorate(DayViewFacade view) {
                view.addSpan(new ForegroundColorSpan(Color.GRAY));
            }
        });
    }

    private void marcarParadasNoCalendario(MaterialCalendarView calendarView, List<Parada> paradas) {
        Map<CalendarDay, Integer> mapaCores = new HashMap<>();

        for (Parada parada : paradas) {
            if (parada.getDataParada() != null && !parada.getDataParada().isEmpty()) {
                try {
                    Date date = dateFormat.parse(parada.getDataParada());
                    if (date != null) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(date);
                        CalendarDay dia = CalendarDay.from(calendar);

                        if (!mapaCores.containsKey(dia)) {
                            int cor = Color.rgb(0, 0, 126); // Azul escuro
                            mapaCores.put(dia, cor);

                            // Adicionar decorador para esta data
                            List<CalendarDay> datas = new ArrayList<>();
                            datas.add(dia);
                            calendarView.addDecorator(new EventoDecorador(datas, new ColorDrawable(cor)));
                        }
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // Filtra o RecyclerView para mostrar apenas itens do dia selecionado
    private void filtrarParadasPorData(CalendarDay dataSelecionada) {
        List<Parada> filtradas = new ArrayList<>();

        for (Parada parada : todasParadas) {
            if (parada.getDataParada() != null && !parada.getDataParada().isEmpty()) {
                try {
                    Date date = dateFormat.parse(parada.getDataParada());
                    if (date != null) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(date);

                        if (calendar.get(Calendar.YEAR) == dataSelecionada.getYear() &&
                                calendar.get(Calendar.MONTH) == dataSelecionada.getMonth() &&
                                calendar.get(Calendar.DAY_OF_MONTH) == dataSelecionada.getDay()) {
                            filtradas.add(parada);
                        }
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }

        paradaCalendarAdapter.updateData(filtradas);
    }

    // Decorador para o mês atual
    private class CurrentMonthDecorator implements DayViewDecorator {
        @Override
        public boolean shouldDecorate(CalendarDay day) {
            Calendar currentCalendar = Calendar.getInstance();
            Calendar dayCalendar = day.getCalendar();
            return dayCalendar.get(Calendar.MONTH) == currentCalendar.get(Calendar.MONTH) &&
                    dayCalendar.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR);
        }

        @Override
        public void decorate(DayViewFacade view) {
            view.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            view.addSpan(new ForegroundColorSpan(Color.BLACK));
        }
    }

    // Decorador para eventos
    private class EventoDecorador implements DayViewDecorator {
        private final List<CalendarDay> dates;
        private final Drawable drawable;

        public EventoDecorador(List<CalendarDay> dates, Drawable drawable) {
            this.dates = dates;
            this.drawable = drawable;
        }

        @Override
        public boolean shouldDecorate(CalendarDay day) {
            return dates.contains(day);
        }

        @Override
        public void decorate(DayViewFacade view) {
            view.setBackgroundDrawable(drawable);
            view.addSpan(new ForegroundColorSpan(Color.WHITE)); // Texto branco sobre fundo colorido
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}