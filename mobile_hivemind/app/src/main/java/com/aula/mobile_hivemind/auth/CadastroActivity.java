package com.aula.mobile_hivemind.auth;

import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment; // Import for NavHostFragment
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.aula.mobile_hivemind.R;
import com.aula.mobile_hivemind.databinding.ActivityCadastroBinding;

public class CadastroActivity extends AppCompatActivity {

    private ActivityCadastroBinding binding;
    private NavController navController; // Declare navController at class level
    private AppBarConfiguration appBarConfiguration; // Declare appBarConfiguration at class level

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        binding = ActivityCadastroBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Get the NavHostFragment directly from the FragmentContainerView
        // This is a more robust way to ensure the NavController is ready
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();

            // Define your AppBarConfiguration with the top-level destinations
            // Initialize appBarConfiguration here
            appBarConfiguration = new AppBarConfiguration.Builder(
                    R.id.cadastroBasicoFragment,
                    R.id.cadastroEnderecoFragment,
                    R.id.cadastroEstruturaFragment,
                    R.id.cadastroPessoalFragment,
                    R.id.criacaoContaFragment
            ).build();

            // Setup the ActionBar with the NavController
            NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        } else {
            // Handle the case where navHostFragment is null (e.g., log an error)
            // This should ideally not happen if your layout is correct
            System.err.println("Error: NavHostFragment not found with ID R.id.nav_host_fragment");
        }
    }

    // Override onSupportNavigateUp to allow the Up button to navigate back
    @Override
    public boolean onSupportNavigateUp() {
        if (navController != null && appBarConfiguration != null) {
            // Correctly call navigateUp with navController and appBarConfiguration
            return NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp();
        }
        return super.onSupportNavigateUp();
    }
}
