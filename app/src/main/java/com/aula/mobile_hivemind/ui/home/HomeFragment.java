package com.aula.mobile_hivemind.ui.home;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
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
import android.widget.ImageView;
import android.widget.PopupMenu;
import androidx.appcompat.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
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
import com.aula.mobile_hivemind.notification.ParadaNotificationManager;
import com.aula.mobile_hivemind.recyclerViewParadas.Parada;
import com.aula.mobile_hivemind.recyclerViewParadas.ParadaAdapter;
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
    private SharedPreferences sharedPreferences;
    private String userEmail;
    private String userType;
    private String userSetor;
    private ShapeableImageView avatar;
    private SqlApiService sqlApiService;
    private BottomSheetDialog bottomSheetDialog;
    private List<MaquinaResponseDTO> listaMaquinas;
    private ShapeableImageView btnProfileMenu;
    private PopupMenu profilePopupMenu;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        sharedPreferences = requireContext().getSharedPreferences("ProfilePrefs", Context.MODE_PRIVATE);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sharedPreferences = requireContext().getSharedPreferences("ProfilePrefs", Context.MODE_PRIVATE);

        textMensagem = view.findViewById(R.id.textMensagem);

        String userName = sharedPreferences.getString("user_name", "Usuário");
        textMensagem.setText("Olá, " + userName + "! Seja Bem-vindo(a)!");

        SearchView searchView = view.findViewById(R.id.searchView);

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
                Toast.makeText(getContext(), "Operador: Visualizando apenas paradas do setor " + userSetor, Toast.LENGTH_SHORT).show();
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
        // Navegar para fragment de perfil
        if (getActivity() instanceof MainActivity) {
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
            navController.navigate(R.id.navigation_logout);
        }
    }

    private void abrirHistoricoNotificacoes() {
        // Navegar para fragment de histórico de notificações
        if (getActivity() instanceof MainActivity) {
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
            navController.navigate(R.id.notificationHistoryFragment);
        }
    }

    private void obterInformacoesUsuarioECarregarParadas() {
        if (getActivity() instanceof MainActivity) {
            userType = ((MainActivity) getActivity()).getUserType();
        }

        userEmail = sharedPreferences.getString("user_email", null);

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
                                    case "engenheiro": userType = "MOP"; break;
                                    case "supervisor": userType = "RH"; break;
                                    default: userType = "regular";
                                }
                            }

                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("user_setor", userSetor);
                            editor.putString("user_type", userType);

                            if (sqlId != null) {
                                editor.putInt("user_id", sqlId.intValue());
                            }

                            buscarNomeDoUsuarioAtual(userEmail, new UsuarioNomeCallback() {
                                @Override
                                public void onNomeEncontrado(String nomeUsuario) {
                                    editor.putString("user_name", nomeUsuario);
                                    editor.apply();
                                    textMensagem.setText("Olá, " + nomeUsuario + "! Seja bem-vindo(a)!");
                                }

                                @Override
                                public void onErro(String mensagemErro) {
                                    editor.apply();
                                }
                            });

                        }
                        carregarParadas();
                    })
                    .addOnFailureListener(e -> {
                        carregarParadas();
                    });
        } else {
            carregarParadas();
        }
    }

    private String getProfileImageKey() {
        String userEmail = sharedPreferences.getString("user_email", null);
        return "profile_image_" + (userEmail != null ? userEmail.hashCode() : "default");
    }

    private String getCloudinaryUrlKey() {
        String userEmail = sharedPreferences.getString("user_email", null);
        return "cloudinary_url_" + (userEmail != null ? userEmail.hashCode() : "default");
    }

    private void carregarImagemPerfil() {
        // Verificar se o fragment está anexado à Activity
        if (!isAdded() || getContext() == null) {
            Log.d("HomeFragment", "Fragment não está anexado, abortando carregamento de imagem");
            return;
        }

        if (btnProfileMenu == null) {
            Log.e("HomeFragment", "btnProfileMenu é nulo - não pode carregar imagem");
            return;
        }

        Log.d("HomeFragment", "Carregando imagem do perfil no btnProfileMenu");

        String cloudinaryUrl = sharedPreferences.getString(getCloudinaryUrlKey(), null);
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

        String encodedImage = sharedPreferences.getString(getProfileImageKey(), null);
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

        Button btnFinalizarParada = modalView.findViewById(R.id.btnFinalizarParada);
        if (btnFinalizarParada != null && "MOP".equals(userType)) {
            btnFinalizarParada.setVisibility(View.VISIBLE);
            btnFinalizarParada.setOnClickListener(v -> {
                RegistroParadaResponseDTO paradaMongo = converterParaRegistroParadaDTO(parada);
                finalizarParada(paradaMongo);
            });
        } else {
            btnFinalizarParada.setVisibility(View.GONE);
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
                    // Adicionar 24 horas para corrigir
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
                    requireActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(requireContext(), "Falha na conexão: " + t.getMessage(), Toast.LENGTH_LONG).show();
                        }
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
                // Se "Todos" estiver ativo, mostra todas
                chipTodos.setChecked(true);
                paradaAdapter.setParadas(new ArrayList<>(allParadasList));
                return;
            }

            // Caso contrário, mostra apenas as selecionadas
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

    private void finalizarParada(RegistroParadaResponseDTO paradaMongo) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Finalizar Parada")
                .setMessage("Tem certeza que deseja finalizar esta parada?")
                .setPositiveButton("Sim", (dialog, which) -> processarFinalizacao(paradaMongo))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void processarFinalizacao(RegistroParadaResponseDTO paradaMongo) {
        String nomeEngenheiro = obterNomeEngenheiroLogado();
        ParadaSQLRequestDTO paradaSQL = new ParadaSQLRequestDTO(paradaMongo);

        ProgressDialog progressDialog = new ProgressDialog(requireContext());
        progressDialog.setMessage("Finalizando parada...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        Call<ResponseBody> call = sqlApiService.salvarParadaSQL(paradaSQL);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                progressDialog.dismiss();
                if (response.isSuccessful()) {
                    excluirParadaMongoDB(paradaMongo.getId(), paradaMongo, nomeEngenheiro);
                } else {
                    Toast.makeText(requireContext(), "Erro ao salvar no SQL: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(requireContext(), "Falha na conexão SQL: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String obterNomeEngenheiroLogado() {
        return sharedPreferences.getString("user_name", "Engenheiro");
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

    private void excluirParadaMongoDB(String idMongo, RegistroParadaResponseDTO paradaMongo, String nomeEngenheiro) {
        if (idMongo == null || idMongo.isEmpty()) {
            Toast.makeText(requireContext(), "Erro: ID da parada inválido", Toast.LENGTH_SHORT).show();
            return;
        }

        ProgressDialog progressDialog = new ProgressDialog(requireContext());
        progressDialog.setMessage("Excluindo do sistema...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        Call<ResponseBody> call = apiService.excluirRegistro(idMongo);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                progressDialog.dismiss();
                if (response.isSuccessful()) {
                    enviarNotificacaoParadaFinalizada(paradaMongo, nomeEngenheiro);
                    if (bottomSheetDialog != null && bottomSheetDialog.isShowing()) {
                        bottomSheetDialog.dismiss();
                    }
                    Toast.makeText(requireContext(), "Parada finalizada com sucesso!", Toast.LENGTH_SHORT).show();
                    carregarParadas();
                } else {
                    Toast.makeText(requireContext(), "Erro ao excluir do MongoDB", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(requireContext(), "Erro ao excluir do MongoDB: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void enviarNotificacaoParadaFinalizada(RegistroParadaResponseDTO parada, String nomeEngenheiro) {
        try {
            String tempoDuracao = calcularTempoDuracao(parada.getHora_Inicio(), parada.getHora_Fim());
            String nomeMaquina = buscarNomeMaquinaPorId(parada.getId_maquina());
            String dataParada = formatarData(parada.getDt_parada());

            buscarNomeUsuarioCriador(parada.getId_usuario(), new UsuarioNomeCallback() {
                @Override
                public void onNomeEncontrado(String nomeUsuarioCriador) {
                    ParadaNotificationManager.enviarNotificacaoParadaFinalizada(
                            requireContext(),
                            nomeEngenheiro,
                            nomeMaquina,
                            tempoDuracao,
                            dataParada,
                            nomeUsuarioCriador
                    );
                }

                @Override
                public void onErro(String mensagemErro) {
                    ParadaNotificationManager.enviarNotificacaoParadaFinalizada(
                            requireContext(),
                            nomeEngenheiro,
                            nomeMaquina,
                            tempoDuracao,
                            dataParada,
                            "Usuário " + parada.getId_usuario()
                    );
                }
            });

        } catch (Exception e) {
            String dataFallback = parada.getDt_parada() != null ?
                    new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(parada.getDt_parada()) : "data não informada";

            ParadaNotificationManager.enviarNotificacaoParadaFinalizada(
                    requireContext(),
                    nomeEngenheiro != null ? nomeEngenheiro : "Engenheiro",
                    "Máquina " + parada.getId_maquina(),
                    "Tempo não calculado",
                    dataFallback,
                    "Usuário " + parada.getId_usuario()
            );
        }
    }

    private String formatarData(Date data) {
        if (data == null) return "data não informada";
        try {
            return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(data);
        } catch (Exception e) {
            return "data não informada";
        }
    }

    private void buscarNomeUsuarioCriador(Integer idUsuario, UsuarioNomeCallback callback) {
        if (idUsuario == null) {
            callback.onErro("ID do usuário é nulo");
            return;
        }

        try {
            SharedPreferences sharedPreferences = requireContext().getSharedPreferences("ProfilePrefs", Context.MODE_PRIVATE);
            int usuarioLogadoId = sharedPreferences.getInt("user_id", -1);

            if (usuarioLogadoId == idUsuario) {
                String nomeUsuario = sharedPreferences.getString("user_name", null);
                if (nomeUsuario != null && !nomeUsuario.isEmpty()) {
                    callback.onNomeEncontrado(nomeUsuario);
                    return;
                }
            }

            buscarNomeUsuarioDoFirestore(idUsuario, callback);

        } catch (Exception e) {
            callback.onErro("Erro ao buscar usuário");
        }
    }

    private void buscarNomeUsuarioDoFirestore(Integer idUsuario, UsuarioNomeCallback callback) {
        if (idUsuario == null) {
            callback.onErro("ID do usuário é nulo");
            return;
        }

        db.collection("trabalhadores")
                .whereEqualTo("sqlId", idUsuario)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                        String login = document.getString("login");
                        Long sqlId = document.getLong("sqlId");

                        if (login != null && !login.isEmpty() && sqlId != null && sqlId.equals(idUsuario.longValue())) {
                            callback.onNomeEncontrado(login);
                        } else {
                            callback.onErro("Dados do usuário inconsistentes");
                        }
                    } else {
                        callback.onErro("Usuário não encontrado");
                    }
                })
                .addOnFailureListener(e -> {
                    callback.onErro("Falha na consulta");
                });
    }

    private String calcularTempoDuracao(Date horaInicio, Date horaFim) {
        if (horaInicio == null || horaFim == null) {
            return "Tempo não calculável";
        }

        long diff = horaFim.getTime() - horaInicio.getTime();
        long diffMinutes = diff / (60 * 1000);
        long diffHours = diffMinutes / 60;
        long remainingMinutes = diffMinutes % 60;

        if (diffHours > 0) {
            return String.format(Locale.getDefault(), "%dh %02dmin", diffHours, remainingMinutes);
        } else {
            return String.format(Locale.getDefault(), "%dmin", diffMinutes);
        }
    }

    private RegistroParadaResponseDTO converterParaRegistroParadaDTO(Parada parada) {
        return new RegistroParadaResponseDTO(
                parada.getId(),
                parada.getId_maquina(),
                parada.getId_usuario(),
                parada.getDes_parada(),
                parada.getDes_setor(),
                parada.getDt_parada(),
                parada.getHora_Fim(),
                parada.getHora_Inicio()
        );
    }
}