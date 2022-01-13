package com.tttv.thiendinh.breakroid;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
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
import com.tttv.thiendinh.breakroid.Model.Question;
import com.tttv.thiendinh.breakroid.Model.User;
import com.tttv.thiendinh.breakroid.dao.QuizContract;
import com.tttv.thiendinh.breakroid.dao.QuizDbHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Random;
import java.util.regex.Pattern;

public class QuizActivity extends AppCompatActivity {
    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String KEY_COUNTER = "keyCounter";
    private static final long COUNTDOWN = 20000;

    private static final String RESTORE_SCORE = "restoreScore";
    private static final String RESTORE_COUNTER = "restoreCounter";
    private static final String RESTORE_REMAINTIME = "restoreRemainTime";
    private static final String RESTORE_QUESTIONS = "restoreQuestions";
    private static final String RESTORE_ANSWERD = "restoreAnswered";

    private TextView txt_score, txt_iterator, txt_remainTime, txt_question;
    private Button btn_confirm;
    private RadioButton r_option1, r_option2, r_option3, r_option4;
    private RadioGroup r_group;
    private ProgressBar mProgressCircle;

    private ColorStateList defaultColorRadio, defaultColorCountdown;

    private ArrayList<Question> questionsList;
    private DatabaseReference myRef;
    private FirebaseDatabase database;
    private QuizDbHelper helper;

