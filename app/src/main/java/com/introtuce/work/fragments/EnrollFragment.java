package com.introtuce.work.fragments;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.introtuce.work.MainActivity;
import com.introtuce.work.Model.UserModel;
import com.introtuce.work.R;
import com.introtuce.work.UsersAdapter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import pub.devrel.easypermissions.EasyPermissions;

import static android.app.Activity.RESULT_OK;

public class EnrollFragment extends Fragment {

    private Button addUserBtn;
    private DatabaseReference databaseReference;
    private LinearLayout selectProfilePic;
    private EditText inputFirstName, inputLastName, inputGender, inputCountry, inputState, inputHomeTown, inputPhoneNumber, inputTelephone;
    private ImageView profilePic;
    private Uri filePath;
    private Button inputDOB;
    private String dob = "";
    UserModel userModel = new UserModel();
    private List<String> numbers = new ArrayList<>();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_enroll, container, false);
        init(view);
        String[] perms = {Manifest.permission.READ_EXTERNAL_STORAGE};
        if (!EasyPermissions.hasPermissions(getContext(), perms)) {
            EasyPermissions.requestPermissions(getActivity(), getString(R.string.rationale_location_contacts), 100, perms);
        }

        getUsers();
        return view;
    }

    private void getUsers() {
        for (UserModel model : UsersFragment.usersList) {
            numbers.add(model.getPhoneNumber());
        }
    }

    private void init(View view) {
        databaseReference = FirebaseDatabase.getInstance().getReference();
        addUserBtn = view.findViewById(R.id.add_user_btn);
        inputFirstName = view.findViewById(R.id.first_name);
        inputLastName = view.findViewById(R.id.last_name);
        inputCountry = view.findViewById(R.id.country);
        inputDOB = view.findViewById(R.id.dob);
        inputGender = view.findViewById(R.id.gender);
        inputHomeTown = view.findViewById(R.id.home_towm);
        inputState = view.findViewById(R.id.state);
        inputPhoneNumber = view.findViewById(R.id.phone_number);
        inputTelephone = view.findViewById(R.id.telephone);
        selectProfilePic = view.findViewById(R.id.select_photo_layout);
        profilePic = view.findViewById(R.id.profile_image);

        selectProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_PICK);
                i.setType("image/*");
                startActivityForResult(i, 200);
            }
        });

        addUserBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                uploadImage();
                validate();

            }
        });

        inputDOB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Calendar calendar = Calendar.getInstance();
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                int month = calendar.get(Calendar.MONTH);
                int year = calendar.get(Calendar.YEAR);
                // date picker dialog
                DatePickerDialog pickerDialog = new DatePickerDialog(getContext(),
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                inputDOB.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year);
                                dob = year + "-" + (monthOfYear + 1) + "-" + dayOfMonth;   // for YYYY-MM-DD format
                            }
                        }, year, month, day);
                pickerDialog.show();
            }
        });
    }

    private void uploadImage() {
        FirebaseStorage storage;
        StorageReference storageReference;
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        if (filePath != null) {
            final ProgressDialog progressDialog = new ProgressDialog(getContext());
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            StorageReference ref = storageReference.child("images/" + UUID.randomUUID().toString());
            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            Toast.makeText(getContext(), "Uploaded", Toast.LENGTH_SHORT).show();
                            taskSnapshot.getMetadata().getReference().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    userModel.setImage(uri.toString());
                                    userModel.setFirstName(inputFirstName.getText().toString().trim());
                                    userModel.setLastName(inputLastName.getText().toString().trim());
                                    userModel.setDob(inputDOB.getText().toString().trim());
                                    userModel.setGender(inputGender.getText().toString().trim());
                                    userModel.setCountry(inputCountry.getText().toString().trim());
                                    userModel.setPhoneNumber(inputPhoneNumber.getText().toString().trim());
                                    userModel.setHomeTown(inputHomeTown.getText().toString().trim());
                                    userModel.setState(inputState.getText().toString().trim());
                                    userModel.setTelephoneNumber(inputTelephone.getText().toString().trim());
                                    uploadData(userModel);
                                    inputCountry.getText().clear();
                                    inputDOB.setText("Date of birth");
                                    inputFirstName.getText().clear();
                                    inputLastName.getText().clear();
                                    inputGender.getText().clear();
                                    inputHomeTown.getText().clear();
                                    inputTelephone.getText().clear();
                                    inputState.getText().clear();
                                    inputPhoneNumber.getText().clear();
                                    profilePic.setImageResource(R.drawable.pic);
                                    filePath = null;
                                    dob = "";
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(getContext(), "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
                                    .getTotalByteCount());
                            progressDialog.setMessage("Uploaded " + (int) progress + "%");
                        }
                    });
        } else {
            Toast.makeText(getContext(), "Please select profile photo", Toast.LENGTH_SHORT).show();
        }
    }

    private void validate() {
        if (inputFirstName.getText().toString().isEmpty()) {
            inputFirstName.requestFocus();
            inputFirstName.setError("Please enter valid name");
        } else if (inputLastName.getText().toString().isEmpty()) {
            inputLastName.requestFocus();
            inputLastName.setError("Please enter valid last name");
        } else if (dob.isEmpty()) {
            inputDOB.requestFocus();
            inputDOB.setError("Please choose DOB");
        } else if (inputPhoneNumber.getText().toString().isEmpty()) {
            inputPhoneNumber.requestFocus();
            inputPhoneNumber.setError("Please enter valid phone number");
        } else if (inputPhoneNumber.getText().toString().length() != 10) {
            inputPhoneNumber.requestFocus();
            inputPhoneNumber.setError("Please enter valid phone number");
        } else if (inputTelephone.getText().toString().isEmpty()) {
            inputTelephone.requestFocus();
            inputTelephone.setError("Please enter valid telephone number");
        } else if (inputState.getText().toString().isEmpty()) {
            inputState.requestFocus();
            inputState.setError("Please enter state");
        } else if (inputHomeTown.getText().toString().isEmpty()) {
            inputHomeTown.requestFocus();
            inputHomeTown.setError("Please enter Home towm");
        } else if (inputCountry.getText().toString().isEmpty()) {
            inputCountry.requestFocus();
            inputCountry.setError("Please enter country");
        } else if (inputGender.getText().toString().isEmpty()) {
            inputGender.requestFocus();
            inputGender.setError("Please enter gender");
        } else if (numbers.contains(inputPhoneNumber.getText().toString())) {
            inputPhoneNumber.setError("This number already exists");
            inputPhoneNumber.requestFocus();
        } else {
            uploadImage();

        }

    }

    private void uploadData(UserModel model) {
        String id = model.getFirstName() + model.getPhoneNumber();
        databaseReference.child("users").child(id).setValue(model);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 200 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), filePath);
                profilePic.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}