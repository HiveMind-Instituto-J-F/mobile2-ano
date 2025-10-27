package com.aula.mobile_hivemind.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.aula.mobile_hivemind.MainActivity;
import com.aula.mobile_hivemind.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Esconder action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        Button btnEntrar = findViewById(R.id.btnEntrar);
        btnEntrar.setOnClickListener(v -> {
            String txtEmail = ((EditText) findViewById(R.id.editTextEMAILCONT)).getText().toString();
            String txtSenha = ((EditText) findViewById(R.id.editTextPassword)).getText().toString();

            if (txtEmail.isEmpty() || txtSenha.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
                return;
            }

            // Verificar diretamente na collection trabalhadores
            verificarLoginNoFirestore(txtEmail, txtSenha);
        });
    }

    private void verificarLoginNoFirestore(String email, String senha) {
        // Busca o usuário na collection trabalhadores pelo login (email)
        db.collection("trabalhadores")
                .whereEqualTo("login", email)
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        // Usuário encontrado - verificar senha
                        DocumentSnapshot document = task.getResult().getDocuments().get(0);
                        String senhaFirestore = document.getString("senha");
                        String tipoPerfil = document.getString("tipo_perfil");

                        if (senhaFirestore != null && senhaFirestore.equals(senha)) {
                            // LOGIN BEM-SUCEDIDO
                            String userType = mapearTipoPerfilParaUserType(tipoPerfil);

                            android.content.SharedPreferences sharedPreferences = getSharedPreferences("ProfilePrefs", MODE_PRIVATE);
                            android.content.SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("user_email", email); // 🔧 SALVA O EMAIL
                            editor.apply(); // 🔧 IMPORTANTE: Salva imediatamente

                            Log.d("Login", "Email salvo no SharedPreferences: " + email);

                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.putExtra("USER_TYPE", userType);
                            intent.putExtra("TIPO_PERFIL_ORIGINAL", tipoPerfil);
                            startActivity(intent);
                            finish();

                        } else {
                            // Senha incorreta
                            Toast.makeText(LoginActivity.this, "Senha incorreta", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        // Usuário não encontrado
                        Toast.makeText(LoginActivity.this, "Usuário não encontrado", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(LoginActivity.this, "Erro: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private String mapearTipoPerfilParaUserType(String tipoPerfil) {
        if (tipoPerfil == null) return "regular";

        switch (tipoPerfil.toLowerCase()) {
            case "operador":
                return "regular";
            case "engenheiro":
                return "MOP";
            case "supervisor":
                return "RH";
            default:
                return "regular";
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Se quiser manter usuário logado entre sessões, pode implementar SharedPreferences
        // Por enquanto, sempre mostrar tela de login
    }
}