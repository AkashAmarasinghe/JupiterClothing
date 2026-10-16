package lk.akash.jupclothing.activity;

import android.content.Intent;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;

import lk.akash.jupclothing.R;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            getWindow().setDecorFitsSystemWindows(false);

            WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null){
                controller.hide(WindowInsets.Type.statusBars()| WindowInsets.Type.navigationBars());
            }
        }else {
            getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN
            );
        }
        setContentView(R.layout.activity_splash);

        ImageView imageView = findViewById(R.id.splashLogo);

        Glide.with(this)
                .asBitmap()
                .load(R.drawable.logo1)
                .override(500)
                .into(imageView);

        new Handler(Looper.getMainLooper())
                .postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        findViewById(R.id.spashProgressBar).setVisibility(View.VISIBLE);
                    }
                },1000);

        new Handler(Looper.getMainLooper())
                .postDelayed(new Runnable() {
                    @Override
                    public void run() {
                   findViewById(R.id.spashProgressBar).setVisibility(View.INVISIBLE);
                   Intent intent = new Intent(SplashActivity.this,SignInActivity.class);
                    startActivity(intent);
                    finish();
                    }
                },3000);


    }
}