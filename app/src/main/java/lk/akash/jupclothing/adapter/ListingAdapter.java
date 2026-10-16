package lk.akash.jupclothing.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import lk.akash.jupclothing.R;
import lk.akash.jupclothing.model.Product;
import lk.akash.jupclothing.util.ImageLoader;

public class ListingAdapter extends RecyclerView.Adapter<ListingAdapter.ViewHolder> {
    private final List<Product> products;
    private final OnProductClickListner listner;

    public ListingAdapter(List<Product> products, OnProductClickListner listner) {
        this.products = products;
        this.listner = listner;
    }

    @NonNull @Override public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_listing, parent, false));
    }

    @Override public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = products.get(position);
        holder.productTitle.setText(product.getTitle() == null ? "" : product.getTitle());
        holder.productPrice.setText("LKR " + product.getPrice());
        String image = product.getImages() != null && !product.getImages().isEmpty() ? product.getImages().get(0) : null;
        ImageLoader.load(holder.productImage, image);
        holder.itemView.setOnClickListener(v -> {
            v.startAnimation(AnimationUtils.loadAnimation(v.getContext(), R.anim.click_animation));
            if (listner != null) listner.onProductClick(product);
        });
    }

    @Override public int getItemCount() { return products == null ? 0 : products.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage; TextView productTitle, productPrice;
        public ViewHolder(@NonNull View itemView) { super(itemView); productImage=itemView.findViewById(R.id.listing_img); productTitle=itemView.findViewById(R.id.listing_name); productPrice=itemView.findViewById(R.id.listing_price); }
    }
    public interface OnProductClickListner { void onProductClick(Product product); }
}
