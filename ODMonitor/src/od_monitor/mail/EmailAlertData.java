package od_monitor.mail;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.util.Log;
import android.widget.TextView;

public class EmailAlertData implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4876634523641900239L;
	public static final String email_alert_file_name = "email_set";
	public static final String email_alert_folder_name = "email_alert";
	public final static String[] reminder_interval_string = { "30 minute", "60 minute", "90 minute", "120 minute"};  
	public static final Map<String, Integer> REMINDER_INTERVAL;
	public static final Map<String, Integer> REMINDER_INTERVAL_INDEX;
	static {
        Map<String, Integer> aMap = new HashMap<String, Integer>();
        aMap.put(reminder_interval_string[0], 30);
        aMap.put(reminder_interval_string[1], 60);
        aMap.put(reminder_interval_string[2], 90);
        aMap.put(reminder_interval_string[3], 120);
        REMINDER_INTERVAL = Collections.unmodifiableMap(aMap);
        
        Map<String, Integer> aMapString = new HashMap<String, Integer>();
        aMapString.put(reminder_interval_string[0], 0);
        aMapString.put(reminder_interval_string[1], 1);
        aMapString.put(reminder_interval_string[2], 2);
        aMapString.put(reminder_interval_string[3], 3);
        REMINDER_INTERVAL_INDEX = Collections.unmodifiableMap(aMapString);
    }
	
	private String fromEmail;
	private String fromPassword;
	private String toEmails;
	private String emailSubject = "OD Monitor Experiment Alert";
	private String emailBody = "Experimental data on the attached file!";
	private String reminder_interval = reminder_interval_string[0];
	
	public EmailAlertData() {
		
	}
	
	public void set_fromEmail(String from) {
		fromEmail = from;
	}
	
	public void set_fromPassword(String password) {
		fromPassword = password;
	}
	
	public void set_toEmails(String to) {
		toEmails = to;
	}
	
	public void set_emailSubject(String subject) {
		emailSubject = subject;
	}
	
	public void set_emailBody(String body) {
		emailBody = body;
	}
	
	public void set_reminder_interval(String interval) {
		reminder_interval = interval;
	}
	
	public String get_fromEmail() {
		return fromEmail;
	}
	
	public String get_fromPassword() {
		return fromPassword;
	}
	
	public String get_toEmails() {
		return toEmails;
	}
	
	public String get_emailSubject() {
		return emailSubject;
	}
	
	public String get_emailBody() {
		return emailBody;
	}
	
	public String get_reminder_interval() {
		return reminder_interval;
	}
}
