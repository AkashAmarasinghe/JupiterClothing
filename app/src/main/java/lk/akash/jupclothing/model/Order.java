package lk.akash.jupclothing.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    private String id;
    private String userId;
    private String productId;
    private String date;
    private String time;
    private String price;
    private String userName;
    private String productName;
    private String paymentCode;


}