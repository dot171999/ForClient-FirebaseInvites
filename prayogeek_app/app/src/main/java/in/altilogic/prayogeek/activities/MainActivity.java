package in.altilogic.prayogeek.activities;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
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
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;


import in.altilogic.prayogeek.FireBaseHelper;
import in.altilogic.prayogeek.Global_Var;
import in.altilogic.prayogeek.R;
import in.altilogic.prayogeek.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import pub.devrel.easypermissions.EasyPermissions;

import static in.altilogic.prayogeek.utils.Utils.saveSharedSetting;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener, EasyPermissions.PermissionCallbacks, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

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

    boolean isOnline = false;

    public static final String TAG = "YOUSCOPE-DB-MAIN";
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
    private DrawerLayout mDrawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        ((Global_Var) getApplicationContext()).Set_ConnectionStatus(Global_Var.CS_APP_OPENED);
        Log.d(TAG, "onCreate()");

        mFireBaseHelper = new FireBaseHelper();
        firebase_auth_init();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart()");
        saveSharedSetting(this, TutorialActivity.CURRENT_SCREEN_SETTINGS, 0);
        isOnline = Utils.isOnline(this);
        if (!isOnline) {
            Toast.makeText(this, "Network is not available ", Toast.LENGTH_SHORT).show();
//            finish();
        }
        if(isUserFullProfile() && isOnboardingShowing())
            updateLocation();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause()");
        if (mAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed()");
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
        Log.d(TAG, "onResume()");
        if (mFirebaseAuth != null)
            mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        Log.d(TAG, "onCreateOptionsMenu()");
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected()");
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
                    loadList1();
                    list1_check_for_update();
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

    private boolean isListsEquals(List<String> list, ArrayList<String> arrayList){
        if(list != null && arrayList != null){
            if(list.size() != arrayList.size())
                return false;

            for(int i=0; i<list.size(); i++){
                if(!list.get(i).equals(arrayList.get(i))){
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private void checkSelectedList1() {
        String varList1 = ((Global_Var) getApplicationContext()).Get_Category();
        if( varList1 != null && dataList1 != null && dataList1.contains(varList1))
        {
            for(int i=0; i<dataList1.size(); i++){
                if(dataList1.get(i).equals(varList1)){
                    mList1.setSelection(i);
                    Log.d(TAG, " Set Selected LIST1 : " + varList1);
                    break;
                }
            }
        }
    }

    private void checkSelectedList2() {
        String varList2 = ((Global_Var) getApplicationContext()).Get_Module_Name();
        int idxSelected = 0;
        if( varList2 != null && dataList2 != null && dataList2.contains(varList2))
        {
            for(int i=0; i<dataList2.size(); i++){
                if(dataList2.get(i).equals(varList2)){
                    idxSelected = i;
                    break;
                }
            }
        }
        mList2.setSelection(idxSelected);
        Log.d(TAG, " Set Selected LIST2 : " + mList2.getSelectedItem().toString());
    }

    private void list1_check_for_update() {
        mFireBaseHelper.read("Colleges", "College_List", new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    List<String> colleges = (List<String>) documentSnapshot.get("Colleges");

                    if(isListsEquals(colleges, dataList1)) {
                        checkSelectedList1();
                        return;
                    }

                    Log.d(TAG, "Updating List1");

                    dataList1.clear();
                    if (colleges != null && colleges.size() > 0) {
                        Log.d(TAG, " data: " + colleges.toString());

                        dataList1.addAll(colleges);
                        adapterList1.notifyDataSetChanged();
                        saveList1();
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
        Log.d(TAG, " list2_update: " + document);
        mDocumentName = document;
        mFireBaseHelper.read("Colleges", document, new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    boolean mIndividual = false;
                    Global_Var GlobalVar = (Global_Var) getApplicationContext();
                    dataList2.clear();
                    DocumentSnapshot docum = task.getResult();
                    if (mDocumentName != null && docum != null) {
                        switch (mDocumentName) {
                            case "Demo": {
                                ((Global_Var) getApplicationContext()).Set_Project_Access(false);
                                List<String> dataMap = (List<String>) docum.get("demo_modules");
                                if (dataMap != null && dataMap.size() > 0) {
                                    Log.d(TAG, " data: " + dataMap.toString());
                                    dataList2.addAll(dataMap);
                                }
                                break;
                            }
                            case "Individual": {
                                mIndividual = true;
                                Map<String, Object> dataMap = (Map<String, Object>) docum.getData();
                                if (dataMap != null) {
                                    if (dataMap.containsKey(mEmailId)) {
                                        ((Global_Var) getApplicationContext()).Set_Project_Access(true);

                                        Map<String, Object> dataModules = (Map<String, Object>) dataMap.get((Object) mEmailId);
                                        if (dataModules != null) {
                                            if (dataModules.containsKey("module")) {
                                                String dataMod = (String) dataModules.get((Object) "module");
                                                if (dataMod != null) {
                                                    dataList2.add(dataMod);
                                                    Log.d(TAG, " module " + dataMod);
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
                                                    GlobalVar.Set_Validity(dataMod);
                                                    if (!GlobalVar.isValidity()) {
                                                        Toast.makeText(getApplicationContext(),getString(R.string.subsciption_expired), Toast.LENGTH_SHORT ).show();
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        ((Global_Var) getApplicationContext()).Set_Project_Access(false);
                                        printErrorMessage("You have not Subscribed");
                                    }
                                    Log.d(TAG, " Select Individual " + dataMap.toString());
                                }
                                break;
                            }
                            default: {
                                List<String> dataMap = (List<String>) docum.get("college_modules");
                                if (dataMap != null && dataMap.size() > 0) {
                                    Log.d(TAG, " data: " + dataMap.toString());
                                    dataList2.addAll(dataMap);
                                }
                                ((Global_Var) getApplicationContext()).Set_Project_Access(false);

                                Map<String, Object> defMap = (Map<String, Object>) docum.getData();
                                if (defMap != null && defMap.size() > 0) {
                                    if (defMap.containsKey("proj_access")) {
                                        List<String> mails = (List<String>) defMap.get("proj_access");
                                        if(mails.contains(mEmailId))
                                        {
                                            ((Global_Var) getApplicationContext()).Set_Project_Access(true);
                                        }
                                    }
                                    Log.d(TAG, " data: " + defMap.toString());
                                }

                                break;
                            }
                        }
                    }

                    adapterList2.notifyDataSetChanged();
                    String message = "";
                    if(dataList2 != null && dataList2.size() > 0) {
                        if(mIndividual) {
                            message = getValidityString();
                        }
                        else
                            message = "App will connect to Module " + ( mList2 != null && mList2.getSelectedItem() != null ? mList2.getSelectedItem().toString() : "");

                        checkSelectedList2();
                    }

                    printInfoMessage(message);

                    Log.d(MainActivity.TAG, (docum != null ? docum.getId() : "NULL") + " => " + (docum != null ? docum.getData() : "NULL"));
                } else {
                    Log.w(MainActivity.TAG, "Error getting documents.", task.getException());
                }
            }
        });
    }

    private String getValidityString() {
        StringBuilder sb = new StringBuilder();
        Global_Var GlobalVar = (Global_Var) getApplicationContext();
        if (GlobalVar.isValidity()) {
            sb.append("Subscription valid until : ").append(GlobalVar.Get_Validity().toDate().toString()).append("\n");
            sb.append("App will connect to Module ").append(dataList2.get(0));
        }
        else
            sb.append(getString(R.string.subsciption_expired)).append("\n");

        return sb.toString();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult()");

        switch (requestCode) {
            case RC_SIGN_IN:
                if (resultCode == RESULT_OK) {
                    Toast.makeText(this, "Signed in!", Toast.LENGTH_SHORT).show();

                    FirebaseUser user = mFirebaseAuth.getCurrentUser();
                    if (user != null) {
                        // user is signed in
                        mUsername = user.getDisplayName();
                        mEmailId = user.getEmail();
                        Global_Var GlobalVar = (Global_Var) getApplicationContext();
                        GlobalVar.Set_Username(mUsername);
                        GlobalVar.Set_EmailId(mEmailId);
                    }
                    ui_init();
                } else if (resultCode == RESULT_CANCELED) {
                    if (isOnline) {
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
                if(!isOnboardingShowing())
                    runOnboarding();
                break;
            case RC_BUTTON1:
            case RC_BUTTON2:
                ((Global_Var) getApplicationContext()).Set_ConnectionStatus(Global_Var.CS_DISCONNECTED);
                saveGlobals();
                Log.d(TAG, "onActivityResult: " + ((Global_Var) getApplicationContext()).Get_Category() + " - " + ((Global_Var) getApplicationContext()).Get_Module_Name());
                list1_check_for_update();
                break;
        }
    }

    boolean is_init = false;

    private void ui_init() {
        Log.d(TAG, "ui_init()");
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

        if (isUserFullProfile()) {
            if (!isOnboardingShowing()) {
                Log.d(TAG, "First use, run onboarding");
                runOnboarding();
            }
        } else {
            runProfileChange();
        }

//        adapterList1 = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_item, dataList1);
        adapterList1 = new ArrayAdapter<String>(MainActivity.this, R.layout.spinner_custom, dataList1);
        adapterList1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mList1.setAdapter(adapterList1);
//        adapterList2 = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_item, dataList2);
        adapterList2 = new ArrayAdapter<String>(MainActivity.this, R.layout.spinner_custom, dataList2);
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
                String collection,document;
                if (dataList2.size() > 0 && dataList2.size() > i) {
                    printInfoMessage("App will connect to Module " + dataList2.get(i));
                    if((mList1.getSelectedItem().toString() == "Demo") || (mList1.getSelectedItem().toString() == "Individual"))
                    {
                        collection = "Hw_Modules";
                        document = "module_info_100";
                    }
                    else
                    {
                        collection = "Colleges";
                        document = mList1.getSelectedItem().toString();
                    }

                    mFireBaseHelper.read(collection, document, new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if(mList1.getSelectedItem().toString().equals("Individual"))  {
                                printInfoMessage(getValidityString());
                            }
                            if(task != null){
                                DocumentSnapshot docum = task.getResult();
                                Map<String, Object> hw =  (Map<String, Object>) docum.get(mList2.getSelectedItem().toString());
                                if (hw != null) {
                                    long hw_version = (Long) hw.get((String) "hw_version");
                                    long ina1_cal = (Long) hw.get((String) "ina1_cal");
                                    long ina2_cal = (Long) hw.get((String) "ina2_cal");
                                    String mac_address = (String) hw.get((String) "mac_address");
                                    Global_Var GlobalVar = ((Global_Var) getApplicationContext());
                                    GlobalVar.Set_INA1Calibration((int) ina1_cal);
                                    GlobalVar.Set_INA2Calibration((int) ina2_cal);
                                    GlobalVar.Set_MacAddress(mac_address);
                                }
                            }
                        }
                    });
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        printInfoMessage("Select the Correct Module to Connect");
    }

    private void navigation_driver_init() {
        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.addDrawerListener(toggle);
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
                mDrawer.closeDrawer(Gravity.LEFT);
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
        if (mUsername != null)
            tvNavDrUser.setText(mUsername);

        if (mEmailId != null)
            tvNavDrEmail.setText(mEmailId);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onClick(View view) {
        Log.d(TAG, "onClick()");

        boolean individual = mList1.getSelectedItem().toString().equals("Individual");
        switch (view.getId()) {
            case R.id.btn_button1:
                printInfoMessage("press button 1");
                updateLocation();
                if(individual && !( (Global_Var) getApplicationContext() ).isValidity() ) {
                    Toast.makeText(getApplicationContext(),getString(R.string.subsciption_expired), Toast.LENGTH_SHORT ).show();
                }
                else{
                    ((Global_Var) getApplicationContext()).Set_ConnectionStatus(Global_Var.CS_CONNECTED);
                    saveGlobals();
                    startActivityForResult(new Intent(MainActivity.this, Button1Activity.class), RC_BUTTON1);
                }
                break;
            case R.id.btn_button2:
                if(individual && !( (Global_Var) getApplicationContext() ).isValidity() ) {
                    Toast.makeText(getApplicationContext(),getString(R.string.subsciption_expired), Toast.LENGTH_SHORT ).show();
                }
                else {
                    printInfoMessage("press button 2");
                    updateLocation();
                    ((Global_Var) getApplicationContext()).Set_ConnectionStatus(Global_Var.CS_CONNECTED);
                    saveGlobals();
                    startActivityForResult(new Intent(MainActivity.this, Button2Activity.class), RC_BUTTON2);
                }
                break;
            case R.id.btn_button3:
                printInfoMessage("press button 3");
                updateLocation();
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

        if (!isOnboardingShowing()) {
            Log.d(TAG, "First use, run onboarding");
            runOnboarding();
        }
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
        Log.d(TAG, "Start update location ");
        isGpsEnabled();
        if (checkLocationPermissions()) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Fail permission: update location ");
                return;
            }
            mFusedLocationClient.getLastLocation().addOnSuccessListener( new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        Global_Var GlobalVar = (Global_Var) getApplicationContext();
                        GlobalVar.Set_Location(location.getLatitude(), location.getLongitude());
                        Log.d(TAG, "update location " + location.toString());
                    }
                    else {
                        Log.d(TAG, "no location ");
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
        startActivityForResult(new Intent(MainActivity.this, OnBoardingActivity.class), RC_ON_BOARDING);
    }

    private boolean isOnboardingShowing(){
        return !Boolean.valueOf(Utils.readSharedSetting(MainActivity.this, PREF_USER_FIRST_TIME, "true"));
    }

    private void saveGlobals(){
        if(mList1 != null && mList1.getSelectedItem()!= null) {
            String list1 = mList1.getSelectedItem().toString();
            ((Global_Var) getApplicationContext()).Set_Category(list1);
        }
        if(mList2 != null && mList2.getSelectedItem()!= null) {
            String module = mList2.getSelectedItem().toString();
            ((Global_Var) getApplicationContext()).Set_Module_Name(module);
        }
        mFireBaseHelper.write((Global_Var) getApplicationContext());
    }

    private boolean isGpsEnabled(){
        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );

        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                            startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                            dialog.cancel();
                        }
                    });
            final AlertDialog alert = builder.create();
            alert.show();
        }
        else
            return true;
        return false;
    }

    private void saveList1(){
        if(dataList1 != null && dataList1.size() > 0) {
            StringBuilder sb = new StringBuilder();
            for(int i=0; i<dataList1.size(); i++){
                sb.append(dataList1.get(i));
                if(i<dataList1.size()-1)
                    sb.append(",");
            }

            Utils.saveSharedSetting(this, "PREFERENCES_LIST1", sb.toString());
        }
    }

    private void loadList1() {
        String list1 = Utils.readSharedSetting(this, "PREFERENCES_LIST1", null);
        if(dataList1 != null) {
            dataList1.clear();
            if(list1 == null) {
                dataList1.add("Demo");
                dataList1.add("Individual");
                dataList1.add("RNSIT");
            }
            else{
                try {
                    String[] lists = list1.split(",");
                    Collections.addAll(dataList1, lists);
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
        if(adapterList1 != null)
            adapterList1.notifyDataSetChanged();
    }

    @Override
    public void onConnected(@android.support.annotation.Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