    private int total, counter;
    private Question current;
    private int score;
    private CountDownTimer countDownTimer;
    private long remainTime;
    private String email;
    private boolean state, isFinishedCurrentDate, answered, isUpdated;
    private int stars;
    private String formattedDate, id;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);
        helper = QuizDbHelper.getInstance(this);

        txt_score = findViewById(R.id.score);
        txt_iterator = findViewById(R.id.iterator);
        txt_remainTime = findViewById(R.id.remainTime);
        txt_question = findViewById(R.id.question);

        btn_confirm = findViewById(R.id.btn_confirm);

        r_option1 = findViewById(R.id.option1);
        r_option2 = findViewById(R.id.option2);
        r_option3 = findViewById(R.id.option3);
        r_option4 = findViewById(R.id.option4);
        r_group = findViewById(R.id.r_group);
        mProgressCircle = findViewById(R.id.progress_circle);

        defaultColorRadio = r_option1.getTextColors();
        defaultColorCountdown = txt_remainTime.getTextColors();
        isUpdated = false;
        state = false;
        isFinishedCurrentDate = false;
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        formattedDate = df.format(c);

        SharedPreferences prefs = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        final int savedCounter = prefs.getInt(KEY_COUNTER, 0);

        questionsList = new ArrayList<>();

        try {
            database = FirebaseDatabase.getInstance();
            myRef = database.getReference();
            myRef.child("questions").addValueEventListener(new ValueEventListener() {

                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (!isUpdated && savedInstanceState == null && (savedCounter < 1 || savedCounter > 5)) {
                        isUpdated = true;

                        int total = (int) dataSnapshot.getChildrenCount();
                        Random random = new Random();
                        ArrayList<Integer> rans = new ArrayList<Integer>();

                        while (rans.size() < 5) {
                            int a = random.nextInt(total);

                            if (!rans.contains(a)) {
                                rans.add(a);
                            }
                        }
                        Collections.sort(rans);

                        int i = 0, count = 0;
                        for (DataSnapshot questionSnapshot : dataSnapshot.getChildren()) {
                            if (i == rans.get(count)) {
                                Question question = questionSnapshot.getValue(Question.class);
                                questionsList.add(question);
                                count++;
                            }
                            if (count >= rans.size()) break;
                            i++;
                        }
                        initial();
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    toast("Updating is canceled!");
                }
            });
            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            FirebaseUser firebaseUser = mAuth.getCurrentUser();
            if (firebaseUser != null && isNetworkAvailable()) {
                id = firebaseUser.getEmail();
                init();
            }
            if (isLoggedInFacebook() && isNetworkAvailable()) {
                getFbInfo();
            }

        } catch (Exception ex) {
            startActivity(getIntent());
        }
        if (savedInstanceState == null && savedCounter > 0 && savedCounter < 6)
            continueLastQuestion(savedCounter);
        if (savedInstanceState != null) resume(savedInstanceState);
        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirm();
            }
        });
    }

    private void init(){
        myRef.child("users/" + formatEmail(id) + "/challenge_state").addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                state = dataSnapshot.getValue(Boolean.class);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                toast("Updating is canceled!");
            }
        });
        myRef.child("users/" + formatEmail(id) + "/" + User.STARS).addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists())
                    stars = dataSnapshot.getValue(Integer.class);
                else stars = 0;
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

    private void initial() {
        counter = 0;
        total = questionsList.size();
        Collections.shuffle(questionsList);
        nextQuestion();
        saveToLocal();
    }

    private void resume(Bundle savedInstanceState) {
        r_option1.setBackgroundResource(R.drawable.radio_style);
        r_option2.setBackgroundResource(R.drawable.radio_style);
        r_option3.setBackgroundResource(R.drawable.radio_style);
        r_option4.setBackgroundResource(R.drawable.radio_style);
        mProgressCircle.setVisibility(View.GONE);
        questionsList = savedInstanceState.getParcelableArrayList(RESTORE_QUESTIONS);
        if (questionsList == null) finish();
        total = questionsList.size();
        score = savedInstanceState.getInt(RESTORE_SCORE);
        counter = savedInstanceState.getInt(RESTORE_COUNTER);
        remainTime = savedInstanceState.getLong(RESTORE_REMAINTIME);
        answered = savedInstanceState.getBoolean(RESTORE_ANSWERD);
        current = questionsList.get(counter - 1);

        if (answered) {
            reloadTimeRemain();
            showSolution();
        } else startCountdown();
    }

    private void continueLastQuestion(int saved) {
        counter = saved;
        getAllQuestions();
        total = questionsList.size();
        nextQuestion();
    }

    public void getAllQuestions() {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.beginTransaction();
        Cursor c = db.rawQuery("SELECT * FROM " + QuizContract.QuestionsTable.TABLE_NAME, null);

        if (c.moveToFirst()) {
            do {
                Question question = new Question();
                question.setId(c.getString(c.getColumnIndex(QuizContract.QuestionsTable.COLUMN_FIRE)));
                question.setQ(c.getString(c.getColumnIndex(QuizContract.QuestionsTable.COLUMN_QUESTION)));
                question.setA(c.getString(c.getColumnIndex(QuizContract.QuestionsTable.COLUMN_OPTION1)));
                question.setB(c.getString(c.getColumnIndex(QuizContract.QuestionsTable.COLUMN_OPTION2)));
                question.setC(c.getString(c.getColumnIndex(QuizContract.QuestionsTable.COLUMN_OPTION3)));
                question.setD(c.getString(c.getColumnIndex(QuizContract.QuestionsTable.COLUMN_OPTION4)));
                question.setT(c.getInt(c.getColumnIndex(QuizContract.QuestionsTable.COLUMN_ANSWER_NR)));
                questionsList.add(question);
            } while (c.moveToNext());
        }

        c.close();
    }

    private void toast(String text) {
        Toast.makeText(QuizActivity.this, text, Toast.LENGTH_LONG).show();
    }

    private void saveToLocal() {
        helper = QuizDbHelper.getInstance(this);
        SQLiteDatabase db = helper.getWritableDatabase();
        String clearDBQuery = "DELETE FROM " + QuizContract.QuestionsTable.TABLE_NAME;
        db.execSQL(clearDBQuery);
        for (Question question : this.questionsList) {
            addQuestion(question);
        }
    }

    private void addQuestion(Question question) {
        SQLiteDatabase db = helper.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(QuizContract.QuestionsTable.COLUMN_FIRE, question.getId());
        cv.put(QuizContract.QuestionsTable.COLUMN_QUESTION, question.getQ());
        cv.put(QuizContract.QuestionsTable.COLUMN_OPTION1, question.getA());
        cv.put(QuizContract.QuestionsTable.COLUMN_OPTION2, question.getB());
        cv.put(QuizContract.QuestionsTable.COLUMN_OPTION3, question.getC());
        cv.put(QuizContract.QuestionsTable.COLUMN_OPTION4, question.getD());
        cv.put(QuizContract.QuestionsTable.COLUMN_ANSWER_NR, question.getT());

        db.insert(QuizContract.QuestionsTable.TABLE_NAME, null, cv);
    }

    private void nextQuestion() {
        mProgressCircle.setVisibility(View.GONE);
        r_option1.setBackgroundResource(R.drawable.radio_style);
        r_option2.setBackgroundResource(R.drawable.radio_style);
        r_option3.setBackgroundResource(R.drawable.radio_style);
        r_option4.setBackgroundResource(R.drawable.radio_style);
        r_group.clearCheck();
        txt_remainTime.setTextColor(defaultColorCountdown);

        if (counter < total) {
            current = questionsList.get(counter);
            txt_question.setText(current.getQ());
            r_option1.setText(current.getA());
            r_option2.setText(current.getB());
            r_option3.setText(current.getC());
            r_option4.setText(current.getD());

            counter++;
            answered = false;
            txt_iterator.setText("Question: " + counter + "/" + total);
            btn_confirm.setText("Confirm");

            remainTime = COUNTDOWN;
            startCountdown();
            saveCurrentQuestion(counter - 1);
        } else {
            saveCurrentQuestion(0);
            if (isNetworkAvailable() && id != null && !isFinishedCurrentDate)
                finishTest();
            else finish();
        }
    }

    private void saveCurrentQuestion(int saved) {
        SharedPreferences prefs = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_COUNTER, saved);
        editor.apply();
    }

    private void confirm() {
        if (!answered && remainTime > 0) {
            if (r_option1.isChecked() || r_option2.isChecked() || r_option3.isChecked() || r_option4.isChecked()) {
                checkAnswer();
            } else {
                toast("Please select answer");
            }
        } else {
            nextQuestion();
        }
    }

    private void checkAnswer() {
        countDownTimer.cancel();
        answered = true;
        RadioButton selected = findViewById(r_group.getCheckedRadioButtonId());
        int selectedIndex = r_group.indexOfChild(selected) + 1;

        if (selectedIndex == current.getT()) {
            score++;
            txt_score.setText("Score: " + score);
        }
        showSolution();
    }

    private void showSolution() {
        RadioButton selected = findViewById(r_group.getCheckedRadioButtonId());
        int selectedIndex = r_group.indexOfChild(selected) + 1;
        if (selectedIndex == current.getT()) {
            switch (selectedIndex) {
                case 1:
                    r_option1.setBackgroundResource(R.drawable.button_correct_style);
                    break;
                case 2:
                    r_option2.setBackgroundResource(R.drawable.button_correct_style);
                    break;
                case 3:
                    r_option3.setBackgroundResource(R.drawable.button_correct_style);
                    break;
                case 4:
                    r_option4.setBackgroundResource(R.drawable.button_correct_style);
                    break;
            }
        } else {
            switch (selectedIndex) {
                case 1:
                    r_option1.setBackgroundResource(R.drawable.button_wrong_style);
                    break;
                case 2:
                    r_option2.setBackgroundResource(R.drawable.button_wrong_style);
                    break;
                case 3:
                    r_option3.setBackgroundResource(R.drawable.button_wrong_style);
                    break;
                case 4:
                    r_option4.setBackgroundResource(R.drawable.button_wrong_style);
                    break;
            }
        }

        if (counter < total) {
            btn_confirm.setText("Next >");
        } else btn_confirm.setText("Finish");
    }

    private void startCountdown() {
        countDownTimer = new CountDownTimer(remainTime, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                remainTime = millisUntilFinished;
                reloadTimeRemain();
            }

            @Override
            public void onFinish() {
                remainTime = 0;
                reloadTimeRemain();
            }
        }.start();
    }

    private void reloadTimeRemain() {
        txt_remainTime.setText(remainTime / 60000 + ":" + (remainTime / 1000) % 60);
        if (remainTime < 10000) {
            txt_remainTime.setTextColor(Color.RED);
        }
        if (remainTime < 1) {
            if (counter > 4) btn_confirm.setText("Finish");
            else
                btn_confirm.setText("Next >");
        }
    }

    private void finishTest() {
        if (state == true) {
            stars += score * 10 - (5 - score) * 20;
        } else stars += score;

        DatabaseReference recordRef = database.getReference("users/" + formatEmail(id) + "/record");
        recordRef.child(formattedDate).setValue(score);
        DatabaseReference starsRef = database.getReference("users/" + formatEmail(id) + "/" + User.STARS);
        starsRef.setValue(stars);
        finish();
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(RESTORE_SCORE, score);
        outState.putInt(RESTORE_COUNTER, counter);
        outState.putLong(RESTORE_REMAINTIME, remainTime);
        outState.putParcelableArrayList(RESTORE_QUESTIONS, questionsList);
        outState.putBoolean(RESTORE_ANSWERD, answered);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
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

}
