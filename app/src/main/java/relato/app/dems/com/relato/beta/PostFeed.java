package relato.app.dems.com.relato.beta;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class PostFeed extends AppCompatActivity {
    private ImageButton mPostImageSelect;
    private EditText mPostTitle;
    private EditText mPostDesciption;
    private Button mBtnAddPost;
    private Uri mImageUri = null;
    private StorageReference mStorage;
    private DatabaseReference mDatabase;
    private static final int GALLERY_REQUEST = 1;
    private ProgressDialog mProgresDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_feed);
        mStorage = FirebaseStorage.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Blog");
        mProgresDialog= new ProgressDialog(this);
        mPostImageSelect = (ImageButton) findViewById(R.id.postImageSelect);
        mPostTitle = (EditText) findViewById(R.id.postTitle);
        mPostDesciption = (EditText) findViewById(R.id.postDescription);
        mBtnAddPost = (Button) findViewById(R.id.btnAddPost);
        mPostImageSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,GALLERY_REQUEST);
            }
        });
        mBtnAddPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPosting();
            }
        });
    }

    private void startPosting(){
        mProgresDialog.setMessage("Posteando al Blog");
        mProgresDialog.show();
        final String title_val = mPostTitle.getText().toString().trim();
        final String desc_val = mPostDesciption.getText().toString().trim();
        if(!TextUtils.isEmpty(title_val) && !TextUtils.isEmpty(desc_val) && mImageUri !=null){
            final StorageReference filepath = mStorage.child("Blog_images").child(mImageUri.getLastPathSegment());
           filepath.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
               @Override
               public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                   Uri downloadUrl = taskSnapshot.getDownloadUrl();
                   DatabaseReference newPost = mDatabase.push();
                   newPost.child("title").setValue(title_val);
                   newPost.child("desc").setValue(desc_val);
                   newPost.child("image").setValue(downloadUrl.toString());
                   startActivity(new Intent(PostFeed.this,FeedRelatos.class));
                   mProgresDialog.dismiss();

               }
           });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GALLERY_REQUEST && resultCode == RESULT_OK){
            mImageUri = data.getData();
            mPostImageSelect.setImageURI(mImageUri);
        }
    }
}
