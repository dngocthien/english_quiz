package com.tttv.thiendinh.breakroid;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Admin extends AppCompatActivity {

    Button btn_save;
    EditText et_q, et_a, et_b, et_c, et_d;
    RadioGroup radioGroup;

    private DatabaseReference myRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        et_q = findViewById(R.id.et_q);
        et_a = findViewById(R.id.et_a);
        et_b = findViewById(R.id.et_b);
        et_c = findViewById(R.id.et_c);
        et_d = findViewById(R.id.et_d);
        radioGroup = findViewById(R.id.correct);

        btn_save = findViewById(R.id.btn_save);
        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                write();
            }
        });

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef = database.getReference("questions");

    }

    private void write() {
        String q = et_q.getText().toString().trim();
        String a = et_a.getText().toString().trim();
        String b = et_b.getText().toString().trim();
        String c = et_c.getText().toString().trim();
        String d = et_d.getText().toString().trim();
        int t = radioGroup.getCheckedRadioButtonId();
        if (q.length() < 1 || a.length() < 1 || b.length() < 1 || t < 0) {
            Toast.makeText(this, "You didn't finish!", Toast.LENGTH_LONG).show();
        } else {
            String id = myRef.push().getKey();
            Question question = new Question(id, q, a, b, c, d, t);
            myRef.child(id).setValue(question);

            et_q.setText("");
            et_a.setText("");
            et_b.setText("");
            et_c.setText("");
            et_d.setText("");
            radioGroup.clearCheck();
            Toast.makeText(this, "Saved question successfully", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
