package com.aula.mobile_hivemind.ui.dashboard;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aula.mobile_hivemind.R;
import com.aula.mobile_hivemind.databinding.FragmentDashboardBinding;

import com.aula.mobile_hivemind.ui.dashboard.itens.ProgressBarAdapter;
import com.aula.mobile_hivemind.ui.dashboard.itens.ProgressItem;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DashboardFragment extends Fragment {

    TextView textResumoTitulo, textResumoNumero, textData, txtPorcent;
    private FragmentDashboardBinding binding;
    private PieChart pieChart;
    private RecyclerView recyclerViewProgressBars;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);

        textResumoNumero = binding.textResumoNumero;
        textResumoTitulo = binding.textResumoTitulo;
        textData = binding.textData;
        txtPorcent = binding.txtPorcent;

        // Referência aos novos gráficos e RecyclerView
        pieChart = binding.pieChart;
        recyclerViewProgressBars = binding.recyclerViewProgressBars;

        // Atualiza a data para o dia de hoje
        textData.setText("Mês " + LocalDate.now().getMonthValue() + "/" + LocalDate.now().getYear());

        // Atualiza os dados do dashboard
        atualizarResumo();

        // Configura e popula o novo gráfico
        setupPieChart();
        setupProgressBars();

        return binding.getRoot();
    }

    // Método para buscar os dados do banco e atualizar a interface
    private void atualizarResumo() {
        // Supondo que você tenha métodos que retornam a quantidade de paradas
        int paradasDiaAnterior = buscarParadasDoDiaAnterior(); // implementar
        int paradasDiaAtual = buscarParadasDoDiaAtual();       // implementar

        // Atualiza o número total de paradas
        textResumoNumero.setText(String.valueOf(paradasDiaAtual));

        // Calcula a variação em porcentagem
        double porcentagem;
        if (paradasDiaAnterior != 0) {
            porcentagem = ((double)(paradasDiaAtual - paradasDiaAnterior) / paradasDiaAnterior) * 100;
        } else {
            porcentagem = paradasDiaAtual * 100; // evita divisão por zero
        }

        // Formata e atualiza o TextView
        if (porcentagem >= 0) {
            txtPorcent.setText("cerca de " + String.format("%.1f%%", porcentagem) + " a mais em relação ao dia anterior");
        } else {
            txtPorcent.setText("cerca de " +String.format("%.1f%%", porcentagem) + " a menos em relação ao dia anterior");
        }
    }

    // Exemplos de métodos fictícios de busca do banco
    private int buscarParadasDoDiaAnterior() {
        // Aqui você faria a query no banco para pegar o total de paradas do dia anterior
        return 10; // apenas exemplo
    }

    private int buscarParadasDoDiaAtual() {
        // Aqui você faria a query no banco para pegar o total de paradas do dia atual
        return 12; // apenas exemplo
    }

    // Configura o gráfico de rosca
    private void setupPieChart() {
        // Dados fictícios baseados na imagem
        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(20f, "Setor A"));
        entries.add(new PieEntry(18f, "Setor B"));
        entries.add(new PieEntry(16f, "Setor C"));
        entries.add(new PieEntry(14f, "Setor D"));
        entries.add(new PieEntry(12f, "Setor E"));

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

        // Configurações do gráfico
        pieChart.setData(pieData);
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setHoleColor(Color.TRANSPARENT);
        pieChart.setHoleRadius(58f); // Raio interno para o buraco (donut)
        pieChart.setTransparentCircleRadius(61f);
        pieChart.setDrawCenterText(true);
        pieChart.setCenterText("$2 570");
        pieChart.setCenterTextColor(Color.WHITE);
        pieChart.setCenterTextSize(24f);

        // Remover a legenda padrão
        Legend legend = pieChart.getLegend();
        legend.setEnabled(false);

        pieChart.invalidate(); // Atualiza o gráfico
    }

    // Configura as barras de progresso no RecyclerView
    private void setupProgressBars() {
        List<ProgressItem> progressItems = new ArrayList<>();
        // Dados fictícios baseados na imagem
        progressItems.add(new ProgressItem("Setor A", 25, R.color.red_500));
        progressItems.add(new ProgressItem("Setor B", 22, R.color.pink_500));
        progressItems.add(new ProgressItem("Setor C", 20, R.color.purple_500));
        progressItems.add(new ProgressItem("Setor D", 17, R.color.indigo_500));
        progressItems.add(new ProgressItem("Setor E", 15, R.color.blue_500));

        recyclerViewProgressBars.setLayoutManager(new LinearLayoutManager(getContext()));

        // Define o adapter com os dados fictícios
        ProgressBarAdapter adapter = new ProgressBarAdapter(progressItems);
        recyclerViewProgressBars.setAdapter(adapter);
    }
}
