package com.example.gekpoh.boliao;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import static android.app.Activity.RESULT_OK;

public class EditEventFragment2 extends Fragment {

    private static final int RC_PHOTO_PICKER = 1;
    private String tempId;
    private Uri photoUri;
    private boolean activityCreated = false;

    private ImageView imageViewActivityPic;
    private EditText editTextNumPeople;
    private EditText editTextDescription;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mGroupsDatabaseReference;
    private FirebaseStorage mFirebaseStorage;
    private StorageReference mProfilePicStorageReference;

    private String groupId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.create_new_event_fragment_layout2, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        Bundle args = getArguments();
        groupId = args.getString("groupId");

        mFirebaseDatabase = FirebaseDatabaseUtils.getDatabase();
        mGroupsDatabaseReference = mFirebaseDatabase.getReference().child("groups").child("temppics");
        mFirebaseStorage = FirebaseStorage.getInstance();
        mProfilePicStorageReference = mFirebaseStorage.getReference().child("groupprofilepics");

        imageViewActivityPic = getView().findViewById(R.id.imageViewActivityPic);
        editTextNumPeople = getView().findViewById(R.id.editTextNumPeople);
        editTextDescription = getView().findViewById(R.id.editTextDescription);

        imageViewActivityPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final PopupMenu popupMenu = new PopupMenu(getActivity(), v);
                popupMenu.getMenuInflater().inflate(R.menu.menu_editprofilepic, popupMenu.getMenu());

                if (tempId == null) {
                    Menu menu = popupMenu.getMenu();
                    menu.findItem(R.id.remove_pic).setEnabled(false);
                }

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.remove_pic:
                                deletePic();
                                Toast.makeText(getActivity(), "Activity pic removed", Toast.LENGTH_SHORT).show();
                                imageViewActivityPic.setImageResource(R.drawable.profilepic);
                                return true;
                            case R.id.update_pic:
                                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                                intent.setType("image/jpeg");
                                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                                startActivityForResult(Intent.createChooser(intent, "Complete action using"), RC_PHOTO_PICKER);
                                return true;
                            default:
                                return false;
                        }
                    }
                });

                popupMenu.show();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_PHOTO_PICKER) {
            if (resultCode == RESULT_OK) {

                // Delete previous file in Firebase Storage
                if (tempId != null) {
                    deletePic();
                }

                // Upload new file in Firebase Storage
                Uri selectedImageUri = data.getData();
                final StorageReference photoRef = mProfilePicStorageReference.child(selectedImageUri.getLastPathSegment());
                UploadTask uploadTask = photoRef.putFile(selectedImageUri);

                Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }

                        // Continue with the task to get the download URL
                        return photoRef.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            photoUri = task.getResult();
                            tempId = mGroupsDatabaseReference.push().getKey();
                            mGroupsDatabaseReference.child(tempId).setValue(photoUri.toString());
                            Toast.makeText(getActivity(), "Activity pic updated", Toast.LENGTH_SHORT).show();

                            Glide.with(imageViewActivityPic.getContext())
                                    .load(photoUri.toString())
                                    .into(imageViewActivityPic);
                        }
                        else {
                            Toast.makeText(getActivity(), "Upload failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }
    }

    private void deletePic() {
        // Delete Firebase Database entry
        mGroupsDatabaseReference.child(tempId).removeValue();

        // Delete Firebase Storage entry
        final StorageReference photoDeleteRef = mFirebaseStorage.getReferenceFromUrl(photoUri.toString());

        photoDeleteRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {

            }
        });

        tempId = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (tempId != null && activityCreated == false) {   // in case user exits app after uploading photo but not creating activity
            deletePic();
            imageViewActivityPic.setImageResource(R.drawable.profilepic);
        }
    }

    public String sendNumPeople() {return editTextNumPeople.getText().toString(); }
    public String sendDescription() { return editTextDescription.getText().toString(); }
    public String sendPhotoUri() {
        if (tempId == null) return null;
        else {
            mGroupsDatabaseReference.child(tempId).removeValue();   // delete the temp entry
            activityCreated = true;  // so that storage ref won't be deleted thru onDestroy
            return photoUri.toString();
        }
    }
}