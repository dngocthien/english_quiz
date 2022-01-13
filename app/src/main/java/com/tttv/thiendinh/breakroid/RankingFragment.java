package com.tttv.thiendinh.breakroid;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

public class RankingFragment extends Fragment {
    private ArrayList<User> userArrayList;

    private ListView listview_ranking;

    private boolean isUpdated;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_ranking, container, false);
        isUpdated = false;

        listview_ranking = (ListView) rootView.findViewById(R.id.listview_ranking);

        if (isNetworkAvailable()) {
            @NonNull FirebaseDatabase database = FirebaseDatabase.getInstance();
            @NonNull DatabaseReference myRef = database.getReference();
            myRef.child("users").orderByChild("stars").limitToLast(30).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (!isUpdated) {
                        isUpdated = true;
                        userArrayList = new ArrayList<>();
                        for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                            User user = new User();
                            user.setMy_name(childSnapshot.child(User.SAVE_NAME).getValue(String.class));
                            if (childSnapshot.child(User.SAVE_AVATAR) != null)
                                user.setMy_avatar(childSnapshot.child(User.SAVE_AVATAR).getValue(String.class));
                            else user.setMy_avatar("");
                            if (childSnapshot.child(User.STARS).exists()) {
                                user.setStars(childSnapshot.child(User.STARS).getValue(Integer.class));
                                userArrayList.add(user);
                            }
                        }
                        Collections.reverse(userArrayList);
                        ArrayAdapter adapter = new RankingListViewAdapter(userArrayList, getActivity());
                        listview_ranking.setAdapter(adapter);
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    toast("Updating is canceled!");
                }
            });
        }

        listview_ranking = rootView.findViewById(R.id.listview_ranking);
        return rootView;

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
}
