package od_monitor.experiment;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import od_monitor.app.ODMonitorApplication;
import od_monitor.app.data.ExperimentScriptData;
import od_monitor.app.data.SensorDataComposition;
import od_monitor.app.file.FileOperateByteArray;
import od_monitor.experiment.ODMonitorSensor.CMD_T;

import android.content.Context;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.ftdi.j2xx.D2xxManager;
import com.ftdi.j2xx.FT_Device;

public class ExperimentalOperationInstruct {
	public static String Tag = "ExperimentalOperationInstruct";
	public static Context ExperimentalOperationInstructContext;
	public D2xxManager ftdid2xx;
	
	public static final int EXPERIMENT_MAX_SENSOR_COUNT = 4;
	private long delay_start_system_time = 0;
	private long total_experiment_start_system_time = 0;
	private long step_experiment_start_system_time = 0;
	private static final int shaker_command_retry_count = 5;
	private static final int sensor_command_retry_count = 5;
	private static final long shaker_command_fail_retry_delay = 300;
	private static final long MAIL_ALERT_UNIT = 60000;
	//private static final long MAIL_ALERT_UNIT = 1000;
	
    /*graphical objects*/
    public TextView readText;
    public SensorDataComposition[] current_one_sensor_data = new SensorDataComposition[EXPERIMENT_MAX_SENSOR_COUNT];
    public double init_od = 0;
    public ODCalculate od_cal = new ODCalculate();
  
    public DeviceUART shaker;
    public int shaker_port_num = -1;
    public int sensor_data_index = 0;
    public final static char[] shaker_id = new char[] {'I', 'D', ' '};
    public final static char[] shaker_on = new char[] {'O', 'N', ' '};
    public final static char[] shaker_off = new char[] {'O', 'F', ' '};
    public final static char[] shaker_speed = new char[] {'S', 'S', '0'};
    public final static char[] shaker_temperature = new char[] {'S', 'C'};
    public final static char[] shaker_max_time = new char[] {'S', 'T', '0', '5', '9', '4', '0', ' '};
    public final static char[] shaker_end = new char[] {'0', ' '};
    
    List<repeat_informat> list_repeat_count = new ArrayList<repeat_informat>();
    List<repeat_informat> list_repeat_time = new ArrayList<repeat_informat>();

    /*20150121 added by michael
     * sensor instance */
    public ODMonitorSensor mODMonitorSensor;
    private mutil_sensor_raw_data_composition sensor_raw;
    public class experiment_mail_alert {
    	public final static int ALERT_OD_VALUE_COUNT_THRESHOLD = 1;
    	
    	public boolean enable_mail_alert_interval = false;
    	public boolean enable_mail_alert_od_value = false;
    	public long mail_alert_interval = 0;
    	public long mail_alert_start_time = 0;
    	public double mail_alert_od_value = 0;
    	public int mail_alert_od_value_count = 0;
    	public boolean once_alert_od_value = true;
    	public boolean is_mail_alert_interval = false;
    	public boolean is_mail_alert_od_value = false;
    }
    
    experiment_mail_alert mail_alert = null;
    
	public class repeat_informat {
		public int repeat_instruct_index;
		public int repeat_instruct_from;
		public int repeat_count;
		public long repeat_time;
	};
	
	public class mutil_sensor_raw_data_composition {
		private static final int ONE_SENSOR_COMPOSITION_SIZE = 10;
		private static final int IS_ONLINE = 0;
		private static final int INDEX = 1;
		private int total_sensor_count = 0;
		public byte[] sensor_raw_buffer = null;
		public int[] raw_data = null;
		
		public mutil_sensor_raw_data_composition (int total_sensor) {
			total_sensor_count = total_sensor;
			sensor_raw_buffer = new byte[total_sensor_count*4*ONE_SENSOR_COMPOSITION_SIZE];
		}
		
		public void convert_raw_byte_to_int_buffer() {
			 IntBuffer raw_IntBuffer = ByteBuffer.wrap(sensor_raw_buffer).order(ByteOrder.LITTLE_ENDIAN).asIntBuffer();
		     raw_data = new int[raw_IntBuffer.remaining()];
		     raw_IntBuffer.get(raw_data);
		}
		
