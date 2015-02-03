package od_monitor.app.data;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class step_experiment_script_data implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 5273557851116510352L;
	public static final int INSTRUCT_SET_HIGH_SPEED = 0;
	public static final int INSTRUCT_SET_TEMPERATURE = 1;
	public static final int INSTRUCT_SET_HIGH_SPEED_OPERATION_DURATION = 2;
	public static final int INSTRUCT_SET_LOW_SPEED = 3;
	public static final int INSTRUCT_SET_LOW_SPEED_OPERATION_DURATION = 4;
	public static final int INSTRUCT_READ_SENSOR = 5;
	public static final int INSTRUCT_SET_EXPERIMENT_OPERATION_DURATION = 6;
	public static final int TOTAL_INSTRUCT_COUNT = 7;
	
	public static final int high_speed_rpm = 250;
	public static final int low_speed_rpm = 100;
	public static final int high_speed_operation_duration = 150;
	public static final int low_speed_operation_duration = 30;
	public static final int temperature = 37;
	public static final int experiment_operation_duration = 360;
	private experiment_script_data[] instruct = new experiment_script_data[7];
	
	public step_experiment_script_data() {
		instruct[INSTRUCT_SET_HIGH_SPEED] = new experiment_script_data();
        instruct[INSTRUCT_SET_HIGH_SPEED].set_instruct_value(experiment_script_data.INSTRUCT_SHAKER_SET_SPEED);
        instruct[INSTRUCT_SET_HIGH_SPEED].set_shaker_speed_value(high_speed_rpm);
             
        instruct[INSTRUCT_SET_TEMPERATURE] = new experiment_script_data();
        instruct[INSTRUCT_SET_TEMPERATURE].set_instruct_value(experiment_script_data.INSTRUCT_SHAKER_SET_TEMPERATURE);
        instruct[INSTRUCT_SET_TEMPERATURE].set_shaker_temperature_value(temperature);
         
        instruct[INSTRUCT_SET_HIGH_SPEED_OPERATION_DURATION] = new experiment_script_data();
        instruct[INSTRUCT_SET_HIGH_SPEED_OPERATION_DURATION].set_instruct_value(experiment_script_data.INSTRUCT_DELAY);
        instruct[INSTRUCT_SET_HIGH_SPEED_OPERATION_DURATION].set_delay_value(high_speed_operation_duration);
          
        instruct[INSTRUCT_SET_LOW_SPEED] = new experiment_script_data();
        instruct[INSTRUCT_SET_LOW_SPEED].set_instruct_value(experiment_script_data.INSTRUCT_SHAKER_SET_SPEED);
        instruct[INSTRUCT_SET_LOW_SPEED].set_shaker_speed_value(low_speed_rpm);
     
        instruct[INSTRUCT_SET_LOW_SPEED_OPERATION_DURATION] = new experiment_script_data();
        instruct[INSTRUCT_SET_LOW_SPEED_OPERATION_DURATION].set_instruct_value(experiment_script_data.INSTRUCT_DELAY);
        instruct[INSTRUCT_SET_LOW_SPEED_OPERATION_DURATION].set_delay_value(low_speed_operation_duration);
      
        instruct[INSTRUCT_READ_SENSOR] = new experiment_script_data();
        instruct[INSTRUCT_READ_SENSOR].set_instruct_value(experiment_script_data.INSTRUCT_READ_SENSOR);
    
        instruct[INSTRUCT_SET_EXPERIMENT_OPERATION_DURATION] = new experiment_script_data();
        instruct[INSTRUCT_SET_EXPERIMENT_OPERATION_DURATION].set_instruct_value(experiment_script_data.INSTRUCT_REPEAT_TIME);
        instruct[INSTRUCT_SET_EXPERIMENT_OPERATION_DURATION].set_repeat_from_value(0);
        instruct[INSTRUCT_SET_EXPERIMENT_OPERATION_DURATION].set_repeat_time_value(experiment_operation_duration);
	}
	
	public int get_high_speed_rpm() {
		 return instruct[INSTRUCT_SET_HIGH_SPEED].get_shaker_speed_value();
	}
	
	public int get_high_speed_operation_duration() {
		return instruct[INSTRUCT_SET_HIGH_SPEED_OPERATION_DURATION].get_delay_value();
	}
	
	public int get_low_speed_rpm() {
		 return instruct[INSTRUCT_SET_LOW_SPEED].get_shaker_speed_value();
	}
	
	public int get_low_speed_operation_duration() {
		return instruct[INSTRUCT_SET_LOW_SPEED_OPERATION_DURATION].get_delay_value();
	}
	
	public int get_temperature() {
		 return instruct[INSTRUCT_SET_TEMPERATURE].get_shaker_temperature_value();
	}
	
	public int get_experiment_operation_duration() {
		 return instruct[INSTRUCT_SET_EXPERIMENT_OPERATION_DURATION].get_repeat_time_value();
	}
	
	public void set_high_speed_rpm(int speed) {
		 instruct[INSTRUCT_SET_HIGH_SPEED].set_shaker_speed_value(speed);
	}
	
	public void set_high_speed_operation_duration(int duration) {
		instruct[INSTRUCT_SET_HIGH_SPEED_OPERATION_DURATION].set_delay_value(duration);
	}
	
	public void set_low_speed_rpm(int speed) {
		 instruct[INSTRUCT_SET_LOW_SPEED].set_shaker_speed_value(speed);
	}
	
	public void set_low_speed_operation_duration(int duration) {
		instruct[INSTRUCT_SET_LOW_SPEED_OPERATION_DURATION].set_delay_value(duration);
	}
	
	public void set_temperature(int temperature) {
		 instruct[INSTRUCT_SET_TEMPERATURE].set_shaker_temperature_value(temperature);
	}
	
	public void set_experiment_operation_duration(int duration) {
		 instruct[INSTRUCT_SET_EXPERIMENT_OPERATION_DURATION].set_repeat_time_value(duration);
	}
	
	public String get_high_speed_rpm_string() {
		 return instruct[INSTRUCT_SET_HIGH_SPEED].get_shaker_speed_string();
	}
	
	public String get_high_speed_operation_duration_string() {
		return instruct[INSTRUCT_SET_HIGH_SPEED_OPERATION_DURATION].get_delay_string();
	}
	
	public String get_low_speed_rpm_string() {
		 return instruct[INSTRUCT_SET_LOW_SPEED].get_shaker_speed_string();
	}
	
	public String get_low_speed_operation_duration_string() {
		return instruct[INSTRUCT_SET_LOW_SPEED_OPERATION_DURATION].get_delay_string();
	}
	
	public String get_temperature_string() {
		 return instruct[INSTRUCT_SET_TEMPERATURE].get_shaker_temperature_string();
	}
	
	public String get_experiment_operation_duration_string() {
		 return instruct[INSTRUCT_SET_EXPERIMENT_OPERATION_DURATION].get_repeat_time_string();
	}
	
	public int get_step_instruct_to_file_buffer(int start_instruct_index, byte[] file_buffer) {
		int current_instruct_index = start_instruct_index;
		
		instruct[INSTRUCT_SET_EXPERIMENT_OPERATION_DURATION].set_repeat_from_value(start_instruct_index);
		for (int i = 0; i < TOTAL_INSTRUCT_COUNT; i++) {
			ByteBuffer byteBuffer = ByteBuffer.allocate(4);
    	    byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
    	
		    byte[] buffer = instruct[i].get_buffer();
		    byte[] index_bytes = byteBuffer.putInt(current_instruct_index).array();
		    current_instruct_index++;
       	    System.arraycopy(index_bytes, 0, buffer, experiment_script_data.INDEX_START, experiment_script_data.INDEX_SIZE);
       	    System.arraycopy(buffer, 0, file_buffer, i*experiment_script_data.BUFFER_SIZE, experiment_script_data.BUFFER_SIZE);
		}
		
		return current_instruct_index;
	}
	
	public int set_step_instruct_data(int list_pos, List<HashMap<String,Object>> list, HashMap<Object, Object> experiment_item) {
		experiment_script_data data = (experiment_script_data)experiment_item.get(list.get(list_pos));
		list_pos++;
	
		if (data.get_instruct_value() == experiment_script_data.INSTRUCT_SHAKER_SET_SPEED)
            instruct[INSTRUCT_SET_HIGH_SPEED].set_shaker_speed_value(data.get_shaker_speed_value());
		else
			return -1;
          
		data = (experiment_script_data)experiment_item.get(list.get(list_pos));
		list_pos++;
		if (data.get_instruct_value() == experiment_script_data.INSTRUCT_SHAKER_SET_TEMPERATURE)
            instruct[INSTRUCT_SET_TEMPERATURE].set_shaker_temperature_value(data.get_shaker_temperature_value());
		else
			return -2;
         
		data = (experiment_script_data)experiment_item.get(list.get(list_pos));
		list_pos++;
		if (data.get_instruct_value() == experiment_script_data.INSTRUCT_DELAY)
            instruct[INSTRUCT_SET_HIGH_SPEED_OPERATION_DURATION].set_delay_value(data.get_delay_value());
		else
			return -3;
          
		data = (experiment_script_data)experiment_item.get(list.get(list_pos));
		list_pos++;
		if (data.get_instruct_value() == experiment_script_data.INSTRUCT_SHAKER_SET_SPEED)
            instruct[INSTRUCT_SET_LOW_SPEED].set_shaker_speed_value(data.get_shaker_speed_value());
		else
			return -4;
     
		data = (experiment_script_data)experiment_item.get(list.get(list_pos));
		list_pos++;
		if (data.get_instruct_value() == experiment_script_data.INSTRUCT_DELAY)
            instruct[INSTRUCT_SET_LOW_SPEED_OPERATION_DURATION].set_delay_value(data.get_delay_value());
		else
			return -5;
      
		data = (experiment_script_data)experiment_item.get(list.get(list_pos));
		list_pos++;
		if (data.get_instruct_value() == experiment_script_data.INSTRUCT_READ_SENSOR)
            instruct[INSTRUCT_READ_SENSOR].set_instruct_value(experiment_script_data.INSTRUCT_READ_SENSOR);
		else
			return -6;
    
		data = (experiment_script_data)experiment_item.get(list.get(list_pos));
		list_pos++;
		if (data.get_instruct_value() == experiment_script_data.INSTRUCT_REPEAT_TIME)
            instruct[INSTRUCT_SET_EXPERIMENT_OPERATION_DURATION].set_repeat_time_value(data.get_repeat_time_value());
		else
			return -7;
		
		return list_pos;
	}
}
