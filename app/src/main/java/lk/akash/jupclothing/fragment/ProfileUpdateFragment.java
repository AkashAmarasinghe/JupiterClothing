package lk.akash.jupclothing.fragment;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.SetOptions;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import lk.akash.jupclothing.R;
import lk.akash.jupclothing.databinding.FragmentProfileUpdateBinding;
import lk.akash.jupclothing.util.FirestoreMapper;
import lk.akash.jupclothing.util.ImageLoader;

public class ProfileUpdateFragment extends Fragment {

    private static final String PHOTO_FIELD = "profileImageBase64";
    private static final String STATE_PHOTO = "pendingProfilePhoto";
    private static final String STATE_ADDRESS = "profileAddress";
    private static final int PHOTO_SIZE = 256;
    private static final int MAX_PHOTO_BYTES = 100 * 1024;
    private static final int MAX_BASE64_LENGTH =
            ((MAX_PHOTO_BYTES + 2) / 3) * 4;

    private FragmentProfileUpdateBinding binding;
    private FirebaseUser currentUser;
    private FirebaseFirestore firestore;
    private Toast customToast;

    private String pendingPhotoBase64;
    private boolean loading;
    private boolean processingPhoto;
    private boolean saving;
    private int viewGeneration;

    private final Handler mainHandler =
            new Handler(Looper.getMainLooper());

    private final ExecutorService photoExecutor =
            Executors.newSingleThreadExecutor();

    private final ActivityResultLauncher<String> photoPicker =
            registerForActivityResult(
                    new ActivityResultContracts.GetContent(),
                    uri -> {
                        if (uri != null && binding != null
                                && isAdded() && !saving) {
                            preparePhoto(uri);
                        }
                    }
            );

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        binding = FragmentProfileUpdateBinding.inflate(
                inflater, container, false
        );

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        firestore = FirebaseFirestore.getInstance();

        viewGeneration++;
        loading = true;
        processingPhoto = false;
        saving = false;

        pendingPhotoBase64 = savedInstanceState == null
                ? null
                : savedInstanceState.getString(STATE_PHOTO);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        boolean restoreAddress = savedInstanceState != null
                && savedInstanceState.containsKey(STATE_ADDRESS);

        if (restoreAddress) {
            binding.updateAddress.setText(
                    savedInstanceState.getString(STATE_ADDRESS, "")
            );
        }

        if (pendingPhotoBase64 != null) {
            showPhoto(pendingPhotoBase64);
        }

        binding.updatePhotoBtn.setOnClickListener(v -> {
            if (!loading && !processingPhoto && !saving) {
                photoPicker.launch("image/*");
            }
        });

        binding.updateProfileImage.setOnClickListener(
                v -> binding.updatePhotoBtn.performClick()
        );

        binding.updateProfileBtn.setOnClickListener(
                v -> updateProfile()
        );

        binding.signUpSignInBtn.setOnClickListener(v -> {
            if (saving) {
                return;
            }

            BottomNavigationView bottomNav =
                    requireActivity().findViewById(
                            R.id.bottom_navigation_view
                    );

            if (bottomNav != null) {
                bottomNav.setSelectedItemId(R.id.bottomNavProfile);
            } else {
                getParentFragmentManager()
                        .beginTransaction()
                        .replace(
                                R.id.fragment_container,
                                new ProfileFragment()
                        )
                        .commit();
            }
        });

