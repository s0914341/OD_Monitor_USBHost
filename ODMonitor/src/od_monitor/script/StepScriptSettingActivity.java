package od_monitor.script;

import java.io.IOException;
import java.nio.ByteBuffer;

import od_monitor.app.data.ExperimentScriptData;
import od_monitor.app.data.StepExperimentScriptData;

import od_monitor.app.ODMonitorApplication;
import od_monitor.app.R;
import od_monitor.app.R.id;
import od_monitor.app.R.layout;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

public class StepScriptSettingActivity extends Activity {
	public String Tag = "StepScriptSettingActivity";
	public Button button_ok;
	public EditText editText_high_speed_rpm;
	public EditText editText_high_speed_operation_duration;
	public EditText editText_low_speed_rpm;
	public EditText editText_low_speed_operation_duration;
	public EditText editText_temperature;
	public EditText editText_experiment_operation_duration_hour;
	public EditText editText_experiment_operation_duration_min;
	
	public StepExperimentScriptData item_data;
	public int total_item = 0;
	public long item_id = 0;
	public int item_position = 0;
	public ArrayAdapter<String> spinner_repeat_from_Adapter;
	public ArrayAdapter<String> spinner_instruct_Adapter;
	private boolean data_change_flag = false;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    ODMonitorApplication app_data = ((ODMonitorApplication)this.getApplication());
	    app_data.addActivity(this);
	    setContentView(R.layout.step_script_setting);
	    
	    Intent intent = getIntent(); 
	    item_data = (StepExperimentScriptData)intent.getSerializableExtra("send_step_experiment_script_data");
	    total_item = intent.getIntExtra("send_total_item", 0);
	    item_id = intent.getLongExtra("send_item_id", 0);
	    item_position = intent.getIntExtra("send_item_position", 0);
	    
	    String title_string = "Step Setting: " + (item_position+1);
	    setTitle(title_string);
	    
	    editText_high_speed_rpm = (EditText) findViewById(R.id.editTextHighSpeedRPM);
	    editText_high_speed_rpm.setText(item_data.get_high_speed_rpm_string()); 
	    
	    editText_high_speed_operation_duration = (EditText) findViewById(R.id.editTextHighSpeedOperationDuration);
	    editText_high_speed_operation_duration.setText(item_data.get_high_speed_operation_duration_string()); 
	    
	    editText_low_speed_rpm = (EditText) findViewById(R.id.editTextLowSpeedRPM);
	    editText_low_speed_rpm.setText(item_data.get_low_speed_rpm_string()); 
	    
	    editText_low_speed_operation_duration = (EditText) findViewById(R.id.editTextLowSpeedOperationDuration);
	    editText_low_speed_operation_duration.setText(item_data.get_low_speed_operation_duration_string()); 
	    
	    editText_temperature = (EditText) findViewById(R.id.editTextTemperature);
	    editText_temperature.setText(item_data.get_temperature_string()); 
	    
	    String duration_hour = "" + item_data.get_experiment_operation_duration()/60;
	    String duration_min = "" + item_data.get_experiment_operation_duration()%60;
	    editText_experiment_operation_duration_hour = (EditText) findViewById(R.id.editExperimentOperationDurationHour);
	    editText_experiment_operation_duration_hour.setText(duration_hour); 
	    
	    editText_experiment_operation_duration_min = (EditText) findViewById(R.id.editExperimentOperationDurationMin);
	    editText_experiment_operation_duration_min.setText(duration_min); 
	   
	    
	    editText_high_speed_rpm.setOnFocusChangeListener(new View.OnFocusChangeListener() {
	        public void onFocusChange(View v, boolean hasFocus) {
	            // TODO Auto-generated method stub
	            if (!hasFocus) {
	            	if (editText_high_speed_rpm.getText().toString().trim().equals("")) {
	            		editText_high_speed_rpm.setText(item_data.get_high_speed_rpm_string()); 
	            	} else {
	            	    int val = Integer.parseInt(editText_high_speed_rpm.getText().toString().trim());
	                    if ((val > 300) || (val < 50)) {
	        	            editText_high_speed_rpm.setError("50~300");
	                    } else {
	                        editText_high_speed_rpm.setError(null);
	                    }
	            	}
	            }
	        }
	    });
	    
	    editText_high_speed_rpm.addTextChangedListener(new TextWatcher() {
	        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
	        public void afterTextChanged(Editable s) {
	        	data_change_flag = true;
		    }
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
	    });
	    
