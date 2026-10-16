package lk.akash.jupclothing.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import lk.akash.jupclothing.R;

public class PaymentApprovedFragment extends Fragment {

    private TextView txtDeliveryDate;
    private Button btnBackToShop;

    private static final String TAG = "PaymentApprovedFrag";

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {

        View view = inflater.inflate(
                R.layout.fragment_payment_approved,
                container,
                false
        );


        txtDeliveryDate = view.findViewById(R.id.approveDate);
        btnBackToShop = view.findViewById(R.id.btnBack);


        if (btnBackToShop != null) {

            btnBackToShop.setOnClickListener(v -> {

                BottomNavigationView bottomNav =
                        requireActivity()
                                .findViewById(R.id.bottom_navigation_view);

                if (bottomNav != null) {


                    bottomNav.setSelectedItemId(R.id.bottomNavHome);

                } else {

                    getParentFragmentManager()
                            .beginTransaction()
                            .replace(
                                    R.id.fragment_container,
                                    new HomeFragment()
                            )
                            .commit();
                }

            });

        } else {

            Log.e(
                    TAG,
                    "btnBack button not found. Check fragment_payment_approved.xml"
            );
        }

        if (txtDeliveryDate == null) {

            showCustomToast(
                    "approveDate TextView not found"
            );

            Log.e(
                    TAG,
                    "approveDate TextView is null. Check XML id."
            );

            return view;
        }
        txtDeliveryDate.setText("Loading date...");

        loadExpectedDate();

        return view;
    }

    private void loadExpectedDate() {

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {

            showCustomToast("User not logged in");

            txtDeliveryDate.setText(
                    "User not logged in"
            );

            return;
        }


        String userId =
                FirebaseAuth
                        .getInstance()
                        .getCurrentUser()
                        .getUid();


        FirebaseFirestore
                .getInstance()
                .collection("payments")
                .whereEqualTo("userId", userId)
                .get()

                .addOnSuccessListener(queryDocumentSnapshots -> {

                    if (!isAdded()) {
                        return;
                    }


                    // No payment found
                    if (queryDocumentSnapshots == null ||
                            queryDocumentSnapshots.isEmpty()) {

                        txtDeliveryDate.setText(
                                "No payment found"
                        );

                        return;
                    }


                    String latestDate = null;



                    for (QueryDocumentSnapshot document :
                            queryDocumentSnapshots) {

                        String paymentDate =
                                document.getString("date");


                        if (paymentDate != null &&
                                !paymentDate.trim().isEmpty()) {

                            if (latestDate == null ||
                                    paymentDate.compareTo(latestDate) > 0) {

                                latestDate = paymentDate;
                            }
                        }
                    }


                    if (latestDate == null) {

                        txtDeliveryDate.setText(
                                "No payment date found"
                        );

                        return;
                    }

                    try {

                        SimpleDateFormat inputFormat =
                                new SimpleDateFormat(
                                        "yyyy-MM-dd",
                                        Locale.getDefault()
                                );



                        SimpleDateFormat outputFormat =
                                new SimpleDateFormat(
                                        "MMM dd, yyyy",
                                        Locale.ENGLISH
                                );


                        java.util.Date paymentDate =
                                inputFormat.parse(latestDate);


                        if (paymentDate == null) {

                            txtDeliveryDate.setText(
                                    "Date parse error"
                            );

                            return;
                        }


                        Calendar calendar =
                                Calendar.getInstance();

                        calendar.setTime(paymentDate);

                        // Expected delivery = payment date + 4 days
                        calendar.add(
                                Calendar.DAY_OF_MONTH,
                                4
                        );


                        String expectedDeliveryDate =
                                outputFormat.format(
                                        calendar.getTime()
                                );


                        txtDeliveryDate.setText(
                                expectedDeliveryDate
                        );


                        Log.d(
                                TAG,
                                "Expected delivery date: "
                                        + expectedDeliveryDate
                        );


                    } catch (Exception e) {

                        Log.e(
                                TAG,
                                "Date parsing error: "
                                        + e.getMessage(),
                                e
                        );

                        txtDeliveryDate.setText(
                                "Date format error"
                        );
                    }

                })

                .addOnFailureListener(e -> {

                    if (!isAdded()) {
                        return;
                    }


                    Log.e(
                            TAG,
                            "Firestore load failed: "
                                    + e.getMessage(),
                            e
                    );


                    txtDeliveryDate.setText(
                            "Failed to load date"
                    );


                    showCustomToast(
                            "Error: " + e.getMessage()
                    );

                });
    }


    // =====================================================
    // CUSTOM TOAST
    // =====================================================
    private void showCustomToast(String message) {

        if (!isAdded()) {
            return;
        }


        try {

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


            if (text != null) {
                text.setText(message);
            }


            Toast toast =
                    new Toast(requireContext());

            toast.setDuration(
                    Toast.LENGTH_SHORT
            );

            toast.setView(layout);

            toast.show();


        } catch (Exception e) {

            // Normal toast fallback
            Toast.makeText(
                    requireContext(),
                    message,
                    Toast.LENGTH_SHORT
            ).show();
        }
    }
}