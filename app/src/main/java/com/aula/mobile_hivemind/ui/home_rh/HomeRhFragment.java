package com.aula.mobile_hivemind.ui.home_rh;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aula.mobile_hivemind.MainActivity;
import com.aula.mobile_hivemind.R;
import com.aula.mobile_hivemind.api.RetrofitClient;
import com.aula.mobile_hivemind.dto.RegistroParadaResponseDTO;
import com.aula.mobile_hivemind.ui.dashboard.itens.ProgressBarAdapter;
import com.aula.mobile_hivemind.ui.dashboard.itens.ProgressItem;
import com.bumptech.glide.Glide;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.imageview.ShapeableImageView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeRhFragment extends Fragment {

    private ChipGroup chipGroupSetores;
    private Chip chipTodos;
    private RecyclerView recyclerViewProgressBars;
    private PieChart pieChart;
    private ShapeableImageView avatar;
    private SharedPreferences sharedPreferences;
    private TextView centerText;
    private com.aula.mobile_hivemind.api.ApiService apiService;

    // Lista para armazenar todos os setores disponíveis
    private List<String> todosSetores = new ArrayList<>();
    // Mapa para armazenar contagem de paradas por setor
    private Map<String, Integer> paradasPorSetor = new HashMap<>();

    public HomeRhFragment() {
        // Construtor público vazio requerido
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home_rh, container, false);

        // Inicializar API Service
        apiService = RetrofitClient.getApiService();

        // inicializa as views
        pieChart = root.findViewById(R.id.pieChart);
        recyclerViewProgressBars = root.findViewById(R.id.recyclerViewProgressBars);
        chipGroupSetores = root.findViewById(R.id.chipGroupSetores);
        centerText = root.findViewById(R.id.centerText);

        // Carrega dados reais da API
        carregarDadosParadas();

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sharedPreferences = requireContext().getSharedPreferences("ProfilePrefs", 0);
        avatar = view.findViewById(R.id.imageView3);
        carregarImagemPerfil();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setFabVisibility(true);
            ((MainActivity) getActivity()).setBottomNavigationVisibility(true);
        }
        carregarImagemPerfil();

        // Recarregar dados quando o fragment for retomado
        carregarDadosParadas();
    }

    private void carregarDadosParadas() {
        Call<List<RegistroParadaResponseDTO>> call = apiService.getAllRegistros();
        call.enqueue(new Callback<List<RegistroParadaResponseDTO>>() {
            @Override
            public void onResponse(Call<List<RegistroParadaResponseDTO>> call, Response<List<RegistroParadaResponseDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    processarParadasDoDia(response.body());
                } else {
                    Log.e("HomeRhFragment", "Erro ao carregar paradas: " + response.code());
                    // Mostrar dados vazios em caso de erro
                    mostrarDadosVazios();
                }
            }

            @Override
            public void onFailure(Call<List<RegistroParadaResponseDTO>> call, Throwable t) {
                Log.e("HomeRhFragment", "Falha na conexão: " + t.getMessage());
                mostrarDadosVazios();
            }
        });
    }

    private void processarParadasDoDia(List<RegistroParadaResponseDTO> todasParadas) {
        paradasPorSetor.clear();
        todosSetores.clear();

        Calendar hoje = Calendar.getInstance();
        hoje.set(Calendar.HOUR_OF_DAY, 0);
        hoje.set(Calendar.MINUTE, 0);
        hoje.set(Calendar.SECOND, 0);
        hoje.set(Calendar.MILLISECOND, 0);

        Calendar amanha = (Calendar) hoje.clone();
        amanha.add(Calendar.DAY_OF_MONTH, 1);

        // Contar paradas por setor no dia atual
        for (RegistroParadaResponseDTO registro : todasParadas) {
            try {
                if (registro.getDt_parada() != null &&
                        registro.getDes_setor() != null && !registro.getDes_setor().isEmpty()) {

                    Date dataParada = registro.getDt_parada();

                    // Verificar se é do dia atual
                    if (dataParada.after(hoje.getTime()) && dataParada.before(amanha.getTime())) {
                        String setor = registro.getDes_setor();
                        paradasPorSetor.put(setor, paradasPorSetor.getOrDefault(setor, 0) + 1);

                        // Adiciona à lista de setores se não existir
                        if (!todosSetores.contains(setor)) {
                            todosSetores.add(setor);
                        }
                    }
                }
            } catch (Exception e) {
                Log.e("DashboardFragment", "Erro ao processar parada: " + e.getMessage());
            }
        }

        // Atualizar UI com os dados reais
        atualizarUIComDadosReais();
    }

    private void atualizarUIComDadosReais() {
        if (paradasPorSetor.isEmpty()) {
            mostrarDadosVazios();
        } else {
            // Configurar chips com setores reais
            addChipsToChipGroup(new ArrayList<>(todosSetores));

            // Configurar gráfico e barras de progresso
            setupPieChart();
            setupProgressBars();
        }
    }

    private void mostrarDadosVazios() {
        // Mostrar mensagem de nenhuma parada hoje
        centerText.setText("Nenhuma parada hoje");

        // Limpar chips
        chipGroupSetores.removeAllViews();

        // Mostrar gráfico vazio
        pieChart.clear();
        pieChart.setCenterText("0");
        pieChart.invalidate();

        // Limpar recycler view
        recyclerViewProgressBars.setAdapter(new ProgressBarAdapter(new ArrayList<>()));
    }

    private void addChipsToChipGroup(List<String> sectors) {
        chipGroupSetores.removeAllViews();

        if (sectors.isEmpty()) {
            return;
        }

        // Chip "Todos"
        chipTodos = new Chip(getContext());
        chipTodos.setText("Todos");
        chipTodos.setId(View.generateViewId());
        chipTodos.setCheckable(true);
        chipTodos.setClickable(true);
        chipTodos.setChecked(true);
        chipGroupSetores.addView(chipTodos);

        // Chips para cada setor real
        for (String sectorName : sectors) {
            Chip chip = new Chip(getContext());
            chip.setText(sectorName);
            chip.setId(View.generateViewId());
            chip.setCheckable(true);
            chip.setClickable(true);
            chipGroupSetores.addView(chip);
        }

        // Listener de seleção dos chips
        chipGroupSetores.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                chipTodos.setChecked(true);
                updatePieChart(new ArrayList<>(sectors));
                updateProgressBars(new ArrayList<>(sectors));
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
        List<ProgressItem> progressItems = getProgressItemsFromRealData();
        ArrayList<PieEntry> entries = new ArrayList<>();

        int totalParadas = 0;
        for (ProgressItem item : progressItems) {
            entries.add(new PieEntry(item.getProgress(), item.getLabel()));
            totalParadas += item.getProgress();
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

        // Usar formatter para mostrar valores absolutos em vez de porcentagens
        pieData.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) value);
            }
        });

        pieData.setValueTextSize(11f);
        pieData.setValueTextColor(Color.WHITE);

        pieChart.setData(pieData);
        pieChart.setUsePercentValues(false); // Mostrar valores absolutos
        pieChart.getDescription().setEnabled(false);
        pieChart.setHoleColor(Color.TRANSPARENT);
        pieChart.setHoleRadius(58f);
        pieChart.setTransparentCircleRadius(61f);
        pieChart.setDrawCenterText(true);
        pieChart.setCenterText(String.valueOf(totalParadas));
        pieChart.setCenterTextColor(Color.WHITE);
        pieChart.setCenterTextSize(24f);

        Legend legend = pieChart.getLegend();
        legend.setEnabled(false);

        pieChart.invalidate();
    }

    private void setupProgressBars() {
        List<ProgressItem> progressItems = getProgressItemsFromRealData();
        recyclerViewProgressBars.setLayoutManager(new LinearLayoutManager(getContext()));
        ProgressBarAdapter adapter = new ProgressBarAdapter(progressItems);
        recyclerViewProgressBars.setAdapter(adapter);
    }

    private void updateProgressBars(List<String> filteredSectors) {
        List<ProgressItem> allItems = getProgressItemsFromRealData();
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
        List<ProgressItem> allItems = getProgressItemsFromRealData();
        ArrayList<PieEntry> entries = new ArrayList<>();
        int totalParadas = 0;

        for (ProgressItem item : allItems) {
            if (filteredSectors.contains(item.getLabel())) {
                entries.add(new PieEntry(item.getProgress(), item.getLabel()));
                totalParadas += item.getProgress();
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
        pieData.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) value);
            }
        });
        pieData.setValueTextSize(11f);
        pieData.setValueTextColor(Color.WHITE);

        pieChart.setData(pieData);
        pieChart.setCenterText(String.valueOf(totalParadas));
        pieChart.invalidate();
    }

    private List<ProgressItem> getProgressItemsFromRealData() {
        List<ProgressItem> progressItems = new ArrayList<>();

        if (paradasPorSetor.isEmpty()) {
            return progressItems;
        }

        // Lista de cores
        int[] colors = {R.color.red_500, R.color.pink_500, R.color.purple_500,
                R.color.indigo_500, R.color.blue_500, R.color.cyan_500,
                R.color.teal_500, R.color.green_500, R.color.yellow_500};

        int colorIndex = 0;
        for (Map.Entry<String, Integer> entry : paradasPorSetor.entrySet()) {
            String setor = entry.getKey();
            int quantidade = entry.getValue();
            int color = colors[colorIndex % colors.length];

            progressItems.add(new ProgressItem(setor, quantidade, color));
            colorIndex++;
        }

        return progressItems;
    }

    private void carregarImagemPerfil() {
        if (avatar == null) return;

        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("ProfilePrefs", Context.MODE_PRIVATE);
        String userEmail = sharedPreferences.getString("user_email", null);
        if (userEmail == null) {
            avatar.setImageResource(R.drawable.img);
            return;
        }

        String cloudinaryUrlKey = "cloudinary_url_" + userEmail.hashCode();
        String profileImageKey = "profile_image_" + userEmail.hashCode();

        String cloudinaryUrl = sharedPreferences.getString(cloudinaryUrlKey, null);
        String encodedImage = sharedPreferences.getString(profileImageKey, null);

        if (cloudinaryUrl != null && !cloudinaryUrl.isEmpty()) {
            Glide.with(requireContext())
                    .load(cloudinaryUrl)
                    .circleCrop()
                    .placeholder(R.drawable.img)
                    .error(R.drawable.img)
                    .into(avatar);
        } else if (encodedImage != null && !encodedImage.isEmpty()) {
            byte[] byteArray = Base64.decode(encodedImage, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
            Glide.with(requireContext())
                    .load(bitmap)
                    .circleCrop()
                    .placeholder(R.drawable.img)
                    .into(avatar);
        } else {
            avatar.setImageResource(R.drawable.img);
        }
    }
}
