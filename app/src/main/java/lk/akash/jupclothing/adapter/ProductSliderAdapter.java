package lk.akash.jupclothing.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.List;

import lk.akash.jupclothing.R;
import lk.akash.jupclothing.util.ImageLoader;

public class ProductSliderAdapter extends RecyclerView.Adapter<ProductSliderAdapter.ProductSliderViewHolder>{
    private final List<String> images;
    public ProductSliderAdapter(List<String> images){this.images=images==null? Collections.emptyList():images;}
    @NonNull @Override public ProductSliderViewHolder onCreateViewHolder(@NonNull ViewGroup parent,int viewType){return new ProductSliderViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.product_slider_item,parent,false));}
    @Override public void onBindViewHolder(@NonNull ProductSliderViewHolder holder,int position){ImageLoader.load(holder.imageView,images.get(position));}
    @Override public int getItemCount(){return images.size();}
    public static class ProductSliderViewHolder extends RecyclerView.ViewHolder{ImageView imageView;public ProductSliderViewHolder(@NonNull View itemView){super(itemView);imageView=itemView.findViewById(R.id.product_slider_item_image);}}
}
