package lk.akash.jupclothing.model;

import java.security.PrivateKey;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    private String uid;
    private String name;
    private String email;
    private String password;
    private String profilePictureUrl;
    private String mobile;
    private String address;



}
