package od_monitor.script;

import java.io.IOException;
import java.nio.ByteBuffer;

import od_monitor.app.data.experiment_script_data;
import od_monitor.app.data.step_experiment_script_data;

import ODMonitor.App.R;
import ODMonitor.App.R.id;
import ODMonitor.App.R.layout;
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

public class step_script_setting_activity extends Activity {
	public String Tag = "step_script_setting_activity";
	public Button button_ok;
	public EditText editText_high_speed_rpm;
	public EditText editText_high_speed_operation_duration;
	public EditText editText_low_speed_rpm;
	public EditText editText_low_speed_operation_duration;
	public EditText editText_temperature;
	public EditText editText_experiment_operation_duration;
	
	public step_experiment_script_data item_data;
	public int total_item = 0;
	public long item_id = 0;
	public int item_position = 0;
	public ArrayAdapter<String> spinner_repeat_from_Adapter;
	public ArrayAdapter<String> spinner_instruct_Adapter;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.step_script_setting);
	    
	    Intent intent = getIntent(); 
	    item_data = (step_experiment_script_data)intent.getSerializableExtra("send_step_experiment_script_data");
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
	    
	    editText_experiment_operation_duration = (EditText) findViewById(R.id.editExperimentOperationDuration);
	    editText_experiment_operation_duration.setText(item_data.get_experiment_operation_duration_string()); 
	   
	    
	    editText_high_speed_rpm.addTextChangedListener(new TextWatcher() {
	        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

	        }

	        public void afterTextChanged(Editable s) {
		        try {
		    	     int val = Integer.parseInt(s.toString());
		    	     if(val > 255) {
		    	        s.replace(0, s.length(), "255", 0, 3);
		    	     } else if(val < 1) {
		    	        s.replace(0, s.length(), "1", 0, 1);
		    	     }
		    	     item_data.set_high_speed_rpm(Integer.parseInt(s.toString()));
		    	     Log.i(Tag, "afterTextChanged");
		    	   } catch (NumberFormatException ex) {
		    	      // Do something
		    	   }
		    }

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub
				Log.i(Tag, "onTextChanged");
			}
	    });
	    
	    editText_high_speed_operation_duration.addTextChangedListener(new TextWatcher() {
	        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

	        }
	      
	        public void afterTextChanged(Editable s) {
		        try {
		    	     int val = Integer.parseInt(s.toString());
		    	     if(val > 86400) {
		    	        s.replace(0, s.length(), "2147483000", 0, 10);
		    	     } else if(val < 1) {
		    	        s.replace(0, s.length(), "1", 0, 1);
		    	     }
		    	     item_data.set_high_speed_operation_duration(Integer.parseInt(s.toString()));
		    	   } catch (NumberFormatException ex) {
		    	      // Do something
		    	   }
		    }

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub
				
			}
	    });
	    
	    editText_temperature.addTextChangedListener(new TextWatcher() {
	        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

	        }

	      
	        public void afterTextChanged(Editable s) {
		        try {
		    	     int val = Integer.parseInt(s.toString());
		    	     if(val > 255) {
		    	        s.replace(0, s.length(), "255", 0, 3);
		    	     } else if(val < 1) {
		    	        s.replace(0, s.length(), "1", 0, 1);
		    	     }
		    	     item_data.set_temperature(Integer.parseInt(s.toString()));
		    	   } catch (NumberFormatException ex) {
		    	      // Do something
		    	   }
		    }


			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub
				
			}
	    });
	    
	    editText_low_speed_rpm.addTextChangedListener(new TextWatcher() {
	        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

	        }

	      
	        public void afterTextChanged(Editable s) {
		        try {
		    	     int val = Integer.parseInt(s.toString());
		    	     if(val > 255) {
		    	        s.replace(0, s.length(), "255", 0, 3);
		    	     } else if(val < 1) {
		    	        s.replace(0, s.length(), "1", 0, 1);
		    	     }
		    	     item_data.set_low_speed_rpm(Integer.parseInt(s.toString()));
		    	   } catch (NumberFormatException ex) {
		    	      // Do something
		    	   }
		    }


			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub
				
			}
	    });
	    
	    editText_low_speed_operation_duration.addTextChangedListener(new TextWatcher() {
	        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

	        }

	      
	        public void afterTextChanged(Editable s) {
		        try {
		    	     int val = Integer.parseInt(s.toString());
		    	     if(val > 255) {
		    	        s.replace(0, s.length(), "255", 0, 3);
		    	     } else if(val < 1) {
		    	        s.replace(0, s.length(), "1", 0, 1);
		    	     }
		    	     item_data.set_low_speed_operation_duration(Integer.parseInt(s.toString()));
		    	   } catch (NumberFormatException ex) {
		    	      // Do something
		    	   }
		    }


			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub
				
			}
	    });
	    
	    editText_experiment_operation_duration.addTextChangedListener(new TextWatcher() {
	        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

	        }
	      
	        public void afterTextChanged(Editable s) {
		        try {
		    	     int val = Integer.parseInt(s.toString());
		    	     if(val > 86400) {
		    	        s.replace(0, s.length(), "2147483000", 0, 10);
		    	     } else if(val < 1) {
		    	        s.replace(0, s.length(), "1", 0, 1);
		    	     }
		    	     item_data.set_experiment_operation_duration(Integer.parseInt(s.toString()));
		    	   } catch (NumberFormatException ex) {
		    	      // Do something
		    	   }
		    }

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub
				
			}
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
		
		Intent intent = new Intent();
		intent.putExtra("return_step_experiment_script_data", item_data); //value should be your string from the edittext
		intent.putExtra("return_item_id", item_id);
		intent.putExtra("return_item_position", item_position);
		setResult(RESULT_OK, intent); //The data you want to send back
		Log.d(Tag, "save_experiment_script = " + item_id);
	}
}
