package jp.co.robo.kt.dolbysetting;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;


/**
 * Implementation of App Widget functionality.
 * 処理の実体はサービスにあるので、ここはサービスの起動と停止のみ。
 */
public class DolbySettingWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // 複数のAppWidgetを配置したときに正しく表示させるために、
        // onEnabled()ではなくonUpdate()でstartService()を実行する。
        context.startService(new Intent(context, DolbySettingService.class));
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
        super.onEnabled(context);
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
        context.stopService(new Intent(context, DolbySettingService.class));
        super.onDisabled(context);
    }

}
