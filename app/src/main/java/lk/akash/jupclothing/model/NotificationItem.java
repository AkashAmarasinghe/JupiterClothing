package lk.akash.jupclothing.model;

import java.util.Date;

public class NotificationItem {

    private final String id;
    private final String title;
    private final String message;
    private final boolean read;
    private final Date createdAt;

    public NotificationItem(
            String id,
            String title,
            String message,
            boolean read,
            Date createdAt
    ) {

        this.id = id;
        this.title = title;
        this.message = message;
        this.read = read;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public boolean isRead() {
        return read;
    }

    public Date getCreatedAt() {
        return createdAt;
    }
}