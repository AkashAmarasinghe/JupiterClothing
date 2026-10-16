package lk.akash.jupclothing.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import lk.akash.jupclothing.R;
import lk.akash.jupclothing.model.NotificationItem;

public class NotificationAdapter
        extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private final List<NotificationItem> notificationList;

    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat(
                    "MMM d, yyyy • h:mm a",
                    Locale.getDefault()
            );

    public NotificationAdapter(
            List<NotificationItem> notificationList
    ) {

        this.notificationList = notificationList;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(
                        R.layout.item_notification,
                        parent,
                        false
                );

        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull NotificationViewHolder holder,
            int position
    ) {

        NotificationItem item =
                notificationList.get(position);

        String title = item.getTitle();

        if (title == null || title.trim().isEmpty()) {

            holder.title.setText(
                    "JUPITER Update"
            );

        } else {

            holder.title.setText(title);
        }

        String message = item.getMessage();

        holder.message.setText(
                message == null ? "" : message
        );

        if (item.getCreatedAt() != null) {

            holder.time.setText(
                    dateFormat.format(
                            item.getCreatedAt()
                    )
            );

        } else {

            holder.time.setText(
                    "Just now"
            );
        }

        holder.unreadDot.setVisibility(
                item.isRead()
                        ? View.GONE
                        : View.VISIBLE
        );
    }

    @Override
    public int getItemCount() {

        return notificationList.size();
    }

    static class NotificationViewHolder
            extends RecyclerView.ViewHolder {

        final TextView title;
        final TextView message;
        final TextView time;

        final View unreadDot;

        NotificationViewHolder(
                @NonNull View itemView
        ) {

            super(itemView);

            title =
                    itemView.findViewById(
                            R.id.notificationItemTitle
                    );

            message =
                    itemView.findViewById(
                            R.id.notificationItemMessage
                    );

            time =
                    itemView.findViewById(
                            R.id.notificationItemTime
                    );

            unreadDot =
                    itemView.findViewById(
                            R.id.notificationItemUnreadDot
                    );
        }
    }
}