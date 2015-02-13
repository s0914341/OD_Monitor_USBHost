package org.achartengine.chartdemo.demo.chart;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import od_monitor.app.ODMonitorActivity;
import od_monitor.app.R;
import od_monitor.app.data.SensorDataComposition;
import od_monitor.app.file.FileOperateBmp;
import od_monitor.app.file.FileOperateByteArray;
import od_monitor.experiment.ExperimentalOperationInstruct;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.SeriesSelection;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.Toast;

public class ODChartToBitmap {
	public final static String Tag = ODChartToBitmap.class.getName();
	  /** The main dataset that includes all the series that go into a chart. */
	private XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();
	  /** The main renderer that includes all the renderers customizing a chart. */
	private XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
	  /** The most recently added series. */
	private TimeSeries[] mCurrentSeries = {null, null, null, null};
	  /** The most recently created renderer, customizing the current series. */
    private XYSeriesRenderer mCurrentRenderer;
	  /** The chart view that displays the data. */
	private GraphicalView mChartView;
	public long current_index = -1;
	public int[] current_raw_index = {-1, -1, -1, -1};
	
	private long chart_start_time = System.currentTimeMillis();
	private long chart_end_time = chart_start_time + 20000;
	private static final String SERIES_NAME = "OD series ";
	private double chart_max_od_value = 0;
	private double chart_min_od_value = 0;
	private String file_name;
	private String file_dir;
	
	public ODChartToBitmap(Context context, int left, int top, int right, int bottom) {
		// set some properties on the main renderer
	      mRenderer.setApplyBackgroundColor(true);
	      mRenderer.setBackgroundColor(Color.argb(255, 0, 0, 0));
	      mRenderer.setAxisTitleTextSize(16);
	      mRenderer.setChartTitleTextSize(20);
	      mRenderer.setLabelsTextSize(15);
	      mRenderer.setLegendTextSize(15);
	      mRenderer.setMargins(new int[] { 20, 30, 15, 0 });
	      mRenderer.setZoomEnabled(true);
	      mRenderer.setExternalZoomEnabled(true);
	      mRenderer.setInScroll(true);
	      mRenderer.setShowGrid(true);
	      mRenderer.setPointSize(8);
	      for (int i = 0; i < ExperimentalOperationInstruct.EXPERIMENT_MAX_SENSOR_COUNT; i++)
	          init_od_series(i);
	      
	      double XMax = 0, XMin = 0, YMax = 0, YMin = 0;
	      int have_item_count = 0;
	      for (int i = 0; i < ExperimentalOperationInstruct.EXPERIMENT_MAX_SENSOR_COUNT; i++) {
	    	  if (mCurrentSeries[i].getItemCount() > 0) {
	    		  if (mCurrentSeries[i].getMaxY() > YMax)
	    	          YMax = mCurrentSeries[i].getMaxY();
	    		  
	    		  if (mCurrentSeries[i].getMaxX() > XMax)
	    	          XMax = mCurrentSeries[i].getMaxX();
	    		  
	    		  if (mCurrentSeries[i].getMinY() > YMin)
	    	          YMin = mCurrentSeries[i].getMinY();
	    		  
	    		  if (mCurrentSeries[i].getMinX() > XMin)
	    	          XMin = mCurrentSeries[i].getMinX();
	    		  
	    		  have_item_count++;
	    	  }  
	      }
	      
	      if (have_item_count > 1) {
	          double margin = 0;
	          margin = (YMax - YMin)/10;
	          if (0 == margin)
		          margin = 1;
	          
	          if (0 == (XMax-XMin)) {
	        	  mRenderer.setYAxisMin(YMin - margin);
	    		  mRenderer.setYAxisMax(YMax + margin);
	          } else {
	              mRenderer.setRange(new double[] {XMin, XMax, YMin - margin , YMax + margin});
	          }
	      }
	      
	      mChartView = ChartFactory.getTimeChartView(context, mDataset, mRenderer, "MM/dd h:mm:ss a");
	      mChartView.layout(left, top, right, bottom);
	     
	      FileOperateBmp write_file = new FileOperateBmp("od_chart", "chart", "png");
	      file_dir = write_file.get_file_dir();
	      try {
	    	  file_name = write_file.generate_filename();
	          write_file.create_file(file_name);
	          write_file.write_file(getViewBitmap(mChartView), Bitmap.CompressFormat.PNG, 100);
      		  write_file.flush_close_file();
	      } catch (IOException e) {
	      	  // TODO Auto-generated catch block
	      	  e.printStackTrace();
	      }
	}
	
	public String get_file_dir() {
		return file_dir;
	}
	
	public String get_file_name() {
		return file_name;
	}
	
