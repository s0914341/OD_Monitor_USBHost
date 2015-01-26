package ODMonitor.App;

import java.util.ArrayList;

import ODMonitor.App.data.chart_display_data;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ftdi.j2xx.D2xxManager;
import com.ftdi.j2xx.FT_Device;


public class DeviceUART {

	// original ///////////////////////////////
	static Context DeviceUARTContext;
	D2xxManager ftdid2xx;
	FT_Device ftDev = null;
	int DevCount = -1;
    int currentIndex = -1;
    int openIndex = 0;
	
    /*graphical objects*/
    TextView readText;

    static int iEnableReadFlag = 1;

    /*local variables*/
    public int baudRate; /*baud rate*/
    public byte stopBit; /*1:1stop bits, 2:2 stop bits*/
    public byte dataBit; /*8:8bit, 7: 7bit*/
    public byte parity;  /* 0: none, 1: odd, 2: even, 3: mark, 4: space*/
    public byte flowControl; /*0:none, 1: flow control(CTS,RTS)*/
    public int portNumber; /*port number*/
    ArrayList<CharSequence> portNumberList;

    public static final int readLength = 512;
    public int readcount = 0;
    public int iavailable = 0;
    byte[] readData;
    char[] readDataToText;
    public boolean bReadThreadGoing = false;
    public readThread read_thread;

    boolean uart_configured = false;

	/* Constructor */
	public DeviceUART(Context parentContext , D2xxManager ftdid2xxContext, TextView read_text)
	{
		DeviceUARTContext = parentContext;
		ftdid2xx = ftdid2xxContext;
		readText = read_text;
		
		readData = new byte[readLength];
		readDataToText = new char[readLength];
		
		/* by default it is 9600 */
		baudRate = 38400;
		/* default is stop bit 1 */
		stopBit = 1;
		/* default data bit is 8 bit */
		dataBit = 8;
		/* default is none */
		parity = 0;
		/* default flow control is is none */
		flowControl = 0;
		portNumber = 1; 	
	}

	public void notifyUSBDeviceAttach() {
		createDeviceList();
	}
	
	public void notifyUSBDeviceDetach() {
		disconnectFunction();
	}	

	public int createDeviceList() {
		int tempDevCount = ftdid2xx.createDeviceInfoList(DeviceUARTContext);
		
		if (tempDevCount > 0) {
			if( DevCount != tempDevCount ) {
				DevCount = tempDevCount;
			}
		} else {
			DevCount = -1;
			currentIndex = -1;
		}
		
		Toast.makeText(DeviceUARTContext, "DevCount:"+DevCount, Toast.LENGTH_SHORT).show();
		return DevCount;
	}
	
	public void disconnectFunction() {
		DevCount = -1;
		currentIndex = -1;
		bReadThreadGoing = false;
		
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		if (ftDev != null) {
			synchronized(ftDev) {
				if( true == ftDev.isOpen()) {
					ftDev.close();
				}
			}
		}
	}
	
	public int connectFunction(int port_num) {
		int tmpProtNumber = openIndex + 1;
        int ret = -1;
		
        openIndex = port_num;
		if (currentIndex != openIndex ) {
			if(null == ftDev) {
				ftDev = ftdid2xx.openByIndex(DeviceUARTContext, openIndex);
			} else {
				synchronized(ftDev) {
					ftDev = ftdid2xx.openByIndex(DeviceUARTContext, openIndex);
				}
			}
			uart_configured = false;
		} else {
			Toast.makeText(DeviceUARTContext,"Device port " + tmpProtNumber + " is already opened",Toast.LENGTH_SHORT).show();
			ret = 1;
			return ret;
		}

		if (ftDev == null) {
			Toast.makeText(DeviceUARTContext,"open device port("+tmpProtNumber+") NG, ftDev == null", Toast.LENGTH_SHORT).show();
			return ret;
		}
			
		if (true == ftDev.isOpen()) {
			currentIndex = openIndex;
			Toast.makeText(DeviceUARTContext, "open device port(" + tmpProtNumber + ") OK", Toast.LENGTH_SHORT).show();
				
			if (false == bReadThreadGoing) {
				read_thread = new readThread(handler);
				read_thread.start();
				bReadThreadGoing = true;
			}
			
			ret = 0;
		} else {			
			Toast.makeText(DeviceUARTContext, "open device port(" + tmpProtNumber + ") NG", Toast.LENGTH_SHORT).show();
			//Toast.makeText(DeviceUARTContext, "Need to get permission!", Toast.LENGTH_SHORT).show();			
		}
		
		return ret;
	}

