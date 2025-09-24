package com.aula.mobile_hivemind.ui.home_rh;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aula.mobile_hivemind.R;
import com.aula.mobile_hivemind.ui.dashboard.itens.ProgressBarAdapter;
import com.aula.mobile_hivemind.ui.dashboard.itens.ProgressItem;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HomeRhFragment extends Fragment {

    private ChipGroup chipGroupSetores;
    private Chip chipTodos;
    private RecyclerView recyclerViewProgressBars;
    private PieChart pieChart;
    private ImageButton filtrarParadas;

    public HomeRhFragment() {
        // Construtor público vazio requerido
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home_rh, container, false);

        pieChart = root.findViewById(R.id.pieChart);
        recyclerViewProgressBars = root.findViewById(R.id.recyclerViewProgressBars);
        chipGroupSetores = root.findViewById(R.id.chipGroupSetores);
        filtrarParadas = root.findViewById(R.id.filterButton);

        setupPieChart();
        setupProgressBars();

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        List<ProgressItem> allItems = getProgressItemsFromDatabase();

        // pega setores únicos
        Set<String> uniqueSectors = new HashSet<>();
        for (ProgressItem item : allItems) {
            if (item.getLabel() != null && !item.getLabel().trim().isEmpty()) {
                uniqueSectors.add(item.getLabel());
            }
        }

        List<String> sectorsList = new ArrayList<>(uniqueSectors);
        Collections.sort(sectorsList);

        // botão para mostrar/esconder filtros
        filtrarParadas.setOnClickListener(v -> {
            if (chipGroupSetores.getVisibility() == View.GONE) {
                chipGroupSetores.setVisibility(View.VISIBLE);
            } else {
                chipGroupSetores.setVisibility(View.GONE);
            }
        });

        chipGroupSetores.setVisibility(View.GONE);
        addChipsToChipGroup(sectorsList);
    }

    private void addChipsToChipGroup(List<String> sectors) {
        chipGroupSetores.removeAllViews();

        // Chip "Todos"
        chipTodos = new Chip(getContext());
        chipTodos.setText("Todos");
        chipTodos.setId(View.generateViewId());
        chipTodos.setCheckable(true);
        chipTodos.setClickable(true);
        chipTodos.setChecked(true);

        // Chips de setores
        for (String sectorName : sectors) {
            Chip chip = new Chip(getContext());
            chip.setText(sectorName);
            chip.setId(View.generateViewId());
            chip.setCheckable(true);
            chipGroupSetores.addView(chip);
        }

        chipGroupSetores.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                chipTodos.setChecked(true);
                updatePieChart(sectors);
                updateProgressBars(sectors);
            } else {
                List<String> selectedSectors = new ArrayList<>();

                for (int id : checkedIds) {
                    Chip chip = group.findViewById(id);
                    if (chip != null) {
                        String name = chip.getText().toString();
                        if (name.equals("Todos")) {
                            selectedSectors = new ArrayList<>(sectors);
                            break;
                        } else {
                            selectedSectors.add(name);
                        }
                    }
                }

                updatePieChart(selectedSectors);
                updateProgressBars(selectedSectors);

                if (!selectedSectors.contains("Todos")) {
                    chipTodos.setChecked(false);
                }
            }
        });
    }

    private void setupPieChart() {
        List<ProgressItem> progressItems = getProgressItemsFromDatabase();
        ArrayList<PieEntry> entries = new ArrayList<>();

        float total = 0f;
        for (ProgressItem item : progressItems) {
            entries.add(new PieEntry(item.getProgress(), item.getLabel()));
            total += item.getProgress();
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);

        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.parseColor("#EF4444"));
        colors.add(Color.parseColor("#EC2B7B"));
        colors.add(Color.parseColor("#B68DF6"));
        colors.add(Color.parseColor("#8059E7"));
        colors.add(Color.parseColor("#4C65F1"));
        colors.add(Color.parseColor("#26C8D8"));
        colors.add(Color.parseColor("#43D8B0"));
        colors.add(Color.parseColor("#82E762"));
        colors.add(Color.parseColor("#FFD95D"));
        dataSet.setColors(colors);

        PieData pieData = new PieData(dataSet);
        pieData.setValueFormatter(new PercentFormatter(pieChart));
        pieData.setValueTextSize(11f);
        pieData.setValueTextColor(Color.WHITE);

        pieChart.setData(pieData);
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setHoleColor(Color.TRANSPARENT);
        pieChart.setHoleRadius(58f);
        pieChart.setTransparentCircleRadius(61f);
        pieChart.setDrawCenterText(true);
        pieChart.setCenterText("$" + String.format("%.0f", total));
        pieChart.setCenterTextColor(Color.WHITE);
        pieChart.setCenterTextSize(24f);

        Legend legend = pieChart.getLegend();
        legend.setEnabled(false);

        pieChart.invalidate();
    }

    private void setupProgressBars() {
        List<ProgressItem> progressItems = getProgressItemsFromDatabase();
        recyclerViewProgressBars.setLayoutManager(new LinearLayoutManager(getContext()));
        ProgressBarAdapter adapter = new ProgressBarAdapter(progressItems);
        recyclerViewProgressBars.setAdapter(adapter);
    }

    private void updateProgressBars(List<String> filteredSectors) {
        List<ProgressItem> allItems = getProgressItemsFromDatabase();
        List<ProgressItem> filteredItems = new ArrayList<>();

        for (ProgressItem item : allItems) {
            if (filteredSectors.contains(item.getLabel())) {
                filteredItems.add(item);
            }
        }

        recyclerViewProgressBars.setLayoutManager(new LinearLayoutManager(getContext()));
        ProgressBarAdapter adapter = new ProgressBarAdapter(filteredItems);
        recyclerViewProgressBars.setAdapter(adapter);
    }

    private void updatePieChart(List<String> filteredSectors) {
        List<ProgressItem> allItems = getProgressItemsFromDatabase();
        ArrayList<PieEntry> entries = new ArrayList<>();
        float total = 0f;

        for (ProgressItem item : allItems) {
            if (filteredSectors.contains(item.getLabel())) {
                entries.add(new PieEntry(item.getProgress(), item.getLabel()));
                total += item.getProgress();
            }
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);

        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.parseColor("#EF4444"));
        colors.add(Color.parseColor("#EC2B7B"));
        colors.add(Color.parseColor("#B68DF6"));
        colors.add(Color.parseColor("#8059E7"));
        colors.add(Color.parseColor("#4C65F1"));
        colors.add(Color.parseColor("#26C8D8"));
        colors.add(Color.parseColor("#43D8B0"));
        colors.add(Color.parseColor("#82E762"));
        colors.add(Color.parseColor("#FFD95D"));
        dataSet.setColors(colors);

        PieData pieData = new PieData(dataSet);
        pieData.setValueFormatter(new PercentFormatter(pieChart));
        pieData.setValueTextSize(11f);
        pieData.setValueTextColor(Color.WHITE);

        pieChart.setData(pieData);
        pieChart.setCenterText("$" + String.format("%.0f", total));
        pieChart.invalidate();
    }

    private List<ProgressItem> getProgressItemsFromDatabase() {
        List<ProgressItem> progressItems = new ArrayList<>();

        String[] setores = {"Setor H", "Setor B", "Setor C", "Setor D", "Setor E"};
        int[] progressos = {25, 22, 20, 17, 1};

        int[] colors = {R.color.red_500, R.color.pink_500, R.color.purple_500, R.color.indigo_500, R.color.blue_500};

        for (int i = 0; i < setores.length; i++) {
            progressItems.add(new ProgressItem(setores[i], progressos[i], colors[i % colors.length]));
        }

        return progressItems;
    }
}
