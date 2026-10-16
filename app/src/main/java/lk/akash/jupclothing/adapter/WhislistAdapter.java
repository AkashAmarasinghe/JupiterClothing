package lk.akash.jupclothing.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import lk.akash.jupclothing.R;
import lk.akash.jupclothing.model.Product;
import lk.akash.jupclothing.model.Whishlist;
import lk.akash.jupclothing.util.FirestoreMapper;
import lk.akash.jupclothing.util.ImageLoader;

public class WhislistAdapter extends RecyclerView.Adapter<WhislistAdapter.ViewHolder> {

    private final List<Whishlist> whishlistsItem;
    private final OnProductClickListner listner;

    public WhislistAdapter(List<Whishlist> whishlistsItems, OnProductClickListner listner) {
        this.whishlistsItem = whishlistsItems;
        this.listner = listner;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_whislist, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Whishlist wishlist = whishlistsItem.get(position);
        holder.productTitle.setText("");
        holder.productPrice.setText("");
        ImageLoader.load(holder.productImage, null);

        if (wishlist == null || wishlist.getProductId() == null || wishlist.getProductId().trim().isEmpty()) return;
        String productId = wishlist.getProductId();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // New data uses the Firestore document id as product id. Try direct lookup first.
        db.collection("products").document(productId).get()
                .addOnSuccessListener(doc -> {
                    if (holder.getBindingAdapterPosition() == RecyclerView.NO_POSITION) return;
                    if (doc.exists()) {
                        bindProduct(holder, FirestoreMapper.toProduct(doc));
                    } else {
                        // Compatibility with old documents where productId is only a field.
                        db.collection("products").get().addOnSuccessListener(snapshot -> {
                            for (com.google.firebase.firestore.DocumentSnapshot item : snapshot.getDocuments()) {
                                Product product = FirestoreMapper.toProduct(item);
                                if (productId.equals(product.getProductId())) {
                                    bindProduct(holder, product);
                                    break;
                                }
                            }
                        });
                    }
                });
    }

    private void bindProduct(ViewHolder holder, Product product) {
        if (product == null || holder.getBindingAdapterPosition() == RecyclerView.NO_POSITION) return;
        holder.productTitle.setText(product.getTitle() == null ? "" : product.getTitle());
        holder.productPrice.setText("LKR " + product.getPrice());
        String image = product.getImages() != null && !product.getImages().isEmpty() ? product.getImages().get(0) : null;
        ImageLoader.load(holder.productImage, image);
        holder.itemView.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos != RecyclerView.NO_POSITION && listner != null) listner.onProductClick(product, pos);
        });
    }

    @Override public int getItemCount() { return whishlistsItem == null ? 0 : whishlistsItem.size(); }

    public void removeItem(int position) {
        if (position >= 0 && position < whishlistsItem.size()) {
            whishlistsItem.remove(position);
            notifyItemRemoved(position);
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productTitle;
        TextView productPrice;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.item_whislist_image);
            productTitle = itemView.findViewById(R.id.item_whislist_title);
            productPrice = itemView.findViewById(R.id.item_whislist_price);
        }
    }

    public interface OnProductClickListner {
        void onProductClick(Product product, int position);
    }
}
