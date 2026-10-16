package lk.akash.jupclothing.fragment;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.Gravity;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import lk.akash.jupclothing.R;
import lk.akash.jupclothing.databinding.FragmentProfileBinding;
import lk.akash.jupclothing.util.FirestoreMapper;
import lk.akash.jupclothing.util.ImageLoader;

public class ProfileFragment extends Fragment {

    private static final String PHOTO_FIELD = "profileImageBase64";
    private static final int PHOTO_SIZE = 256;
    private static final int MAX_BASE64_LENGTH =
            ((100 * 1024 + 2) / 3) * 4;

    private FragmentProfileBinding binding;
    private FirebaseUser currentUser;
    private Toast customToast;
    private int viewGeneration;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        binding = FragmentProfileBinding.inflate(
                inflater, container, false
        );

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        viewGeneration++;

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

        binding.editProfileBtn.setOnClickListener(v ->
                requireActivity()
                        .getSupportFragmentManager()
                        .beginTransaction()
                        .replace(
                                R.id.fragment_container,
                                new ProfileUpdateFragment()
                        )
                        .addToBackStack(null)
                        .commit()
        );

        loadUserData();
    }

    private boolean hasCurrentView(int generation) {
        return isAdded() && binding != null
                && generation == viewGeneration;
    }

    private void loadUserData() {
        if (currentUser == null) {
            toast("User not logged in", Toast.LENGTH_SHORT);
            return;
        }

        if (currentUser.getDisplayName() != null) {
            binding.profileHeaderName.setText(
                    currentUser.getDisplayName()
            );
        }

        if (currentUser.getEmail() != null) {
            binding.profileHeaderEmail.setText(
                    currentUser.getEmail()
            );
        }

        final int generation = viewGeneration;

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUser.getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    if (!hasCurrentView(generation)) {
                        return;
                    }

                    if (doc.exists()) {
                        String name = FirestoreMapper.string(
                                doc, "name", "fullName"
                        );

                        String email = FirestoreMapper.string(
                                doc, "email"
                        );

                        String mobile = FirestoreMapper.string(
                                doc,
                                "mobile",
                                "phone",
                                "phoneNumber"
                        );

                        if (!name.isEmpty()) {
                            binding.profileHeaderName.setText(name);
                        }

                        if (!email.isEmpty()) {
                            binding.profileHeaderEmail.setText(email);
                        }

                        if (!mobile.isEmpty()) {
                            binding.profileHeaderMobile.setText(mobile);
                        }
                    }

                    loadProfilePhoto(doc);
                })
                .addOnFailureListener(e -> {
                    if (!hasCurrentView(generation)) {
                        return;
                    }

                    loadProfilePhoto(null);

                    toast(
                            "Unable to load profile. Check Firestore Rules."
                    );
                });
    }

    private void loadProfilePhoto(@Nullable DocumentSnapshot doc) {
        String photoUrl = "";

        if (doc != null && doc.exists()) {
            if (showStoredPhoto(
                    FirestoreMapper.string(doc, PHOTO_FIELD)
            )) {
                return;
            }

            photoUrl = FirestoreMapper.string(
                    doc,
                    "profilePictureUrl",
                    "getProfileImage",
                    "profileImage",
                    "photoUrl"
            );
        }

        if (photoUrl.isEmpty() && currentUser != null
                && currentUser.getPhotoUrl() != null) {
            photoUrl = currentUser.getPhotoUrl().toString();
        }

        if (!photoUrl.isEmpty()) {
            ImageLoader.load(binding.headerProfilePic, photoUrl);
        }
    }

    private boolean showStoredPhoto(String encoded) {
        if (binding == null || encoded == null
                || encoded.isEmpty()
                || encoded.length() > MAX_BASE64_LENGTH) {
            return false;
        }

        try {
            byte[] bytes = Base64.decode(
                    encoded, Base64.DEFAULT
            );

            BitmapFactory.Options options =
                    new BitmapFactory.Options();

            options.inJustDecodeBounds = true;

            BitmapFactory.decodeByteArray(
                    bytes, 0, bytes.length, options
            );

            if (options.outWidth <= 0 || options.outHeight <= 0) {
                return false;
            }

            options.inSampleSize = 1;

            while (Math.max(
                    options.outWidth,
                    options.outHeight
            ) / options.inSampleSize > PHOTO_SIZE * 2) {
                options.inSampleSize *= 2;
            }

            options.inJustDecodeBounds = false;

            Bitmap bitmap = BitmapFactory.decodeByteArray(
                    bytes, 0, bytes.length, options
            );

            if (bitmap != null) {
                binding.headerProfilePic.setImageBitmap(bitmap);
                return true;
            }

        } catch (IllegalArgumentException ignored) {
            // Invalid Base64 falls back to the existing photo URL.
        }

        return false;
    }

    private void toast(String message) {
        toast(message, Toast.LENGTH_LONG);
    }

    @SuppressWarnings("deprecation")
    private void toast(String message, int duration) {
        if (!isAdded() || binding == null) {
            return;
        }

        if (customToast != null) {
            customToast.cancel();
        }

        View toastView = getLayoutInflater().inflate(
                R.layout.custom_toast, null
        );

        TextView toastText =
                toastView.findViewById(R.id.toastText);

        toastText.setText(message);

        int bottomOffset = Math.round(
                80 * getResources().getDisplayMetrics().density
        );

        customToast = new Toast(requireContext());
        customToast.setDuration(duration);
        customToast.setGravity(
                Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL,
                0,
                bottomOffset
        );
        customToast.setView(toastView);
        customToast.show();
    }

    @Override
    public void onDestroyView() {
        if (customToast != null) {
            customToast.cancel();
            customToast = null;
        }

        viewGeneration++;
        binding = null;
        super.onDestroyView();
    }
}