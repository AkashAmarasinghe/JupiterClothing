package lk.akash.jupclothing.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import lk.akash.jupclothing.R;
import lk.akash.jupclothing.model.User;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private final List<User> userList;
    private final List<User> fullUserList;
    private final OnUserActionListener listener;

    public UserAdapter(List<User> userList, OnUserActionListener listener) {
        this.userList = userList;
        this.fullUserList = new ArrayList<>(userList);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.admin_user_loader, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = userList.get(position);

        holder.userFullName.setText(user.getName() != null ? user.getName() : "N/A");
        holder.userEmail.setText(user.getEmail() != null ? user.getEmail() : "N/A");
        holder.userAddress.setText(user.getAddress() != null ? user.getAddress() : "N/A");
        holder.userMobile.setText(user.getMobile() != null ? user.getMobile() : "N/A");

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onUserClick(user, holder.getAdapterPosition());
            }
        });

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEdit(user);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDelete(user);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList != null ? userList.size() : 0;
    }

    public void updateFullList(List<User> newList) {
        fullUserList.clear();
        fullUserList.addAll(newList);

        userList.clear();
        userList.addAll(newList);
        notifyDataSetChanged();
    }

    public void filter(String text) {
        userList.clear();

        if (text == null || text.trim().isEmpty()) {
            userList.addAll(fullUserList);
        } else {
            String searchText = text.toLowerCase().trim();

            for (User user : fullUserList) {
                String name = user.getName() != null ? user.getName().toLowerCase() : "";
                String email = user.getEmail() != null ? user.getEmail().toLowerCase() : "";
                String mobile = user.getMobile() != null ? user.getMobile().toLowerCase() : "";
                String address = user.getAddress() != null ? user.getAddress().toLowerCase() : "";

                if (name.contains(searchText) ||
                        email.contains(searchText) ||
                        mobile.contains(searchText) ||
                        address.contains(searchText)) {
                    userList.add(user);
                }
            }
        }

        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView userFullName, userEmail, userAddress, userMobile;
        Button btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            userFullName = itemView.findViewById(R.id.adminUserloaderName);
            userEmail = itemView.findViewById(R.id.adminUserloaderEmail);
            userAddress = itemView.findViewById(R.id.adminUserloaderAddress);
            userMobile = itemView.findViewById(R.id.adminUserloadermobile);
            btnEdit = itemView.findViewById(R.id.adminUserloaderEdit);
            btnDelete = itemView.findViewById(R.id.adminUserloaderDelete);
        }
    }

    public interface OnUserActionListener {
        void onUserClick(User user, int position);
        void onEdit(User user);
        void onDelete(User user);
    }
}