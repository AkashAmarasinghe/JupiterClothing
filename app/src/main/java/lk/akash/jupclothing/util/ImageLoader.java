package lk.akash.jupclothing.util;

import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;

import lk.akash.jupclothing.R;

public final class ImageLoader {
    private ImageLoader() {}

    public static void load(ImageView view, String url) {
        if (url == null || url.trim().isEmpty()) {
            Glide.with(view).load(R.drawable.error_img).into(view);
            return;
        }
        String value = url.trim();
        if (value.startsWith("gs://")) {
            try {
                FirebaseStorage.getInstance().getReferenceFromUrl(value).getDownloadUrl()
                        .addOnSuccessListener(uri -> Glide.with(view)
                                .load(uri)
                                .placeholder(R.drawable.error_img)
                                .error(R.drawable.error_img)
                                .centerCrop().into(view))
                        .addOnFailureListener(e -> Glide.with(view).load(R.drawable.error_img).into(view));
                return;
            } catch (Exception ignored) {}
        }
        Glide.with(view)
                .load(value)
                .placeholder(R.drawable.error_img)
                .error(R.drawable.error_img)
                .centerCrop()
                .into(view);
    }
}
