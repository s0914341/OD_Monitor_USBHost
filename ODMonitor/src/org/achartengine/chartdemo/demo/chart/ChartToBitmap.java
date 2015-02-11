package org.achartengine.chartdemo.demo.chart;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import od_monitor.app.R;
import od_monitor.app.data.SensorDataComposition;
import od_monitor.app.file.FileOperateBmp;
import od_monitor.app.file.FileOperateByteArray;

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
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.Toast;

public class ChartToBitmap {
	public String Tag = "ChartToBitmap";
	  /** The main dataset that includes all the series that go into a chart. */
	private XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();
	  /** The main renderer that includes all the renderers customizing a chart. */
	private XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
	  /** The most recently added series. */
	private TimeSeries mCurrentSeries = null;
	  /** The most recently created renderer, customizing the current series. */
    private XYSeriesRenderer mCurrentRenderer;
	  /** The chart view that displays the data. */
	private GraphicalView mChartView;
	public long current_index = -1;
	public int current_raw_index = -1;
	
	private long chart_start_time = System.currentTimeMillis();
	private long chart_end_time = chart_start_time + 20000;
	private static final String SERIES_NAME = "OD series ";
	private double chart_max_od_value = 0;
	private double chart_min_od_value = 0;
	
	public void ChartToBitmapFile(Context context) {
		// set some properties on the main renderer
	      mRenderer.setApplyBackgroundColor(true);
	      mRenderer.setBackgroundColor(Color.argb(100, 50, 50, 50));
	      mRenderer.setAxisTitleTextSize(16);
	      mRenderer.setChartTitleTextSize(20);
	      mRenderer.setLabelsTextSize(15);
	      mRenderer.setLegendTextSize(15);
	      mRenderer.setMargins(new int[] { 20, 30, 15, 0 });
	      // long now = Math.round(new Date().getTime() / DAY) * DAY;
	      //long now = new Date().getTime();
	    
	     // mRenderer.setRange(new double[] {now, now+60000, 0, 50});
	      //mRenderer.setZoomButtonsVisible(true);
	      mRenderer.setZoomEnabled(true);
	      mRenderer.setExternalZoomEnabled(true);
	      mRenderer.setInScroll(true);
	      mRenderer.setShowGrid(true);
	      mRenderer.setPointSize(8);
	      init_time_series();
	      
	      if (mChartView == null) {
	            //LinearLayout layout = (LinearLayout) findViewById(R.id.chart);
	            mChartView = ChartFactory.getTimeChartView(context, mDataset, mRenderer, "MM/dd h:mm:ss a");
	            //mChartView = ChartFactory.getLineChartView(this, mDataset, mRenderer);
	            // enable the chart click events
	            mRenderer.setClickEnabled(true);
	            mRenderer.setSelectableBuffer(10);
	            mChartView.setOnClickListener(new View.OnClickListener() {
	            public void onClick(View v) {
	            // handle the click event on the chart
	                SeriesSelection seriesSelection = mChartView.getCurrentSeriesAndPoint();
	                if (seriesSelection == null) {
	                   // Toast.makeText(ODChartBuilder.this, "No chart element", Toast.LENGTH_SHORT).show();
	                } else {
	                    // display information of the clicked point
	                	Date date = new Date((long)seriesSelection.getXValue());
	                	SimpleDateFormat date_format = new SimpleDateFormat("MM/dd h:mm:ss a");
	                }
	            }
	            });
	            mChartView.layout(0, 0, 1024, 768);
	            //mChartView.repaint();
	            FileOperateBmp write_file = new FileOperateBmp("od_chart", "chart", "png");
	      		  try {
	      			  write_file.create_file(write_file.generate_filename());
	      		  } catch (IOException e) {
	      			  // TODO Auto-generated catch block
	      			  e.printStackTrace();
	      		  }
	      		  write_file.write_file(getViewBitmap(mChartView), Bitmap.CompressFormat.PNG, 100);
	      		  write_file.flush_close_file();
	         //   layout.addView(mChartView, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
	            boolean enabled = mDataset.getSeriesCount() > 0;
	        } else {
	            mChartView.repaint();
	        }
	}
	
	public void CreateNewSeries() {
        String seriesTitle = SERIES_NAME + (mDataset.getSeriesCount() + 1);
        // create a new series of data
        TimeSeries series = new TimeSeries(seriesTitle);
        // XYSeries series = new XYSeries(seriesTitle);
        mDataset.addSeries(series);
        mCurrentSeries = series;
        // create a new renderer for the new series
        XYSeriesRenderer renderer = new XYSeriesRenderer();
        mRenderer.addSeriesRenderer(renderer);
        // set some renderer properties
        renderer.setColor(Color.argb(255, 0, 255, 0));
        renderer.setPointStyle(PointStyle.CIRCLE);
        renderer.setFillPoints(true);
        renderer.setDisplayChartValues(true);
        // renderer.setDisplayChartValuesDistance(10);
        mCurrentRenderer = renderer;
       // mChartView.repaint();
    }
	
	public void refresh_current_view_range(Date x, double y) {
		  double XMax = mRenderer.getXAxisMax();
		  double XMin = mRenderer.getXAxisMin();
		  double thread = XMin+(XMax-XMin)*8.0/10.0;
		  if (thread < x.getTime()) {
			  //double refresh_XMin = XMin+(XMax-XMin)*7.0/10.0;
			  
			  double refresh_XMin = x.getTime()-((XMax-XMin)*3.0/10.0);
			  mRenderer.setXAxisMin(refresh_XMin);
			  mRenderer.setXAxisMax(refresh_XMin+(XMax-XMin));
		  }
	  }
	
	public void init_time_series() {
        Date date = null;
        double od_value = 0;
        long size = 0;
        
   
        FileOperateByteArray read_file = new FileOperateByteArray(SensorDataComposition.sensor_raw_folder_name, SensorDataComposition.sensor_raw_file_name, true);
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
		         if (mCurrentSeries == null) {
		        	 chart_start_time = date.getTime();
		             mRenderer.setRange(new double[] {chart_start_time, chart_start_time+20000, od_value-2, od_value+2});
				     CreateNewSeries();
		         }
		         
		         chart_end_time = date.getTime();
		         if (od_value > chart_max_od_value)
		        	 chart_max_od_value = od_value;
		         else if (od_value < chart_min_od_value)
		        	 chart_min_od_value = od_value;
		            
		         current_raw_index = one_sensor_data.get_sensor_get_index();
		         if (1 == current_raw_index) {
		        	 mRenderer.setRange(new double[] {chart_start_time, chart_start_time+(chart_end_time-chart_start_time)*10, od_value-2, od_value+2});
		         }
			     mCurrentSeries.add(date, od_value);
			}
		    
			 refresh_current_view_range(date, od_value); 
		} else {
			if (mCurrentSeries == null) {
			    long init_date = new Date().getTime();
	            mRenderer.setRange(new double[] {init_date, init_date+20000, -2, 2});
	            CreateNewSeries();
	            chart_start_time = System.currentTimeMillis();
	            mCurrentSeries.add(new Date(chart_start_time), 0);
	            mCurrentSeries.add(new Date(chart_start_time+1000), 1);
	            mCurrentSeries.add(new Date(chart_start_time+2000), 2);
	            mCurrentSeries.add(new Date(chart_start_time+3000), 3);
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
