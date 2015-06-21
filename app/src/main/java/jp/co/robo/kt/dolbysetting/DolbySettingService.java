package jp.co.robo.kt.dolbysetting;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.dolby.DolbyMobileAudioEffectClient;
import android.media.dolby.DolbyMobileClientCallbacks;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

/**
 * Implementation of Service.
 * AppWidgetからの要求で起動される。
 */
public class DolbySettingService extends Service {
    private static final String TAG = "DolbySettingService";
    private boolean mDolbyOn;
    private DolbyMobileAudioEffectClient mDolbyClient;
    private DolbyMobileClientCallbacks mCallback;
    private static final int NOTIFICATION_ID = 0;
    private static final String ACTION = "jp.co.robo.kt.dolbysetting.ACTION_TOGGLE_DOLBY";

    public DolbySettingService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return(null);
    }

    @Override
    public void onCreate() {
        initDolbyClient();
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        finalizeDolbyClient();
        deleteNotification();
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = (intent == null)? "":intent.getAction();
        if (ACTION.equals(action)) {
            toggleDolby();
        } else {
            initializeWidget();
            displayNotification();
        }
        return(START_STICKY);
    }

    private void initDolbyClient() {
        mDolbyClient = new DolbyMobileAudioEffectClient();
        mCallback = new DolbyMobileClientCallbacks() {
            @Override
            public void onEffectOnChanged(boolean on) {
                Log.d(TAG, "onEffectOnChanged(" + on + ")");
                mDolbyOn = on;
                updateWidget();
                displayNotification();
            }
            @Override
            public void onPresetChanged(int presetCategory, int preset) {
            }
            @Override
            public void onServiceConnected() {
                mDolbyOn = mDolbyClient.getDolbyEffectOn();
                Log.d(TAG, "onServiceConnected() called. mDolbyOn=" + mDolbyOn);
                updateWidget();
                displayNotification();
            }
            @Override
            public void onServiceDisconnected() {
            }
        };
        mDolbyClient.registerCallback(mCallback);
        mDolbyClient.bindToRemoteRunningService(this);
    }

    private void finalizeDolbyClient() {
        if (mDolbyClient != null) {
            if (mCallback != null) {
                mDolbyClient.unregisterCallback(mCallback);
                mCallback = null;
            }
            mDolbyClient.unBindFromRemoteRunningService(this);
            mDolbyClient = null;
        }
    }

    private void initializeWidget() {
        displayWidget(true);
    }

    private void updateWidget() {
        displayWidget(false);
    }

    private void displayWidget(boolean initialize) {
        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.main);
        if (initialize) {
            remoteViews.setOnClickPendingIntent(R.id.WidgetButton, getPendingIntent());
        }
        int buttonImageId = (mDolbyOn)? R.mipmap.widget_button_on:R.mipmap.widget_button_off;
        remoteViews.setImageViewResource(R.id.WidgetButton, buttonImageId);
        ComponentName widget = new ComponentName(this, DolbySettingWidget.class);
        AppWidgetManager manager = AppWidgetManager.getInstance(this);
        manager.updateAppWidget(widget, remoteViews);
    }

    private void displayNotification() {
        Notification.Builder builder = new Notification.Builder(getApplicationContext());
        String contentString;
        int smallIconId;
        int largeIconId;
        if (mDolbyOn) {
            contentString = "DOLBY=ON";
            smallIconId = R.mipmap.ic_stat_notify_on_small;
            largeIconId = R.mipmap.ic_stat_notify_on_large;
        } else {
            contentString = "DOLBY=OFF";
            smallIconId = R.mipmap.ic_stat_notify_off_small;
            largeIconId = R.mipmap.ic_stat_notify_off_large;
        }
        builder.setContentIntent(getPendingIntent());
        builder.setTicker(contentString);
        builder.setContentTitle(contentString);
        builder.setAutoCancel(false);
        builder.setSmallIcon(smallIconId);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), largeIconId));
        NotificationManager manager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        manager.notify(NOTIFICATION_ID, builder.build());
    }

    private void deleteNotification() {
        NotificationManager manager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        manager.cancel(NOTIFICATION_ID);
    }

    private void toggleDolby() {
        if (mDolbyClient != null) {
            boolean dolbyOn = !mDolbyOn;
            mDolbyClient.setDolbyEffectOn(dolbyOn);
            mDolbyClient.setGlobalEffectOn(dolbyOn);
        }
    }

    private PendingIntent getPendingIntent() {
        return(PendingIntent.getService(this, 0, new Intent(ACTION), 0));
    }
}
