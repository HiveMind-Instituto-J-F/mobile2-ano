package com.aula.mobile_hivemind.ui.calendar;

import android.graphics.drawable.Drawable;

import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.CalendarDay;

import android.text.style.ForegroundColorSpan;
import java.util.Collection;
import java.util.HashSet;

public class EventoDecorador implements DayViewDecorator {

    private final HashSet<CalendarDay> datas;
    private final Drawable background;

    public EventoDecorador(Collection<CalendarDay> datas, Drawable background) {
        this.datas = new HashSet<>(datas);
        this.background = background;
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return datas.contains(day);
    }

    @Override
    public void decorate(DayViewFacade view) {
        view.setBackgroundDrawable(background);
        view.addSpan(new ForegroundColorSpan(0xFFFFFFFF)); // Branco
    }
}
