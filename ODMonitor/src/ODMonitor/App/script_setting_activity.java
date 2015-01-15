package ODMonitor.App;

import java.io.IOException;
import java.nio.ByteBuffer;

import ODMonitor.App.data.experiment_script_data;
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

public class script_setting_activity extends Activity {
	public String Tag = "script_setting_activity";
	public Button button_ok;
	public EditText editText_repeat_count;
	public EditText editText_repeat_time;
	public EditText editText_shaker_temperature;
	public EditText editText_shaker_speed;
	public EditText editText_delay;
	
	public Spinner spinner_instruct;
	public Spinner spinner_repeat_from;
	experiment_script_data item_data;
	public int total_item = 0;
	public long item_id = 0;
	public int item_position = 0;
	public ArrayAdapter<String> spinner_repeat_from_Adapter;
	public ArrayAdapter<String> spinner_instruct_Adapter;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.script_setting);
	    
	    Intent intent = getIntent(); 
	    item_data = (experiment_script_data)intent.getSerializableExtra("send_experiment_script_data");
	    total_item = intent.getIntExtra("send_total_item", 0);
	    item_id = intent.getLongExtra("send_item_id", 0);
	    item_position = intent.getIntExtra("send_item_position", 0);
	    
	    String title_string = String.format("Script Setting: %d", item_position+1);
	    setTitle(title_string);
	    
	    editText_repeat_count = (EditText) findViewById(R.id.editText_repeat_count);
	    editText_repeat_time = (EditText) findViewById(R.id.editText_repeat_time);
	    editText_shaker_temperature = (EditText) findViewById(R.id.editText_shaker_temperature);
	    editText_shaker_speed = (EditText) findViewById(R.id.editText_shaker_speed);
	    editText_delay = (EditText) findViewById(R.id.editText_delay);
	    
	    editText_repeat_count.addTextChangedListener(new TextWatcher() {
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
		    	     item_data.set_repeat_count_value(Byte.parseByte(s.toString()));
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
	    
	    editText_repeat_time.addTextChangedListener(new TextWatcher() {
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
		    	     item_data.set_repeat_time_value(Byte.parseByte(s.toString()));
		    	   } catch (NumberFormatException ex) {
		    	      // Do something
		    	   }
		    }

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub
				
			}
	    });
	    
	    editText_shaker_temperature.addTextChangedListener(new TextWatcher() {
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
		    	     item_data.set_shaker_temperature_value(Integer.parseInt(s.toString()));
		    	   } catch (NumberFormatException ex) {
		    	      // Do something
		    	   }
		    }


			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub
				
			}
	    });
	    
	    editText_shaker_speed.addTextChangedListener(new TextWatcher() {
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
		    	     item_data.set_shaker_speed_value(Integer.parseInt(s.toString()));
		    	   } catch (NumberFormatException ex) {
		    	      // Do something
		    	   }
		    }


			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub
				
			}
	    });
	    
	    editText_delay.addTextChangedListener(new TextWatcher() {
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
		    	     item_data.set_delay_value(Integer.parseInt(s.toString()));
		    	   } catch (NumberFormatException ex) {
		    	      // Do something
		    	   }
		    }


			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub
				
			}
	    });
	  
	    spinner_repeat_from = (Spinner)findViewById(R.id.spinner_repeat_from);
        spinner_repeat_from_Adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, android.R.id.text1);
        spinner_repeat_from_Adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_repeat_from.setAdapter(spinner_repeat_from_Adapter);
	    if ((item_data.get_instruct_value() == experiment_script_data.INSTRUCT_REPEAT_COUNT) || (item_data.get_instruct_value() == experiment_script_data.INSTRUCT_REPEAT_TIME)) {
	        for (int i = 0; i < item_position; i++) {
	    	    String str;
	    	    str = String.format("%d", i+1);
	    	    spinner_repeat_from_Adapter.add(str);
	    	    spinner_repeat_from_Adapter.notifyDataSetChanged();
	        }
	        spinner_repeat_from.setSelection(item_data.get_repeat_from_value()-1);
	    }
	    
	    spinner_repeat_from.setOnItemSelectedListener(new OnItemSelectedListener() { 
	        public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
	        	item_data.set_repeat_from_value(position+1);
	        }
	        
	        public void onNothingSelected(AdapterView<?> parentView) {
	            // your code here
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
	    
	    spinner_instruct = (Spinner)findViewById(R.id.spinner_instruct);
	   // ArrayAdapter<String> spinner_instruct_Adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, android.R.id.text1);
	    spinner_instruct_Adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, android.R.id.text1);
	    spinner_instruct_Adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    spinner_instruct.setAdapter(spinner_instruct_Adapter);
	    
	    int size = experiment_script_data.SCRIPT_INSTRUCT.size();
	    for (int i = 0; i < size; i++) {
	    	if (item_position == 0) {
	    		if ((i == experiment_script_data.INSTRUCT_REPEAT_COUNT) || (i == experiment_script_data.INSTRUCT_REPEAT_TIME)) { 
	    			continue;
	    		}
	    	}
	    	spinner_instruct_Adapter.add(experiment_script_data.SCRIPT_INSTRUCT.get(i));
	    }
	    spinner_instruct_Adapter.notifyDataSetChanged();
	    spinner_instruct.setSelection(item_data.get_instruct_value());
	    refresh_UI_object_enable(item_data.get_instruct_string());

	    spinner_instruct.setOnItemSelectedListener(new OnItemSelectedListener() { 
	        public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
	        	Log.i(Tag, "select item: "+ position);
	        	      	
	        	refresh_UI_object_enable(spinner_instruct_Adapter.getItem(position));
	        	item_data.set_instruct_value(position);
	        }

	        public void onNothingSelected(AdapterView<?> parentView) {
	            // your code here
	        }
	    });
	}
	
	public void refresh_UI_object_enable(String instruct_str) {
		int position = 0;
		
		for (int i = 0; i < experiment_script_data.SCRIPT_INSTRUCT.size(); i++) {
    		String s = experiment_script_data.SCRIPT_INSTRUCT.get(i);
    		if (s.equals(instruct_str)) {
    			position = i;
    			break;
    		}
    	}
		
    	spinner_repeat_from.setEnabled(experiment_script_data.SCRIPT_SETTING_ENABLE_LIST.get(position).get(0));
    	spinner_repeat_from_Adapter.clear();
    	if ((position == experiment_script_data.INSTRUCT_REPEAT_COUNT) || (position == experiment_script_data.INSTRUCT_REPEAT_TIME)) { 
    	    for (int i = 0; i < item_position; i++) {
    	        String str;
    	        str = String.format("%d", i+1);
    	        spinner_repeat_from_Adapter.add(str);
    	        spinner_repeat_from_Adapter.notifyDataSetChanged();
            }
    	    spinner_repeat_from.setSelection(item_data.get_repeat_from_value()-1);
    	} 
    	
    	if (experiment_script_data.SCRIPT_SETTING_ENABLE_LIST.get(position).get(1) == false)
    		editText_repeat_count.setText("");
    	else
    		editText_repeat_count.setText(item_data.get_repeat_count_string()); 	
    	editText_repeat_count.setEnabled(experiment_script_data.SCRIPT_SETTING_ENABLE_LIST.get(position).get(1));
    	
    	if (experiment_script_data.SCRIPT_SETTING_ENABLE_LIST.get(position).get(2) == false)
    		editText_repeat_time.setText("");
    	else
    		editText_repeat_time.setText(item_data.get_repeat_time_string());      	
    	editText_repeat_time.setEnabled(experiment_script_data.SCRIPT_SETTING_ENABLE_LIST.get(position).get(2));
    	
    	if (experiment_script_data.SCRIPT_SETTING_ENABLE_LIST.get(position).get(3) == false)
    		editText_shaker_temperature.setText("");
    	else
    		editText_shaker_temperature.setText(item_data.get_shaker_temperature_string());
    	editText_shaker_temperature.setEnabled(experiment_script_data.SCRIPT_SETTING_ENABLE_LIST.get(position).get(3));
    	
    	if (experiment_script_data.SCRIPT_SETTING_ENABLE_LIST.get(position).get(4) == false)
    		editText_shaker_speed.setText("");
    	else
    		editText_shaker_speed.setText(item_data.get_shaker_speed_string());
    	editText_shaker_speed.setEnabled(experiment_script_data.SCRIPT_SETTING_ENABLE_LIST.get(position).get(4));
    	
    	if (experiment_script_data.SCRIPT_SETTING_ENABLE_LIST.get(position).get(5) == false)
    		editText_delay.setText("");
    	else
    		editText_delay.setText(item_data.get_delay_string());
    	editText_delay.setEnabled(experiment_script_data.SCRIPT_SETTING_ENABLE_LIST.get(position).get(5));
	}
	
	public void save_experiment_script() throws NumberFormatException{	
		if (spinner_instruct.getSelectedItemPosition() == experiment_script_data.INSTRUCT_SHAKER_SET_SPEED) {
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
		Log.d(Tag, "save_experiment_script = " + item_id);
	}
}