 	public void SetConfig(int baud, byte dataBits, byte stopBits, byte parity, byte flowControl) {
		if (ftDev.isOpen() == false) {
			Log.e("j2xx", "SetConfig: device not open");
			return;
		}

		// configure our port
		// reset to UART mode for 232 devices
		ftDev.setBitMode((byte) 0, D2xxManager.FT_BITMODE_RESET);
		ftDev.setBaudRate(baud);

		switch (dataBits) {
		    case 7:
			    dataBits = D2xxManager.FT_DATA_BITS_7;
			break;
			
		    case 8:
			    dataBits = D2xxManager.FT_DATA_BITS_8;
			break;
			
		    default:
			    dataBits = D2xxManager.FT_DATA_BITS_8;
			break;
		}

		switch (stopBits) {
		    case 1:
			    stopBits = D2xxManager.FT_STOP_BITS_1;
			break;
			
		    case 2:
			    stopBits = D2xxManager.FT_STOP_BITS_2;
			break;
			
		    default:
			    stopBits = D2xxManager.FT_STOP_BITS_1;
			break;
		}

		switch (parity) {
		    case 0:
			    parity = D2xxManager.FT_PARITY_NONE;
			break;
			
		    case 1:
			    parity = D2xxManager.FT_PARITY_ODD;
			break;
			
		    case 2:
			    parity = D2xxManager.FT_PARITY_EVEN;
			break;
			
		    case 3:
			    parity = D2xxManager.FT_PARITY_MARK;
			break;
			
		    case 4:
			    parity = D2xxManager.FT_PARITY_SPACE;
			break;
			
		    default:
			    parity = D2xxManager.FT_PARITY_NONE;
			break;
		}

		ftDev.setDataCharacteristics(dataBits, stopBits, parity);

		short flowCtrlSetting;
		switch (flowControl) {
		case 0:
			flowCtrlSetting = D2xxManager.FT_FLOW_NONE;
			break;
		case 1:
			flowCtrlSetting = D2xxManager.FT_FLOW_RTS_CTS;
			break;
		case 2:
			flowCtrlSetting = D2xxManager.FT_FLOW_DTR_DSR;
			break;
		case 3:
			flowCtrlSetting = D2xxManager.FT_FLOW_XON_XOFF;
			break;
		default:
			flowCtrlSetting = D2xxManager.FT_FLOW_NONE;
			break;
		}

		// TODO : flow ctrl: XOFF/XOM
		// TODO : flow ctrl: XOFF/XOM
		ftDev.setFlowControl(flowCtrlSetting, (byte) 0x0b, (byte) 0x0d);

		uart_configured = true;
		Toast.makeText(DeviceUARTContext, "Config done", Toast.LENGTH_SHORT).show();
	}

    public void EnableRead (){    	
    	iEnableReadFlag = (iEnableReadFlag + 1)%2;
    	    	
		if(iEnableReadFlag == 1) {
			ftDev.purge((byte) (D2xxManager.FT_PURGE_TX));
			ftDev.restartInTask();
		} else {
			ftDev.stopInTask();
		}
    }

    public int SendMessage(String writeData, char[] readDataChar) {
    	int ret = 0;
    	String read_string = new String("");
    	
		if (ftDev.isOpen() == false) {
			Log.e("j2xx", "SendMessage: device not open");
			ret = -1;
			return ret;
		}
		
		ftDev.setLatencyTimer((byte) 16);
//		ftDev.purge((byte) (D2xxManager.FT_PURGE_TX | D2xxManager.FT_PURGE_RX));
		
		synchronized(ftDev) {
		    iavailable = 0;
		}
		
		byte[] OutData = writeData.getBytes();
		ftDev.write(OutData, writeData.length());
		
		try {
			int  i = 10;
			while ((i--) > 0)
			    Thread.sleep(10);
		} catch (InterruptedException e) {
		}
		
		synchronized(ftDev) {
            if (iavailable > 0) {
            	if (iavailable > readDataChar.length)
            		iavailable = readDataChar.length;
            	
            	for (int i = 0; i < iavailable; i++) {
            		readDataChar[i] = readDataToText[i];
				}
            	ret = iavailable;
            	iavailable = 0;
			} else {
				Toast.makeText(DeviceUARTContext, "no data receive", Toast.LENGTH_SHORT).show();
			}
		}
		
	    return ret;
    }

	final Handler handler =  new Handler() {
    	@Override
    	public void handleMessage(Message msg) {
    		int readDataAvailableLength = msg.getData().getInt("read_data_available_length");
    		if(readDataAvailableLength > 0) {
    			char[] readDataChar = msg.getData().getCharArray("read_data_char_array");
    			readText.setText(String.copyValueOf(readDataChar, 0, readDataAvailableLength));
    		}
    	}
    };

	private class readThread  extends Thread {
		Handler mHandler;

		readThread(Handler h) {
			mHandler = h;
			this.setPriority(Thread.MIN_PRIORITY);
		}

		@Override
		public void run() {
			int read_length = 0;
         
			while(true == bReadThreadGoing) {
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
				}

				synchronized(ftDev) {
					read_length = ftDev.getQueueStatus();				
					if (read_length > 0) {
						if((read_length+iavailable) > readLength){
							read_length = (readLength-iavailable);
						}
						
						ftDev.read(readData, read_length);
						char[] read_data_put = new char[read_length];
						for (int i = 0; i < read_length; i++) {
							readDataToText[i+iavailable] = (char) readData[i];
							read_data_put[i] = (char) readData[i];
						}
						
						iavailable += read_length;
		
						Bundle b = new Bundle(1);
					    b.putCharArray("read_data_char_array", read_data_put);
					    b.putInt("read_data_available_length", iavailable);
					    Message msg = mHandler.obtainMessage();
				        msg.setData(b);
					    mHandler.sendMessage(msg);
					}
				}
			}
		}
	}
	
	public int GetDeviceInformation(int device_type) throws InterruptedException {
		int devCount = 0;
		int device_num = -1;

		devCount = ftdid2xx.createDeviceInfoList(DeviceUARTContext);

		Log.i("FtdiModeControl",
				"Device number = " + Integer.toString(devCount));
		if (devCount > 0) {
			D2xxManager.FtDeviceInfoListNode[] deviceList = new D2xxManager.FtDeviceInfoListNode[devCount];
			ftdid2xx.getDeviceInfoList(devCount, deviceList);
			
			// deviceList[0] = ftdid2xx.getDeviceInfoListDetail(0);
			
			// display the chip type for the first device
			for (int i = 0; i < devCount; i++) {
				if (deviceList[i].type == device_type) {
					device_num = i;
					break;
				}
			}
		} else {
		}

		return device_num;
	}
}

