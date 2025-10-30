package com.aula.mobile_hivemind.ui.perfil;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.aula.mobile_hivemind.R;
import com.aula.mobile_hivemind.auth.login.LoginActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class PerfilFragment extends Fragment {


    public PerfilFragment() {
    }

    public static PerfilFragment newInstance() {
        return new PerfilFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_perfil, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Oculta a BottomNavigationView se estiver presente na MainActivity
        View bottomNav = requireActivity().findViewById(R.id.nav_view);
        FloatingActionButton fab = requireActivity().findViewById(R.id.fab_main);
        Button btnSair = view.findViewById(R.id.btnSair);
        if (bottomNav != null && fab != null) {
            bottomNav.setVisibility(View.GONE);
            fab.setVisibility(View.GONE);
        }

        btnSair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
                requireActivity().finish();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Restaura a visibilidade da BottomNavigationView ao sair do fragmento
        View bottomNav = requireActivity().findViewById(R.id.nav_view);
        FloatingActionButton fab = requireActivity().findViewById(R.id.fab_main);
        if (bottomNav != null && fab != null) {
            bottomNav.setVisibility(View.VISIBLE);
            fab.setVisibility(View.VISIBLE);
        }
    }
}
