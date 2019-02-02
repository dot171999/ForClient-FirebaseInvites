package in.altilogic.prayogeek.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import in.altilogic.prayogeek.R;

public class ProfileActivity extends AppCompatActivity {
    private AutoCompleteTextView etCollege;
    private AutoCompleteTextView etSemester;

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
    }
}
