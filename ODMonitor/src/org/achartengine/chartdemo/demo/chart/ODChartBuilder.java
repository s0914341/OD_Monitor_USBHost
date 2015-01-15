/**
 * Copyright (C) 2009 - 2013 SC 4ViewSoft SRL
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.achartengine.chartdemo.demo.chart;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Date;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.SeriesSelection;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import ODMonitor.App.ODMonitorActivity;
import ODMonitor.App.ODMonitor_Application;
import ODMonitor.App.OD_calculate;
import ODMonitor.App.R;
import ODMonitor.App.data.android_accessory_packet;
import ODMonitor.App.data.chart_display_data;
import ODMonitor.App.data.experiment_script_data;
import ODMonitor.App.data.machine_information;
import ODMonitor.App.data.sync_data;
import ODMonitor.App.file.file_operate_bmp;
import ODMonitor.App.file.file_operate_byte_array;
import ODMonitor.App.file.file_operation;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ODChartBuilder extends Activity {
  public String Tag = "XYChartBuilder";
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
  public data_read_thread data_read_thread;
  public long current_index = -1;
  public int current_raw_index = -1;
  
  private static final String SERIES_NAME = "OD series ";
  private static final long SECOND = 1000;
  private static final long MINUTE = 60*SECOND;
  private static final long HOUR = 60*MINUTE;
  private static final long DAY = 24*HOUR;
  private static final int HOURS = 24;
  
  public sync_data sync_chart_notify;
  private boolean data_read_thread_run = false;
  public TextView debug_view;
  private ImageButton zoom_in_button;
  private ImageButton zoom_out_button;
  private ImageButton zoom_fit_button;
  private ImageButton save_chart_button;
  private long chart_start_time = new Date().getTime();
  private long chart_end_time = chart_start_time + 20000;
  private double chart_max_od_value = 0;
  private double chart_min_od_value = 0;


  @Override
  protected void onSaveInstanceState(Bundle outState) {
      super.onSaveInstanceState(outState);
      // save the current data, for instance when changing screen orientation
      outState.putSerializable("dataset", mDataset);
      outState.putSerializable("renderer", mRenderer);
      outState.putSerializable("current_series", mCurrentSeries);
      outState.putSerializable("current_renderer", mCurrentRenderer);
  }

  @Override
  protected void onRestoreInstanceState(Bundle savedState) {
      super.onRestoreInstanceState(savedState);
      // restore the current data, for instance when changing the screen
      // orientation
      mDataset = (XYMultipleSeriesDataset) savedState.getSerializable("dataset");
      mRenderer = (XYMultipleSeriesRenderer) savedState.getSerializable("renderer");
      mCurrentSeries = (TimeSeries) savedState.getSerializable("current_series");
      mCurrentRenderer = (XYSeriesRenderer) savedState.getSerializable("current_renderer");
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
	  Log.d(Tag, "on Create");
      super.onCreate(savedInstanceState);
      setContentView(R.layout.chart_layout);
      getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
      Thread.currentThread().setName("Thread_XYChartBuilder");
      
      debug_view = (TextView)findViewById(R.id.DebugView);
      
      ODMonitor_Application app_data = ((ODMonitor_Application)this.getApplication());
	  sync_chart_notify = app_data.get_sync_chart_notify();
      // set some properties on the main renderer
      mRenderer.setApplyBackgroundColor(true);
      mRenderer.setBackgroundColor(Color.argb(100, 50, 50, 50));
      mRenderer.setAxisTitleTextSize(16);
      mRenderer.setChartTitleTextSize(20);
      mRenderer.setLabelsTextSize(15);
      mRenderer.setLegendTextSize(15);
      mRenderer.setMargins(new int[] { 20, 30, 15, 0 });
      // long now = Math.round(new Date().getTime() / DAY) * DAY;
      long now = new Date().getTime();
    
      mRenderer.setRange(new double[] {now, now+60000, 0, 50});
      //mRenderer.setZoomButtonsVisible(true);
      mRenderer.setZoomEnabled(true);
      mRenderer.setExternalZoomEnabled(true);
      mRenderer.setInScroll(true);
      mRenderer.setShowGrid(true);
      mRenderer.setPointSize(8);
      
      zoom_in_button = (ImageButton) findViewById(R.id.zoomIn);
      zoom_in_button.setOnClickListener(new View.OnClickListener() {
          public void onClick(View v) {
    	      mChartView.zoomIn();
      	  }
	  });
      
      zoom_out_button = (ImageButton) findViewById(R.id.zoomOut);
      zoom_out_button.setOnClickListener(new View.OnClickListener() {
          public void onClick(View v) {
    	      mChartView.zoomOut();
      	  }
	  });
      
      zoom_fit_button = (ImageButton) findViewById(R.id.zoomFit);
      zoom_fit_button.setOnClickListener(new View.OnClickListener() {
          public void onClick(View v) {
        	  double margin = 0;
        	  if (chart_start_time >= chart_end_time)
        		  chart_end_time = chart_start_time + 20000;
        	  
        	  margin = (chart_max_od_value - chart_min_od_value)/10;
        	  mRenderer.setRange(new double[] {chart_start_time, chart_end_time, chart_min_od_value - margin , chart_max_od_value + margin});
        	  mChartView.repaint();
    	      //mChartView.zoomReset();
      	  }
	  });
      
      save_chart_button = (ImageButton) findViewById(R.id.saveChart);
      save_chart_button.setOnClickListener(new View.OnClickListener() {
          public void onClick(View v) {
              file_operate_bmp write_file = new file_operate_bmp("od_chart", "chart", "png");
      		  try {
      			  write_file.create_file(write_file.generate_filename_no_date());
      		  } catch (IOException e) {
      			  // TODO Auto-generated catch block
      			  e.printStackTrace();
      		  }
      		  write_file.write_file(mChartView.toBitmap(), Bitmap.CompressFormat.PNG, 100);
      		  write_file.flush_close_file();
      		  Toast.makeText(ODChartBuilder.this, "Save chart success!", Toast.LENGTH_SHORT).show(); 
      	  }
	  });
    
      init_time_series();
      //read_thread();
      data_read_thread = new data_read_thread(handler);
      data_read_thread_run = true;
      data_read_thread.start();
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

  public void SerialAdd(Date x, double y) {
      // add a new data point to the current series
	  refresh_current_view_range(x, y);
      mCurrentSeries.add(x, y);
      // repaint the chart such as the newly added point to be visible
      mChartView.repaint();
  }
  
  final Handler handler =  new Handler() {
  	@Override 
  	public void handleMessage(Message msg) {
  		int current_index = msg.getData().getInt("current_raw_index");
  		int pre_index = msg.getData().getInt("new_pre_raw_index");
    	
  		chart_display_data objectRcvd = (chart_display_data)msg.getData().getSerializable("chart");
  		
  		if (objectRcvd.get_index_value() == 0) {
  			chart_start_time = objectRcvd.get_date_value();
  			mRenderer.setRange(new double[] {chart_start_time, chart_start_time+20000, objectRcvd.get_concentration_value()-2, objectRcvd.get_concentration_value()+2});
  			CreateNewSeries();
  		    mCurrentSeries.add(new Date(objectRcvd.get_date_value()), objectRcvd.get_concentration_value());
  		} else {
  		    SerialAdd(new Date(objectRcvd.get_date_value()), objectRcvd.get_concentration_value());
  		}
  		
  		chart_end_time = objectRcvd.get_date_value();
  		if (objectRcvd.get_concentration_value() > chart_max_od_value)
       	 chart_max_od_value = objectRcvd.get_concentration_value();
  		else if (objectRcvd.get_concentration_value() < chart_min_od_value)
       	 chart_min_od_value = objectRcvd.get_concentration_value();
  		
  		String str = String.format("handler test current_index:%d, new pre index:%d, concentration:%f", current_index, pre_index, objectRcvd.get_concentration_value());
    	debug_view.setText(str);
  	
		Log.d(Tag, "data_read_thread handler id:"+Thread.currentThread().getId() + "process:" + android.os.Process.myTid());
  	}
  };
  
  
  private class data_read_thread  extends Thread {
		Handler mHandler;
		
		data_read_thread(Handler h){
			mHandler = h;
		}
		
		public void run() {
			Bundle b = new Bundle(1);
			long size = 0;
			
			while (data_read_thread_run) {
				Log.d(Tag, "data_read_thread  id:"+Thread.currentThread().getId() + "process:" + android.os.Process.myTid());
				synchronized (sync_chart_notify) {
				    try {
				    	sync_chart_notify.wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				file_operate_byte_array read_file = new file_operate_byte_array("od_sensor", "sensor_offline_byte", true);
		        try {
		        	size = read_file.open_read_file(read_file.generate_filename_no_date());
		        } catch (IOException e) {
			        // TODO Auto-generated catch block
			        e.printStackTrace();
		        }
		        
		        read_file.seek_read_file(size-(long)(4*OD_calculate.experiment_data_size));
    	        byte[] new_pre_raw_index_bytes = new byte[4];
    	        read_file.read_file(new_pre_raw_index_bytes);
    	        ByteBuffer byte_buffer = ByteBuffer.wrap(new_pre_raw_index_bytes, 0, 4);
                byte_buffer = ByteBuffer.wrap(new_pre_raw_index_bytes, 0, 4);
                int new_pre_raw_index = byte_buffer.getInt();
		        
                if (new_pre_raw_index == current_raw_index) {
                	double od_value = 0;
                	try {
    		        	size = read_file.open_read_file(read_file.generate_filename_no_date());
    		        } catch (IOException e) {
    			        // TODO Auto-generated catch block
    			        e.printStackTrace();
    		        }
                	read_file.seek_read_file(0);
                	byte[] experiment_start_ms_bytes = new byte[8];
                	read_file.read_file(experiment_start_ms_bytes);
  
         			byte_buffer = ByteBuffer.wrap(experiment_start_ms_bytes, 0, 8);
         			//byte_buffer.order(ByteOrder.LITTLE_ENDIAN);
         			long experiment_start_ms = byte_buffer.getLong();
                	
                	
         			try {
    		        	size = read_file.open_read_file(read_file.generate_filename_no_date());
    		        } catch (IOException e) {
    			        // TODO Auto-generated catch block
    			        e.printStackTrace();
    		        }
                	byte[] data = new byte[4*OD_calculate.experiment_data_size];
                	read_file.seek_read_file(size-(long)(4*OD_calculate.experiment_data_size));
                	read_file.read_file(data);
                	
   				    byte_buffer = ByteBuffer.wrap(data, 4*OD_calculate.current_raw_index_index, 4);
   				    current_raw_index = byte_buffer.getInt();
   				
   				    byte_buffer = ByteBuffer.wrap(data, 4*OD_calculate.experiment_seconds_index, 4);
   				    long elapsed_time = (long)(byte_buffer.getInt()*1000);
   				    Date date = new Date(experiment_start_ms + elapsed_time);	 
  
   				    byte_buffer = ByteBuffer.wrap(data, 4*OD_calculate.sensor_index_index, 4);
   				    current_index = byte_buffer.getInt();
   			
   				    int[] channel_data = new int[OD_calculate.total_sensor_channel];
   				    for (int i = 0; i < OD_calculate.total_sensor_channel; i++) {
   		        	    byte_buffer = ByteBuffer.wrap(data, 4*(OD_calculate.sensor_ch1_index+i), 4);
   		        	    channel_data[i] = byte_buffer.getInt();
   				    }
   				 
   		            od_value = OD_calculate.calculate_od(channel_data);      
   		          /*  if (mCurrentSeries == null) {
   		                mRenderer.setRange(new double[] {date.getTime(), date.getTime()+20000, 0, 50});
   				        CreateNewSeries();
   		            }*/
   		            
   		            Message msg = mHandler.obtainMessage();
   		            chart_display_data chart_data = new chart_display_data();
   		            chart_data.set_index_value(current_index);
   		            chart_data.set_date_value(experiment_start_ms + elapsed_time);
   		            chart_data.set_concentration_value(od_value);
   		            b.putSerializable("chart", chart_data);
   		            b.putInt("current_raw_index", current_raw_index);
		            b.putInt("new_pre_raw_index", new_pre_raw_index);
		            msg.setData(b);
 	                mHandler.sendMessage(msg);
                }
			}
		}
	}
  
    public void init_renderer_range(long x_start, double y_max, double y_min) {
    	mRenderer.setRange(new double[] {x_start, x_start+20000, y_min, y_max});
    }
    
    public void init_time_series() {
        Date date = null;
        double od_value = 0;
        long experiment_start_ms = 0;
        long size = 0;
        
   
        file_operate_byte_array read_file = new file_operate_byte_array("od_sensor", "sensor_offline_byte", true);
        try {
        	size = read_file.open_read_file(read_file.generate_filename_no_date());
        } catch (IOException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        }
        
        if (size >= 8) {
			int offset = 0;
		    byte[] data = new byte[(int) size];
		    
		    read_file.read_file(data);
		    
			ByteBuffer byte_buffer = ByteBuffer.wrap(data, offset, 8);
			offset += 8;
			//byte_buffer.order(ByteOrder.LITTLE_ENDIAN);
			experiment_start_ms = byte_buffer.getLong();
			
			while (size-offset >= (OD_calculate.experiment_data_size*4)) {
				 int[] channel_data = new int[OD_calculate.total_sensor_channel];
				 offset += OD_calculate.current_raw_index_index*4;
				 byte_buffer = ByteBuffer.wrap(data, offset, 4);
				 current_raw_index = byte_buffer.getInt();
				 
				 offset += (OD_calculate.experiment_seconds_index - OD_calculate.current_raw_index_index) *4;
				 byte_buffer = ByteBuffer.wrap(data, offset, 4);
				 long elapsed_time = (long)(byte_buffer.getInt()*1000);
				 date = new Date(experiment_start_ms + elapsed_time);	 
				 offset += (OD_calculate.sensor_index_index-OD_calculate.experiment_seconds_index)*4;
				 
				 byte_buffer = ByteBuffer.wrap(data, offset, 4);
				 current_index = byte_buffer.getInt();
				 offset += (OD_calculate.sensor_ch1_index-OD_calculate.sensor_index_index)*4;
				 for (int i = 0; i < OD_calculate.total_sensor_channel; i++) {
					byte[] channel_data_bytes = new byte[4];
					System.arraycopy(data, offset, channel_data_bytes, 0, 4);
		     	    offset += 4;
		        	ByteBuffer byte_buffer_channel = ByteBuffer.wrap(channel_data_bytes, 0, 4);
		    		//byte_buffer.order(ByteOrder.LITTLE_ENDIAN);
		        	channel_data[i] = byte_buffer_channel.getInt();
				 }
				 
		         od_value = OD_calculate.calculate_od(channel_data);      
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
		            
		       // current_index = (long)data[OD_calculate.sensor_index_index];
			     mCurrentSeries.add(date, od_value);
			}
		    
			 refresh_current_view_range(date, od_value); 
		} else {
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
        //  mChartView.repaint();
    }

    @Override
    protected void onResume() {
	    Log.d(Tag, "on Resume");
        super.onResume();
        if (mChartView == null) {
            LinearLayout layout = (LinearLayout) findViewById(R.id.chart);
            mChartView = ChartFactory.getTimeChartView(this, mDataset, mRenderer, "h:mm:ss a");
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
                	Toast.makeText(
                    ODChartBuilder.this,
                    SERIES_NAME + (seriesSelection.getSeriesIndex()+1)
                    + "\nclosest point value X=" + date.getHours() + ":" + date.getMinutes() + ":" + date.getSeconds() 
                    + "\nY=" + seriesSelection.getValue(), Toast.LENGTH_SHORT).show();
                    /*Toast.makeText(
                        ODChartBuilder.this,
                        "Chart element in series index= " + seriesSelection.getSeriesIndex()
                        + "\ndata point index= " + seriesSelection.getPointIndex()
                        + "\nclosest point value X=" + date.getHours() + ":" + date.getMinutes() + ":" + date.getSeconds() 
                        + "\nY=" + seriesSelection.getValue(), Toast.LENGTH_SHORT).show();*/
                }
            }
            });
          layout.addView(mChartView, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
          boolean enabled = mDataset.getSeriesCount() > 0;
        } else {
          mChartView.repaint();
        }
    }
  
    @Override
    public void onPause() {
  	    Log.d(Tag, "on Pause");
  	    data_read_thread_run = false;
  	    super.onPause();
    }
  
	@Override
	public void onDestroy() {
		Log.d(Tag, "on Destory");
		super.onDestroy();
	}
}