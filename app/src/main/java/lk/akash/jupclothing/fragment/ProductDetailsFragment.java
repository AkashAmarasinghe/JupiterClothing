package lk.akash.jupclothing.fragment;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lk.akash.jupclothing.R;
import lk.akash.jupclothing.activity.SignInActivity;
import lk.akash.jupclothing.adapter.ProductSliderAdapter;
import lk.akash.jupclothing.adapter.SectionAdapter;
import lk.akash.jupclothing.databinding.FragmentProductDetailsBinding;
import lk.akash.jupclothing.model.CartItem;
import lk.akash.jupclothing.model.Product;
import lk.akash.jupclothing.model.Whishlist;
import lk.akash.jupclothing.util.FirestoreMapper;


public class ProductDetailsFragment<container> extends Fragment {

    private FragmentProductDetailsBinding binding;

    private String productId;

    private int quantity = 1;

    private int avbqty;

    private Map<String, ChipGroup> attrChipGroups = new HashMap<>();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

            productId = getArguments().getString("productId");

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentProductDetailsBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().findViewById(R.id.bottom_navigation_view).setVisibility(View.GONE);

        getActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                requireActivity().getSupportFragmentManager().popBackStack();

            }
        });

        //loadProduct details

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("products").get()
                .addOnSuccessListener(qds -> {
                    Product product = null;
                    for (com.google.firebase.firestore.DocumentSnapshot doc : qds.getDocuments()) {
                        Product candidate = FirestoreMapper.toProduct(doc);
                        if (productId != null && productId.equals(candidate.getProductId())) { product = candidate; break; }
                    }
                    if (product == null || binding == null) return;
                    ProductSliderAdapter adapter = new ProductSliderAdapter(product.getImages());
                    binding.productImageSlider.setAdapter(adapter);
                    binding.dotsIndicator.attachTo(binding.productImageSlider);
                    enableImageDoubleTapZoom();
                    binding.productDetailsTitle.setText(product.getTitle());
                    binding.productDetailsPrice.setText("LKR "+product.getPrice());
                    binding.productDteailsAvbQty.setText(String.valueOf(product.getStockCount()));
                    avbqty = product.getStockCount();
                    if (product.getAttributes()!=null) product.getAttributes().forEach(attribute -> renderAttributes(attribute,binding.productDetailsAttributesContainerSize));
                })
                .addOnFailureListener(e -> Toast.makeText(requireContext(), "Unable to load product. Check Firestore Rules.", Toast.LENGTH_LONG).show());

        binding.productDetailsBtnMinus.setOnClickListener(v ->{

            if (quantity>1){
                quantity--;//implement karnawa
                binding.productDetailsQty.setText(String.valueOf(quantity));
            }
        });

        binding.productDetailsBtnPlus.setOnClickListener(v -> {

            if (quantity<avbqty){
                quantity++;
                binding.productDetailsQty.setText(String.valueOf(quantity));
            }
        });


        loadTopSellProducts();

        binding.productDetailsBtnAddToCart.setOnClickListener(v-> {

            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

            if (firebaseAuth.getCurrentUser()==null){
                Intent intent = new Intent(getActivity(), SignInActivity.class);
                startActivity(intent);

            }else{

                List<CartItem.Attribute> attributes = getFinalSelections();

                CartItem cartItem= new CartItem(productId,quantity,attributes);


                String uid = firebaseAuth.getCurrentUser().getUid();

                db.collection("users").document(uid).collection("cart")
                        .document(productId)
                        .set(cartItem)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                LayoutInflater inflater = getLayoutInflater();
                                View layout = inflater.inflate(R.layout.custom_toast, null);

                                TextView text = layout.findViewById(R.id.toastText);
                                text.setText("Item Added To the Cart");

                                Toast toast = new Toast(requireContext());
                                toast.setDuration(Toast.LENGTH_SHORT);
                                toast.setView(layout);
                                toast.show();
                            }
                        });
            }
        });

        binding.watchlist.setOnClickListener(v-> {

            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

            if (firebaseAuth.getCurrentUser()==null){
                Intent intent = new Intent(getActivity(), SignInActivity.class);
                startActivity(intent);

            }else{

                List<Whishlist.Attribute> attributes = getFinalSelectionsWhislist();

                Whishlist whislistItem= new Whishlist(productId,attributes);


                String uid = firebaseAuth.getCurrentUser().getUid();

                db.collection("users").document(uid).collection("wishlist")
                        .document(productId)
                        .set(whislistItem)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                LayoutInflater inflater = getLayoutInflater();
                                View layout = inflater.inflate(R.layout.custom_toast, null);

                                TextView text = layout.findViewById(R.id.toastText);
                                text.setText("Item Added to the wishlist");

                                Toast toast = new Toast(requireContext());
                                toast.setDuration(Toast.LENGTH_SHORT);
                                toast.setView(layout);
                                toast.show();
                            }
                        })
                        .addOnFailureListener(e -> {
                            if (isAdded()) {
                                Toast.makeText(requireContext(), "Unable to add to wishlist: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
            }
        });
    }

    // Double-tap the displayed product image to zoom in or return to normal.
    private void enableImageDoubleTapZoom() {
        RecyclerView slider = (RecyclerView) binding.productImageSlider.getChildAt(0);
        final View[] zoomedPage = {null};

        GestureDetector detector = new GestureDetector(requireContext(),
                new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onDoubleTap(MotionEvent e) {
                        View page = slider.findChildViewUnder(e.getX(), e.getY());
                        if (page == null) return true;

                        if (zoomedPage[0] != null && zoomedPage[0] != page) {
                            zoomedPage[0].setScaleX(1f);
                            zoomedPage[0].setScaleY(1f);
                        }

                        boolean zoomIn = page.getScaleX() <= 1f;
                        page.animate().scaleX(zoomIn ? 2f : 1f)
                                .scaleY(zoomIn ? 2f : 1f).setDuration(200).start();
                        zoomedPage[0] = zoomIn ? page : null;
                        return true;
                    }
                });

        slider.addOnItemTouchListener(new RecyclerView.SimpleOnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv,
                                                 @NonNull MotionEvent event) {
                // Observe taps without blocking the existing image-swipe behavior.
                detector.onTouchEvent(event);
                return false;
            }
        });

        binding.productImageSlider.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                if (zoomedPage[0] != null) {
                    zoomedPage[0].animate().cancel();
                    zoomedPage[0].setScaleX(1f);
                    zoomedPage[0].setScaleY(1f);
                    zoomedPage[0] = null;
                }
            }
        });
    }

    private void loadTopSellProducts() {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("products")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot qds) {
                        if (!qds.isEmpty()){
                            List<Product> products = new ArrayList<>();
                            qds.getDocuments().forEach(doc -> products.add(FirestoreMapper.toProduct(doc)));

                            LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL,false);
                            binding.productDetailsTopSellSection.itemSectionContainer.setLayoutManager(layoutManager);

                            SectionAdapter adapter = new SectionAdapter(products, product -> {

                                Bundle bundel = new Bundle();
                                bundel.putString("productId",product.getProductId());

                                ProductDetailsFragment productDetailsFragment = new ProductDetailsFragment();
                                productDetailsFragment.setArguments(bundel);

                                getParentFragmentManager().beginTransaction()
                                        .replace(R.id.fragment_container,productDetailsFragment)
                                        .addToBackStack(null)
                                        .commit();
                            });
                            binding.productDetailsTopSellSection.itemSectionTitle.setText("Top Selling Products");
                            binding.productDetailsTopSellSection.itemSectionContainer.setAdapter(adapter);
                        }
                    }
                });


    }


    private void renderAttributes(Product.Attribute attribute, ViewGroup container) {
        if (attribute == null || container == null) return;
        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.VERTICAL);

        TextView label = new TextView(requireContext());
        label.setText(attribute.getName() == null ? "" : attribute.getName());
        label.setPadding(0, 10, 0, 10);
        label.setTextColor(Color.WHITE);
        row.addView(label);

        ChipGroup group = new ChipGroup(requireContext());
        group.setSelectionRequired(true);
        group.setSingleSelection(true);

        if (attribute.getValues() != null) {
            attribute.getValues().forEach(value -> {
                if (value == null) return;
                Chip chip = new Chip(requireContext());
                chip.setCheckable(true);
                chip.setId(View.generateViewId());
                chip.setTag(value);

                if ("color".equalsIgnoreCase(attribute.getType())) {
                    try {
                        chip.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor(value)));
                    } catch (IllegalArgumentException ex) {
                        chip.setText(value);
                        chip.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor("#FFD700")));
                        chip.setTextColor(Color.BLACK);
                    }
                    chip.setChipMinHeight(100f);
                } else {
                    chip.setText(value);
                    chip.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor("#FFD700")));
                    chip.setTextColor(Color.BLACK);
                }
                group.addView(chip);
            });
        }

        row.addView(group);
        container.addView(row);
        if (attribute.getName() != null) attrChipGroups.put(attribute.getName(), group);
    }

    private List<CartItem.Attribute> getFinalSelections() {
        List<CartItem.Attribute> attributes = new ArrayList<>();

        for (Map.Entry<String, ChipGroup> entry : attrChipGroups.entrySet()) {
            String attributeName = entry.getKey();
            ChipGroup chipGroup = entry.getValue();

            int checkedChipId = chipGroup.getCheckedChipId();

            if (checkedChipId != -1) {
                // FIX: Find the chip INSIDE the specific chipGroup
                Chip chip = chipGroup.findViewById(checkedChipId);
                if (chip != null) {
                    String selectedValue = chip.getTag().toString();
                    attributes.add(new CartItem.Attribute(attributeName, selectedValue));
                }
            }
        }
        return attributes;
    }

    private List<Whishlist.Attribute> getFinalSelectionsWhislist() {
        List<Whishlist.Attribute> attributess = new ArrayList<>();

        for (Map.Entry<String, ChipGroup> entry : attrChipGroups.entrySet()) {
            String attributeName = entry.getKey();

        }
        return attributess;
    }

    @Override
    public void onStop() {
        super.onStop();

        getActivity().findViewById(R.id.bottom_navigation_view).setVisibility(View.VISIBLE);

    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().findViewById(R.id.bottom_navigation_view).setVisibility(View.GONE);

    }
}