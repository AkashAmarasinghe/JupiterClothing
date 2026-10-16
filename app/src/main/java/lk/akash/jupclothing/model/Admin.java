package lk.akash.jupclothing.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Admin {

    private String uid;
    private String name;
    private String email;
    private String password;
    private String mobile;
    private String profilePictureUrl;



}
