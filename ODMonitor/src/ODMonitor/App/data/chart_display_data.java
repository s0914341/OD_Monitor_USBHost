package ODMonitor.App.data;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Date;

public class chart_display_data implements Serializable{
	/**
	 * we need define a serial version UID value myself, 
	 * avoid different JVM version which general different UID value,
	 * the reason will possible cause deserialize fail. 
	 */
	private static final long serialVersionUID = 6216079271089948708L;
	protected long index;
	protected long date;
	protected double concentration;
	
	private static final int index_length = Long.SIZE/8;
	private static final int date_length = Long.SIZE/8;
	private static final int concentration_length = Double.SIZE/8;
	
	private static final int total_length = index_length+date_length+concentration_length;
	
	public chart_display_data() {

	}
	
	public chart_display_data(long local_index, double local_concentration) {
		date = new Date().getTime();
		concentration = local_concentration;
		index = local_index;
	}
	
	public long get_index_value() {
		return index;
	}
	
	public long get_date_value() {
		return date;
	}
	
	public double get_concentration_value() {
		return concentration;
	}
	
	public void set_index_value(long data) {
		index = data;
	}
	
	public void set_date_value(long data) {
        date = data;;
	}
	
	public void set_concentration_value(double data) {
		concentration = data;
	}
	
	public static int get_total_length() {
		return total_length;
	}
	
	public byte[] get_object_buffer() {
		byte[] index_bytes = ByteBuffer.allocate(index_length).putLong(index).array();
		byte[] date_bytes = ByteBuffer.allocate(date_length).putLong(date).array();
		byte[] concentration_bytes = ByteBuffer.allocate(concentration_length).putDouble(concentration).array();
		byte[] combined = new byte[index_bytes.length + date_bytes.length + concentration_bytes.length];
		int offset = 0;

		System.arraycopy(index_bytes, 0, combined, offset, index_bytes.length);
		offset += index_bytes.length;
		System.arraycopy(date_bytes, 0, combined, offset, date_bytes.length);
		offset += date_bytes.length;
		System.arraycopy(concentration_bytes, 0, combined, offset, concentration_bytes.length);
		offset += concentration_bytes.length;
		
		return combined;
	}
	
	public int set_object_buffer(byte[] buf) {
		byte[] index_bytes = new byte[index_length];
		byte[] date_bytes = new byte[date_length];
		byte[] concentration_bytes = new byte[concentration_length];
		int offset = 0;
		int ret = -1;
		
		if (buf.length == total_length) {
		    System.arraycopy(buf, offset, index_bytes, 0, index_length);
		    offset += index_length;
		    index = ByteBuffer.wrap(index_bytes, 0, index_length).getLong();
		    
		    System.arraycopy(buf, offset, date_bytes, 0, date_length);
		    offset += date_length;
		    date = ByteBuffer.wrap(date_bytes, 0, date_length).getLong();
		    
		    System.arraycopy(buf, offset, concentration_bytes, 0, concentration_length);
		    offset += concentration_length;
		    concentration = ByteBuffer.wrap(concentration_bytes, 0, concentration_length).getDouble();
		    
		    ret = 0;
		}
		
		return ret;
	}
}
