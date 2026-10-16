package lk.akash.jupclothing.fragment;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import lk.akash.jupclothing.R;
import lk.akash.jupclothing.adapter.CartAdapter;
import lk.akash.jupclothing.databinding.FragmentCartragmentBinding;
import lk.akash.jupclothing.model.CartItem;
import lk.akash.jupclothing.model.Product;
import lk.akash.jupclothing.util.FirestoreMapper;

public class Cartragment extends Fragment {

    private FragmentCartragmentBinding binding;
    private List<CartItem> cartItems;
    private MaterialButton btnProceed;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCartragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        requireActivity().getSupportFragmentManager().popBackStack();
                    }
                }
        );

        btnProceed = binding.cartBtnProceed;

        btnProceed.setOnClickListener(v -> {
            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            if (firebaseAuth.getCurrentUser() == null) {
                Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show();
                return;
            }

            String uid = firebaseAuth.getCurrentUser().getUid();
            String totalPrice = binding.cartTextTotal.getText().toString().trim();

            db.collection("users")
                    .document(uid)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        String userName = "";
                        String userAddress = "";
                        List<Product> products = new ArrayList<>();
                        String productId = "";


                        if (documentSnapshot.exists()) {
                            userName = documentSnapshot.getString("name");
                            userAddress = documentSnapshot.getString("address");
                            if (userName == null || userAddress == null) {
                                userName = "";
                                userAddress = "";
                                productId = "";

                            }
                        }
                        Bundle bundle = new Bundle();
                        bundle.putString("total_price", totalPrice);
                        bundle.putString("user_name", userName);
                        bundle.putString("user_address", userAddress);

                        ArrayList<String> productIds = new ArrayList<>();
                        for (CartItem item : cartItems) {
                            productIds.add(item.getProductId());
                        }

                        bundle.putStringArrayList("product_ids", productIds); // me line eka danne one

                        PaymentFragment paymentFragment = new PaymentFragment();
                        paymentFragment.setArguments(bundle);

                        requireActivity()
                                .getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.fragment_container, paymentFragment)
                                .addToBackStack(null)
                                .commit();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(requireContext(), "Failed to load user data", Toast.LENGTH_SHORT).show()
                    );
        });

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (firebaseAuth.getCurrentUser() != null) {
            String uid = firebaseAuth.getCurrentUser().getUid();
            db.collection("users").document(uid).collection("cart")
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot qds) {

                            if (!qds.isEmpty()) {
                                cartItems = qds.toObjects(CartItem.class);

                                LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
                                binding.cartCartItem.setLayoutManager(layoutManager);

                                CartAdapter adapter = new CartAdapter(cartItems);

                                adapter.setOnQuantityChanegedListner(() -> updateTotal());

                                adapter.setOnRemoveListner(position -> {
                                    if (firebaseAuth.getCurrentUser() == null) return;

                                    String currentUid = firebaseAuth.getCurrentUser().getUid();
                                    CartItem removedItem = cartItems.get(position);

                                    db.collection("users")
                                            .document(currentUid)
                                            .collection("cart")
                                            .document(removedItem.getProductId())
                                            .delete()
                                            .addOnSuccessListener(unused -> {
                                                cartItems.remove(position);
                                                adapter.notifyItemRemoved(position);
                                                updateTotal();

                                                LayoutInflater inflater = getLayoutInflater();
                                                View layout = inflater.inflate(R.layout.custom_toast, null);
                                                TextView text = layout.findViewById(R.id.toastText);
                                                text.setText("Product removed from Cart");
                                                Toast toast = new Toast(requireContext());
                                                toast.setDuration(Toast.LENGTH_SHORT);
                                                toast.setView(layout);
                                                toast.show();
                                            })
                                            .addOnFailureListener(e ->
                                                    Toast.makeText(getContext(), "Failed to remove product: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                                            );
                                });

                                binding.cartCartItem.setAdapter(adapter);
                                updateTotal();
                            } else {
                                cartItems = new ArrayList<>();
                                binding.cartTextTotal.setText(String.format(Locale.US, "LKR %.2f", 0.00));
                            }
                        }
                    });
        }
    }

    private void updateTotal() {
        if (cartItems == null || cartItems.isEmpty()) {
            binding.cartTextTotal.setText(String.format(Locale.US, "LKR %.2f", 0.00));
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        List<String> productIds = new ArrayList<>();
        cartItems.forEach(cartItem -> productIds.add(cartItem.getProductId()));

        db.collection("products")
                .get()
                .addOnSuccessListener(qds -> {
                    Map<String, Product> productMap = new HashMap<>();
                    qds.getDocuments().forEach(documentSnapshot -> {
                        Product product = FirestoreMapper.toProduct(documentSnapshot);
                        if (product.getProductId() != null) productMap.put(product.getProductId(), product);
                    });
                    double total = 0;
                    for (CartItem cartItem : cartItems) {
                        Product product = productMap.get(cartItem.getProductId());
                        if (product != null) total += product.getPrice() * cartItem.getQuantity();
                    }
                    binding.cartTextTotal.setText(String.format(Locale.US, "LKR %.2f", total));
                })
                .addOnFailureListener(e -> Toast.makeText(requireContext(), "Unable to calculate cart. Check Firestore Rules.", Toast.LENGTH_LONG).show());
    }
}