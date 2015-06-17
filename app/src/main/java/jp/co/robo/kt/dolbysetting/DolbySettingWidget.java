package jp.co.robo.kt.dolbysetting;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


/**
 * Implementation of App Widget functionality.
 */
public class DolbySettingWidget extends AppWidgetProvider {

    private static final String TAG = "DolbySettingWidget";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d(TAG, "onUpdate()");
        // There may be multiple widgets active, so update all of them
        /*
        for (int i = 0; i < appWidgetIds.length; i++) {
            updateAppWidget(context, appWidgetManager, appWidgetIds[i]);
        }
        */
        // 複数のAppWidgetを配置したときに正しく表示させるために、
        // onEnabled()ではなくonUpdate()でstartService()を実行する。
        context.startService(new Intent(context, DolbySettingService.class));
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }


    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
        Log.d(TAG, "onEnabled()");
        super.onEnabled(context);
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
        context.stopService(new Intent(context, DolbySettingService.class));
        Log.d(TAG, "onDisabled()");
        super.onDisabled(context);
    }

    /*
    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        Log.d(TAG, "updateAppWidget(" + appWidgetId + ")");
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        Log.d(TAG, "onReceive(" + intent.getAction() + ")");
        super.onReceive(context, intent);
    }
    */

}
