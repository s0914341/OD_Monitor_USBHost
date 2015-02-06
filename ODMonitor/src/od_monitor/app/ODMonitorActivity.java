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

import od_monitor.app.data.AndroidAccessoryPacket;
import od_monitor.app.data.ChartDisplayData;
import od_monitor.app.data.ExperimentScriptData;
import od_monitor.app.data.MachineInformation;
import od_monitor.app.data.SensorDataComposition;
import od_monitor.app.data.SyncData;
import od_monitor.app.file.ExportDatabaseCSVTask;
import od_monitor.app.file.FileOperateBmp;
import od_monitor.app.file.FileOperateByteArray;
import od_monitor.app.file.FileOperateObject;
import od_monitor.app.file.FileOperation;
import od_monitor.experiment.ExperimentalOperationInstruct;
import od_monitor.experiment.ODCalculate;
import od_monitor.experiment.ExperimentalOperationInstruct.repeat_informat;
import od_monitor.experiment.ODMonitorSensor.CMD_T;
import od_monitor.mail.EmailAlertData;
import od_monitor.mail.EmailSettingActivity;
import od_monitor.mail.SendMailSmtp;
import od_monitor.script.ScriptActivityList;
import od_monitor.script.StepScriptActivityList;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.chartdemo.demo.chart.AverageTemperatureChart;
import org.achartengine.chartdemo.demo.chart.IDemoChart;
import org.achartengine.chartdemo.demo.chart.ODChartBuilder;
import org.achartengine.chartdemo.demo.chart.ODChartToBitmap;
import org.achartengine.model.SeriesSelection;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import com.ftdi.j2xx.D2xxManager;

