package ODMonitor.App;

import ODMonitor.App.data.sync_data;
import android.app.Application;
import android.content.Context;

public class ODMonitor_Application extends Application {
	private sync_data sync_chart_notify = null;
	
	public void set_sync_chart_notify(sync_data data) {
		sync_chart_notify = data;
	}
	
	public sync_data get_sync_chart_notify() {
		return sync_chart_notify;
	}
}
