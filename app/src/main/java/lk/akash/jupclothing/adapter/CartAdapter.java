package lk.akash.jupclothing.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Locale;

import lk.akash.jupclothing.R;
import lk.akash.jupclothing.model.CartItem;
import lk.akash.jupclothing.model.Product;
import lk.akash.jupclothing.util.FirestoreMapper;
import lk.akash.jupclothing.util.ImageLoader;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {
    private final List<CartItem> cartItems;
    private OnQuantityChangeListner changeListner;
    private OnRemoveListner removeListnerlistner;

    public CartAdapter(List<CartItem> cartItems) { this.cartItems = cartItems; }
    public void setOnQuantityChanegedListner(OnQuantityChangeListner listner){ this.changeListner = listner; }
    public void setOnRemoveListner(OnRemoveListner listner){ this.removeListnerlistner = listner; }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CartItem cartItem = cartItems.get(position);
        if (cartItem == null || cartItem.getProductId() == null) return;
        String productId = cartItem.getProductId();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("products").document(productId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                bindProduct(holder, cartItem, FirestoreMapper.toProduct(doc));
            } else {
                db.collection("products").get().addOnSuccessListener(snapshot -> {
                    for (com.google.firebase.firestore.DocumentSnapshot item : snapshot.getDocuments()) {
                        Product product = FirestoreMapper.toProduct(item);
                        if (productId.equals(product.getProductId())) {
                            bindProduct(holder, cartItem, product);
                            break;
                        }
                    }
                });
            }
        });
    }

    private void bindProduct(ViewHolder holder, CartItem cartItem, Product product) {
        int currentPosition = holder.getBindingAdapterPosition();
        if (currentPosition == RecyclerView.NO_POSITION || product == null) return;
        holder.productTitle.setText(product.getTitle() == null ? "" : product.getTitle());
        holder.productPrice.setText(String.format(Locale.US,"LKR %.2f", product.getPrice()));
        holder.productQty.setText(String.valueOf(cartItem.getQuantity()));
        String image = product.getImages() != null && !product.getImages().isEmpty() ? product.getImages().get(0) : null;
        ImageLoader.load(holder.productImage, image);

        holder.btnPlus.setOnClickListener(view -> {
            if (cartItem.getQuantity() < product.getStockCount()) {
                cartItem.setQuantity(cartItem.getQuantity() + 1);
                persistQuantity(cartItem);
                notifyItemChanged(holder.getBindingAdapterPosition());
                if (changeListner != null) changeListner.onChange();
            }
        });
        holder.btnMinus.setOnClickListener(view -> {
            if (cartItem.getQuantity() > 1) {
                cartItem.setQuantity(cartItem.getQuantity() - 1);
                persistQuantity(cartItem);
                notifyItemChanged(holder.getBindingAdapterPosition());
                if (changeListner != null) changeListner.onChange();
            }
        });
        holder.productRemove.setOnClickListener(view -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos != RecyclerView.NO_POSITION && removeListnerlistner != null) removeListnerlistner.onRemove(pos);
        });
    }

    private void persistQuantity(CartItem item) {
        if (item == null || item.getProductId() == null || FirebaseAuth.getInstance().getCurrentUser() == null) return;
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore.getInstance().collection("users").document(uid).collection("cart")
                .document(item.getProductId()).set(item);
    }

    @Override public int getItemCount() { return cartItems == null ? 0 : cartItems.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productTitle, productPrice, productQty, productRemove;
        AppCompatButton btnPlus, btnMinus;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.item_cart_image);
            productTitle = itemView.findViewById(R.id.item_cart_title);
            productPrice = itemView.findViewById(R.id.item_cart_price);
            productQty = itemView.findViewById(R.id.item_cart_quantity);
            btnPlus = itemView.findViewById(R.id.item_card_btn_plus);
            btnMinus = itemView.findViewById(R.id.item_card_btn_minus);
            productRemove = itemView.findViewById(R.id.item_cart_remove);
        }
    }

    public interface OnQuantityChangeListner { void onChange(); }
    public interface OnRemoveListner { void onRemove(int position); }
}
