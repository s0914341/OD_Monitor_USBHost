package od_monitor.script;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import od_monitor.app.data.ExperimentScriptData;
import od_monitor.app.data.StepExperimentScriptData;
import od_monitor.app.file.FileOperateByteArray;
import od_monitor.script.SwipeDismissListViewTouchListener.DismissCallbacks;

import org.achartengine.chartdemo.demo.chart.IDemoChart;
import org.achartengine.chartdemo.demo.chart.ODChartBuilder;

import od_monitor.app.ODMonitorActivity;
import od_monitor.app.ODMonitorApplication;
import od_monitor.app.R;
import od_monitor.app.R.array;
import od_monitor.app.R.drawable;
import od_monitor.app.R.id;
import od_monitor.app.R.layout;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class StepScriptActivityList extends Activity {
	public static String Tag = "StepScriptActivityList";
	private final static String key_experiment = "experiment";
	private final static String key_picture = "picture";
	private final static String key_index = "index"; 
	private final static String key_step = "step"; 
	private final static String key_high_speed_rpm = "high_speed_rpm"; 
	private final static String key_high_speed_duration = "high_speed_duration"; 
	private final static String key_low_speed_rpm = "low_speed_rpm"; 
	private final static String key_low_speed_duration = "low_speed_duration"; 
	private final static String key_temperature = "temperature"; 
	private final static String key_operation_duration = "operation_duration"; 
	
	private static final int INSERT_BEFORE=Menu.FIRST-1;  
    private static final int DELETE=Menu.FIRST;  
    private static final int INSERT_AFTER=Menu.FIRST+1;  
	
	private final static int PICK_CONTACT_REQUEST = 0;
	
	private final static int SCRIPT_HEADER_SIZE = 5;
	private final static byte SCRIPT_HEADER = (byte) 0xFF;
	private final static int TEXT_DEFAULT_COLOR = Color.BLACK;
	private final static int TEXT_REMINDER_SAVE_COLOR = Color.RED;
	List<HashMap<String,Object>> list = new ArrayList<HashMap<String,Object>>();
	public HashMap<Object, Object> experiment_item = new HashMap<Object, Object>();
	public SimpleAdapter adapter;
	public ListView list_view;
	Button button_add_item;
	Button button_clear_all;
	Button button_save_script;
	Button button_load_script;
	SwipeDismissListViewTouchListener touchListener;
	
	private static final int[] mPics=new int[]{
        R.drawable.experiment_step
    };
	
	/** Called when the activity is first created. */

	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    ODMonitorApplication app_data = ((ODMonitorApplication)this.getApplication());
	    app_data.addActivity(this);
	  //把資料加入ArrayList中
	    setContentView(R.layout.step_script_layout);
	    list_view = (ListView) findViewById(R.id.listViewStep);
	    
	    if (0 != load_script_to_step(list, experiment_item)) {
	        add_default_experiment_script();
	    }
	    
	  //新增SimpleAdapter
	    adapter = new SimpleAdapter(this, list, R.layout.step_script_list,
	                                new String[] {key_picture, key_index, key_step, key_high_speed_rpm, key_high_speed_duration, key_temperature, key_low_speed_rpm, key_low_speed_duration, key_operation_duration},
	                                new int[] { R.id.imageViewStepPicture, R.id.textViewStepIndex, R.id.textViewStep, R.id.textViewHighSpeed, R.id.textViewHighSpeedDuration, R.id.textViewTemperature, R.id.textViewLowSpeed, R.id.textViewLowSpeedDuration, R.id.textViewOperationDuration } );
	    
	    //listview物件使用setAdapter方法（比對ListActivity是用setListAdapter）
	    list_view.setAdapter(adapter);
	    
	    //ListActivity設定adapter
	   // setListAdapter( adapter );
	    
	    //啟用按鍵過濾功能，這兩行都會進行過濾
	    list_view.setTextFilterEnabled(true);
	   // getListView().setTextFilterEnabled(true);
	   
	    registerForContextMenu(list_view);
	    
	    touchListener = new SwipeDismissListViewTouchListener(
	    		        list_view,
	    		        new SwipeDismissListViewTouchListener.DismissCallbacks() {
	    		            public void onDismiss(ListView listView, int[] reverseSortedPositions) {
	    		                for (int position : reverseSortedPositions) {
	    		                    delete_instruct(position, experiment_item, list);
	    		                    Log.i(Tag, "onDismiss position:"+position);
	    		                }
	    		                adapter.notifyDataSetChanged();
	    		            }

							public boolean canDismiss(int position) {
							    // TODO Auto-generated method stub
								Log.i(Tag, "canDismiss");
								return true;
							}
	    		        });
	    
	   
	    list_view.setOnTouchListener(touchListener);
	    list_view.setOnScrollListener(touchListener.makeScrollListener());
	    
	    list_view.setOnItemClickListener(new OnItemClickListener() {
	    	 
	        public void onItemClick(AdapterView<?> arg0, View view,
	                int position, long id) {
	            Log.d(Tag, "ListViewItem id = " + id);
	            Log.d(Tag, "ListViewItem position= " + position);
	          //  if (touchListener.isDismissing() == false)
	                show_script_setting_dialog(id, position);
	        }
	    });
	    
        //Typeface cFont = Typeface.createFromAsset(getAssets(), "fonts/Sansation-Bold.ttf");
	    button_add_item = (Button) findViewById(R.id.button_add);
	  //  button_add_item.setTypeface(cFont);
	    button_add_item.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		try {
        			add_new_instruct(list.size(),  experiment_item, list);
                    adapter.notifyDataSetChanged();
                } catch (NullPointerException e) {
                    Log.i(Tag, "Tried to add null value");
                }
        	}
		});
	    
	    button_clear_all = (Button) findViewById(R.id.button_clear_all);
	    //button_clear_all.setTypeface(cFont);
	    button_clear_all.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		try {
        			list.clear();
        			experiment_item.clear();
        		    adapter.notifyDataSetChanged();
        		    button_save_script.setTextColor(TEXT_REMINDER_SAVE_COLOR);
                } catch (NullPointerException e) {
                    Log.i(Tag, "Tried to clear all exception");
                }
        	}
		});
	    
	    button_save_script = (Button) findViewById(R.id.button_save);
	    //button_save_script.setTypeface(cFont);
	    button_save_script.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		save_script_setting_to_file();
        	}
		});
	    
	    button_load_script = (Button) findViewById(R.id.button_load);
	    //button_load_script.setTypeface(cFont);
	    button_load_script.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {	
        		if (0 == load_script_to_step(list, experiment_item)) {
        		    adapter.notifyDataSetChanged();
        		    button_save_script.setTextColor(TEXT_DEFAULT_COLOR);
                    Toast.makeText(StepScriptActivityList.this, "Load script success!", Toast.LENGTH_SHORT).show(); 
        		} else {
        			Toast.makeText(StepScriptActivityList.this, "No script file existed!", Toast.LENGTH_SHORT).show(); 
        		}
        	}
		});
    }
	
	public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {   //確定按下退出鍵
        	ColorStateList mList = button_save_script.getTextColors();
        	int color = mList.getDefaultColor();
        	if (Color.RED == color)
                ConfirmExit();
        	else 
        		finish();
            return true;  
        }  
        
        return super.onKeyDown(keyCode, event);  
    }

    public void ConfirmExit(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(StepScriptActivityList.this).setIcon(R.drawable.alert);
        alertDialog.setTitle("Exit Script Setting");
        alertDialog.setMessage("Do you want to save and exit?\n\nYes: save and exit\nNo: exit");
        
        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int i) {
            	save_script_setting_to_file();
            	finish();
            }
        });
        
        alertDialog.setNeutralButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int i) {
            	finish();
            }
        });
        
        alertDialog.setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int i) {

            }
        });

        alertDialog.show();
    }
    
    public void save_script_setting_to_file() {
    	/* check repeat instruction of script if is over 3 recursive level */
    	/*	if (3 <= check_script_recursive()) {
    			 Toast.makeText(step_script_activity_list.this, "repeat recursive over 3 level", Toast.LENGTH_SHORT).show(); 
    			 return;
    		}*/
    	button_save_script.setTextColor(TEXT_DEFAULT_COLOR);
    	FileOperateByteArray write_file = new FileOperateByteArray("ExperimentScript", "ExperimentScript", true);
    	try {
    		write_file.delete_file(write_file.generate_filename_no_date());
    		write_file.create_file(write_file.generate_filename_no_date());
    		byte[] header = new byte[5];
    		int total_step_count = list.size();
    		int total_instruct_count = total_step_count*StepExperimentScriptData.TOTAL_INSTRUCT_COUNT + 2; //need add power on & power off instruct
    		{
    			ByteBuffer byteBuffer = ByteBuffer.allocate(4);
    			byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

    			byte[] total_step_count_bytes = byteBuffer.putInt(total_instruct_count).array();
    			System.arraycopy(total_step_count_bytes, 0, header, 1, 4);
    			header[0] = SCRIPT_HEADER;
    			write_file.write_file(header);
    		}

    		int current_instruct_index = 1;
    		{
    			ExperimentScriptData shaker_on = new ExperimentScriptData();
    			shaker_on.set_instruct_value(ExperimentScriptData.INSTRUCT_SHAKER_ON);
    			ByteBuffer byteBuffer = ByteBuffer.allocate(4);
    			byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

    			byte[] buffer = shaker_on.get_buffer();
    			byte[] index_bytes = byteBuffer.putInt(current_instruct_index).array();
    			current_instruct_index++;
    			System.arraycopy(index_bytes, 0, buffer, ExperimentScriptData.INDEX_START, ExperimentScriptData.INDEX_SIZE);
    			write_file.write_file(buffer);
    		}

    		byte[] file_buffer = new byte[StepExperimentScriptData.TOTAL_INSTRUCT_COUNT * ExperimentScriptData.BUFFER_SIZE];
    		for (int i = 0; i < total_step_count; i++) {
    			StepExperimentScriptData temp;
    			ByteBuffer byteBuffer = ByteBuffer.allocate(4);
    			byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

    			temp = (StepExperimentScriptData)experiment_item.get(list.get(i));
    			current_instruct_index = temp.get_step_instruct_to_file_buffer(current_instruct_index, file_buffer);
    			write_file.write_file(file_buffer);
    		}

    		{
    			ExperimentScriptData shaker_off = new ExperimentScriptData();
    			shaker_off.set_instruct_value(ExperimentScriptData.INSTRUCT_SHAKER_OFF);
    			ByteBuffer byteBuffer = ByteBuffer.allocate(4);
    			byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

    			byte[] buffer = shaker_off.get_buffer();
    			byte[] index_bytes = byteBuffer.putInt(current_instruct_index).array();
    			current_instruct_index++;
    			System.arraycopy(index_bytes, 0, buffer, ExperimentScriptData.INDEX_START, ExperimentScriptData.INDEX_SIZE);
    			write_file.write_file(buffer);
    		}

    		{
    			ExperimentScriptData final_instruct = new ExperimentScriptData();
    			final_instruct.set_instruct_value(ExperimentScriptData.INSTRUCT_FINISH);
    			ByteBuffer byteBuffer = ByteBuffer.allocate(4);
    			byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
    			byte[] buffer = final_instruct.get_buffer();
    			byte[] index_bytes = byteBuffer.putInt(current_instruct_index).array();
    			System.arraycopy(index_bytes, 0, buffer, ExperimentScriptData.INDEX_START, ExperimentScriptData.INDEX_SIZE);
    			write_file.write_file(buffer);
    		}

    		write_file.flush_close_file();
    		Toast.makeText(StepScriptActivityList.this, "Save Script Success", Toast.LENGTH_SHORT).show(); 
    	} catch (IOException e) {
    		// TODO Auto-generated catch block
    		e.printStackTrace();
    	}	
    }
	
	public static int load_script_to_step(List<HashMap<String,Object>> load_list, HashMap<Object, Object> load_experiment_item) {
		int ret = 0;
		List<HashMap<String,Object>> temp_list = new ArrayList<HashMap<String,Object>>();
		HashMap<Object, Object> temp_experiment_item = new HashMap<Object, Object>();
		
		if (0 ==load_script(temp_list, temp_experiment_item)) {
			ExperimentScriptData instruct = (ExperimentScriptData)temp_experiment_item.get(temp_list.get(0));
			if (ExperimentScriptData.INSTRUCT_SHAKER_ON == instruct.get_instruct_value()) {
				temp_list.remove(0);
				instruct = (ExperimentScriptData)temp_experiment_item.get(temp_list.get(temp_list.size()-1));
				if (ExperimentScriptData.INSTRUCT_SHAKER_OFF == instruct.get_instruct_value()) {
					temp_list.remove(temp_list.size()-1);
					int total_instruct = temp_list.size();
					if ((total_instruct > 0) && (total_instruct%StepExperimentScriptData.TOTAL_INSTRUCT_COUNT == 0)) {
						int list_pos = 0;
						int step_position = 0;
						load_list.clear();
						load_experiment_item.clear();
					    do { 
					    	StepExperimentScriptData step = new StepExperimentScriptData();
					    	list_pos = step.set_step_instruct_data(list_pos, temp_list, temp_experiment_item);
					    	HashMap<String, Object> item_string_view = new HashMap<String, Object>();
					    	
					    	refresh_step_script_list_view(step_position, step, item_string_view);
							load_list.add(step_position, item_string_view);
							step_position++;
						    if (null == load_experiment_item.put(item_string_view, step))
						        	Log.d(Tag, "load_script step = " + step_position);
					    } while (total_instruct > list_pos);
					} else {
						ret = -3;
						Log.d(Tag, "script file is noy match step instruct count!");
					}
				} else {
					ret = -2;
					Log.d(Tag, "script last instruct is not shaker off!");
				}
			} else {
				ret = -2;
				Log.d(Tag, "script first instruct is not shaker on!");
			}
		} else {
			ret = -1;
			Log.d(Tag, "load script fail!");
		}
		
		return ret;
	}
	
	public static int load_script(List<HashMap<String,Object>> load_list, HashMap<Object, Object> load_experiment_item) {
		int ret = 0;
		
		FileOperateByteArray read_file = new FileOperateByteArray("ExperimentScript", "ExperimentScript", true);
		try {
	        long file_len = 0;
	        
			file_len = read_file.open_read_file(read_file.generate_filename_no_date());
			if (file_len > 0) {
				byte[] read_buf = new byte[(int)file_len];
				read_file.read_file(read_buf);
			
				load_list.clear();
				load_experiment_item.clear();
			    for (int i = 0; i < (file_len-SCRIPT_HEADER_SIZE)/ExperimentScriptData.BUFFER_SIZE; i++) {
			        ExperimentScriptData script = new ExperimentScriptData();
				    byte[] set_data_bytes = new byte[ExperimentScriptData.BUFFER_SIZE];
				    byte[] index_bytes = new byte[ExperimentScriptData.INDEX_SIZE];
	                int offset = (i*ExperimentScriptData.BUFFER_SIZE)+SCRIPT_HEADER_SIZE;
				    int index = 0;
				    int position = 0;
				    HashMap<String, Object> item_string_view = new HashMap<String, Object>();
				 
				    System.arraycopy(read_buf, offset, index_bytes, 0, ExperimentScriptData.INDEX_SIZE);

				    ByteBuffer buffer = ByteBuffer.wrap(index_bytes, 0, ExperimentScriptData.INDEX_SIZE);
				    buffer.order(ByteOrder.LITTLE_ENDIAN);
				    index = buffer.getInt();
				    System.arraycopy(read_buf, offset, set_data_bytes, 0, ExperimentScriptData.BUFFER_SIZE);
				    script.set_buffer(set_data_bytes);
				
				    if (script.get_instruct_value() == ExperimentScriptData.INSTRUCT_FINISH)
				    	break;
				    
				    position = index-1;
				    refresh_script_list_view(position, script, item_string_view);
				    load_list.add(position, item_string_view);
			        if (null == load_experiment_item.put(item_string_view, script))
			        	Log.d(Tag, "button_load_script position = " + index);
			    }
			} else {
			    ret = -2;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			ret = -1;
			e.printStackTrace();
		}	
		
		return ret;
	}
	
	public void add_default_experiment_script() {
		int position = list.size();
		
        HashMap<String, Object> item_string_view = new HashMap<String, Object>();
        StepExperimentScriptData new_item_data = new StepExperimentScriptData();
        	
        refresh_step_script_list_view(position, new_item_data, item_string_view);
        list.add(position, item_string_view);
        experiment_item.put(item_string_view, new_item_data);
        button_save_script.setTextColor(TEXT_REMINDER_SAVE_COLOR);
	}
	
	protected void delete_instruct(int position, HashMap<Object, Object> item_data, List<HashMap<String,Object>> local_list) {
		item_data.remove(local_list.get(position));
		local_list.remove(position);
    	refresh_experiment_script_index(position, item_data, local_list);
    	button_save_script.setTextColor(TEXT_REMINDER_SAVE_COLOR);
	}
	
	protected void add_new_instruct(int position, HashMap<Object, Object> item_data, List<HashMap<String,Object>> local_list) {
		HashMap<String, Object> item_string_view = new HashMap<String, Object>();
        StepExperimentScriptData new_item_data = new StepExperimentScriptData();
        refresh_step_script_list_view(position, new_item_data, item_string_view);
        local_list.add(position, item_string_view);
        button_save_script.setTextColor(TEXT_REMINDER_SAVE_COLOR);
        if (null == item_data.put(item_string_view, new_item_data))
        	Log.d(Tag, "add_new_instruct position = " + position);
	}
	
	protected void refresh_experiment_script_index(int position, HashMap<Object, Object> item_data, List<HashMap<String,Object>> local_list) {
		Log.d(Tag, "refresh_experiment_script_index size = " + item_data.size()); 
        for(int i = position; i < local_list.size(); i++) {
	        StepExperimentScriptData temp;
	        HashMap<String, Object> item_string_view = local_list.get(i);
	
	        temp = (StepExperimentScriptData)item_data.get(item_string_view);
	        item_data.remove(item_string_view);
	        refresh_step_script_list_view(i, temp, item_string_view);
	        Log.d(Tag, "refresh_experiment_script index: " + i); 
	        if (null == item_data.put(item_string_view, temp))
	        	 Log.d(Tag, "refresh_experiment_script_index index = " + i);
        }	
        Log.d(Tag, "refresh_experiment_script_index size = " + item_data.size()); 
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
	    if (v.getId()==R.id.listViewStep) {
	        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
	        menu.setHeaderTitle("Edit");
	        String[] menuItems = getResources().getStringArray(R.array.list_menu);
	        for (int i = 0; i < menuItems.length; i++) {
	            menu.add(Menu.NONE, i, i, menuItems[i]);
	        }
	    }
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
	    AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
	    int menuItemIndex = item.getItemId();
	    String[] menuItems = getResources().getStringArray(R.array.list_menu);
	    String menuItemName = menuItems[menuItemIndex];
	    Log.d(Tag, "onContextItemSelected index = " + menuItemIndex); 
	    int position = (int) adapter.getItemId(info.position); 
	    
	    switch (menuItemIndex) {
	        case INSERT_BEFORE:
	        	add_new_instruct(position, experiment_item, list);
	        	refresh_experiment_script_index(position, experiment_item, list);
        	    adapter.notifyDataSetChanged();
	        break;
	        
	        case DELETE:
	        	delete_instruct(position, experiment_item, list);
                adapter.notifyDataSetChanged();
            break;
            
	        case INSERT_AFTER:
	        	int insert_position = position+1;
	        	add_new_instruct(insert_position,  experiment_item, list);
	        	if((insert_position++) < list.size())
	        	    refresh_experiment_script_index(insert_position, experiment_item, list);
        	    adapter.notifyDataSetChanged();
	        break;
	    }
        
	   // String listItemName = Countries[info.position];

	  //  TextView text = (TextView)findViewById(R.id.footer);
	  //  text.setText(String.format("Selected %s for item %s", menuItemName, listItemName));
        Log.d(Tag, "onContextItemSelected position = " + position);
	    return true;
	}
	
	public void show_script_setting_dialog(long id, int position) {
		    //In the method that is called when click on "update"
	    	Intent intent = new Intent(this, StepScriptSettingActivity.class);
	    	intent.setClass(StepScriptActivityList.this, StepScriptSettingActivity.class); 
	    	intent.putExtra("send_step_experiment_script_data", (StepExperimentScriptData)experiment_item.get(list.get(position))); 
	    	intent.putExtra("send_total_item", list.size()); 
	    	intent.putExtra("send_item_id", id); 
	    	intent.putExtra("send_item_position", position); 
	    	
	    	startActivityForResult(intent, PICK_CONTACT_REQUEST); //I always put 0 for someIntValue
    }
	
	//In your class
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);
	    //Retrieve data in the intent
		if (requestCode == PICK_CONTACT_REQUEST) {
	        // Make sure the request was successful
	        if (resultCode == RESULT_OK) {
	        	long id = data.getLongExtra("return_item_id", -1);
	        	int position = data.getIntExtra("return_item_position", -1);
	        	if (data.getBooleanExtra("data_change_flag", false)) {
	        		button_save_script.setTextColor(TEXT_REMINDER_SAVE_COLOR);
	        	}
	        	
	     	    if (id >= 0 && position >= 0) {
	     	        StepExperimentScriptData item_data = (StepExperimentScriptData)data.getSerializableExtra("return_step_experiment_script_data");  
	     	        experiment_item.remove(list.get(position));
	     	        
	     	      //  HashMap<String, Object> item_string_view = new HashMap<String, Object>();
	     	     //   refresh_script_list_view((int)id, item_data, item_string_view);
	   	          //  list.set(position, item_string_view);
	     	       // experiment_item.put(list.get((int)id), item_data);
	     	        refresh_step_script_list_view(position, item_data, list.get(position));
	     	        experiment_item.put(list.get(position), item_data);
	   	            adapter.notifyDataSetChanged();  
	     	    }
	     	    
	     	    Log.d(Tag, "onActivityResult position = " + position);
	        }
	    }
	}
	
	public static void refresh_step_script_list_view(int index, StepExperimentScriptData item_data, HashMap<String, Object> item_string_view) {
        /* avoid item_string_view object is the same for HashMap, need let item_string_view has a key value always different */
        item_string_view.put(key_experiment, item_data);
        item_string_view.put(key_picture, mPics[0]);

        String str_index = String.format("%d", index+1);
        item_string_view.put(key_index, str_index);
        item_string_view.put(key_step, "Step");
        item_string_view.put(key_high_speed_rpm,"High Speed: " + item_data.get_high_speed_rpm_string() + "rpm");
        item_string_view.put(key_high_speed_duration, "High Speed Duration: " + item_data.get_low_speed_operation_duration_string() + "sec");
        item_string_view.put(key_temperature, "Temperature: " + item_data.get_temperature_string() + "℃");
        item_string_view.put(key_low_speed_rpm, "Low Speed: " + item_data.get_low_speed_rpm() + "rpm");
        item_string_view.put(key_low_speed_duration, "Low Speed Duration: " + item_data.get_low_speed_operation_duration_string() + "sec");
        String duration_hour = "" + item_data.get_experiment_operation_duration()/60;
	    String duration_min = "" + item_data.get_experiment_operation_duration()%60;
        item_string_view.put(key_operation_duration, "Experiment Duration: " + duration_hour + "hour " + duration_min + "min");
	}
	
	public static void refresh_script_list_view(int index, ExperimentScriptData item_data, HashMap<String, Object> item_string_view) {
        /* avoid item_string_view object is the same for HashMap, need let item_string_view has a key value always different */
        item_string_view.put(key_experiment, item_data);
	}
	
	@Override
    public void onResume() {
    	super.onResume();
    	Log.d(Tag, "on Resume");
    }
    
    @Override
    public void onPause() {
    	Log.d(Tag, "on Pause");
    	super.onPause();
    }
    
	@Override
	public void onDestroy() {
		Log.d(Tag, "on Destory");
		super.onDestroy();
	}
}
