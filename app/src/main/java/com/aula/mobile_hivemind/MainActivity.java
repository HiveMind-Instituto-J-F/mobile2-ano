package com.aula.mobile_hivemind;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.aula.mobile_hivemind.auth.LoginActivity;
import com.aula.mobile_hivemind.utils.CustomToast;
import com.aula.mobile_hivemind.utils.SharedPreferencesManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.aula.mobile_hivemind.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ExtendedFloatingActionButton fabMain;
    private ActivityMainBinding binding;
    private NavController navController;
    private int userType;
    private boolean isFabOpen = false;

    // Constantes para tipos de usuário
    private static final int TIPO_USUARIO_COMUM = 1;
    private static final int TIPO_USUARIO_MANUTENCAO = 2;
    private static final int TIPO_USUARIO_ADMIN = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Obter tipo de usuário do SharedPreferencesManager (como int)
        userType = SharedPreferencesManager.getInstance(this).getUserType();

        // Se não encontrar no SharedPreferences, tentar do Intent
        if (userType == -1) {
            userType = getIntent().getIntExtra("USER_TYPE", TIPO_USUARIO_COMUM);
        }

        Log.d("MainActivity", "Tipo de usuário: " + userType);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        fabMain = findViewById(R.id.fab_main);

        navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);

        setupNavigationVisibilityController();

        setupNavigationByUserType();
        setupFabAction();
    }

    private void setupNavigationVisibilityController() {
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            int[] hideBottomNavDestinations = {
                    R.id.navigation_logout,
                    R.id.notificationHistoryFragment,
                    R.id.navigation_historico_diario,
                    R.id.addParadaFragment,
                    R.id.confirmationFragment
            };

            boolean shouldHide = false;
            for (int destinationId : hideBottomNavDestinations) {
                if (destination.getId() == destinationId) {
                    shouldHide = true;
                    break;
                }
            }

            if (shouldHide) {
                hideBottomNavigation();
            } else {
                showBottomNavigation();
            }

            controlFabVisibility(destination.getId());
        });
    }

    private void controlFabVisibility(int destinationId) {
        int[] showFabDestinations = {
                R.id.navigation_home,
                R.id.navigation_homerh
        };

        boolean shouldShowFab = false;
        for (int fabDestinationId : showFabDestinations) {
            if (destinationId == fabDestinationId) {
                shouldShowFab = true;
                break;
            }
        }

        if (shouldShowFab && userHasFabPermission()) {
            showFab();
        } else {
            hideFab();
        }
    }

    public void hideBottomNavigation() {
        BottomNavigationView navView = binding.navView;
        if (navView != null) {
            navView.setVisibility(View.GONE);
        }
        hideFab();
    }

    public void showBottomNavigation() {
        BottomNavigationView navView = binding.navView;
        if (navView != null) {
            navView.setVisibility(View.VISIBLE);
        }
    }

    private void showFab() {
        if (fabMain != null && userHasFabPermission()) {
            fabMain.setVisibility(View.VISIBLE);
            fabMain.setAlpha(1f);
            fabMain.setScaleX(1f);
            fabMain.setScaleY(1f);
        }
    }

    private void hideFab() {
        if (fabMain != null) {
            fabMain.setVisibility(View.GONE);
        }
    }

    // -------------------- FAB --------------------
    public void setFabVisibility(boolean visible) {
        if (fabMain != null) {
            if (!userHasFabPermission()) {
                fabMain.setVisibility(View.GONE);
                return;
            }

            if (visible) {
                showFab();
            } else {
                hideFab();
            }
        }
    }

    public void setBottomNavigationVisibility(boolean visible) {
        BottomNavigationView navView = binding.navView;
        if (navView != null) {
            navView.setVisibility(visible ? View.VISIBLE : View.GONE);
        }

        if (visible) {
            if (navController != null) {
                int currentDestination = navController.getCurrentDestination().getId();
                controlFabVisibility(currentDestination);
            }
        } else {
            hideFab();
        }
    }

    private void setupFabAction() {
        if (!userHasFabPermission()) {
            fabMain.setVisibility(View.GONE);
            return;
        }

        fabMain.setVisibility(View.VISIBLE);

        fabMain.setOnClickListener(v -> {
            switch (userType) {
                case TIPO_USUARIO_COMUM: // OPERADOR
                    navController.navigate(R.id.addParadaFragment);
                    break;
                case TIPO_USUARIO_MANUTENCAO: // ENGENHEIRO
                    navController.navigate(R.id.maintenanceFragment);
                    break;

                default:
                    CustomToast.showWarning(this, "Ação não disponível para seu perfil");
                    break;
            }
        });
    }

    private boolean userHasFabPermission() {
        return userType == TIPO_USUARIO_COMUM || userType == TIPO_USUARIO_MANUTENCAO;
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
    public int getUserType() { // Mudar para retornar int
        return userType;
    }

    // Método auxiliar para obter como string (se necessário em outros lugares)
    public String getUserTypeAsString() {
        switch (userType) {
            case TIPO_USUARIO_COMUM:
                return "regular";
            case TIPO_USUARIO_MANUTENCAO:
                return "man";
            case TIPO_USUARIO_ADMIN:
                return "RH";
            default:
                return "regular";
        }
    }

    private void setupNavigationByUserType() {
        BottomNavigationView navView = binding.navView;
        AppBarConfiguration appBarConfiguration;

        if (userType == TIPO_USUARIO_COMUM) {
            // OPERADOR - apenas home
            appBarConfiguration = new AppBarConfiguration.Builder(
                    R.id.navigation_home
            ).build();

            configureMenuVisibility(navView, true, false, false);

        } else if (userType == TIPO_USUARIO_MANUTENCAO) {
            // ENGENHEIRO - home, dashboard, calendar
            appBarConfiguration = new AppBarConfiguration.Builder(
                    R.id.navigation_home,
                    R.id.navigation_dashboard,
                    R.id.navigation_calendar
            ).build();

            configureMenuVisibility(navView, true, true, true);

        } else {
            // PADRÃO (fallback) - apenas home
            appBarConfiguration = new AppBarConfiguration.Builder(
                    R.id.navigation_home
            ).build();

            configureMenuVisibility(navView, true, false, false);
        }

        NavigationUI.setupWithNavController(navView, navController);

        // Navega para a tela inicial baseada no tipo de usuário
        if (userType == TIPO_USUARIO_COMUM || userType == TIPO_USUARIO_MANUTENCAO) {
            navController.navigate(R.id.navigation_home);
        }
    }

    private void configureMenuVisibility(BottomNavigationView navView, boolean home, boolean dashboard, boolean calendar) {
        Menu menu = navView.getMenu();

        // Verificação segura para evitar NullPointerException
        MenuItem homeItem = menu.findItem(R.id.navigation_home);
        MenuItem dashboardItem = menu.findItem(R.id.navigation_dashboard);
        MenuItem calendarItem = menu.findItem(R.id.navigation_calendar);

        if (homeItem != null) {
            homeItem.setVisible(home);
        }

        if (dashboardItem != null) {
            dashboardItem.setVisible(dashboard);
        }

        if (calendarItem != null) {
            calendarItem.setVisible(calendar);
        }
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}