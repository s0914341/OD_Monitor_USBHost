package od_monitor.app;

import java.util.LinkedList;
import java.util.List;

import od_monitor.app.data.SyncData;
import android.app.Activity;
import android.app.Application;
import android.content.Context;

public class ODMonitorApplication extends Application {
	/**
	 * no_devices = true is not to need sensor and shaker for app test
	 * run normal experiment, please set no_devices is false.
	 */
	public static final boolean no_devices = true;
	private SyncData sync_chart_notify = null;
	private boolean mail_alert_load = false;
	private SyncData sync_mail_alert = new SyncData();
	private List<Activity> activityList = new LinkedList<Activity>();
	private static ODMonitorApplication instance;
	
	public void set_sync_chart_notify(SyncData data) {
		sync_chart_notify = data;
	}
	
	public SyncData get_sync_chart_notify() {
		return sync_chart_notify;
	}
	
	public void set_mail_alert_load(boolean alert) {
		synchronized (sync_mail_alert) {
		    mail_alert_load = alert;
		}
	}
	
	public boolean is_mail_alert_load() {
		return mail_alert_load;
	}

	//單例模式中獲取唯一的MyApplication實例
	public static ODMonitorApplication getInstance() {
	    if(null == instance) {
	        instance = new ODMonitorApplication();
	    }
	    
	    return instance;
	}
	
	//添加Activity到容器中
	public void addActivity(Activity activity) {
	    activityList.add(activity);
	}
	
	//遍歷所有Activity並finish
	public void exit() {
	    for(Activity activity:activityList) {
	        activity.finish();
	    }
	    
	    System.exit(0);
	}
}
