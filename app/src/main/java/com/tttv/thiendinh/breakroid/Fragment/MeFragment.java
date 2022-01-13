package com.tttv.thiendinh.breakroid.Fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.tttv.thiendinh.breakroid.Model.User;
import com.tttv.thiendinh.breakroid.PostQuestionActivity;
import com.tttv.thiendinh.breakroid.R;
import com.tttv.thiendinh.breakroid.RemindActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Pattern;

import static android.app.Activity.RESULT_OK;
import static com.facebook.FacebookSdk.getApplicationContext;

public class MeFragment extends Fragment implements View.OnClickListener {
    private final int PICK_IMAGE_REQUEST = 71;

    private static final int RC_SIGN_IN = 101;

    private @NonNull
    GoogleSignInClient mGoogleSignInClient;
    private @NonNull
    CallbackManager mCallbackManager;
    private @NonNull
    FirebaseAuth mAuth;
    private SignInButton btn_login;
    private LoginButton btn_facebook;

    private @NonNull
    FirebaseStorage storage;
    private @NonNull
    StorageReference storageReference;
    private @NonNull
    FirebaseUser firebaseUser;
    private @NonNull
    FirebaseDatabase database;

    private TextView txt_uname, btn_logout, btn_postQuestion, btn_feedback, btn_remind;
    private Switch sw_chalange;
    private ImageView avatar, img_noUser;
    private Uri filePath;
    private Button btn_upload;
    private View row_challenge, login_buttons;

    private String id;
    private String fb_url;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mCallbackManager = CallbackManager.Factory.create();
        FacebookSdk.sdkInitialize(getApplicationContext());
        return inflater.inflate(R.layout.fragment_me, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        storage = FirebaseStorage.getInstance();
        btn_login = (SignInButton) view.findViewById(R.id.btn_google_login);
        btn_login.setOnClickListener(this);
        sw_chalange = getActivity().findViewById(R.id.btn_challenge);
        sw_chalange.setOnClickListener(this);
        btn_upload = view.findViewById(R.id.btn_upload);
        btn_upload.setOnClickListener(this);
        btn_upload.setVisibility(View.GONE);
        txt_uname = view.findViewById(R.id.uname);
        btn_logout = view.findViewById(R.id.btn_logout);
        btn_logout.setOnClickListener(this);
        btn_logout.setVisibility(View.GONE);
        btn_postQuestion = view.findViewById(R.id.btn_version);
        btn_postQuestion.setOnClickListener(this);
        btn_postQuestion.setVisibility(View.GONE);
        btn_feedback = view.findViewById(R.id.btn_feedback);
        btn_feedback.setOnClickListener(this);
        btn_feedback.setVisibility(View.GONE);
        btn_remind=view.findViewById(R.id.btn_remind);
        btn_remind.setOnClickListener(this);
        btn_remind.setVisibility(View.GONE);
        avatar = view.findViewById(R.id.avatar);
        avatar.setOnClickListener(this);
        avatar.setVisibility(View.GONE);
        img_noUser = view.findViewById(R.id.img_noUser);
        img_noUser.setVisibility(View.GONE);
        row_challenge = (View) view.findViewById(R.id.row_challenge);
        row_challenge.setVisibility(View.GONE);
        login_buttons = view.findViewById(R.id.loginButtons);
        login_buttons.setVisibility(View.GONE);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(getActivity(), gso);

        btn_facebook = getActivity().findViewById(R.id.buttonFacebookLogin);
        btn_facebook.setFragment(this);
        btn_facebook.setReadPermissions(Arrays.asList("email", "public_profile"));

        try {
            mAuth = FirebaseAuth.getInstance();
            firebaseUser = mAuth.getCurrentUser();
            if (firebaseUser != null && isNetworkAvailable()) {
                id = firebaseUser.getEmail();
                loginGSuccessed();
            }
            if (isLoggedInFacebook() && isNetworkAvailable()) {
                getFbInfo();
            }
        } catch (Exception ex) {
            toast("Cannot load data!");
        }

        btn_facebook.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
//                handleFacebookAccessToken(loginResult.getAccessToken());
                getFbInfo();
            }

            @Override
            public void onCancel() {
                LoginManager.getInstance().logOut();
                toast("Canceled logging in with Facebook");
            }

