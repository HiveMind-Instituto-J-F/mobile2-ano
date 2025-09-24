package com.aula.mobile_hivemind.ui.home;

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
import com.aula.mobile_hivemind.recyclerViewParadas.adapter.ParadaAdapter;
import com.aula.mobile_hivemind.recyclerViewParadas.ParadaModel;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class HomeFragment extends Fragment {
    ImageButton filtrarParadas;
    private ChipGroup chipGroupSetores;
    private Chip chipTodos;

    private RecyclerView recyclerViewParadas;
    private ParadaAdapter paradaAdapter;

    private List<ParadaModel> allParadasList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        filtrarParadas = view.findViewById(R.id.filterButton);
        chipGroupSetores = view.findViewById(R.id.chipGroupSetores);
        recyclerViewParadas = view.findViewById(R.id.recyclerViewParadas);

        allParadasList = getAllParadasFromDatabase();

        Set<String> uniqueSectors = new HashSet<>();
        for (ParadaModel parada : allParadasList) {
            if (parada.getSetor() != null && !parada.getSetor().trim().isEmpty()) {
                uniqueSectors.add(parada.getSetor());
            }
        }

        List<String> sectorsList = new ArrayList<>(uniqueSectors);

        Collections.sort(sectorsList);

        filtrarParadas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (chipGroupSetores.getVisibility() == View.GONE) {
                    chipGroupSetores.setVisibility(View.VISIBLE);
                } else {
                    chipGroupSetores.setVisibility(View.GONE);
                }
            }
        });

        chipGroupSetores.setVisibility(View.GONE);
        addChipsToChipGroup(sectorsList);

        recyclerViewParadas.setLayoutManager(new LinearLayoutManager(getContext()));

        paradaAdapter = new ParadaAdapter(new ArrayList<>(allParadasList));
        recyclerViewParadas.setAdapter(paradaAdapter);
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

        // Listener para atualização de seleção
        chipGroupSetores.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                chipTodos.setChecked(true);
                paradaAdapter.updateData(new ArrayList<>(allParadasList));
            } else {
                List<ParadaModel> filteredList = new ArrayList<>();

                // Verifica cada chip selecionado
                for (int id : checkedIds) {
                    Chip chip = group.findViewById(id);
                    if (chip != null) {
                        String name = chip.getText().toString();
                        if (name.equals("Todos")) {
                            // Se "Todos" estiver selecionado, ignora outros chips
                            filteredList = new ArrayList<>(allParadasList);
                            break;
                        } else {
                            chipTodos.setCheckable(false);
                            filteredList.addAll(filterParadasBySector(name));
                        }
                    }
                }

                // Atualiza RecyclerView
                paradaAdapter.updateData(filteredList);

                // Se qualquer chip estiver marcado, desmarca "Todos"
                if (!checkedIds.contains(chipTodos.getId())) {
                    chipTodos.setChecked(false);
                }
            }
        });
    }


    private List<ParadaModel> getAllParadasFromDatabase() {
        List<ParadaModel> paradas = new ArrayList<>();
        paradas.add(new ParadaModel("Parada Estação Central", "Setor A", "08:00"));
        paradas.add(new ParadaModel("Parada Praça da Matriz", "Setor B", "08:15"));
        paradas.add(new ParadaModel("Parada Rua do Comércio", "Setor A", "08:30"));
        paradas.add(new ParadaModel("Parada Avenida Principal", "Setor C", "08:45"));
        paradas.add(new ParadaModel("Parada Terminal Rodoviário", "Setor D", "09:00"));
        paradas.add(new ParadaModel("Parada Mercado Público", "Setor B", "09:15"));
        paradas.add(new ParadaModel("Parada Hospital Municipal", "Setor A", "09:30"));
        paradas.add(new ParadaModel("Parada Centro de Convenções", "Setor E", "09:45"));
        paradas.add(new ParadaModel("Parada Museu Histórico", "Setor C", "10:00"));
        paradas.add(new ParadaModel("Parada Parque Urbano", "Setor D", "10:15"));
        return paradas;
    }

    private List<ParadaModel> filterParadasBySector(String sectorName) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            return allParadasList.stream()
                    .filter(parada -> parada.getSetor() != null && parada.getSetor().equals(sectorName))
                    .collect(Collectors.toList());
        } else {
            List<ParadaModel> filteredList = new ArrayList<>();
            for (ParadaModel parada : allParadasList) {
                if (parada.getSetor() != null && parada.getSetor().equals(sectorName)) {
                    filteredList.add(parada);
                }
            }
            return filteredList;
        }
    }
}