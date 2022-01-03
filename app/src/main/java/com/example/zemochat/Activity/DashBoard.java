package com.example.zemochat.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.widget.Toast;

import com.example.zemochat.Fragment.ChatFragment;
import com.example.zemochat.Fragment.ContactFragment;
import com.example.zemochat.Fragment.ProfileFragment;
import com.example.zemochat.R;
import com.example.zemochat.Utils.Util;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;

public class DashBoard extends AppCompatActivity {

    private ChipNavigationBar navigationBar;
    private Fragment fragment = null;
    private Util util;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dash_board);
        util = new Util();
        navigationBar = findViewById(R.id.navigationChip);

        if (savedInstanceState == null) {
            navigationBar.setItemSelected(R.id.chat, true);
            getSupportFragmentManager().beginTransaction().replace(R.id.dashboardContainer, new ChatFragment()).commit();
        }

        navigationBar.setOnItemSelectedListener(new ChipNavigationBar.OnItemSelectedListener() {
            @Override
            public void onItemSelected(int i) {
                switch (i) {

                    case R.id.chat:
                        fragment = new ChatFragment();
                        break;
                    case R.id.contacts:
                        fragment = new ContactFragment();
                        break;
                    case R.id.profile:
                        fragment = new ProfileFragment();
                        break;
                }

                if (fragment != null)
                    getSupportFragmentManager().beginTransaction().replace(R.id.dashboardContainer, fragment).commit();
            }
        });


    }

    @Override
    protected void onResume() {
        util.updateOnlineStatus("online");
        super.onResume();
    }

    @Override
    protected void onPause() {
        util.updateOnlineStatus(String.valueOf(System.currentTimeMillis()));
        super.onPause();
    }
}
