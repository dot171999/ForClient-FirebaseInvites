package in.altilogic.prayogeek.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(savedInstanceState == null)
        {
            super.onCreate(savedInstanceState);

//            try{
//                Thread.sleep(1000);
//            }
//            catch (Exception e) {
//                e.printStackTrace();
//            }
        }
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}

