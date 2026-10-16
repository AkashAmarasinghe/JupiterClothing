package lk.akash.jupclothing.model;


import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem {

    private String productId;
    private int quantity;

    private List<Attribute> attributes;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Attribute {

        private String name;
        private String value;
    }
}
