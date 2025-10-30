package com.aula.mobile_hivemind;

import android.os.Bundle;
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

    private boolean isFabMenuOpen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        com.aula.mobile_hivemind.databinding.ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);

        // Navegação
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_calendar, R.id.navigation_dashboard)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        // FAM setup
        FloatingActionButton fabMain = findViewById(R.id.fab_main);
        FloatingActionButton fabOp1 = findViewById(R.id.fab_op1);
        FloatingActionButton fabOp2 = findViewById(R.id.fab_op2);

        fabMain.setOnClickListener(v -> {
            if (isFabMenuOpen) {
                fabOp1.hide();
                fabOp2.hide();
            } else {
                fabOp1.show();
                fabOp2.show();
            }
            isFabMenuOpen = !isFabMenuOpen;
        });

        fabOp1.setOnClickListener(v -> {
            Toast.makeText(this, "Opção 1 clicada", Toast.LENGTH_SHORT).show();
            closeFabMenu(fabOp1, fabOp2);
        });

        fabOp2.setOnClickListener(v -> {
            Toast.makeText(this, "Opção 2 clicada", Toast.LENGTH_SHORT).show();
            closeFabMenu(fabOp1, fabOp2);
        });
    }

    private void closeFabMenu(FloatingActionButton... fabs) {
        for (FloatingActionButton fab : fabs) {
            fab.hide();
        }
        isFabMenuOpen = false;
    }

    @Override
    public void onBackPressed() {
        if (isFabMenuOpen) {
            closeFabMenu(findViewById(R.id.fab_op1), findViewById(R.id.fab_op2));
        } else {
            super.onBackPressed();
        }
    }
}