	public void CreateNewSeries(int sensor_num) {
        String seriesTitle = SERIES_NAME + (sensor_num + 1);
        // create a new series of data
        TimeSeries series = new TimeSeries(seriesTitle);
        // XYSeries series = new XYSeries(seriesTitle);
        mDataset.addSeries(series);
        mCurrentSeries[sensor_num] = series;
        // create a new renderer for the new series
        XYSeriesRenderer renderer = new XYSeriesRenderer();
        mRenderer.addSeriesRenderer(renderer);
        // set some renderer properties
        renderer.setColor(ODChartBuilder.SERIES_COLOR[sensor_num]);
        renderer.setPointStyle(PointStyle.CIRCLE);
        renderer.setFillPoints(true);
        renderer.setDisplayChartValues(true);
        // renderer.setDisplayChartValuesDistance(10);
        mCurrentRenderer = renderer;
    }
	
	public void refresh_current_view_range(Date x, double y) {
		  double XMax = mRenderer.getXAxisMax();
		  double XMin = mRenderer.getXAxisMin();
		  double YMax = mRenderer.getYAxisMax();
		  double YMin = mRenderer.getYAxisMin();
		  double threadX = XMin+(XMax-XMin)*8.0/10.0;
		  if (threadX < x.getTime()) {
			  //double refresh_XMin = XMin+(XMax-XMin)*7.0/10.0;
			  
			  double refresh_XMin = x.getTime()-((XMax-XMin)*3.0/10.0);
			  mRenderer.setXAxisMin(refresh_XMin);
			  mRenderer.setXAxisMax(refresh_XMin+(XMax-XMin));
		  }
		  
		  if ((y > YMax) || (y < YMin)) {
			  mRenderer.setYAxisMin(y-2);
			  mRenderer.setYAxisMax(y+2);
		  }
	}
	
	public void init_od_series(int sensor_num) {
        Date date = null;
        double od_value = 0;
        long size = 0;
        
        String file_name = SensorDataComposition.sensor_raw_file_name + (sensor_num+1);
        FileOperateByteArray read_file = new FileOperateByteArray(SensorDataComposition.sensor_raw_folder_name, file_name, true);
        try {
        	size = read_file.open_read_file(read_file.generate_filename_no_date());
        } catch (IOException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        }
        
        if (size >= SensorDataComposition.total_size) {
			int offset = 0;
		    byte[] data = new byte[(int) size];
		    read_file.read_file(data);
		    SensorDataComposition one_sensor_data = new SensorDataComposition();
			
			while ((size-offset) >= SensorDataComposition.total_size) {
				 one_sensor_data.set_buffer(data, offset, SensorDataComposition.total_size);
				 date = new Date(one_sensor_data.get_sensor_measurement_time());	 
				 offset += SensorDataComposition.total_size;
				 
				 od_value = one_sensor_data.get_sensor_od_value();    
		        // od_value = OD_calculate.calculate_od(one_sensor_data.get_channel_data());      
		         if (mCurrentSeries[sensor_num] == null) {
		        	 chart_start_time = date.getTime();
		             mRenderer.setRange(new double[] {chart_start_time, chart_start_time+20000, od_value-2, od_value+2});
				     CreateNewSeries(sensor_num);
		         }
		         
		         chart_end_time = date.getTime();
		         if (od_value > chart_max_od_value)
		        	 chart_max_od_value = od_value;
		         else if (od_value < chart_min_od_value)
		        	 chart_min_od_value = od_value;
		            
		         current_raw_index[sensor_num] = one_sensor_data.get_sensor_get_index();
		         if (1 == current_raw_index[sensor_num]) {
		        	 mRenderer.setRange(new double[] {chart_start_time, chart_start_time+(chart_end_time-chart_start_time)*10, od_value-2, od_value+2});
		         }
		         mCurrentSeries[sensor_num].add(date, od_value);
			}
		    
			 refresh_current_view_range(date, od_value); 
		} else {
			if (mCurrentSeries[sensor_num] == null) {
			    long init_date = new Date().getTime();
	            mRenderer.setRange(new double[] {init_date, init_date+20000, -2, 2});
	            CreateNewSeries(sensor_num);
	        }
		}
    }
	
	public Bitmap getViewBitmap(View view)
	{
	    //Get the dimensions of the view so we can re-layout the view at its current size
	    //and create a bitmap of the same size 
	    int width = view.getWidth();
	    int height = view.getHeight();

	    int measuredWidth = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY);
	    int measuredHeight = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY);

	    //Cause the view to re-layout
	    view.measure(measuredWidth, measuredHeight);
	    view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());

	    //Create a bitmap backed Canvas to draw the view into
	    Bitmap b = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
	    Canvas c = new Canvas(b);

	    //Now that the view is laid out and we have a canvas, ask the view to draw itself into the canvas
	    view.draw(c);

	    return b;
	}
}
