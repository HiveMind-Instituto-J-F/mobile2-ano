package com.aula.mobile_hivemind.ui.home;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aula.mobile_hivemind.MainActivity;
import com.aula.mobile_hivemind.R;
import com.aula.mobile_hivemind.api.ApiMongoRegistroParadasService;
import com.aula.mobile_hivemind.api.RetrofitClient;
import com.aula.mobile_hivemind.dto.RegistroParadaResponseDTO;
import com.aula.mobile_hivemind.recyclerViewParadas.Parada;
import com.aula.mobile_hivemind.recyclerViewParadas.ParadaAdapter;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {
    ImageButton filtrarParadas;
    private ChipGroup chipGroupSetores;
    private Chip chipTodos;

    private RecyclerView recyclerViewParadas;
    private ParadaAdapter paradaAdapter;

    private List<Parada> paradasList;
    private List<Parada> allParadasList;
    private ApiMongoRegistroParadasService apiService;

    private FirebaseFirestore db;
    private SharedPreferences sharedPreferences;
    private String userEmail;
    private String userType;
    private String userSetor;
    private int userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        filtrarParadas = view.findViewById(R.id.filterButton);
        chipGroupSetores = view.findViewById(R.id.chipGroupSetores);
        recyclerViewParadas = view.findViewById(R.id.recyclerViewParadas);

        // Inicializar API Service
        apiService = RetrofitClient.getApiMongoRegistroService();

        // Inicializar Firestore e SharedPreferences
        db = FirebaseFirestore.getInstance();
        sharedPreferences = requireContext().getSharedPreferences("ProfilePrefs", 0);

        // Inicializar listas
        paradasList = new ArrayList<>();
        allParadasList = new ArrayList<>();

        // Configurar RecyclerView
        recyclerViewParadas.setLayoutManager(new LinearLayoutManager(getContext()));
        paradaAdapter = new ParadaAdapter(paradasList);

        paradaAdapter.setOnItemClickListener(parada -> {
            abrirModalParada(parada);
        });

        recyclerViewParadas.setAdapter(paradaAdapter);

        // 🔧 OBTER INFORMAÇÕES DO USUÁRIO PRIMEIRO
        obterInformacoesUsuarioECarregarParadas();

        filtrarParadas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 🔧 SE FOR OPERADOR, NÃO MOSTRA FILTRO (APENAS SEU SETOR)
                if ("regular".equals(userType)) {
                    Toast.makeText(getContext(), "Operador: Visualizando apenas paradas do setor " + userSetor, Toast.LENGTH_SHORT).show();
                    return;
                }

                if (chipGroupSetores.getVisibility() == View.GONE) {
                    chipGroupSetores.setVisibility(View.VISIBLE);
                } else {
                    chipGroupSetores.setVisibility(View.GONE);
                }
            }
        });

        chipGroupSetores.setVisibility(View.GONE);
    }

    private void obterInformacoesUsuarioECarregarParadas() {
        // 🔧 PRIMEIRO TENTA OBTER O USER_TYPE DA MAIN ACTIVITY
        if (getActivity() instanceof MainActivity) {
            userType = ((MainActivity) getActivity()).getUserType();
            Log.d("HomeFragment", "UserType da MainActivity: " + userType);
        }

        userEmail = sharedPreferences.getString("user_email", null);
        userId = sharedPreferences.getInt("user_id", 0);

        if (userEmail != null && !userEmail.isEmpty()) {
            db.collection("trabalhadores")
                    .whereEqualTo("login", userEmail)
                    .limit(1)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            String tipoPerfilOriginal = task.getResult().getDocuments().get(0).getString("tipo_perfil");
                            userSetor = task.getResult().getDocuments().get(0).getString("setor");

                            // 🔧 SE USER_TYPE NÃO VEIO DA MAIN ACTIVITY, MAPEAR AQUI
                            if (userType == null && tipoPerfilOriginal != null) {
                                switch (tipoPerfilOriginal.toLowerCase()) {
                                    case "operador":
                                        userType = "regular";
                                        break;
                                    case "engenheiro":
                                        userType = "man";
                                        break;
                                    case "supervisor":
                                        userType = "RH";
                                        break;
                                    default:
                                        userType = "regular";
                                }
                            }

                            Log.d("HomeFragment", "Usuário: " + userEmail + ", Tipo: " + userType + ", Setor: " + userSetor);

                            // 🔧 AGORA CARREGAR PARADAS COM FILTRO APROPRIADO
                            carregarParadas();

                        } else {
                            Log.e("HomeFragment", "Usuário não encontrado no Firestore");
                            // 🔧 SE NÃO ENCONTROU NO FIRESTORE, USA O USER_TYPE DA MAIN ACTIVITY
                            carregarParadas();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("HomeFragment", "Erro ao buscar usuário: " + e.getMessage());
                        // 🔧 EM CASO DE ERRO, USA O USER_TYPE DA MAIN ACTIVITY
                        carregarParadas();
                    });
        } else {
            Log.e("HomeFragment", "Email do usuário não disponível");
            carregarParadas();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setFabVisibility(true);
            ((MainActivity) getActivity()).setBottomNavigationVisibility(true);
        }
    }

    private void abrirModalParada(Parada parada) {
        View modalView = LayoutInflater.from(requireContext()).inflate(R.layout.modal_parada, null);

        TextView txtId = modalView.findViewById(R.id.txtId);
        TextView txtIdMaquina = modalView.findViewById(R.id.txtIdMaquina);
        TextView txtIdUsuario = modalView.findViewById(R.id.txtIdUsuario);
        TextView txtDescricaoParada = modalView.findViewById(R.id.txtDescricaoParada);
        TextView txtSetor = modalView.findViewById(R.id.txtSetor);
        TextView txtDataParada = modalView.findViewById(R.id.txtDataParada);
        TextView txtHoraInicio = modalView.findViewById(R.id.txtHoraInicio);
        TextView txtHoraFim = modalView.findViewById(R.id.txtHoraFim);
        ImageButton btnFechar = modalView.findViewById(R.id.btnFechar);
        Button btnManutencao = modalView.findViewById(R.id.btnManutencao);

        btnManutencao.setVisibility(View.GONE);

        if ("man".equals(userType)) {
            btnManutencao.setVisibility(View.VISIBLE);
        }

        // Preenchendo com os dados do ResponseDTO - USANDO OS NOVOS GETTERS
        txtId.setText(parada.getId() != null ? parada.getId() : "N/A");
        txtIdMaquina.setText(String.valueOf(parada.getId_maquina()));
        txtIdUsuario.setText(String.valueOf(parada.getId_usuario()));
        txtDescricaoParada.setText(parada.getDes_parada());
        txtSetor.setText(parada.getDes_setor());

        // Formatando as datas
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

        txtDataParada.setText(parada.getDt_parada() != null ? dateFormat.format(parada.getDt_parada()) : "N/A");
        txtHoraInicio.setText(parada.getHora_Inicio() != null ? timeFormat.format(parada.getHora_Inicio()) : "N/A");
        txtHoraFim.setText(parada.getHora_Fim() != null ? timeFormat.format(parada.getHora_Fim()) : "N/A");

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
        bottomSheetDialog.setContentView(modalView);
        bottomSheetDialog.show();

        btnFechar.setOnClickListener(v -> bottomSheetDialog.dismiss());

        btnManutencao.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            abrirManutencao(parada);
        });
    }

    private void carregarParadas() {
        Call<List<RegistroParadaResponseDTO>> call = apiService.getAllRegistros();
        call.enqueue(new Callback<List<RegistroParadaResponseDTO>>() {
            @Override
            public void onResponse(Call<List<RegistroParadaResponseDTO>> call, Response<List<RegistroParadaResponseDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    paradasList.clear();
                    allParadasList.clear();

                    // Converter RegistroParadaResponseDTO para Parada
                    for (RegistroParadaResponseDTO registro : response.body()) {
                        Parada parada = converterParaParada(registro);
                        allParadasList.add(parada);
                    }

                    // 🔧 APLICAR FILTRO BASEADO NO TIPO DE USUÁRIO
                    aplicarFiltroUsuario();

                    // 🔧 EXTRAIR SETORES APENAS DAS PARADAS FILTRADAS
                    if (!"regular".equals(userType)) {
                        Set<String> setores = new HashSet<>();
                        for (Parada parada : paradasList) {
                            if (parada.getDes_setor() != null && !parada.getDes_setor().isEmpty()) {
                                setores.add(parada.getDes_setor());
                            }
                        }
                        addChipsToChipGroup(new ArrayList<>(setores));
                    }

                } else {
                    Toast.makeText(getContext(), "Erro ao carregar paradas: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<RegistroParadaResponseDTO>> call, Throwable t) {
                Toast.makeText(getContext(), "Falha na conexão: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    public void abrirManutencao(Parada parada) {
        if (!"man".equals(userType)) {
            Toast.makeText(getContext(), "Acesso restrito a engenheiros.", Toast.LENGTH_SHORT).show();
            return;
        }

        Bundle bundle = new Bundle();
        bundle.putInt("idMaquina", parada.getId_maquina());
        bundle.putInt("codigoColaborador", parada.getId_usuario());
        bundle.putString("setor", parada.getDes_setor());
        bundle.putString("descricaoParada", parada.getDes_parada());
        bundle.putInt("userId", userId);

        // ✅ ENVIAR DATA E HORÁRIOS DA PARADA ORIGINAL
        bundle.putString("dataParada", formatarData(parada.getDt_parada()));
        bundle.putSerializable("horaInicio", parada.getHora_Inicio());
        bundle.putSerializable("horaFim", parada.getHora_Fim());

        // Passar o ID do MongoDB também
        bundle.putString("idMongo", parada.getId());

        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
        navController.navigate(R.id.maintenanceFragment, bundle);
    }

    // Método auxiliar para formatar data
    private String formatarData(Date data) {
        if (data == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(data);
    }

    private void aplicarFiltroUsuario() {
        paradasList.clear();

        if ("regular".equals(userType)) {
            for (Parada parada : allParadasList) {
                if (parada.getDes_setor() != null && parada.getDes_setor().equals(userSetor)) {
                    paradasList.add(parada);
                }
            }

            // Esconder botão de filtro para Operador
            filtrarParadas.setVisibility(View.GONE);
            chipGroupSetores.setVisibility(View.GONE);

            Log.d("HomeFragment", "Operador - Setor: " + userSetor + ", Paradas: " + paradasList.size());

        } else {
            paradasList.addAll(allParadasList);
            filtrarParadas.setVisibility(View.VISIBLE);
            Log.d("HomeFragment", userType + " - Todas as paradas: " + allParadasList.size());
        }

        paradaAdapter.notifyDataSetChanged();
    }

    private Parada converterParaParada(RegistroParadaResponseDTO registro) {
        return new Parada(
                registro.getId(),                    // id
                registro.getId_maquina(),           // id_maquina
                registro.getId_usuario(),           // id_usuario
                registro.getDes_parada(),           // des_parada
                registro.getDes_setor(),            // des_setor
                registro.getDt_parada(),            // dt_parada
                registro.getHora_Inicio(),          // hora_Inicio
                registro.getHora_Fim()              // hora_Fim
        );
    }

    private void addChipsToChipGroup(List<String> sectors) {
        // Limpa chips antigos
        chipGroupSetores.removeAllViews();

        // Chip "Todos"
        chipTodos = new Chip(requireContext());
        chipTodos.setText("Todos");
        chipTodos.setId(View.generateViewId());
        chipTodos.setCheckable(true);
        chipTodos.setClickable(true);
        chipTodos.setChecked(true);
        chipGroupSetores.addView(chipTodos);

        // Chips para cada setor
        for (String sectorName : sectors) {
            Chip chip = new Chip(requireContext());
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
                paradaAdapter.setParadas(new ArrayList<>(allParadasList));
            } else {
                List<Parada> filteredList = new ArrayList<>();
                boolean todosSelecionado = false;

                // Verifica cada chip selecionado
                for (int id : checkedIds) {
                    Chip chip = group.findViewById(id);
                    if (chip != null) {
                        String name = chip.getText().toString();
                        if (name.equals("Todos")) {
                            todosSelecionado = true;
                            break;
                        } else {
                            filteredList.addAll(filterParadasBySector(name));
                        }
                    }
                }

                if (todosSelecionado) {
                    // Se "Todos" estiver selecionado, mostra todas as paradas
                    filteredList = new ArrayList<>(allParadasList);

                    // Desmarca outros chips
                    for (int i = 0; i < group.getChildCount(); i++) {
                        Chip currentChip = (Chip) group.getChildAt(i);
                        if (!currentChip.getText().toString().equals("Todos")) {
                            currentChip.setChecked(false);
                        }
                    }
                }

                // Atualiza RecyclerView
                paradaAdapter.setParadas(filteredList);
            }
        });
    }

    private List<Parada> filterParadasBySector(String sectorName) {
        List<Parada> filteredList = new ArrayList<>();
        for (Parada parada : allParadasList) {
            if (sectorName.equals(parada.getDes_setor())) {
                filteredList.add(parada);
            }
        }
        return filteredList;
    }
}