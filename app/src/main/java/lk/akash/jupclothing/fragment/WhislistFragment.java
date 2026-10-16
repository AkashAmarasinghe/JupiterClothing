package lk.akash.jupclothing.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lk.akash.jupclothing.R;
import lk.akash.jupclothing.adapter.WhislistAdapter;
import lk.akash.jupclothing.databinding.FragmentWhislistBinding;
import lk.akash.jupclothing.model.Whishlist;

public class WhislistFragment extends Fragment {
    private FragmentWhislistBinding binding;
    private final List<Whishlist> items = new ArrayList<>();
    private WhislistAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentWhislistBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() { requireActivity().getSupportFragmentManager().popBackStack(); }
        });

        binding.wehishlistWhishlistItem.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new WhislistAdapter(items, (product, position) -> removeWishlistItem(product.getProductId(), position));
        binding.wehishlistWhishlistItem.setAdapter(adapter);
        loadWishlist();
    }

    private void loadWishlist() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) return;
        String uid = auth.getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Correct collection name used by the existing database screenshot: `wishlist`.
        db.collection("users").document(uid).collection("wishlist").get()
                .addOnSuccessListener(snapshot -> {
                    if (binding == null) return;
                    Map<String, Whishlist> merged = new LinkedHashMap<>();
                    snapshot.getDocuments().forEach(doc -> {
                        Whishlist w = doc.toObject(Whishlist.class);
                        if (w == null) w = new Whishlist();
                        if (w.getProductId() == null || w.getProductId().trim().isEmpty()) w.setProductId(doc.getId());
                        merged.put(w.getProductId(), w);
                    });
                    // Read legacy misspelled collection too, so old saved items are not lost.
                    db.collection("users").document(uid).collection("whishlist").get()
                            .addOnSuccessListener(oldSnapshot -> {
                                oldSnapshot.getDocuments().forEach(doc -> {
                                    Whishlist w = doc.toObject(Whishlist.class);
                                    if (w == null) w = new Whishlist();
                                    if (w.getProductId() == null || w.getProductId().trim().isEmpty()) w.setProductId(doc.getId());
                                    merged.put(w.getProductId(), w);
                                });
                                items.clear();
                                items.addAll(merged.values());
                                adapter.notifyDataSetChanged();
                            })
                            .addOnFailureListener(e -> {
                                items.clear();
                                items.addAll(merged.values());
                                adapter.notifyDataSetChanged();
                            });
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) Toast.makeText(requireContext(), "Unable to load wishlist: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void removeWishlistItem(String productId, int position) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null || productId == null) return;
        String uid = auth.getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(uid).collection("wishlist").document(productId).delete()
                .addOnSuccessListener(unused -> {
                    // Also clean up a legacy copy if it exists.
                    db.collection("users").document(uid).collection("whishlist").document(productId).delete();
                    if (adapter != null) adapter.removeItem(position);
                    showToast("Removed from wishlist");
                })
                .addOnFailureListener(e -> {
                    // It may only exist in the legacy collection.
                    db.collection("users").document(uid).collection("whishlist").document(productId).delete()
                            .addOnSuccessListener(unused -> {
                                if (adapter != null) adapter.removeItem(position);
                                showToast("Removed from wishlist");
                            })
                            .addOnFailureListener(err -> {
                                if (isAdded()) Toast.makeText(requireContext(), "Unable to remove wishlist item: " + err.getMessage(), Toast.LENGTH_LONG).show();
                            });
                });
    }

    private void showToast(String message) {
        if (!isAdded()) return;
        View layout = getLayoutInflater().inflate(R.layout.custom_toast, null);
        TextView text = layout.findViewById(R.id.toastText);
        text.setText(message);
        Toast toast = new Toast(requireContext());
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }

    @Override public void onDestroyView() { super.onDestroyView(); binding = null; }
}