		public int get_is_online(int sensor_num) {
			return raw_data[IS_ONLINE + sensor_num*ONE_SENSOR_COMPOSITION_SIZE];
		}
		
		public int[] get_raw_data(int sensor_num) {
			int[] channel_data = new int[SensorDataComposition.raw_total_sensor_data_size];
			System.arraycopy(raw_data, INDEX + sensor_num*ONE_SENSOR_COMPOSITION_SIZE, channel_data, 0, SensorDataComposition.raw_total_sensor_data_size);
			
			return channel_data;
		}
	}
	
	public ExperimentalOperationInstruct(Context parentContext, D2xxManager ftdid2xxContext, TextView read_text) {
	    ExperimentalOperationInstructContext = parentContext;
	    ftdid2xx = ftdid2xxContext;
	    readText = read_text;
	    
	    shaker = new DeviceUART(ExperimentalOperationInstructContext, ftdid2xx, read_text);
	    shaker.baudRate = 38400;
	    shaker.dataBit = 8;
	    shaker.stopBit = 1;
	    shaker.parity = 0;
	    shaker.flowControl = 0; 
	    
        /*20150121 added by michael*/
        mODMonitorSensor = new ODMonitorSensor (parentContext);
        sensor_raw = new mutil_sensor_raw_data_composition(EXPERIMENT_MAX_SENSOR_COUNT);
	}
	
	public double get_init_od() {
		return init_od;
	}
	
	public String get_init_od_string() {
		return Double.toString(init_od);
	}
	
	public void set_init_od (double od) {
		init_od = od;
	}
	
	public void set_enable_mail_alert_interval(boolean en) {
		mail_alert.enable_mail_alert_interval = en;
	}
	
	public void set_enable_mail_alert_od_value(boolean en) {
		mail_alert.enable_mail_alert_od_value = en;
	}
	
	public boolean get_enable_mail_alert_interval() {
		return mail_alert.enable_mail_alert_interval;
	}
	
	public boolean get_enable_mail_alert_od_value() {
		return mail_alert.enable_mail_alert_od_value;
	}
	
	public int get_mail_alert_interval() {
		return (int)(mail_alert.mail_alert_interval/MAIL_ALERT_UNIT);
	}
	
	public void set_mail_alert_interval(int interval) {
		mail_alert.mail_alert_interval = (long)(interval*MAIL_ALERT_UNIT);
	}
	
	public void set_mail_alert_start_time(long start_time) {
		mail_alert.mail_alert_start_time = start_time;
	}
	
	public boolean is_mail_alert_interval() {
		if (false == mail_alert.enable_mail_alert_interval) {
			mail_alert.is_mail_alert_interval = false;
			return mail_alert.is_mail_alert_interval;
		}
		
		if (0 >= mail_alert.mail_alert_interval) {
			mail_alert.is_mail_alert_interval = false;
		} else {
		    if ((System.currentTimeMillis() - mail_alert.mail_alert_start_time) >= mail_alert.mail_alert_interval) {
		    	mail_alert.is_mail_alert_interval = true;
		    	mail_alert.mail_alert_start_time = System.currentTimeMillis();
            } else {
            	mail_alert.is_mail_alert_interval = false;
            }
		}
		
		return mail_alert.is_mail_alert_interval;
	}
	
	public long get_mail_alert_interval_countdown() {
		long countdown_time = 0;
		if (mail_alert.mail_alert_interval >= (System.currentTimeMillis() - mail_alert.mail_alert_start_time)) {
			countdown_time = mail_alert.mail_alert_interval - (System.currentTimeMillis() - mail_alert.mail_alert_start_time);
		}
		
		return countdown_time;
	}
	
	public void set_mail_alert_od_value(double od) {
		mail_alert.mail_alert_od_value = od;
	}
	
	public double get_mail_alert_od_value() {
		return mail_alert.mail_alert_od_value;
	}
	
	public void enable_once_mail_alert_od_value() {
        mail_alert.once_alert_od_value = true;
        mail_alert.is_mail_alert_od_value = false;
        mail_alert.mail_alert_od_value_count = 0;
	}
	
	public boolean is_mail_alert_od_value() {
		if (mail_alert.is_mail_alert_od_value) {
			mail_alert.is_mail_alert_od_value = false;
			return true;
		} else {
			return false;
		}
	}
	