        refreshButtons();
        loadUserData(restoreAddress);
    }

    private boolean hasCurrentView(int generation) {
        return isAdded() && binding != null
                && generation == viewGeneration;
    }

    private void refreshButtons() {
        if (binding == null) {
            return;
        }

        boolean ready = currentUser != null
                && !loading && !processingPhoto && !saving;

        binding.updatePhotoBtn.setEnabled(ready);
        binding.updateProfileImage.setEnabled(ready);
        binding.updateProfileBtn.setEnabled(ready);
        binding.updateAddress.setEnabled(!loading && !saving);
        binding.signUpSignInBtn.setEnabled(!saving);

        binding.updateProfileBtn.setText(
                saving ? "Saving..." : "Save Updates"
        );

        binding.updatePhotoBtn.setText(
                processingPhoto ? "Preparing..." : "Change Photo"
        );
    }

    private void loadUserData(boolean restoreAddress) {
        if (currentUser == null) {
            loading = false;
            refreshButtons();
            toast("User not logged in");
            return;
        }

        if (currentUser.getEmail() != null) {
            binding.updateEmail.setText(currentUser.getEmail());
        }

        final int generation = viewGeneration;

        firestore.collection("users")
                .document(currentUser.getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    if (!hasCurrentView(generation)) {
                        return;
                    }

                    if (doc.exists()) {
                        binding.updateFullName.setText(
                                FirestoreMapper.string(
                                        doc, "name", "fullName"
                                )
                        );

                        String email =
                                FirestoreMapper.string(doc, "email");

                        if (!email.isEmpty()) {
                            binding.updateEmail.setText(email);
                        }

                        binding.updateMobile.setText(
                                FirestoreMapper.string(
                                        doc,
                                        "mobile",
                                        "phone",
                                        "phoneNumber"
                                )
                        );



                        if (!restoreAddress) {
                            binding.updateAddress.setText(
                                    FirestoreMapper.string(
                                            doc, "address"
                                    )
                            );
                        }
                    }

                    loadProfilePhoto(doc);
                    loading = false;
                    refreshButtons();
                })
                .addOnFailureListener(e -> {
                    if (!hasCurrentView(generation)) {
                        return;
                    }

                    loading = false;
                    refreshButtons();
                    loadProfilePhoto(null);

                    toast(
                            "Unable to load profile. Check your connection "
                                    + "and Firestore permissions."
                    );
                });
    }

    private void loadProfilePhoto(@Nullable DocumentSnapshot doc) {
        if (binding == null || pendingPhotoBase64 != null) {
            return;
        }

        String photoUrl = "";

        if (doc != null && doc.exists()) {
            if (showPhoto(FirestoreMapper.string(doc, PHOTO_FIELD))) {
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
            ImageLoader.load(binding.updateProfileImage, photoUrl);
        }
    }

    private void preparePhoto(Uri uri) {
        final int generation = viewGeneration;

        final ContentResolver resolver =
                requireContext()
                        .getApplicationContext()
                        .getContentResolver();

        processingPhoto = true;
        refreshButtons();

        photoExecutor.execute(() -> {
            try {
                String encoded = encodePhoto(resolver, uri);

                mainHandler.post(() -> {
                    if (!hasCurrentView(generation)) {
                        return;
                    }

                    pendingPhotoBase64 = encoded;
                    showPhoto(encoded);
                    processingPhoto = false;
                    refreshButtons();

                    toast(
                            "Photo selected. Tap Save Updates to save it."
                    );
                });

            } catch (IOException | RuntimeException e) {
                mainHandler.post(() -> {
                    if (!hasCurrentView(generation)) {
                        return;
                    }

                    processingPhoto = false;
                    refreshButtons();

                    toast(
                            "Unable to open this photo. Try another image."
                    );
                });
            }
        });
    }

    private String encodePhoto(
            ContentResolver resolver,
            Uri uri
    ) throws IOException {

        BitmapFactory.Options options =
                new BitmapFactory.Options();

        options.inJustDecodeBounds = true;

        try (InputStream stream = resolver.openInputStream(uri)) {
            if (stream == null) {
                throw new IOException("Image unavailable");
            }

            BitmapFactory.decodeStream(stream, null, options);
        }

        if (options.outWidth <= 0 || options.outHeight <= 0) {
            throw new IOException("Unsupported image");
        }

        options.inSampleSize = sampleSize(
                options.outWidth, options.outHeight
        );
        options.inJustDecodeBounds = false;

        Bitmap bitmap;

        try (InputStream stream = resolver.openInputStream(uri)) {
            if (stream == null) {
                throw new IOException("Image unavailable");
            }

            bitmap = BitmapFactory.decodeStream(
                    stream, null, options
            );
        }

        if (bitmap == null) {
            throw new IOException("Cannot decode image");
        }

        try {
            Bitmap oriented = orientPhoto(resolver, uri, bitmap);

            if (oriented != bitmap) {
                bitmap.recycle();
                bitmap = oriented;
            }

            float scale = Math.min(
                    1f,
                    (float) PHOTO_SIZE
                            / Math.max(
                            bitmap.getWidth(),
                            bitmap.getHeight()
                    )
            );

            Bitmap resized = Bitmap.createScaledBitmap(
                    bitmap,
                    Math.max(
                            1,
                            Math.round(bitmap.getWidth() * scale)
                    ),
                    Math.max(
                            1,
                            Math.round(bitmap.getHeight() * scale)
                    ),
                    true
            );

            if (resized != bitmap) {
                bitmap.recycle();
                bitmap = resized;
            }

            ByteArrayOutputStream bytes =
                    new ByteArrayOutputStream();

            for (int quality = 85; quality >= 35; quality -= 10) {
                bytes.reset();

                boolean compressed = bitmap.compress(
                        Bitmap.CompressFormat.JPEG,
                        quality,
                        bytes
                );

                if (!compressed) {
                    throw new IOException("Cannot compress image");
                }

                if (bytes.size() <= MAX_PHOTO_BYTES) {
                    return Base64.encodeToString(
                            bytes.toByteArray(),
                            Base64.NO_WRAP
                    );
                }
            }

            throw new IOException("Image too large");

        } finally {
            bitmap.recycle();
        }
    }

    private int sampleSize(int width, int height) {
        int sample = 1;

        while (Math.max(width, height) / sample > PHOTO_SIZE * 2) {
            sample *= 2;
        }

        return sample;
    }

    private Bitmap orientPhoto(
            ContentResolver resolver,
            Uri uri,
            Bitmap bitmap
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            return bitmap;
        }

        try (InputStream stream = resolver.openInputStream(uri)) {
            if (stream == null) {
                return bitmap;
            }

            ExifInterface exif = new ExifInterface(stream);

            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
            );

            Matrix matrix = new Matrix();

            switch (orientation) {
                case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                    matrix.setScale(-1, 1);
                    break;

                case ExifInterface.ORIENTATION_ROTATE_180:
                    matrix.setRotate(180);
                    break;

                case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                    matrix.setScale(1, -1);
                    break;

                case ExifInterface.ORIENTATION_TRANSPOSE:
                    matrix.setRotate(90);
                    matrix.postScale(-1, 1);
                    break;

                case ExifInterface.ORIENTATION_ROTATE_90:
                    matrix.setRotate(90);
                    break;

                case ExifInterface.ORIENTATION_TRANSVERSE:
                    matrix.setRotate(-90);
                    matrix.postScale(-1, 1);
                    break;

                case ExifInterface.ORIENTATION_ROTATE_270:
                    matrix.setRotate(-90);
                    break;

                default:
                    return bitmap;
            }

            return Bitmap.createBitmap(
                    bitmap,
                    0,
                    0,
                    bitmap.getWidth(),
                    bitmap.getHeight(),
                    matrix,
                    true
            );

        } catch (IOException | RuntimeException e) {
            return bitmap;
        }
    }

    private boolean showPhoto(String encoded) {
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

            options.inSampleSize = sampleSize(
                    options.outWidth, options.outHeight
            );
            options.inJustDecodeBounds = false;

            Bitmap bitmap = BitmapFactory.decodeByteArray(
                    bytes, 0, bytes.length, options
            );

            if (bitmap != null) {
                binding.updateProfileImage.setImageBitmap(bitmap);
                return true;
            }

        } catch (IllegalArgumentException ignored) {
            // Invalid Base64 falls back to the existing photo URL.
        }

        return false;
    }

    private void updateProfile() {
        if (binding == null || currentUser == null
                || loading || processingPhoto || saving) {
            return;
        }

        FirebaseUser signedInUser =
                FirebaseAuth.getInstance().getCurrentUser();

        if (signedInUser == null
                || !signedInUser.getUid().equals(
                currentUser.getUid()
        )) {
            toast(
                    "Please sign in again before updating your profile."
            );
            return;
        }

        String address = binding.updateAddress
                .getText()
                .toString()
                .trim();

        if (address.isEmpty()) {
            binding.updateAddress.setError("Enter address");
            binding.updateAddress.requestFocus();
            return;
        }

        Map<String, Object> values = new HashMap<>();
        values.put("address", address);

        final String photoToSave = pendingPhotoBase64;

        if (photoToSave != null) {
            values.put(PHOTO_FIELD, photoToSave);
        }

        final int generation = viewGeneration;

        saving = true;
        refreshButtons();

        firestore.collection("users")
                .document(currentUser.getUid())
                .set(values, SetOptions.merge())
                .addOnSuccessListener(unused -> {
                    if (!hasCurrentView(generation)) {
                        return;
                    }

                    pendingPhotoBase64 = null;
                    saving = false;
                    refreshButtons();

                    toast(
                            photoToSave == null
                                    ? "Address updated successfully"
                                    : "Profile photo and address "
                                    + "updated successfully"
                    );
                })
                .addOnFailureListener(e -> {
                    if (!hasCurrentView(generation)) {
                        return;
                    }

                    saving = false;
                    refreshButtons();

                    if (e instanceof FirebaseFirestoreException
                            && ((FirebaseFirestoreException) e).getCode()
                            == FirebaseFirestoreException.Code.PERMISSION_DENIED) {

                        toast(
                                "Update blocked by Firestore rules. "
                                        + "Your account needs permission "
                                        + "to update its user document."
                        );

                    } else {
                        toast(
                                "Unable to save profile. "
                                        + "Check your connection "
                                        + "and try again."
                        );
                    }
                });
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
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        if (pendingPhotoBase64 != null) {
            outState.putString(
                    STATE_PHOTO, pendingPhotoBase64
            );
        }

        if (binding != null) {
            outState.putString(
                    STATE_ADDRESS,
                    binding.updateAddress.getText().toString()
            );
        }
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

    @Override
    public void onDestroy() {
        photoExecutor.shutdownNow();
        super.onDestroy();
    }
}