package od_monitor.mail;

import java.io.IOException;
import java.nio.ByteBuffer;

import od_monitor.app.data.ExperimentScriptData;
import od_monitor.app.data.SensorDataComposition;
import od_monitor.app.file.FileOperateByteArray;
import od_monitor.app.file.FileOperateObject;

import od_monitor.app.ODMonitorApplication;
import od_monitor.app.R;
import od_monitor.app.R.id;
import od_monitor.app.R.layout;
import od_monitor.experiment.ODCalculate;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
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
	public CheckBox checkBoxAlertInterval;
	public CheckBox checkBoxAlertODValue;
	public EditText editTextAlertODValue;
	
	public Spinner spinnerReminderInterval;
	public ArrayAdapter<String> spinnerReminderIntervalAdapter;
	public EmailAlertData email_set = null;
	private boolean spinner_initial = false;
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
	    
	    button_ok = (Button) findViewById(R.id.button_ok);
	    button_ok.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		try {
        			if (0 == save_mail_setting())
        			    finish();
                } catch (NullPointerException e) {
                    Log.i(Tag, "email setting ok button exception");
                } catch (NumberFormatException ex) {
                	Log.i(Tag, "button_ok NumberFormatException");
                }
        	}
		});
	    
	    editTextFromEmail = (EditText) findViewById(R.id.editTextFromEmail);
	    editTextPassword = (EditText) findViewById(R.id.editTextPassword);
	    editTextToEmail = (EditText) findViewById(R.id.editTextToEmail);
	    editTextSubject = (EditText) findViewById(R.id.editTextSubject);
	    editTextBody = (EditText) findViewById(R.id.editTextBody);
	    
	    if (null == email_set)
	    	email_set = new EmailAlertData();
	  
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
	    
	    editTextFromEmail.addTextChangedListener(new TextWatcher() {
	        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
	        public void afterTextChanged(Editable s) {
	        	button_ok.setTextColor(Color.RED);
		    }
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
	    });
	    
	    editTextPassword.addTextChangedListener(new TextWatcher() {
	        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
	        public void afterTextChanged(Editable s) {
	        	button_ok.setTextColor(Color.RED);
		    }
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
	    });
	    
	    editTextToEmail.addTextChangedListener(new TextWatcher() {
	        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
	        public void afterTextChanged(Editable s) {
	        	button_ok.setTextColor(Color.RED);
		    }
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
	    });
	    
	    editTextSubject.addTextChangedListener(new TextWatcher() {
	        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
	        public void afterTextChanged(Editable s) {
	        	button_ok.setTextColor(Color.RED);
		    }
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
	    });
	    
	    editTextBody.addTextChangedListener(new TextWatcher() {
	        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
	        public void afterTextChanged(Editable s) {
	        	button_ok.setTextColor(Color.RED);
		    }
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
	    });
	  
	    spinnerReminderInterval = (Spinner)findViewById(R.id.spinner_alert_interval);
	    spinnerReminderIntervalAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, android.R.id.text1);
	    spinnerReminderIntervalAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    spinnerReminderInterval.setAdapter(spinnerReminderIntervalAdapter);
	 
	    for (int i = 0; i < EmailAlertData.reminder_interval_string.length; i++) {
	        spinnerReminderIntervalAdapter.add(EmailAlertData.reminder_interval_string[i]);
	        spinnerReminderIntervalAdapter.notifyDataSetChanged();
	    }
	    
	    if (null != email_set.get_alert_interval())
	        spinnerReminderInterval.setSelection(EmailAlertData.REMINDER_INTERVAL_INDEX.get(email_set.get_alert_interval()));
	    else
	    	spinnerReminderInterval.setSelection(0);
	    
	    spinner_initial = true;
	    spinnerReminderInterval.setOnItemSelectedListener(new OnItemSelectedListener() { 
	        public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
	        	if (false == spinner_initial)
	        	    button_ok.setTextColor(Color.RED);
	        	spinner_initial = false;
	        }
	        
	        public void onNothingSelected(AdapterView<?> parentView) {
	            // your code here
	        }
	    });
	    
	    editTextAlertODValue = (EditText) findViewById(R.id.editTextAlertODValue);
	    editTextAlertODValue.setText(email_set.get_alert_od_value_string());
	    editTextAlertODValue.addTextChangedListener(new TextWatcher() {
	        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
	        public void afterTextChanged(Editable s) {
	        	button_ok.setTextColor(Color.RED);
		    }
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
	    });
	    
	    checkBoxAlertInterval = (CheckBox)findViewById(R.id.checkBoxAlertInterval);
	    checkBoxAlertInterval.setChecked(email_set.is_enable_alert_interval());
	    spinnerReminderInterval.setEnabled(email_set.is_enable_alert_interval());
	    checkBoxAlertInterval.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
	            public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
	            	email_set.enable_alert_interval(isChecked);
	            	spinnerReminderInterval.setEnabled(email_set.is_enable_alert_interval());
	            	button_ok.setTextColor(Color.RED);
	            }
	        }
	    );     
	    
	    checkBoxAlertODValue = (CheckBox)findViewById(R.id.checkBoxAlertODValue);
	    checkBoxAlertODValue.setChecked(email_set.is_enable_alert_od_value());
	    editTextAlertODValue.setEnabled(email_set.is_enable_alert_od_value());
	    editTextAlertODValue.setOnFocusChangeListener(new View.OnFocusChangeListener() {
	        public void onFocusChange(View v, boolean hasFocus) {
	            // TODO Auto-generated method stub
	            if (!hasFocus) {
	            	if (editTextAlertODValue.getText().toString().trim().equals("")) {
	            		editTextAlertODValue.setText(email_set.get_alert_od_value_string()); 
	            	} else {
	            		editTextAlertODValue.setError(null);
	            	}
	            }
	        }
	    });
	    
	    checkBoxAlertODValue.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
            	email_set.enable_alert_od_value(isChecked);
            	editTextAlertODValue.setEnabled(email_set.is_enable_alert_od_value());
            	button_ok.setTextColor(Color.RED);
            }
        }); 
	}
	
	public int save_mail_setting() {	
		int ret = 0;
		if (editTextAlertODValue.getText().toString().trim().equals("")) {
			editTextAlertODValue.setError("Please enter value");
			ret = -1;
		} else {
	        FileOperateObject write_file = new FileOperateObject(EmailAlertData.email_alert_folder_name, EmailAlertData.email_alert_file_name);
	 
            try {
			    write_file.create_file(write_file.generate_filename_no_date());
			    email_set.set_fromEmail(editTextFromEmail.getText().toString().trim());
			    email_set.set_fromPassword(editTextPassword.getText().toString().trim());
			    email_set.set_toEmails(editTextToEmail.getText().toString().trim());
			    email_set.set_emailSubject(editTextSubject.getText().toString().trim());
			    email_set.set_emailBody(editTextBody.getText().toString().trim());
			    email_set.set_alert_interval(spinnerReminderInterval.getSelectedItem().toString());
			    double val = Double.parseDouble(editTextAlertODValue.getText().toString().trim());
			    email_set.set_alert_od_value(val);
			    write_file.write_file(email_set);
			    ODMonitorApplication app_data = ((ODMonitorApplication)this.getApplication());
			    app_data.set_mail_alert_load(true);
		    } catch (IOException e) {
			    // TODO Auto-generated catch block
			    e.printStackTrace();
		    }
		}
		
		return ret;
	}
}
