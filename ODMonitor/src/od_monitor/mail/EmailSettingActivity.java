package od_monitor.mail;

import java.io.IOException;
import java.nio.ByteBuffer;

import od_monitor.app.data.ExperimentScriptData;
import od_monitor.app.data.SensorDataComposition;
import od_monitor.app.file.FileOperateByteArray;
import od_monitor.app.file.FileOperateObject;

import od_monitor.app.R;
import od_monitor.app.R.id;
import od_monitor.app.R.layout;
import od_monitor.experiment.ODCalculate;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

public class EmailSettingActivity extends Activity {
	public String Tag = "EmailSettingActivity";
	public Button button_ok;
	public EditText editTextFromEmail;
	public EditText editTextPassword;
	public EditText editTextToEmail;
	public EditText editTextSubject;
	public EditText editTextBody;
	
	public Spinner spinnerReminderInterval;
	public ArrayAdapter<String> spinnerReminderIntervalAdapter;
	public EmailAlertData email_set = null;
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.email_setting);
	    
	    String title_string = "Email Alert Setting";
	    setTitle(title_string);
	    
	    FileOperateObject read_file = new FileOperateObject(EmailAlertData.email_alert_folder_name, EmailAlertData.email_alert_file_name);
	    try {
			read_file.open_read_file(read_file.generate_filename_no_date());
			email_set = (EmailAlertData)read_file.read_file_object();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	    
	    editTextFromEmail = (EditText) findViewById(R.id.editTextFromEmail);
	    editTextPassword = (EditText) findViewById(R.id.editTextPassword);
	    editTextToEmail = (EditText) findViewById(R.id.editTextToEmail);
	    editTextSubject = (EditText) findViewById(R.id.editTextSubject);
	    editTextBody = (EditText) findViewById(R.id.editTextBody);
	    if (null != email_set) {
	        String fromEmail = email_set.get_fromEmail();
	        if (null != fromEmail)
	        	editTextFromEmail.setText(fromEmail);
	        
	        String fromPassword = email_set.get_fromPassword();
	        if (null != fromPassword)
	        	editTextPassword.setText(fromPassword);
	        
	        String toEmails = email_set.get_toEmails();
	        if (null != toEmails)
	        	editTextToEmail.setText(toEmails);
	        
	        String emailSubject = email_set.get_emailSubject();
	        if (null != emailSubject)
	        	editTextSubject.setText(emailSubject);
	        
	        String emailBody = email_set.get_emailBody();
	        if (null != emailBody)
	        	editTextBody.setText(emailBody);
	    } else {
	    	email_set = new EmailAlertData();
	    	editTextSubject.setText(email_set.get_emailSubject());
	    	editTextBody.setText(email_set.get_emailBody());
	    }
	  
	    spinnerReminderInterval = (Spinner)findViewById(R.id.spinner_reminder_interval);
	    spinnerReminderIntervalAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, android.R.id.text1);
	    spinnerReminderIntervalAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    spinnerReminderInterval.setAdapter(spinnerReminderIntervalAdapter);
	 
	    for (int i = 0; i < EmailAlertData.reminder_interval_string.length; i++) {
	        spinnerReminderIntervalAdapter.add(EmailAlertData.reminder_interval_string[i]);
	        spinnerReminderIntervalAdapter.notifyDataSetChanged();
	    }
	    spinnerReminderInterval.setSelection(EmailAlertData.REMINDER_INTERVAL_INDEX.get(email_set.get_reminder_interval()));
	    
	    spinnerReminderInterval.setOnItemSelectedListener(new OnItemSelectedListener() { 
	        public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
	 
	        }
	        
	        public void onNothingSelected(AdapterView<?> parentView) {
	            // your code here
	        }
	    });
	    
	    button_ok = (Button) findViewById(R.id.button_ok);
	    button_ok.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		try {
        			save_mail_setting();
        			finish();
                } catch (NullPointerException e) {
                    Log.i(Tag, "email setting ok button exception");
                } catch (NumberFormatException ex) {
                	Log.i(Tag, "button_ok NumberFormatException");
                }
        	}
		});
	}
	
	public void save_mail_setting() {	
	    FileOperateObject write_file = new FileOperateObject(EmailAlertData.email_alert_folder_name, EmailAlertData.email_alert_file_name);
	 
        try {
			write_file.create_file(write_file.generate_filename_no_date());
			email_set.set_fromEmail(editTextFromEmail.getText().toString().trim());
			email_set.set_fromPassword(editTextPassword.getText().toString().trim());
			email_set.set_toEmails(editTextToEmail.getText().toString().trim());
			email_set.set_emailSubject(editTextSubject.getText().toString().trim());
			email_set.set_emailBody(editTextBody.getText().toString().trim());
			email_set.set_reminder_interval(spinnerReminderInterval.getSelectedItem().toString());
			write_file.write_file(email_set);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
		
		
		/*if (spinnerReminderInterval.getSelectedItemPosition() == ExperimentScriptData.INSTRUCT_SHAKER_SET_SPEED) {
			try {
			    int shaker_speed = Integer.parseInt(editText_shaker_speed.getText().toString());
			    if (shaker_speed < 20) {
			    	shaker_speed = 20;
			    } else if (shaker_speed > 255) {
			    	shaker_speed = 255;
			    }
			    
			    item_data.set_shaker_speed_value(shaker_speed);
			} catch (NumberFormatException ex) {
	    	      // Do something
				Builder WarrningDialog = new AlertDialog.Builder(this);
				WarrningDialog.setTitle("Warrning");
				WarrningDialog.setMessage("please enter correct number");
				WarrningDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
		            public void onClick(DialogInterface dialog, int i) {
		            }
		        });
				WarrningDialog.show();
				Log.d(Tag, "NumberFormatException");
				throw new NumberFormatException("shaker_speed format exception");
	    	}
		}
		
		Intent intent = new Intent();
		intent.putExtra("return_experiment_script_data", item_data); //value should be your string from the edittext
		intent.putExtra("return_item_id", item_id);
		intent.putExtra("return_item_position", item_position);
		setResult(RESULT_OK, intent); //The data you want to send back
		Log.d(Tag, "save_experiment_script = " + item_id);*/
	}
}
