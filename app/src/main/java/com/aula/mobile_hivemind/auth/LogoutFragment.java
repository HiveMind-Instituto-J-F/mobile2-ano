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
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.aula.mobile_hivemind.R;
import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.android.material.imageview.ShapeableImageView;
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
    private static final String cloudName = "djouiin10";
    private static final String uploadPreset = "Main_preset";

    private TextView usuarioLogado;
    private Button btnSair;
    private ShapeableImageView imgPerfil;

    // Permissões e launchers
    private ActivityResultLauncher<String[]> permissionLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<Uri> cameraLauncher;

    private Uri currentPhotoUri;
    private SharedPreferences sharedPreferences;
    private FirebaseFirestore db;
    private String userEmail;
    private boolean cloudinaryInitialized = false;

    public LogoutFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = requireContext().getSharedPreferences(PREFS_NAME, 0);
        db = FirebaseFirestore.getInstance();
        initializeLaunchers();
        initCloudinary();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_logout, container, false);

        usuarioLogado = view.findViewById(R.id.usuarioLogado);
        btnSair = view.findViewById(R.id.btnSair);
        imgPerfil = view.findViewById(R.id.imgFoto);

        loadUserProfileImage();

        btnSair.setOnClickListener(v -> mostrarDialogoConfirmacaoLogout());
        imgPerfil.setOnClickListener(v -> showImagePickerDialog());

        return view;
    }

    private void initCloudinary() {
        try {
            Map config = new HashMap();
            config.put("cloud_name", cloudName);
            config.put("secure", true);

            // 🔧 INICIALIZAR SEM VERIFICAR SE JÁ ESTÁ INICIALIZADO
            MediaManager.init(requireContext(), config);
            cloudinaryInitialized = true;
            Log.d(TAG, "Cloudinary inicializado com sucesso");

        } catch (Exception e) {
            Log.e(TAG, "Erro ao inicializar Cloudinary: " + e.getMessage());
            cloudinaryInitialized = false;
            Toast.makeText(requireContext(), "Cloudinary não configurado", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isCloudinaryReady() {
        return cloudinaryInitialized;
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
                    .placeholder(R.drawable.img)
                    .into(imgPerfil);
            return;
        }

        String encodedImage = sharedPreferences.getString(getProfileImageKey(), null);
        if (encodedImage != null) {
            byte[] byteArray = Base64.decode(encodedImage, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
            if (bitmap != null) {
                imgPerfil.setImageBitmap(bitmap);
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

//        Toast.makeText(getContext(), "Deslogado com sucesso", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    private void uploadToCloudinary(Uri imageUri) {
        if (!isCloudinaryReady()) {
            Log.e(TAG, "Cloudinary não está inicializado");
//            Toast.makeText(requireContext(), "Serviço de imagens não disponível", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Iniciando upload para Cloudinary");

        String publicId = "profile_" + (userEmail != null ? userEmail.replace("@", "_").replace(".", "_") : "unknown") + "_" + System.currentTimeMillis();

        MediaManager.get().upload(imageUri)
                .option("public_id", publicId)
                .option("folder", "profile_pictures")
                .unsigned(uploadPreset)
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {
                        Log.d(TAG, "Upload iniciado: " + requestId);
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(requireContext(), "Enviando imagem...", Toast.LENGTH_SHORT).show());
                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {
                        Log.d(TAG, "Upload progresso: " + bytes + "/" + totalBytes);
                    }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        Log.d(TAG, "Upload concluído com sucesso");
                        String imageUrl = (String) resultData.get("secure_url");

                        requireActivity().runOnUiThread(() -> {
                            Toast.makeText(requireContext(), "Imagem salva na nuvem!", Toast.LENGTH_SHORT).show();
                            saveCloudinaryUrl(imageUrl);
                            Glide.with(requireContext())
                                    .load(imageUrl)
                                    .placeholder(R.drawable.img)
                                    .into(imgPerfil);
                        });
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        Log.e(TAG, "Erro no upload: " + error.getDescription());
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(requireContext(), "Erro ao enviar imagem", Toast.LENGTH_LONG).show());
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {
                        Log.w(TAG, "Upload reagendado: " + error.getDescription());
                    }
                })
                .dispatch();
    }

    private void initializeLaunchers() {
        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    Boolean cameraGranted = result.get(Manifest.permission.CAMERA);
                    Boolean storageGranted = result.getOrDefault(
                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                                    ? Manifest.permission.READ_MEDIA_IMAGES
                                    : Manifest.permission.READ_EXTERNAL_STORAGE,
                            false
                    );

                    if (cameraGranted != null && cameraGranted && storageGranted != null && storageGranted) {
                        Log.d(TAG, "Permissões concedidas");
                    } else {
                        Toast.makeText(requireContext(), "Permissões necessárias não concedidas", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            handleSelectedImage(imageUri);
                        }
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
            Log.e(TAG, "Erro ao criar arquivo para foto", e);
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
            imgPerfil.setImageBitmap(resizedBitmap);

            uploadToCloudinary(imageUri);

        } catch (IOException e) {
            Toast.makeText(requireContext(), "Erro ao carregar imagem", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Erro ao carregar imagem", e);
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