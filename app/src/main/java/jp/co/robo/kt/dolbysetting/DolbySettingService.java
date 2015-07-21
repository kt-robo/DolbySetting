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
    private boolean mDolbyOn;
    private DolbyMobileAudioEffectClient mDolbyClient;
    private DolbyMobileClientCallbacks mCallback;

    public DolbySettingService() {
        mDolbyOn = false;
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
        Log.d(getClass().getName(), "onStartCommand(action=" + action + ")");
        if (getAction().equals(action)) {
            toggleDolby();
        } else {
            displayWidget();
            displayNotification();
        }
        return(START_STICKY);
    }

    private void initDolbyClient() {
        mDolbyClient = new DolbyMobileAudioEffectClient();
        mCallback = new DolbyMobileClientCallbacks() {
            @Override
            public void onEffectOnChanged(boolean on) {
                Log.d(getClass().getName(), "onEffectOnChanged(on=" + on + "):mDolbyOn=" + mDolbyOn);
                if (mDolbyOn != on) {
                    mDolbyOn = on;
                    displayWidget();
                    displayNotification();
                }
            }
            @Override
            public void onPresetChanged(int presetCategory, int preset) {
            }
            @Override
            public void onServiceConnected() {
                boolean dolbyOn = mDolbyClient.getDolbyEffectOn();
                Log.d(getClass().getName(), "onServiceConnected():mDolbyOn=" + mDolbyOn + ":dolbyOn=" + dolbyOn);
                if (mDolbyOn != dolbyOn) {
                    displayWidget();
                    displayNotification();
                }
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

    private void displayWidget() {
        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.main);
        remoteViews.setOnClickPendingIntent(R.id.WidgetButton, getPendingIntent());
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
            contentString = getString(R.string.dolby_on);
            smallIconId = R.mipmap.ic_stat_notify_on_small;
            largeIconId = R.mipmap.ic_stat_notify_on_large;
        } else {
            contentString = getString(R.string.dolby_off);
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
        manager.notify(getResources().getInteger(R.integer.notificatioin_id), builder.build());
    }

    private void deleteNotification() {
        NotificationManager manager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        manager.cancel(getResources().getInteger(R.integer.notificatioin_id));
    }

    private void toggleDolby() {
        Log.d(getClass().getName(), "toggleDolby():mDolbyOn=" + mDolbyOn + ":mDolbyClient=" + mDolbyClient);
        if (mDolbyClient != null) {
            // mDolbyOnと実際の設定値が食い違うことがある（原因不明）ため、
            // このdolbyOnはmDolbyOnではなく実際の設定値をもとにする。
            boolean dolbyOn = !mDolbyClient.getDolbyEffectOn();
            mDolbyClient.setDolbyEffectOn(dolbyOn);
            mDolbyClient.setGlobalEffectOn(dolbyOn);
        }
    }

    private PendingIntent getPendingIntent() {
        final Intent intent = new Intent(getAction());
        return(PendingIntent.getService(this, 0, intent, 0));
    }

    private String getAction() {
        return(getString(R.string.action_name));
    }
}
