package com.codewithjosh.NewsExpose2k20;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.codewithjosh.NewsExpose2k20.models.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.auth.User;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    Button btn_register;
    EditText et_user_name, et_email, et_password, et_re_password;
    LinearLayout nav_login, is_loading;

    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    FirebaseFirestore firebaseFirestore;

    DatabaseReference databaseRef;
    DocumentReference documentRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        btn_register = findViewById(R.id.btn_register);
        et_user_name = findViewById(R.id.et_user_name);
        et_email = findViewById(R.id.et_email);
        et_password = findViewById(R.id.et_password);
        et_re_password = findViewById(R.id.et_re_password);
        nav_login = findViewById(R.id.nav_login);
        is_loading = findViewById(R.id.is_loading);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        nav_login.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        btn_register.setOnClickListener(v -> {

            is_loading.setVisibility(View.VISIBLE);
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(CommentActivity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
            if (getCurrentFocus() != null) getCurrentFocus().clearFocus();

            final String s_user_name = et_user_name.getText().toString().toLowerCase();
            final String s_email = et_email.getText().toString().toLowerCase();
            final String s_password = et_password.getText().toString();
            final String s_re_password = et_re_password.getText().toString();

            if (!isConnected()) {
                is_loading.setVisibility(View.GONE);
                Toast.makeText(RegisterActivity.this, "No Internet Connection!", Toast.LENGTH_SHORT).show();
            }
            else if (s_user_name.isEmpty() || s_email.isEmpty()
                    || s_password.isEmpty() || s_re_password.isEmpty()) {
                is_loading.setVisibility(View.GONE);
                Toast.makeText(RegisterActivity.this, "All fields are required!", Toast.LENGTH_SHORT).show();
            }
            else if (!s_user_name.startsWith("@ne.")) {
                is_loading.setVisibility(View.GONE);
                Toast.makeText(RegisterActivity.this, "Username must starts with @ne.", Toast.LENGTH_SHORT).show();
            }
            else if (s_user_name.length() < 5) {
                is_loading.setVisibility(View.GONE);
                Toast.makeText(RegisterActivity.this, "Provide a valid Username", Toast.LENGTH_SHORT).show();
            }
            else if (!s_email.endsWith("@ne.xpose")) {
                is_loading.setVisibility(View.GONE);
                Toast.makeText(RegisterActivity.this, "Email must end with @ne.xpose", Toast.LENGTH_SHORT).show();
            }
            else if (s_email.length() < 10) {
                is_loading.setVisibility(View.GONE);
                Toast.makeText(RegisterActivity.this, "Provide a valid Email Address", Toast.LENGTH_SHORT).show();
            }
            else if (s_password.length() < 6) {
                is_loading.setVisibility(View.GONE);
                Toast.makeText(RegisterActivity.this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            }
            else if (!s_password.equals(s_re_password)) {
                is_loading.setVisibility(View.GONE);
                Toast.makeText(RegisterActivity.this, "Password doesn't match", Toast.LENGTH_SHORT).show();
            }
            else onRegister(s_user_name, s_email, s_password);

        });

    }

    private void onRegister(final String s_user_name, final String s_email, final String s_password) {

        firebaseFirestore
                .collection("Users")
                .whereEqualTo("user_name", s_user_name)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    if (queryDocumentSnapshots.isEmpty()) {

                        firebaseAuth
                                .createUserWithEmailAndPassword(s_email, s_password)
                                .addOnSuccessListener(authResult -> {

                                    final String s_user_bio = "";
                                    final String s_user_id = authResult.getUser().getUid();
                                    final String s_user_image = "https://firebasestorage.googleapis.com/v0/b/news-expose-2k20.appspot.com/o/20220415_Res%2FDefaultUserImage.png?alt=media&token=4cdbad29-194b-410e-80fa-feb641a06998";
                                    final boolean user_is_admin = false;
                                    final int i_version_code = BuildConfig.VERSION_CODE;

                                    final UserModel user = new UserModel(
                                            s_user_bio,
                                            s_email,
                                            s_user_id,
                                            s_user_image,
                                            user_is_admin,
                                            s_user_name,
                                            i_version_code
                                    );

                                    documentRef = firebaseFirestore
                                            .collection("Users")
                                            .document(s_user_id);

                                    documentRef
                                            .get()
                                            .addOnSuccessListener(documentSnapshot -> {

                                                if (documentSnapshot != null)

                                                    if (!documentSnapshot.exists()) {

                                                        documentRef
                                                                .set(user)
                                                                .addOnSuccessListener(unused -> {

                                                                    HashMap<String, Object> support = new HashMap<>();
                                                                    support.put("user_email", s_email);
                                                                    support.put("user_name", s_user_name);
                                                                    support.put("user_version_code", i_version_code);

                                                                    databaseRef = firebaseDatabase
                                                                            .getReference("Users")
                                                                            .child(s_user_id);

                                                                    databaseRef
                                                                            .get()
                                                                            .addOnSuccessListener(dataSnapshot -> {

                                                                                if (!dataSnapshot.exists()) {

                                                                                    databaseRef
                                                                                            .setValue(support)
                                                                                            .addOnSuccessListener(_unused -> {

                                                                                                is_loading.setVisibility(View.GONE);

                                                                                                Toast.makeText(this, "You're Successfully Added!", Toast.LENGTH_LONG).show();
                                                                                                startActivity(new Intent(this, LoginActivity.class));
                                                                                                finish();
                                                                                            });
                                                                                }
                                                                            });


                                                                });
                                                    }
                                            });

                                }).addOnFailureListener(e -> {

                                    if (e.toString().contains("The email address is already in use by another account")) {
                                        is_loading.setVisibility(View.GONE);
                                        Toast.makeText(this, "Email is Already Exist!", Toast.LENGTH_SHORT).show();
                                    } else if (e.toString().contains("A network error (such as timeout, interrupted connection or unreachable host) has occurred")) {
                                        is_loading.setVisibility(View.GONE);
                                        Toast.makeText(this, "No Internet Connection!", Toast.LENGTH_SHORT).show();
                                    } else {
                                        is_loading.setVisibility(View.GONE);
                                        Toast.makeText(this, "Please Contact Your Service Provider", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        is_loading.setVisibility(View.GONE);
                        Toast.makeText(this, "Username is Already Taken!", Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private boolean isConnected() {

        ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();

    }

}