	    editText_high_speed_operation_duration.setOnFocusChangeListener(new View.OnFocusChangeListener() {
	        public void onFocusChange(View v, boolean hasFocus) {
	            // TODO Auto-generated method stub
	            if (!hasFocus) {
	            	if (editText_high_speed_operation_duration.getText().toString().trim().equals("")) {
	            		editText_high_speed_operation_duration.setText(item_data.get_high_speed_operation_duration_string()); 
	            	} else {
	            	    int val = Integer.parseInt(editText_high_speed_operation_duration.getText().toString().trim());
	                    if ((val > 99999) || (val < 10)) {
	                	    editText_high_speed_operation_duration.setError("10~99999");
	                    } else {
	                	    editText_high_speed_operation_duration.setError(null);
	                    }
	            	}
	            }
	        }
	    });
	    
	    editText_high_speed_operation_duration.addTextChangedListener(new TextWatcher() {
	        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
	        public void afterTextChanged(Editable s) {
	        	data_change_flag = true;
		    }
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
	    });
	    
	    editText_temperature.setOnFocusChangeListener(new View.OnFocusChangeListener() {
	        public void onFocusChange(View v, boolean hasFocus) {
	            // TODO Auto-generated method stub
	            if (!hasFocus) {
	            	if (editText_temperature.getText().toString().trim().equals("")) {
	            		editText_temperature.setText(item_data.get_temperature_string()); 
	            	} else {
	            	    int val = Integer.parseInt(editText_temperature.getText().toString().trim());
	                    if ((val > 70) || (val < 0)) {
	                	    editText_temperature.setError("0~70");
	                    } else {
	                	    editText_temperature.setError(null);
	                    }
	                    
	            	}
	            }
	        }
	    });
	    
	    editText_temperature.addTextChangedListener(new TextWatcher() {
	        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
	        public void afterTextChanged(Editable s) {
	        	data_change_flag = true;
		    }
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
	    });
	    
	    editText_low_speed_rpm.setOnFocusChangeListener(new View.OnFocusChangeListener() {
	        public void onFocusChange(View v, boolean hasFocus) {
	            // TODO Auto-generated method stub
	            if (!hasFocus) {
	            	if (editText_low_speed_rpm.getText().toString().trim().equals("")) {
	            		editText_low_speed_rpm.setText(item_data.get_low_speed_rpm_string()); 
	            	} else {
	            	    int val = Integer.parseInt(editText_low_speed_rpm.getText().toString().trim());
	                    if ((val > 300) || (val < 50)) {
	                	    editText_low_speed_rpm.setError("50~300");
	                    } else {
	                	    editText_low_speed_rpm.setError(null);
	                    }
	            	}
	            }
	        }
	    });
	    
	    editText_low_speed_rpm.addTextChangedListener(new TextWatcher() {
	        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
	        public void afterTextChanged(Editable s) {
	        	data_change_flag = true;
		    }
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
	    });
	    
	    editText_low_speed_operation_duration.setOnFocusChangeListener(new View.OnFocusChangeListener() {
	        public void onFocusChange(View v, boolean hasFocus) {
	            // TODO Auto-generated method stub
	            if (!hasFocus) {
	            	if (editText_low_speed_operation_duration.getText().toString().trim().equals("")) {
	            		editText_low_speed_operation_duration.setText(item_data.get_low_speed_operation_duration_string()); 
	            	} else {
	            	    int val = Integer.parseInt(editText_low_speed_operation_duration.getText().toString().trim());
	                    if ((val > 99999) || (val < 10)) {
	                	    editText_low_speed_operation_duration.setError("10~99999");
	                    } else {
	                	    editText_low_speed_operation_duration.setError(null);
	                    }
	            	}
	            }
	        }
	    });
	    
	    editText_low_speed_operation_duration.addTextChangedListener(new TextWatcher() {
	        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
	        public void afterTextChanged(Editable s) {
	        	data_change_flag = true;
		    }
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
	    });
	    
	    editText_experiment_operation_duration_hour.setOnFocusChangeListener(new View.OnFocusChangeListener() {
	        public void onFocusChange(View v, boolean hasFocus) {
	            // TODO Auto-generated method stub
	            if (!hasFocus) {
	            	if (editText_experiment_operation_duration_hour.getText().toString().trim().equals("")) {
	            		String duration_hour = "" + item_data.get_experiment_operation_duration()/60;
	            		editText_experiment_operation_duration_hour.setText(duration_hour); 
	            	} else {
	            	    int val = Integer.parseInt(editText_experiment_operation_duration_hour.getText().toString().trim());
	                    if ((val > 90) || (val < 0)) {
	                	    editText_experiment_operation_duration_hour.setError("0~90");
	                    } else {
	                	    editText_experiment_operation_duration_hour.setError(null);
	                    }
	            	}
	            }
	        }
	    });
	    
	    editText_experiment_operation_duration_hour.addTextChangedListener(new TextWatcher() {
	        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
	        public void afterTextChanged(Editable s) {
	        	data_change_flag = true;
		    }
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
	    });
	    
