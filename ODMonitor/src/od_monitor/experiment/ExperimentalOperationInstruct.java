package od_monitor.experiment;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

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
	
	private long delay_start_system_time = 0;
	private long total_experiment_start_system_time = 0;
	private long step_experiment_start_system_time = 0;
	private long mail_alert_interval = 0;
    private long mail_alert_start_time = 0;
    private boolean is_mail_alert = false;
	private static final int shaker_command_retry_count = 5;
	private static final int sensor_command_retry_count = 5;
	
    /*graphical objects*/
    public TextView readText;
    public SensorDataComposition current_one_sensor_data;
    public double init_od = 0;
    public ODCalculate od_cal = new ODCalculate();
  
    public DeviceUART shaker;
    public DeviceUART sensor;
    public int shaker_port_num = -1;
    public int sensor_port_num = -1;
    public int sensor_data_index = 0;
    public final static char[] shaker_id = new char[] {'I', 'D', ' '};
    public final static char[] shaker_on = new char[] {'O', 'N', ' '};
    public final static char[] shaker_off = new char[] {'O', 'F', ' '};
    public final static char[] shaker_speed = new char[] {'S', 'S', '0'};
    public final static char[] shaker_temperature = new char[] {'S', 'C'};
    public final static char[] shaker_end = new char[] {'0', ' '};
    
    List<repeat_informat> list_repeat_count = new ArrayList<repeat_informat>();
    List<repeat_informat> list_repeat_time = new ArrayList<repeat_informat>();

    /*20150121 added by michael
     * sensor instance */
    public ODMonitorSensor mODMonitorSensor;
    private byte[] sensor_raw_buffer;
	public class repeat_informat {
		public int repeat_instruct_index;
		public int repeat_instruct_from;
		public int repeat_count;
		public long repeat_time;
	};
	
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
	    
	    sensor = new DeviceUART(ExperimentalOperationInstructContext, ftdid2xx, read_text);
	    sensor.baudRate = 115200;
	    sensor.dataBit = 8;
	    sensor.stopBit = 1;
	    sensor.parity = 0;
	    sensor.flowControl = 0;
	    
        /*20150121 added by michael*/
        mODMonitorSensor = new ODMonitorSensor ( parentContext );
        sensor_raw_buffer = new byte[160];
	}
	
	public double get_init_od() {
		return init_od;
	}
	
	public String get_init_od_string() {
		String init_od_string = "" + init_od;
		return init_od_string;
	}
	
	public void set_init_od (double od) {
		init_od = od;
	}
	
	public void set_mail_alert_interval(int interval) {
		mail_alert_interval = (long)(interval*60000);
		//mail_alert_interval = (long)(1*60000);
	}
	
	public boolean get_is_mail_alert() {
		if (0 >= mail_alert_interval) {
			is_mail_alert = false;
		} else {
		    if ((System.currentTimeMillis() - mail_alert_start_time) >= mail_alert_interval) {
        	    is_mail_alert = true;
        	    mail_alert_start_time = System.currentTimeMillis();
            } else {
        	    is_mail_alert = false;
            }
		}
		
		return is_mail_alert;
	}
	
	public int initial_experiment_devices() {
		int ret = 0;
		
		sensor_data_index = 0;
		mail_alert_interval = 0;
		is_mail_alert = false;
		total_experiment_start_system_time = System.currentTimeMillis();
		step_experiment_start_system_time = total_experiment_start_system_time;
		mail_alert_start_time = total_experiment_start_system_time;
		od_cal.initialize(init_od);
		mODMonitorSensor.IOCTL( CMD_T.HID_CMD_ODMONITOR_HELLO, 0, 0, null, 1 );
		if (0 == open_shaker_port()) {
			FileOperateByteArray write_file = new FileOperateByteArray(SensorDataComposition.sensor_raw_folder_name, SensorDataComposition.sensor_raw_file_name, true);
	    	try {
	    		write_file.delete_file(write_file.generate_filename_no_date());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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
	
	public String get_current_one_sensor_data_string() {
		return current_one_sensor_data.get_sensor_data_string();
	}
	
	public SensorDataComposition get_current_one_sensor_data() {
		return current_one_sensor_data;
	}
	
	/*public int save_sensor_data_to_file(int index, long time, String inStr) {
		int ret = 0;
		
        file_operate_byte_array write_file = new file_operate_byte_array(sensor_data_composition.sensor_raw_folder_name, sensor_data_composition.sensor_raw_file_name, true);
    	try {
    	//	write_file.delete_file(write_file.generate_filename_no_date());
            write_file.create_file(write_file.generate_filename_no_date());
            
            if (inStr != null) {
    		    int[] raw_data = OD_calculate.parse_raw_data(inStr);
    		    if ((raw_data != null) && (raw_data.length == sensor_data_composition.raw_total_sensor_data_size)) {
    		    	current_one_sensor_data = new sensor_data_composition();
    		    	current_one_sensor_data.set_sensor_get_index(index);
    		    	// write this sensor data time to file
    		    	current_one_sensor_data.set_sensor_measurement_time(time);
    		    	current_one_sensor_data.set_raw_sensor_data(raw_data);
    		    	write_file.write_file(current_one_sensor_data.buffer);
    		    } else {
    		    	ret = -3;
    		    	Log.e(Tag, "parse raw data fail");
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
	}*/
	
	public int save_sensor_data_to_file(int index, long time, int[] raw_data, String file_name) {
		int ret = 0;
		double od_value = 0;
		
        FileOperateByteArray write_file = new FileOperateByteArray(SensorDataComposition.sensor_raw_folder_name, file_name, true);
    	try {
            write_file.create_file(write_file.generate_filename_no_date());
            
            if (raw_data != null) {
                if (raw_data.length == SensorDataComposition.raw_total_sensor_data_size) {
    		    	current_one_sensor_data = new SensorDataComposition();
    		    	current_one_sensor_data.set_sensor_get_index(index);
    		    	// write this sensor data time to file
    		    	current_one_sensor_data.set_sensor_measurement_time(time);
    		    	current_one_sensor_data.set_raw_sensor_data(raw_data);
    		    	od_value = od_cal.calculate_od(current_one_sensor_data.get_channel_data()); 
    		    	current_one_sensor_data.set_sensor_od_value(od_value);
    		    	write_file.write_file(current_one_sensor_data.buffer);
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
		
		if (mODMonitorSensor.isDeviceOnline() ) {
	        mODMonitorSensor.IOCTL( CMD_T.HID_CMD_ODMONITOR_REQUEST_RAW_DATA, 0, 0, null, 1 );
	        try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
			}
	        
	        mODMonitorSensor.IOCTL( CMD_T.HID_CMD_ODMONITOR_GET_RAW_DATA, 0, 0, sensor_raw_buffer, 0 );
	        IntBuffer raw_IntBuffer = ByteBuffer.wrap(sensor_raw_buffer).order(ByteOrder.LITTLE_ENDIAN).asIntBuffer();
	        int[] raw_data = new int[raw_IntBuffer.remaining()];
	        raw_IntBuffer.get(raw_data);
	        Log.d(Tag, raw_data[0]+","+ raw_data[1]+","+ raw_data[2]+","+raw_data[3]+","+raw_data[4]+","+raw_data[5]+","+raw_data[6]+","+raw_data[7]+","+raw_data[8]+","+raw_data[9]);
	        int[] raw_data_save = new int[SensorDataComposition.raw_total_sensor_data_size];
	        
	        for (int j = 0; j < 4; j++) {
	        	if (1 == raw_data[j*10]) {
	                for (int i = 0; i < SensorDataComposition.raw_total_sensor_data_size; i++) {
	        	        raw_data_save[i] = raw_data[1+i+j*10];
	                }
	        
	                String file_name = SensorDataComposition.sensor_raw_file_name + (j+1);
	                if (0 == save_sensor_data_to_file(sensor_data_index, new Date().getTime(), raw_data_save, SensorDataComposition.sensor_raw_file_name)) {
			            ret = 0;
			        }
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
		int ret = 0;
		char[] readDataChar = new char[cmd.length+10];
		int receive_length = 0;
		
        for (int i = 0; i < cmd.length; i++) {
            String send_cmd = String.copyValueOf(cmd, i, 1);
    		if (0 < (receive_length = shaker.SendMessage(send_cmd, readDataChar))) {
    		    String read_string = String.copyValueOf(readDataChar, 0, receive_length);
    		    if (false == read_string.equals(send_cmd)) {
    		        ret = -1;
    		    }
    		} else {
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
		    }
		}
		
		if (0 == ret)
			current_instruct_data.next_instruct_index++;
		
		return ret;
	}
	
	public int shaker_off_instruct(ExperimentScriptData current_instruct_data) {
		int ret = -1;
		int try_count = shaker_command_retry_count;
		
		while ((try_count--) > 0) {
		    if (0 == send_shaker_command(shaker_off)) {
			    ret = 0;
			    break;
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
				
			if (0 == ret)
				break;
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
			
			if (0 == ret)
				break;
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
