package in.altilogic.prayogeek.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AutoCompleteTextView;

import in.altilogic.prayogeek.R;

public class ProfileActivity extends AppCompatActivity {
    private AutoCompleteTextView etCollege;
    private AutoCompleteTextView etSemester;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
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
