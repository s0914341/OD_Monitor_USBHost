package od_monitor.app;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import od_monitor.app.data.android_accessory_packet;
import od_monitor.app.data.chart_display_data;
import od_monitor.app.data.experiment_script_data;
import od_monitor.app.data.machine_information;
import od_monitor.app.data.sensor_data_composition;
import od_monitor.app.data.sync_data;
import od_monitor.app.file.file_operate_byte_array;
import od_monitor.app.file.file_operation;
import od_monitor.experiment.ExperimentalOperationInstruct;
import od_monitor.experiment.ODMonitor_Sensor.CMD_T;
import od_monitor.script.script_activity_list;
import od_monitor.script.step_script_activity_list;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.chartdemo.demo.chart.AverageTemperatureChart;
import org.achartengine.chartdemo.demo.chart.IDemoChart;
import org.achartengine.chartdemo.demo.chart.ODChartBuilder;
import org.achartengine.model.SeriesSelection;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import com.ftdi.j2xx.D2xxManager;

import ODMonitor.App.R;
import ODMonitor.App.R.drawable;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.os.SystemClock;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.Chronometer.OnChronometerTickListener;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.os.CountDownTimer;

public class ODMonitorActivity extends Activity {
	public static String Tag = "ODMonitorActivity";
	private static final String ACTION_USB_PERMISSION = "OD.MONITOR.USB_PERMISSION";
	public UsbManager usbmanager, mUsbManager;
	public UsbAccessory usbaccessory;
	public PendingIntent mPermissionIntent;
	public ParcelFileDescriptor filedescriptor = null;
	public FileInputStream inputstream = null;
	public FileOutputStream outputstream = null;
	public boolean mPermissionRequestPending = true;
	
	public static final int UI_GET_MACHINE_INFO_REFRESH = 0;
	public static final int UI_SEND_SCRIPT = 1;
	public static final int UI_SHOW_INITIAL_DIALOG = 2;
	public static final int UI_CANCLE_INITIAL_DIALOG = 3;
	
	public static final int EXPERIMENT_START = 0;
	public static final int EXPERIMENT_RUNNING = 1;
	public static final int EXPERIMENT_STOP = 2;
	public static final int EXPERIMENT_NOTIFY_CHART = 3;
	public static final int EXPERIMENT_OPEN_SCRIPT_ERROR = 4;
	
	public static final long WAIT_TIMEOUT = 3000;
	public static final long WAIT_TIMEOUT_GET_EXPERIMENT = 10000;
	public byte  ledPrevMap = 0x00;
	//public byte[] usbdataIN;
	public android_accessory_packet acc_pkg_transfer = new android_accessory_packet(android_accessory_packet.INIT_PREFIX_VALUE);
	public android_accessory_packet acc_pkg_receive = new android_accessory_packet(android_accessory_packet.NO_INIT_PREFIX_VALUE);
	
	public SeekBar volumecontrol;
    public ProgressBar slider;
    
    public ImageButton start_button; //Button led1;
    public ImageButton stop_button; //Button led3;
    public ImageButton chart_button; //Button led4;
    public ImageButton script_button;
    public ImageButton button6;
    
    public ImageView ledvolume;
    
    public ImageView sensor_status;
    public ImageView shaker_status;
    
    //public EditText etInput; //shaker command input
   // public TextView shaker_return;
   // public TextView debug_view;
   // public TextView sensor_data_view;
    public int get_experiment_data_start = 0;
    public long script_length = 0;
    public int script_offset = 0;
    public byte[] script = null;
    final Context context = this;
    
    /*thread to listen USB data*/
    public sync_data sync_object;
    public sync_data sync_send_script;
    public sync_data sync_start_experiment;
    
    public ProgressDialog mypDialog;
    public sync_data sync_get_experiment;
    public sync_data sync_chart_notify;
    private boolean experiment_thread_run = false;
    private boolean experiment_stop = false;
    private SwipeRefreshLayout laySwipe;
    public Thread experiment_time_thread = null;
    public boolean experiment_time_run = false;
    
