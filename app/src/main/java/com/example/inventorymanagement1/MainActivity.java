package com.example.inventorymanagement1;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private Button login_button;
    private Button register_button;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        auth = FirebaseAuth.getInstance();

        FirebaseUser user = auth.getCurrentUser();

        login_button = findViewById(R.id.Login_button);
        register_button = findViewById(R.id.Register_button);

        if(user != null){
            finish();
            startActivity(new Intent(this, HomeActivity.class));
        }
        else {
            login_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    login();
                }
            });

            register_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    register();
                }
            });

        }
    }

    public void login ()
    {
        startActivity(new Intent(this,LoginActivity.class));
    }

    public void register ()
    {
        startActivity(new Intent(this,RegisterActivity.class));
    }
}