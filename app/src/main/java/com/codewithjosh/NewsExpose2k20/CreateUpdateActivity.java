package com.codewithjosh.NewsExpose2k20;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.HashMap;

public class CreateUpdateActivity extends AppCompatActivity {

    EditText et_update_content;
    ImageButton btn_back;
    ImageView iv_update_image;
    TextView btn_create_update;

    String s_update_image;
    UploadTask uTask;
    Uri uri;

    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    FirebaseStorage firebaseStorage;

    StorageReference storageRef;

    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_update);

        et_update_content = findViewById(R.id.et_update_content);
        btn_back = findViewById(R.id.btn_back);
        iv_update_image = findViewById(R.id.iv_update_image);
        btn_create_update = findViewById(R.id.btn_create_update);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();

        storageRef = firebaseStorage.getReference("Updates");

        btn_back.setOnClickListener(v -> {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        });

        btn_create_update.setOnClickListener(v -> onUpdate());

        CropImage.activity()
                .setAspectRatio(1, 1)
                .start(this);
    }

    //    TODO: FOUND ISSUE: BUG
    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void onUpdate() {
        pd = new ProgressDialog(this);
        pd.setMessage("Updating");
        pd.show();

        if (uri != null) {
            final StorageReference _storageRef = storageRef.child(System.currentTimeMillis()
                    + "." + getFileExtension(uri));

            uTask = _storageRef.putFile(uri);
            uTask.continueWithTask(task -> {
                if (!task.isSuccessful()) throw task.getException();
                return _storageRef.getDownloadUrl();

            }).addOnSuccessListener(uri -> {

                if (uri != null) s_update_image = uri.toString();

                DatabaseReference databaseRef = firebaseDatabase.getReference("Updates");

                final String s_user_id = firebaseAuth.getCurrentUser().getUid();
                final String s_update_id = databaseRef.push().getKey();
                final String s_update_content = et_update_content.getText().toString();

//                    TODO: FOUND ISSUE: UPDATE THE DETAILS
                HashMap<String, Object> update = new HashMap<>();
                update.put("updateid", s_update_id);
                update.put("updateimage", s_update_image);
                update.put("subject", s_update_content);
                update.put("source", s_user_id);

                if (s_update_id != null)

                    databaseRef
                            .child(s_update_id)
                            .setValue(update)
                            .addOnSuccessListener(runnable -> {
                                pd.dismiss();
                                startActivity(new Intent(this, HomeActivity.class));
                                finish();
                            });

            });
        } else Toast.makeText(this, "No Image Selected!", Toast.LENGTH_SHORT).show();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            uri = result.getUri();
            iv_update_image.setImageURI(uri);
        } else {
            Toast.makeText(this, "Something gone wrong!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        }
    }
}