            @Override
            public void onError(FacebookException error) {
                toast("An error occured!");
            }
        });

        if (id == null && !isLoggedInFacebook()) {
            login_buttons.setVisibility(View.VISIBLE);
            img_noUser.setVisibility(View.VISIBLE);
            txt_uname.setText("Login");
        }
    }

    private void init() {
        if (id != null && isNetworkAvailable()) {
            database = FirebaseDatabase.getInstance();
            DatabaseReference myRef = database.getReference();
            myRef.child("users/" + formatEmail(id) + "/challenge_state").addValueEventListener(new ValueEventListener() {

                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    sw_chalange.setChecked(dataSnapshot.getValue(Boolean.class));
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    toast("Updating is canceled!");
                }
            });
            myRef.child("users/" + formatEmail(id) + "/" + User.SAVE_AVATAR).addValueEventListener(new ValueEventListener() {

                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue(String.class)!=null)
                        Picasso.get().load(dataSnapshot.getValue(String.class)).into(avatar);
                    else {
                        Picasso.get().load(R.drawable.ic_person_black_24dp).into(avatar);
                        if (firebaseUser != null)
                            saveUserAvatar(firebaseUser.getPhotoUrl().toString());
                        if (fb_url != null) saveUserAvatar(fb_url);
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    toast("Updating is canceled!");
                }
            });
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
            }
        }

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), filePath);
                avatar.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_google_login:
                signInG();
                break;
            case R.id.btn_logout:
                logout();
                break;
            case R.id.btn_challenge:
                saveChallengeState();
                break;
            case R.id.btn_version:
                postQuestion();
                break;
            case R.id.avatar:
                changeAvatar();
                break;
            case R.id.btn_upload:
                uploadImage();
                break;
            case R.id.btn_feedback:
                feedback();
                break;
            case R.id.btn_remind:
                remind();
                break;
        }
    }

    public boolean isLoggedInFacebook() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        return accessToken != null;
    }

    private void getFbInfo() {
        GraphRequest request = GraphRequest.newMeRequest(
                AccessToken.getCurrentAccessToken(),
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(
                            JSONObject object,
                            GraphResponse response) {
                        try {

                            String i = object.getString("id");
                            String email = "";
                            String first_name = "";
                            String last_name = "";
                            String image_url = "";

                            if (object.has("email")) {
                                email = object.getString("email");
                                i = email;
                            }
                            if (object.has("first_name")) {
                                first_name = object.getString("first_name");
                            }
                            if (object.has("last_name")) {
                                last_name = object.getString("last_name");
                            }
                            if (object.has("image_url")) {
                                image_url = object.getString("http://graph.facebook.com/" + id + "/picture?type=large");
                                fb_url = image_url;
                            }
                            id = i;
                            saveFbUser(i, email, first_name, last_name);
                            loginFbSuccessed(first_name + " " + last_name);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,first_name,last_name,email");
        request.setParameters(parameters);
        request.executeAsync();
    }

    private void saveFbUser(String i, String email, String firstName, String lastName) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("users/" + formatEmail(i));

        myRef.child(User.SAVE_MAIL).setValue(email);
        myRef.child(User.SAVE_NAME).setValue(firstName + " " + lastName);
    }

    private void loginFbSuccessed(String name) {
        init();
        txt_uname.setText(name);
        avatar.setVisibility(View.VISIBLE);
        img_noUser.setVisibility(View.GONE);
        row_challenge.setVisibility(View.VISIBLE);
        btn_postQuestion.setVisibility(View.VISIBLE);
        btn_feedback.setVisibility(View.VISIBLE);
        btn_remind.setVisibility(View.VISIBLE);
        btn_logout.setVisibility(View.VISIBLE);

        login_buttons.setVisibility(View.GONE);
    }

    private void signInG() {
        if (isNetworkAvailable()) {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        } else toast("Please check the network!");
    }

    private void logout() {
        mAuth.signOut();
        LoginManager.getInstance().logOut();
        logoutGSuccessed();
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            loginGSuccessed();
                            saveGUser();
                        } else {
                            toast("Could not log in with Google");
                        }
                    }
                });
    }

    private void loginGSuccessed() {
        init();
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        txt_uname.setText(firebaseUser.getDisplayName());
        avatar.setVisibility(View.VISIBLE);
        img_noUser.setVisibility(View.GONE);
        row_challenge.setVisibility(View.VISIBLE);
        btn_postQuestion.setVisibility(View.VISIBLE);
        btn_feedback.setVisibility(View.VISIBLE);
        btn_remind.setVisibility(View.VISIBLE);
        btn_logout.setVisibility(View.VISIBLE);

        login_buttons.setVisibility(View.GONE);
    }

    private void logoutGSuccessed() {
        txt_uname.setText("Login");
        avatar.setVisibility(View.GONE);
        img_noUser.setVisibility(View.VISIBLE);
        row_challenge.setVisibility(View.GONE);
        btn_postQuestion.setVisibility(View.GONE);
        btn_feedback.setVisibility(View.GONE);
        btn_remind.setVisibility(View.GONE);
        btn_logout.setVisibility(View.GONE);

        login_buttons.setVisibility(View.VISIBLE);
    }

    private void saveGUser() {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        String email = firebaseUser.getEmail();
        DatabaseReference myRef = database.getReference("users/" + formatEmail(email));

        myRef.child(User.SAVE_MAIL).setValue(email);
        myRef.child(User.SAVE_NAME).setValue(firebaseUser.getDisplayName());
        myRef.child(User.SAVE_PHONE).setValue(firebaseUser.getPhoneNumber());
    }

    private void saveUserAvatar(String url) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("users/" + formatEmail(id));

        myRef.child(User.SAVE_AVATAR).setValue(url);
    }

    private String formatEmail(String mail) {
        mail = mail.substring(0, mail.lastIndexOf("."));
        mail = mail.replaceAll(Pattern.quote("."), "");
        mail = mail.replaceAll(Pattern.quote("#"), "");
        mail = mail.replaceAll(Pattern.quote("$"), "");
        mail = mail.replaceAll(Pattern.quote("["), "");
        mail = mail.replaceAll(Pattern.quote("]"), "");
        return mail;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void toast(String text) {
        Toast.makeText(getActivity(), text, Toast.LENGTH_LONG).show();
    }


    private void changeAvatar() {
        Intent intent = new Intent();
        intent.setType("circle/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        btn_upload.setVisibility(View.VISIBLE);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    private void uploadImage() {
        if (id != null && isNetworkAvailable()) {
            if (filePath != null) {
                final ProgressDialog progressDialog = new ProgressDialog(getContext());
                progressDialog.setTitle("Uploading...");
                progressDialog.show();
                storageReference = storage.getReference();
                final StorageReference ref = storageReference.child("avatar/" + formatEmail(id) + ".png");
                ref.putFile(filePath)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                progressDialog.dismiss();
                                toast("Uploaded");
                                ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        saveUserAvatar(uri.toString());
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception exception) {
                                        toast("Could not save avatar!");
                                    }
                                });
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                toast("Upload failed!");
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
            }
        }
        btn_upload.setVisibility(View.GONE);
    }

    private void postQuestion() {
        Intent intent = new Intent(getActivity(), PostQuestionActivity.class);
        startActivity(intent);
    }

    private void saveChallengeState() {
        if (!isNetworkAvailable()) return;
        Boolean switchState = ((Switch) sw_chalange).isChecked();
        if (switchState)
            toast("You will receive 10 star for each right answer and -20 star for each wrong answer");
        else
            toast("You will receive 1 star for each right answer and 0 star for each wrong answer");
        if (id != null) {
            DatabaseReference myRef = database.getReference("users/" + formatEmail(id) + "/");
            myRef.child("challenge_state").setValue(switchState);
        }
    }

    private void feedback() {
        final FirebaseUser firebaseUser = mAuth.getCurrentUser();
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_feedback, null);
        dialogBuilder.setView(dialogView);

        final EditText message = (EditText) dialogView.findViewById(R.id.customFeedback);

        dialogBuilder.setTitle("Feedback");
        dialogBuilder.setPositiveButton("Send", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String nameStr = firebaseUser.getDisplayName();
                String messageStr = message.getText().toString().trim();

                Intent emailIntent = new Intent(Intent.ACTION_SEND);
                emailIntent.setData(Uri.parse("mailto:"));
                emailIntent.setType("message/rfc822");
                emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"thiendinh.it.97@gmail.com"});
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Feedback Elazy");
                emailIntent.putExtra(Intent.EXTRA_TEXT, nameStr + ": " + messageStr);

                if (emailIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivity(Intent.createChooser(emailIntent, "Send Email ..."));
                } else {
                    toast("You don't have any email app");
                }

            }
        });
        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //pass
            }
        });

        AlertDialog b = dialogBuilder.create();
        b.show();
    }

    private void remind(){
        Intent intent = new Intent(getActivity(),RemindActivity.class);
        startActivity(intent);
    }
}
