package lk.akash.jupclothing.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import lk.akash.jupclothing.R;
import lk.akash.jupclothing.databinding.ActivityMainBinding;
import lk.akash.jupclothing.databinding.SideNavHeaderBinding;
import lk.akash.jupclothing.fragment.Cartragment;
import lk.akash.jupclothing.fragment.CategoryFragment;
import lk.akash.jupclothing.fragment.HomeFragment;
import lk.akash.jupclothing.fragment.NotificationFragment;

import lk.akash.jupclothing.fragment.ProfileFragment;
import lk.akash.jupclothing.fragment.WhislistFragment;
import lk.akash.jupclothing.model.User;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, NavigationBarView.OnItemSelectedListener {

    private ActivityMainBinding binding;
    private SideNavHeaderBinding sideNavHeaderBinding;

    private DrawerLayout drawerLayout;
    private MaterialToolbar toolbar;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private NavigationView navigationView;
    private BottomNavigationView bottomNavigationView;
    private ImageView mainCall;
    private ImageView mainNotification;
    private View notificationBadgeDot;
    private ListenerRegistration notificationBadgeListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mainCall = findViewById(R.id.main_call);
        mainNotification = findViewById(R.id.main_notification);
        notificationBadgeDot = findViewById(R.id.notification_badge_dot);

        mainCall.setOnClickListener(v -> {
            String phoneNumber = "tel:0771234567"; // mekata oya number eka danna
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse(phoneNumber));
            startActivity(intent);
        });
        // notch / status bar eka athule content yanna epa
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        View headerView = binding.sideNavigationView.getHeaderView(0);
        sideNavHeaderBinding = SideNavHeaderBinding.bind(headerView);

        drawerLayout = binding.drawerLayout;
        toolbar = binding.toolBar;
        navigationView = binding.sideNavigationView;
        bottomNavigationView = binding.bottomNavigationView;

        // toolbar eka status bar / notch eke pallēhata genna
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (view, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());

            toolbar.setPadding(
                    toolbar.getPaddingLeft(),
                    insets.top,
                    toolbar.getPaddingRight(),
                    toolbar.getPaddingBottom()
            );

            bottomNavigationView.setPadding(
                    bottomNavigationView.getPaddingLeft(),
                    bottomNavigationView.getPaddingTop(),
                    bottomNavigationView.getPaddingRight(),
                    insets.bottom
            );

            return windowInsets;
        });

        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.drawer_open,
                R.string.drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    getSupportFragmentManager().popBackStack();
                } else {
                    finish();
                }
            }
        });

        navigationView.setNavigationItemSelectedListener(this);
        bottomNavigationView.setOnItemSelectedListener(this);

        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
            navigationView.setCheckedItem(R.id.sideNavHome);
            bottomNavigationView.getMenu().findItem(R.id.bottomNavHome).setChecked(true);
        }

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        mainNotification.setOnClickListener(v -> {
            if (firebaseAuth.getCurrentUser() == null) {
                Intent intent = new Intent(MainActivity.this, SignInActivity.class);
                startActivity(intent);
                return;
            }

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new NotificationFragment())
                    .addToBackStack("notifications")
                    .commit();
        });

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            startNotificationBadgeListener(currentUser);

            firebaseFirestore.collection("users").document(currentUser.getUid()).get()
                    .addOnSuccessListener(ds -> {
                        User user = ds.toObject(User.class);
                        if (user != null) {
                            sideNavHeaderBinding.headreUserName.setText(user.getName());
                            sideNavHeaderBinding.headerUserEmail.setText(user.getEmail());

                            Glide.with(MainActivity.this)
                                    .load(user.getProfilePictureUrl())
                                    .circleCrop()
                                    .into(sideNavHeaderBinding.headerProfilePic);
                        }
                    });

            navigationView.getMenu().findItem(R.id.sideNavLogin).setVisible(false);

            navigationView.getMenu().findItem(R.id.sideNavProfile).setVisible(true);
            navigationView.getMenu().findItem(R.id.sideNavOrders).setVisible(true);
            navigationView.getMenu().findItem(R.id.sideNavWhislist).setVisible(true);
            navigationView.getMenu().findItem(R.id.sideNavCart).setVisible(true);
            navigationView.getMenu().findItem(R.id.sideNavMessage).setVisible(true);
            navigationView.getMenu().findItem(R.id.sideNavLogout).setVisible(true);

            bottomNavigationView.getMenu().findItem(R.id.bottomNavCart).setVisible(true);
            bottomNavigationView.getMenu().findItem(R.id.bottomNavProfile).setVisible(true);
            bottomNavigationView.getMenu().findItem(R.id.bottomNavWhislist).setVisible(true);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

        int itemId = menuItem.getItemId();

        Menu navMenu = navigationView.getMenu();
        Menu bottomNavMenu = bottomNavigationView.getMenu();

        for (int i = 0; i < navMenu.size(); i++) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CUPCAKE) {
                navMenu.getItem(i).setChecked(false);
            }
        }

        for (int i = 0; i < bottomNavMenu.size(); i++) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CUPCAKE) {
                bottomNavMenu.getItem(i).setChecked(false);
            }
        }

        if (itemId == R.id.sideNavHome || itemId == R.id.bottomNavHome) {
            loadFragment(new HomeFragment());
            navigationView.setCheckedItem(R.id.sideNavHome);
            bottomNavigationView.getMenu().findItem(R.id.bottomNavHome).setChecked(true);

        } else if (itemId == R.id.sideNavProfile || itemId == R.id.bottomNavProfile) {

            if (firebaseAuth.getCurrentUser() == null) {
                Intent intent = new Intent(MainActivity.this, SignInActivity.class);
                startActivity(intent);
                finish();
                return true;
            }

            loadFragment(new ProfileFragment());
            navigationView.setCheckedItem(R.id.sideNavProfile);
            bottomNavigationView.getMenu().findItem(R.id.bottomNavProfile).setChecked(true);



        } else if (itemId == R.id.sideNavWhislist || itemId == R.id.bottomNavWhislist) {
            if (firebaseAuth.getCurrentUser() == null) {
                Intent intent = new Intent(MainActivity.this, SignInActivity.class);
                startActivity(intent);
                finish();
                return true;
            }

            loadFragment(new WhislistFragment());
            navigationView.setCheckedItem(R.id.sideNavWhislist);
            bottomNavigationView.getMenu().findItem(R.id.bottomNavWhislist).setChecked(true);

        } else if (itemId == R.id.sideNavCart || itemId == R.id.bottomNavCart) {
            if (firebaseAuth.getCurrentUser() == null) {
                Intent intent = new Intent(MainActivity.this, SignInActivity.class);
                startActivity(intent);
                finish();
                return true;
            }

            loadFragment(new Cartragment());
            navigationView.setCheckedItem(R.id.sideNavCart);
            bottomNavigationView.getMenu().findItem(R.id.bottomNavCart).setChecked(true);


        } else if (itemId == R.id.bottomNavCategory) {
            loadFragment(new CategoryFragment());
            bottomNavigationView.getMenu().findItem(R.id.bottomNavCategory).setChecked(true);

        } else if (itemId == R.id.sideNavLogin) {
            Intent intent = new Intent(MainActivity.this, SignInActivity.class);
            startActivity(intent);

        } else if (itemId == R.id.sideNavLogout) {
            if (notificationBadgeListener != null) {
                notificationBadgeListener.remove();
                notificationBadgeListener = null;
            }
            notificationBadgeDot.setVisibility(View.GONE);
            firebaseAuth.signOut();
            loadFragment(new HomeFragment());
            navigationView.getMenu().clear();
            navigationView.inflateMenu(R.menu.side_nav_menu);

            navigationView.removeHeaderView(sideNavHeaderBinding.getRoot());
            View newHeaderView = navigationView.inflateHeaderView(R.layout.side_nav_header);
            sideNavHeaderBinding = SideNavHeaderBinding.bind(newHeaderView);
        }

        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }

        return true;
    }

    private void startNotificationBadgeListener(FirebaseUser currentUser) {
        if (notificationBadgeListener != null) {
            notificationBadgeListener.remove();
        }

        notificationBadgeListener = firebaseFirestore
                .collection("users")
                .document(currentUser.getUid())
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null || snapshot == null || !snapshot.exists()) {
                        notificationBadgeDot.setVisibility(View.GONE);
                        return;
                    }

                    Boolean hasUnread = snapshot.getBoolean("hasUnreadNotifications");
                    notificationBadgeDot.setVisibility(Boolean.TRUE.equals(hasUnread) ? View.VISIBLE : View.GONE);
                });
    }

    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

    @Override
    protected void onDestroy() {
        if (notificationBadgeListener != null) {
            notificationBadgeListener.remove();
            notificationBadgeListener = null;
        }
        super.onDestroy();
    }
}