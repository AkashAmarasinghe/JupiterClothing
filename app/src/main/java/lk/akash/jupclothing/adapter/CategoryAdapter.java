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
import lk.akash.jupclothing.model.Category;
import lk.akash.jupclothing.util.ImageLoader;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {
    private final List<Category> categories;
    private final OnCategoryClickListner listner;
    public CategoryAdapter(List<Category> categories, OnCategoryClickListner listner){ this.categories=categories; this.listner=listner; }
    @NonNull @Override public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,int viewType){ return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category,parent,false)); }
    @Override public void onBindViewHolder(@NonNull ViewHolder holder,int position){
        Category c=categories.get(position); holder.categoryName.setText(c.getName()==null?"":c.getName()); ImageLoader.load(holder.categoryImage,c.getImageUrl());
        holder.itemView.setOnClickListener(v->{ v.startAnimation(AnimationUtils.loadAnimation(v.getContext(),R.anim.click_animation)); if(listner!=null) listner.onCategoryClick(c); });
    }
    @Override public int getItemCount(){ return categories==null?0:categories.size(); }
    public static class ViewHolder extends RecyclerView.ViewHolder { ImageView categoryImage; TextView categoryName; ViewHolder(@NonNull View v){super(v);categoryImage=v.findViewById(R.id.category_img);categoryName=v.findViewById(R.id.category_name);} }
    public interface OnCategoryClickListner{ void onCategoryClick(Category category); }
}
