package lk.akash.jupclothing.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {
    // Firestore document id. Kept separately because older data may store a display
    // value in the `category` field while products reference the document id.
    private String documentId;
    private String category;
    private String name;
    private String imageUrl;
}