	private void compare_alert_od_value(int sensor_num) {
		if (false == mail_alert.enable_mail_alert_od_value) {
			mail_alert.is_mail_alert_od_value = false;
			return;
		}
		
		if (mail_alert.once_alert_od_value) {
		    if (mail_alert.mail_alert_od_value <= current_one_sensor_data[sensor_num].get_sensor_od_value()) {
			    mail_alert.mail_alert_od_value_count++;
			    if (experiment_mail_alert.ALERT_OD_VALUE_COUNT_THRESHOLD <= mail_alert.mail_alert_od_value_count) {
				    mail_alert.is_mail_alert_od_value = true;
				    mail_alert.once_alert_od_value = false;
			    }	
		    } else {
			    mail_alert.is_mail_alert_od_value = false;
		    }
	    } else {
	    	mail_alert.is_mail_alert_od_value = false;
	    }
	}
	
	public int initial_experiment_devices() {
		int ret = 0;
		
		mail_alert = new experiment_mail_alert();
		sensor_data_index = 0;
		mail_alert.mail_alert_interval = 0;
		delay_start_system_time = 0;
		mail_alert.is_mail_alert_interval = false;
		total_experiment_start_system_time = System.currentTimeMillis();
		step_experiment_start_system_time = total_experiment_start_system_time;
		set_mail_alert_start_time(total_experiment_start_system_time);
		od_cal.initialize(init_od);
		if (!ODMonitorApplication.no_devices)
		    mODMonitorSensor.IOCTL( CMD_T.HID_CMD_ODMONITOR_HELLO, 0, 0, null, 1 );
		if (0 == open_shaker_port()) {
			for (int i = 0; i < EXPERIMENT_MAX_SENSOR_COUNT; i++) {
			    String file_name = SensorDataComposition.sensor_raw_file_name + (i+1);
				FileOperateByteArray write_file = new FileOperateByteArray(SensorDataComposition.sensor_raw_folder_name, file_name, true);
	    	    try {
	    		    write_file.delete_file(write_file.generate_filename_no_date());
			    } catch (IOException e) {
				    // TODO Auto-generated catch block
				    e.printStackTrace();
			    }	
			}
		} else {
			ret = -1;
		}
		   
		return ret;
	}
	
	public int check_shaker_port_number() {
		int ret = -1;
		int total_port_num = shaker.createDeviceList();
		char[] readDataChar = new char[20];
		int receive_length = 0;
		int try_count = 3;
		
		if (total_port_num <= 0) {
			ret = -2;
		} else {
			try {
			    shaker_port_num = shaker.GetDeviceInformation(D2xxManager.FT_DEVICE_232R);
		    } catch (InterruptedException e) {
			    // TODO Auto-generated catch block
			    e.printStackTrace();
		    }
		}
		
	/*	for (int i = 0; i < total_port_num; i++) {
			if (0 == shaker.connectFunction(i)) {
    		    shaker.SetConfig(shaker.baudRate, shaker.dataBit, shaker.stopBit, shaker.parity, shaker.flowControl);
    		    while ((try_count--) > 0) {
    		        if (0 < (receive_length = shaker.SendMessage("ID ", readDataChar))) {
    		    	    String read_string = String.copyValueOf(readDataChar, 0, receive_length);
    		    	    if (true == read_string.equals("ID CIs-iCRLF")) {
    		    	        shaker.disconnectFunction();
    		    	        shaker_port_num = i;
    		    	        ret = 0;
    		    	        try_count = 0;
    		    	    }
    		        } else {
    		        	try_count = 0;
    		        }
    		    }
    		    shaker.disconnectFunction();
    	    } else {
    		    Log.d(Tag, "shaker connect NG");
    	    } 		
		}*/
		
		if (shaker_port_num >= 0) {
			Toast.makeText(ExperimentalOperationInstructContext, "shaker port number:" + shaker_port_num, Toast.LENGTH_SHORT).show();
			ret = 0;
		} else {
			Toast.makeText(ExperimentalOperationInstructContext, "shaker port not be found", Toast.LENGTH_SHORT).show();
		}
		
		return ret;
	}
	
	
/*	public int open_shaker_port() {
		int ret = 0;
		
		if (0 == check_shaker_port_number()) {
		    if (0 < shaker.createDeviceList() && (shaker_port_num >= 0)) {
    	        if (0 == shaker.connectFunction(shaker_port_num)) {
    		        shaker.SetConfig(shaker.baudRate, shaker.dataBit, shaker.stopBit, shaker.parity, shaker.flowControl);
    	        } else {
    	    	    ret = -3;
    		        Log.d(Tag, "shaker connect NG");
    	        }
		    } else {
			    ret = -2;
    	        Log.d(Tag, "no device on list");
		    }
	    } else {
	    	ret = -1;
	        Log.d(Tag, "no shaker port in device list");
	    }
		
		return ret;
	}*/
	
