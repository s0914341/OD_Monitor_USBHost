package od_monitor.experiment;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.locks.ReentrantLock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.hardware.usb.UsbRequest;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.ParcelFileDescriptor;
import android.util.Log;

public class ODMonitor_Sensor {
	//usb host
	public final String Tag = "ODMonitor_Sensor";
	private Context mContext;
    private UsbManager mManager;
    private UsbDevice mDevice;
    private UsbDeviceConnection mDeviceConnection;
    private UsbInterface mInterface;
    UsbEndpoint mEndpointOut = null;
    UsbEndpoint mEndpointIn = null;
    private BroadcastReceiver mReceiver;
    public static final int USB_VID = 0x0416;
    public static final int USB_PID = 0x5021;
    //public static final int USB_PID = 0xC142;
    public static final int USB_class = 0xff;
    //public static final int USB_class = 0xff;
    public static final int USB_subclass = 0x00;
    public static final int USB_protocol = 0x00;
/*20130316 added by michael*/
    // pool of requests for the OUT endpoint
    private final LinkedList<UsbRequest> mOutRequestPool = new LinkedList<UsbRequest>();
    private final LinkedList<UsbRequest> mInRequestPool = new LinkedList<UsbRequest>();
    
    private final CMD_T message;
    public int show_temp_msg;
    public FileOutputStream fos;
    IntBuffer Sensor_dev_data;

    /*20131208 added by michael*/
    Object lock1, lock2;
    /*20131224 added by michael*/
    private final ReentrantLock lock3 = new ReentrantLock();
    /*20140731 added by michael*/
	public int Fw_Version_Code;
	public String Fw_Version_Name, Fw_md5_checksum;
	public int Hw_Version_Code;
	public byte[] fw_header_bytes = new byte[256];
	/*20140811 added by michael*/
/*	char Version_Name[48];
	int bin_size;
	unsigned char check_digits[16];*/

    
    public ODMonitor_Sensor(Context context) {
    	mContext = context;
    	mDevice = null;
    	mInterface = null;
    	mDeviceConnection = null;
    	mReceiver = null;
    	message = new CMD_T();
    	show_temp_msg = 1;
    	try {
			fos = new FileOutputStream("/mnt/sdcard/debug.txt");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	mManager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);    	
    	
    	/*20131208 added by michael*/
    	lock1 = new Object();
    	lock2 = new Object();
    	
    	/*20140731 added by michael*/
    	Reset_Device_Info();
    }
    
    /*20140731 added by michael*/
    public void Reset_Device_Info() {
    	Fw_Version_Code = -1;
    	Fw_Version_Name = "";
    	Fw_md5_checksum = "";
    }
/*    private void RegisterReceiver() {
    	IntentFilter int_filter;
    	
    	mReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
					UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
					if (device==mDevice) {
						mDevice = null;
						mInterface = null;
					}
				}
			}
    		
    	};
    	int_filter = new IntentFilter();
    	int_filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
    	mContext.registerReceiver(mReceiver, int_filter);
    }*/
    
    public UsbInterface findInterface(UsbDevice device) {
        int count = device.getInterfaceCount();

        UsbInterface intf = null;
        for (int i = 0; i < count; i++) {
            intf = device.getInterface(i);
            if (intf.getInterfaceClass()==USB_class && intf.getInterfaceSubclass()==USB_subclass &&
                    intf.getInterfaceProtocol()==USB_protocol) {
            	return intf;
            }
        }
        return null;
    }
    
	public boolean Enumeration() {
		mManager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
		show_debug(Tag+"The line number is " + new Exception().getStackTrace()[0].getLineNumber()+"\n");
		for (UsbDevice device : mManager.getDeviceList().values()) {
			if (device != null) {
				if (device.getVendorId() == USB_VID
						&& device.getProductId() == USB_PID) {
					UsbInterface intf = findInterface(device);

					if (device != null && intf != null) {
						// RegisterReceiver();
						if (setInterface(device, intf)) {
							show_debug(Tag
									+ "The line number is "
									+ new Exception().getStackTrace()[0]
											.getLineNumber() + "\n");
							return true;
						}
					}
				}
			}

			/*
			 * UsbInterface intf = findAdbInterface(device); if
			 * (setAdbInterface(device, intf)) { break; }
			 */
		}
		show_debug(Tag+"The line number is " + new Exception().getStackTrace()[0].getLineNumber()+"\n");
/*		if (mReceiver != null) {
			  mContext.unregisterReceiver(mReceiver);
			  mReceiver = null;
		}*/
		return false;
	}

