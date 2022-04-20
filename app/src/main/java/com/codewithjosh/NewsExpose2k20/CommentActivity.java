package com.codewithjosh.NewsExpose2k20;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codewithjosh.NewsExpose2k20.adapters.CommentAdapter;
import com.codewithjosh.NewsExpose2k20.models.CommentModel;
import com.codewithjosh.NewsExpose2k20.models.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CommentActivity extends AppCompatActivity {

    EditText add_comment;
    ImageView image_profile, send, back;

    String updateid;
    String userid;
    FirebaseUser firebaseUser;
    private RecyclerView recyclerView;
    private CommentAdapter commentAdapter;
    private List<CommentModel> commentList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        add_comment = findViewById(R.id.add_comment);
        image_profile = findViewById(R.id.image_profile);
        send = findViewById(R.id.send);
        back = findViewById(R.id.back);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(CommentActivity.this, MainActivity.class));
            }
        });

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        getImage();

        Intent intent = getIntent();
        updateid = intent.getStringExtra("updateid");
        userid = intent.getStringExtra("userid");

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (add_comment.getText().toString().equals("")) {
                    Toast.makeText(CommentActivity.this, "You can't send empty comment", Toast.LENGTH_SHORT).show();
                } else {
                    add_comment();
                }
            }
        });

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true); // false
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        commentList = new ArrayList<>();
        commentAdapter = new CommentAdapter(this, commentList);
        recyclerView.setAdapter(commentAdapter);

        readComments();

    }

    private void getImage() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserModel userModel = dataSnapshot.getValue(UserModel.class);
                Glide.with(getApplicationContext()).load(userModel.getImageurl()).into(image_profile);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void add_comment() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Comments").child(updateid);

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("comment", add_comment.getText().toString().trim());
        hashMap.put("userid", firebaseUser.getUid());

        reference.push().setValue(hashMap);
        add_comment.setText("");
    }

    private void readComments() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Comments").child(updateid);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                commentList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    CommentModel commentModel = snapshot.getValue(CommentModel.class);
                    commentList.add(commentModel);
                }

                commentAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}