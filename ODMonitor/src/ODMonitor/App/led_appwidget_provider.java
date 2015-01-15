package ODMonitor.App;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

public class led_appwidget_provider extends AppWidgetProvider {

	String Tag = "App Widge text";
	static int semaphore = 0;
	RemoteViews views;
	
    /*public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
    	int i, n;
    	
    	n = appWidgetIds.length;
    	//for (i = 0; i < n; i++) {
    		views = new RemoteViews(context.getPackageName(), R.layout.example_appwidget_layout);
    		views.setTextViewText(R.id.textView1, "samephore: " + Integer.toString(semaphore));
    		
    		// Create an Intent to launch ExampleActivity
            Intent intent = new Intent(context, LogFileChooserActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
            
    		views.setOnClickPendingIntent(R.id.button1, pendingIntent);
    		appWidgetManager.updateAppWidget(appWidgetIds, views);
    	//}
    	semaphore++;
    	Log.d(Tag, "onUpdate");
    }*/
    
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		views = new RemoteViews(context.getPackageName(), R.layout.led_appwidget_layout);
		views.setImageViewResource(R.id.led_imagebutton, R.drawable.icon);
		
		// Create an Intent to launch ExampleActivity
        Intent intent = new Intent(context, ODMonitorActivity.class);
        //intent.setAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        intent.setAction(Intent.ACTION_MAIN);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        
		views.setOnClickPendingIntent(R.id.led_imagebutton, pendingIntent);
		appWidgetManager.updateAppWidget(appWidgetIds, views);
		Log.d(Tag, "onUpdate");
	}
	
    public void onDeleted(Context context, int[] appWidgetIds) {
    	Log.d(Tag, "onDeleted"+Integer.toString(appWidgetIds.length));
    }
    
    public void onEnabled(Context context) {
    	Log.d(Tag, "onEnabled");
    }
    
    public void onDisabled(Context context) {
    	Log.d(Tag, "onDisabled");
    }
}

