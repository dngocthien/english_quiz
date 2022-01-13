package com.tttv.thiendinh.breakroid;

import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.LineChartView;

public class RecordFragment extends Fragment implements AdapterView.OnItemSelectedListener {
    private @NonNull
    FirebaseUser firebaseUser;

    private TreeMap<String, Integer> record;
    private LineChartView lineChartView;
    private ArrayList<String> axisData;
    private ArrayList<Float> yAxisData;

    private Spinner spinner, spinner_month, spinner_year;

    private boolean isUpdated;
    private String formattedDate, id;
    private Date c;
    private ArrayAdapter<String> yearAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_record, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        formattedDate = df.format(c);

        isUpdated = false;

        spinner = (Spinner) getActivity().findViewById(R.id.spinner_date);
        spinner.setOnItemSelectedListener(this);
        spinner_month = (Spinner) getActivity().findViewById(R.id.spinner_month);
        spinner_month.setOnItemSelectedListener(this);
        spinner_year = (Spinner) getActivity().findViewById(R.id.spinner_year);
        spinner_year.setOnItemSelectedListener(this);

        String[] categories = {"Day", "Month"};
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, categories);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);

        String[] months = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
        ArrayAdapter<String> monthAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, months);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_month.setAdapter(monthAdapter);

        final ArrayList<String> years = new ArrayList<>();
        int thisYear = Integer.parseInt(formattedDate.substring(0, 4));
        for (int y = 2017; y <= thisYear; y++) {
            years.add(y + "");
        }
        yearAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, years);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_year.setAdapter(yearAdapter);

        record = new TreeMap<>();

        try {
            @NonNull FirebaseAuth mAuth = FirebaseAuth.getInstance();
            firebaseUser = mAuth.getCurrentUser();
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
        if (!isNetworkAvailable()) return;
        @NonNull FirebaseDatabase database = FirebaseDatabase.getInstance();
        @NonNull DatabaseReference myRef = database.getReference();
        myRef.child("users/" + formatEmail(id) + "/record").addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!isUpdated) {
                    for (DataSnapshot recordSnapshot : dataSnapshot.getChildren()) {
                        record.put(recordSnapshot.getKey() + "", recordSnapshot.getValue(Integer.class));
                    }
                    isUpdated = true;

                    spinner_year.setSelection(yearAdapter.getPosition(formattedDate.substring(0, 4)));
                    spinner_month.setSelection(c.getMonth());
                    processData(0, formattedDate.substring(5, 7), formattedDate.substring(0, 4));
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                toast("Updating is canceled!");
            }
        });
    }

    private void processData(int type, String m, String y) {
        if (record.size() < 1) return;
        axisData = new ArrayList<>();
        yAxisData = new ArrayList<>();
        // 0: by day; 1: by month
        if (type == 1) {
            int n = 1;
            int sum = 0;
            String month = "";
            for (Map.Entry<String, Integer> entry : record.entrySet()) {
                if (entry.getKey().substring(0, 4).equals(y)) {
                    if (entry.getKey().substring(0, 7).equals(month)) {
                        n++;
                        sum += entry.getValue();
                    } else {
                        axisData.add(entry.getKey().substring(5, 7));
                        if (!month.equals(""))
                            yAxisData.add((float) sum / (float) n);
                        month = entry.getKey().substring(0, 7);
                        n = 1;
                        sum = entry.getValue();
                    }
                }
            }
            yAxisData.add((float) sum / (float) n);
        } else {
            for (Map.Entry<String, Integer> entry : record.entrySet()) {
                if (entry.getKey().substring(0, 4).equals(y)) {
                    if (entry.getKey().substring(5, 7).equals(m)) {
                        axisData.add(entry.getKey().substring(8));
                        yAxisData.add(Float.valueOf(entry.getValue()));
                    }
                }
            }
        }
        draw();
    }

    private void toast(String text) {
        Toast.makeText(getActivity(), text, Toast.LENGTH_LONG).show();
    }

    private void draw() {
        View view = getView();
        lineChartView = view.findViewById(R.id.chart);

        List yAxisValues = new ArrayList();
        List axisValues = new ArrayList();


        Line line = new Line(yAxisValues).setColor(Color.parseColor("#33aaee"));

        for (int i = 0; i < axisData.size(); i++) {
            axisValues.add(i, new AxisValue(i).setLabel(axisData.get(i)));
        }

        for (int i = 0; i < yAxisData.size(); i++) {
            yAxisValues.add(new PointValue(i, yAxisData.get(i)));
        }

        List lines = new ArrayList();
        lines.add(line);

        LineChartData data = new LineChartData();
        data.setLines(lines);

        Axis axis = new Axis();
        axis.setValues(axisValues);
        axis.setTextSize(16);
        axis.setTextColor(Color.parseColor("#777777"));
        data.setAxisXBottom(axis);

        Axis yAxis = new Axis();
        yAxis.setTextColor(Color.parseColor("#777777"));
        yAxis.setTextSize(16);
        data.setAxisYLeft(yAxis);

        lineChartView.setLineChartData(data);
        Viewport viewport = new Viewport(lineChartView.getMaximumViewport());
        viewport.top = 5;
        viewport.bottom = 0;
        lineChartView.setMaximumViewport(viewport);
        lineChartView.setCurrentViewport(viewport);
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

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (isUpdated) {
            int type = spinner.getSelectedItemPosition();
            if (type == 1)
                spinner_month.setVisibility(View.GONE);
            else spinner_month.setVisibility(View.VISIBLE);

            int m = spinner_month.getSelectedItemPosition() + 1;
            int y = Integer.parseInt(spinner_year.getSelectedItem().toString());
            processData(type, m + "", y + "");
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
