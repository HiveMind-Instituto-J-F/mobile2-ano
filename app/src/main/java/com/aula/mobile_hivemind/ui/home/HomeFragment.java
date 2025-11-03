package com.aula.mobile_hivemind.ui.home;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
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
import com.aula.mobile_hivemind.dto.RegistroParadaResponseDTO;
import com.aula.mobile_hivemind.recyclerViewParadas.Parada;
import com.aula.mobile_hivemind.recyclerViewParadas.ParadaAdapter;
import com.aula.mobile_hivemind.utils.CustomToast;
import com.aula.mobile_hivemind.utils.SharedPreferencesManager;
import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {
    interface UsuarioNomeCallback {
        void onNomeEncontrado(String nomeUsuario);
        void onErro(String mensagemErro);
    }

    private TextView textMensagem;
    private MaterialButton filtrarParadas;
    private ChipGroup chipGroupSetores;
    private Chip chipTodos;
    private RecyclerView recyclerViewParadas;
    private ParadaAdapter paradaAdapter;
    private List<Parada> paradasList;
    private List<Parada> allParadasList;
    private com.aula.mobile_hivemind.api.ApiService apiService;
    private FirebaseFirestore db;
    private SharedPreferencesManager prefsManager;
    private String userEmail;
    private String userType;
    private int userIntType;
    private String userSetor;
    private int userId;
    private ShapeableImageView avatar;
    private SqlApiService sqlApiService;
    private BottomSheetDialog bottomSheetDialog;
    private List<MaquinaResponseDTO> listaMaquinas;
    private ShapeableImageView btnProfileMenu;
    private PopupMenu profilePopupMenu;
    private SearchView searchView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Usar SharedPreferencesManager em vez de SharedPreferences direto
        prefsManager = SharedPreferencesManager.getInstance(requireContext());

        textMensagem = view.findViewById(R.id.textMensagem);

        // Obter dados do SharedPreferencesManager
        String userName = prefsManager.getUserName();
        userEmail = prefsManager.getUserEmail();
        userId = prefsManager.getUserId();

        textMensagem.setText("Olá, " + userName + "! Seja Bem-vindo(a)!");

        searchView = view.findViewById(R.id.searchView);
        searchView.setIconifiedByDefault(false);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filtrarParadasTexto(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filtrarParadasTexto(newText);
                return true;
            }
        });

        btnProfileMenu = view.findViewById(R.id.btnProfileMenu);
        setupProfileMenu();

        filtrarParadas = view.findViewById(R.id.filterButton);
        chipGroupSetores = view.findViewById(R.id.chipGroupSetores);
        recyclerViewParadas = view.findViewById(R.id.recyclerViewParadas);

        apiService = RetrofitClient.getApiService();
        sqlApiService = RetrofitClient.getSqlApiService();
        db = FirebaseFirestore.getInstance();

        paradasList = new ArrayList<>();
        allParadasList = new ArrayList<>();
        listaMaquinas = new ArrayList<>();

        recyclerViewParadas.setLayoutManager(new LinearLayoutManager(getContext()));
        paradaAdapter = new ParadaAdapter(paradasList, sqlApiService);
        paradaAdapter.setOnItemClickListener(this::abrirModalParada);
        recyclerViewParadas.setAdapter(paradaAdapter);

        carregarImagemPerfil();
        obterInformacoesUsuarioECarregarParadas();

        filtrarParadas.setOnClickListener(v -> {
            if ("regular".equals(userType)) {
                CustomToast.showInfo(getContext(), "Operador: Visualizando apenas paradas do setor " + userSetor);
                return;
            }
            chipGroupSetores.setVisibility(chipGroupSetores.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
        });

        chipGroupSetores.setVisibility(View.GONE);
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

    private void filtrarParadasTexto(String texto) {
        List<Parada> filtradas = new ArrayList<>();

        for (Parada parada : allParadasList) {
            if (
                    (parada.getDes_parada() != null && parada.getDes_parada().toLowerCase().contains(texto.toLowerCase())) ||
                            (parada.getDes_setor() != null && parada.getDes_setor().toLowerCase().contains(texto.toLowerCase())) ||
                            (parada.getId_maquina() != null && String.valueOf(parada.getId_maquina()).contains(texto)) ||
                            (parada.getId_usuario() != null && String.valueOf(parada.getId_usuario()).contains(texto))
            ) {
                filtradas.add(parada);
            }
        }

        paradaAdapter.setParadas(filtradas);
    }

    private void setupProfileMenu() {
        btnProfileMenu.setOnClickListener(v -> {
            profilePopupMenu = new PopupMenu(requireContext(), v);
            profilePopupMenu.getMenuInflater().inflate(R.menu.profile_menu, profilePopupMenu.getMenu());

            profilePopupMenu.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.menu_profile) {
                    abrirPerfil();
                    return true;
                } else if (itemId == R.id.menu_notification_history) {
                    abrirHistoricoNotificacoes();
                    return true;
                }
                return false;
            });

            profilePopupMenu.show();
        });
    }

    private void abrirPerfil() {
        if (getActivity() instanceof MainActivity) {
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
            navController.navigate(R.id.navigation_logout);
        }
    }

    private void abrirHistoricoNotificacoes() {
        if (getActivity() instanceof MainActivity) {
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
            navController.navigate(R.id.notificationHistoryFragment);
        }
    }

    private void obterInformacoesUsuarioECarregarParadas() {
        if (getActivity() instanceof MainActivity) {
            userIntType = ((MainActivity) getActivity()).getUserType();
        }

        // Já obtidos no onViewCreated, mas podemos verificar novamente
        userEmail = prefsManager.getUserEmail();
        userId = prefsManager.getUserId();

        Log.d("HomeFragment", "userEmail ATUAL: " + userEmail);
        Log.d("HomeFragment", "userId ATUAL: " + userId);

        if (userEmail != null && !userEmail.isEmpty()) {
            db.collection("trabalhadores")
                    .whereEqualTo("login", userEmail)
                    .limit(1)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            DocumentSnapshot document = task.getResult().getDocuments().get(0);
                            String tipoPerfilOriginal = document.getString("tipo_perfil");
                            userSetor = document.getString("setor");
                            Long sqlId = document.getLong("sqlId");

                            if (userType == null && tipoPerfilOriginal != null) {
                                switch (tipoPerfilOriginal.toLowerCase()) {
                                    case "operador": userType = "regular"; break;
                                    case "engenheiro": userType = "man"; break;
                                    case "supervisor": userType = "man"; break;
                                    default: userType = "regular";
                                }
                            }

                            // Salvar no SharedPreferencesManager se necessário
                            if (sqlId != null) {
                                prefsManager.setUserId(sqlId.intValue());
                            }

                            buscarNomeDoUsuarioAtual(userEmail, new UsuarioNomeCallback() {
                                @Override
                                public void onNomeEncontrado(String nomeUsuario) {
                                    prefsManager.setUserName(nomeUsuario);
                                    if (isAdded() && getContext() != null) {
                                        requireActivity().runOnUiThread(() -> {
                                            textMensagem.setText("Olá, " + nomeUsuario + "! Seja bem-vindo(a)!");
                                        });
                                    }
                                }

                                @Override
                                public void onErro(String mensagemErro) {
                                    Log.e("HomeFragment", "Erro ao buscar nome: " + mensagemErro);
                                }
                            });

                        }
                        carregarParadas();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("HomeFragment", "Erro ao buscar informações do usuário: " + e.getMessage());
                        carregarParadas();
                    });
        } else {
            Log.e("HomeFragment", "Email do usuário não encontrado no SharedPreferencesManager");
            carregarParadas();
        }
    }

    private String getProfileImageKey() {
        String userEmail = prefsManager.getUserEmail();
        return "profile_image_" + (userEmail != null ? userEmail.hashCode() : "default");
    }

    private String getCloudinaryUrlKey() {
        String userEmail = prefsManager.getUserEmail();
        return "cloudinary_url_" + (userEmail != null ? userEmail.hashCode() : "default");
    }

    private void carregarImagemPerfil() {
        if (!isAdded() || getContext() == null) {
            Log.d("HomeFragment", "Fragment não está anexado, abortando carregamento de imagem");
            return;
        }

        if (btnProfileMenu == null) {
            Log.e("HomeFragment", "btnProfileMenu é nulo - não pode carregar imagem");
            return;
        }

        Log.d("HomeFragment", "Carregando imagem do perfil no btnProfileMenu");

        // Para imagens, ainda precisamos usar SharedPreferences direto pois o SharedPreferencesManager
        // não gerencia as imagens. Mas podemos migrar isso também se necessário.
        android.content.SharedPreferences imagePrefs = requireContext().getSharedPreferences("ProfilePrefs", Context.MODE_PRIVATE);

        String cloudinaryUrl = imagePrefs.getString(getCloudinaryUrlKey(), null);
        if (cloudinaryUrl != null && !cloudinaryUrl.isEmpty()) {
            Log.d("HomeFragment", "Carregando imagem do Cloudinary: " + cloudinaryUrl);
            Glide.with(requireContext())
                    .load(cloudinaryUrl)
                    .circleCrop()
                    .placeholder(R.drawable.img)
                    .error(R.drawable.img)
                    .into(btnProfileMenu);
            return;
        }

        String encodedImage = imagePrefs.getString(getProfileImageKey(), null);
        if (encodedImage != null && !encodedImage.isEmpty()) {
            Log.d("HomeFragment", "Carregando imagem codificada em Base64");
            try {
                byte[] byteArray = Base64.decode(encodedImage, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
                if (bitmap != null) {
                    Glide.with(requireContext())
                            .load(bitmap)
                            .circleCrop()
                            .placeholder(R.drawable.img)
                            .error(R.drawable.img)
                            .into(btnProfileMenu);
                } else {
                    Log.e("HomeFragment", "Falha ao decodificar Bitmap");
                    btnProfileMenu.setImageResource(R.drawable.img);
                }
            } catch (Exception e) {
                Log.e("HomeFragment", "Erro ao decodificar imagem Base64: " + e.getMessage());
                btnProfileMenu.setImageResource(R.drawable.img);
            }
        } else {
            Log.d("HomeFragment", "Nenhuma imagem encontrada, usando padrão");
            btnProfileMenu.setImageResource(R.drawable.img);
        }
    }

    private void abrirModalParada(Parada parada) {
        View modalView = LayoutInflater.from(requireContext()).inflate(R.layout.modal_parada, null);

        TextView txtIdMaquina = modalView.findViewById(R.id.txtIdMaquina);
        TextView txtCodigoColaborador = modalView.findViewById(R.id.txtCodigoColaborador);
        TextView txtNomeMaquina = modalView.findViewById(R.id.txtNomeMaquina);
        TextView txtSetor = modalView.findViewById(R.id.txtSetor);
        TextView txtDataParada = modalView.findViewById(R.id.txtData);
        TextView txtHoraInicio = modalView.findViewById(R.id.txtHoraInicio);
        TextView txtHoraFim = modalView.findViewById(R.id.txtHoraFim);
        TextView txtDuracao = modalView.findViewById(R.id.txtDuracao);
        TextView txtDescricaoParada = modalView.findViewById(R.id.txtDescricao);

        txtIdMaquina.setText(parada.getId_maquina() != null ? String.valueOf(parada.getId_maquina()) : "Não informado");
        txtCodigoColaborador.setText(parada.getId_usuario() != null ? String.valueOf(parada.getId_usuario()) : "Não informado");
        txtSetor.setText(parada.getDes_setor() != null ? parada.getDes_setor() : "Não informado");

        if (parada.getDt_parada() != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd, MMM yyyy", Locale.getDefault());
            txtDataParada.setText(dateFormat.format(parada.getDt_parada()));
        } else {
            txtDataParada.setText("Não informado");
        }

        txtDescricaoParada.setText(parada.getDes_parada() != null ? parada.getDes_parada() : "Não informado");

        processarHorasEDuracao(parada, txtHoraInicio, txtHoraFim, txtDuracao);
        buscarNomeMaquinaPorId(parada.getId_maquina(), txtNomeMaquina);

        // BOTÃO MANUTENÇÃO - MANTIDO
        Button btnManutencao = modalView.findViewById(R.id.btnManutencao);
        if (btnManutencao != null && "man".equals(userType)) {
            btnManutencao.setVisibility(View.VISIBLE);
            btnManutencao.setOnClickListener(v -> {
                bottomSheetDialog.dismiss();
                finalizarParada(parada);
            });
        } else {
            btnManutencao.setVisibility(View.GONE);
        }

        bottomSheetDialog = new BottomSheetDialog(requireContext());
        bottomSheetDialog.setContentView(modalView);
        bottomSheetDialog.show();

        ImageButton btnFechar = modalView.findViewById(R.id.btnFechar);
        if (btnFechar != null) {
            btnFechar.setOnClickListener(v -> bottomSheetDialog.dismiss());
        }
    }

    private void processarHorasEDuracao(Parada parada, TextView txtHoraInicio, TextView txtHoraFim, TextView txtDuracao) {
        try {
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            timeFormat.setTimeZone(TimeZone.getDefault());

            if (parada.getHora_Inicio() != null) {
                txtHoraInicio.setText(timeFormat.format(parada.getHora_Inicio()));
            } else {
                txtHoraInicio.setText("Não informado");
            }

            if (parada.getHora_Fim() != null) {
                txtHoraFim.setText(timeFormat.format(parada.getHora_Fim()));
            } else {
                txtHoraFim.setText("Não informado");
            }

            if (parada.getHora_Inicio() != null && parada.getHora_Fim() != null) {
                long diff = parada.getHora_Fim().getTime() - parada.getHora_Inicio().getTime();

                if (diff < 0) {
                    diff += 24 * 60 * 60 * 1000;
                }

                long diffMinutes = diff / (60 * 1000);
                long diffHours = diffMinutes / 60;
                long remainingMinutes = diffMinutes % 60;

                String duracao = diffHours > 0 ?
                        String.format(Locale.getDefault(), "%dh %02dmin", diffHours, remainingMinutes) :
                        String.format(Locale.getDefault(), "%dmin", diffMinutes);
                txtDuracao.setText(duracao);
            } else {
                txtDuracao.setText("Não calculável");
            }

        } catch (Exception e) {
            txtHoraInicio.setText("Erro");
            txtHoraFim.setText("Erro");
            txtDuracao.setText("Erro");
            e.printStackTrace();
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
                    listaMaquinas = response.body();
                    String nomeMaquina = "Máquina não encontrada";

                    for (MaquinaResponseDTO maquina : listaMaquinas) {
                        if (maquina.getId() != null && maquina.getId().equals(idMaquina.longValue())) {
                            nomeMaquina = maquina.getNome() != null ? maquina.getNome() : "Nome não disponível";
                            break;
                        }
                    }
                    txtNomeMaquina.setText(nomeMaquina);
                } else {
                    txtNomeMaquina.setText("Erro ao buscar máquina");
                }
            }

            @Override
            public void onFailure(Call<List<MaquinaResponseDTO>> call, Throwable t) {
                txtNomeMaquina.setText("Falha na conexão");
            }
        });
    }

    private String buscarNomeMaquinaPorId(Integer idMaquina) {
        if (idMaquina == null) return "Máquina não informada";

        try {
            if (listaMaquinas != null) {
                for (MaquinaResponseDTO maquina : listaMaquinas) {
                    if (maquina.getId() != null && maquina.getId().equals(idMaquina.longValue())) {
                        return maquina.getNome() != null ? maquina.getNome() : "Máquina " + idMaquina;
                    }
                }
            }
            return "Máquina " + idMaquina;
        } catch (Exception e) {
            return "Máquina " + idMaquina;
        }
    }

    private void carregarParadas() {
        Call<List<RegistroParadaResponseDTO>> call = apiService.getAllRegistros();
        call.enqueue(new Callback<List<RegistroParadaResponseDTO>>() {
            @Override
            public void onResponse(Call<List<RegistroParadaResponseDTO>> call, Response<List<RegistroParadaResponseDTO>> response) {
                if (!isAdded() || getContext() == null) {
                    Log.w("HomeFragment", "Fragment não está anexado, ignorando resposta");
                    return;
                }

                if (response.isSuccessful() && response.body() != null) {
                    paradasList.clear();
                    allParadasList.clear();

                    for (RegistroParadaResponseDTO registro : response.body()) {
                        allParadasList.add(converterParaParada(registro));
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
                        if (isAdded()) {
                            addChipsToChipGroup(new ArrayList<>(setores));
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<List<RegistroParadaResponseDTO>> call, Throwable t) {
                if (isAdded() && getContext() != null) {
                    requireActivity().runOnUiThread(() -> {
                        CustomToast.showError(requireContext(), "Falha na conexão: " + t.getMessage());
                    });
                } else {
                    Log.e("HomeFragment", "Fragment não está anexado, não é possível mostrar Toast");
                }
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
            filtrarParadas.setVisibility(View.GONE);
            chipGroupSetores.setVisibility(View.GONE);
        } else {
            paradasList.addAll(allParadasList);
            filtrarParadas.setVisibility(View.GONE);
            chipGroupSetores.setVisibility(View.VISIBLE);
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
        if (!isAdded() || getContext() == null || chipGroupSetores == null) {
            Log.w("HomeFragment", "Fragment não está anexado ou chipGroupSetores é nulo");
            return;
        }

        chipGroupSetores.removeAllViews();

        // Chip "Todos"
        chipTodos = new Chip(requireContext());
        chipTodos.setText("Todos");
        chipTodos.setCheckable(true);
        chipTodos.setChecked(true);
        chipGroupSetores.addView(chipTodos);

        for (String sectorName : sectors) {
            Chip chip = new Chip(requireContext());
            chip.setText(sectorName);
            chip.setCheckable(true);
            chipGroupSetores.addView(chip);
        }

        chipGroupSetores.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty() || chipTodos.isChecked()) {
                chipTodos.setChecked(true);
                paradaAdapter.setParadas(new ArrayList<>(allParadasList));
                return;
            }

            List<String> setoresSelecionados = new ArrayList<>();
            for (int id : checkedIds) {
                Chip chip = group.findViewById(id);
                if (chip != null && !chip.getText().toString().equals("Todos")) {
                    setoresSelecionados.add(chip.getText().toString());
                }
            }

            List<Parada> filtradas = new ArrayList<>();
            for (Parada parada : allParadasList) {
                if (setoresSelecionados.contains(parada.getDes_setor())) {
                    filtradas.add(parada);
                }
            }

            paradaAdapter.setParadas(filtradas);
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
            CustomToast.showWarning(getContext(), "Acesso restrito a engenheiros.");
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
        bundle.putString("horaInicio", formatarHora(parada.getHora_Inicio()));
        bundle.putString("horaFim", formatarHora(parada.getHora_Fim()));

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

    private String formatarHora(Date date) {
        if (date == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        return sdf.format(date);
    }

    private void buscarNomeDoUsuarioAtual(String userEmail, UsuarioNomeCallback callback) {
        db.collection("trabalhadores")
                .whereEqualTo("login", userEmail)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                        String login = document.getString("login");
                        if (login != null && !login.isEmpty()) {
                            callback.onNomeEncontrado(login);
                        } else {
                            callback.onErro("Login não encontrado");
                        }
                    } else {
                        callback.onErro("Usuário não encontrado");
                    }
                })
                .addOnFailureListener(e -> {
                    callback.onErro("Erro na busca");
                });
    }
}