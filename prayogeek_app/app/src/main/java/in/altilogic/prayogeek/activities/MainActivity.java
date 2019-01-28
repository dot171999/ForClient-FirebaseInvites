package in.altilogic.prayogeek.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;


import in.altilogic.prayogeek.FireBaseHelper;
import in.altilogic.prayogeek.Global_Var;
import in.altilogic.prayogeek.R;
import in.altilogic.prayogeek.utils.Utils;

import java.util.Arrays;
import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener, EasyPermissions.PermissionCallbacks {

    private static final float LOCATION_REFRESH_DISTANCE = 100.f;
    private static final long LOCATION_REFRESH_TIME = 1000;
    // Firebase Authentication
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    public static final int RC_SIGN_IN = 3;

    private String mUsername;
    private String mEmailId;
    public static final String ANONYMOUS = "anonymous";

    public static final String PREF_USER_FIRST_TIME = "user_first_time";
    boolean isUserFirstTime;
    boolean isOnline = false;

    public static final String TAG = "YOUSCOPE-DB";
    private Toolbar mToolbar = null;

    private Spinner mList1, mList2;
    private TextView tvOut;
    private Button mButton1, mButton2, mButton3, mButton4;

    private TextView tvNavDrUser, tvNavDrEmail;
    private LocationManager mLocationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        firebase_auth_init();
    }

    @Override
    public void onStart() {
        super.onStart();
        isOnline = Utils.isOnline(this);
        if(!isOnline) {
            Toast.makeText(this, "Network is not available ", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mFirebaseAuth != null)
            mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out_menu:
                //sign out
                AuthUI.getInstance().signOut(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void firebase_auth_init() {
        isOnline = Utils.isOnline(this);

        mFirebaseAuth = FirebaseAuth.getInstance();

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // user is signed in
                    mUsername = user.getDisplayName();
                    mEmailId = user.getEmail();
                    Global_Var GlobalVar = (Global_Var) getApplicationContext();
                    GlobalVar.Set_Username(mUsername);
                    GlobalVar.Set_EmailId(mEmailId);
                    ui_init();

//                    FireBaseHelper fireBaseHelper = new FireBaseHelper(FirebaseDatabase.getInstance().getReference());
//                    fireBaseHelper.WriteGlobalVar(GlobalVar);
                } else {
                    // user is signed out
                    mUsername = ANONYMOUS;

                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false) // Smart lock automatically saves user credentials and logs in
                                    .setAvailableProviders(
                                            Arrays.asList(new AuthUI.IdpConfig.GoogleBuilder().build()))
                                    .build(),
                            RC_SIGN_IN);
                }
            }
        };
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RC_SIGN_IN:
                if (resultCode == RESULT_OK) {
                    Toast.makeText(this, "Signed in!", Toast.LENGTH_SHORT).show();

                    ui_init();
                } else if (resultCode == RESULT_CANCELED) {
                    if (isOnline == true) {
                        Toast.makeText(this, "Sign In Cancelled!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Turn ON Internet to Sign In!", Toast.LENGTH_SHORT).show();
                    }
                    finish();
                }
                break;
        }
    }

    private void ui_init() {
        navigation_driver_init();

        mList1 = findViewById(R.id.spList1);
        mList2 = findViewById(R.id.spList2);
        tvOut = findViewById(R.id.tv_main1);
        mButton1 = findViewById(R.id.btn_button1);
        mButton2 = findViewById(R.id.btn_button2);
        mButton3 = findViewById(R.id.btn_button3);
        mButton4 = findViewById(R.id.btn_button4);
        mButton1.setOnClickListener(this);
        mButton2.setOnClickListener(this);
        mButton3.setOnClickListener(this);
        mButton4.setOnClickListener(this);

        isUserFirstTime = Boolean.valueOf(Utils.readSharedSetting(MainActivity.this, PREF_USER_FIRST_TIME, "true"));
        if (isUserFirstTime)
            startActivity(new Intent(MainActivity.this, OnBoardingActivity.class)
                    .putExtra(PREF_USER_FIRST_TIME, isUserFirstTime));

        checkPermissions();
    }

    private void navigation_driver_init() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View headerView = navigationView.getHeaderView(0);
        tvNavDrUser = headerView.findViewById(R.id.tv_nav_user);
        tvNavDrEmail = headerView.findViewById(R.id.tv_nav_email);

        if(mUsername != null)
            tvNavDrUser.setText(mUsername);

        if(mEmailId != null)
            tvNavDrEmail.setText(mEmailId);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        Uri uri = null;

        if (id == R.id.nav_on_boarding) {
            Intent intent = new Intent(this, OnBoardingActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.nav_basic_electronic_tutorial) {
            Toast.makeText(this, "Run electronic tutorial", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_about_us) {
            uri = Uri.parse(getString(R.string.nav_link_about_us));
        } else if (id == R.id.nav_facebook) {
            uri = Uri.parse(getString(R.string.nav_link_facebook));
        } else if (id == R.id.nav_instagram) {
            uri = Uri.parse(getString(R.string.nav_link_instagram));
        } else if (id == R.id.nav_youtube_channel) {
            uri = Uri.parse(getString(R.string.nav_link_youtube_channel));
        } else if (id == R.id.nav_link_to_website) {
            uri = Uri.parse(getString(R.string.nav_link_to_website));
        } else if (id == R.id.nav_privacy_policy) {
            Toast.makeText(this, "Privacy policy", Toast.LENGTH_SHORT).show();
        }
        if(uri != null) {
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_button1:
                tvOut.setText("press button 1");
                break;
            case R.id.btn_button2:
                tvOut.setText("press button 2");
                break;
            case R.id.btn_button3:
                tvOut.setText("press button 3");
                break;
            case R.id.btn_button4:
                tvOut.setText("press button 4");
                break;
        }
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        Log.d(TAG, "onPermissionsGranted " + requestCode);
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        Log.d(TAG, "onPermissionsDenied " + requestCode);
        finish();
    }

    @SuppressLint("MissingPermission")
    private boolean checkPermissions() {
        String[] perms = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
        if (EasyPermissions.hasPermissions(this, perms)) {
            // Already have permission, do the thing
            mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REFRESH_TIME,
                    LOCATION_REFRESH_DISTANCE, mLocationListener);
            return true;
        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(this, getString(R.string.string_describe_why_do_you_need_a_location),
                    1000, perms);
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG, "onRequestPermissionsResult " + requestCode);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            Log.d(TAG, "onLocationChanged: " + location.getLatitude() + ", "+ location.getLongitude());
            ((Global_Var) getApplicationContext()).Set_Location(location.getLatitude(), location.getLongitude());
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
            Log.d(TAG, "onStatusChanged " + (s != null? s : "null") +  "; " + i);
        }

        @Override
        public void onProviderEnabled(String s) {
            Log.d(TAG, "onProviderEnabled " + (s != null? s : "null"));
        }

        @Override
        public void onProviderDisabled(String s) {
            Log.d(TAG, "onProviderDisabled " + (s != null? s : "null"));
        }
    };
}

