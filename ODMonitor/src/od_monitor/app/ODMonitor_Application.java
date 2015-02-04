package od_monitor.app;

import od_monitor.app.data.SyncData;
import android.app.Application;
import android.content.Context;

public class ODMonitor_Application extends Application {
	private SyncData sync_chart_notify = null;
	
	public void set_sync_chart_notify(SyncData data) {
		sync_chart_notify = data;
	}
	
	public SyncData get_sync_chart_notify() {
		return sync_chart_notify;
	}
}