    /**
     * FTDI D2xx USB to UART
     */
    public static D2xxManager ftD2xx = null;
    public ExperimentalOperationInstruct experiment;
    boolean mRequest_USB_permission;
    
    GVTable table;
	SQLiteDatabase db;
	int id;
	private static final String TABLE_NAME = "stu";
	private static final String INDEX = "NO";
	private static final String DATE = "date";
	private static final String OD1 = "OD1";
	private static final String OD2 = "OD2";
	private static final String OD3 = "OD3";
	private static final String OD4 = "OD4";
    
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {     
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.od_monitor);
        Thread.currentThread().setName("Thread_ODMonitorActivity");
        
        IntentFilter filter = new IntentFilter();
        //filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        filter.addAction(ACTION_USB_PERMISSION);
        filter.setPriority(500);
        Log.d(Tag, "filter" +filter);
        this.registerReceiver(mUsbReceiver, filter);   
		
		//shaker_return = (TextView)findViewById(R.id.ShakerReturn);
		//shaker_return.setMovementMethod(new ScrollingMovementMethod());
		//debug_view = (TextView)findViewById(R.id.ExperimentInformation);
		//debug_view.setMovementMethod(new ScrollingMovementMethod());
		//sensor_data_view = (TextView)findViewById(R.id.SensorData);
		//sensor_data_view.setMovementMethod(new ScrollingMovementMethod());
		sensor_status = (ImageView)findViewById(R.id.SensorStatus);
		sensor_status.setEnabled(false);
		shaker_status = (ImageView)findViewById(R.id.ShakerStatus);
		shaker_status.setEnabled(false);
		
		table = new GVTable(this);
		table.gvSetTableRowCount(50);
		LinearLayout ly = (LinearLayout) findViewById(R.id.GridLayout);
		table.setTableOnClickListener(new GVTable.OnTableClickListener() {
			public void onTableClickListener(int x, int y, Cursor c) {
				c.moveToPosition(y);
				//String str=c.getString(x)+" Position:("+String.valueOf(x)+","+String.valueOf(y)+")";
				String str = c.getString(x);
				Toast.makeText(ODMonitorActivity.this, str, Toast.LENGTH_SHORT).show();
			}
		});
		
		table.setOnPageSwitchListener(new GVTable.OnPageSwitchListener() {
			public void onPageSwitchListener(int pageID,int pageCount) {
				/*String str="Total:"+String.valueOf(pageCount)+
				" Page:"+String.valueOf(pageID);*/
				String str = "Page:"+String.valueOf(pageID);
				Toast.makeText(ODMonitorActivity.this, str, Toast.LENGTH_SHORT).show();
			}
		});
		
		ly.addView(table);
		
		//data_write_thread = new data_write_thread(handler);
		//data_write_thread.start();
	//	textView2 = (TextView) findViewById(R.id.test);
	//	textView2.setText( Html.fromHtml("<a href=\"http://www.maestrogen.com/ftp/i-track/user_manual.html\">iTrack User Manual</a>") );
	//	textView2.setMovementMethod(LinkMovementMethod.getInstance());
		
		mypDialog = new ProgressDialog(this);
		mypDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		mypDialog.setTitle("OD Monitor");
		mypDialog.setMessage("Get device information!");
		//mypDialog.setIcon(R.drawable.android);
		//mypDialog.setButton("Google",this);
		mypDialog.setIndeterminate(true);
		mypDialog.setCancelable(false);
		sync_object = new sync_data();
		sync_chart_notify = new sync_data();
		sync_send_script = new sync_data();
		sync_start_experiment = new sync_data();
		ODMonitor_Application app_data = ((ODMonitor_Application)this.getApplication());
		app_data.set_sync_chart_notify(sync_chart_notify);
		
