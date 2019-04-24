package in.altilogic.prayogeek.activities;

public class TutorialRemoteActivity extends RemoteScreenActivity {
    @Override
    public void onStart() {
        super.onStart();
        downloadRemoteScreen("Tutorials");
    }
}
