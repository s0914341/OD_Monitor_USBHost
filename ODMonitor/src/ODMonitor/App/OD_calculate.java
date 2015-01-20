package ODMonitor.App;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.util.Log;

public class OD_calculate {
	/** 
	 * Beer-Lambert equation
	 * c=(A x e) / b
	 * c is concentration, 
	 * A is the absorbance in AU, 
	 * e is the wavelength-dependent extinction coefficient in ng-cm/£gl,
	 * b is the pathlength 
	 */
	public static String Tag = "OD_calculate";
	public static final double e_double_DNA = 50; //ng-cm/£gl, Double-stranded DNA
	public static final double e_single_DNA = 33; //ng-cm/£gl, Single-stranded DNA
	public static final double e_RNA = 40; //ng-cm/£gl, RNA
	
	public static final int pre_raw_index_index = 0;
	public static final int current_raw_index_index = 1;
	public static final int experiment_seconds_index = 2;
	public static final int sensor_index_index = 3;
	public static final int sensor_ch1_index = 4;
	public static final int sensor_ch2_index = 5;
	public static final int sensor_ch3_index = 6;
	public static final int sensor_ch4_index = 7;
	public static final int sensor_ch5_index = 8;
	public static final int sensor_ch6_index = 9;
	public static final int sensor_ch7_index = 10;
	public static final int sensor_ch8_index = 11;
	
	public static final int total_sensor_channel = 8;
	public static final int experiment_data_size = 12;
	
	public static final double[] Upscale_factors = new double[] {13000/10, 13000/30.9, 13000/78.7, 13000/260, 13000/549, 13000/1500, 13000/5100, 1};
	public static final double[] Adjecency_Channel_Ratio = new double[] {30.9/10, 78.7/30.9, 260/78.7, 549/260, 1500/549, 5100/1500, 13000/5100};
	public static final int Ref_OD_Count = 25;
	public static int Ref_OD_times = 0;
	public static double Ref_OD = 0.0;
	
	
	
	public static double sample_OD_value(double I1, double I2) {
        double ODvalue = 0;
        
		ODvalue = (-1)*Math.log10(I2/I1);
		return ODvalue;
	}
	
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
		int[] data = new int[experiment_data_size];
		
		for (int i = 0; i < experiment_data_size; i++) {
			if (m.find()) {
				data[i] = Integer.parseInt(m.group());
			} else {
			    data = null;
			    break;
			}
		}
		
		return data;
	}
	
	public static double calculate_od(int[] data) {
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
		double[] upscale_raw_data = new double[total_sensor_channel];
		double[] channels_od = new double[total_sensor_channel];
		
		
        if (data.length == total_sensor_channel) {
        	channel_index = 0;
        	while (channel_index < total_sensor_channel) {
        		raw_data = data[channel_index];
        	    if ((channel_index > 0) && channel_index < total_sensor_channel) {
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
        
        if ( Ref_OD_times < Ref_OD_Count ) {
          Ref_OD = Ref_OD + final_od;
          Ref_OD_times++;
          if ( Ref_OD_times == Ref_OD_Count )
            Ref_OD = Ref_OD / Ref_OD_Count;
          final_od = 0;
        }
        else {
           if (channel_count > 0)
             final_od = final_od - Ref_OD;
        }
        
//0.6143 * Final_OD - 0.5181 * Final_OD ^ 2 + 0.1981 * Final_OD ^ 3
        if ( final_od >= 0) 
          mapped_od = 0.6143 * final_od - 0.5181 * Math.pow( final_od, 2 )  + 0.1981 * Math.pow( final_od , 3 );
        else
           mapped_od = initial_OD600 + Math.pow( -1, 2 * Math.random() ) + 1 ) * ( int ( ( 3 * Math.random() ) + 1 ) ) * 0.01
        return final_od;
	}
	
	public static void initialize () {
		Ref_OD_times = 0;
		Ref_OD = 0;
		initial_OD600 = 0;
	}
}
