package com.tttv.thiendinh.breakroid.Fragment;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.tttv.thiendinh.breakroid.Model.User;
import com.tttv.thiendinh.breakroid.QuizActivity;
import com.tttv.thiendinh.breakroid.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;

public class HomeFragment extends Fragment implements RewardedVideoAdListener {
    private @NonNull
    FirebaseAuth mAuth;
    private @NonNull
    FirebaseUser firebaseUser;
    private @NonNull
    DatabaseReference myRef;

    private Button btn_start;
    private TextView txt_stars;
    private ImageView img_stars;

    private String id;
    private RewardedVideoAd mRewardedVideoAd;
    private String formattedDate;
    private boolean isFinishedCurrentDate;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mAuth = FirebaseAuth.getInstance();
        try {
            String android_id = Settings.Secure.getString(getActivity().getContentResolver(), Settings.Secure.ANDROID_ID).toUpperCase();
            MobileAds.initialize(this.getActivity(), "ca-app-pub-3940256099942544/6300978111");
            mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this.getActivity());
            mRewardedVideoAd.setRewardedVideoAdListener(this);
            mRewardedVideoAd.loadAd("ca-app-pub-3940256099942544/5224354917",
                    new AdRequest.Builder().addTestDevice(AdRequest.DEVICE_ID_EMULATOR).addTestDevice(android_id).build());
        } catch (Exception ex) {
            toast("Cannot load ad");
        }
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        isFinishedCurrentDate = false;
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        formattedDate = df.format(c);

        btn_start = view.findViewById(R.id.btn_start);
        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mRewardedVideoAd.isLoaded()&&(isFinishedCurrentDate||id==null))
                    mRewardedVideoAd.show();
                else start();
            }
        });
        txt_stars = view.findViewById(R.id.txt_stars);
        img_stars = view.findViewById(R.id.img_star);
        img_stars.setVisibility(View.GONE);

        try {
            firebaseUser = mAuth.getCurrentUser();
            final @NonNull FirebaseDatabase database = FirebaseDatabase.getInstance();
            myRef = database.getReference();
            if (firebaseUser != null && isNetworkAvailable()) {
                id = firebaseUser.getEmail();
                init();
            }
            if (isLoggedInFacebook() && isNetworkAvailable()) {
                getFbInfo();
            }
        } catch (Exception ex) {
            toast("Cannot load data!");
        }
    }

    private void init() {
        if (id == null) return;
        myRef.child("users/" + formatEmail(id) + "/" + User.STARS).addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    int stars = dataSnapshot.getValue(Integer.class);
                    txt_stars.setText(stars + "");
                    img_stars.setVisibility(View.VISIBLE);
                } else {
                    initialStars();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                toast("Updating is canceled!");
            }
        });
        myRef.child("users/" + formatEmail(id) + "/record/" + formattedDate).addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists())
                    isFinishedCurrentDate = true;
            }

            @Override
            public void onCancelled(DatabaseError error) {
                toast("Updating is canceled!");
            }
        });
    }

    private void start() {
        if (isNetworkAvailable()) {
            Intent intent = new Intent(getActivity(), QuizActivity.class);
            startActivity(intent);
        } else toast("Please check the network!");
    }

    private void toast(String text) {
        Toast.makeText(getActivity(), text, Toast.LENGTH_LONG).show();
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

                            if (object.has("email")) {
                                email = object.getString("email");
                                i = email;
                            }
                            id = i;
                            init();
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

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
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

    private void initialStars() {
        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        String email = firebaseUser.getEmail();
        DatabaseReference myRef = database.getReference("users/" + formatEmail(email));

        myRef.child(User.STARS).setValue(0);
    }

    @Override
    public void onRewardedVideoAdLoaded() {
    }

    @Override
    public void onRewardedVideoAdOpened() {
    }

    @Override
    public void onRewardedVideoStarted() {
    }

    @Override
    public void onRewardedVideoAdClosed() {
        start();
    }

    @Override
    public void onRewarded(RewardItem rewardItem) {
    }

    @Override
    public void onRewardedVideoAdLeftApplication() {
    }

    @Override
    public void onRewardedVideoAdFailedToLoad(int i) {
        toast("Failed to load ads:" + i);
    }

    @Override
    public void onRewardedVideoCompleted() {
    }

    @Override
    public void onResume() {
        mRewardedVideoAd.resume(getActivity());
        super.onResume();
    }

    @Override
    public void onPause() {
        mRewardedVideoAd.pause(getActivity());
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mRewardedVideoAd.destroy(getActivity());
        super.onDestroy();
    }
}