import od_monitor.app.R;
import od_monitor.app.R.drawable;
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
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
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
	public final static String Tag = ODMonitorActivity.class.getName();
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
	public static final int EXPERIMENT_EMAIL_ALERT = 5;
	
	public static final long WAIT_TIMEOUT = 3000;
	public static final long WAIT_TIMEOUT_GET_EXPERIMENT = 10000;
	public byte  ledPrevMap = 0x00;
	//public byte[] usbdataIN;
	public AndroidAccessoryPacket acc_pkg_transfer = new AndroidAccessoryPacket(AndroidAccessoryPacket.INIT_PREFIX_VALUE);
	public AndroidAccessoryPacket acc_pkg_receive = new AndroidAccessoryPacket(AndroidAccessoryPacket.NO_INIT_PREFIX_VALUE);
	
	public SeekBar volumecontrol;
    public ProgressBar slider;
    
    public ImageButton start_button;
    public ImageButton stop_button;
    public ImageButton chart_button;
    public ImageButton script_button;
    public ImageButton mail_button;
    
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
    public SyncData sync_object;
    public SyncData sync_send_script;
    public SyncData sync_start_experiment;
    
    public ProgressDialog mypDialog;
    public SyncData sync_get_experiment;
    public SyncData sync_chart_notify;
    private boolean experiment_thread_run = false;
    private boolean experiment_stop = false;
    private SwipeRefreshLayout laySwipe;
    public Thread experiment_time_thread = null;
    public boolean experiment_time_run = false;
    public EditText editText_init_od;
    
    /**
     * FTDI D2xx USB to UART
     */
    public static D2xxManager ftD2xx = null;
    public ExperimentalOperationInstruct experiment;
    boolean mRequest_USB_permission;
    
    GVTable table;
	SQLiteDatabase OD_monitor_db;
	int id;
	public static final String OD_VALUE_TABLE_NAME = "od";
	private static final String INDEX = "NO";
	private static final String DATE = "date";
	private static final String OD1 = "OD1";
	private static final String OD2 = "OD2";
	private static final String OD3 = "OD3";
	private static final String OD4 = "OD4";
	public static final String OD_CHANNEL_RAW_TABLE_NAME = "raw";
	private static final String CH1 = "CH1";
	private static final String CH2 = "CH2";
	private static final String CH3 = "CH3";
	private static final String CH4 = "CH4";
	private static final String CH5 = "CH5";
	private static final String CH6 = "CH6";
	private static final String CH7 = "CH7";
	private static final String CH8 = "CH8";
	
	public class mail_attach_file {
		public String file;
		public String content_type;
	};
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {     
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.od_monitor);
    	//LayoutInflater inflater = getLayoutInflater(); //調用Activity的getLayoutInflater()
    	
    	
    	
    	/*View v = inflater.inflate(R.layout.od_monitor, null, false);
    	if ( v instanceof LinearLayout ) {
    		Log.d ( Tag, " LinearLayout" );
    		LinearLayout lin_layout = ( LinearLayout ) v;
    		lin_layout.setDrawingCacheEnabled(true);
    		lin_layout.buildDrawingCache();
    		Bitmap bm = lin_layout.getDrawingCache();
    		
    		ImageView img_v = (ImageView) lin_layout.findViewById( R.id.ShakerStatus );
    		img_v.setDrawingCacheEnabled(true);
    		img_v.buildDrawingCache();
    		Bitmap bm1 = img_v.getDrawingCache();
    		
    		
    		FileOperateBmp write_file = new FileOperateBmp("od_chart", "chart", "png");
    		  try {
    			  write_file.create_file(write_file.generate_filename());
    		  } catch (IOException e) {
    			  // TODO Auto-generated catch block
    			  e.printStackTrace();
    		  }
    		  write_file.write_file(bm1, Bitmap.CompressFormat.PNG, 100);
    		  write_file.flush_close_file(); 
    	}*/
    	
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
		
		mypDialog = new ProgressDialog(this);
		mypDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		mypDialog.setTitle("OD Monitor");
		mypDialog.setMessage("Get device information!");
		//mypDialog.setIcon(R.drawable.android);
		//mypDialog.setButton("Google",this);
		mypDialog.setIndeterminate(true);
		mypDialog.setCancelable(false);
		sync_object = new SyncData();
		sync_chart_notify = new SyncData();
		sync_send_script = new SyncData();
		sync_start_experiment = new SyncData();
		ODMonitorApplication app_data = ((ODMonitorApplication)this.getApplication());
		app_data.set_sync_chart_notify(sync_chart_notify);
		
		try {
    		ftD2xx = D2xxManager.getInstance(this);
    	} catch (D2xxManager.D2xxException ex) {
    		ex.printStackTrace();
    	}
  
        SetupD2xxLibrary();
		//experiment = new ExperimentalOperationInstruct(context, ftD2xx, shaker_return);
        experiment = new ExperimentalOperationInstruct(context, ftD2xx, null);
        
        editText_init_od = (EditText) findViewById(R.id.editTextInitOD);
		editText_init_od.setText(experiment.get_init_od_string()); 
		editText_init_od.setOnFocusChangeListener(new View.OnFocusChangeListener() {
		        public void onFocusChange(View v, boolean hasFocus) {
		            // TODO Auto-generated method stub
		            if (!hasFocus) {
		            	if (editText_init_od.getText().toString().trim().equals("")) {
		            		editText_init_od.setText(experiment.get_init_od_string()); 
		            	} else {
		            	    double val = Double.parseDouble(editText_init_od.getText().toString().trim());
		                    if ((val > 0.05) || (val < 0)) {
		                    	editText_init_od.setError("0~0.05");
		                    } else {
		                    	editText_init_od.setError(null);
		                    }
		            	}
		            }
		        }
		    });
               
		start_button = (ImageButton) findViewById(R.id.ButtonStart);
		start_button.setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {
		    	boolean sensor_connected = sensor_status.isEnabled();
		    	boolean shaker_connected = shaker_status.isEnabled();
		    	
		    	if ((false == sensor_connected) || (false == shaker_connected)) {
		    		Toast.makeText(ODMonitorActivity.this, "devices is not ready!",Toast.LENGTH_SHORT).show();
		    	} else {
		    	    if (false == experiment_thread_run) {
		    	    	try {
		    	    	    double val = Double.parseDouble(editText_init_od.getText().toString().trim());
		    	    	    if ((val > 0.05) || (val < 0)) {
			                    editText_init_od.setError("0~0.05");
			                } else {
			                	editText_init_od.setError(null);
			                	editText_init_od.setEnabled(false);
			                	experiment.set_init_od(val);
			                	if (0 == experiment.initial_experiment_devices()) {
					    	        if (null != OD_monitor_db) {
					    	            if (true == OD_monitor_db.isOpen())
					    	            	OD_monitor_db.close();
					    	        }
					    	        	
					    	        table.gvRemoveAll();
					    	        experiment_stop = false;
					    	        experiment_thread_run = true;
					    	        new Thread(new experiment_thread(handler)).start(); 
					    	    } else {
					    	    	Toast.makeText(ODMonitorActivity.this, "initial experiment devices fail!",Toast.LENGTH_SHORT).show();
					    	    }
			                }
		    	        } catch (NumberFormatException ex) {
		    	        	editText_init_od.setError("0~0.05");
		    	        	Toast.makeText(ODMonitorActivity.this, "Please enter correct initial OD value!",Toast.LENGTH_SHORT).show();
	                	    Log.i(Tag, "button_ok NumberFormatException");
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
			//	new ExportDatabaseCSVTask(context, db, TABLE_NAME).execute("");
			//	SendMailSmtp mail = new SendMailSmtp();
			//	mail.SendMailUseSMTP();
		    	//View v = ODMonitorActivity.this.getWindow().getDecorView();
		    	/*LayoutInflater inflater = getLayoutInflater(); //調用Activity的getLayoutInflater()
		    	
		    	
		    	
		    	View v1 = inflater.inflate(R.layout.od_monitor, null, false);
		    	if ( v1 instanceof LinearLayout ) {

		    		LinearLayout lin_layout = ( LinearLayout ) v1;
		    		lin_layout.setDrawingCacheEnabled(true);
		    		lin_layout.buildDrawingCache();
		    		//Bitmap bm = lin_layout.getDrawingCache();
		    		for ( int i = 0; i < lin_layout.getChildCount(); i++ ) {
		    			
		    		}
		    		
		    		if (v1.getMeasuredHeight() <= 0) {
		    			v1.measure(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		    		}
		    		
		    		ImageView img_v = (ImageView) lin_layout.findViewById( R.id.ShakerStatus );
		    		//Bitmap bm = viewToBitmap ( v1, v1.getMeasuredWidth(), v1.getMeasuredHeight() );
		    		Bitmap bm = viewToBitmap ( img_v, (int) (img_v.getMeasuredWidth()*0.5), (int)(img_v.getMeasuredHeight()*0.5));
		    		
		    		FileOperateBmp write_file = new FileOperateBmp("od_chart", "chart", "png");
					try {
						write_file.create_file(write_file.generate_filename());
						write_file.write_file(bm, Bitmap.CompressFormat.PNG, 100);
						write_file.flush_close_file();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		    	}*/
				
				/*ODChartToBitmap chart = new ODChartToBitmap(ODMonitorActivity.this, 0, 0, 1024, 768);
	        	String chart_path = chart.get_file_dir() + chart.get_file_name();
	        	Log.d (Tag, chart_path);*/

			}
		});  
        
        mail_button = (ImageButton) findViewById(R.id.ButtonMail);
        mail_button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				show_email_activity();
			}
		});
        
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
		OD_monitor_db = SQLiteDatabase.create(null);
		Log.d("DB Path", OD_monitor_db.getPath());
		String amount = String.valueOf(databaseList().length);
		Log.d("DB amount", amount);

		String sql_od_value = "CREATE TABLE " + OD_VALUE_TABLE_NAME + " (" + 
		        INDEX	+ " text not null, " + DATE + " text not null," + OD1 + " text not null," +
		        OD2	+ " text not null, " + OD3 + " text not null," +
		        OD4	+ " text not null "+");";
		
		String sql_od_channel_raw = "CREATE TABLE " + OD_CHANNEL_RAW_TABLE_NAME + " (" + 
		        INDEX	+ " text not null, " + DATE + " text not null," + CH1 + " text not null," +
		        CH2	+ " text not null, " + CH3 + " text not null," + CH4 + " text not null," +
		        CH5	+ " text not null, " + CH6 + " text not null," + CH7 + " text not null," +
		        CH8	+ " text not null "+");";
		try {
			OD_monitor_db.execSQL("DROP TABLE IF EXISTS " + OD_VALUE_TABLE_NAME);
			OD_monitor_db.execSQL(sql_od_value);
			
			OD_monitor_db.execSQL("DROP TABLE IF EXISTS " + OD_CHANNEL_RAW_TABLE_NAME);
			OD_monitor_db.execSQL(sql_od_channel_raw);
		} catch (SQLException e) {}
	}
	
	void FileToODDataDB() {
		Date date = null;
        double od_value = 0;
        long size = 0;
        
        FileOperateByteArray read_file = new FileOperateByteArray(SensorDataComposition.sensor_raw_folder_name, SensorDataComposition.sensor_raw_file_name, true);
        try {
        	size = read_file.open_read_file(read_file.generate_filename_no_date());
        } catch (IOException e) {
	        // TODO Auto-generated catch block
        	Log.d(Tag, "file open fail!");
	        e.printStackTrace();
	        return;
        }
        
        if (size >= SensorDataComposition.total_size) {
			int offset = 0;
		    byte[] data = new byte[(int) size];
		    read_file.read_file(data);
		    SensorDataComposition one_sensor_data = new SensorDataComposition();
		    CreateODDataDB();
			
			while ((size-offset) >= SensorDataComposition.total_size) {
				one_sensor_data.set_buffer(data, offset, SensorDataComposition.total_size);
				date = new Date(one_sensor_data.get_sensor_measurement_time());
				od_value = one_sensor_data.get_sensor_od_value();    
				offset += SensorDataComposition.total_size;
				InsertODDateToDB(one_sensor_data);
			}
			
			table.gvUpdatePageBar("select count(*) from " + OD_VALUE_TABLE_NAME, OD_monitor_db);
		    table.gvReadyTable("select * from " + OD_VALUE_TABLE_NAME, OD_monitor_db);
			table.refresh_last_table();
		} else {
			Log.d(Tag, "file is nothing to show!");
		}
	}

	void InsertODDateToDB(SensorDataComposition sensor_data) {
		String sql_od_value = "insert into " + OD_VALUE_TABLE_NAME + " (" + 
			INDEX + ", " + DATE + ", " + OD1 + ", " + OD2 + ", " + OD3 + ", " + OD4
					+ ") values('" + sensor_data.get_sensor_get_index_string()
					+ "', '" + sensor_data.get_sensor_measurement_time_string() + "','"
					+ sensor_data.get_sensor_od_value_string() + "','NA','NA','NA');";
		
		int[] channel_data = sensor_data.get_channel_data();
		String[] channel_data_string = {Integer.toString(channel_data[0]), Integer.toString(channel_data[1]), Integer.toString( channel_data[2]),
				                        Integer.toString(channel_data[3]), Integer.toString(channel_data[4]), Integer.toString(channel_data[5]),
				                        Integer.toString(channel_data[6]), Integer.toString(channel_data[7])};
		
		String sql_od_channel_raw = "insert into " + OD_CHANNEL_RAW_TABLE_NAME + " (" + 
				INDEX + ", " + DATE + ", " + CH1 + ", " + CH2 + ", " + CH3 + ", " + CH4
				        + ", " + CH5 + ", " + CH6 + ", " + CH7 + ", " + CH8
						+ ") values('" + sensor_data.get_sensor_get_index_string()
						+ "', '" + sensor_data.get_sensor_measurement_time_string() + "','"
						+ channel_data_string[0] + "','"
						+ channel_data_string[1] + "','"
						+ channel_data_string[2] + "','"
						+ channel_data_string[3] + "','"
						+ channel_data_string[4] + "','"
						+ channel_data_string[5] + "','"
						+ channel_data_string[6] + "','"
						+ channel_data_string[7] + "');";
		try {
			OD_monitor_db.execSQL(sql_od_value);
			OD_monitor_db.execSQL(sql_od_channel_raw);
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
        	intent = new Intent(this, StepScriptActivityList.class);
        	startActivity(intent);
    }
    
    public void show_email_activity() {
    	Intent intent = null;
    	intent = new Intent(this, EmailSettingActivity.class);
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
		return super.onCreateOptionsMenu(menu);
	}
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch(item.getItemId()) {
    	    case R.id.item_send_email:
    	    	List<mail_attach_file> list_mail_attach = new ArrayList<mail_attach_file>();
	        	list_mail_attach.add(export_experiment_chart());
	        	
	        	if (null != OD_monitor_db) {
	        	    list_mail_attach.add(export_experiment_csv(OD_monitor_db, OD_VALUE_TABLE_NAME));
	        	    list_mail_attach.add(export_experiment_csv(OD_monitor_db, OD_CHANNEL_RAW_TABLE_NAME));
	        	} else {
	        		Log.d (Tag, "OD_monitor_db is not created!");
	        	}
	        	
	        	SendMailSmtp mail = new SendMailSmtp();
	    		mail.SendMailUseSMTP(list_mail_attach);
    	    	Log.d(Tag, "send email!");
    	    break;
    	    
    	    case R.id.item_export_raw_data:
    	    	if (null != OD_monitor_db) {
    	    		export_experiment_csv(OD_monitor_db, OD_CHANNEL_RAW_TABLE_NAME);
    	    	} else {
    	    		Toast.makeText(ODMonitorActivity.this, "OD_monitor_db is not created!",Toast.LENGTH_SHORT).show();
    	    		Log.d(Tag, "no data base is created!");
    	    	}
        	break;
        	
    	    default:
                return super.onOptionsItemSelected(item);
    	}
    	
    	return super.onOptionsItemSelected(item);
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
	
	public void notify_chart_receive_data() {
		if (sync_chart_notify != null) {
	        synchronized (sync_chart_notify) {
	        	sync_chart_notify.set_status(SyncData.STATUS_DATA_AVAILABLE);
	    	    sync_chart_notify.notify();
	        }
	    }
	}
	
	public mail_attach_file export_experiment_chart() {
		mail_attach_file mail_attach = new mail_attach_file();
    	
    	ODChartToBitmap chart = new ODChartToBitmap(ODMonitorActivity.this, 0, 0, 1024, 768);
    	mail_attach.file = chart.get_file_dir() + "/" + chart.get_file_name();
    	mail_attach.content_type = "image/png";
    	Log.d (Tag, mail_attach.file);
    	
    	return mail_attach;
	}
	
	public mail_attach_file export_experiment_csv(SQLiteDatabase db, String table_name) {
		mail_attach_file mail_attach = new mail_attach_file();
    	
    	ExportDatabaseCSVTask csv = new ExportDatabaseCSVTask(context, db, table_name);
    	csv.ExportDatabaseCSVImmediately();
    	mail_attach.file = csv.get_file_dir() + "/" +csv.get_file_name();
    	mail_attach.content_type = "text/csv";
    	Log.d (Tag, mail_attach.file);
    
    	return mail_attach;
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
		    		
		    		String experiment_process = "index: " + current_instruct_index + ",  " + "time: " + current_experiment_time + ",  " + "instruct: " + ExperimentScriptData.SCRIPT_INSTRUCT.get(current_instruct_value) + "\n";
		    		//String experiment_process = String.format("index:%d,   time:%d,   instruct:%s \n", current_instruct_index, current_experiment_time, experiment_script_data.SCRIPT_INSTRUCT.get(current_instruct_value));
		    		//debug_view.setText(experiment_process);
		        break;
		        
		        case EXPERIMENT_STOP:
		        	experiment_time_run = false;
		        	experiment.close_shaker_port();
		        	if (null != OD_monitor_db) {
		        	    new ExportDatabaseCSVTask(context, OD_monitor_db, OD_VALUE_TABLE_NAME).ExportDatabaseCSVImmediately();
		        	}
		        	editText_init_od.setEnabled(true);
		        break;
		        
		        case EXPERIMENT_NOTIFY_CHART:
		        	notify_chart_receive_data();
		        	SensorDataComposition sensor_data = (SensorDataComposition)b.getSerializable("sensor_data_composition");
		        	if (null != sensor_data) {
		        		InsertODDateToDB(sensor_data);
		        		table.gvUpdatePageBar("select count(*) from " + OD_VALUE_TABLE_NAME, OD_monitor_db);
						table.gvReadyTable("select * from " + OD_VALUE_TABLE_NAME, OD_monitor_db);
						table.refresh_last_table();
		        	}
		        	
		        //	sensor_data_view.setText(b.getString("experiment sensor data", "no sensor data"));
		        break;
		        
		        case EXPERIMENT_OPEN_SCRIPT_ERROR:
		        	Toast.makeText(getApplicationContext(), "Script open error!", Toast.LENGTH_SHORT).show();
		        break;
		        
		        case EXPERIMENT_EMAIL_ALERT:
		        	List<mail_attach_file> list_mail_attach = new ArrayList<mail_attach_file>();
		        	list_mail_attach.add(export_experiment_chart());
		        	
		        	if (null != OD_monitor_db) {
		        	    list_mail_attach.add(export_experiment_csv(OD_monitor_db, OD_VALUE_TABLE_NAME));
		        	    list_mail_attach.add(export_experiment_csv(OD_monitor_db, OD_CHANNEL_RAW_TABLE_NAME));
		        	} else {
		        		Log.d (Tag, "OD_monitor_db is not created!");
		        	}
		        	
		        	SendMailSmtp mail = new SendMailSmtp();
		    		mail.SendMailUseSMTP(list_mail_attach);
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
			ExperimentScriptData current_instruct_data;
			int ret = 0;
			Bundle b = new Bundle(1);
			Message msg;
			EmailAlertData email_set;
			
			Thread.currentThread().setName("Thread_Experiment");
			/* load script file fail, send message to handler show Toast */
			if (0 != ScriptActivityList.load_script(list, experiment_item)) {
				b = new Bundle(1);
				b.putInt("experiment status", EXPERIMENT_OPEN_SCRIPT_ERROR);
				msg = mHandler.obtainMessage();
		        msg.setData(b);
			    mHandler.sendMessage(msg);
			    return;
			}
			current_instruct_data = (ExperimentScriptData)experiment_item.get(list.get(next_instruct_index));
			
			/* read email alert setting file */
			FileOperateObject read_file = new FileOperateObject(EmailAlertData.email_alert_folder_name, EmailAlertData.email_alert_file_name);
		    try {
				read_file.open_read_file(read_file.generate_filename_no_date());
				email_set = (EmailAlertData)read_file.read_file_object();
				if (null != email_set) {
					experiment.set_mail_alert_interval(EmailAlertData.REMINDER_INTERVAL.get(email_set.get_reminder_interval()));
				}
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				return;
			}
			
			/* send message to handler, start experiment timer and create OD SQLite data base */
			b = new Bundle(1);
			b.putInt("experiment status", EXPERIMENT_START);
			msg = mHandler.obtainMessage();
	        msg.setData(b);
		    mHandler.sendMessage(msg);
		    
			while (experiment_thread_run) {
				// instruct index from 0 to ...
				next_instruct_index = current_instruct_data.next_instruct_index;
				/* if next_instruct_index over list size or user push stop button, experiment will end run */
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
						
			    current_instruct_data = (ExperimentScriptData)experiment_item.get(list.get(next_instruct_index));
				current_instruct_data.current_instruct_index = next_instruct_index;
				current_instruct_data.next_instruct_index = next_instruct_index;
				
				/* compare mail alert interval with current system time, if arrival, send message to handler to send Email*/
				if (experiment.get_is_mail_alert()) {
					b = new Bundle(1);
					b.putInt("experiment status", EXPERIMENT_EMAIL_ALERT);
				    msg = mHandler.obtainMessage();
			        msg.setData(b);
				    mHandler.sendMessage(msg);
					/*b = new Bundle(1);
					b.putInt("experiment status", EXPERIMENT_RUNNING);
				    b.putInt("current instruct index", current_instruct_data.current_instruct_index);
				    b.putInt("current instruct value", current_instruct_data.get_instruct_value());
				    b.putLong("current experiment time", new Date().getTime());
				    msg = mHandler.obtainMessage();
			        msg.setData(b);
				    mHandler.sendMessage(msg);*/
				}
				
				switch(current_instruct_data.get_instruct_value()) {
				    case ExperimentScriptData.INSTRUCT_READ_SENSOR:
				        ret = experiment.read_sensor_instruct(current_instruct_data);
				        /* send message to handler, to notify OD chart refresh view data */
				        if (0 == ret) {
				        	SensorDataComposition sensor_data = experiment.get_current_one_sensor_data();
				        	String sensor_string = sensor_data.get_sensor_data_string();
				        	b = new Bundle(1);
				        	b.putBoolean("mail alert", experiment.get_is_mail_alert());
				        	b.putInt("experiment status", EXPERIMENT_NOTIFY_CHART);
							b.putString("experiment sensor data", sensor_string);
							b.putSerializable("sensor_data_composition", sensor_data);
							msg = mHandler.obtainMessage();
					        msg.setData(b);
						    mHandler.sendMessage(msg);
				        }
				    break;
						
					case ExperimentScriptData.INSTRUCT_SHAKER_ON:
						ret = experiment.shaker_on_instruct(current_instruct_data);
					break;
						
					case ExperimentScriptData.INSTRUCT_SHAKER_OFF:
						ret = experiment.shaker_off_instruct(current_instruct_data);
					break;
						
					case ExperimentScriptData.INSTRUCT_SHAKER_SET_TEMPERATURE:
						ret = experiment.shaker_set_temperature_instruct(current_instruct_data);
					break;
						
					case ExperimentScriptData.INSTRUCT_SHAKER_SET_SPEED:
						ret = experiment.shaker_set_speed_instruct(current_instruct_data);
					break;
						
					case ExperimentScriptData.INSTRUCT_REPEAT_COUNT:
						ret = experiment.repeat_count_instruct(current_instruct_data);
					break;
						
					case ExperimentScriptData.INSTRUCT_REPEAT_TIME:
						//ret = experiment.repeat_time_instruct(current_instruct_data);
						ret = experiment.step_experiment_duration_instruct(current_instruct_data);
					break;

					case ExperimentScriptData.INSTRUCT_DELAY:
						ret = experiment.experiment_delay_instruct(current_instruct_data);
					break;
						
					default:
					break;
				}
			}
			
			Log.d(Tag, "exit experiment thread run!");
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



