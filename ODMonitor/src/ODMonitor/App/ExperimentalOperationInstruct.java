package ODMonitor.App;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.ftdi.j2xx.D2xxManager;
import com.ftdi.j2xx.FT_Device;

import ODMonitor.App.data.experiment_script_data;

public class ExperimentalOperationInstruct {
	public static String Tag = "ExperimentalOperationInstruct";
	public static Context ExperimentalOperationInstructContext;
	public D2xxManager ftdid2xx;
	
    /*graphical objects*/
    public TextView readText;
    
    public DeviceUART shaker;
    public DeviceUART sensor;
    public int shaker_port_num = -1;
    public int sensor_port_num = -1;
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
    ODMonitor_Sensor mODMonitorSensor;
	public class repeat_informat {
		public int repeat_instruct_index;
		public int repeat_instruct_from;
		public int repeat_count;
		public long repeat_time;
	};
	
	ExperimentalOperationInstruct(Context parentContext, D2xxManager ftdid2xxContext, TextView read_text) {
	    ExperimentalOperationInstructContext = parentContext;
	    ftdid2xx = ftdid2xxContext;
	    readText = read_text;
	    
	    shaker = new DeviceUART(ExperimentalOperationInstructContext, ftdid2xx, readText);
	    shaker.baudRate = 38400;
	    shaker.dataBit = 8;
	    shaker.stopBit = 1;
	    shaker.parity = 0;
	    shaker.flowControl = 0; 
	    
	    sensor = new DeviceUART(ExperimentalOperationInstructContext, ftdid2xx, readText);
	    sensor.baudRate = 115200;
	    sensor.dataBit = 8;
	    sensor.stopBit = 1;
	    sensor.parity = 0;
	    sensor.flowControl = 0;
	    
        /*20150121 added by michael*/
        mODMonitorSensor = new ODMonitor_Sensor ( parentContext );
	}
	
	public int check_shaker_port_number() {
		int ret = -1;
		int total_port_num = shaker.createDeviceList();
		char[] readDataChar = new char[20];
		int receive_length = 0;
		int try_count = 3;
		
		if (total_port_num <= 0) {
			ret = -2;
			return ret;
		}
		
		for (int i = 0; i < total_port_num; i++) {
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
		}
		
		if (ret == 0)
			Toast.makeText(ExperimentalOperationInstructContext, "shaker check OK", Toast.LENGTH_SHORT).show();
		else
			Toast.makeText(ExperimentalOperationInstructContext, "shaker check fail", Toast.LENGTH_SHORT).show();
		
		return ret;
	}
	
	public int check_sensor_port_number() {
		int ret = -1;
		int total_port_num = sensor.createDeviceList();
		char[] readDataChar = new char[50];
		int receive_length = 0;
		int try_count = 3;
		
		if (total_port_num <= 0) {
			ret = -2;
			return ret;
		}
		
		for (int i = 0; i < total_port_num; i++) {
			if (0 == sensor.connectFunction(i)) {
				sensor.SetConfig(sensor.baudRate, sensor.dataBit, sensor.stopBit, sensor.parity, sensor.flowControl);
				while ((try_count--) > 0) {
    		        if (0 < (receive_length = sensor.SendMessage("Hello OD Monitor\r", readDataChar))) {
    		    	    String read_string = String.copyValueOf(readDataChar, 0, receive_length);
    		    	    if (true == read_string.equals("I am OD monitor\r")) {
    		    		    sensor.disconnectFunction();
    		    	        sensor_port_num = i;
    		    	        ret = 0;
    		    	        try_count = 0;
    		    	    } else {
    		    	        Toast.makeText(ExperimentalOperationInstructContext, "sensor check no match", Toast.LENGTH_SHORT).show();
    		    	    }
    		        } else {
    		        	Toast.makeText(ExperimentalOperationInstructContext, "sensor send message fail", Toast.LENGTH_SHORT).show();
    		        }
				}
    		        
    		    sensor.disconnectFunction();
    	    } else {
    		    Log.d(Tag, "sensor connect NG");
    	    } 		
		}
		
		if (ret == 0)
			Toast.makeText(ExperimentalOperationInstructContext, "sensor check OK", Toast.LENGTH_SHORT).show();
		else
			Toast.makeText(ExperimentalOperationInstructContext, "sensor check fail", Toast.LENGTH_SHORT).show();
		
		return ret;
	}
	
	public int read_sensor_instruct(experiment_script_data current_instruct_data) {
		int ret = -1;
		char[] readDataChar = new char[128];
		int receive_length = 0;
		
		if (0 < sensor.createDeviceList()) {
    	    if (0 == sensor.connectFunction(0/*sensor_port_num*/)) {
    	    	sensor.SetConfig(sensor.baudRate, sensor.dataBit, sensor.stopBit, sensor.parity, sensor.flowControl);
    		    if (0 < (receive_length = sensor.SendMessage("Request OD data\r", readDataChar))) {
    		    	    String read_string = String.copyValueOf(readDataChar, 0, receive_length);
    		    	    ret = 0;
    		    	    Toast.makeText(ExperimentalOperationInstructContext, read_string, Toast.LENGTH_SHORT).show();
    		    }
    		    sensor.disconnectFunction();
    	    } else {
    		    Log.d(Tag, "sensor connect NG");
    	    } 		
        } else {
    	    Log.d(Tag, "no device on list");
        }
		
		if (ret == 0)
			Toast.makeText(ExperimentalOperationInstructContext, "sensor request OK", Toast.LENGTH_SHORT).show();
		else
			Toast.makeText(ExperimentalOperationInstructContext, "sensor request fail", Toast.LENGTH_SHORT).show();
		
		return ret;
	}
	
