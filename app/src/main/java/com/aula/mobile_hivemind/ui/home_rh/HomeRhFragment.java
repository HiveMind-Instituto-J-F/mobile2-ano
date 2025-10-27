package com.aula.mobile_hivemind.ui.home_rh;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import java.util.List;

public class HomeRhFragment extends Fragment {

    private ChipGroup chipGroupSetores;
    private Chip chipTodos;
    private RecyclerView recyclerViewProgressBars;
    private PieChart pieChart;

    public HomeRhFragment() {
        // Construtor público vazio requerido
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // infla o layout
        View root = inflater.inflate(R.layout.fragment_home_rh, container, false);

        // inicializa as views
        pieChart = root.findViewById(R.id.pieChart);
        recyclerViewProgressBars = root.findViewById(R.id.recyclerViewProgressBars);
        chipGroupSetores = root.findViewById(R.id.chipGroupSetores);

        // configura e popula os dados
        setupPieChart();
        setupProgressBars();
        addChipsToChipGroup(new ArrayList<String>() {{
            add("Setor A");
            add("Setor B");
            add("Setor C");
            add("Setor D");
            add("Setor E");
        }});

        return root;
    }

    private void addChipsToChipGroup(List<String> sectors) {
        // Limpa chips antigos
        chipGroupSetores.removeAllViews();

        // Chip "Todos"
        chipTodos = new Chip(getContext(), null, 0);
        chipTodos.setText("Todos");
        chipTodos.setId(View.generateViewId());
        chipTodos.setCheckable(true);
        chipTodos.setClickable(true);
        chipTodos.setChecked(true);

        // Chips para cada setor
        for (String sectorName : sectors) {
            Chip chip = new Chip(getContext(), null, 0);
            chip.setText(sectorName);
            chip.setId(View.generateViewId());
            chip.setCheckable(true);
            chip.setClickable(true);
            chipGroupSetores.addView(chip);
        }

        // Listener de seleção dos chips
        chipGroupSetores.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                // Nenhum chip → volta para "Todos"
                chipTodos.setChecked(true);
                updatePieChart(sectors);          // mostra todos no gráfico
                updateProgressBars(sectors);      // mostra todos no RecyclerView
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
                            chipTodos.setCheckable(false);
                            selectedSectors.add(name);
                        }
                    }
                }

                // Atualiza gráfico e recycler com setores filtrados
                updatePieChart(selectedSectors);
                updateProgressBars(selectedSectors);

                // Se algum setor foi selecionado → desmarca "Todos"
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

        // Cores personalizadas
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.parseColor("#EF4444")); // Vermelho
        colors.add(Color.parseColor("#EC2B7B")); // Rosa
        colors.add(Color.parseColor("#B68DF6")); // Roxo
        colors.add(Color.parseColor("#8059E7")); // Azul escuro
        colors.add(Color.parseColor("#4C65F1")); // Azul
        colors.add(Color.parseColor("#26C8D8")); // Ciano
        colors.add(Color.parseColor("#43D8B0")); // Verde claro
        colors.add(Color.parseColor("#82E762")); // Verde
        colors.add(Color.parseColor("#FFD95D")); // Amarelo
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

    // Configura as barras de progresso no RecyclerView
    private void setupProgressBars() {
        List<ProgressItem> progressItems = getProgressItemsFromDatabase();

        recyclerViewProgressBars.setLayoutManager(new LinearLayoutManager(getContext()));
        ProgressBarAdapter adapter = new ProgressBarAdapter(progressItems);
        recyclerViewProgressBars.setAdapter(adapter);
    }

    private void updateProgressBars(List<String> filteredSectors) {
        List<ProgressItem> allItems = getProgressItemsFromDatabase();
        List<ProgressItem> filteredItems = new ArrayList<>();

        // Filtra apenas os setores selecionados
        for (ProgressItem item : allItems) {
            if (filteredSectors.contains(item.getLabel())) {
                filteredItems.add(item);
            }
        }

        // Atualiza RecyclerView
        recyclerViewProgressBars.setLayoutManager(new LinearLayoutManager(getContext()));
        ProgressBarAdapter adapter = new ProgressBarAdapter(filteredItems);
        recyclerViewProgressBars.setAdapter(adapter);
    }



    private void updatePieChart(List<String> filteredSectors) {
        List<ProgressItem> allItems = getProgressItemsFromDatabase();
        ArrayList<PieEntry> entries = new ArrayList<>();
        float total = 0f;

        // Filtra apenas os setores selecionados
        for (ProgressItem item : allItems) {
            if (filteredSectors.contains(item.getLabel())) {
                entries.add(new PieEntry(item.getProgress(), item.getLabel()));
                total += item.getProgress();
            }
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);

        // Cores
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

        // Lista manual de setores e progresso
        String[] setores = {"Setor H", "Setor B", "Setor C", "Setor D", "Setor E"};
        int[] progressos = {25, 22, 20, 17, 1};

        // Lista de cores (vai rotacionar se tiver mais setores que cores)
        int[] colors = {R.color.red_500, R.color.pink_500, R.color.purple_500, R.color.indigo_500, R.color.blue_500};

        for (int i = 0; i < setores.length; i++) {
            String nome = setores[i];
            int progresso = progressos[i];
            int color = colors[i % colors.length]; // Rotaciona cores

            progressItems.add(new ProgressItem(nome, progresso, color));
        }

        return progressItems;
    }

}
