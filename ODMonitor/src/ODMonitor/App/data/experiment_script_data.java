package ODMonitor.App.data;

import android.annotation.SuppressLint;
import android.util.SparseBooleanArray;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class experiment_script_data implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 7763243430535626819L;
	public static final int  INSTRUCT_READ_SENSOR = 0;
	public static final int  INSTRUCT_SHAKER_ON = 1; 
	public static final int  INSTRUCT_SHAKER_OFF = 2;
	public static final int  INSTRUCT_SHAKER_SET_TEMPERATURE = 3;
	public static final int  INSTRUCT_SHAKER_SET_SPEED = 4;
	public static final int  INSTRUCT_REPEAT_COUNT = 5;
	public static final int  INSTRUCT_REPEAT_TIME = 6;
	public static final int  INSTRUCT_DELAY = 7;
	public static final int  INSTRUCT_FINISH = 8;
	
	public static final int  INDEX_START = 0;
	public static final int  INDEX_SIZE = 4;
	public static final int  INSTRUCT_START = INDEX_SIZE;
	public static final int  INSTRUCT_SIZE = 4;
	public static final int  ARGUMENT1_START = INDEX_SIZE+INSTRUCT_SIZE;
	public static final int  ARGUMENT1_SIZE = 4;
	public static final int  ARGUMENT2_START = INDEX_SIZE+INSTRUCT_SIZE+ARGUMENT1_SIZE;
	public static final int  ARGUMENT2_SIZE = 4;
	public static final int  BUFFER_SIZE = INDEX_SIZE+INSTRUCT_SIZE+ARGUMENT1_SIZE+ARGUMENT2_SIZE;
	public static final Map<Integer, String> SCRIPT_INSTRUCT;
    static {
        Map<Integer, String> aMap = new HashMap<Integer, String>();
        aMap.put(INSTRUCT_READ_SENSOR, "Read Sensor");
        aMap.put(INSTRUCT_SHAKER_ON, "Shaker ON");
        aMap.put(INSTRUCT_SHAKER_OFF, "Shaker OFF");
        aMap.put(INSTRUCT_SHAKER_SET_TEMPERATURE, "Shaker Set Temperature");
        aMap.put(INSTRUCT_SHAKER_SET_SPEED, "Shaker Set Speed");
        aMap.put(INSTRUCT_REPEAT_COUNT, "Repeat Count");
        aMap.put(INSTRUCT_REPEAT_TIME, "Repeat Time");
        aMap.put(INSTRUCT_DELAY, "Delay");
        SCRIPT_INSTRUCT = Collections.unmodifiableMap(aMap);
    }
    
    public static final List<SparseBooleanArray> SCRIPT_SETTING_ENABLE_LIST;
    static {
       // Map<Integer, Boolean> aMap = new HashMap<Integer, Boolean>();
        List<SparseBooleanArray> list = new ArrayList<SparseBooleanArray>();
        SparseBooleanArray read_sensor_enable = new SparseBooleanArray(6);
        read_sensor_enable.put(0, false);
        read_sensor_enable.put(1, false);
        read_sensor_enable.put(2, false);
        read_sensor_enable.put(3, false);
        read_sensor_enable.put(4, false);
        read_sensor_enable.put(5, false);
        list.add(read_sensor_enable);
        
        SparseBooleanArray shaker_on_enable = new SparseBooleanArray(6);
        shaker_on_enable.put(0, false);
        shaker_on_enable.put(1, false);
        shaker_on_enable.put(2, false);
        shaker_on_enable.put(3, false);
        shaker_on_enable.put(4, false);
        shaker_on_enable.put(5, false);
        list.add(shaker_on_enable);
        
        SparseBooleanArray shaker_off_enable = new SparseBooleanArray(6);
        shaker_off_enable.put(0, false);
        shaker_off_enable.put(1, false);
        shaker_off_enable.put(2, false);
        shaker_off_enable.put(3, false);
        shaker_off_enable.put(4, false);
        shaker_off_enable.put(5, false);
        list.add(shaker_off_enable);
        
        SparseBooleanArray shaker_set_temperature_enable = new SparseBooleanArray(6);
        shaker_set_temperature_enable.put(0, false);
        shaker_set_temperature_enable.put(1, false);
        shaker_set_temperature_enable.put(2, false);
        shaker_set_temperature_enable.put(3, true);
        shaker_set_temperature_enable.put(4, false);
        shaker_set_temperature_enable.put(5, false);
        list.add(shaker_set_temperature_enable);
        
        SparseBooleanArray shaker_set_speed_enable = new SparseBooleanArray(6);
        shaker_set_speed_enable.put(0, false);
        shaker_set_speed_enable.put(1, false);
        shaker_set_speed_enable.put(2, false);
        shaker_set_speed_enable.put(3, false);
        shaker_set_speed_enable.put(4, true);
        shaker_set_speed_enable.put(5, false);
        list.add(shaker_set_speed_enable);
        
        SparseBooleanArray repeat_count_enable = new SparseBooleanArray(6);
        repeat_count_enable.put(0, true);
        repeat_count_enable.put(1, true);
        repeat_count_enable.put(2, false);
        repeat_count_enable.put(3, false);
        repeat_count_enable.put(4, false);
        repeat_count_enable.put(5, false);
        list.add(repeat_count_enable);
        
        SparseBooleanArray repeat_time_enable = new SparseBooleanArray(6);
        repeat_time_enable.put(0, true);
        repeat_time_enable.put(1, false);
        repeat_time_enable.put(2, true);
        repeat_time_enable.put(3, false);
        repeat_time_enable.put(4, false);
        repeat_time_enable.put(5, false);
        list.add(repeat_time_enable);
        
        SparseBooleanArray delay_enable = new SparseBooleanArray(6);
        delay_enable.put(0, false);
        delay_enable.put(1, false);
        delay_enable.put(2, false);
        delay_enable.put(3, false);
        delay_enable.put(4, false);
        delay_enable.put(5, true);
        list.add(delay_enable);
        
        SCRIPT_SETTING_ENABLE_LIST = Collections.unmodifiableList(list);
    }
    
    private int instruct = INSTRUCT_READ_SENSOR;
    private int repeat_from = 1;
    private int repeat_count = 1;
    private int repeat_time = 1;
    private int shaker_temperature = 25;
    private int shaker_speed = 20;
    private int delay = 10;
    
    public void set_instruct_value(int data) {
	    instruct = data;
	}
    
    public int get_instruct_value() {
		return instruct;
	}
    
    public String get_instruct_string() {
		return SCRIPT_INSTRUCT.get(get_instruct_value());
	}
    
    public void set_repeat_from_value(int data) {
    	repeat_from = data;
	}
    
    public int get_repeat_from_value() {
		return repeat_from;
	}
    
    public String get_repeat_from_string() {
    	String str;
    	str = String.format("%d", get_repeat_from_value());
		return str;
	}
    
    public void set_repeat_count_value(int data) {
    	repeat_count = data;
	}
    
    public int get_repeat_count_value() {
		return repeat_count;
	}
    
    public String get_repeat_count_string() {
    	String str;
    	str = String.format("%d", get_repeat_count_value());
		return str;
	}
    
    public void set_repeat_time_value(int data) {
    	repeat_time = data;
	}
    
    public int get_repeat_time_value() {
		return repeat_time;
	}
    
    public String get_repeat_time_string() {
    	String str;
    	str = String.format("%d", get_repeat_time_value());
		return str;
	}
    
    public void set_shaker_temperature_value(int data) {
    	shaker_temperature = data;
	}
    
    public int get_shaker_temperature_value() {
		return shaker_temperature;
	}
    
    public String get_shaker_temperature_string() {
    	String str;
    	str = String.format("%d", get_shaker_temperature_value());
		return str;
	}
    
    public void set_shaker_speed_value(int data) {
    	shaker_speed = data;
	}
    
    public int get_shaker_speed_value() {
		return shaker_speed;
	}
    
    public String get_shaker_speed_string() {
    	String str;
    	str = String.format("%d", get_shaker_speed_value());
		return str;
	}
    
    public void set_delay_value(int data) {
    	delay = data;
	}
    
    public int get_delay_value() {
		return delay;
	}
    
    public String get_delay_string() {
    	String str;
    	str = String.format("%d", get_delay_value());
		return str;
	}
    
    public byte[] get_buffer() {
    	byte[] buffer = new byte[BUFFER_SIZE];
    	Arrays.fill(buffer, (byte)0);
    	
    	{
    	    ByteBuffer byteBuffer = ByteBuffer.allocate(4);
    	    byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
    	
    	    byte[] instruct_bytes = byteBuffer.putInt(instruct).array();
    	    System.arraycopy(instruct_bytes, 0, buffer, INSTRUCT_START, INSTRUCT_SIZE);
    	}
 
    	switch (instruct) {
    	    case INSTRUCT_SHAKER_SET_TEMPERATURE: { 
    	    	ByteBuffer byteBuffer = ByteBuffer.allocate(4);
    	    	byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
    	    	byte[] shaker_temperature_bytes = byteBuffer.putInt(shaker_temperature).array();
    	    	System.arraycopy(shaker_temperature_bytes, 0, buffer, ARGUMENT1_START, ARGUMENT1_SIZE);
    	    } break;
    	    
    	    case INSTRUCT_SHAKER_SET_SPEED: {
    	    	ByteBuffer byteBuffer = ByteBuffer.allocate(4);
    	    	byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
    	    	byte[] shaker_speed_bytes = byteBuffer.putInt(shaker_speed).array();
    	    	System.arraycopy(shaker_speed_bytes, 0, buffer, ARGUMENT1_START, ARGUMENT1_SIZE);
    	    } break;
    	    
    	    case INSTRUCT_REPEAT_COUNT: {
    	    	ByteBuffer byteBuffer = ByteBuffer.allocate(4);
    	    	byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
    	    	byte[] repeat_from_bytes = byteBuffer.putInt(repeat_from).array();
    	    	byteBuffer = ByteBuffer.allocate(4);
    	    	byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
    	    	byte[] repeat_count_bytes = byteBuffer.putInt(repeat_count).array();
    	    	System.arraycopy(repeat_from_bytes, 0, buffer, ARGUMENT2_START, ARGUMENT2_SIZE);
    	    	System.arraycopy(repeat_count_bytes, 0, buffer, ARGUMENT1_START, ARGUMENT1_SIZE);
    	    } break;
        	    
        	case INSTRUCT_REPEAT_TIME: {
        		ByteBuffer byteBuffer = ByteBuffer.allocate(4);
            	byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        		byte[] repeat_from_bytes = byteBuffer.putInt(repeat_from).array();
        		byteBuffer = ByteBuffer.allocate(4);
        		byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        		byte[] repeat_time_bytes = byteBuffer.putInt(repeat_time).array();
        		System.arraycopy(repeat_from_bytes, 0, buffer, ARGUMENT2_START, ARGUMENT2_SIZE);
    	    	System.arraycopy(repeat_time_bytes, 0, buffer, ARGUMENT1_START, ARGUMENT1_SIZE);
        	} break;
        	
        	case INSTRUCT_DELAY: {
        		ByteBuffer byteBuffer = ByteBuffer.allocate(4);
            	byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
    	    	byte[] delay_bytes = byteBuffer.putInt(delay).array();
    	    	System.arraycopy(delay_bytes, 0, buffer, ARGUMENT1_START, ARGUMENT1_SIZE);
        	} break;
    	    
    	    default:
    	    break;
    	}
    	
    	return buffer;
    }
    
    public int set_buffer(byte[] buffer) {
    	byte[] instruct_bytes = new byte[INSTRUCT_SIZE];
        int ret = -1;
        
    	if (buffer.length == BUFFER_SIZE) {
    		System.arraycopy(buffer, INSTRUCT_START, instruct_bytes, 0, INSTRUCT_SIZE);
    		ByteBuffer byte_buffer = ByteBuffer.wrap(instruct_bytes, 0, INSTRUCT_SIZE);
    		byte_buffer.order(ByteOrder.LITTLE_ENDIAN);
            instruct = byte_buffer.getInt();
            
    		switch (instruct) {
    	        case INSTRUCT_SHAKER_SET_TEMPERATURE:
    	    	    byte[] shaker_temperature_bytes = new byte[ARGUMENT1_SIZE];
    	    	    System.arraycopy(buffer, ARGUMENT1_START, shaker_temperature_bytes, 0, ARGUMENT1_SIZE);
    	    	    byte_buffer = ByteBuffer.wrap(shaker_temperature_bytes, 0, ARGUMENT1_SIZE);
    	    		byte_buffer.order(ByteOrder.LITTLE_ENDIAN);
    	            shaker_temperature = byte_buffer.getInt();
    	        break;
    	    
    	        case INSTRUCT_SHAKER_SET_SPEED:
    	        	byte[] shaker_speed_bytes = new byte[ARGUMENT1_SIZE];
    	    	    System.arraycopy(buffer, ARGUMENT1_START, shaker_speed_bytes, 0, ARGUMENT1_SIZE);
    	    	    byte_buffer = ByteBuffer.wrap(shaker_speed_bytes, 0, ARGUMENT1_SIZE);
    	    		byte_buffer.order(ByteOrder.LITTLE_ENDIAN);
    	            shaker_speed = byte_buffer.getInt();
    	        break;
    	    
    	        case INSTRUCT_REPEAT_COUNT: {
    	        	byte[] repeat_count_bytes = new byte[ARGUMENT1_SIZE];
    	    	    System.arraycopy(buffer, ARGUMENT1_START, repeat_count_bytes, 0, ARGUMENT1_SIZE);
    	    	    byte_buffer = ByteBuffer.wrap(repeat_count_bytes, 0, ARGUMENT1_SIZE);
    	    		byte_buffer.order(ByteOrder.LITTLE_ENDIAN);
    	            repeat_count = byte_buffer.getInt();
    	            
    	            byte[] repeat_from_bytes = new byte[ARGUMENT2_SIZE];
    	    	    System.arraycopy(buffer, ARGUMENT2_START, repeat_from_bytes, 0, ARGUMENT2_SIZE);
    	    	    byte_buffer = ByteBuffer.wrap(repeat_from_bytes, 0, ARGUMENT2_SIZE);
    	    		byte_buffer.order(ByteOrder.LITTLE_ENDIAN);
    	            repeat_from = byte_buffer.getInt();
    	        } break;
        	    
        	    case INSTRUCT_REPEAT_TIME: {
        	    	byte[] repeat_time_bytes = new byte[ARGUMENT1_SIZE];
    	    	    System.arraycopy(buffer, ARGUMENT1_START, repeat_time_bytes, 0, ARGUMENT1_SIZE);
    	    	    byte_buffer = ByteBuffer.wrap(repeat_time_bytes, 0, ARGUMENT1_SIZE);
    	    		byte_buffer.order(ByteOrder.LITTLE_ENDIAN);
    	            repeat_time = byte_buffer.getInt();
    	            
    	            byte[] repeat_from_bytes = new byte[ARGUMENT2_SIZE];
    	    	    System.arraycopy(buffer, ARGUMENT2_START, repeat_from_bytes, 0, ARGUMENT2_SIZE);
    	    	    byte_buffer = ByteBuffer.wrap(repeat_from_bytes, 0, ARGUMENT2_SIZE);
    	    		byte_buffer.order(ByteOrder.LITTLE_ENDIAN);
    	            repeat_from = byte_buffer.getInt();
        	    } break;
        	    
        	    case INSTRUCT_DELAY:
    	        	byte[] delay_bytes = new byte[ARGUMENT1_SIZE];
    	    	    System.arraycopy(buffer, ARGUMENT1_START, delay_bytes, 0, ARGUMENT1_SIZE);
    	    	    byte_buffer = ByteBuffer.wrap(delay_bytes, 0, ARGUMENT1_SIZE);
    	    		byte_buffer.order(ByteOrder.LITTLE_ENDIAN);
    	            delay = byte_buffer.getInt();
    	        break;
    	        
    	        default:
    	        break;
    	    }
    		
		    ret = 0;
		}
		
		return ret;
    }
}
