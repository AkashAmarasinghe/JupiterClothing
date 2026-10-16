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
public class Whishlist {

    private String productId;

    private List<Attribute> attributes;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Attribute {

        private String name;

    }
}
