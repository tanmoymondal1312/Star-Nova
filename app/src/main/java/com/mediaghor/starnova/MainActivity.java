package com.mediaghor.starnova;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.mediaghor.starnova.Fragments.AiFragment;
import com.mediaghor.starnova.Fragments.DailyTaskFragment;
import com.mediaghor.starnova.Fragments.ProfileFragment;
import com.mediaghor.starnova.Models.BottomNavViewModel;
import com.nafis.bottomnavigation.NafisBottomNavigation;

public class MainActivity extends AppCompatActivity {

    NafisBottomNavigation nafisBottomNavigation;
    private BottomNavViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1️⃣ Enable Edge-to-Edge manually
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Window window = getWindow();
            window.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            );

            // Transparent status bar
            window.setStatusBarColor(Color.TRANSPARENT);
            // Navigation bar color same as bottom navbar
            window.setNavigationBarColor(getColor(R.color.on_layout_bg));
        }

        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        bottomNavigation();
    }

    private void bottomNavigation() {
        viewModel = new ViewModelProvider(this).get(BottomNavViewModel.class);
        nafisBottomNavigation = findViewById(R.id.bottomNavigation);

        // Add items
        nafisBottomNavigation.add(new NafisBottomNavigation.Model(1, R.drawable.ic_icon_daily_task));
        nafisBottomNavigation.add(new NafisBottomNavigation.Model(2, R.drawable.ic_ai_btn));
        nafisBottomNavigation.add(new NafisBottomNavigation.Model(3, R.drawable.ic_profile));

        // Handle item click
        nafisBottomNavigation.setOnShowListener(model -> {
            Fragment selectedFragment = null;

            switch (model.getId()) {
                case 1:
                    selectedFragment = new DailyTaskFragment();
                    break;
                case 2:
                    selectedFragment = new AiFragment();
                    break;
                case 3:
                    selectedFragment = new ProfileFragment();
                    break;
            }

            if (selectedFragment != null) {
                setFragment(selectedFragment);
            }
            return null;
        });

        // Show first item as selected
        nafisBottomNavigation.show(2, true);

        nafisBottomNavigation.post(() -> {
            int navHeight = nafisBottomNavigation.getHeight();
            // Store the height in the ViewModel
            viewModel.setNavHeight(navHeight);
            Log.d("NavHeight", "Bottom Nav Height Measured: " + navHeight);
        });
    }

    // Dynamic fragment replace
    private void setFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.frameLayoutMain, fragment);
        transaction.commit();
    }

    // Method to change nav selection from other fragments/places
    public void selectNavItem(int id) {
        if (nafisBottomNavigation != null) {
            nafisBottomNavigation.show(id, true);
        }
    }
}
