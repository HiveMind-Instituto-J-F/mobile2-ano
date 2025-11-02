package com.aula.mobile_hivemind;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.aula.mobile_hivemind.auth.LoginActivity;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private ExtendedFloatingActionButton fabMain;
    private ActivityMainBinding binding;
    private NavController navController;
    private String userType;
    private boolean isFabOpen = false;

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
                case "regular": // OPERADOR
                    navController.navigate(R.id.addParadaFragment);
                    break;

                case "MOP": // ENGENHEIRO
                    navController.navigate(R.id.addParadaFragment);
                    break;

                default:
                    Toast.makeText(this, "Ação não disponível para seu perfil", Toast.LENGTH_SHORT).show();
                    break;
            }
        });
    }

    private boolean userHasFabPermission() {
        return "regular".equals(userType) || "MOP".equals(userType);
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
            // OPERADOR - apenas home
            appBarConfiguration = new AppBarConfiguration.Builder(
                    R.id.navigation_home
            ).build();

            configureMenuVisibility(navView, true, false, false);

        } else if ("MOP".equals(userType)) {
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
        if ("regular".equals(userType) || "MOP".equals(userType)) {
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