	public int open_shaker_port() {
		int ret = 0;
		
		if (ODMonitorApplication.no_devices)
			return ret;
		
		//shaker.Enumeration();
        if (0 == shaker.connectFunction(shaker.getDevice())) {
    		shaker.SetConfig(shaker.baudRate, shaker.dataBit, shaker.stopBit, shaker.parity, shaker.flowControl);
    	} else {
    	    ret = -2;
    		Log.d(Tag, "shaker connect NG");
    	}
	
		return ret;
	}
	
	public int close_shaker_port() {
		int ret = 0;
		
		shaker.disconnectFunction();
		return ret;
	}
	
	public String get_current_one_sensor_data_string(int sensor_num) {
		return current_one_sensor_data[sensor_num].get_sensor_data_string();
	}
	
	public SensorDataComposition[] get_current_one_sensor_data() {
		return current_one_sensor_data;
	}
	
	public int save_sensor_data_to_file(int index, long time, int[] raw_data, String file_name, int sensor_num, boolean data_valid) {
		int ret = 0;
		double od_value = 0;
		
        FileOperateByteArray write_file = new FileOperateByteArray(SensorDataComposition.sensor_raw_folder_name, file_name, true);
    	try {
            write_file.create_file(write_file.generate_filename_no_date());  
            if (raw_data != null) {
                if (raw_data.length == SensorDataComposition.raw_total_sensor_data_size) {
    		    	current_one_sensor_data[sensor_num] = new SensorDataComposition();
    		    	current_one_sensor_data[sensor_num].set_data_valid(data_valid);
    		    	current_one_sensor_data[sensor_num].set_sensor_get_index(index);
    		    	// write this sensor data time to file
    		    	current_one_sensor_data[sensor_num].set_sensor_measurement_time(time);
    		    	if (data_valid) {
    		    	    current_one_sensor_data[sensor_num].set_raw_sensor_data(raw_data);
    		    	    if (ODMonitorApplication.no_devices) {
    		    	        od_value = od_cal.calculate_od(current_one_sensor_data[sensor_num].get_channel_data())+(double)(sensor_num+1)*Math.random(); 
    		    	    } else {
    		    		    od_value = od_cal.calculate_od(current_one_sensor_data[sensor_num].get_channel_data());
    		    	    }
    		    	    current_one_sensor_data[sensor_num].set_sensor_od_value(od_value);
    		    	}
    		    	write_file.write_file(current_one_sensor_data[sensor_num].buffer);
    		    } else {
    		    	ret = -3;
    		    	Log.e(Tag, "raw data length is not match");
    		    }
    		} else {
    			ret = -2;
    		}
    		
    		write_file.flush_close_file();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			ret = -1;
		}
    	
    	return ret;
	}
	
