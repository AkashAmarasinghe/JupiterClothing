package lk.akash.jupclothing.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lk.akash.jupclothing.adapter.NotificationAdapter;
import lk.akash.jupclothing.databinding.FragmentNotifiactionBinding;
import lk.akash.jupclothing.model.NotificationItem;

public class NotificationFragment extends Fragment {

    private FragmentNotifiactionBinding binding;

    private final List<NotificationItem> notificationList = new ArrayList<>();
    private NotificationAdapter notificationAdapter;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    private ListenerRegistration notificationListener;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentNotifiactionBinding.inflate(
                inflater,
                container,
                false
        );

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {

        super.onViewCreated(view, savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        notificationAdapter = new NotificationAdapter(notificationList);

        binding.notificationRecyclerView.setLayoutManager(
                new LinearLayoutManager(requireContext())
        );

        binding.notificationRecyclerView.setAdapter(notificationAdapter);

        loadNotifications();
    }

    private void loadNotifications() {

        FirebaseUser user = firebaseAuth.getCurrentUser();

        if (user == null) {

            showEmptyState();

            Toast.makeText(
                    requireContext(),
                    "Please sign in to view notifications",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String uid = user.getUid();

        binding.notificationProgress.setVisibility(View.VISIBLE);

        notificationListener = firestore
                .collection("users")
                .document(uid)
                .collection("notifications")
                .addSnapshotListener((snapshot, error) -> {

                    if (binding == null) {
                        return;
                    }

                    binding.notificationProgress.setVisibility(View.GONE);

                    if (error != null) {

                        showEmptyState();

                        Toast.makeText(
                                requireContext(),
                                "Failed to load notifications: " + error.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show();

                        return;
                    }

                    notificationList.clear();

                    List<DocumentReference> unreadDocuments =
                            new ArrayList<>();

                    if (snapshot != null) {

                        for (DocumentSnapshot document :
                                snapshot.getDocuments()) {

                            String title =
                                    document.getString("title");

                            String message =
                                    document.getString("message");

                            Boolean readValue =
                                    document.getBoolean("read");

                            boolean isRead =
                                    readValue != null && readValue;

                            Date createdAt =
                                    getNotificationDate(document);

                            NotificationItem item =
                                    new NotificationItem(
                                            document.getId(),
                                            title,
                                            message,
                                            isRead,
                                            createdAt
                                    );

                            notificationList.add(item);

                            if (!isRead) {
                                unreadDocuments.add(
                                        document.getReference()
                                );
                            }
                        }
                    }

                    // Newest notifications first
                    notificationList.sort((item1, item2) -> {

                        Date date1 = item1.getCreatedAt();
                        Date date2 = item2.getCreatedAt();

                        if (date1 == null && date2 == null) {
                            return 0;
                        }

                        if (date1 == null) {
                            return 1;
                        }

                        if (date2 == null) {
                            return -1;
                        }

                        return date2.compareTo(date1);
                    });

                    notificationAdapter.notifyDataSetChanged();

                    if (notificationList.isEmpty()) {

                        showEmptyState();

                    } else {

                        binding.emptyNotificationsLayout
                                .setVisibility(View.GONE);

                        binding.notificationRecyclerView
                                .setVisibility(View.VISIBLE);
                    }

                    // User opened notifications -> mark unread notifications read
                    if (!unreadDocuments.isEmpty()) {

                        markNotificationsAsRead(
                                uid,
                                unreadDocuments
                        );
                    }
                });
    }

    private Date getNotificationDate(
            DocumentSnapshot document
    ) {

        Timestamp createdAt =
                document.getTimestamp("createdAt");

        if (createdAt != null) {
            return createdAt.toDate();
        }

        Timestamp createdAtClient =
                document.getTimestamp("createdAtClient");

        if (createdAtClient != null) {
            return createdAtClient.toDate();
        }

        return null;
    }

    private void markNotificationsAsRead(
            String uid,
            List<DocumentReference> unreadDocuments
    ) {

        WriteBatch batch =
                firestore.batch();

        for (DocumentReference reference :
                unreadDocuments) {

            batch.update(
                    reference,
                    "read",
                    true
            );
        }

        batch.update(
                firestore
                        .collection("users")
                        .document(uid),
                "hasUnreadNotifications",
                false
        );

        batch.commit()
                .addOnFailureListener(e -> {

                    if (!isAdded()) {
                        return;
                    }

                    Toast.makeText(
                            requireContext(),
                            "Unable to update notification status",
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }

    private void showEmptyState() {

        if (binding == null) {
            return;
        }

        binding.notificationProgress
                .setVisibility(View.GONE);

        binding.notificationRecyclerView
                .setVisibility(View.GONE);

        binding.emptyNotificationsLayout
                .setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroyView() {

        if (notificationListener != null) {

            notificationListener.remove();
            notificationListener = null;
        }

        binding = null;

        super.onDestroyView();
    }
}