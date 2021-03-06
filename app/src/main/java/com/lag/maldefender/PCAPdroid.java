/*
 * This file is part of PCAPdroid.
 *
 * PCAPdroid is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PCAPdroid is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PCAPdroid.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2020-21 - Emanuele Faranda
 */

package com.lag.maldefender;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;

import com.lag.maldefender.activities.ErrorActivity;
import com.lag.maldefender.model.Blacklists;
import com.lag.maldefender.model.CtrlPermissions;
import com.lag.maldefender.model.MatchList;
import com.lag.maldefender.model.Prefs;
import com.onesignal.OSMutableNotification;
import com.onesignal.OSNotification;
import com.onesignal.OSNotificationReceivedEvent;
import com.onesignal.OneSignal;


import net.gotev.uploadservice.UploadServiceConfig;

import java.lang.ref.WeakReference;

import cat.ereza.customactivityoncrash.config.CaocConfig;

/* The PCAPdroid app class.
 * This class is instantiated before anything else, and its reference is stored in the mInstance.
 * Global state is stored into this class via singletons. Contrary to static singletons, this does
 * not require passing the localized Context to the singletons getters methods.
 *
 * IMPORTANT: do not override getResources() with mLocalizedContext, otherwise the Webview used for ads will crash!
 * https://stackoverflow.com/questions/56496714/android-webview-causing-runtimeexception-at-webviewdelegate-getpackageid
 */
public class PCAPdroid extends Application {
    private static final String ONESIGNAL_APP_ID = "c6e12511-5f40-4587-a512-628bf5e75f77";
    public static final String notificationChannelID = "TestChannel";
    private static final String TAG = "PCAPdroid";
    private MatchList mVisMask;
    private NotificationCompat.Builder xyz;
    private MatchList mMalwareWhitelist;

    private MatchList mBlocklist;
    private Blacklists mBlacklists;
    private CtrlPermissions mCtrlPermissions;
    private Context mLocalizedContext;
    private static WeakReference<PCAPdroid> mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE);
        OneSignal.initWithContext(this);
        OneSignal.setAppId(ONESIGNAL_APP_ID);

        createNotificationChannel();
        UploadServiceConfig.initialize(this,notificationChannelID,BuildConfig.DEBUG);

        Utils.BuildType buildtp = Utils.getBuildType(this);
        Log.d(TAG, "Build type: " + buildtp);

        CaocConfig.Builder builder = CaocConfig.Builder.create();
        if((buildtp == Utils.BuildType.PLAYSTORE) || (buildtp == Utils.BuildType.UNKNOWN)) {
            // Disabled to get reports via the Android system reporting facility and for unsupported builds
            builder.enabled(false);
        } else {
            builder.errorDrawable(R.drawable.ic_app_crash)
                    .errorActivity(ErrorActivity.class);
        }
        builder.apply();

        mInstance = new WeakReference<>(this);
        mLocalizedContext = createConfigurationContext(Utils.getLocalizedConfig(this));

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String theme = prefs.getString(Prefs.PREF_APP_THEME, "");

        if("".equals(theme)) {
            if(Utils.isTv(this)) {
                // Use the dark theme by default on Android TV
                theme = "dark";
                prefs.edit().putString(Prefs.PREF_APP_THEME, theme).apply();
            } else
                theme = "system";
        }
        Utils.setAppTheme(theme);



    }

    private void createNotificationChannel()
    {




        if(Build.VERSION.SDK_INT>=26)
        {
            NotificationManager manager= (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel channel= new NotificationChannel(notificationChannelID,"TestApp Channel", NotificationManager.IMPORTANCE_LOW);


            manager.createNotificationChannel(channel);
        }


    }

    public static @NonNull PCAPdroid getInstance() {
        return mInstance.get();
    }

    public MatchList getVisualizationMask() {
        if(mVisMask == null)
            mVisMask = new MatchList(mLocalizedContext, Prefs.PREF_VISUALIZATION_MASK);

        return mVisMask;
    }

    public Blacklists getBlacklists() {
        if(mBlacklists == null)
            mBlacklists = new Blacklists(mLocalizedContext);
        return mBlacklists;
    }

    public MatchList getMalwareWhitelist() {
        if(mMalwareWhitelist == null)
            mMalwareWhitelist = new MatchList(mLocalizedContext, Prefs.PREF_MALWARE_WHITELIST);
        return mMalwareWhitelist;
    }

    public MatchList getBlocklist() {
        if(mBlocklist == null)
            mBlocklist = new MatchList(mLocalizedContext, Prefs.PREF_BLOCKLIST);
        return mBlocklist;
    }

    public CtrlPermissions getCtrlPermissions() {
        if(mCtrlPermissions == null)
            mCtrlPermissions = new CtrlPermissions(this);
        return mCtrlPermissions;
    }






}