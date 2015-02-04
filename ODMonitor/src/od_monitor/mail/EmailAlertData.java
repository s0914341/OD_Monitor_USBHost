package od_monitor.mail;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class EmailAlertData implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4876634523641900239L;
	public static final String email_alert_file_name = "email_set";
	public static final String email_alert_folder_name = "email_alert";
	public final static String[] reminder_interval_string = { "30", "60", "90", "120"};  
	public static final Map<String, Integer> REMINDER_INTERVAL;
	static {
        Map<String, Integer> aMap = new HashMap<String, Integer>();
        aMap.put(reminder_interval_string[0], 30);
        aMap.put(reminder_interval_string[1], 60);
        aMap.put(reminder_interval_string[2], 90);
        aMap.put(reminder_interval_string[3], 120);
        REMINDER_INTERVAL = Collections.unmodifiableMap(aMap);
    }
	
	public String addressee;
	public int reminder_interval = 30;
	
	public EmailAlertData() {
		
	}
	
	public void set_addressee(String to) {
		addressee = to;
	}
	
	public void set_reminder_interval(int interval) {
		reminder_interval = interval;
	}
	
	public String get_addressee() {
		return addressee;
	}
	
	public int get_reminder_interval() {
		return reminder_interval;
	}
}