	public int read_sensor_instruct(ExperimentScriptData current_instruct_data) {
		int ret = -1;
		char[] readDataChar = new char[128];
		int receive_length = 0;
		int try_count = sensor_command_retry_count;
		
		if (ODMonitorApplication.no_devices) {
			boolean[] is_online = {true, false, false, false};
			int[] raw_data_save = {597, 704, 702, 698, 698, 694, 694, 692, 693};
			
			long current_system_time = System.currentTimeMillis();
			for (int sensor_num = 0; sensor_num < EXPERIMENT_MAX_SENSOR_COUNT; sensor_num++) {
			    String file_name = SensorDataComposition.sensor_raw_file_name + (sensor_num+1);
			    if (0 == save_sensor_data_to_file(sensor_data_index, current_system_time, raw_data_save, file_name, sensor_num, is_online[sensor_num])) {
			    	if (is_online[sensor_num])
			    	    compare_alert_od_value(sensor_num);
	                ret = 0;
	            }
			}
			
			sensor_data_index++;
			current_instruct_data.next_instruct_index++;	
			return ret;
		}
		
		if (mODMonitorSensor.isDeviceOnline()) {
	        mODMonitorSensor.IOCTL( CMD_T.HID_CMD_ODMONITOR_REQUEST_RAW_DATA, 0, 0, null, 1 );
	        try {
				Thread.sleep(22000);
			} catch (InterruptedException e) {
			}
	        
	        mODMonitorSensor.IOCTL( CMD_T.HID_CMD_ODMONITOR_GET_RAW_DATA, 0, 0, sensor_raw.sensor_raw_buffer, 0 );
	        sensor_raw.convert_raw_byte_to_int_buffer();
	       // Log.d(Tag, raw_data[0]+","+ raw_data[1]+","+ raw_data[2]+","+raw_data[3]+","+raw_data[4]+","+raw_data[5]+","+raw_data[6]+","+raw_data[7]+","+raw_data[8]+","+raw_data[9]);
	       // int[] raw_data_save = new int[SensorDataComposition.raw_total_sensor_data_size];
	        
	        
	        long current_system_time = System.currentTimeMillis();
			for (int sensor_num = 0; sensor_num < EXPERIMENT_MAX_SENSOR_COUNT; sensor_num++) {
				boolean data_valid = false;
				if (1 == sensor_raw.get_is_online(sensor_num)) {
					data_valid = true;
				}
			    String file_name = SensorDataComposition.sensor_raw_file_name + (sensor_num+1);
			    if (0 == save_sensor_data_to_file(sensor_data_index, current_system_time, sensor_raw.get_raw_data(sensor_num), file_name, sensor_num, data_valid)) {
            	    if (data_valid)
			    	    compare_alert_od_value(sensor_num);
	                ret = 0;
	            }
			}
	        	
	        sensor_data_index++;
			current_instruct_data.next_instruct_index++;
	    }
		
		/*String read_string = String.format("index: 597, 0704,  0702,  0698,  0698,  0694,  0694,  0692,  0693\r");
		if (0 == save_sensor_data_to_file(sensor_data_index, new Date().getTime(), read_string)) {
        	sensor_data_index++;
		    ret = 0;
		}
		current_instruct_data.next_instruct_index++;*/
		
		return ret;
	}
	
	public int send_shaker_command(char[] cmd) {
		int ret = -1;
		char[] readDataChar = new char[cmd.length+10];
		int receive_length = 0;
		
		if (ODMonitorApplication.no_devices) {
			return 0;
		}
		
        for (int i = 0; i < cmd.length; i++) {
            String send_cmd = String.copyValueOf(cmd, i, 1);
    		if (0 < (receive_length = shaker.SendMessage(send_cmd, readDataChar))) {
    		    String read_string = String.copyValueOf(readDataChar, 0, receive_length);
    		    if (read_string.equals(send_cmd)) {
    		        ret = 0;
    		    }
    		} else {
    		}
        }
		
		return ret;
	}
	
	public int shaker_max_time_instruct() {
		int ret = -1;
		int try_count = shaker_command_retry_count;
	
		while ((try_count--) > 0) {
		    if (0 == send_shaker_command(shaker_max_time)) {
			    ret = 0;
			    break;
		    } else {
		    	try {
			        Thread.sleep(shaker_command_fail_retry_delay);
				} catch (InterruptedException e) {
				}
		    }
		}
		
		return ret;
	}
	
	public int shaker_on_instruct(ExperimentScriptData current_instruct_data) {
		int ret = -1;
		int try_count = shaker_command_retry_count;
	
		while ((try_count--) > 0) {
		    if (0 == send_shaker_command(shaker_on)) {
			    ret = 0;
			    break;
		    } else {
		    	try {
			        Thread.sleep(shaker_command_fail_retry_delay);
				} catch (InterruptedException e) {
				}
		    }
		}
		
		if (0 == ret)
			current_instruct_data.next_instruct_index++;
		
		return ret;
	}
	