	public boolean Enumeration(UsbDevice device) {
		
		if (device != null) {
		    if (device.getVendorId() == USB_VID && device.getProductId() == USB_PID) {
		        UsbInterface intf = findInterface(device);
		        if (intf != null) {
			        //RegisterReceiver();
			        if (setInterface(device, intf))
				        return true;
		        }
		    }
		}
		
/*		if (mReceiver != null) {
		  mContext.unregisterReceiver(mReceiver);
		  mReceiver = null;
		}*/
		return false;
	}
	
//device DETACHED 
	public void DeviceOffline() {
		setInterface(null, null);
		mOutRequestPool.clear();
		mInRequestPool.clear();
	}
	
	public boolean isDeviceOnline() {
		if (mDevice != null && mInterface != null) {
			Log.d(Tag, "device is online");
			return true;
		} else {
			Log.d(Tag, "device is not online");
			return false;
		}
	}
	
	public UsbDevice getDevice() {
		return mDevice;
	}

	public void reset() {
	}
	
	/*20130318 added by michael*/
	//deal with the following Itracker data
	/*mItracker_dev.coord_index;
	mItracker_dev.Valid_Coord_Buf;
	mItracker_dev.Valid_Coord_Histogram;
	mItracker_dev.Valid_Coord_Buf_Seq;
	mItracker_dev.Valid_Coord_Seq_Index;
	mItracker_dev.Valid_Coord_Back_For;*/
	//if need update well plate UI return 1 else return 0
	
    // get an OUT request from our pool
    public UsbRequest getOutRequest() {
        //synchronized(mOutRequestPool) {
		UsbRequest request;
		/*
		 * 20131227 modified by michael validation the request
		 */
		if (mOutRequestPool.isEmpty()) {
			request = new UsbRequest();
			if (request.initialize(mDeviceConnection, mEndpointOut))
				return request;
			else
				return null;
		} else {
			request = mOutRequestPool.removeFirst();
			if (request.initialize(mDeviceConnection, mEndpointOut))
				return request;
			else
				return null;
		}
        //}
    }	

    // get an IN request from the pool
    public UsbRequest getInRequest() {
        //synchronized(mInRequestPool) {
		UsbRequest request;
		/*
		 * 20131227 modified by michael validation the request
		 */
		if (mInRequestPool.isEmpty()) {
			request = new UsbRequest();
			if (request.initialize(mDeviceConnection, mEndpointIn))
				return request;
			else
				return null;
		} else {
			request = mInRequestPool.removeFirst();
			if (request.initialize(mDeviceConnection, mEndpointIn))
				return request;
			else
				return null;
		} 
        //}
    }