	    editText_experiment_operation_duration_min.setOnFocusChangeListener(new View.OnFocusChangeListener() {
	        public void onFocusChange(View v, boolean hasFocus) {
	            // TODO Auto-generated method stub
	            if (!hasFocus) {
	            	if (editText_experiment_operation_duration_min.getText().toString().trim().equals("")) {
	            		String duration_min = "" + item_data.get_experiment_operation_duration()%60;
	            		editText_experiment_operation_duration_min.setText(duration_min); 
	            	} else {
	                 	int val = Integer.parseInt(editText_experiment_operation_duration_min.getText().toString().trim());
	                    if ((val > 59) || (val < 0)) {
	                	    editText_experiment_operation_duration_min.setError("0~59");
	                    } else {
	                	    editText_experiment_operation_duration_min.setError(null);
	                    }
	            	}
	            }
	        }
	    });
	    
	    editText_experiment_operation_duration_min.addTextChangedListener(new TextWatcher() {
	        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
	        public void afterTextChanged(Editable s) {
	        	data_change_flag = true;
		    }
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
	    });
	    
	    button_ok = (Button) findViewById(R.id.button_ok);
	    button_ok.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		try {
        			save_experiment_script();
        			finish();
                } catch (NullPointerException e) {
                    Log.i(Tag, "script setting ok button exception");
                } catch (NumberFormatException ex) {
                	Log.i(Tag, "button_ok NumberFormatException");
                }
        	}
		});
	}
	
	public void save_experiment_script() throws NumberFormatException{	
		/*if (spinner_instruct.getSelectedItemPosition() == experiment_script_data.INSTRUCT_SHAKER_SET_SPEED) {
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
		}*/
		boolean fail = false; 
		int val = Integer.parseInt(editText_high_speed_rpm.getText().toString().trim());
        if ((val > 300) || (val < 50)) {
        	editText_high_speed_rpm.setError("50~300");
        	fail = true;
        } else {
        	editText_high_speed_rpm.setError(null);
        	item_data.set_high_speed_rpm(val);
        }
        
        val = Integer.parseInt(editText_high_speed_operation_duration.getText().toString().trim());
        if ((val > 99999) || (val < 10)) {
        	editText_high_speed_operation_duration.setError("10~99999");
        	fail = true;
        } else {
        	editText_high_speed_operation_duration.setError(null);
        	item_data.set_high_speed_operation_duration(val);
        }
      
        val = Integer.parseInt(editText_temperature.getText().toString().trim());
        if ((val > 70) || (val < 0)) {
        	editText_temperature.setError("0~70");
        	fail = true;
        } else {
        	editText_temperature.setError(null);
        	item_data.set_temperature(val);
        }
     
        val = Integer.parseInt(editText_low_speed_rpm.getText().toString().trim());
        if ((val > 300) || (val < 50)) {
        	editText_low_speed_rpm.setError("50~300");
        	fail = true;
        } else {
        	editText_low_speed_rpm.setError(null);
        	item_data.set_low_speed_rpm(val);
        }
        
        val = Integer.parseInt(editText_low_speed_operation_duration.getText().toString().trim());
        if ((val > 99999) || (val < 10)) {
        	editText_low_speed_operation_duration.setError("10~99999");
        	fail = true;
        } else {
        	editText_low_speed_operation_duration.setError(null);
        	item_data.set_low_speed_operation_duration(val);
        }
        
        int duration_hour = Integer.parseInt(editText_experiment_operation_duration_hour.getText().toString().trim());
        if ((duration_hour > 90) || (duration_hour < 0)) {
        	editText_experiment_operation_duration_hour.setError("0~90");
        	fail = true;
        } else {
        	editText_experiment_operation_duration_hour.setError(null);
        }
        
        int duration_min = Integer.parseInt(editText_experiment_operation_duration_min.getText().toString().trim());
        if ((duration_min > 59) || (duration_min < 0)) {
        	editText_experiment_operation_duration_min.setError("0~59");
        	fail = true;
        } else {
        	editText_experiment_operation_duration_min.setError(null);
        }
        
        if (false == fail) {
        	if ((0 == duration_hour) && (0 == duration_min)) {
        		fail = true;
        	} else {
        		item_data.set_experiment_operation_duration(duration_hour*60 + duration_min);
        	}
        }
        
        if (false == fail) {
		    Intent intent = new Intent();
		    intent.putExtra("data_change_flag", data_change_flag);
		    intent.putExtra("return_step_experiment_script_data", item_data); //value should be your string from the edittext
		    intent.putExtra("return_item_id", item_id);
		    intent.putExtra("return_item_position", item_position);
		    setResult(RESULT_OK, intent); //The data you want to send back
		    Log.d(Tag, "save_experiment_script = " + item_id);
        } else {
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
}
