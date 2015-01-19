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

import ODMonitor.App.R.drawable;
import ODMonitor.App.data.android_accessory_packet;
import ODMonitor.App.data.chart_display_data;
import ODMonitor.App.data.experiment_script_data;
import ODMonitor.App.data.machine_information;
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
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class ODMonitorActivity extends Activity {
	public String Tag = "ODMonitorActivity";
	private static final String ACTION_USB_PERMISSION = "OD.MONITOR.USB_PERMISSION";
	public UsbManager usbmanager;
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
    public int get_experiment_data_start = 0;
    public long script_length = 0;
    public int script_offset = 0;
    public byte[] script = null;
    private IDemoChart[] mCharts = new IDemoChart[] { new AverageTemperatureChart() };
    final Context context = this;
    
    /*thread to listen USB data*/
    public experiment_thread handlerThread;
    public sync_data sync_object;
    public sync_data sync_send_script;
    public sync_data sync_start_experiment;
    
    public TextView textView2;
    public ProgressDialog mypDialog;
    public sync_data sync_get_experiment;
    public sync_data sync_chart_notify;
    private boolean experiment_thread_run = false;
    private SwipeRefreshLayout laySwipe;
    
    /**
     * FTDI D2xx USB to UART
     */
    public static D2xxManager ftD2xx = null;
    public DeviceUART shaker;
    public ExperimentalOperationInstruct experiment;
    
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {     
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.od_monitor);
        Thread.currentThread().setName("Thread_ODMonitorActivity");
        
        
        
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        filter.setPriority(500);
        Log.d(Tag, "filter" +filter);
        this.registerReceiver(mUsbReceiver, filter);   
		
		shaker_return = (TextView)findViewById(R.id.ShakerReturn);
		shaker_return.setMovementMethod(new ScrollingMovementMethod());
		debug_view = (TextView)findViewById(R.id.DebugView);
		connect_status = (ImageView)findViewById(R.id.ConnectStatus);
		connect_status.setEnabled(false);
		mass_storage_status = (ImageView)findViewById(R.id.MassStorageStatus);
		mass_storage_status.setEnabled(false);
		sensor_status = (ImageView)findViewById(R.id.SensorStatus);
		sensor_status.setEnabled(false);
		shaker_status = (ImageView)findViewById(R.id.ShakerStatus);
		shaker_status.setEnabled(false);
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
		experiment = new ExperimentalOperationInstruct(this, ftD2xx, shaker_return);
               
		start_button = (ImageButton) findViewById(R.id.Button1);
		start_button.setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {
		        /*if (0 < shaker.createDeviceList()) {
			    	if (0 == shaker.connectFunction()) {
			    		shaker.SetConfig(shaker.baudRate, shaker.dataBit, shaker.stopBit, shaker.parity, shaker.flowControl);
			    		shaker.SendMessage("ON ");
			    		shaker.disconnectFunction();
			    	} else {
			    		Log.d(Tag, "shaker connect NG");
			    	} 		
			    } else {
			    	Log.d(Tag, "no device on list");
			    } */
		    /*	if (0 == experiment.check_shaker_port_number()) {
		    	    handlerThread = new experiment_thread(handler);
		    	    experiment_thread_run = true;
				    handlerThread.start();
		    	} else {
		    		Toast.makeText(ODMonitorActivity.this, "no find shaker device!",Toast.LENGTH_SHORT).show();
		    	}*/
		    	experiment.check_shaker_port_number();
		    	experiment.check_sensor_port_number();
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
				experiment_script_data current_instruct_data = new experiment_script_data();
				experiment.shaker_off_instruct(current_instruct_data);
			}
		});
        
        button4 = (ImageButton) findViewById(R.id.Button4);
        button4.setOnClickListener(new View.OnClickListener()
        {
			public void onClick(View v) {
        		show_chart_activity();
			}
		});  
        
        button5 = (ImageButton) findViewById(R.id.Button5);
        button5.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
        		//show_script_activity();
				experiment_script_data current_instruct_data = new experiment_script_data();
				experiment.read_sensor_instruct(current_instruct_data);
			}
		});  
        
        button6 = (ImageButton) findViewById(R.id.Button6);
        button6.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				//set_tablet_on_off_line((byte)0, false);
				//SensorDataReceive();
				experiment_script_data current_instruct_data = new experiment_script_data();
				experiment.shaker_on_instruct(current_instruct_data);
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
    
    public int set_tablet_on_off_line(byte status, boolean block) {
    	int ret = 0;
    	
		byte[] data = new byte[1];
		data[0] = status;
		if (0 != (ret = WriteUsbCommand(android_accessory_packet.DATA_TYPE_SET_TABLET_ON_OFF_LINE, android_accessory_packet.STATUS_OK, data, 1)))
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
			if (0 == get_experiment_data(true, true)) {
				if (0 == get_machine_information(true)) {
				    if (0 != set_tablet_on_off_line((byte)1, true)) {
				    	Log.d(Tag, "get_machine_information fail");
				    }
				} else {
					Log.d(Tag, "get_machine_information fail");
				}
			} else {
				Log.d(Tag, "get_experiment_data fail");
			}
			
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
	
	public void SensorDataReceive(android_accessory_packet rec) {
		file_operate_byte_array write_file = new file_operate_byte_array("od_sensor", "sensor_offline_byte", true);
		file_operate_byte_array read_file = new file_operate_byte_array("od_sensor", "sensor_offline_byte", true);
		byte[] byte_str = new byte[rec.get_Len_value()];
    	System.arraycopy(rec.buffer, android_accessory_packet.DATA_START, byte_str, 0, rec.get_Len_value());
    	String str = new String(byte_str);
    	
    	try {
    		int[] data = null;
            data = OD_calculate.parse_raw_data(str);
            if (data != null) {
            	long size = read_file.open_read_file(read_file.generate_filename_no_date());
         	    if (size <= 0)
         	        return;
         	    
         	    if (size >= (4*OD_calculate.experiment_data_size)+8) {
         	        read_file.seek_read_file(size-(long)(4*OD_calculate.experiment_data_size)+4);
        	        byte[] final_current_raw_index_bytes = new byte[4];
        	        read_file.read_file(final_current_raw_index_bytes);
        	        ByteBuffer byte_buffer = ByteBuffer.wrap(final_current_raw_index_bytes, 0, 4);
                    byte_buffer = ByteBuffer.wrap(final_current_raw_index_bytes, 0, 4);
                    int final_current_raw_index = byte_buffer.getInt();
                    
                    if (final_current_raw_index != data[OD_calculate.pre_raw_index_index]) {
                        return;
    	            }
         	    } else {
         	    	if ((data[OD_calculate.current_raw_index_index] != 0) || (data[OD_calculate.pre_raw_index_index] != 0))
         	    		return;
         	    }
            	
            	try {
            		write_file.create_file(write_file.generate_filename_no_date());
            		ByteBuffer byteBuffer = ByteBuffer.allocate(data.length * 4);        
                    IntBuffer intBuffer = byteBuffer.asIntBuffer();
                    intBuffer.put(data);
                    byte[] data_bytes = byteBuffer.array();
            		write_file.write_file(data_bytes);
            		write_file.flush_close_file();
            		shaker_return.append(str);
            		//shaker_return.setText(str);
            		// notify ODChartBuilder object has new sensor to display
            		if (sync_chart_notify != null) {
                	    synchronized (sync_chart_notify) {
                	    	sync_chart_notify.notify();
                	    }
                	}
        		} catch (IOException e) {
        			// TODO Auto-generated catch block
        			e.printStackTrace();
        			return;
        		}
            }
         } catch (IOException e) {
 	        // TODO Auto-generated catch block
 	        e.printStackTrace();
         }
	}
	
	public void convert_string_to_byte_file() {
		file_operation read_file = new file_operation("od_sensor", "sensor_offline", true);
        try {
	        read_file.open_read_file(read_file.generate_filename_no_date());
        } catch (IOException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        }
        
        file_operate_byte_array write_file = new file_operate_byte_array("od_sensor", "sensor_offline_byte", false);
    	try {
    	//	write_file.delete_file(write_file.generate_filename_no_date());
            write_file.create_file(write_file.generate_filename_no_date());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        try {    	
	        String sensor_str = new String();
	        sensor_str = read_file.read_file();
	        if(sensor_str != null) {
	        	byte[] temp = OD_calculate.parse_date(sensor_str);
	        	write_file.write_file(temp);
	        }
	        
	        sensor_str = read_file.read_file();
	        while (sensor_str != null) {
				int[] data = null;
		        data = OD_calculate.parse_raw_data(sensor_str);
		        if (data != null) {
		        	for (int i = 0; i < data.length; i++) {
		        		ByteBuffer byteBuffer = ByteBuffer.allocate(4);
		        		byte[] bytes = byteBuffer.putInt(data[i]).array();
		        		write_file.write_file(bytes);
		        	}
		        } else {
		        	Log.e(Tag, "parse raw data fail");
		        }
		        
		        sensor_str = read_file.read_file();
			} 
	        
	        write_file.flush_close_file();
        } catch (IOException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
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
		
	
	final Handler handler =  new Handler() {
    	@SuppressLint("DefaultLocale") @Override 
    	public void handleMessage(Message msg) {	
    		String view_str = msg.getData().getString("aoa thread exception", "no exception");
    		if (true == view_str.equals("aoa thread exception"))
			    debug_view.setText(view_str);
    		
    		Bundle b = msg.getData();
    		android_accessory_packet handle_receive_data = new android_accessory_packet(android_accessory_packet.NO_INIT_PREFIX_VALUE);
    		byte[] recv = b.getByteArray(android_accessory_packet.key_receive);
    		
    		Log.d(Tag, "Handler  id:"+Thread.currentThread().getId() + "process:" + android.os.Process.myTid());
    		
    		if (recv == null) {
    			Log.e(Tag, "handler recv is null!");
    			return;
    		}
    		handle_receive_data.copy_to_buffer(recv, android_accessory_packet.get_size());
    		switch (handle_receive_data.get_Type_value()) {
    		    case android_accessory_packet.DATA_TYPE_GET_MACHINE_STATUS: {
    		    	machine_information info = new machine_information();
    		    	info.copy_to_buffer(handle_receive_data.get_data(0, machine_information.TOTAL_SIZE), machine_information.TOTAL_SIZE);
    		    } break;
        		
    		    case android_accessory_packet.DATA_TYPE_SEND_SHAKER_COMMAND:
    		    	
        		break;
    		
    		    case android_accessory_packet.DATA_TYPE_GET_SHAKER_RETURN: {
    		    	byte[] byte_str = new byte[android_accessory_packet.get_data_size()];
    		    	System.arraycopy(handle_receive_data.buffer, android_accessory_packet.DATA_START, byte_str, 0, android_accessory_packet.get_data_size());
    		    	String str = new String(byte_str);
    		    	
    		    	shaker_return.setText(str);
    		    } break;
    			
    		    case android_accessory_packet.DATA_TYPE_GET_EXPERIMENT_DATA:
    		    	//if (1 == get_experiment_data_start) {
    		    		if (android_accessory_packet.STATUS_FAIL == handle_receive_data.get_Status_value()) {
    		    		    get_experiment_data_start = 0;
        		    		Toast.makeText(ODMonitorActivity.this, "Get experiment data fail", Toast.LENGTH_SHORT).show(); 
        		    		if (sync_get_experiment != null) {
    		        	        synchronized (sync_get_experiment) {
    		        	        	sync_get_experiment.set_status(sync_data.STATUS_END);
    		        	        	sync_get_experiment.set_is_timeout(false);
    		        	        	sync_get_experiment.notify();
    		        	        }
    		        	    }
    		    		} else {
    		    	        int len = handle_receive_data.get_Len_value();
    		    		    byte[] experiment_data = new byte[len];
        		    	    System.arraycopy(handle_receive_data.buffer, handle_receive_data.DATA_START, experiment_data, 0, len); 
        		            //String debug_str;
        		    	    //debug_str = String.format("prefix:%d, type:%d, status:%d, len:%d", handle_receive_data.get_Prefix_value(),
        		    	    //   		handle_receive_data.get_Type_value(), handle_receive_data.get_Status_value(), handle_receive_data.get_Len_value());
        		    	    //debug_view.setText(debug_str);
        		    	    //String experiment_str = new String(experiment_data);
        		    	    file_operate_byte_array write_file = new file_operate_byte_array("od_sensor", "sensor_offline", true);
        		     	    try {
        		                write_file.create_file(write_file.generate_filename_no_date());
        		    		    write_file.write_file(experiment_data);
        		    		    write_file.flush_close_file();
        				    } catch (IOException e) {
        					    // TODO Auto-generated catch block
        					    e.printStackTrace();
        				    }
        		    	
        		    	    if (android_accessory_packet.STATUS_OK == handle_receive_data.get_Status_value()) {
        		    		    get_experiment_data_start = 0;
        		    		    convert_string_to_byte_file();
        		    		    Toast.makeText(ODMonitorActivity.this, "Get experiment data complete", Toast.LENGTH_SHORT).show(); 
        		    		    if (sync_get_experiment != null) {
        		        	        synchronized (sync_get_experiment) {
        		        	        	sync_get_experiment.set_status(sync_data.STATUS_END);
        		        	        	sync_get_experiment.set_is_timeout(false);
        		        	        	sync_get_experiment.notify();
        		        	        }
        		        	    }
        		    	    } else if (android_accessory_packet.STATUS_HAVE_DATA == handle_receive_data.get_Status_value()) {
        		    	    	if (sync_get_experiment != null) {
        		        	        synchronized (sync_get_experiment) {
        		        	        	sync_get_experiment.set_meta_data(len);
        		        	        	sync_get_experiment.set_status(sync_data.STATUS_CONTINUE);
        		        	        	sync_get_experiment.set_is_timeout(false);
        		        	        	sync_get_experiment.notify();
        		        	        }
        		        	    }	
        		    	    }
    		    	    }	
    		    //	}
        		break;
        		
    		    case android_accessory_packet.DATA_TYPE_SET_EXPERIMENT_SCRIPT:
    		    	if (script_offset < script_length) {
    		    		long len = 0;
    		    		byte status = android_accessory_packet.STATUS_HAVE_DATA;
    		    		byte[] script_buffer;
    		    		if ((script_length-script_offset) > android_accessory_packet.DATA_SIZE)
    		    			len = android_accessory_packet.DATA_SIZE;
    		    		else
    		    			len = script_length-script_offset;
    		    		
    		    		script_buffer = new byte[(int)len];
    		    		System.arraycopy(script, script_offset, script_buffer, 0, (int)len);
    		    		if ((script_offset+len) == script_length)
    		    			status = android_accessory_packet.STATUS_OK;
    		    		else
    		    			status = android_accessory_packet.STATUS_HAVE_DATA;
    		    		WriteUsbCommand(android_accessory_packet.DATA_TYPE_SET_EXPERIMENT_SCRIPT, status, script_buffer, (int)len);
    		    		script_offset += len;
    		    	} else {
    		    		script_offset = 0;
    		    		script_length = 0;
    		    		script = null; 
    		    		synchronized (sync_send_script) {
    		    			sync_send_script.notify();
	        	        }
    		    	}
            	break;
    			
    		    case android_accessory_packet.DATA_TYPE_SET_EXPERIMENT_STATUS:
    		    	Log.d(Tag, "RECEIVE SET EXPERIMENT STATUS");
                break;
                
    		    case android_accessory_packet.DATA_TYPE_NOTIFY_EXPERIMENT_DATA:
    		    	SensorDataReceive(handle_receive_data);
        		break;	
        		
    		    case android_accessory_packet.DATA_TYPE_SET_TABLET_ON_OFF_LINE:
    		    	Log.d(Tag, "Set tablet on off line");
    		    	
    		    	if (sync_object != null) {
    		    	    synchronized (sync_object) {
    		    	    	sync_object.set_is_timeout(false);
    		    		    sync_object.notify();
    		    	    }
    		    	}
        		break;	
    		}
    	}
    };
	
	private class experiment_thread  extends Thread {
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
			
			Thread.currentThread().setName("Thread_Experiment");
			script_activity_list.load_script(list, experiment_item);
			current_instruct_data = (experiment_script_data)experiment_item.get(list.get(next_instruct_index));
			while(experiment_thread_run) {
				// instruct index from 0 to ...
				next_instruct_index = current_instruct_data.next_instruct_index;
				
				if (next_instruct_index >= (list.size())) {
					experiment_thread_run = false;
					break;
				}
						
			    current_instruct_data = (experiment_script_data)experiment_item.get(list.get(next_instruct_index));
				current_instruct_data.current_instruct_index = next_instruct_index;
				
				switch(current_instruct_data.get_instruct_value()) {
				    case experiment_script_data.INSTRUCT_READ_SENSOR:
				        ret = experiment.read_sensor_instruct(current_instruct_data);
				    break;
						
					case experiment_script_data.INSTRUCT_SHAKER_ON:
						ret =  experiment.shaker_on_instruct(current_instruct_data);
					break;
						
					case experiment_script_data.INSTRUCT_SHAKER_OFF:
						ret =  experiment.shaker_off_instruct(current_instruct_data);
					break;
						
					case experiment_script_data.INSTRUCT_SHAKER_SET_TEMPERATURE:
						ret =  experiment.shaker_set_temperature_instruct(current_instruct_data);
					break;
						
					case experiment_script_data.INSTRUCT_SHAKER_SET_SPEED:
						ret =  experiment.shaker_set_speed_instruct(current_instruct_data);
					break;
						
					case experiment_script_data.INSTRUCT_REPEAT_COUNT:
						ret =  experiment.repeat_count_instruct(current_instruct_data);
					break;
						
					case experiment_script_data.INSTRUCT_REPEAT_TIME:
						ret =  experiment.repeat_time_instruct(current_instruct_data);
					break;

					case experiment_script_data.INSTRUCT_DELAY:
						ret =  experiment.experiment_delay_instruct(current_instruct_data);
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
			if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
				Log.i(Tag,"DETACHED...");
				
	      
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
		}	
	};
};



