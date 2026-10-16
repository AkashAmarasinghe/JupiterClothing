package lk.akash.jupclothing.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import lk.akash.jupclothing.R;
import lk.akash.jupclothing.adapter.ListingAdapter;
import lk.akash.jupclothing.databinding.FragmentListingBinding;
import lk.akash.jupclothing.model.Product;
import lk.akash.jupclothing.util.FirestoreMapper;

public class ListingFragment extends Fragment {
    private FragmentListingBinding binding;
    private String categoryId;
    private String categoryValue;
    private String categoryName;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            categoryId = getArguments().getString("categoryId");
            categoryValue = getArguments().getString("categoryValue");
            categoryName = getArguments().getString("categoryName");
        }
    }

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentListingBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() { requireActivity().getSupportFragmentManager().popBackStack(); }
        });
        binding.recyclerViewListing.setLayoutManager(new GridLayoutManager(requireContext(), 2));

        FirebaseFirestore.getInstance().collection("products").get()
                .addOnSuccessListener(snapshot -> {
                    if (binding == null) return;
                    List<Product> products = new ArrayList<>();
                    snapshot.getDocuments().forEach(doc -> {
                        if (isBlank(categoryId) && isBlank(categoryValue) && isBlank(categoryName)) {
                            products.add(FirestoreMapper.toProduct(doc));
                        } else if (FirestoreMapper.matchesCategory(doc, categoryId, categoryValue, categoryName)) {
                            products.add(FirestoreMapper.toProduct(doc));
                        }
                    });
                    binding.recyclerViewListing.setAdapter(new ListingAdapter(products, product -> {
                        if (product == null || isBlank(product.getProductId())) return;
                        Bundle bundle = new Bundle();
                        bundle.putString("productId", product.getProductId());
                        ProductDetailsFragment fragment = new ProductDetailsFragment();
                        fragment.setArguments(bundle);
                        getParentFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, fragment)
                                .addToBackStack(null).commit();
                    }));
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) Toast.makeText(requireContext(), "Unable to load products: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private boolean isBlank(String value) { return value == null || value.trim().isEmpty(); }

    @Override public void onDestroyView() { super.onDestroyView(); binding = null; }
}
