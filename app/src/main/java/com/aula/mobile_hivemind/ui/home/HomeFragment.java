package com.aula.mobile_hivemind.ui.home;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
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
import com.aula.mobile_hivemind.api.RetrofitClient;
import com.aula.mobile_hivemind.api.SqlApiService;
import com.aula.mobile_hivemind.dto.MaquinaResponseDTO;
import com.aula.mobile_hivemind.dto.ParadaSQLRequestDTO;
import com.aula.mobile_hivemind.dto.RegistroParadaResponseDTO;
import com.aula.mobile_hivemind.recyclerViewParadas.Parada;
import com.aula.mobile_hivemind.recyclerViewParadas.ParadaAdapter;
import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import okhttp3.ResponseBody;
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
    private com.aula.mobile_hivemind.api.ApiService apiService;

    private FirebaseFirestore db;
    private SharedPreferences sharedPreferences;
    private String userEmail;
    private String userType;
    private String userSetor;
    private int userId;
    private ShapeableImageView avatar;
    private SqlApiService sqlApiService;
    private BottomSheetDialog bottomSheetDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sharedPreferences = requireContext().getSharedPreferences("ProfilePrefs", 0);

        avatar = view.findViewById(R.id.imageView3);

        carregarImagemPerfil();

        filtrarParadas = view.findViewById(R.id.filterButton);
        chipGroupSetores = view.findViewById(R.id.chipGroupSetores);
        recyclerViewParadas = view.findViewById(R.id.recyclerViewParadas);

        // Inicializar API Service
        apiService = RetrofitClient.getApiService();
        sqlApiService = RetrofitClient.getSqlApiService();

        // Inicializar Firestore e SharedPreferences
        db = FirebaseFirestore.getInstance();

        // Inicializar listas
        paradasList = new ArrayList<>();
        allParadasList = new ArrayList<>();

        // Configurar RecyclerView
        recyclerViewParadas.setLayoutManager(new LinearLayoutManager(getContext()));
        paradaAdapter = new ParadaAdapter(paradasList, sqlApiService);

        paradaAdapter.setOnItemClickListener(parada -> {
            abrirModalParada(parada);
        });

        recyclerViewParadas.setAdapter(paradaAdapter);

        obterInformacoesUsuarioECarregarParadas();

        filtrarParadas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
        if (getActivity() instanceof MainActivity) {
            userType = ((MainActivity) getActivity()).getUserType();
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

                            carregarParadas();
                        } else {
                            carregarParadas();
                        }
                    })
                    .addOnFailureListener(e -> {
                        carregarParadas();
                    });
        } else {
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

        carregarImagemPerfil();
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

    private void abrirModalParada(Parada parada) {
        View modalView = LayoutInflater.from(requireContext()).inflate(R.layout.modal_parada, null);

        // Buscar todas as TextViews
        TextView txtIdMaquina = modalView.findViewById(R.id.txtIdMaquina);
        TextView txtCodigoColaborador = modalView.findViewById(R.id.txtCodigoColaborador);
        TextView txtNomeMaquina = modalView.findViewById(R.id.txtNomeMaquina);
        TextView txtSetor = modalView.findViewById(R.id.txtSetor);
        TextView txtDataParada = modalView.findViewById(R.id.txtData);
        TextView txtHoraInicio = modalView.findViewById(R.id.txtHoraInicio);
        TextView txtHoraFim = modalView.findViewById(R.id.txtHoraFim);
        TextView txtDuracao = modalView.findViewById(R.id.txtDuracao);
        TextView txtDescricaoParada = modalView.findViewById(R.id.txtDescricao);

        // Define os valores básicos
        txtIdMaquina.setText(parada.getId_maquina() != null ? String.valueOf(parada.getId_maquina()) : "Não informado");
        txtCodigoColaborador.setText(parada.getId_usuario() != null ? String.valueOf(parada.getId_usuario()) : "Não informado");
        txtSetor.setText(parada.getDes_setor() != null ? parada.getDes_setor() : "Não informado");
        if (parada.getDt_parada() != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd, MMM yyyy", Locale.getDefault());
            String dataFormatada = dateFormat.format(parada.getDt_parada());
            txtDataParada.setText(dataFormatada);
        } else {
            txtDataParada.setText("Não informado");
        }
        txtDescricaoParada.setText(parada.getDes_parada() != null ? parada.getDes_parada() : "Não informado");

        // Processar horas e duração
        processarHorasEDuracao(parada, txtHoraInicio, txtHoraFim, txtDuracao);

        // Buscar nome da máquina pelo ID
        buscarNomeMaquinaPorId(parada.getId_maquina(), txtNomeMaquina);

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
        bottomSheetDialog.setContentView(modalView);
        bottomSheetDialog.show();

        ImageButton btnFechar = modalView.findViewById(R.id.btnFechar);
        if (btnFechar != null) {
            btnFechar.setOnClickListener(v -> bottomSheetDialog.dismiss());
        }

        Button btnFinalizarParada = modalView.findViewById(R.id.btnManutencao);
        if (btnFinalizarParada != null && "man".equals(userType)) { // Engenheiro
            btnFinalizarParada.setVisibility(View.VISIBLE);
            btnFinalizarParada.setOnClickListener(v -> {
                bottomSheetDialog.dismiss();
                finalizarParada(parada);
            });
        } else {
            btnFinalizarParada.setVisibility(View.GONE);
        }
    }

    private void processarHorasEDuracao(Parada parada, TextView txtHoraInicio, TextView txtHoraFim, TextView txtDuracao) {
        try {
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

            if (parada.getHora_Inicio() != null) {
                String horaInicio = timeFormat.format(parada.getHora_Inicio());
                txtHoraInicio.setText(horaInicio);
            } else {
                txtHoraInicio.setText("Não informado");
            }

            if (parada.getHora_Fim() != null) {
                String horaFim = timeFormat.format(parada.getHora_Fim());
                txtHoraFim.setText(horaFim);
            } else {
                txtHoraFim.setText("Não informado");
            }

            if (parada.getHora_Inicio() != null && parada.getHora_Fim() != null) {
                long diff = parada.getHora_Fim().getTime() - parada.getHora_Inicio().getTime();
                long diffMinutes = diff / (60 * 1000);
                long diffHours = diffMinutes / 60;
                long remainingMinutes = diffMinutes % 60;

                String duracao;
                if (diffHours > 0) {
                    duracao = String.format(Locale.getDefault(), "%dh %02dmin", diffHours, remainingMinutes);
                } else {
                    duracao = String.format(Locale.getDefault(), "%dmin", diffMinutes);
                }
                txtDuracao.setText(duracao);
            } else {
                txtDuracao.setText("Não calculável");
            }

        } catch (Exception e) {
            txtHoraInicio.setText("Erro");
            txtHoraFim.setText("Erro");
            txtDuracao.setText("Erro");
        }
    }

    private void buscarNomeMaquinaPorId(Integer idMaquina, TextView txtNomeMaquina) {
        if (idMaquina == null) {
            txtNomeMaquina.setText("ID não informado");
            return;
        }

        txtNomeMaquina.setText("Carregando...");

        if (sqlApiService == null) {
            txtNomeMaquina.setText("Erro de configuração");
            return;
        }

        Call<List<MaquinaResponseDTO>> call = sqlApiService.listarMaquinas();

        call.enqueue(new Callback<List<MaquinaResponseDTO>>() {
            @Override
            public void onResponse(Call<List<MaquinaResponseDTO>> call, Response<List<MaquinaResponseDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<MaquinaResponseDTO> maquinas = response.body();

                    String nomeMaquinaEncontrada = "Máquina não encontrada";
                    boolean encontrou = false;

                    // Log detalhado de todas as máquinas
                    for (int i = 0; i < maquinas.size(); i++) {
                        MaquinaResponseDTO maquina = maquinas.get(i);

                        if (maquina.getId() != null && maquina.getId().equals(idMaquina.longValue())) {
                            nomeMaquinaEncontrada = maquina.getNome() != null ? maquina.getNome() : "Nome não disponível";
                            break;
                        }
                    }

                    txtNomeMaquina.setText(nomeMaquinaEncontrada);

                } else {
                    switch (response.code()) {
                        case 401:
                            txtNomeMaquina.setText("Erro de autenticação");
                            break;
                        case 404:
                            txtNomeMaquina.setText("API não encontrada");
                            break;
                        case 500:
                            txtNomeMaquina.setText("Erro interno do servidor");
                            break;
                        default:
                            txtNomeMaquina.setText("Erro ao buscar máquina");
                    }
                }
            }

            @Override
            public void onFailure(Call<List<MaquinaResponseDTO>> call, Throwable t) {
                txtNomeMaquina.setText("Falha na conexão");
            }
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

                    ordenarParadasPorDataEHora(allParadasList);

                    aplicarFiltroUsuario();

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
//                    Toast.makeText(getContext(), "Erro ao carregar paradas: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<RegistroParadaResponseDTO>> call, Throwable t) {
                Toast.makeText(getContext(), "Falha na conexão: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void ordenarParadasPorDataEHora(List<Parada> paradas) {
        paradas.sort((p1, p2) -> {
            Date d1 = p1.getDt_parada();
            Date d2 = p2.getDt_parada();

            if (d1 == null && d2 == null) return 0;
            if (d1 == null) return 1;
            if (d2 == null) return -1;

            return d2.compareTo(d1);
        });
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
        } else {
            paradasList.addAll(allParadasList);
            filtrarParadas.setVisibility(View.VISIBLE);
        }

        paradaAdapter.notifyDataSetChanged();
    }

    private Parada converterParaParada(RegistroParadaResponseDTO registro) {
        return new Parada(
                registro.getId(),
                registro.getId_maquina(),
                registro.getId_usuario(),
                registro.getDes_parada(),
                registro.getDes_setor(),
                registro.getDt_parada(),
                registro.getHora_Fim(),
                registro.getHora_Inicio()
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

    public void finalizarParada(Parada parada) {
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

    private String formatarData(Date data) {
        if (data == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(data);
    }
}