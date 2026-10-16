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
import lk.akash.jupclothing.adapter.CategoryAdapter;
import lk.akash.jupclothing.databinding.FragmentCategoryBinding;
import lk.akash.jupclothing.model.Category;
import lk.akash.jupclothing.util.FirestoreMapper;

public class CategoryFragment extends Fragment {
    private FragmentCategoryBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCategoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() { requireActivity().getSupportFragmentManager().popBackStack(); }
        });

        binding.recycleVierwCategory.setLayoutManager(new GridLayoutManager(requireContext(), 2));

        FirebaseFirestore.getInstance().collection("categories").get()
                .addOnSuccessListener(snapshot -> {
                    List<Category> categories = new ArrayList<>();
                    snapshot.getDocuments().forEach(doc -> categories.add(FirestoreMapper.toCategory(doc)));
                    binding.recycleVierwCategory.setAdapter(new CategoryAdapter(categories, category -> {
                        if (category.getCategory() == null || category.getCategory().trim().isEmpty()) return;
                        Bundle bundle = new Bundle();
                        bundle.putString("categoryId", category.getDocumentId());
                        bundle.putString("categoryValue", category.getCategory());
                        bundle.putString("categoryName", category.getName());
                        ListingFragment fragment = new ListingFragment();
                        fragment.setArguments(bundle);
                        getParentFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, fragment)
                                .addToBackStack(null).commit();
                    }));
                })
                .addOnFailureListener(e -> Toast.makeText(requireContext(),
                        "Unable to load categories. Check Firestore Rules.", Toast.LENGTH_LONG).show());
    }

    @Override public void onDestroyView() { super.onDestroyView(); binding = null; }
}
