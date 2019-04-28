package in.altilogic.prayogeek.activities;

import android.os.Bundle;

public class UserGuideActivity extends RemoteScreenActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        findRemoteScreen("UserGuide");
    }
}
