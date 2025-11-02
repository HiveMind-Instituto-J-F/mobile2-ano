package com.aula.mobile_hivemind.auth;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.aula.mobile_hivemind.MainActivity;
import com.aula.mobile_hivemind.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class LogoutFragment extends Fragment {
    private static final String TAG = "LogoutFragment";

    private static final String PREFS_NAME = "ProfilePrefs";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String CLOUD_NAME = "djouiin10";
    private static final String UPLOAD_PRESET = "Main_preset";

    private TextView usuarioLogado;
    private TextView txtSetorUsuario;
    private Button btnSair;
    private ImageButton btnFechar;
    private ShapeableImageView imgPerfil;

    private ActivityResultLauncher<String[]> permissionLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<Uri> cameraLauncher;

    private Uri currentPhotoUri;
    private SharedPreferences sharedPreferences;
    private FirebaseFirestore db;
    private String userEmail;
    private CardView itemInfoHistorico;
    private String userType;

    public LogoutFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = requireContext().getSharedPreferences(PREFS_NAME, 0);
        db = FirebaseFirestore.getInstance();
        initializeLaunchers();

        CloudinaryManager.init(requireContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_logout, container, false);

        usuarioLogado = view.findViewById(R.id.usuarioLogado);
        txtSetorUsuario = view.findViewById(R.id.txtSetorUsuario);
        btnSair = view.findViewById(R.id.btnSair);
        btnFechar = view.findViewById(R.id.btnFechar);
        imgPerfil = view.findViewById(R.id.imgFoto);

        userEmail = sharedPreferences.getString(KEY_USER_EMAIL, null);
        if (userEmail == null) {
            Toast.makeText(requireContext(), "Erro: usuário não identificado", Toast.LENGTH_SHORT).show();
        } else {
            usuarioLogado.setText(userEmail);
        }

        loadUserProfileImage();

        carregarSetorUsuario();

        // Obter o tipo de usuário
        userType = sharedPreferences.getString("user_type", "regular");

        // Encontrar o item do histórico
        itemInfoHistorico = view.findViewById(R.id.itemInfoHistorico);

        // Configurar visibilidade do histórico baseado no tipo de usuário
        configurarVisibilidadeHistorico();

        // Configurar clique apenas se não for engenheiro
        if (!isEngenheiro() && itemInfoHistorico != null) {
            itemInfoHistorico.setOnClickListener(v -> {
                NavController navController = Navigation.findNavController(v);
                navController.navigate(R.id.navigation_historico_diario);
            });
        }

        btnSair.setOnClickListener(v -> mostrarDialogoConfirmacaoLogout());
        btnFechar.setOnClickListener(v -> fecharTela());
        imgPerfil.setOnClickListener(v -> showImagePickerDialog());

        itemInfoHistorico.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.navigation_historico_diario);
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setFabVisibility(false);
        }
    }

    private void carregarSetorUsuario() {
        String userSetor = sharedPreferences.getString("user_setor", null);
        if (userSetor != null && !userSetor.isEmpty()) {
            txtSetorUsuario.setText(userSetor);
        } else {
            buscarSetorDoFirestore();
        }
    }

    private void buscarSetorDoFirestore() {
        if (userEmail == null) {
            txtSetorUsuario.setText("Não informado");
            return;
        }

        db.collection("trabalhadores")
                .whereEqualTo("login", userEmail)
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentSnapshot document = task.getResult().getDocuments().get(0);
                        String setor = document.getString("setor");
                        if (setor != null && !setor.isEmpty()) {
                            txtSetorUsuario.setText(setor);
                            // Salvar no SharedPreferences para uso futuro
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("user_setor", setor);
                            editor.apply();
                        } else {
                            txtSetorUsuario.setText("Não informado");
                        }
                    } else {
                        txtSetorUsuario.setText("Não informado");
                    }
                })
                .addOnFailureListener(e -> {
                    txtSetorUsuario.setText("Não informado");
                });
    }

    private boolean isEngenheiro() {
        return "MOP".equals(userType);
    }

    private void configurarVisibilidadeHistorico() {
        if (itemInfoHistorico != null) {
            if (isEngenheiro()) {
                // Engenheiro: esconder completamente o item do histórico
                itemInfoHistorico.setVisibility(View.GONE);
            } else {
                // Operador: mostrar o item do histórico
                itemInfoHistorico.setVisibility(View.VISIBLE);
            }
        }
    }

    private void fecharTela() {
        if (getActivity() != null) {
            getActivity().onBackPressed();
        }
    }

    private String getProfileImageKey() {
        return "profile_image_" + (userEmail != null ? userEmail.hashCode() : "default");
    }

    private String getCloudinaryUrlKey() {
        return "cloudinary_url_" + (userEmail != null ? userEmail.hashCode() : "default");
    }

    private void loadUserProfileImage() {
        String cloudinaryUrl = sharedPreferences.getString(getCloudinaryUrlKey(), null);
        if (cloudinaryUrl != null) {
            Glide.with(requireContext())
                    .load(cloudinaryUrl)
                    .transform(new CircleCrop())
                    .placeholder(R.drawable.img)
                    .into(imgPerfil);
            return;
        }

        String encodedImage = sharedPreferences.getString(getProfileImageKey(), null);
        if (encodedImage != null) {
            byte[] byteArray = Base64.decode(encodedImage, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
            if (bitmap != null) {
                Glide.with(requireContext())
                        .load(bitmap)
                        .transform(new CircleCrop())
                        .into(imgPerfil);
            }
        } else {
            imgPerfil.setImageResource(R.drawable.img);
        }
    }

    private void saveProfileImageLocally(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        String encodedImage = Base64.encodeToString(byteArray, Base64.DEFAULT);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(getProfileImageKey(), encodedImage);
        editor.apply();
    }

    private void saveCloudinaryUrl(String imageUrl) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(getCloudinaryUrlKey(), imageUrl);
        editor.apply();
    }

    private void removerFotoPerfil() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(getProfileImageKey());
        editor.remove(getCloudinaryUrlKey());
        editor.apply();

        imgPerfil.setImageResource(R.drawable.img);
        Toast.makeText(requireContext(), "Foto removida com sucesso", Toast.LENGTH_SHORT).show();
    }

    private void mostrarDialogoRemoverFoto() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Remover Foto")
                .setMessage("Tem certeza que deseja remover sua foto de perfil?")
                .setPositiveButton("Sim, Remover", (dialog, which) -> removerFotoPerfil())
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void showImagePickerDialog() {
        checkPermissions();

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Foto de Perfil");
        builder.setMessage("Escolha uma opção para sua foto de perfil");

        boolean usuarioTemFoto = sharedPreferences.contains(getProfileImageKey()) ||
                sharedPreferences.contains(getCloudinaryUrlKey());

        builder.setPositiveButton("Tirar Foto", (dialog, which) -> openCamera());
        builder.setNegativeButton("Escolher da Galeria", (dialog, which) -> openGallery());

        if (usuarioTemFoto) {
            builder.setNeutralButton("Remover Foto", (dialog, which) -> mostrarDialogoRemoverFoto());
        } else {
            builder.setNeutralButton("Cancelar", (dialog, which) -> dialog.dismiss());
        }

        builder.show();
    }

    private void mostrarDialogoConfirmacaoLogout() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Confirmar Saída")
                .setMessage("Tem certeza que deseja sair da sua conta?")
                .setPositiveButton("Sim, Sair", (dialog, which) -> fazerLogout())
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void fazerLogout() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(KEY_USER_EMAIL);
        editor.apply();

        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    private void uploadToCloudinary(Uri imageUri) {
        CloudinaryManager.init(requireContext());

        if (!CloudinaryManager.isInitialized()) {
            Toast.makeText(requireContext(), "Cloudinary não configurado", Toast.LENGTH_SHORT).show();
            return;
        }

        if (userEmail == null) {
            Toast.makeText(requireContext(), "Usuário não identificado. Faça login novamente.", Toast.LENGTH_SHORT).show();
            return;
        }

        String sanitizedEmail = userEmail.replace("@", "_").replace(".", "_");
        String publicId = "profile_" + sanitizedEmail + "_" + System.currentTimeMillis();

        MediaManager.get().upload(imageUri)
                .option("public_id", publicId)
                .option("folder", "profile_pictures/" + sanitizedEmail)
                .unsigned(UPLOAD_PRESET)
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {}

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {}

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        String imageUrl = (String) resultData.get("secure_url");
                        saveCloudinaryUrl(imageUrl);
                        Glide.with(requireContext())
                                .load(imageUrl)
                                .transform(new CircleCrop())
                                .placeholder(R.drawable.img)
                                .into(imgPerfil);
                        Toast.makeText(requireContext(), "Imagem salva com sucesso!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        Toast.makeText(requireContext(), "Erro ao enviar imagem: " + error.getDescription(), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {}
                })
                .dispatch();
    }

    private void initializeLaunchers() {
        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    boolean allGranted = true;
                    for (Boolean granted : result.values()) {
                        if (granted == null || !granted) {
                            allGranted = false;
                            break;
                        }
                    }
                    if (!allGranted) {
                        Toast.makeText(requireContext(), "Permissões necessárias não concedidas", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) handleSelectedImage(imageUri);
                    }
                }
        );

        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                success -> {
                    if (success && currentPhotoUri != null) {
                        handleSelectedImage(currentPhotoUri);
                    } else {
                        Toast.makeText(requireContext(), "Foto não foi tirada", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void checkPermissions() {
        String[] permissions;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_MEDIA_IMAGES};
        } else {
            permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE};
        }
        permissionLauncher.launch(permissions);
    }

    private void openCamera() {
        try {
            File photoFile = createImageFile();
            if (photoFile != null) {
                currentPhotoUri = FileProvider.getUriForFile(
                        requireContext(),
                        requireContext().getPackageName() + ".provider",
                        photoFile
                );
                cameraLauncher.launch(currentPhotoUri);
            }
        } catch (IOException e) {
            Toast.makeText(requireContext(), "Erro ao criar arquivo para foto", Toast.LENGTH_SHORT).show();
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    private void handleSelectedImage(Uri imageUri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireContext().getContentResolver(), imageUri);
            Bitmap resizedBitmap = resizeBitmap(bitmap, 500);

            saveProfileImageLocally(resizedBitmap);
            Glide.with(requireContext())
                    .load(resizedBitmap)
                    .transform(new CircleCrop())
                    .into(imgPerfil);

            uploadToCloudinary(imageUri);

        } catch (IOException e) {
            Toast.makeText(requireContext(), "Erro ao carregar imagem", Toast.LENGTH_SHORT).show();
        }
    }

    private Bitmap resizeBitmap(Bitmap bitmap, int maxSize) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        if (width <= maxSize && height <= maxSize) return bitmap;

        float ratio = (float) width / height;
        int newWidth, newHeight;

        if (width > height) {
            newWidth = maxSize;
            newHeight = (int) (maxSize / ratio);
        } else {
            newHeight = maxSize;
            newWidth = (int) (maxSize * ratio);
        }

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (currentPhotoUri != null) {
            outState.putString("currentPhotoUri", currentPhotoUri.toString());
        }
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            String savedUri = savedInstanceState.getString("currentPhotoUri");
            if (savedUri != null) {
                currentPhotoUri = Uri.parse(savedUri);
            }
        }
    }
}
