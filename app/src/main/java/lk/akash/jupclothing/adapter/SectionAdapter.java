package lk.akash.jupclothing.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import lk.akash.jupclothing.util.ImageLoader;

import java.util.List;

import lk.akash.jupclothing.R;
import lk.akash.jupclothing.model.Product;

public class SectionAdapter extends RecyclerView.Adapter<SectionAdapter.ViewHolder> {

    private List<Product> products;

    private OnProductClickListner listner;
    public SectionAdapter(List<Product> products , OnProductClickListner listner){
        this.products = products;
        this.listner = listner;
    }

    @NonNull
    @Override
    public SectionAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product_recycler,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SectionAdapter.ViewHolder holder, int position) {

        Product product = products.get(position);
        holder.productTitle.setText(product.getTitle());
        holder.productPrice.setText("LKR "+product.getPrice());
        String image = product.getImages() != null && !product.getImages().isEmpty() ? product.getImages().get(0) : null;
        ImageLoader.load(holder.productImage, image);

        holder.itemView.setOnClickListener(v->{

            Animation animation = AnimationUtils.loadAnimation(v.getContext(), R.anim.click_animation);
            v.startAnimation(animation);

            if (listner != null){
                listner.onProductClick(product);
            }
        });
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        ImageView productImage;

        TextView productTitle;

        TextView productPrice;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            productImage = itemView.findViewById(R.id.item_product_image);
            productTitle = itemView.findViewById(R.id.item_product_r_name);
            productPrice = itemView.findViewById(R.id.item_product_r_price);
        }
    }

    public interface OnProductClickListner{
        void onProductClick(Product product);


    }
}
