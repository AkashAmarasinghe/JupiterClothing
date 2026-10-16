package lk.akash.jupclothing.activity;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import lk.akash.jupclothing.R;
import lk.akash.jupclothing.databinding.ActivitySignUpBinding;
import lk.akash.jupclothing.model.User;

public class SignUpActivity extends AppCompatActivity {

    Button successBtn;
    private ActivitySignUpBinding binding;

    private FirebaseAuth firebaseAuth;

    private FirebaseFirestore firebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);

        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        binding.signUpSignInBtn.setOnClickListener(view -> {
            Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
            startActivity(intent);
            finish();
        });

        binding.signUpSignUpBtn.setOnClickListener(view -> {
            String name = binding.signUpFullName.getText().toString().trim();
            String email = binding.signUpEmail.getText().toString().trim();
            String password = binding.signUpPassword.getText().toString().trim();
            String retryPassword = binding.signUpConfirmPassword.getText().toString().trim();
            String mobile  = binding.signUpMobile.getText().toString().trim();


            if (name.isEmpty()){
                binding.signUpFullName.setError("Name is Required");
                binding.signUpFullName.requestFocus();
                return;
            }
            if (email.isEmpty()){
                binding.signUpEmail.setError("Email is Required");
                binding.signUpEmail.requestFocus();
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                binding.signUpEmail.setError("Enter Valid Email");
                binding.signUpEmail.requestFocus();
                return;
            }

            if (password.isEmpty()){
                binding.signUpPassword.setError("Password is required");
                binding.signUpPassword.requestFocus();
                return;
            }

            if (password.length() < 6){
                binding.signUpPassword.setError("Password must be at 6 characters");
                binding.signUpPassword.requestFocus();
                return;
            }

            if (!retryPassword.equals(password)){
                binding.signUpPassword.setError("Confirm the entered the password");
                binding.signUpPassword.requestFocus();
                return;
            }

            if (mobile.isEmpty()){
                binding.signUpMobile.setError("CMobile Number is required");
                binding.signUpMobile.requestFocus();
                return;
            }

            if (mobile.length()!=10) {
                binding.signUpMobile.setError("Enter Valid Mobile Number");
                binding.signUpMobile.requestFocus();
                return;
            }



            firebaseAuth.createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){

                              String uid = task.getResult().getUser().getUid();//save karapu kenage details tika ganna puluwan

                                User user  = User.builder().uid(uid).name(name).email(email).mobile(mobile).build();

                              firebaseFirestore.collection("users")
                                      .document(uid)
                                      .set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                          @Override
                                          public void onSuccess(Void unused) {

                                              successBtn = findViewById(R.id.sign_up_sign_up_btn);

                                              successBtn.setOnClickListener(new View.OnClickListener() {
                                                  @Override
                                                  public void onClick(View view) {
                                                      showSuccessDialog();
//                                                      Intent intent = new Intent(SignUpActivity.this,MainActivity.class);
//                                                      startActivity(intent);
//                                                      finish();
                                                  }
                                              });


                                          }
                                      }).addOnFailureListener(new OnFailureListener() {
                                          @Override
                                          public void onFailure(@NonNull Exception e) {

                                          }
                                      });
                            }else{
                                showErrorDialog();

                            }

                        }
                    });
        });

    }
    private void showSuccessDialog(){
        LinearLayout successConstrainerLayout = findViewById(R.id.successlinearLayout);
        View view = LayoutInflater.from(SignUpActivity.this).inflate(R.layout.success_dialog, successConstrainerLayout);
        Button successDone = view.findViewById(R.id.successDone);

        AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
        builder.setView(view);
        final AlertDialog alertDialog = builder.create();

        successDone.findViewById(R.id.successDone).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
                Intent intent = new Intent(SignUpActivity.this,MainActivity.class);
                startActivity(intent);
            }
        });

        if (alertDialog.getWindow() != null){
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }
        alertDialog.show();

    }

    private void showErrorDialog(){
        LinearLayout successConstrainerLayout = findViewById(R.id.errorlinearLayout);
        View view = LayoutInflater.from(SignUpActivity.this).inflate(R.layout.error_dialog, successConstrainerLayout);
        Button successDone = view.findViewById(R.id.errorDone);

        AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
        builder.setView(view);
        final AlertDialog alertDialog = builder.create();

        successDone.findViewById(R.id.errorDone).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });

        if (alertDialog.getWindow() != null){
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }
        alertDialog.show();

    }
}