package in.altilogic.prayogeek.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

import in.altilogic.prayogeek.R;
import in.altilogic.prayogeek.RemoteButtonScreen;

import static android.content.Context.INPUT_METHOD_SERVICE;

public class Utils {
    private static final String TAG = "YOUSCOPE-DB-UTILS";
    private static final String PREFERENCES_FILE = "altilogic_prayogeek_settings";
    private static final String REMOTE_SCREEN_PREFIX = "REMOTE_SCREEN_";


    public static int getToolbarHeight(Context context) {
        int height = (int) context.getResources().getDimension(R.dimen.abc_action_bar_default_height_material);
        return height;
    }

    public static int getStatusBarHeight(Context context) {
        int height = (int) context.getResources().getDimension(R.dimen.statusbar_size);
        return height;
    }


    public static Drawable tintMyDrawable(Drawable drawable, int color) {
        drawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTint(drawable, color);
        DrawableCompat.setTintMode(drawable, PorterDuff.Mode.SRC_IN);
        return drawable;
    }


    public static String readSharedSetting(Context ctx, String settingName, String defaultValue) {
        SharedPreferences sharedPref = ctx.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        return sharedPref.getString(settingName, defaultValue);
    }

    public static int readSharedSetting(Context ctx, String settingName, int defaultValue) {
        SharedPreferences sharedPref = ctx.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        return sharedPref.getInt(settingName, defaultValue);
    }

    public static void saveSharedSetting(Context ctx, String settingName, String settingValue) {
        SharedPreferences sharedPref = ctx.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(settingName, settingValue);
        editor.apply();
    }

    public static void saveSharedSetting(Context ctx, String settingName, int settingValue) {
        SharedPreferences sharedPref = ctx.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(settingName, settingValue);
        editor.apply();
    }

    public static boolean isOnline(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }

    public static String[] getAssetsList(Context context) {
        String[] assetsList;
        try {
            assetsList = context.getAssets().list("");
        } catch (IOException e) {
            return null;
        }
        return assetsList;
    }

    /**
     * Hides the soft keyboard
     */
    public static void hideSoftKeyboard(Activity activity) {
        if(activity.getCurrentFocus()!=null) {
            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        }
    }

    /**
     * Shows the soft keyboard
     */
    public static void showSoftKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) view.getContext().getSystemService(INPUT_METHOD_SERVICE);
        view.requestFocus();
        inputMethodManager.showSoftInput(view, 0);
    }

    public static String getTimestamp(long millis) {
        DateFormat formatter = new SimpleDateFormat("[HH:mm:ss.SSS]", Locale.US);
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        return formatter.format(new Date(millis));
    }

    public static String asciiToHex(String asciiStr) {
        char[] chars = asciiStr.toCharArray();
        StringBuilder hex = new StringBuilder();
        for (char ch : chars) {
            if((int) ch <= 0xF)
                hex.append(Integer.toHexString(0));

            hex.append(Integer.toHexString((int) ch));
        }
        return hex.toString();
    }

    public static String checkSlashSymbols(String name) {
        String compareSymbol = "";
        if(name.contains("/"))
            compareSymbol = "/";
        else
            return name;

        String[] parts = name.split(compareSymbol);
        StringBuilder sb = new StringBuilder();
        int len = 0;
        for(String part : parts){
            sb.append(part);
            if(len++ < parts.length-1)
                sb.append(" ");
        }

        return sb.toString();
    }

    public static void saveScreen(Context context, String key, RemoteButtonScreen screen) {
        if(screen == null)
            return;
        Log.d(TAG, "save screen " + screen.toString());
        saveSharedSetting(context, REMOTE_SCREEN_PREFIX + key, screen.toString());
    }

    public static RemoteButtonScreen loadScreen(Context context, String key) {
        if(key == null)
            return null;
        String params = readSharedSetting(context, REMOTE_SCREEN_PREFIX + key, null);
        if(params == null)
            return  null;
        Log.d(TAG, "load screen " + params);

        return new RemoteButtonScreen(params);
    }
}

