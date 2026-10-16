//package lk.akash.jupclothing.activity;
//
//import android.content.Intent;
//import android.graphics.Color;
//import android.os.Build;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.Looper;
//import android.view.View;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.TextView;
//
//import androidx.activity.EdgeToEdge;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.graphics.Insets;
//import androidx.core.view.ViewCompat;
//import androidx.core.view.WindowInsetsCompat;
//import androidx.drawerlayout.widget.DrawerLayout;
//import androidx.viewpager2.widget.ViewPager2;
//
//import com.google.android.material.button.MaterialButton;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import lk.akash.jupclothing.R;
//import lk.akash.jupclothing.adapter.OnboardingItem;
//import lk.akash.jupclothing.model.OnBoardingItem;
//
//public class Description1Activity extends AppCompatActivity {
//
//
//    private LinearLayout dotsLayout;
//    private ViewPager2 veiwPager;
//    private MaterialButton btnNext;
//
//    private final List<OnBoardingItem> pages = new ArrayList<>();
//
//    private final Handler handler = new Handler(Looper.getMainLooper());
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
//        setContentView(R.layout.activity_description1);
//
//        getWindow().setStatusBarColor(Color.WHITE);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
//            getWindow().setStatusBarContrastEnforced(false);
//        }
//
//        TextView btnSkip = findViewById(R.id.onbarding_main_view_btn);
//
//        btnSkip.setOnClickListener(view -> {
//            if (veiwPager != null && veiwPager.getAdapter()!=null){
//                int last = veiwPager.getAdapter().getItemCount()-1;
//                if (last>=0)veiwPager.setCurrentItem(last,true);
//            }
//        });
//
//        veiwPager = findViewById(R.id.onboarding_main_view);
//        btnNext = findViewById(R.id.onbarding_main_view_btn);
//
//        pages.add(new OnBoardingItem(
//           R.drawable.onb1,
//           "Explore Fasion",
//            "Explore the 2026's hottest fasion,Shoes, accessories and more" ,
//            false
//        ));
//
//        pages.add(new OnBoardingItem(
//                R.drawable.onb2,
//                "Select What You Love",
//                "Exclusive curated selection of top brands in the palm of your hand",
//                false
//
//        ));
//
//        pages.add(new OnBoardingItem(
//                R.drawable.onb3,
//                "Be the Real You",
//                "It brings you the latest trends and products from all over the  world",
//                false
//        ));
//
//        veiwPager.setAdapter(new OnboardingItem(pages));
//
//        setupDots(pages.size());
//        updateDotsAndButton(0);
//
//        veiwPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
//            @Override
//            public void onPageSelected(int position) {
//                updateDotsAndButton(position);
//                updateSkip(position);
//            }
//        });
//
//        btnNext.setOnClickListener(view -> {
//            int pos = veiwPager.getCurrentItem();
//            if (pos<pages.size()-1){
//                veiwPager.setCurrentItem(pos+1,true);
//            }else {
//                startActivity(new Intent(Description1Activity.this, SignInActivity.class));
//                finish();
//            }
//        });
//
//
//
//
//
//    }
//    private void setupDots(int count) {
//        dotsLayout.removeAllViews();
//        for (int i = 0; i < count; i++) {
//            ImageView dot = new ImageView(this);
//            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
//                    LinearLayout.LayoutParams.WRAP_CONTENT,
//                    LinearLayout.LayoutParams.WRAP_CONTENT
//            );
//            lp.setMargins(8, 0, 8, 0);
//            dot.setLayoutParams(lp);
//            dot.setImageResource(R.drawable.dot_inactive);
//            dotsLayout.addView(dot);
//        }
//    }
//
//    private void updateDotsAndButton(int position) {
//        int count = dotsLayout.getChildCount();
//        for (int i = 0; i < count; i++) {
//            ImageView dot = (ImageView) dotsLayout.getChildAt(i);
//            dot.setImageResource(i == position ? R.drawable.dot_active : R.drawable.dot_inactive);
//        }
//
//        if (position == pages.size() - 1) {
//            btnNext.setText("Get Started");
//        } else {
//            btnNext.setText("Next");
//        }
//    }
//
//    private void goToLogin() {
//        startActivity(new Intent(Description1Activity.this, SignInActivity.class));
//        finish();
//    }
//
//    private void updateSkip(int position) {
//        TextView btnSkip = findViewById(R.id.onbarding_main_view_btn);
//        btnSkip.setVisibility(position == pages.size() - 1 ? View.GONE : View.VISIBLE);
//    }
//}