	public int shaker_off() {
		int ret = -1;
		int try_count = shaker_command_retry_count;
		
		while ((try_count--) > 0) {
		    if (0 == send_shaker_command(shaker_off)) {
			    ret = 0;
			    break;
		    } else {
		    	try {
			        Thread.sleep(shaker_command_fail_retry_delay);
				} catch (InterruptedException e) {
				}
		    }
		}
		
		return ret;
	}
	
	public int shaker_off_instruct(ExperimentScriptData current_instruct_data) {
		int ret = -1;
		int try_count = shaker_command_retry_count;
		
		while ((try_count--) > 0) {
		    if (0 == send_shaker_command(shaker_off)) {
			    ret = 0;
			    break;
		    } else {
		    	try {
			        Thread.sleep(shaker_command_fail_retry_delay);
				} catch (InterruptedException e) {
				}
		    }
		}
		
		if (0 == ret)
			current_instruct_data.next_instruct_index++;
		
		return ret;
	}
	
	public int shaker_set_temperature_instruct(ExperimentScriptData current_instruct_data) {
		int ret = 0;
		int try_count = shaker_command_retry_count;
		
		while ((try_count--) > 0) {
            ret = 0;
		    if (0 != send_shaker_command(shaker_temperature)) 
		    	ret = -1;
			char[] temperature = current_instruct_data.get_shaker_temperature_string().toCharArray();
			if (0 != send_shaker_command(temperature))
				ret = -1;
			if (0 != send_shaker_command(shaker_end)) 
				ret = -1;
				
			if (0 == ret) {
				break;
			} else {
				try {
			        Thread.sleep(shaker_command_fail_retry_delay);
				} catch (InterruptedException e) {
				}
			}
		}
		
		if (0 == ret)
			current_instruct_data.next_instruct_index++;
		
		return ret;
	}
	
	public int shaker_set_speed_instruct(ExperimentScriptData current_instruct_data) {
		int ret = -1;
        int try_count = shaker_command_retry_count;
		
		while ((try_count--) > 0) {
            ret = 0;
		    if (0 != send_shaker_command(shaker_speed))
		    	ret = -1;
		    
		    char[] speed = new char[3];
		    if (current_instruct_data.get_shaker_speed_value() < 100) {
		    	if (current_instruct_data.get_shaker_speed_value() < 10) {
		    		char[] temp = current_instruct_data.get_shaker_speed_string().toCharArray();
		    		speed[0] = '0';
		    		speed[1] = '0';
		    		speed[2] = temp[0];
		    	} else {
		    		char[] temp = current_instruct_data.get_shaker_speed_string().toCharArray();
		    		speed[0] = '0';
		    		speed[1] = temp[0];
		    		speed[2] = temp[1];
		    	}
		    } else {
		    	speed = current_instruct_data.get_shaker_speed_string().toCharArray();
		    }
			if (0 != send_shaker_command(speed))
				ret = -1;
			if (0 != send_shaker_command(shaker_end))
				ret = -1;
			
			if (0 == ret) {
				break;
			} else {
				try {
			        Thread.sleep(shaker_command_fail_retry_delay);
				} catch (InterruptedException e) {
				}
			}
		}
		
		if (0 == ret) 
			current_instruct_data.next_instruct_index++;
		
		return ret;
	}
	
	public int repeat_count_instruct(ExperimentScriptData current_instruct_data) {
		int ret = 0;
		int size = list_repeat_count.size();
		boolean has_repeat_in_list = false;
			
		for (int i = 0; i < size; i++) {
			if (list_repeat_count.get(i).repeat_instruct_index == current_instruct_data.current_instruct_index) {
				if (list_repeat_count.get(i).repeat_count > 0) {
					list_repeat_count.get(i).repeat_count--;
					current_instruct_data.next_instruct_index = list_repeat_count.get(i).repeat_instruct_from;
					Log.d(Tag, "repeat_count_instruct:"+ list_repeat_count.get(i).repeat_count);
				} else {
					Log.d(Tag, "repeat_count_instruct:"+ list_repeat_count.get(i).repeat_count);
					list_repeat_count.remove(i);
					current_instruct_data.next_instruct_index++;
				}
				has_repeat_in_list = true;
				break;
			}
		}
	    
		if (false == has_repeat_in_list) {
			repeat_informat repeat_count = new repeat_informat();
			repeat_count.repeat_instruct_index = current_instruct_data.current_instruct_index;
			// because get_repeat_from_value() from 1 to ..., so we need to -1
			repeat_count.repeat_instruct_from = current_instruct_data.get_repeat_from_value()-1;
			repeat_count.repeat_count = current_instruct_data.get_repeat_count_value()-1;
			list_repeat_count.add(repeat_count);
			current_instruct_data.next_instruct_index = repeat_count.repeat_instruct_from;
			Log.d(Tag, "repeat_count_instruct:"+ repeat_count.repeat_count);
		}
	
		return ret;
	}
	
