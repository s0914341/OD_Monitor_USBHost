package od_monitor.app;

import od_monitor.app.data.SyncData;
import android.app.Application;
import android.content.Context;

public class ODMonitorApplication extends Application {
	private SyncData sync_chart_notify = null;
	private boolean mail_alert_load = false;
	private SyncData sync_mail_alert = new SyncData();
	
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
}
