package lk.akash.jupclothing.activity;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import lk.akash.jupclothing.R;
import lk.akash.jupclothing.databinding.ActivitySignInBinding;

public class SignInActivity extends AppCompatActivity {

    private ActivitySignInBinding binding;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();

        binding.signInSignUpBtn.setOnClickListener(view -> {
            Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
            startActivity(intent);
            finish();
        });

        binding.signInInputEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {
                // no need
            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // no need
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // no need
            }
        });

        binding.signInSignInBtn.setOnClickListener(view -> signInUser());
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            updateUI(currentUser);
        }
    }

    private void signInUser() {
        String email = binding.signInInputEmail.getText().toString().trim();
        String password = binding.signInInputPassword.getText().toString().trim();

        if (email.isEmpty()) {
            binding.signInInputEmail.setError("Email is required");
            binding.signInInputEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.signInInputEmail.setError("Enter Valid Email");
            binding.signInInputEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            binding.signInInputPassword.setError("Password is required");
            binding.signInInputPassword.requestFocus();
            return;
        }

        binding.signInSignInBtn.setEnabled(false);

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        binding.signInSignInBtn.setEnabled(true);

                        if (task.isSuccessful()) {
                            showSuccessDialog();
                        } else {
                            showErrorDialog();
                        }
                    }
                });
    }

    private void updateUI(FirebaseUser user) {
        if (user == null) {
            return;
        }

        Intent intent = new Intent(SignInActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showSuccessDialog() {
        LinearLayout successConstrainerLayout = findViewById(R.id.successlinearLayout);
        View view = LayoutInflater.from(SignInActivity.this).inflate(R.layout.success_dialog, successConstrainerLayout, false);
        Button successDone = view.findViewById(R.id.successDone);

        AlertDialog.Builder builder = new AlertDialog.Builder(SignInActivity.this);
        builder.setView(view);
        final AlertDialog alertDialog = builder.create();

        successDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
                updateUI(firebaseAuth.getCurrentUser());
            }
        });

        if (alertDialog.getWindow() != null) {
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }
        alertDialog.show();
    }

    private void showErrorDialog() {
        LinearLayout errorConstrainerLayout = findViewById(R.id.errorlinearLayout);
        View view = LayoutInflater.from(SignInActivity.this).inflate(R.layout.error_dialog, errorConstrainerLayout, false);
        Button errorDone = view.findViewById(R.id.errorDone);

        AlertDialog.Builder builder = new AlertDialog.Builder(SignInActivity.this);
        builder.setView(view);
        final AlertDialog alertDialog = builder.create();

        errorDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });

        if (alertDialog.getWindow() != null) {
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }
        alertDialog.show();
    }
}
