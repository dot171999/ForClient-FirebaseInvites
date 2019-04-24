package in.altilogic.prayogeek.activities;

public class UserGuideActivity extends RemoteScreenActivity {
    @Override
    public void onStart() {
        super.onStart();
        downloadRemoteScreen("UserGuide");
    }
}
