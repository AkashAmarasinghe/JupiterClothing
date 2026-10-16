package lk.akash.jupclothing.fragment;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import lk.akash.jupclothing.R;
import lk.akash.jupclothing.adapter.ListingAdapter;
import lk.akash.jupclothing.adapter.homeCategoryAdapter;
import lk.akash.jupclothing.databinding.FragmentHomeBinding;
import lk.akash.jupclothing.model.Category;
import lk.akash.jupclothing.model.Product;
import lk.akash.jupclothing.util.FirestoreMapper;

public class  HomeFragment extends Fragment {
    private FragmentHomeBinding homeBinding;
    private homeCategoryAdapter homecategoryAdapter;
    private VideoView bgVideoView;
    private ListingAdapter listingAdapter;
    private final Handler autoScrollHandler = new Handler(Looper.getMainLooper());
    private Runnable autoScrollRunnable;
    private int currentPosition = 0;
    private List<Product> allProducts = new ArrayList<>();
    private final List<Product> filteredProducts = new ArrayList<>();
    private boolean searchSetup;

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        homeBinding = FragmentHomeBinding.inflate(inflater, container, false);
        bgVideoView = homeBinding.homeVideo;
        bgVideoView.setVideoURI(Uri.parse("android.resource://" + requireActivity().getPackageName() + "/" + R.raw.cc));
        bgVideoView.requestFocus();
        bgVideoView.setOnPreparedListener(mp -> mp.setLooping(true));
        bgVideoView.start();
        return homeBinding.getRoot();
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() { requireActivity().getSupportFragmentManager().popBackStack(); }
        });
        homeBinding.homeCategoryRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        homeBinding.homeProductRecyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("products").get().addOnSuccessListener(snapshot -> {
            allProducts = new ArrayList<>();
            snapshot.getDocuments().forEach(doc -> allProducts.add(FirestoreMapper.toProduct(doc)));
            filteredProducts.clear(); filteredProducts.addAll(allProducts);
            listingAdapter = new ListingAdapter(filteredProducts, product -> openProduct(product.getProductId()));
            homeBinding.homeProductRecyclerView.setAdapter(listingAdapter);
            setupSearch();
        }).addOnFailureListener(e -> Toast.makeText(requireContext(), "Unable to load products. Check Firestore Rules.", Toast.LENGTH_LONG).show());

        db.collection("categories").get().addOnSuccessListener(snapshot -> {
            List<Category> categories = new ArrayList<>();
            snapshot.getDocuments().forEach(doc -> categories.add(FirestoreMapper.toCategory(doc)));
            homecategoryAdapter = new homeCategoryAdapter(categories, category -> {
                Bundle bundle = new Bundle();
                bundle.putString("categoryId", category.getDocumentId());
                bundle.putString("categoryValue", category.getCategory());
                bundle.putString("categoryName", category.getName());
                ListingFragment fragment = new ListingFragment(); fragment.setArguments(bundle);
                getParentFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack(null).commit();
            });
            homeBinding.homeCategoryRecyclerView.setAdapter(homecategoryAdapter);
            if (!categories.isEmpty()) startAutoScroll(categories.size());
        }).addOnFailureListener(e -> Toast.makeText(requireContext(), "Unable to load categories. Check Firestore Rules.", Toast.LENGTH_LONG).show());
    }

    private void openProduct(String id) {
        if (id == null || id.trim().isEmpty()) return;
        Bundle bundle = new Bundle(); bundle.putString("productId", id);
        ProductDetailsFragment fragment = new ProductDetailsFragment(); fragment.setArguments(bundle);
        getParentFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack(null).commit();
    }

    private void setupSearch() {
        if (searchSetup) return; searchSetup = true;
        homeBinding.searchEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                String q = s.toString().trim();
                if (q.isEmpty()) { showHomeContent(); filteredProducts.clear(); filteredProducts.addAll(allProducts); }
                else { showSearchResultsOnly(); filterProducts(q); }
                if (listingAdapter != null) listingAdapter.notifyDataSetChanged();
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void filterProducts(String query) {
        filteredProducts.clear(); String q = query.toLowerCase();
        for (Product p : allProducts) {
            String title = p.getTitle() == null ? "" : p.getTitle().toLowerCase();
            String cat = p.getCategoryId() == null ? "" : p.getCategoryId().toLowerCase();
            if (title.contains(q) || cat.contains(q)) filteredProducts.add(p);
        }
    }

    private void smoothScrollWithSpeed(int position) {
        if (homeBinding == null || homeBinding.homeCategoryRecyclerView.getLayoutManager() == null) return;
        RecyclerView.SmoothScroller scroller = new LinearSmoothScroller(requireContext()) {
            @Override protected float calculateSpeedPerPixel(DisplayMetrics dm) { return 150f / dm.densityDpi; }
            @Override protected int getHorizontalSnapPreference() { return SNAP_TO_START; }
        };
        scroller.setTargetPosition(position);
        homeBinding.homeCategoryRecyclerView.getLayoutManager().startSmoothScroll(scroller);
    }

    private void startAutoScroll(int itemCount) {
        if (itemCount <= 0) return;
        if (autoScrollRunnable != null) autoScrollHandler.removeCallbacks(autoScrollRunnable);
        autoScrollRunnable = new Runnable() { @Override public void run() {
            if (homeBinding == null || homecategoryAdapter == null || homecategoryAdapter.getItemCount() == 0) return;
            currentPosition++; smoothScrollWithSpeed(currentPosition); autoScrollHandler.postDelayed(this, 2000);
        }};
        autoScrollHandler.postDelayed(autoScrollRunnable, 2000);
    }

    @Override public void onPause() { super.onPause(); if (autoScrollRunnable != null) autoScrollHandler.removeCallbacks(autoScrollRunnable); }
    @Override public void onResume() { super.onResume(); if (homecategoryAdapter != null && homecategoryAdapter.getRealItemCount() > 0) startAutoScroll(homecategoryAdapter.getRealItemCount()); }
    private void showSearchResultsOnly() { homeBinding.homeCategoryRecyclerView.setVisibility(View.GONE); homeBinding.cardVedioViewHome.setVisibility(View.GONE); homeBinding.homeProductRecyclerView.setVisibility(View.VISIBLE); }
    private void showHomeContent() { homeBinding.homeCategoryRecyclerView.setVisibility(View.VISIBLE); homeBinding.cardVedioViewHome.setVisibility(View.VISIBLE); homeBinding.homeProductRecyclerView.setVisibility(View.VISIBLE); }
    @Override public void onDestroyView() { super.onDestroyView(); if (autoScrollRunnable != null) autoScrollHandler.removeCallbacks(autoScrollRunnable); homeBinding = null; }
}
