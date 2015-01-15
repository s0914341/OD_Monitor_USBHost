package ODMonitor.App;

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

import org.achartengine.chartdemo.demo.chart.IDemoChart;
import org.achartengine.chartdemo.demo.chart.ODChartBuilder;

import ODMonitor.App.R.drawable;
import ODMonitor.App.data.experiment_script_data;
import ODMonitor.App.file.file_operate_byte_array;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
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

public class script_activity_list extends Activity {
	public String Tag = "script_activity_list";
	private final static String key_experiment = "experiment";
	private final static String key_picture = "picture";
	private final static String key_index = "index"; 
	private final static String key_instruction = "instruction"; 
	private final static String key_repeat_from = "repeat_from"; 
	private final static String key_repeat_count = "repeat_count"; 
	private final static String key_repeat_time = "repeat_time";
	private final static String key_shaker_argument = "shaker_argument";
	private final static String key_delay = "delay";
	
	private static final int INSERT_BEFORE=Menu.FIRST-1;  
    private static final int DELETE=Menu.FIRST;  
    private static final int INSERT_AFTER=Menu.FIRST+1;  
	
	private final static int PICK_CONTACT_REQUEST = 0;
	
	private final static int SCRIPT_HEADER_SIZE = 5;
	private final static byte SCRIPT_HEADER = (byte) 0xFF;
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
        R.drawable.sensor_read,R.drawable.on,R.drawable.off, R.drawable.shaker_temperature,R.drawable.shaker_speed,
        R.drawable.repeat_count,R.drawable.repeat_time,R.drawable.delay
    };
	
	/** Called when the activity is first created. */

	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	  //把資料加入ArrayList中
	    setContentView(R.layout.script_layout);
	    list_view = (ListView) findViewById(R.id.listView1);
	    
	    add_default_experiment_script();
	    
	  //新增SimpleAdapter
	    adapter = new SimpleAdapter(this, list, R.layout.script_list,
	                                new String[] {key_picture, key_index, key_instruction, key_repeat_from, key_repeat_count, key_repeat_time, key_shaker_argument, key_delay},
	                                new int[] { R.id.imageView1, R.id.textViewIndex, R.id.textViewInstruction, R.id.textViewFrom, R.id.textViewCount, R.id.textViewTime, R.id.textViewShakerArgument, R.id.textViewDelay } );
	    
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
	    
	    button_add_item = (Button) findViewById(R.id.button_add);
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
	    button_clear_all.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		try {
        			list.clear();
        			experiment_item.clear();
        		    adapter.notifyDataSetChanged();
                } catch (NullPointerException e) {
                    Log.i(Tag, "Tried to clear all exception");
                }
        	}
		});
	    
	    button_save_script = (Button) findViewById(R.id.button_save);
	    button_save_script.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		/* check repeat instruction of script if is over 3 recursive level */
        		if (3 <= check_script_recursive()) {
        			 Toast.makeText(script_activity_list.this, "repeat recursive over 3 level", Toast.LENGTH_SHORT).show(); 
        			 return;
        		}
        		
        		file_operate_byte_array write_file = new file_operate_byte_array("ExperimentScript", "ExperimentScript", true);
        		try {
        			write_file.delete_file(write_file.generate_filename_no_date());
        			write_file.create_file(write_file.generate_filename_no_date());
        			byte[] header = new byte[5];
        			int total_instruction_number = list.size();
        			{
        			    ByteBuffer byteBuffer = ByteBuffer.allocate(4);
        	    	    byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        	    	
        			    byte[] total_instruction_number_bytes = byteBuffer.putInt(total_instruction_number).array();
    	        	    System.arraycopy(total_instruction_number_bytes, 0, header, 1, 4);
        			    header[0] = SCRIPT_HEADER;
        			}

        			write_file.write_file(header);
        			for (int i = 0; i < total_instruction_number; i++) {
        				 experiment_script_data temp;
        				 ByteBuffer byteBuffer = ByteBuffer.allocate(4);
         	    	     byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
         	    	
        			     temp = (experiment_script_data)experiment_item.get(list.get(i));
        			     byte[] buffer = temp.get_buffer();
        			     byte[] index_bytes = byteBuffer.putInt(i+1).array();
        	        	 System.arraycopy(index_bytes, 0, buffer, experiment_script_data.INDEX_START, experiment_script_data.INDEX_SIZE);
        			     write_file.write_file(buffer);
        			}
        			experiment_script_data final_instruct = new experiment_script_data();
        			final_instruct.set_instruct_value(experiment_script_data.INSTRUCT_FINISH);
        			{
        				 ByteBuffer byteBuffer = ByteBuffer.allocate(4);
        	    	     byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        			     byte[] buffer = final_instruct.get_buffer();
   			             byte[] index_bytes = byteBuffer.putInt(total_instruction_number+1).array();
   	        	         System.arraycopy(index_bytes, 0, buffer, experiment_script_data.INDEX_START, experiment_script_data.INDEX_SIZE);
   	        	         write_file.write_file(buffer);
        			}
        	
		            write_file.flush_close_file();
		            Toast.makeText(script_activity_list.this, "Save Script Success", Toast.LENGTH_SHORT).show(); 
        		} catch (IOException e) {
        			// TODO Auto-generated catch block
        			e.printStackTrace();
        		}	
        	}
		});
	    
	    button_load_script = (Button) findViewById(R.id.button_load);
	    button_load_script.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		file_operate_byte_array read_file = new file_operate_byte_array("ExperimentScript", "ExperimentScript", true);
        		try {
        	        long file_len = 0;
        	        
        			file_len = read_file.open_read_file(read_file.generate_filename_no_date());
        			if (file_len > 0) {
        				byte[] read_buf = new byte[(int)file_len];
        				read_file.read_file(read_buf);
        			
        				list.clear();
            			experiment_item.clear();
        			    for (int i = 0; i < (file_len-SCRIPT_HEADER_SIZE)/experiment_script_data.BUFFER_SIZE; i++) {
        			        experiment_script_data script = new experiment_script_data();
        				    byte[] set_data_bytes = new byte[experiment_script_data.BUFFER_SIZE];
        				    byte[] index_bytes = new byte[experiment_script_data.INDEX_SIZE];
        	                int offset = (i*experiment_script_data.BUFFER_SIZE)+SCRIPT_HEADER_SIZE;
        				    int index = 0;
        				    int position = 0;
        				    HashMap<String, Object> item_string_view = new HashMap<String, Object>();
        				 
        				    System.arraycopy(read_buf, offset, index_bytes, 0, experiment_script_data.INDEX_SIZE);

        				    ByteBuffer buffer = ByteBuffer.wrap(index_bytes, 0, experiment_script_data.INDEX_SIZE);
        				    buffer.order(ByteOrder.LITTLE_ENDIAN);
        				    index = buffer.getInt();
        				    System.arraycopy(read_buf, offset, set_data_bytes, 0, experiment_script_data.BUFFER_SIZE);
        				    script.set_buffer(set_data_bytes);
        				
        				    if (script.get_instruct_value() == experiment_script_data.INSTRUCT_FINISH)
        				    	break;
        				    
        				    position = index-1;
        				    refresh_script_list_view(position, script, item_string_view);
        				    list.add(position, item_string_view);
        			        if (null == experiment_item.put(item_string_view, script))
        			        	Log.d(Tag, "button_load_script position = " + index);
        			    }
        			    adapter.notifyDataSetChanged();
		                Toast.makeText(script_activity_list.this, "Load Script Success", Toast.LENGTH_SHORT).show(); 
        			}
        		} catch (IOException e) {
        			// TODO Auto-generated catch block
        			e.printStackTrace();
        		}	
        	}
		});
    }
	
	public int check_script_recursive() {
		int start_instruction_number = list.size();
		int recursive_level = 0;
		List<int[]> list_repeat = new ArrayList<int[]>();
		
		while((start_instruction_number--) > 0) {
            experiment_script_data temp;
	        temp = (experiment_script_data)experiment_item.get(list.get(start_instruction_number));
		    if ((temp.get_instruct_value() == experiment_script_data.INSTRUCT_REPEAT_COUNT) || 
		        (temp.get_instruct_value() == experiment_script_data.INSTRUCT_REPEAT_TIME)) {
		    	int[] repeat= new int[2];
		    	repeat[0] = start_instruction_number;
		    	repeat[1] = temp.get_repeat_from_value();
		    	list_repeat.add(repeat);
		    }	
		}
		
		for (int i = 0; i < list_repeat.size(); i++) {
			int[] recursive_range = list_repeat.get(i); 
			recursive_level = 1;
			for (int j = i+1; j < list_repeat.size(); j++) {
				int[] temp_recursive = list_repeat.get(j);
				
				if ((temp_recursive[0] < recursive_range[0]) && (temp_recursive[0] > recursive_range[1])) {
					if (temp_recursive[1] > recursive_range[1])
						recursive_range[1] = temp_recursive[1];
					
					recursive_level++;
				}
				
				if ((recursive_level) > 3)
					return recursive_level;
			}
			
		}
		
		return recursive_level;
	}
	
	public void add_default_experiment_script() {
		int position = 0;
		
        for (int i = 1; i <= 9; i++) {
        	HashMap<String, Object> item_string_view = new HashMap<String, Object>();
            experiment_script_data new_item_data = new experiment_script_data();
            position = list.size();
            
        	switch (i) {
        	    case 1:
                    new_item_data.set_instruct_value(experiment_script_data.INSTRUCT_SHAKER_ON);
                break;
                
        	    case 2:
                    new_item_data.set_instruct_value(experiment_script_data.INSTRUCT_SHAKER_SET_SPEED);
                    new_item_data.set_shaker_speed_value(250);
                break;
                
        	    case 3:
                    new_item_data.set_instruct_value(experiment_script_data.INSTRUCT_SHAKER_SET_TEMPERATURE);
                    new_item_data.set_shaker_temperature_value(25);
                break;
                
        	    case 4:
                    new_item_data.set_instruct_value(experiment_script_data.INSTRUCT_DELAY);
                    new_item_data.set_delay_value(150);
                break;
                
        	    case 5:
                    new_item_data.set_instruct_value(experiment_script_data.INSTRUCT_SHAKER_SET_SPEED);
                    new_item_data.set_shaker_speed_value(100);
                break;
                
        	    case 6:
                    new_item_data.set_instruct_value(experiment_script_data.INSTRUCT_DELAY);
                    new_item_data.set_delay_value(30);
                break;
                
        	    case 7:
                    new_item_data.set_instruct_value(experiment_script_data.INSTRUCT_READ_SENSOR);
                break;
                
        	    case 8:
                    new_item_data.set_instruct_value(experiment_script_data.INSTRUCT_REPEAT_COUNT);
                    new_item_data.set_repeat_from_value(2);
                    new_item_data.set_repeat_count_value(10);   
                break;
                
        	    case 9:
                    new_item_data.set_instruct_value(experiment_script_data.INSTRUCT_SHAKER_OFF);
                break;
        	}
        	
            refresh_script_list_view(position, new_item_data, item_string_view);
            list.add(position, item_string_view);
            experiment_item.put(item_string_view, new_item_data);
        }
	}
	
	protected void delete_instruct(int position, HashMap<Object, Object> item_data, List<HashMap<String,Object>> local_list) {
		item_data.remove(local_list.get(position));
		local_list.remove(position);
    	refresh_experiment_script_index(position, item_data, local_list);
	}
	
	protected void add_new_instruct(int position, HashMap<Object, Object> item_data, List<HashMap<String,Object>> local_list) {
		HashMap<String, Object> item_string_view = new HashMap<String, Object>();
        experiment_script_data new_item_data = new experiment_script_data();
        refresh_script_list_view(position, new_item_data, item_string_view);
        local_list.add(position, item_string_view);
        if (null == item_data.put(item_string_view, new_item_data))
        	Log.d(Tag, "add_new_instruct position = " + position);
	}
	
	protected void refresh_experiment_script_index(int position, HashMap<Object, Object> item_data, List<HashMap<String,Object>> local_list) {
		Log.d(Tag, "refresh_experiment_script_index size = " + item_data.size()); 
        for(int i = position; i < local_list.size(); i++) {
	        experiment_script_data temp;
	        HashMap<String, Object> item_string_view = local_list.get(i);
	
	        temp = (experiment_script_data)item_data.get(item_string_view);
	        item_data.remove(item_string_view);
	        refresh_script_list_view(i, temp, item_string_view);
	        Log.d(Tag, "refresh_experiment_script index: " + i + "string:"+item_string_view.get(key_instruction)); 
	        if (null == item_data.put(item_string_view, temp))
	        	 Log.d(Tag, "refresh_experiment_script_index index = " + i);
        }	
        Log.d(Tag, "refresh_experiment_script_index size = " + item_data.size()); 
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
	    if (v.getId()==R.id.listView1) {
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
	    	Intent intent = new Intent(this, script_setting_activity.class);
	    	intent.setClass(script_activity_list.this, script_setting_activity.class); 
	    	intent.putExtra("send_experiment_script_data", (experiment_script_data)experiment_item.get(list.get(position))); 
	    	intent.putExtra("send_total_item", list.size()); 
	    	intent.putExtra("send_item_id", id); 
	    	intent.putExtra("send_item_position", position); 
	    	
	    	//startActivity(intent);
	    	
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
	     	    if (id >= 0 && position >= 0) {
	     	        experiment_script_data item_data = (experiment_script_data)data.getSerializableExtra("return_experiment_script_data");  
	     	        experiment_item.remove(list.get(position));
	     	        
	     	      //  HashMap<String, Object> item_string_view = new HashMap<String, Object>();
	     	     //   refresh_script_list_view((int)id, item_data, item_string_view);
	   	          //  list.set(position, item_string_view);
	     	       // experiment_item.put(list.get((int)id), item_data);
	     	        refresh_script_list_view(position, item_data, list.get(position));
	     	        experiment_item.put(list.get(position), item_data);
	   	            adapter.notifyDataSetChanged();  
	     	    }
	     	    
	     	    Log.d(Tag, "onActivityResult position = " + position);
	        }
	    }
	}
	
	public void refresh_script_list_view(int index,  experiment_script_data item_data, HashMap<String, Object> item_string_view) {
		 String str_index;
        str_index = String.format("%d", index+1);
        int instruct = item_data.get_instruct_value();
        
        Log.d(Tag, "instruct:"+ item_data.get_instruct_string());
        /* avoid item_string_view object is the same for HashMap, need let item_string_view has a key value always different */
        item_string_view.put(key_experiment, item_data);
        item_string_view.put(key_picture, mPics[instruct]);
        Log.d(Tag, "instruct:"+ instruct);
        item_string_view.put(key_index, str_index);
        item_string_view.put(key_instruction, experiment_script_data.SCRIPT_INSTRUCT.get(instruct));
        switch(instruct) {
            case  experiment_script_data.INSTRUCT_READ_SENSOR:
            case  experiment_script_data.INSTRUCT_SHAKER_ON:
            case  experiment_script_data.INSTRUCT_SHAKER_OFF:
	                item_string_view.put(key_repeat_from,"");
	                item_string_view.put(key_repeat_count, "");
	                item_string_view.put(key_repeat_time, "");
	   	            item_string_view.put(key_shaker_argument, "");
	   	            item_string_view.put(key_delay, "");
            break;
            
            case  experiment_script_data.INSTRUCT_REPEAT_COUNT:
	                item_string_view.put(key_repeat_from, "From："+item_data.get_repeat_from_string()+"   ");
	                item_string_view.put(key_repeat_count, "Count："+item_data.get_repeat_count_string());
	                item_string_view.put(key_repeat_time, "");
	   	            item_string_view.put(key_shaker_argument, "");
	   	            item_string_view.put(key_delay, "");
            break;
            
            case  experiment_script_data.INSTRUCT_REPEAT_TIME:
	                item_string_view.put(key_repeat_from, "From："+item_data.get_repeat_from_string()+"   ");
	                item_string_view.put(key_repeat_count, "");
	                item_string_view.put(key_repeat_time, "Time："+item_data.get_repeat_time_string()+"min");
	   	            item_string_view.put(key_shaker_argument, "");
	   	            item_string_view.put(key_delay, "");
            break;
            
            case  experiment_script_data.INSTRUCT_SHAKER_SET_SPEED:
	                item_string_view.put(key_repeat_from, "");
	                item_string_view.put(key_repeat_count, "");
	                item_string_view.put(key_repeat_time, "");
	   	            item_string_view.put(key_shaker_argument, "Shaker speed：" + item_data.get_shaker_speed_string()+"rpm");
	   	            item_string_view.put(key_delay, "");
            break;
            
            case  experiment_script_data.INSTRUCT_SHAKER_SET_TEMPERATURE:
	                item_string_view.put(key_repeat_from, "");
	                item_string_view.put(key_repeat_count, "");
	                item_string_view.put(key_repeat_time, "");
	   	            item_string_view.put(key_shaker_argument, "shaker temperature：" + item_data.get_shaker_temperature_string() + "℃");
	   	            item_string_view.put(key_delay, "");
            break;
            
            case  experiment_script_data.INSTRUCT_DELAY:
                    item_string_view.put(key_repeat_from, "");
                    item_string_view.put(key_repeat_count, "");
                    item_string_view.put(key_repeat_time, "");
   	                item_string_view.put(key_shaker_argument, "");
   	                item_string_view.put(key_delay, "Time:" + item_data.get_delay_string()+"second");
        break;
        }
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
