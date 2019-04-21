package in.altilogic.prayogeek.activities;

import android.os.Bundle;

public class TutorialRemoteActivity extends RemoteScreenActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        downloadRemoteScreen("Tutorials");
    }
}