	public int send_shaker_command(char[] cmd) {
		int ret = 0;
		char[] readDataChar = new char[cmd.length+10];
		int receive_length = 0;
		
		if (0 < shaker.createDeviceList()) {
    	    if (0 == shaker.connectFunction(0/*shaker_port_num*/)) {
    		    shaker.SetConfig(shaker.baudRate, shaker.dataBit, shaker.stopBit, shaker.parity, shaker.flowControl);
    		    for (int i = 0; i < cmd.length; i++) {
    		    	String send_cmd = String.copyValueOf(cmd, i, 1);
    		        if (0 < (receive_length = shaker.SendMessage(send_cmd, readDataChar))) {
    		        	String read_string = String.copyValueOf(readDataChar, 0, receive_length);
    		    	    if (false == read_string.equals(send_cmd)) {
    		    	        ret = -1;
    		    	        //break;
    		    	    }
    		        } else {
    		        }
    		    }
    		    shaker.disconnectFunction();
    	    } else {
    	    	ret = -2;
    		    Log.d(Tag, "shaker connect NG");
    	    } 		
        } else {
        	ret = -3;
    	    Log.d(Tag, "no device on list");
        } 
		
		return ret;
	}
	
	public int shaker_on_instruct(experiment_script_data current_instruct_data) {
		int ret = -1;
		int try_count = 3;
	
		while ((try_count--) > 0) {
		    if (0 == send_shaker_command(shaker_on)) {
			    ret = 0;
			    break;
		    }
		}
		
		return ret;
	}
	
	public int shaker_off_instruct(experiment_script_data current_instruct_data) {
		int ret = -1;
		int try_count = 3;
		
		while ((try_count--) > 0) {
		    if (0 == send_shaker_command(shaker_off)) {
			    ret = 0;
			    break;
		    }
		}
		
		return ret;
	}
	
	public int shaker_set_temperature_instruct(experiment_script_data current_instruct_data) {
		int ret = 0;
		int try_count = 3;
		
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
		
		return ret;
	}
	
	public int shaker_set_speed_instruct(experiment_script_data current_instruct_data) {
		int ret = -1;
        int try_count = 3;
		
		while ((try_count--) > 0) {
            ret = 0;
		    if (0 != send_shaker_command(shaker_speed))
		    	ret = -1;
			char[] speed = current_instruct_data.get_shaker_speed_string().toCharArray();
			if (0 != send_shaker_command(speed))
				ret = -1;
			if (0 != send_shaker_command(shaker_end))
				ret = -1;
			
			if (0 == ret)
				break;
		}
		
		return ret;
	}
	
	public int repeat_count_instruct(experiment_script_data current_instruct_data) {
		int ret = 0;
		int size = list_repeat_count.size();
		boolean has_repeat_in_list = false;
			
		for (int i = 0; i < size; i++) {
			if (list_repeat_count.get(i).repeat_instruct_index == current_instruct_data.current_instruct_index) {
				if (list_repeat_count.get(i).repeat_count > 0) {
					list_repeat_count.get(i).repeat_count--;
					current_instruct_data.next_instruct_index = list_repeat_count.get(i).repeat_instruct_from;
				} else {
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
			repeat_count.repeat_count = current_instruct_data.get_repeat_count_value();
			list_repeat_count.add(repeat_count);
			current_instruct_data.next_instruct_index = repeat_count.repeat_instruct_from;
		}
	
		return ret;
	}
	
	public int repeat_time_instruct(experiment_script_data current_instruct_data) {
		int ret = 0;
		int size = list_repeat_time.size();
		boolean has_repeat_in_list = false;
			
		for (int i = 0; i < size; i++) {
			if (list_repeat_time.get(i).repeat_instruct_index == current_instruct_data.current_instruct_index) {
				if ((new Date().getTime() - list_repeat_count.get(i).repeat_time) >= (current_instruct_data.get_repeat_time_value()*1000)) {
					list_repeat_count.remove(i);
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
			list_repeat_count.add(repeat_time);
			current_instruct_data.next_instruct_index = repeat_time.repeat_instruct_from;
		}
		
		return ret;
	}
	
	public int experiment_delay_instruct(experiment_script_data current_instruct_data) {
		int ret = -1;
		
		try {
			Thread.sleep((long)(current_instruct_data.get_delay_value()*1000));
			ret = 0;
		} catch (InterruptedException e) {

		}
		
		return ret;
	}
}