	/*public int repeat_time_instruct(experiment_script_data current_instruct_data) {
		int ret = 0;
		int size = list_repeat_time.size();
		boolean has_repeat_in_list = false;
			
		for (int i = 0; i < size; i++) {
			if (list_repeat_time.get(i).repeat_instruct_index == current_instruct_data.current_instruct_index) {
				if ((new Date().getTime() - list_repeat_time.get(i).repeat_time) >= (current_instruct_data.get_repeat_time_value()*60000)) {
					list_repeat_time.remove(i);
					current_instruct_data.next_instruct_index++;
				} else {
					current_instruct_data.next_instruct_index = list_repeat_time.get(i).repeat_instruct_from;
				}
				has_repeat_in_list = true;
				break;
			}
		}
	    
		if (false == has_repeat_in_list) {
			repeat_informat repeat_time = new repeat_informat();
			repeat_time.repeat_instruct_index = current_instruct_data.current_instruct_index;
			// because get_repeat_from_value() from 1 to ..., so we need to -1
			repeat_time.repeat_instruct_from = current_instruct_data.get_repeat_from_value()-1;
			repeat_time.repeat_time = new Date().getTime();
			list_repeat_time.add(repeat_time);
			current_instruct_data.next_instruct_index = repeat_time.repeat_instruct_from;
		}
		
		return ret;
	}*/
	
	public int step_experiment_duration_instruct(ExperimentScriptData current_instruct_data) {
		int ret = 0;
		int size = list_repeat_time.size();
		boolean has_repeat_in_list = false;
			
		for (int i = 0; i < size; i++) {
			if (list_repeat_time.get(i).repeat_instruct_index == current_instruct_data.current_instruct_index) {
				if ((new Date().getTime() - list_repeat_time.get(i).repeat_time) >= (current_instruct_data.get_repeat_time_value()*60000)) {
					list_repeat_time.remove(i);
					step_experiment_start_system_time = new Date().getTime();
					current_instruct_data.next_instruct_index++;
				} else {
					current_instruct_data.next_instruct_index = list_repeat_time.get(i).repeat_instruct_from;
				}
				has_repeat_in_list = true;
				break;
			}
		}
	    
		if (false == has_repeat_in_list) {
			repeat_informat repeat_time = new repeat_informat();
			repeat_time.repeat_instruct_index = current_instruct_data.current_instruct_index;
			// because get_repeat_from_value() from 1 to ..., so we need to -1
			repeat_time.repeat_instruct_from = current_instruct_data.get_repeat_from_value()-1;
			repeat_time.repeat_time = step_experiment_start_system_time;
			list_repeat_time.add(repeat_time);
			current_instruct_data.next_instruct_index = repeat_time.repeat_instruct_from;
		}
		
		return ret;
	}
	
	public int experiment_delay_instruct(ExperimentScriptData current_instruct_data) {
		int ret = -1;
		
		try {
			if (0 == delay_start_system_time) {
				delay_start_system_time = new Date().getTime();
			} else {
				long current_system_time = new Date().getTime();
				long delay_time = (long)current_instruct_data.get_delay_value() * 1000;
				if ((current_system_time - delay_start_system_time) > delay_time) {
					delay_start_system_time = 0;
					current_instruct_data.next_instruct_index++;
				}
			}
			
			if (delay_start_system_time != 0)
			    Thread.sleep((long)(1000));
			ret = 0;
		} catch (InterruptedException e) {

		}
		
		return ret;
	}
}
