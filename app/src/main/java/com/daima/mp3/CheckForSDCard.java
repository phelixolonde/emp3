package com.daima.mp3;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;


public class CheckForSDCard {
    //Method to Check If SD Card is mounted or not
    public static boolean isSDCardPresent() {
        return Environment.getExternalStorageState().equals(

                Environment.MEDIA_MOUNTED);
    }


    public static final String PREFERENCE_LAST_NOTIF_ID = "PREFERENCE_LAST_NOTIF_ID";

    public static int getNextNotifId(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        int id = sharedPreferences.getInt(PREFERENCE_LAST_NOTIF_ID, 0) + 1;

        sharedPreferences.edit().putInt(PREFERENCE_LAST_NOTIF_ID, id).apply();
        return id;
    }


}