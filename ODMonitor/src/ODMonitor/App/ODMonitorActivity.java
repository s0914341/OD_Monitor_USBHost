package ODMonitor.App;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

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

import ODMonitor.App.ODMonitor_Sensor.CMD_T;
import ODMonitor.App.R.drawable;
import ODMonitor.App.data.android_accessory_packet;
import ODMonitor.App.data.chart_display_data;
import ODMonitor.App.data.experiment_script_data;
import ODMonitor.App.data.machine_information;
import ODMonitor.App.data.sensor_data_composition;
import ODMonitor.App.data.sync_data;
import ODMonitor.App.file.file_operate_byte_array;
import ODMonitor.App.file.file_operation;
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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
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
	//public static final int EXPERIMENT_SHOW_SENSOR_DATA = 3;
	public static final int EXPERIMENT_NOTIFY_CHART = 3;
	
	public static final long WAIT_TIMEOUT = 3000;
	public static final long WAIT_TIMEOUT_GET_EXPERIMENT = 10000;
	public byte  ledPrevMap = 0x00;
	//public byte[] usbdataIN;
	public android_accessory_packet acc_pkg_transfer = new android_accessory_packet(android_accessory_packet.INIT_PREFIX_VALUE);
	public android_accessory_packet acc_pkg_receive = new android_accessory_packet(android_accessory_packet.NO_INIT_PREFIX_VALUE);
	
	public SeekBar volumecontrol;
    public ProgressBar slider;
    
    public ImageButton start_button; //Button led1;
    public ImageButton button2; //Button led2;
    public ImageButton stop_button; //Button led3;
    public ImageButton button4; //Button led4;
    public ImageButton button5;
    public ImageButton button6;
    
    public ImageView ledvolume;
    
    public ImageView connect_status;
    public ImageView mass_storage_status;
    public ImageView sensor_status;
    public ImageView shaker_status;
    
    public EditText etInput; //shaker command input
    public TextView shaker_return;
    public TextView debug_view;
    public TextView sensor_data_view;
    public int get_experiment_data_start = 0;
    public long script_length = 0;
    public int script_offset = 0;
    public byte[] script = null;
    final Context context = this;
    
    /*thread to listen USB data*/
    public sync_data sync_object;
    public sync_data sync_send_script;
    public sync_data sync_start_experiment;
    
    public TextView textView2;
    public ProgressDialog mypDialog;
    public sync_data sync_get_experiment;
    public sync_data sync_chart_notify;
    private boolean experiment_thread_run = false;
    private boolean experiment_stop = false;
    private SwipeRefreshLayout laySwipe;
    public Chronometer experiment_timer;
    
    /**
     * FTDI D2xx USB to UART
     */
    public static D2xxManager ftD2xx = null;
    public DeviceUART shaker;
    public ExperimentalOperationInstruct experiment;
    boolean mRequest_USB_permission;
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
		
		shaker_return = (TextView)findViewById(R.id.ShakerReturn);
		shaker_return.setMovementMethod(new ScrollingMovementMethod());
		debug_view = (TextView)findViewById(R.id.ExperimentInformation);
		debug_view.setMovementMethod(new ScrollingMovementMethod());
		sensor_data_view = (TextView)findViewById(R.id.SensorData);
		sensor_data_view.setMovementMethod(new ScrollingMovementMethod());
		connect_status = (ImageView)findViewById(R.id.ConnectStatus);
		connect_status.setEnabled(false);
		mass_storage_status = (ImageView)findViewById(R.id.MassStorageStatus);
		mass_storage_status.setEnabled(false);
		sensor_status = (ImageView)findViewById(R.id.SensorStatus);
		sensor_status.setEnabled(false);
		shaker_status = (ImageView)findViewById(R.id.ShakerStatus);
		shaker_status.setEnabled(false);
		experiment_timer = (Chronometer)findViewById(R.id.ExperimentTimer);
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
		shaker = new DeviceUART(this, ftD2xx, shaker_return);
		experiment = new ExperimentalOperationInstruct(context, ftD2xx, shaker_return);
               
		start_button = (ImageButton) findViewById(R.id.Button1);
		start_button.setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {
		    	if (false == experiment_thread_run) {
		    	    experiment.initial_experiment_devices();
		    	    experiment_stop = false;
		    	    experiment_thread_run = true;
		    	    new Thread(new experiment_thread(handler)).start(); 
		    	}
		    }
		});
        
        /*button2 = (ImageButton) findViewById(R.id.Button2);
        button2.setOnClickListener(new View.OnClickListener() {		
			public void onClick(View v) {
				new Thread(new get_experiment_task()).start();
			}
		});*/
        
		stop_button = (ImageButton) findViewById(R.id.Button3);
		stop_button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				/*experiment_script_data current_instruct_data = new experiment_script_data();
				current_instruct_data.set_shaker_temperature_value(31);
				experiment.shaker_set_temperature_instruct(current_instruct_data);*/
				experiment_stop = true;
			}
		});
        
        button4 = (ImageButton) findViewById(R.id.Button4);
        button4.setOnClickListener(new View.OnClickListener()
        {
			public void onClick(View v) {
		        if ( experiment.mODMonitorSensor.isDeviceOnline() ) {
		        	//experiment.mODMonitorSensor.IOCTL( CMD_T.HID_CMD_ODMONITOR_REQUEST_RAW_DATA, 0, 0, null, 1 );
		        	experiment.mODMonitorSensor.IOCTL( CMD_T.HID_CMD_ODMONITOR_GET_RAW_DATA, 0, 0, null, 0 );
		        	//experiment.mODMonitorSensor.IOCTL( CMD_T.HID_CMD_ITRACKER_DATA, 0, 0, null, 0 );
		        }
        		show_chart_activity();
			}
		});  
        
        button5 = (ImageButton) findViewById(R.id.Button5);
        button5.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
        		show_script_activity();
				//experiment_script_data current_instruct_data = new experiment_script_data();
				//experiment.read_sensor_instruct(current_instruct_data);
			}
		});  
        
        button6 = (ImageButton) findViewById(R.id.Button6);
        button6.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				//set_tablet_on_off_line((byte)0, false);
				//SensorDataReceive();
				/*experiment_script_data current_instruct_data = new experiment_script_data();
				experiment.shaker_on_instruct(current_instruct_data);*/
			}
		});
        
        etInput = (EditText)findViewById(R.id.etInput); 
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
         });
        
 /*       laySwipe = (SwipeRefreshLayout) findViewById(R.id.laySwipe);
      //  laySwipe.setOnRefreshListener(this);
        laySwipe.setOnRefreshListener(onSwipeToRefresh);
        laySwipe.setColorSchemeResources(
        	    android.R.color.holo_red_light, 
        	    android.R.color.holo_blue_light, 
        	    android.R.color.holo_green_light, 
        	    android.R.color.holo_orange_light);*/
      
        Log.d ( Tag, "intent get action: " +this.getIntent().getAction());
        Log.d(Tag, "on Create");
        mRequest_USB_permission = false;
        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
		mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        EnumerationDevice( getIntent() );
    }
    
    public void EnumerationDevice(Intent intent) {
    	if (intent.getAction().equals(Intent.ACTION_MAIN)) {
    		if ( experiment.mODMonitorSensor.Enumeration() ) {
    			
    		}
    		else {
    			if ( experiment.mODMonitorSensor.isDeviceOnline() ) {
    				mRequest_USB_permission = true;
					mUsbManager.requestPermission(experiment.mODMonitorSensor.getDevice(), mPermissionIntent);
    			}
    			else {
    				
    			}
    		}
    	}
    	else
    		if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {
    			UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
    			if ( experiment.mODMonitorSensor.Enumeration(device) ) {
    				
    			}
    			else {
    				
    			}
    		}
    }
   /* private OnRefreshListener onSwipeToRefresh = new OnRefreshListener() {
        public void onRefresh() {
        	new Thread(new get_machine_info_task(UIhandler)).start();
        }
    };*/
    
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

    
    
    public int send_script(boolean block) {
    	int ret = 0;
    	
    	file_operate_byte_array read_file = new file_operate_byte_array("ExperimentScript", "ExperimentScript", true);
    	try {
    		script_length = read_file.open_read_file(read_file.generate_filename_no_date());
    		
    		if (script_length > 0) {
    		    script = new byte[(int)script_length];
    		    read_file.read_file(script);
    		    byte[] data = new byte[1];
        		data[0] = 0;
        		WriteUsbCommand(android_accessory_packet.DATA_TYPE_SET_EXPERIMENT_SCRIPT, android_accessory_packet.STATUS_START, data, 0);	
        		
        		if (false == block)
        			return ret;
        		
        		 synchronized (sync_send_script) {
     		        try {
     		        	sync_send_script.wait();
     			    } catch (InterruptedException e) {
     				    // TODO Auto-generated catch block
     				    e.printStackTrace();
     			    }
     		    }	
    		} else {
    			ret = -1;
    			Log.d(Tag, "open script fail");
    		}
		} catch (IOException e) {
			ret = -2;
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	return ret;
    }
    
    public void start_experiment(boolean run) {
    	file_operate_byte_array write_file = new file_operate_byte_array("od_sensor", "sensor_offline_byte", true);
		try {
			write_file.delete_file(write_file.generate_filename_no_date());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		byte[] data = new byte[9];
		if (true == run) {
		    data[0] = android_accessory_packet.STATUS_EXPERIMENT_START;
		} else {
			data[0] = android_accessory_packet.STATUS_EXPERIMENT_STOP;
		}
		// get current time data and write to file
		byte[] start_time_bytes = ByteBuffer.allocate(8).putLong(new Date().getTime()).array();
		try {
			write_file.create_file(write_file.generate_filename_no_date());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		write_file.write_file(start_time_bytes);
		write_file.flush_close_file();
		System.arraycopy(start_time_bytes, 0, data, 1, 8);
		WriteUsbCommand(android_accessory_packet.DATA_TYPE_SET_EXPERIMENT_STATUS, android_accessory_packet.STATUS_OK, data, 9);
    }
    
    public int get_experiment_data(boolean delete_file, boolean block) {
    	int ret = 0;
    	
    	if (true == delete_file) {
    	    file_operate_byte_array write_file = new file_operate_byte_array("od_sensor", "sensor_offline", true);
		    try {
			    write_file.delete_file(write_file.generate_filename_no_date());
		    } catch (IOException e) {
			    // TODO Auto-generated catch block
			    e.printStackTrace();
		    }
    	}
    	
    	int address = 0;
    	do {
    		address += sync_get_experiment.get_meta_data();
    	    byte[] data = android_accessory_packet.set_file_struct(address);
		  //  get_experiment_data_start = 1;
		    WriteUsbCommand(android_accessory_packet.DATA_TYPE_GET_EXPERIMENT_DATA, android_accessory_packet.STATUS_START, data, android_accessory_packet.GET_FILE_STRUCT_SIZE);
		
		    if (false == block)
			    return ret;
		
		    synchronized (sync_get_experiment) {
		        try {
		        	sync_get_experiment.set_is_timeout(true);
		    	    sync_get_experiment.wait(WAIT_TIMEOUT);
		    	    if (sync_object.get_is_time() == true) {
			    		ret = -1;
			    		break;
		    	    }
			    } catch (InterruptedException e) {
				    // TODO Auto-generated catch block
				    e.printStackTrace();
			    }
		    }
    	} while (sync_data.STATUS_END != sync_get_experiment.get_status());
    	
    	return ret;
    }
    
    public int get_machine_information(boolean block) {
    	int ret = 0;
    	
		byte[] data = new byte[1];
		data[0] = 0;
		if (0 != (ret = WriteUsbCommand(android_accessory_packet.DATA_TYPE_GET_MACHINE_STATUS, android_accessory_packet.STATUS_OK, data, 0)))
			return ret;
		
		if (false == block)
			return ret;
		
		synchronized (sync_object) {
		    try {
		    	sync_object.set_is_timeout(true);
		    	sync_object.wait(WAIT_TIMEOUT);
		    	if (sync_object.get_is_time() == true)
		    		ret = -1;
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return ret;
    }
    
    public void show_chart_activity() {
    	Intent intent = null;
    	//intent = mCharts[0].execute(this);
    	intent = new Intent(this, ODChartBuilder.class);
    	startActivity(intent);
    }
    
    public void show_script_activity() {
        	Intent intent = null;
        	intent = new Intent(this, script_activity_list.class);
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
	
	class initial_task implements Runnable {
		Handler mHandler;
		
		initial_task(Handler h) {
			mHandler = h;
		}
		
		public void run() {	
			Message msg = mHandler.obtainMessage();
			msg.arg1 = UI_SHOW_INITIAL_DIALOG;
		    mHandler.sendMessage(msg);
		
			msg = mHandler.obtainMessage();
			msg.arg1 = UI_CANCLE_INITIAL_DIALOG;
		    mHandler.sendMessage(msg);
	    } 	
	}
	
	class get_experiment_task implements Runnable {
		public void run() {	
			get_experiment_data(true, true);
	    } 	
	}
	
	class get_machine_info_task implements Runnable {
		Handler mHandler;
	
		get_machine_info_task(Handler h) {
			mHandler = h;
		}
		
		public void run() {
			String str_status;
			if (0 == get_machine_information(true))
				str_status = new String("refresh done!");
			else
				str_status = new String("refresh fail!");
			/**
			 * send message to UIhandler to do refresh UI task 
			 */
			Bundle b = new Bundle(1);
			Message msg = mHandler.obtainMessage();
			b.putString("get_machine_info_status", str_status);
			msg.arg1 = UI_GET_MACHINE_INFO_REFRESH;
			msg.setData(b);
		    mHandler.sendMessage(msg);
	    } 	
	}
	
	class start_experiment_task implements Runnable {
		Handler mHandler;
		boolean run = false;
		sync_data sync;
	
		start_experiment_task(Handler h, boolean run, sync_data sync) {
			mHandler = h;
			this.run = run;
			this.sync = sync;
		}
		
		public void run() {
			if (true == run) {
			    String str_status = new String("Set experiment exception");
			    switch (send_script(true)) {
			        case 0:
			    	    str_status = new String("Set experiment script success");
			        break;
			
	                case -1:
	            	    str_status = new String("Get script lenght < 0");
	                break;
	        
	                case -2:
	            	    str_status = new String("read_file constructor fail");
	                break;
	            } 
			
			    Bundle b = new Bundle(1);
			    Message msg = mHandler.obtainMessage();
			    b.putString("send_script_status", str_status);
			    msg.arg1 = UI_SEND_SCRIPT;
			    msg.setData(b);
		        mHandler.sendMessage(msg);
			}
			
			start_experiment(run);
			sync.set_status(sync_data.STATUS_END);
	    } 	
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
		        	experiment_timer.setBase(SystemClock.elapsedRealtime());
		        	experiment_timer.start();
		        break;
		        case EXPERIMENT_RUNNING:
		        	int current_instruct_index = b.getInt("current instruct index");
		    		int current_instruct_value = b.getInt("current instruct value");
		    		long current_experiment_time = b.getLong("current experiment time");
		    		
		    		String experiment_process = String.format("index:%d,   time:%d,   instruct:%s \n", current_instruct_index, current_experiment_time, experiment_script_data.SCRIPT_INSTRUCT.get(current_instruct_value));
		    		debug_view.setText(experiment_process);
		        break;
		        
		        case EXPERIMENT_STOP:
		        	experiment_timer.stop();
		        	experiment.close_shaker_port();
		        break;
		        
		     /*   case EXPERIMENT_SHOW_SENSOR_DATA:
		        	sensor_data_view.setText(b.getString("experiment sensor data", "no sensor data"));
		        break;*/
		        
		        case EXPERIMENT_NOTIFY_CHART:
		        	notify_chart_receive_data();
		        	sensor_data_view.setText(b.getString("experiment sensor data", "no sensor data"));
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
			script_activity_list.load_script(list, experiment_item);
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
				        	String sensor_string = experiment.get_current_one_sensor_data_string();
				        	b = new Bundle(1);
				        	b.putInt("experiment status", EXPERIMENT_NOTIFY_CHART);
							b.putString("experiment sensor data", sensor_string);
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
						ret = experiment.repeat_time_instruct(current_instruct_data);
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
	
	
	
	public int WriteUsbCommand(byte type, byte status, byte[] data, int len){	
	    int ret = 0;
	    
		acc_pkg_transfer.set_Type(type);
		acc_pkg_transfer.set_Status(status);
		acc_pkg_transfer.copy_to_data(data, len);
		acc_pkg_transfer.set_Len((byte)len);

		try{
			if(outputstream != null){
				outputstream.write(acc_pkg_transfer.buffer, 0,  len + android_accessory_packet.get_header_size());
			} else {
				ret = -1;
			}
		} catch (IOException e) {
			ret = -2;
		}		
		
		return ret;
	}
	
	/***********USB broadcast receiver*******************************************/
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() 
	{
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
			if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
				if ( experiment.mODMonitorSensor != null && experiment.mODMonitorSensor.getDevice() != null) {
					if (device.getProductId() == experiment.mODMonitorSensor.getDevice().getProductId() && device.getVendorId() == experiment.mODMonitorSensor.getDevice().getVendorId()) {
						Log.i(Tag,"DETACHED...");
						experiment.mODMonitorSensor.DeviceOffline();
					}
				}
				
	      
	            	/*switch (currect_index) 
	            	{

	        		case 5:
	        			((DeviceUARTFragment)currentFragment).notifyUSBDeviceDetach();
	        			break;
	            	default:
	            		//((DeviceInformationFragment)currentFragment).onStart();
	            		break;
	            	}*/
	                   	
			}
			else
				if (action.equals(ACTION_USB_PERMISSION)) {
					
				}
		}	
	};
};