		try {
    		ftD2xx = D2xxManager.getInstance(this);
    	} catch (D2xxManager.D2xxException ex) {
    		ex.printStackTrace();
    	}
  
        SetupD2xxLibrary();
		//experiment = new ExperimentalOperationInstruct(context, ftD2xx, shaker_return);
        experiment = new ExperimentalOperationInstruct(context, ftD2xx, null);
               
		start_button = (ImageButton) findViewById(R.id.ButtonStart);
		start_button.setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {
		    	boolean sensor_connected = sensor_status.isEnabled();
		    	boolean shaker_connected = shaker_status.isEnabled();
		    	
		    	if ((false == sensor_connected) || (false == shaker_connected)) {
		    		Toast.makeText(ODMonitorActivity.this, "devices is not ready!",Toast.LENGTH_SHORT).show();
		    	} else {
		    	    if (false == experiment_thread_run) {
		    	        if (0 == experiment.initial_experiment_devices()) {
		    	        	if (null != db) {
		    	        	    if (true == db.isOpen())
		    	        		    db.close();
		    	        	}
		    	        	
		    	        	table.gvRemoveAll();
		    	            experiment_stop = false;
		    	            experiment_thread_run = true;
		    	            new Thread(new experiment_thread(handler)).start(); 
		    	        } else {
		    	    	    Toast.makeText(ODMonitorActivity.this, "initial experiment devices fail!",Toast.LENGTH_SHORT).show();
		    	        }
		    	    }
		    	}
		    }
		});
        
		stop_button = (ImageButton) findViewById(R.id.ButtonStop);
		stop_button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (true == experiment_thread_run) {
				    experiment_stop = true;
				    Toast.makeText(ODMonitorActivity.this, "Stopping experiment!",Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(ODMonitorActivity.this, "No experiment running!",Toast.LENGTH_SHORT).show();
				}
			}
		});
        
        chart_button = (ImageButton) findViewById(R.id.ButtonChart);
        chart_button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
        		show_chart_activity();
			}
		});  
        
        script_button = (ImageButton) findViewById(R.id.ButtonScript);
        script_button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
        		show_script_activity();
			}
		});  
        
      /*  button6 = (ImageButton) findViewById(R.id.Button6);
        button6.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				//set_tablet_on_off_line((byte)0, false);
				//SensorDataReceive();
				experiment_script_data current_instruct_data = new experiment_script_data();
				experiment.shaker_on_instruct(current_instruct_data);
			}
		});*/
        
       /* etInput = (EditText)findViewById(R.id.etInput); 
        etInput.setOnKeyListener(new OnKeyListener() {
        public boolean onKey(View v, int keyCode, KeyEvent event) { 
            if(event.getAction() == KeyEvent.ACTION_DOWN && 
               keyCode == KeyEvent.KEYCODE_ENTER){ 
               Toast.makeText(ODMonitorActivity.this, etInput.getText(), Toast.LENGTH_SHORT).show(); 
               String cmd = etInput.getText().toString(); 
               char[] readDataChar = new char[cmd.length()+10];
               int receive_length = 0;
               
               if (0 < shaker.createDeviceList()) {
			       if (0 == shaker.connectFunction(0)) {
			    	   shaker.SetConfig(shaker.baudRate, shaker.dataBit, shaker.stopBit, shaker.parity, shaker.flowControl);
			    	   if (0 < (receive_length = shaker.SendMessage(cmd, readDataChar))) {
			    		   String read_string = String.copyValueOf(readDataChar, 0, receive_length);
			    	       Toast.makeText(ODMonitorActivity.this, "return:"+read_string,Toast.LENGTH_SHORT).show();
			    	   } else {
			    		   Toast.makeText(ODMonitorActivity.this, "send message error!",Toast.LENGTH_SHORT).show();
			    	   }
			    	   shaker.disconnectFunction();
			       } else {
			           Log.d(Tag, "shaker connect NG");
			       } 		
			   } else {
			       Log.d(Tag, "no device on list");
			   } 
 
               return true; 
            } 
           return false; 
         } 
         });*/
        
        FileToODDataDB();
        Log.d ( Tag, "intent get action: " +this.getIntent().getAction());
        Log.d(Tag, "on Create");
        mRequest_USB_permission = false;
        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
		mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        EnumerationDevice(getIntent());
    }
    
	void CreateODDataDB() {
		db = SQLiteDatabase.create(null);
		Log.d("DB Path", db.getPath());
		String amount = String.valueOf(databaseList().length);
		Log.d("DB amount", amount);

		String sql = "CREATE TABLE " + TABLE_NAME + " (" + 
		        INDEX	+ " text not null, " + DATE + " text not null," + OD1 + " text not null," +
		        OD2	+ " text not null, " + OD3 + " text not null," +
		        OD4	+ " text not null "+");";
		try {
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
			db.execSQL(sql);
		} catch (SQLException e) {}
	}
	
	void FileToODDataDB() {
		Date date = null;
        double od_value = 0;
        long size = 0;
        
        file_operate_byte_array read_file = new file_operate_byte_array(sensor_data_composition.sensor_raw_folder_name, sensor_data_composition.sensor_raw_file_name, true);
        try {
        	size = read_file.open_read_file(read_file.generate_filename_no_date());
        } catch (IOException e) {
	        // TODO Auto-generated catch block
        	Log.d(Tag, "file open fail!");
	        e.printStackTrace();
	        return;
        }
        
        if (size >= sensor_data_composition.total_size) {
			int offset = 0;
		    byte[] data = new byte[(int) size];
		    read_file.read_file(data);
		    sensor_data_composition one_sensor_data = new sensor_data_composition();
		    CreateODDataDB();
			
			while ((size-offset) >= sensor_data_composition.total_size) {
				one_sensor_data.set_buffer(data, offset, sensor_data_composition.total_size);
				date = new Date(one_sensor_data.get_sensor_measurement_time());
				od_value = one_sensor_data.get_sensor_od_value();    
				offset += sensor_data_composition.total_size;
				InsertODDateToDB(one_sensor_data);
			}
			
			table.gvUpdatePageBar("select count(*) from " + TABLE_NAME,db);
		    table.gvReadyTable("select * from " + TABLE_NAME,db);
			table.refresh_last_table();
		} else {
			Log.d(Tag, "file is nothing to show!");
		}
	}

	void InsertODDateToDB(sensor_data_composition sensor_data) {
		String sql = "insert into " + TABLE_NAME + " (" + 
			INDEX + ", " + DATE + ", " + OD1 + ", " + OD2 + ", " + OD3 + ", " + OD4
					+ ") values('" + sensor_data.get_sensor_get_index_string()
					+ "', '" + sensor_data.get_sensor_measurement_time_string() + "','"
					+ sensor_data.get_sensor_od_value_string() + "','NA','NA','NA');";
		try {
			db.execSQL(sql);
		} catch (SQLException e) {
		}
	}
    
    public void EnumerationDeviceShaker() {
    	if (experiment.shaker.Enumeration()) {
    		shaker_status.setEnabled(true);
        	Log.d(Tag, "*****Shaker EnumerationDevice permission pass!*****");
		} else {
			if (experiment.shaker.isDeviceOnline()) {
				Log.d(Tag, "*****Shaker EnumerationDevice request permission!*****");
				mUsbManager.requestPermission(experiment.shaker.getDevice(), mPermissionIntent);
			} else {
				Log.d(Tag, "*****Shaker EnumerationDevice is not online!*****");
			}
		}
    }
    
    public void EnumerationDevice(Intent intent) {
    	Log.d(Tag, "*****EnumerationDevice start!*****" + intent.getAction());
    	if (intent.getAction().equals(Intent.ACTION_MAIN) || intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {
    		boolean shaker_can_request_permission = false;
    		if (experiment.mODMonitorSensor.Enumeration()) {
    			shaker_can_request_permission = true;
    			sensor_status.setEnabled(true);
    			Log.d(Tag, "*****Sensor EnumerationDevice permission pass!*****");
    		} else {
    			if (experiment.mODMonitorSensor.isDeviceOnline()) {
    				Log.d(Tag, "*****Sensor EnumerationDevice request permission!*****");
    				mRequest_USB_permission = true;
					mUsbManager.requestPermission(experiment.mODMonitorSensor.getDevice(), mPermissionIntent);
    			} else {
    				shaker_can_request_permission = true;
    				Log.d(Tag, "*****Sensor EnumerationDevice is not online!*****");
    			}
    		}
    		
    		if (true == shaker_can_request_permission)
    			EnumerationDeviceShaker();
    	}
    }
    
    @Override
    protected void onNewIntent(Intent intent) {

        super.onNewIntent(intent);

    	setIntent(intent);//must store the new intent unless getIntent() will return the old one
    	if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_ATTACHED))
    		EnumerationDevice(intent);

    	  //processExtraData();
    	
    /*	usbmanager = (UsbManager) getSystemService(Context.USB_SERVICE);
        Log.d(Tag, "usbmanager" +usbmanager);
        mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
        //filter.addAction(UsbManager.ACTION_USB_ACCESSORY_ATTACHED);
        Log.d(Tag, "filter" +filter);
        registerReceiver(mUsbReceiver, filter);*/
    }
    
    private void SetupD2xxLibrary () {
    	/*
        PackageManager pm = getPackageManager();

        for (ApplicationInfo app : pm.getInstalledApplications(0)) {
          Log.d("PackageList", "package: " + app.packageName + ", sourceDir: " + app.nativeLibraryDir);
          if (app.packageName.equals(R.string.app_name)) {
        	  System.load(app.nativeLibraryDir + "/libj2xx-utils.so");
        	  Log.i("ftd2xx-java","Get PATH of FTDI JIN Library");
        	  break;
          }
        }
        */
    	// Specify a non-default VID and PID combination to match if required

    	if(!ftD2xx.setVIDPID(0x0403, 0xada1))
    		Log.i("ftd2xx-java","setVIDPID Error");

    }
    
    public void show_chart_activity() {
    	Intent intent = null;
    	intent = new Intent(this, ODChartBuilder.class);
    	startActivity(intent);
    }
    
    public void show_script_activity() {
        	Intent intent = null;
        	intent = new Intent(this, step_script_activity_list.class);
        	startActivity(intent);
    }
    
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {   //確定按下退出鍵
            ConfirmExit();
            return true;  
        }  
        
        return super.onKeyDown(keyCode, event);  
    }

    public void ConfirmExit(){
        AlertDialog.Builder ad=new AlertDialog.Builder(ODMonitorActivity.this); //創建訊息方塊
        ad.setTitle("EXIT");
        ad.setMessage("Are you sure want to exit?");
        ad.setPositiveButton("Yes", new DialogInterface.OnClickListener() { //按"是",則退出應用程式
            public void onClick(DialogInterface dialog, int i) {
            	unregisterReceiver(mUsbReceiver);
            	System.exit(0);
            	//finish();
            	//LEDActivity.this.finish();//關閉activity
            }
        });
        
        ad.setNegativeButton("No",new DialogInterface.OnClickListener() { //按"否",則不執行任何操作
            public void onClick(DialogInterface dialog, int i) {

            }
        });

        ad.show();//顯示訊息視窗
    }
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.setting_menu, menu);
		return true;
	}
        
    @Override
    public void onRestart() {
        super.onRestart();
    	Log.d(Tag, "on Resume");
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
    	//aoa_thread_run = false;
    }
    
    @Override
    public void onStop() {
    	Log.d(Tag, "on Stop");
    	super.onStop();
    }
    
	@Override
	public void onDestroy() {
		this.unregisterReceiver(mUsbReceiver);
		Log.d(Tag, "on Destory");
		super.onDestroy();
	}
	
	final Handler UIhandler = new Handler() {
		public void handleMessage(Message msg) {	
			switch (msg.arg1) {
			    case UI_GET_MACHINE_INFO_REFRESH: {
			    	final String str_status = msg.getData().getString("get_machine_info_status", "no status");
			    	
			    	this.postDelayed(new Runnable() {
	                    public void run() {
	                        laySwipe.setRefreshing(false);
	                        Toast.makeText(getApplicationContext(), str_status, Toast.LENGTH_SHORT).show();
	                    }
	                }, 1000);
			    
			    } break;
			    
			    case UI_SEND_SCRIPT: {
			    	final String str_status = msg.getData().getString("send_script_status", "no status");
			    	Toast.makeText(getApplicationContext(), str_status, Toast.LENGTH_SHORT).show();
			    } break;
			    
			    case UI_SHOW_INITIAL_DIALOG:
			    	mypDialog.show();
			    break;
			    
			    case UI_CANCLE_INITIAL_DIALOG:
			    	mypDialog.cancel();
			    break;
			}
		}
	};
	
	public void notify_chart_receive_data() {
		if (sync_chart_notify != null) {
	        synchronized (sync_chart_notify) {
	        	sync_chart_notify.set_status(sync_data.STATUS_DATA_AVAILABLE);
	    	    sync_chart_notify.notify();
	        }
	    }
	}
		
	final Handler handler =  new Handler() {
    	public void handleMessage(Message msg) {	
		    Bundle b = msg.getData();
		    
		    int experiment_status = b.getInt("experiment status");
		    switch (experiment_status) {
		        case EXPERIMENT_START:
		        	Runnable myRunnableThread = new CountDownRunner();
		        	experiment_time_thread= new Thread(myRunnableThread); 
		        	experiment_time_run = true;
		        	experiment_time_thread.start();
		        	CreateODDataDB();
		        break;
		        
		        case EXPERIMENT_RUNNING:
		        	int current_instruct_index = b.getInt("current instruct index");
		    		int current_instruct_value = b.getInt("current instruct value");
		    		long current_experiment_time = b.getLong("current experiment time");
		    		
		    		String experiment_process = "index: " + current_instruct_index + ",  " + "time: " + current_experiment_time + ",  " + "instruct: " + experiment_script_data.SCRIPT_INSTRUCT.get(current_instruct_value) + "\n";
		    		//String experiment_process = String.format("index:%d,   time:%d,   instruct:%s \n", current_instruct_index, current_experiment_time, experiment_script_data.SCRIPT_INSTRUCT.get(current_instruct_value));
		    		//debug_view.setText(experiment_process);
		        break;
		        
		        case EXPERIMENT_STOP:
		        	experiment_time_run = false;
		        	//experiment_timer.stop();
		        	experiment.close_shaker_port();
		        break;
		        
		        case EXPERIMENT_NOTIFY_CHART:
		        	notify_chart_receive_data();
		        	sensor_data_composition sensor_data = (sensor_data_composition)b.getSerializable("sensor_data_composition");
		        	if (null != sensor_data) {
		        		InsertODDateToDB(sensor_data);
		        		table.gvUpdatePageBar("select count(*) from " + TABLE_NAME,db);
						table.gvReadyTable("select * from " + TABLE_NAME,db);
						table.refresh_last_table();
		        	}
		        //	sensor_data_view.setText(b.getString("experiment sensor data", "no sensor data"));
		        break;
		        
		        case EXPERIMENT_OPEN_SCRIPT_ERROR:
		        	Toast.makeText(getApplicationContext(), "Script open error!", Toast.LENGTH_SHORT).show();
		        break;
		    }
    	}
    };
	
    class experiment_thread implements Runnable {
		Handler mHandler;
		
		experiment_thread(Handler h) {
			mHandler = h;
		}
		
		public void run() {
			List<HashMap<String,Object>> list = new ArrayList<HashMap<String,Object>>();
			HashMap<Object, Object> experiment_item = new HashMap<Object, Object>();
			int next_instruct_index = 0;
			experiment_script_data current_instruct_data;
			int ret = 0;
			Bundle b = new Bundle(1);
			Message msg;
			
			Thread.currentThread().setName("Thread_Experiment");
			if (0 != script_activity_list.load_script(list, experiment_item)) {
				b = new Bundle(1);
				b.putInt("experiment status", EXPERIMENT_OPEN_SCRIPT_ERROR);
				msg = mHandler.obtainMessage();
		        msg.setData(b);
			    mHandler.sendMessage(msg);
			    return;
			}
			current_instruct_data = (experiment_script_data)experiment_item.get(list.get(next_instruct_index));
			
			b = new Bundle(1);
			b.putInt("experiment status", EXPERIMENT_START);
			msg = mHandler.obtainMessage();
	        msg.setData(b);
		    mHandler.sendMessage(msg);
		    
			while (experiment_thread_run) {
				// instruct index from 0 to ...
				next_instruct_index = current_instruct_data.next_instruct_index;
				
				if (next_instruct_index >= (list.size()) || (true == experiment_stop)) {
					if (true == experiment_stop) {
						ret = experiment.shaker_off_instruct(current_instruct_data);
						experiment_stop = false;
					}
					experiment_thread_run = false;
					b = new Bundle(1);
					b.putInt("experiment status", EXPERIMENT_STOP);
					msg = mHandler.obtainMessage();
			        msg.setData(b);
				    mHandler.sendMessage(msg);
					break;
				}
						
			    current_instruct_data = (experiment_script_data)experiment_item.get(list.get(next_instruct_index));
				current_instruct_data.current_instruct_index = next_instruct_index;
				current_instruct_data.next_instruct_index = next_instruct_index;
				
				b = new Bundle(1);
				b.putInt("experiment status", EXPERIMENT_RUNNING);
			    b.putInt("current instruct index", current_instruct_data.current_instruct_index);
			    b.putInt("current instruct value", current_instruct_data.get_instruct_value());
			    b.putLong("current experiment time", new Date().getTime());
			    msg = mHandler.obtainMessage();
		        msg.setData(b);
			    mHandler.sendMessage(msg);
				
				switch(current_instruct_data.get_instruct_value()) {
				    case experiment_script_data.INSTRUCT_READ_SENSOR:
				        ret = experiment.read_sensor_instruct(current_instruct_data);
				        if (0 == ret) {
				        	sensor_data_composition sensor_data = experiment.get_current_one_sensor_data();
				        	String sensor_string = sensor_data.get_sensor_data_string();
				        	b = new Bundle(1);
				        	b.putInt("experiment status", EXPERIMENT_NOTIFY_CHART);
							b.putString("experiment sensor data", sensor_string);
							b.putSerializable("sensor_data_composition", sensor_data);
							msg = mHandler.obtainMessage();
					        msg.setData(b);
						    mHandler.sendMessage(msg);
				        }
				    break;
						
					case experiment_script_data.INSTRUCT_SHAKER_ON:
						ret = experiment.shaker_on_instruct(current_instruct_data);
					break;
						
					case experiment_script_data.INSTRUCT_SHAKER_OFF:
						ret = experiment.shaker_off_instruct(current_instruct_data);
					break;
						
					case experiment_script_data.INSTRUCT_SHAKER_SET_TEMPERATURE:
						ret = experiment.shaker_set_temperature_instruct(current_instruct_data);
					break;
						
					case experiment_script_data.INSTRUCT_SHAKER_SET_SPEED:
						ret = experiment.shaker_set_speed_instruct(current_instruct_data);
					break;
						
					case experiment_script_data.INSTRUCT_REPEAT_COUNT:
						ret = experiment.repeat_count_instruct(current_instruct_data);
					break;
						
					case experiment_script_data.INSTRUCT_REPEAT_TIME:
						//ret = experiment.repeat_time_instruct(current_instruct_data);
						ret = experiment.step_experiment_duration_instruct(current_instruct_data);
					break;

					case experiment_script_data.INSTRUCT_DELAY:
						ret = experiment.experiment_delay_instruct(current_instruct_data);
					break;
						
					default:
					break;
				}
			}
		}
	}
    
    public void doWork(final long elapsed) {
        runOnUiThread(new Runnable() {
            public void run() {
                try{
                    TextView txtCurrentTime= (TextView)findViewById(R.id.ExperimentTimer);
                    int hours = (int)(elapsed/3600);
                    int minutes = (int)((elapsed%3600)/60);
                    int seconds = (int)((elapsed%3600)%60);
                    String curTime = "Experiment elapsed time: "+ hours + ":" + minutes + ":" + seconds;
                    txtCurrentTime.setText(curTime);
                }catch (Exception e) {}
            }
        });
    }


    class CountDownRunner implements Runnable{
    	long elapsed = 0;
        // @Override
        public void run() {
                while(!Thread.currentThread().isInterrupted() && experiment_time_run){
                    try {
                        doWork(elapsed);
                        Thread.sleep(1000); // Pause of 1 Second
                        elapsed++;
                    } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                    }catch(Exception e){
                    }
                }
        }
    }
	

	/***********USB broadcast receiver*******************************************/
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
			if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
				synchronized (this) {
				    if (experiment.mODMonitorSensor != null && experiment.mODMonitorSensor.getDevice() != null) {
					    if (device.getProductId() == experiment.mODMonitorSensor.getDevice().getProductId() && device.getVendorId() == experiment.mODMonitorSensor.getDevice().getVendorId()) {
						    Log.i(Tag,"DETACHED sensor...");
						    experiment.mODMonitorSensor.DeviceOffline();
						    sensor_status.setEnabled(false);
						    return;
					    }
				    } else {
				    	
				    }
				    
				    if (experiment.shaker != null && experiment.shaker.getDevice() != null) {
					    if (device.getProductId() == experiment.shaker.getDevice().getProductId() && device.getVendorId() == experiment.shaker.getDevice().getVendorId()) {
						    Log.i(Tag,"DETACHED shaker...");
						    experiment.close_shaker_port();
						    experiment.shaker.DeviceOffline();
						    shaker_status.setEnabled(false);
						    return;
					    }
				    } else {
				    	
				    }
				} 	
			} else {
				if (action.equals(ACTION_USB_PERMISSION)) {
					synchronized (this) {
						Log.d(Tag, "mUsbReceiver  id:"+Thread.currentThread().getId() + "process:" + android.os.Process.myTid());
						if(device != null && experiment.mODMonitorSensor.getDevice() != null){
							if (device.getProductId() == experiment.mODMonitorSensor.getDevice().getProductId() && device.getVendorId() == experiment.mODMonitorSensor.getDevice().getVendorId()) {
								experiment.mODMonitorSensor.Enumeration(device);
								EnumerationDeviceShaker();
                        	}
                            Log.d(Tag,"PERMISSION-" + device);
                        }
						
						boolean is_permission = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false);
	                    if(device != null){
	                        if (experiment.mODMonitorSensor.getDevice() != null) {
	                            if (device.getProductId() == experiment.mODMonitorSensor.getDevice().getProductId() && device.getVendorId() == experiment.mODMonitorSensor.getDevice().getVendorId()) {
	                        		sensor_status.setEnabled(is_permission);
	                        		return;
	                        	}
	                        }
	                        	   
	                        if (experiment.shaker.getDevice() != null) {
	                        	if (device.getProductId() == experiment.shaker.getDevice().getProductId() && device.getVendorId() == experiment.shaker.getDevice().getVendorId()) { 
	                        		shaker_status.setEnabled(is_permission);
	                        		return;
	                        	}
	                        }
	                    }
	                }
				}
			}
		}	
	};
};



