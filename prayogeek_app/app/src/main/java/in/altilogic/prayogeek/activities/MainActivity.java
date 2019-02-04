package in.altilogic.prayogeek.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;


import in.altilogic.prayogeek.FireBaseHelper;
import in.altilogic.prayogeek.Global_Var;
import in.altilogic.prayogeek.R;
import in.altilogic.prayogeek.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener, EasyPermissions.PermissionCallbacks {

    private static final float LOCATION_REFRESH_DISTANCE = 100.f;
    private static final long LOCATION_REFRESH_TIME = 1000;
    // Firebase Authentication
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FireBaseHelper mFireBaseHelper;

    public static final int RC_SIGN_IN = 3;
    public static final int RC_ON_BOARDING = 4;
    public static final int RC_PROFILE_CHANGE = 5;
    public static final int RC_BUTTON1 = 6;
    public static final int RC_BUTTON2 = 7;

    private String mUsername;
    private String mEmailId;
    public static final String ANONYMOUS = "anonymous";

    public static final String PREF_USER_FIRST_TIME = "user_first_time";
    public static final String PREF_USER_NAME = "user_name";
    public static final String PREF_EMAILID = "email_id";
    boolean isUserFirstTime;
    boolean isOnline = false;

    public static final String TAG = "YOUSCOPE-DB";
    private Toolbar mToolbar = null;

    private Spinner mList1, mList2;
    private TextView tvInfo, tvError;
    private Button mButton1, mButton2, mButton3, mButton4;

    private TextView tvNavDrUser, tvNavDrEmail;
//    private LocationManager mLocationManager;

    private ArrayAdapter<String> adapterList1;
    private ArrayList<String> dataList1 = new ArrayList<>();
    private ArrayAdapter<String> adapterList2;
    private ArrayList<String> dataList2 = new ArrayList<>();

    private FusedLocationProviderClient mFusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        ((Global_Var) getApplicationContext()).Set_ConnectionStatus(Global_Var.CS_APP_OPENED);
        mFireBaseHelper = new FireBaseHelper();
        firebase_auth_init();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        isOnline = Utils.isOnline(this);
        if (!isOnline) {
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
                    if (!isUserFullProfile()) {
                        Log.d(TAG, "The profile not full, run the profile screen");
                        runProfileChange();
                    }
                    list1_update();
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

    private void list1_update() {
        mFireBaseHelper.read("Colleges", "College_List", new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    dataList1.clear();
                    List<String> colleges = (List<String>) documentSnapshot.get("Colleges");
                    if (colleges != null && colleges.size() > 0) {
                        Log.d(TAG, " data: " + colleges.toString());

                        dataList1.addAll(colleges);
                        adapterList1.notifyDataSetChanged();
                        if (dataList1.size() > 0) {
                            list2_update(dataList1.get(0));
                        }
                    }
                } else {
                    Log.d(TAG, " data: null");
                }
            }
        });
    }

    private String mDocumentName;

    private void list2_update(String document) {
        mDocumentName = document;
        mFireBaseHelper.read("Colleges", document, new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    dataList2.clear();
                    DocumentSnapshot docum = task.getResult();
                    if (mDocumentName != null && docum != null) {
                        if (mDocumentName.equals("Demo")) {
                            List<String> dataMap = (List<String>) docum.get("demo_modules");
                            if (dataMap != null && dataMap.size() > 0) {
                                Log.d(TAG, " data: " + dataMap.toString());
                                dataList2.addAll(dataMap);
                            }
                        } else if (mDocumentName.equals("Individual")) {
                            Map<String, Object> dataMap = (Map<String, Object>) docum.getData();
                            if (dataMap != null) {
                                if (dataMap.containsKey(mEmailId)) {
                                    Map<String, Object> dataModules = (Map<String, Object>) dataMap.get((Object) mEmailId);
                                    if (dataModules != null) {
                                        if (dataModules.containsKey("module")) {
                                            String dataMod = (String) dataModules.get((Object) "module");
                                            if (dataMod != null) {
                                                dataList2.add(dataMod);
                                                Log.d(TAG, " module " + dataMod);
                                                Global_Var GlobalVar = (Global_Var) getApplicationContext();
                                                GlobalVar.Set_Module_Name(dataMod);
                                                if (dataMap.containsKey(dataMod)) {
                                                    Map<String, Object> hw = (Map<String, Object>) dataMap.get(dataMod);
                                                    long hw_version = (Long) hw.get((String) "hw_version");
                                                    long ina1_cal = (Long) hw.get((String) "ina1_cal");
                                                    long ina2_cal = (Long) hw.get((String) "ina2_cal");
                                                    String mac_address = (String) hw.get((String) "mac_address");
                                                    GlobalVar.Set_INA1Calibration((int) ina1_cal);
                                                    GlobalVar.Set_INA2Calibration((int) ina2_cal);
                                                    GlobalVar.Set_MacAddress(mac_address);
                                                }
                                            }
                                        }
                                        if (dataModules.containsKey("validity")) {
                                            Timestamp dataMod = (Timestamp) dataModules.get((Object) "validity");
                                            if (dataMod != null) {
                                                printInfoMessage("Subscription valid untill : " + dataMod.toDate().toString());
                                            }
                                        }
                                    }
                                } else {
                                    printErrorMessage("You have not Subscribed");
                                }
                                Log.d(TAG, " Select Individual " + dataMap.toString());
                            }
                        } else {
                            List<String> dataMap = (List<String>) docum.get("college_modules");
                            if (dataMap != null && dataMap.size() > 0) {
                                Log.d(TAG, " data: " + dataMap.toString());
                                dataList2.addAll(dataMap);
                            }
                        }
                    }

                    adapterList2.notifyDataSetChanged();
                    Log.d(MainActivity.TAG, docum.getId() + " => " + docum.getData());
                } else {
                    Log.w(MainActivity.TAG, "Error getting documents.", task.getException());
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RC_SIGN_IN:
                if (resultCode == RESULT_OK) {
                    Toast.makeText(this, "Signed in!", Toast.LENGTH_SHORT).show();

                    ui_init();
                    FirebaseUser user = mFirebaseAuth.getCurrentUser();
                    if (user != null) {
                        // user is signed in
                        mUsername = user.getDisplayName();
                        mEmailId = user.getEmail();
                        Global_Var GlobalVar = (Global_Var) getApplicationContext();
                        GlobalVar.Set_Username(mUsername);
                        GlobalVar.Set_EmailId(mEmailId);
                    }
                    if (!isUserFullProfile()) {
                        Log.d(TAG, "The profile not full, run the profile screen");
                        runProfileChange();
                    }
                } else if (resultCode == RESULT_CANCELED) {
                    if (isOnline == true) {
                        Toast.makeText(this, "Sign In Cancelled!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Turn ON Internet to Sign In!", Toast.LENGTH_SHORT).show();
                    }
                    finish();
                }
                break;
            case RC_ON_BOARDING:
                Log.d(TAG, "OnBoarding Finish");
                if (!isUserFullProfile())
                    runProfileChange();
                break;
            case RC_PROFILE_CHANGE:
                if (!isUserFullProfile())
                    finish();
                break;
            case RC_BUTTON1:
            case RC_BUTTON2:
                ((Global_Var) getApplicationContext()).Set_ConnectionStatus(Global_Var.CS_DISCONNECTED);
                break;

        }
    }

    boolean is_init = false;

    private void ui_init() {
        if (is_init)
            return;
        is_init = true;
        navigation_driver_init();

        mList1 = findViewById(R.id.spList1);
        mList2 = findViewById(R.id.spList2);
        tvInfo = findViewById(R.id.tv_main1);
        tvError = findViewById(R.id.tv_main2);
        mButton1 = findViewById(R.id.btn_button1);
        mButton2 = findViewById(R.id.btn_button2);
        mButton3 = findViewById(R.id.btn_button3);
        mButton4 = findViewById(R.id.btn_button4);
        mButton1.setOnClickListener(this);
        mButton2.setOnClickListener(this);
        mButton3.setOnClickListener(this);
        mButton4.setOnClickListener(this);

        isUserFirstTime = Boolean.valueOf(Utils.readSharedSetting(MainActivity.this, PREF_USER_FIRST_TIME, "true"));

        if (isUserFirstTime) {
            Log.d(TAG, "First use, run onboarding");
            runOnboarding();
        }

        checkLocationPermissions();

        adapterList1 = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_item, dataList1);
        adapterList1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mList1.setAdapter(adapterList1);
        adapterList2 = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_item, dataList2);
        adapterList2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mList2.setAdapter(adapterList2);

        mList1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (dataList1.size() > 0 && dataList1.size() > i) {
                    Log.d(TAG, "List 1 selected " + i + "; listSize " + dataList1.size());
                    ((Global_Var) getApplicationContext()).Set_Category(dataList1.get(i));
                    list2_update(dataList1.get(i));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        mList2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (dataList2.size() > 0 && dataList2.size() > i) {
                    printInfoMessage("App will connect to Module " + dataList2.get(i));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        printInfoMessage("Select the Correct Module to Connect");
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

        headerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                runProfileChange();
            }
        });

        if (mUsername != null)
            tvNavDrUser.setText(mUsername);

        if (mEmailId != null)
            tvNavDrEmail.setText(mEmailId);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        Uri uri = null;

        if (id == R.id.nav_on_boarding) {
            runOnboarding();
        } else if (id == R.id.nav_basic_electronic_tutorial) {
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
        if (uri != null) {
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
                printInfoMessage("press button 1");
                updateLocation();
                ((Global_Var) getApplicationContext()).Set_ConnectionStatus(Global_Var.CS_CONNECTED);
                break;
            case R.id.btn_button2:
                printInfoMessage("press button 2");
                updateLocation();
                ((Global_Var) getApplicationContext()).Set_ConnectionStatus(Global_Var.CS_CONNECTED);
                startActivityForResult(new Intent(MainActivity.this, Button2Activity.class)
                        .putExtra(PREF_USER_FIRST_TIME, isUserFirstTime), RC_BUTTON2);
                break;
            case R.id.btn_button3:
                printInfoMessage("press button 3");
                updateLocation();
                FireBaseHelper fireBaseHelper = new FireBaseHelper();
                fireBaseHelper.write((Global_Var) getApplicationContext());
                break;
            case R.id.btn_button4:
                printInfoMessage("press button 4");
                updateLocation();
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

    private boolean checkLocationPermissions() {
        String[] perms = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
        if (EasyPermissions.hasPermissions(this, perms)) {
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

    private void printErrorMessage(String message) {
        tvError.setVisibility(View.VISIBLE);
        tvInfo.setVisibility(View.GONE);
        tvError.setText(message);
    }

    private void printInfoMessage(String message) {
        tvInfo.setVisibility(View.VISIBLE);
        tvError.setVisibility(View.GONE);
        tvInfo.setText(message);
    }

    private void updateLocation() {
        if (checkLocationPermissions()) {
            mFusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        Global_Var GlobalVar = (Global_Var) getApplicationContext();
                        GlobalVar.Set_Location(location.getLatitude(), location.getLongitude());
                        Log.d(TAG, "update location " + location.toString());
                    }
                }
            });
        }
    }

    private boolean isUserFullProfile() {
        String college = Utils.readSharedSetting(getApplicationContext(), Global_Var.COLLEGE_NAME, null);
        String semester = Utils.readSharedSetting(getApplicationContext(), Global_Var.SEMESTER, null);
        ((Global_Var) getApplicationContext()).Set_College_Name(college != null ? college : "null");
        ((Global_Var) getApplicationContext()).Set_Semester(semester != null ? semester : "null");
        return college != null && semester != null;
    }

    private void runProfileChange() {
        Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
        intent.putExtra(PREF_USER_NAME, mUsername)
                .putExtra(PREF_EMAILID, mEmailId);
        startActivityForResult(intent, RC_PROFILE_CHANGE);
    }

    private void runOnboarding(){
        startActivityForResult(new Intent(MainActivity.this, OnBoardingActivity.class)
                .putExtra(PREF_USER_FIRST_TIME, isUserFirstTime), RC_ON_BOARDING);
    }
}

