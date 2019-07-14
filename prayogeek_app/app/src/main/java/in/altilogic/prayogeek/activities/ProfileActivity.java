package in.altilogic.prayogeek.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import in.altilogic.prayogeek.Global_Var;
import in.altilogic.prayogeek.R;
import in.altilogic.prayogeek.utils.Utils;

public class ProfileActivity extends AppCompatActivity {
    private AutoCompleteTextView etCollege;
    private AutoCompleteTextView etSemester;
    private Button btnSave;
    private Button btnCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        String userName = getIntent().getStringExtra(MainActivity.PREF_USER_NAME);
        String emailId = getIntent().getStringExtra(MainActivity.PREF_EMAILID);
        TextView tv_nav_user = findViewById(R.id.tv_nav_user);
        TextView tv_nav_email = findViewById(R.id.tv_nav_email);

        if(tv_nav_user != null && userName != null)
            tv_nav_user.setText(userName);

        if(tv_nav_email != null && emailId != null)
            tv_nav_email.setText(emailId);

        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
        etCollege = findViewById(R.id.actvCollege);
        etSemester = findViewById(R.id.actvSemester);
        etSemester.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction()!=KeyEvent.ACTION_DOWN)
                    return false;
                if(keyCode == KeyEvent.KEYCODE_ENTER ){
                    String college = etCollege.getText().toString();
                    String semester = etSemester.getText().toString();
                    Log.d(MainActivity.TAG, college + " ; " + semester);
                    return true;
                }
                return false;
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkProfileInfo())
                    finishActivity();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finishActivity();
            }
        });

        String college = Utils.readSharedSetting(getApplicationContext(), Global_Var.COLLEGE_NAME, null);
        String semester = Utils.readSharedSetting(getApplicationContext(), Global_Var.SEMESTER, null);

        if(college != null)
            etCollege.setText(college);
        if(semester != null)
            etSemester.setText(semester);
    }

    private boolean checkProfileInfo() {
        String college = etCollege.getText().toString();
        String semester = etSemester.getText().toString();
        if(college == null) {
            Toast.makeText(getApplicationContext(),"Enter college name", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(semester == null) {
            Toast.makeText(getApplicationContext(),"Enter semester number", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!college.matches("[a-zA-Z]+") || college.length() <= 2) {
            Toast.makeText(getApplicationContext(),"College name must contain only characters", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!semester.matches("[1-8]+") || semester.length() != 1) {
            Toast.makeText(getApplicationContext(),"Semester must contain only numbers from 1 to 8", Toast.LENGTH_SHORT).show();
            return false;
        }

        Log.d(MainActivity.TAG, "Save College: " + college + "; semester = " + semester);
        Utils.saveSharedSetting(getApplicationContext(), Global_Var.COLLEGE_NAME, college);
        Utils.saveSharedSetting(getApplicationContext(), Global_Var.SEMESTER, semester);
        return true;
    }

    private void finishActivity(){
        if (getParent() == null) {
            setResult(RESULT_OK, new Intent());
        }
        else {
            getParent().setResult(RESULT_OK, new Intent());
        }
        Log.e("Save","save button clicked");
        finish();
    }
}
