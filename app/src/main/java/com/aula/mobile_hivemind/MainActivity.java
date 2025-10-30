package com.aula.mobile_hivemind;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.aula.mobile_hivemind.auth.LoginActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.aula.mobile_hivemind.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private FloatingActionButton fabMain;
    private ActivityMainBinding binding;
    private NavController navController;
    private String userType;
    private boolean isFabOpen = false;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        userType = getIntent().getStringExtra("USER_TYPE");
        if (userType == null) {
            redirectToLogin();
            return;
        }

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        fabMain = findViewById(R.id.fab_main);

        navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);

        setupNavigationByUserType();
        setupFabAction();

        Toast.makeText(this, "Bem-vindo! Perfil: " + userType, Toast.LENGTH_SHORT).show();
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // -------------------- FAB --------------------
    public void setFabVisibility(boolean visible) {
        if (fabMain != null) {
            if (visible) {
                fabMain.setVisibility(View.VISIBLE);
                fabMain.setImageResource(R.drawable.baseline_add_24);
                fabMain.setAlpha(1f);
                fabMain.setScaleX(1f);
                fabMain.setScaleY(1f);
            } else {
                fabMain.setVisibility(View.GONE);
            }
        }
    }

    public void setBottomNavigationVisibility(boolean visible) {
        BottomNavigationView navView = binding.navView;
        if (navView != null) {
            navView.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    private void setupFabAction() {
        // 🔧 TODOS OS USUÁRIOS PODEM VER O FAB, MAS COM AÇÕES DIFERENTES
        fabMain.setVisibility(View.VISIBLE);

        fabMain.setOnClickListener(v -> {
            switch (userType) {
                case "regular": // OPERADOR
                    navController.navigate(R.id.addParadaFragment);
                    break;

                case "MOP": // ENGENHEIRO
                    navController.navigate(R.id.addParadaFragment);
                    break;

                case "RH": // SUPERVISOR
                    // 🔧 RH TAMBÉM PODE ADICIONAR PARADAS
                    navController.navigate(R.id.addParadaFragment);
                    break;

                default:
                    Toast.makeText(this, "Ação não disponível para seu perfil", Toast.LENGTH_SHORT).show();
                    break;
            }
        });
    }

    private void closeFab(FloatingActionButton... fabs) {
        for (FloatingActionButton fab : fabs) {
            fab.hide();
        }
        isFabOpen = false;
    }

    @Override
    public void onBackPressed() {
        if (isFabOpen) {
            closeFab(findViewById(R.id.fab_main));
        } else {
            super.onBackPressed();
        }
    }

    // -------------------- Usuário e navegação --------------------
    public String getUserType() {
        return userType;
    }

    private void setupNavigationByUserType() {
        BottomNavigationView navView = binding.navView;
        AppBarConfiguration appBarConfiguration;

        if ("regular".equals(userType)) {
            // OPERADOR
            appBarConfiguration = new AppBarConfiguration.Builder(
                    R.id.navigation_home,
                    R.id.navigation_logout
            ).build();

            navView.setVisibility(View.VISIBLE);
            navView.getMenu().findItem(R.id.navigation_home).setVisible(true);
            navView.getMenu().findItem(R.id.navigation_dashboard).setVisible(false);
            navView.getMenu().findItem(R.id.navigation_calendar).setVisible(false);
            navView.getMenu().findItem(R.id.navigation_homerh).setVisible(false);
            navView.getMenu().findItem(R.id.navigation_logout).setVisible(true);

        } else if ("man".equals(userType)) {
            // ENGENHEIRO
            appBarConfiguration = new AppBarConfiguration.Builder(
                    R.id.navigation_home,
                    R.id.navigation_dashboard,
                    R.id.navigation_logout
            ).build();

            navView.setVisibility(View.VISIBLE);
            navView.getMenu().findItem(R.id.navigation_home).setVisible(true);
            navView.getMenu().findItem(R.id.navigation_dashboard).setVisible(true);
            navView.getMenu().findItem(R.id.navigation_calendar).setVisible(false);
            navView.getMenu().findItem(R.id.navigation_homerh).setVisible(false);
            navView.getMenu().findItem(R.id.navigation_logout).setVisible(true);

        } else if ("RH".equals(userType)) {
            // SUPERVISOR
            appBarConfiguration = new AppBarConfiguration.Builder(
                    R.id.navigation_homerh,
                    R.id.navigation_calendar,
                    R.id.navigation_logout
            ).build();

            navView.setVisibility(View.VISIBLE);
            navView.getMenu().findItem(R.id.navigation_home).setVisible(false);
            navView.getMenu().findItem(R.id.navigation_dashboard).setVisible(false);
            navView.getMenu().findItem(R.id.navigation_calendar).setVisible(true);
            navView.getMenu().findItem(R.id.navigation_homerh).setVisible(true);
            navView.getMenu().findItem(R.id.navigation_logout).setVisible(true);

            // Navegar para home RH por padrão
            navController.navigate(R.id.navigation_homerh);

        } else {
            // PADRÃO (Operador)
            appBarConfiguration = new AppBarConfiguration.Builder(
                    R.id.navigation_home,
                    R.id.navigation_logout
            ).build();
        }

        NavigationUI.setupWithNavController(navView, navController);
    }
}