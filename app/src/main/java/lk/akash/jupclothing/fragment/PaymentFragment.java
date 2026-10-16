package lk.akash.jupclothing.fragment;

import android.app.Activity;
import android.content.Intent;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import lk.akash.jupclothing.R;
import lk.akash.jupclothing.databinding.FragmentPaymentBinding;
import lk.akash.jupclothing.model.Order;
import lk.payhere.androidsdk.PHConfigs;
import lk.payhere.androidsdk.PHConstants;
import lk.payhere.androidsdk.PHMainActivity;
import lk.payhere.androidsdk.PHResponse;
import lk.payhere.androidsdk.model.InitRequest;
import lk.payhere.androidsdk.model.StatusResponse;

public class PaymentFragment extends Fragment {

    private static final int PAYHERE_REQUEST = 11001;

    private FragmentPaymentBinding binding;

    private String totalPrice = "";
    private String userName = "";
    private String userAddress = "";

    private ArrayList<String> productIds = new ArrayList<>();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {

            totalPrice = getArguments().getString("total_price", "");
            userName = getArguments().getString("user_name", "");
            userAddress = getArguments().getString("user_address", "");

            ArrayList<String> receivedIds =
                    getArguments().getStringArrayList("product_ids");

            if (receivedIds != null) {
                productIds = receivedIds;
            }
        }
    }


    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentPaymentBinding.inflate(
                inflater,
                container,
                false
        );

        return binding.getRoot();
    }


    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {

        super.onViewCreated(view, savedInstanceState);


        requireActivity()
                .getOnBackPressedDispatcher()
                .addCallback(
                        getViewLifecycleOwner(),
                        new OnBackPressedCallback(true) {

                            @Override
                            public void handleOnBackPressed() {

                                requireActivity()
                                        .getSupportFragmentManager()
                                        .popBackStack();
                            }
                        }
                );


        binding.tvEditAddress.setOnClickListener(v -> {

            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(
                            R.id.fragment_container,
                            new ProfileUpdateFragment()
                    )
                    .addToBackStack(null)
                    .commit();
        });


        binding.tvTotalPrice.setText(totalPrice);
        binding.tvUserName.setText(userName);
        binding.tvAddress.setText(userAddress);


        binding.btnPlaceOrder.setOnClickListener(v -> {
            paymentMethod();
        });
    }


    // ----------------------------------------------------
    // PAYHERE PAYMENT
    // ----------------------------------------------------

    private void paymentMethod() {

        InitRequest req = new InitRequest();

        req.setMerchantId("1222787");
        req.setCurrency("LKR");

        // Current project PayHere test amount
        req.setAmount(1000.00);

        req.setOrderId("230000123");

        req.setItemsDescription("Door bell wireless");

        req.setCustom1("This is the custom message 1");
        req.setCustom2("This is the custom message 2");


        req.getCustomer().setFirstName("Saman");
        req.getCustomer().setLastName("Perera");

        req.getCustomer().setEmail(
                "renukapw83@gmail.com"
        );

        req.getCustomer().setPhone(
                "+94721977698"
        );

        req.getCustomer()
                .getAddress()
                .setAddress("No.1, Galle Road");

        req.getCustomer()
                .getAddress()
                .setCity("Colombo");

        req.getCustomer()
                .getAddress()
                .setCountry("Sri Lanka");


        Intent intent = new Intent(
                requireContext(),
                PHMainActivity.class
        );

        intent.putExtra(
                PHConstants.INTENT_EXTRA_DATA,
                req
        );


        PHConfigs.setBaseUrl(
                PHConfigs.SANDBOX_URL
        );


        startActivityForResult(
                intent,
                PAYHERE_REQUEST
        );
    }


    // ----------------------------------------------------
    // PAYMENT RESULT
    // ----------------------------------------------------

    @Override
    public void onActivityResult(
            int requestCode,
            int resultCode,
            @Nullable Intent data
    ) {

        super.onActivityResult(
                requestCode,
                resultCode,
                data
        );


        if (
                requestCode == PAYHERE_REQUEST
                        && data != null
                        && data.hasExtra(
                        PHConstants.INTENT_EXTRA_RESULT
                )
        ) {

            PHResponse<StatusResponse> response =
                    (PHResponse<StatusResponse>)
                            data.getSerializableExtra(
                                    PHConstants.INTENT_EXTRA_RESULT
                            );


            // PAYMENT SUCCESS
            if (
                    resultCode == Activity.RESULT_OK
                            && response != null
                            && response.isSuccess()
            ) {

                savePaymentsAndClearCart();

            } else {

                showCustomToast(
                        "Payment failed or cancelled"
                );
            }
        }
    }


    // ----------------------------------------------------
    // SAVE PAYMENTS + REMOVE PRODUCTS FROM CART
    // ----------------------------------------------------

    private void savePaymentsAndClearCart() {

        FirebaseAuth auth =
                FirebaseAuth.getInstance();

        FirebaseFirestore db =
                FirebaseFirestore.getInstance();


        // Check login
        if (auth.getCurrentUser() == null) {

            showCustomToast(
                    "User not logged in"
            );

            return;
        }


        String userId =
                auth.getCurrentUser().getUid();


        // Check cart product IDs
        if (
                productIds == null
                        || productIds.isEmpty()
        ) {

            showCustomToast(
                    "No products found in cart"
            );

            return;
        }


        /*
         * Firestore batch:
         *
         * 1. Save payment
         * 2. Delete paid item from cart
         *
         * Both operations commit together.
         */

        WriteBatch batch = db.batch();


        SimpleDateFormat dateFormat =
                new SimpleDateFormat(
                        "yyyy-MM-dd",
                        Locale.getDefault()
                );


        SimpleDateFormat timeFormat =
                new SimpleDateFormat(
                        "HH:mm:ss",
                        Locale.getDefault()
                );


        String currentDate =
                dateFormat.format(new Date());

        String currentTime =
                timeFormat.format(new Date());


        double amountValue =
                parseAmount(totalPrice);


        String finalPrice =
                String.format(
                        Locale.getDefault(),
                        "%.2f",
                        amountValue
                );


        // ------------------------------------------------
        // LOOP THROUGH ALL PAID CART PRODUCTS
        // ------------------------------------------------

        for (String productId : productIds) {

            if (
                    productId == null
                            || productId.trim().isEmpty()
            ) {
                continue;
            }


            // --------------------------------------------
            // CREATE PAYMENT DOCUMENT
            // --------------------------------------------

            DocumentReference paymentReference =
                    db.collection("payments")
                            .document();


            Order payment = new Order();

            payment.setId(
                    paymentReference.getId()
            );

            payment.setUserId(
                    userId
            );

            payment.setProductId(
                    productId
            );

            payment.setPrice(
                    finalPrice
            );

            payment.setDate(
                    currentDate
            );

            payment.setTime(
                    currentTime
            );

            payment.setPaymentCode(
                    generatePaymentCode(productId)
            );


            // Save payment
            batch.set(
                    paymentReference,
                    payment
            );


            // --------------------------------------------
            // DELETE PAID PRODUCT FROM CART
            // --------------------------------------------

            DocumentReference cartReference =
                    db.collection("users")
                            .document(userId)
                            .collection("cart")
                            .document(productId);


            batch.delete(
                    cartReference
            );
        }


        // ------------------------------------------------
        // COMMIT PAYMENT SAVE + CART DELETE
        // ------------------------------------------------

        batch.commit()

                .addOnSuccessListener(unused -> {

                    showCustomToast(
                            "Payment completed successfully"
                    );

                    openPaymentApprovedPage();
                })

                .addOnFailureListener(e -> {

                    showCustomToast(
                            "Failed to complete order: "
                                    + e.getMessage()
                    );
                });
    }


    // ----------------------------------------------------
    // PAYMENT APPROVED SCREEN
    // ----------------------------------------------------

    private void openPaymentApprovedPage() {

        if (!isAdded()) {
            return;
        }


        PaymentApprovedFragment fragment =
                new PaymentApprovedFragment();


        Bundle bundle = new Bundle();

        bundle.putString(
                "payment_status",
                "success"
        );


        fragment.setArguments(bundle);


        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(
                        R.id.fragment_container,
                        fragment
                )
                .addToBackStack(null)
                .commit();
    }


    // ----------------------------------------------------
    // PARSE TOTAL AMOUNT
    // ----------------------------------------------------

    private double parseAmount(
            String amountText
    ) {

        if (
                amountText == null
                        || amountText.trim().isEmpty()
        ) {
            return 0.0;
        }


        String cleanAmount =
                amountText
                        .replace("LKR", "")
                        .replace("Rs.", "")
                        .replace("Rs", "")
                        .replace(",", "")
                        .replace("/=", "")
                        .trim();


        try {

            return Double.parseDouble(
                    cleanAmount
            );

        } catch (Exception e) {

            return 0.0;
        }
    }


    // ----------------------------------------------------
    // GENERATE PAYMENT CODE
    // ----------------------------------------------------

    private String generatePaymentCode(
            String productId
    ) {

        String safeProductId =
                (
                        productId == null
                                || productId.trim().isEmpty()
                )
                        ? "PRD"
                        : productId.trim();


        if (safeProductId.length() > 6) {

            safeProductId =
                    safeProductId
                            .substring(0, 6)
                            .toUpperCase();

        } else {

            safeProductId =
                    safeProductId
                            .toUpperCase();
        }


        String timePart =
                new SimpleDateFormat(
                        "yyMMddHHmmss",
                        Locale.getDefault()
                )
                        .format(new Date());


        int randomPart =
                new Random()
                        .nextInt(900)
                        + 100;


        return "PAY-"
                + safeProductId
                + "-"
                + timePart
                + "-"
                + randomPart;
    }


    // ----------------------------------------------------
    // CUSTOM TOAST
    // ----------------------------------------------------

    private void showCustomToast(
            String message
    ) {

        if (!isAdded()) {
            return;
        }


        LayoutInflater inflater =
                getLayoutInflater();


        View layout =
                inflater.inflate(
                        R.layout.custom_toast,
                        null
                );


        TextView text =
                layout.findViewById(
                        R.id.toastText
                );


        text.setText(message);


        Toast toast =
                new Toast(
                        requireContext()
                );


        toast.setDuration(
                Toast.LENGTH_SHORT
        );

        toast.setView(layout);

        toast.show();
    }


    @Override
    public void onDestroyView() {

        super.onDestroyView();

        binding = null;
    }
}