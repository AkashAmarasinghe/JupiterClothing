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

public class homeCategoryAdapter extends RecyclerView.Adapter<homeCategoryAdapter.ViewHolder> {
    private final List<Category> categories;
    private final OnHomeCategoryClickListner listner;
    public homeCategoryAdapter(List<Category> categories, OnHomeCategoryClickListner listner)
    {this.categories=categories;this.listner=listner;}

    @NonNull @Override public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,int viewType){return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.home_item_category,parent,false));}

    @Override public void onBindViewHolder(@NonNull ViewHolder holder,int position){
        if(categories==null||categories.isEmpty())return; Category c=categories.get(position%categories.size()); holder.homeCategoryName.setText(c.getName()==null?"":c.getName()); ImageLoader.load(holder.homeCategoryImage,c.getImageUrl());
        holder.itemView.setOnClickListener(v->{v.startAnimation(AnimationUtils.loadAnimation(v.getContext(),R.anim.click_animation));if(listner!=null)listner.onCategoryClick(c);});
    }

    @Override public int getItemCount(){return categories==null||categories.isEmpty()?0:Integer.MAX_VALUE;}
    public int getRealItemCount(){return categories==null?0:categories.size();}
    public static class ViewHolder extends RecyclerView.ViewHolder{ImageView homeCategoryImage;TextView homeCategoryName;ViewHolder(@NonNull View v){super(v);homeCategoryImage=v.findViewById(R.id.homecategory_img);homeCategoryName=v.findViewById(R.id.homecategory_name);}}
    public interface OnHomeCategoryClickListner{void onCategoryClick(Category category);}
}