    boolean mDebug_Itracker = false;
    public void show_debug(String str) {
		//if (show_temp_msg==1)
			//  Toast.makeText(this.mContext.getApplicationContext(), Tag+"The line number is " + new Exception().getStackTrace()[0].getLineNumber(), Toast.LENGTH_LONG).show();
    	;
		if (mDebug_Itracker == true) {
			try {
				fos.write(str.getBytes());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
    }
	/*20130314 added by michael*/	
	//device IOCTL
    //public synchronized boolean Itracker_IOCTL(int itracker_cmd, int debug) {
    //public boolean Itracker_IOCTL(int itracker_cmd, int debug) {
    public boolean IOCTL(int itracker_cmd, int arg0, int arg1, byte[] dataBytes, int debug) {
    	boolean result = false;
    	
    	synchronized(lock1)
    	{
    	if (isDeviceOnline()) {
		message.set(itracker_cmd, arg0, arg1, dataBytes, debug);
		result = message.process_command(0);
		if (itracker_cmd==CMD_T.HID_CMD_ITRACKER_DATA && result) {
			/*struct I_tracker_type {
				  int Is_Running;
				  int coord_index;
				  Well_Coord_t Valid_Coord_Buf[Max_Coord_Buf];
				  int buffer_locked;
				  Well_Coord_t Newest_Valid_Coord;
				  int X_Led_failure, X_Sensor_failure, Y_Led_failure, Y_Sensor_failure;  //offset from 56
				  int failure_detect_ready;
				};*/
			/*struct I_tracker_type store into integer buffer*/
			Sensor_dev_data = message.mDataBuffer.asIntBuffer();
			if (Sensor_dev_data.limit()==64) {
			coord_index = Sensor_dev_data.get(1);
			//Valid_Coord_Buf = new int [Max_Coord_Buf];
			//Arrays.fill(Valid_Coord_Buf, 0x00);
			Sensor_dev_data.position(2);
			//Sensor_dev_data.get(Valid_Coord_Buf);
			/*20131210 added by michael*/
			//buffer_locked = Sensor_dev_data.get(Sensor_dev_data.limit()-2);
			buffer_locked = Sensor_dev_data.get(12);
			//X_Led_failure = Sensor_dev_data.get(14);
			//X_Sensor_failure = Sensor_dev_data.get(15);
			//Y_Led_failure = Sensor_dev_data.get(16);
			//Y_Sensor_failure = Sensor_dev_data.get(17);
			//failure_detect_ready = Sensor_dev_data.get(18);
			}
			else
				return false;
		}
		else
			if (itracker_cmd==CMD_T.HID_CMD_ITRACKER_FW_HEADER && result) {
				 /* refer sizeof(struct FW_Header)
				struct FW_Header {
	                  int Version_Code;
	                  char Version_Name[48];
	                  int bin_size;
	                  int HW_Version_Code;
	                  unsigned char check_digits[16];
                };*/
				//System.arraycopy(message.mDataBuffer.array(), 0, dataBytes, 0, (arg1-arg0)*PAGE_SIZE);
				StringBuffer sb = new StringBuffer("");
				byte b;
				message.mDataBuffer.get(dataBytes, 0, (arg1-arg0)*PAGE_SIZE);
				System.arraycopy(dataBytes, 0, fw_header_bytes, 0, (arg1-arg0)*PAGE_SIZE);
				message.mDataBuffer.order(ByteOrder.LITTLE_ENDIAN);				
				/*message.mDataBuffer.position(0);
				message.mDataBuffer.position(4);
				message.mDataBuffer.position(52);
				message.mDataBuffer.position(56);*/
				message.mDataBuffer.position(0);
				Fw_Version_Code = message.mDataBuffer.getInt();
				message.mDataBuffer.position(4);
				for (int i = 0; i < 48; i++) {
					b = message.mDataBuffer.get();
					if (b!=0x00)
						sb.append(new String(new byte []{b}));
					else
						break;
				}
				Fw_Version_Name = sb.toString();
				sb.delete(0, sb.length());
				message.mDataBuffer.position(56);
				Hw_Version_Code = message.mDataBuffer.getInt();
				message.mDataBuffer.position(60);
				for (int i = 0; i < 16; i++) {
					sb.append(Integer.toString((message.mDataBuffer.get() & 0xff) + 0x100, 16).substring(1));
				}
				this.Fw_md5_checksum = sb.toString();
			}
			else
				if (itracker_cmd==CMD_T.HID_CMD_ODMONITOR_GET_RAW_DATA && result) {
				//	Sensor_dev_data = message.mDataBuffer.asIntBuffer();
					System.arraycopy(message.mDataBuffer.array(), 0, dataBytes, 0, dataBytes.length);
				/*	for ( int i = 0; i < 10; i++ )
						Log.d ( Tag, Integer.toString(Sensor_dev_data.get(i)));*/
				}
		if (result)
			return true;
		else
			return false;
    	}
    	else
    		return false;
    	}
	}

/*struct I_tracker_setting_type {
int Well_Plate_Mode;
};*/    
	public static final int SZ_I_tracker_setting_type = Integer.SIZE / Byte.SIZE;
/*	struct Well_Coord_t {
	//  uint8_t Liquid_Count[Max_well_X][Max_well_Y];
	  unsigned int Coord_X:5;
	  unsigned int Coord_Y:5;
	  unsigned int Coord_X_Count:4;
	  unsigned int Coord_Y_Count:4;
	};

	struct I_tracker_type {
	  int Is_Running;
	  int coord_index;
	  Well_Coord_t Valid_Coord_Buf[Max_Coord_Buf];
	};
Note that sizeof(Well_Coord_t)=4, sizeof(I_tracker_type)=408
*/
	public static final int Max_Coord_Buf = 10;
	public static final int SZ_I_tracker_type = (4*Integer.SIZE+Max_Coord_Buf*Integer.SIZE+5*Integer.SIZE) / Byte.SIZE;
	/*20140731 added by michael
	 * size of struct FW_Header */
	public static final int SZ_I_track_fw_header = (Integer.SIZE + 48 * Byte.SIZE + Integer.SIZE + Integer.SIZE + 16 * Byte.SIZE) / Byte.SIZE;
	public static final int SZ_OD_monitor_data_type = Integer.SIZE * 10 / Byte.SIZE;
	/* #define PAGE_SIZE 256 */
	public static final int PAGE_SIZE = 256;
	// #define HID_CMD_SIGNATURE 0x43444948
	public static final int HID_CMD_SIGNATURE = 0x43444948;
	public static final int MAX_PAYLOAD = 4096;
	public static final int Well_96 = 1;
	public static final int Well_384 = 0;
	public int Well_Plate_Mode = 0;
	public int coord_index = 0;
	public int Coord_X = 0;
	public int Coord_Y = 0;
	public int Coord_X_Count = 0;
	public int Coord_Y_Count = 0;
	/*public static final int Coord_X_Mask = 0x1f;
	public static final int Coord_Y_Mask = 0x3E0;
	public static final int Coord_X_Count_Mask = 0x3C00;
	public static final int Coord_Y_Count_Mask = 0x3C000;*/
	public static final int Coord_X_Mask = 0x1f;
	public static final int Coord_X_Count_Mask = 0x0f;
	public static final int Coord_Y_shift = 5;
	public static final int Coord_X_Count_shift = 10;
	public static final int Coord_Y_Count_shift = 14;
	/*20131210 added by michael
	 * buffer_locked represent if Valid_Coord_Buf be locked*/
	public int buffer_locked = 0;
	
	public final class CMD_T {
		public final String Tag = "Command";
		/*typedef struct {
		    unsigned char cmd;
		    unsigned char len;
		    unsigned int arg1;
		    unsigned int arg2;
			unsigned int signature;
		    unsigned int checksum;
		}CMD_T;*/
	    public byte cmd;
	    public byte len;
	    //public final static byte[] padding = new byte[2];
	    public int  arg1;
	    public int  arg2;
	    public int Signature;
	    public int Checksum;
	    public static final int SZ_CMD_T = (2*Byte.SIZE+4*Integer.SIZE) / Byte.SIZE;
	    private final ByteBuffer mMessageBuffer;
	    private final ByteBuffer mDataBuffer;
	    
	    public CMD_T() {
	        mMessageBuffer = ByteBuffer.allocate(SZ_CMD_T);
	        mDataBuffer = ByteBuffer.allocate(MAX_PAYLOAD);
	        mMessageBuffer.order(ByteOrder.LITTLE_ENDIAN);
	        mDataBuffer.order(ByteOrder.LITTLE_ENDIAN);
	    }

	  //20120830 added by michael
	    /*#define HID_CMD_IS_ITRACKER_CONN 0x81
	    #define HID_CMD_ITRACKER_START 0x82
	    #define HID_CMD_ITRACKER_STOP 0x83
	    #define HID_CMD_ITRACKER_DATA 0x85
	    #define HID_CMD_ITRACKER_SETTING 0x86*/
	    public static final int HID_CMD_IS_ITRACKER_CONN = 0x81;
	    public static final int HID_CMD_ITRACKER_START = 0x82;
	    public static final int HID_CMD_ITRACKER_STOP = 0x83;
	    public static final int HID_CMD_ITRACKER_DATA = 0x85;
	    public static final int HID_CMD_ITRACKER_SETTING = 0x86;
	    /*20140731 added by michael
	     * app should query firmware header to retrieve version information
	     * */
	    public static final int HID_CMD_ITRACKER_FW_HEADER = 0xA1;
	    /*20140806 added by michael
	     * transfer FW bin data to device
	     * */
	    public static final int HID_CMD_ITRACKER_FW_UPGRADE = 0xA0;
	    public static final int HID_CMD_ODMONITOR_REQUEST_RAW_DATA = 0xC0;
	    public static final int HID_CMD_ODMONITOR_GET_RAW_DATA = 0xC1;
	    public static final int HID_CMD_ODMONITOR_HELLO = 0xC2;
	    
	    private boolean send_request(UsbRequest request, ByteBuffer byte_buf) {
	    	boolean queue_result; 
	    	show_debug(Tag+"The line number is " + new Exception().getStackTrace()[0].getLineNumber()+"\n");
	    	request.setClientData(this);
	    	queue_result = request.queue(byte_buf, byte_buf.limit());
			
	    	if (queue_result==true) {
			request = mDeviceConnection.requestWait();
			CMD_T message = (CMD_T)request.getClientData();
			if (this==message)
				return true;
	    	}
			show_debug(Tag+"The line number is " + new Exception().getStackTrace()[0].getLineNumber());
			return false;
	    }
	    
	    private boolean write_out(ByteBuffer byte_buf, int length) {
	    	boolean result = false;
	    	//byte[] write_buf;
	    	//int byte_count = 0;
	    	show_debug(Tag+"The line number is " + new Exception().getStackTrace()[0].getLineNumber()+"\n");
	    	//write_buf = byte_buf.array();
	    	//byte_count = mDeviceConnection.bulkTransfer(mEndpointOut, write_buf, length, 10000);
	    	//return result;
	    	UsbRequest request = getOutRequest();
	    	if (request != null) {
	    		result =  send_request(request, byte_buf);
	    		mOutRequestPool.add(request);
	    	}
	    	else
	    		result = false;
	    	return result;
	    	/*show_debug(Tag+"The line number is " + new Exception().getStackTrace()[0].getLineNumber()+"\n");
	    	request.setClientData(this);
			request.queue(byte_buf, byte_buf.limit());
			
			request = mDeviceConnection.requestWait();
			CMD_T message = (CMD_T)request.getClientData();
			if (this==message)
				return true;
			show_debug(Tag+"The line number is " + new Exception().getStackTrace()[0].getLineNumber());
			return false;*/
	    }
	    
	    private boolean read_in(ByteBuffer byte_buf, int length) {
	    	boolean result = false;
	    	byte[] read_buf;
	    	int byte_count = 0;
	    	show_debug(Tag+"The line number is " + new Exception().getStackTrace()[0].getLineNumber()+"\n");
	    	read_buf = byte_buf.array();
	    	//Log.d("knight", "buffer length: " + Integer.toString(read_buf.length));
	    	byte_count = mDeviceConnection.bulkTransfer(mEndpointIn, read_buf, length, 0);
	    	//Log.d("knight", "receive bytes " + Integer.toString(byte_count));
	    	if (byte_count != length || length==0) {
	    		Log.d("knight", "receive bytes " + Integer.toString(byte_count));
	    	    return false;
	    	}
	    	else
	    		return true;
	    	/*UsbRequest request = getInRequest();
	    	if (request != null) {
	    		result = send_request(request, byte_buf);
	    		mInRequestPool.add(request);
	    	}
	    	else
	    		result = false;
	    	return result;*/
	    	/*show_debug(Tag+"The line number is " + new Exception().getStackTrace()[0].getLineNumber()+"\n");
	    	request.setClientData(this);
	    	request.queue(byte_buf, byte_buf.limit());

			request = mDeviceConnection.requestWait();
			CMD_T message = (CMD_T)request.getClientData();
			if (this==message)
				return true;
			show_debug(Tag+"The line number is " + new Exception().getStackTrace()[0].getLineNumber());
			return false;*/
	    }
	    //process a a complete command transaction
	    //write command and data to device & read command and data from device
	    public boolean process_command(int debug) {
	    	//Out a transaction from host to device
	    	boolean result;
	    	byte [] byte_buf;

	    	result = write_out(mMessageBuffer, mMessageBuffer.limit());
	    	//if (result)
				//Log.d(Tag, "write command complete");
/*			if (debug != 0) {
				byte_buf = mMessageBuffer.array();
				stream_debug(byte_buf);
			}*/
			
			/*mDataBuffer.clear();
			mDataBuffer.limit((arg2-arg1)*PAGE_SIZE);*/
			//Log.d("knight", "Datat buffer capacity: " + Integer.toString(mDataBuffer.capacity()));
			int command;
			command = (int) cmd&0xff;
			if (command != CMD_T.HID_CMD_ITRACKER_FW_UPGRADE) {
				mDataBuffer.clear();
				mDataBuffer.limit((arg2-arg1)*PAGE_SIZE);
			}
			//In a transaction from host to device¡AOUT
			switch(command) {
			case HID_CMD_ITRACKER_SETTING:
				mDataBuffer.putInt(Well_Plate_Mode);
				/*20140306 added by michael
				 * pipetting detection  mode & detection sensitivity level */
				//mDataBuffer.putInt(Pipetting_Mode);
				//mDataBuffer.putInt(Pipetting_Sensitivity_Level);
				if (result)
					result = write_out(mDataBuffer, mDataBuffer.limit());
		    	show_debug(Tag+"The line number is " + new Exception().getStackTrace()[0].getLineNumber()+"\n");
		    	if (result)
					Log.d(Tag, "write data complete");
				break;
			case HID_CMD_ITRACKER_FW_UPGRADE:
				if (result)
					result = write_out(mDataBuffer, mDataBuffer.limit());
		    	/*show_debug(Tag+"The line number is " + new Exception().getStackTrace()[0].getLineNumber()+"\n");
		    	if (result)
					Log.d(Tag, "write firmware bin data complete");*/
				break;
			}
			
/*			if (debug != 0) {
				byte_buf = mDataBuffer.array();
				stream_debug(byte_buf);
			}*/	    	
			//In a transaction from device to host¡AIN
			switch(command) {
			case HID_CMD_ITRACKER_DATA:
				if (result)
					result = read_in(mDataBuffer, mDataBuffer.limit());
				show_debug(Tag+"The line number is " + new Exception().getStackTrace()[0].getLineNumber()+"\n");
		    	//if (result)
					//Log.d(Tag, "write data complete");
				break;
			
			/*20140731 added by michael*/
	        case HID_CMD_ITRACKER_FW_HEADER:
	        	if (result)
	        		result = read_in(mDataBuffer, mDataBuffer.limit());
	        	break;
	        	
	        case HID_CMD_ODMONITOR_GET_RAW_DATA:
	        	if (result)
	        		result = read_in(mDataBuffer, mDataBuffer.limit());
	        	break;
			}			
			return result;
	    }
	    
		// sets the fields in the command header
		public void set(int command, int argu0, int argu1, byte[] data, int debug) {
			int remainder,index;
			cmd = (byte) command;

			switch (command) {
			case HID_CMD_ITRACKER_SETTING:
				/*		case HID_CMD_ITRACKER_SETTING:
				if (debug)
					printf(">>> Setting up I-tracker preference\n");
				cmd.cmd = HID_CMD_ITRACKER_SETTING;
				cmd.arg1 = 0;
				if (sizeof(I_tracker_setting_type) % PAGE_SIZE)
					cmd.arg2 = (sizeof(I_tracker_setting_type) / PAGE_SIZE) + 1;
				else
					cmd.arg2 = (sizeof(I_tracker_setting_type) / PAGE_SIZE);
				break;*/
				if (debug != 0)
					Log.d(Tag, ">>> Setting up I-tracker preference\n");
				arg1 = 0;
				remainder = SZ_I_tracker_setting_type % PAGE_SIZE;
				if (remainder != 0)
					arg2 = (SZ_I_tracker_setting_type / PAGE_SIZE) + 1;
				else
					arg2 = (SZ_I_tracker_setting_type / PAGE_SIZE);
				break;

				  /*case HID_CMD_ITRACKER_START:
			     if (debug)
			       printf(">>> Starting I-tracker scanning phase\n");
				 cmd.cmd = HID_CMD_ITRACKER_START;
	             cmd.arg1 = 0;
	             cmd.arg2 = 0;
				 break;*/
			case HID_CMD_ITRACKER_START:
				if (debug != 0)
					Log.d(Tag, ">>> Starting I-tracker scanning phase\n");
				arg1 = 0;
				arg2 = 0;
				break;

			case HID_CMD_ITRACKER_STOP:
				arg1 = 0;
				arg2 = 0;
				break;

			case HID_CMD_ITRACKER_DATA:
/*			case HID_CMD_ITRACKER_DATA:
				if (debug)
					printf(">>> Retrieve I-tracker valid coordinate data\n");
				cmd.cmd = HID_CMD_ITRACKER_DATA;
				cmd.arg1 = 0;
				if (sizeof(I_tracker_type) % PAGE_SIZE)
					cmd.arg2 = (sizeof(I_tracker_type) / PAGE_SIZE) + 1;
				else
					cmd.arg2 = (sizeof(I_tracker_type) / PAGE_SIZE);
				break;
*/
				if (debug != 0)
					Log.d(Tag, ">>> Retrieve I-tracker valid coordinate data\n");
				arg1 = 0;
				remainder = SZ_I_tracker_type % PAGE_SIZE;
				if (remainder != 0)
					arg2 = (SZ_I_tracker_type / PAGE_SIZE) + 1;
				else
					arg2 = (SZ_I_tracker_type / PAGE_SIZE);
				break;
				
			case HID_CMD_ITRACKER_FW_HEADER:
				if (debug != 0)
					Log.d(Tag, ">>> Retrieve i-track running firmware header\n");
				arg1 = 0;
				remainder = SZ_I_track_fw_header % PAGE_SIZE;
				if (remainder != 0)
					arg2 = (SZ_I_track_fw_header / PAGE_SIZE) + 1;
				else
					arg2 = (SZ_I_track_fw_header / PAGE_SIZE);
				break;
				
			case HID_CMD_ITRACKER_FW_UPGRADE:
				arg1 = argu0;
				arg2 = argu1;
				mDataBuffer.clear();
				mDataBuffer.limit(arg2*PAGE_SIZE);
				mDataBuffer.put(data, 0, arg2*PAGE_SIZE);
				break;
			
			case HID_CMD_ODMONITOR_GET_RAW_DATA:
				if (debug != 0)
					Log.d(Tag, ">>> Retrieve OD monitor data\n");
				arg1 = 0;
				remainder = ( 4 * SZ_OD_monitor_data_type ) % PAGE_SIZE;
				if (remainder != 0)
					arg2 = ( ( 4 * SZ_OD_monitor_data_type ) / PAGE_SIZE) + 1;
				else
					arg2 = ( ( 4 * SZ_OD_monitor_data_type ) / PAGE_SIZE);
				break;
			}
			len = CMD_T.SZ_CMD_T - 4; /* Not include checksum */
			Signature = HID_CMD_SIGNATURE;
			
			mMessageBuffer.clear();
			mMessageBuffer.put(cmd);
			mMessageBuffer.put(len);
			mMessageBuffer.putInt(arg1);
			mMessageBuffer.putInt(arg2);
			mMessageBuffer.putInt(Signature);
			
			byte [] byte_buf = mMessageBuffer.array();
			Checksum = genCheckSum(byte_buf, 0, byte_buf.length-4);
			mMessageBuffer.putInt(Checksum);
			//debug the CMD_T packet stream
/*			if (debug != 0) {
				byte_buf = mMessageBuffer.array();
				stream_debug(byte_buf);
			}*/
		}

//CMD stream buffer
	    //ByteArrayOutputStream buffer;
	    //DataOutputStream dos;
	    private int genCheckSum(byte [] buf, int start_index, int end_index) {
	    	int i = 0, sum;
/*	    	Checksum = 0;
	    	buffer = new ByteArrayOutputStream(SZ_CMD_T-4);
	    	dos = new DataOutputStream(buffer);
	    	
	    	dos.writeByte(cmd);
	    	dos.writeByte(len);
	    	dos.writeInt(Integer.reverseBytes(arg1));
	    	dos.writeInt(Integer.reverseBytes(arg2));
	    	dos.writeInt(Integer.reverseBytes(Signature));
	    	
	    	byte [] buf = buffer.toByteArray();

//Note that signed bits mask
	    	for (i = 0; i < buf.length; i++)
	    	   Checksum += (int) (buf[i] & 0xff);
	    	dos.writeInt(Integer.reverseBytes(Checksum));*/
	    	
            for (sum = 0, i = start_index; i < end_index; i++)
            	sum += (int) (buf[i] & 0xff);
            return sum;
	    }
	    
	    public void stream_debug(byte [] buf) {
	    	stream_debug(buf, 0, buf.length);
	    }

	    public void stream_debug(byte [] buf, int byteOffset, int byteCount) {
			FileOutputStream debug_out;
			try {
				debug_out = new FileOutputStream("/mnt/sdcard/output.txt");
				debug_out.write(buf, byteOffset, byteCount);
				debug_out.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }

	}

/*update the connection status for the device*/
//return true if success build a connection on device
//re-new mDeviceConnection, mDevice, mInterface, mEndpointOut, mEndpointIn
	private boolean setInterface(UsbDevice device, UsbInterface intf) {
		int i = 0;

		show_debug(Tag+"The line number is " + new Exception().getStackTrace()[0].getLineNumber());
        if (mDeviceConnection != null) {
            if (mInterface != null) {
                mDeviceConnection.releaseInterface(mInterface);
                mInterface = null;
            }
            mDeviceConnection.close();
            mDevice = null;
            mDeviceConnection = null;
        }
        mInterface = intf;
        mDevice = device;

        show_debug(Tag+"The line number is " + new Exception().getStackTrace()[0].getLineNumber());
		if (device==null) {
			Log.d(Tag, "device not found.");
			return false;
		}
		
		show_debug(Tag+"The line number is " + new Exception().getStackTrace()[0].getLineNumber());
		if (intf==null) {
			Log.d(Tag, "interface not found.");
			return false;			
		}
		
		//mDeviceConnection
		UsbDeviceConnection connection = mManager.openDevice(device);
        if (connection==null) {
    	  show_debug(Tag+"The line number is " + new Exception().getStackTrace()[0].getLineNumber()+"\n");
          Log.d(Tag, "open connection failed");
          return false;
        }
        else {
			if (connection.claimInterface(intf, true)) {
				show_debug(Tag+"The line number is " + new Exception().getStackTrace()[0].getLineNumber());
				Log.d(Tag, "connection interface success");
				mDevice = device;
				mDeviceConnection = connection;
				mInterface = intf;
				
				for (i = 0; i < mInterface.getEndpointCount(); i++) {
					UsbEndpoint ep = mInterface.getEndpoint(i);
					if (ep.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
						if (ep.getDirection() == UsbConstants.USB_DIR_OUT) {
					    	show_debug(Tag+"The line number is " + new Exception().getStackTrace()[0].getLineNumber());
							mEndpointOut = ep;
						} else {
							show_debug(Tag+"The line number is " + new Exception().getStackTrace()[0].getLineNumber());
							mEndpointIn = ep;
						}
					}
				}
			}
			else {
				show_debug(Tag+"The line number is " + new Exception().getStackTrace()[0].getLineNumber());
                Log.d(Tag, "claim interface failed");
                connection.close();
                return false;
			}
        }

        return true;
	}


}
