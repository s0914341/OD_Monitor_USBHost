package od_monitor.experiment;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import od_monitor.app.data.SensorDataComposition;

import android.util.Log;

public class ODCalculate {
	/** 
	 * Beer-Lambert equation
	 * c=(A x e) / b
	 * c is concentration, 
	 * A is the absorbance in AU, 
	 * e is the wavelength-dependent extinction coefficient in ng-cm/£gl,
	 * b is the pathlength 
	 */
	public final static String Tag = ODCalculate.class.getName();
	public static final double e_double_DNA = 50; //ng-cm/£gl, Double-stranded DNA
	public static final double e_single_DNA = 33; //ng-cm/£gl, Single-stranded DNA
	public static final double e_RNA = 40; //ng-cm/£gl, RNA
	
	public static final double[] Upscale_factors = new double[] {13000.0/10.0, 13000.0/30.9, 13000.0/78.7, 13000.0/260.0, 13000.0/549.0, 13000.0/1500.0, 13000.0/5100.0, 1.0};
	public static final double[] Adjecency_Channel_Ratio = new double[] {30.9/10, 78.7/30.9, 260/78.7, 549.0/260.0, 1500.0/549.0, 5100.0/1500.0, 13000.0/5100.0};
	public static final int Ref_OD_Count = 25;
	public static int Ref_OD_times = 0;
	public static double Ref_OD = 0.0;
	public double initial_OD600 = 0.0;
	public double pre_final_od = 0.0;
	
	
	public static byte[] parse_date(String s) {
		Pattern p = Pattern.compile("\\d+");
		Matcher m = p.matcher(s);
		int[] int_date = new int[8];
		byte[] byte_date = new byte[8];
		
		for (int i = 0; i < 8; i++) {
			if (m.find()) {
				int_date[i] = Integer.parseInt(m.group());
				byte_date[i] = (byte) (int_date[i]&0xff);
			} else {
				int_date = null;
			    break;
			}
		}
		
		return byte_date;
	}
	
	public static int[] parse_raw_data(String s) {
		//Pattern p = Pattern.compile("(\\d+)/(\\d+)/(\\d+) (\\d+):(\\d+):(\\d+)  index: (\\d+), (\\d+), (\\d+), (\\d+), (\\d+), (\\d+), (\\d+), (\\d+), (\\d+)");
		Pattern p = Pattern.compile("\\d+");
		Matcher m = p.matcher(s);
		int[] data = new int[SensorDataComposition.raw_total_sensor_data_size];
		
		for (int i = 0; i < SensorDataComposition.raw_total_sensor_data_size; i++) {
			if (m.find()) {
				data[i] = Integer.parseInt(m.group());
			} else {
			    data = null;
			    break;
			}
		}
		
		return data;
	}
	
	public double calculate_od(int[] data) {
		int ret = -1;
		double channel_ratio = 0;
		boolean ratio_check_ok = false;
		int channel_index = 0;
		int raw_data = 0;
		double upscale_data = 0;
		int max_val = 0;
		int channel_count = 0;
		double primitive_od = 0;
		double final_od = 0, mapped_od = 0;
		double[] upscale_raw_data = new double[SensorDataComposition.raw_total_sensor_channel];
		double[] channels_od = new double[SensorDataComposition.raw_total_sensor_channel];
		
        if (data.length == SensorDataComposition.raw_total_sensor_channel) {
        	channel_index = 0;
        	while (channel_index < SensorDataComposition.raw_total_sensor_channel) {
        		raw_data = data[channel_index];
        	    if ((channel_index > 0) && channel_index < SensorDataComposition.raw_total_sensor_channel) {
        		    if (data[channel_index-1] > 0) {
        	            channel_ratio = ((double)data[channel_index]/(double)data[channel_index-1])/Adjecency_Channel_Ratio[channel_index-1];
        	            if (channel_ratio > 0.9 && channel_ratio < 1.11) {
        	            	if ((max_val/data[channel_index]) > 1.5) {
        	        	        ratio_check_ok = false;
        	        	        channel_count = 0;
        	        	        primitive_od = 0;
        	        	        break;
        	                } else {
        	        	        ratio_check_ok = true;
        	        	        if (max_val < data[channel_index])
        	        	    	    max_val = data[channel_index];
        	                }
        	            } else {
        	            	ratio_check_ok = false;
        	            }
        		    }
        	    } else {
        	        if (data[channel_index] > data[channel_index+1])
        	        	ratio_check_ok = false;
        	        else
        	        	ratio_check_ok = true;
        	        max_val = raw_data;
        	    }
        	    
        	    if ((raw_data < 4010) && (raw_data > 80) && (ratio_check_ok == true)) 
        	    	upscale_raw_data[channel_index] = raw_data * Upscale_factors[channel_index];
        	    else
        	    	upscale_raw_data[channel_index] = 0;
        	    
        	    upscale_data = upscale_raw_data[channel_index];
        	    if (upscale_data > 0) {
        	    	channels_od[channel_index] =  Math.log10((4095 * Upscale_factors[0]) / upscale_data);
        	    	//channels_od[channel_index] = channels_od[channel_index]/Math.log10(10);
        	    	primitive_od = primitive_od + channels_od[channel_index];
        	    	channel_count++;
        	    } else {
        	    	channels_od[channel_index] = 0;
        	    }
        	    channel_index++;  
        	}
        	
        	if (channel_count > 0)
        		final_od = primitive_od/channel_count;
        } else {
         
        }
        
        if (Ref_OD_times < Ref_OD_Count) {
            Ref_OD = Ref_OD + final_od;
            Ref_OD_times++;
            if ( Ref_OD_times == Ref_OD_Count )
                Ref_OD = Ref_OD / Ref_OD_Count;
            final_od = 0;
        } else {
            if (channel_count > 0) {
                final_od = final_od - Ref_OD;
            } else {
            	int large_zero = 0;
            	for (int i = 0; i < data.length; i++) {
            		if (0 < data[i])
            			large_zero++;
            	}
            	
            	if ((large_zero > 0) && (data[0] == 0) && (data[1] == 0) && (data[2] == 0)) {
            		final_od = pre_final_od;
            	}
            }
        }
        
        pre_final_od = final_od;
        
//0.6143 * Final_OD - 0.5181 * Final_OD ^ 2 + 0.1981 * Final_OD ^ 3
        if (final_od >= 0) {
            mapped_od = 0.6143 * final_od - 0.5181 * Math.pow( final_od, 2 )  + 0.1981 * Math.pow( final_od , 3 );
        } else {
          //  mapped_od = initial_OD600 + Math.pow( -1, 2 * Math.random() + 1 ) * ( Math.floor( ( 3 * Math.random() ) + 1 ) ) * 0.01;
            mapped_od = initial_OD600 + (Math.random() > 0.5 ? 1 : -1) * ( Math.floor( ( 3 * Math.random() ) + 1 ) ) * 0.01;
        }
        
        return mapped_od;
	}
	
	public void initialize (double init_od) {
		Ref_OD_times = 0;
		Ref_OD = 0;
		pre_final_od = 0.0;
		initial_OD600 = init_od;
	}
}
