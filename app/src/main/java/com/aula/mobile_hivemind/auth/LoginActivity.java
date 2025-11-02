package com.aula.mobile_hivemind.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.aula.mobile_hivemind.MainActivity;
import com.aula.mobile_hivemind.R;
import com.aula.mobile_hivemind.utils.CustomToast;
import com.aula.mobile_hivemind.utils.SharedPreferencesManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        Button btnEntrar = findViewById(R.id.btnEntrar);
        btnEntrar.setOnClickListener(v -> {
            String txtEmail = ((EditText) findViewById(R.id.editTextEMAILCONT)).getText().toString().trim();
            String txtSenha = ((EditText) findViewById(R.id.editTextPassword)).getText().toString().trim();

            if (txtEmail.isEmpty() || txtSenha.isEmpty()) {
                CustomToast.showWarning(LoginActivity.this, "Preencha todos os campos");
                return;
            }

            verificarLoginNoFirestore(txtEmail, txtSenha);
        });
    }

    private void verificarLoginNoFirestore(String email, String senha) {
        db.collection("trabalhadores")
                .whereEqualTo("login", email)
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentSnapshot document = task.getResult().getDocuments().get(0);
                        String senhaFirestore = document.getString("senha");
                        String tipoPerfil = document.getString("tipo_perfil");

                        // OBTER ID DO DOCUMENTO DO FIRESTORE
                        String documentId = document.getId();
                        int userId = gerarUserIdFromDocumentId(documentId);

                        if (senhaFirestore != null && senhaFirestore.equals(senha)) {
                            int userType = mapearTipoPerfilParaUserType(tipoPerfil);

                            // SALVAR NO SHAREDPREFERENCESMANAGER
                            SharedPreferencesManager prefs = SharedPreferencesManager.getInstance(this);
                            prefs.setUserEmail(email);
                            prefs.setUserType(userType);
                            prefs.setUserId(userId);
                            prefs.setLoggedIn(true);

                            Log.d("Login", "Dados salvos - Email: " + email + ", Tipo: " + userType + ", ID: " + userId);

                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.putExtra("USER_TYPE", userType);
                            intent.putExtra("USER_ID", userId);
                            intent.putExtra("TIPO_PERFIL_ORIGINAL", tipoPerfil);
                            startActivity(intent);
                            finish();

                        } else {
                            CustomToast.showWarning(LoginActivity.this, "Senha incorreta");
                        }
                    } else {
                        CustomToast.showWarning(LoginActivity.this, "Usuário não encontrado");
                    }
                })
                .addOnFailureListener(e -> {
                    CustomToast.showError(LoginActivity.this, "Erro: " + e.getMessage());
                });
    }

    private int mapearTipoPerfilParaUserType(String tipoPerfil) {
        if (tipoPerfil == null) return 1; // TIPO_USUARIO_COMUM

        switch (tipoPerfil.toLowerCase()) {
            case "operador":
                return 1; // TIPO_USUARIO_COMUM
            case "engenheiro":
                return 2; // TIPO_USUARIO_MANUTENCAO
            case "supervisor":
                return 3; // TIPO_USUARIO_ADMIN
            default:
                return 1; // TIPO_USUARIO_COMUM
        }
    }

    private int gerarUserIdFromDocumentId(String documentId) {
        try {
            // Se o documentId for numérico, converter para int
            if (documentId.matches("\\d+")) {
                return Integer.parseInt(documentId);
            } else {
                // Se não for numérico, gerar um hash estável baseado no documentId
                return Math.abs(documentId.hashCode() % 10000); // Limitar a 4 dígitos
            }
        } catch (Exception e) {
            // Fallback: gerar ID baseado no timestamp
            return (int) (System.currentTimeMillis() % 10000);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }
}