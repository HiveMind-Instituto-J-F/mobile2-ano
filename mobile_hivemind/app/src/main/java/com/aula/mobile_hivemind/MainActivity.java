package com.aula.mobile_hivemind;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.aula.mobile_hivemind.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private boolean isFabOpen = false;
    private String userType;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Esconder a ActionBar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        // Binding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Primeiro buscar tipo de usuário e configurar navegação
        fetchUserTypeAndSetupNavigation();

        // Agora inicializar FAB, depois que userType já foi definido
        setupFabAction();
    }

    // -------------------- FAB --------------------
    private void setupFabAction() {
        FloatingActionButton fabMain = findViewById(R.id.fab_main);

        // Configurar visibilidade do FAB principal conforme userType
        if ("RH".equals(userType)) {
            fabMain.setVisibility(View.GONE);
        } else {
            fabMain.setVisibility(View.VISIBLE);
        }

        fabMain.setOnClickListener(v -> {
            if ("regular".equals(userType)) {
                Toast.makeText(this, "Opção 1 de regular", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Opção 1 clicada", Toast.LENGTH_SHORT).show();
            }
            closeFab(fabMain);
            isFabOpen = !isFabOpen;
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
    private void fetchUserTypeAndSetupNavigation() {
        // Aqui você pega do seu banco o tipo de usuário
        userType = getUserTypeFromDatabase(); // Exemplo síncrono

        setupNavigationByUserType(userType);
    }

    private String getUserTypeFromDatabase() {
        // TODO: substituir pela lógica real do seu banco
        return "RH"; // Exemplo: "regular", "MOP", "RH"
    }

    private void setupNavigationByUserType(String userType) {
        BottomNavigationView navView = binding.navView;

        AppBarConfiguration appBarConfiguration;

        if ("regular".equals(userType)) {
            appBarConfiguration = new AppBarConfiguration.Builder(
                    R.id.navigation_home,
                    R.id.addParadaFragment
            ).build();

            navView.getMenu().findItem(R.id.navigation_calendar).setVisible(true);
            navView.getMenu().findItem(R.id.navigation_dashboard).setVisible(false);

        } else if ("MOP".equals(userType)) {
            appBarConfiguration = new AppBarConfiguration.Builder(
                    R.id.navigation_home_mop,
                    R.id.navigation_dashboard,
                    R.id.maintenanceFragment
            ).build();
            NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);

            navView.getMenu().findItem(R.id.navigation_home).setVisible(false);
            navView.getMenu().findItem(R.id.navigation_homerh).setVisible(false);
            navView.getMenu().findItem(R.id.navigation_calendar).setVisible(false);
            navController.navigate(R.id.navigation_homerh);

        } else if ("RH".equals(userType)) {
            appBarConfiguration = new AppBarConfiguration.Builder(
                    R.id.navigation_calendar,
                    R.id.navigation_dashboard,
                    R.id.navigation_homerh
            ).build();
            NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);

            navView.getMenu().findItem(R.id.navigation_home_mop).setVisible(false);
            navView.getMenu().findItem(R.id.navigation_home).setVisible(false);
            navController.navigate(R.id.navigation_homerh);

        } else {
            appBarConfiguration = new AppBarConfiguration.Builder(
                    R.id.navigation_home
            ).build();

            navView.getMenu().findItem(R.id.navigation_calendar).setVisible(false);
            navView.getMenu().findItem(R.id.navigation_dashboard).setVisible(false);
        }

        // Configurar visibilidade geral do BottomNavigationView
        if ("regular".equals(userType)) {
            navView.setVisibility(View.GONE);
        } else {
            navView.setVisibility(View.VISIBLE);
        }

        // Configurar NavController
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
    }
}
