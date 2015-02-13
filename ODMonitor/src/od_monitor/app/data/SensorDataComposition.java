package od_monitor.app.data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SensorDataComposition implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5176695259056597839L;
	public static final String sensor_raw_file_name = "sensor_experiment_data";
	public static final String sensor_raw_folder_name = "sensor_od";
	
	public static final int raw_sensor_index_index = 0;
	public static final int raw_sensor_ch1_index = 1;
	public static final int raw_sensor_ch2_index = 2;
	public static final int raw_sensor_ch3_index = 3;
	public static final int raw_sensor_ch4_index = 4;
	public static final int raw_sensor_ch5_index = 5;
	public static final int raw_sensor_ch6_index = 6;
	public static final int raw_sensor_ch7_index = 7;
	public static final int raw_sensor_ch8_index = 8;
	
	public static final int raw_total_sensor_channel = 8;
	public static final int raw_total_sensor_data_size = raw_sensor_ch8_index+1;
	
	private static final int sensor_get_index = 0;
	private static final int sensor_measurement_time_index = 4;
	private static final int sensor_index_index = 12;
	private static final int sensor_ch1_index = 16;
	private static final int sensor_ch2_index = 20;
	private static final int sensor_ch3_index = 24;
	private static final int sensor_ch4_index = 28;
	private static final int sensor_ch5_index = 32;
	private static final int sensor_ch6_index = 36;
	private static final int sensor_ch7_index = 40;
	private static final int sensor_ch8_index = 44;
	private static final int sensor_od_value_index = 48;
	private static final int data_valid_index = 56;
	
	public static final int total_size = 60;
	
	public byte[] buffer = new byte[total_size];
	
	public SensorDataComposition () {
		set_data_valid(true);
		set_sensor_measurement_time(0);
		set_sensor_get_index(-1);
	}
	
	public boolean is_data_valid() {
		boolean data_valid = true;
		
		ByteBuffer byte_buffer = ByteBuffer.wrap(buffer, data_valid_index, 4);
        if (1 != byte_buffer.getInt()) {
        	data_valid = false; 
        }
		
		return data_valid;
	}
	
	public void set_data_valid(boolean valid) {
		int data_valid = 1;
		
		if (!valid) {
			data_valid = 0;
		}
		
		byte[] data_valid_bytes = ByteBuffer.allocate(4).putInt(data_valid).array();
		System.arraycopy(data_valid_bytes, 0, buffer, data_valid_index, 4);
	}
	
	public static double od_round(double value, int places) {
	    if (places < 0) throw new IllegalArgumentException();

	    BigDecimal bd = new BigDecimal(value);
	    bd = bd.setScale(places, RoundingMode.HALF_UP);
	    return bd.doubleValue();
	}
	
	public int set_buffer(byte[] indata, int indata_start_pos, int len) {
		int retval = -1;
		
		if (len <= (total_size)) {
		    System.arraycopy(indata, indata_start_pos, buffer, 0, len);
		    retval = 0;
		}
		
		return retval;
	}
	
	public int[] get_channel_data() {
        int[] channel_data = new int[raw_total_sensor_channel];
        
        for (int i = 0; i < raw_total_sensor_channel; i++) {
        	channel_data[i] = ByteBuffer.wrap(buffer, sensor_ch1_index+(i*4), 4).getInt();
        }
		
		return channel_data;
	}
	
	public double get_sensor_od_value() {
		ByteBuffer byte_buffer = ByteBuffer.wrap(buffer, sensor_od_value_index, 8);
        return byte_buffer.getDouble();
	}
	
	public String get_sensor_od_value_string() {
		String str = "NA";
		if (is_data_valid())
		    str = Double.toString(get_sensor_od_value());
		return str;
	}
	
	public String get_sensor_measurement_time_string() {
		String str = "NA";
		if (0 != get_sensor_measurement_time()) {
			Date date = new Date(get_sensor_measurement_time());
		    SimpleDateFormat sdf = new SimpleDateFormat("MM/dd HH:mm:ss");
		    str = sdf.format(date);
		}
		return str;
	}
	
	public String get_sensor_get_index_string() {
		String str = "NA";
		
		if (-1 != get_sensor_get_index())
		    str = Integer.toString(get_sensor_get_index());
		
		return str;
	}
	
	public int get_sensor_get_index() {
		ByteBuffer byte_buffer = ByteBuffer.wrap(buffer, sensor_get_index, 4);
        return byte_buffer.getInt();
	}

	public long get_sensor_measurement_time() {
		ByteBuffer byte_buffer = ByteBuffer.wrap(buffer, sensor_measurement_time_index, 8);
        return byte_buffer.getLong();
	}
	
	public int get_sensor_index() {
		ByteBuffer byte_buffer = ByteBuffer.wrap(buffer, sensor_index_index, 4);
        return byte_buffer.getInt();
	}
	
	public void set_sensor_get_index(int index) {
		byte[] index_bytes = ByteBuffer.allocate(4).putInt(index).array();
		System.arraycopy(index_bytes, 0, buffer, sensor_get_index, 4);
	}
	
	public void set_sensor_measurement_time(long time) {
		byte[] time_bytes = ByteBuffer.allocate(8).putLong(time).array();
		System.arraycopy(time_bytes, 0, buffer, sensor_measurement_time_index, 8);
	}
	
	public void set_sensor_index(int index) {
		byte[] index_bytes = ByteBuffer.allocate(4).putInt(index).array();
		System.arraycopy(index_bytes, 0, buffer, sensor_index_index, 4);
	}
	
	public void set_sensor_od_value(double od_value) {
		od_value = od_round(od_value, 3);
		byte[] od_value_bytes = ByteBuffer.allocate(8).putDouble(od_value).array();
		System.arraycopy(od_value_bytes, 0, buffer, sensor_od_value_index, 8);
	}
	
	public int set_raw_sensor_data(int[] raw_data) {
		int ret = 0;
		
		if (raw_data.length == raw_total_sensor_data_size) {
			for (int i = 0; i < raw_total_sensor_data_size; i++) {
			    byte[] raw_bytes = ByteBuffer.allocate(4).putInt(raw_data[i]).array();
			    System.arraycopy(raw_bytes, 0, buffer, sensor_index_index+(i*4), 4);
			}
		} else {
			ret = -1;
		}
		
		return ret;
	}
	
	public String get_sensor_data_string() {
		int[] channel_data = get_channel_data();
		String sensor_string = String.format("%d, %d, %d, %d, %d, %d, %d, %d, %d, %d, %d\n",  get_sensor_get_index(), get_sensor_measurement_time(), get_sensor_index(),
				channel_data[0], channel_data[1], channel_data[2], channel_data[3], channel_data[4], channel_data[5], channel_data[6], channel_data[7]);
		
		return sensor_string;
	}
	
	public String[] get_channel_data_string_array() {
		String[] channel_data_string = {"NA", "NA", "NA", "NA", "NA", "NA", "NA", "NA"};
		if (is_data_valid()) {
			int[] channel_data = get_channel_data();
			for (int i = 0; i < channel_data.length; i++) {
			    channel_data_string[i] = Integer.toString(channel_data[i]);
			}
		}
		
		return channel_data_string;
	}
}
