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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import od_monitor.app.ODMonitorActivity;
import od_monitor.app.ODMonitorApplication;
import od_monitor.app.data.AndroidAccessoryPacket;
import od_monitor.app.data.ChartDisplayData;
import od_monitor.app.data.ExperimentScriptData;
import od_monitor.app.data.MachineInformation;
import od_monitor.app.data.SensorDataComposition;
import od_monitor.app.data.SyncData;
import od_monitor.app.file.FileOperateBmp;
import od_monitor.app.file.FileOperateByteArray;
import od_monitor.app.file.FileOperation;
import od_monitor.experiment.ExperimentalOperationInstruct;
import od_monitor.experiment.ODCalculate;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.SeriesSelection;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import od_monitor.app.R;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
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
  public final static String Tag = ODChartBuilder.class.getName();
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
  public static final int[] SERIES_COLOR = {Color.GREEN, Color.CYAN, Color.YELLOW, Color.MAGENTA};
  
  public static final String SERIES_NAME = "OD series ";
  private static final long SECOND = 1000;
  private static final long MINUTE = 60*SECOND;
  private static final long HOUR = 60*MINUTE;
  private static final long DAY = 24*HOUR;
  private static final int HOURS = 24;
  
  public SyncData sync_chart_handler;
  public SyncData sync_chart_notify;
  private boolean chart_thread_run = false;
  public TextView debug_view;
  private ImageButton zoom_in_button;
  private ImageButton zoom_out_button;
  private ImageButton zoom_fit_button;
  private ImageButton save_chart_button;
  private long chart_start_time = System.currentTimeMillis();
  private long chart_end_time = chart_start_time + 20000;
  private double chart_max_od_value = 0;
  private double chart_min_od_value = 0;
  private boolean refresh_view_range = true;
  private long refresh_view_range_wait = 0;
  private String bmp_file_name;
  private String bmp_file_dir;


  @Override
  protected void onSaveInstanceState(Bundle outState) {
      super.onSaveInstanceState(outState);
      // save the current data, for instance when changing screen orientation
      outState.putSerializable("dataset", mDataset);
      outState.putSerializable("renderer", mRenderer);
      outState.putSerializable("current_series", mCurrentSeries);
      outState.putSerializable("current_renderer", mCurrentRenderer);
      Log.d(Tag, "onSaveInstanceState");
  }

  @Override
  protected void onRestoreInstanceState(Bundle savedState) {
      super.onRestoreInstanceState(savedState);
      // restore the current data, for instance when changing the screen
      // orientation
      mDataset = (XYMultipleSeriesDataset) savedState.getSerializable("dataset");
      mRenderer = (XYMultipleSeriesRenderer) savedState.getSerializable("renderer");
      mCurrentSeries = (TimeSeries[]) savedState.getSerializable("current_series");
      mCurrentRenderer = (XYSeriesRenderer) savedState.getSerializable("current_renderer");
      Log.d(Tag, "onRestoreInstanceState");
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
	  Log.d(Tag, "on Create");
      super.onCreate(savedInstanceState);
      setContentView(R.layout.chart_layout);
      getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
      Thread.currentThread().setName("Thread_XYChartBuilder");
      
      debug_view = (TextView)findViewById(R.id.DebugView);
      
      ODMonitorApplication app_data = ((ODMonitorApplication)this.getApplication());
	  sync_chart_notify = app_data.get_sync_chart_notify();
	  initial_renderer();
      
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
        	  chart_fit();
        	  mChartView.repaint();
      	  }
	  });
      
      save_chart_button = (ImageButton) findViewById(R.id.saveChart);
      save_chart_button.setOnClickListener(new View.OnClickListener() {
          public void onClick(View v) {
              FileOperateBmp write_file = new FileOperateBmp("od_chart", "chart", "png");
      		  try {
      			  write_file.create_file(write_file.generate_filename());
      			  write_file.write_file(mChartView.toBitmap(), Bitmap.CompressFormat.PNG, 100);
        		  write_file.flush_close_file();
        		  Toast.makeText(ODChartBuilder.this, "Save chart success!", Toast.LENGTH_SHORT).show(); 
      		  } catch (IOException e) {
      			  // TODO Auto-generated catch block
      			  e.printStackTrace();
      		  }
      	  }
	  });
    
      init_od_series();
      chart_thread_run = true;
      sync_chart_handler = new SyncData();
      new Thread(new chart_thread(chart_handler)).start(); 
  }
  
  public void initial_renderer() {
	  // set some properties on the main renderer
      mRenderer.setApplyBackgroundColor(true);
      mRenderer.setBackgroundColor(Color.argb(255, 0, 0, 0));
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
  }
  
  public void chart_to_bmp(Context context, int left, int top, int right, int bottom) {
	  initial_renderer();
	  init_od_series();
	  chart_fit();
      mChartView = ChartFactory.getTimeChartView(context, mDataset, mRenderer, "MM/dd h:mm:ss a");
      mChartView.layout(left, top, right, bottom);
     
      FileOperateBmp write_file = new FileOperateBmp("od_chart", "chart", "png");
      bmp_file_dir = write_file.get_file_dir();
      try {
    	  bmp_file_name = write_file.generate_filename();
          write_file.create_file(bmp_file_name);
          write_file.write_file(getViewBitmap(mChartView), Bitmap.CompressFormat.PNG, 100);
  		  write_file.flush_close_file();
      } catch (IOException e) {
      	  // TODO Auto-generated catch block
      	  e.printStackTrace();
      }
  }
  
  public static Bitmap getViewBitmap(View view) {
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
  
  public String get_bmp_file_dir() {
		return bmp_file_dir;
  }

  public String get_bmp_file_name() {
		return bmp_file_name;
  }
  
  public void init_od_series() {
	  for (int i = 0; i < ExperimentalOperationInstruct.EXPERIMENT_MAX_SENSOR_COUNT; i++)
		  od_file_to_series(i);
  }
  
  public void chart_fit() {
	  double XMax = 0, XMin = 0, YMax = 0, YMin = 0;
      int have_item_count = 0;
      boolean first = true;
      for (int i = 0; i < ExperimentalOperationInstruct.EXPERIMENT_MAX_SENSOR_COUNT; i++) {
    	  if (mCurrentSeries[i].getItemCount() > 0) {
    		  if (first) {
	    	      YMax = mCurrentSeries[i].getMaxY();
	    	      XMax = mCurrentSeries[i].getMaxX();  
	    	      YMin = mCurrentSeries[i].getMinY();
	    	      XMin = mCurrentSeries[i].getMinX();
    		  } else {
    			  if (mCurrentSeries[i].getMaxY() > YMax)
	    	          YMax = mCurrentSeries[i].getMaxY();
	    		  
	    		  if (mCurrentSeries[i].getMaxX() > XMax)
	    	          XMax = mCurrentSeries[i].getMaxX();
	    		  
	    		  if (mCurrentSeries[i].getMinY() < YMin)
	    	          YMin = mCurrentSeries[i].getMinY();
	    		  
	    		  if (mCurrentSeries[i].getMinX() < XMin)
	    	          XMin = mCurrentSeries[i].getMinX();
    		  }
    		  
    		  have_item_count += mCurrentSeries[i].getItemCount();
    		  first = false;
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
  }
  
  public void refresh_current_view_range(Date current_x, double current_y, double max_od_y, double min_od_y) {
	  double XMax = mRenderer.getXAxisMax();
	  double XMin = mRenderer.getXAxisMin();
	  double YMax = mRenderer.getYAxisMax();
	  double YMin = mRenderer.getYAxisMin();
	  double threadX = XMin+(XMax-XMin)*8.0/10.0;
	  if (threadX < current_x.getTime()) {
		  //double refresh_XMin = XMin+(XMax-XMin)*7.0/10.0;
		  
		  double refresh_XMin = current_x.getTime()-((XMax-XMin)*3.0/10.0);
		  double refresh_XMax = refresh_XMin+(XMax-XMin);
		  mRenderer.setXAxisMin(refresh_XMin);
		  mRenderer.setXAxisMax(refresh_XMax);
	  }
	 
	  if ((current_y > YMax) || (current_y < YMin)) {
	      double margin = (max_od_y-min_od_y)/20.0;
	      if (margin > 0) {
              mRenderer.setYAxisMin(min_od_y-margin);
	          mRenderer.setYAxisMax(max_od_y+margin);
	      }
	  }
  }

  public void SerialAdd(int sensor_num, Date x, double y, double max_od_y, double min_od_y) {
	  
	  if ((System.currentTimeMillis() - refresh_view_range_wait) > 30000) {
          // add a new data point to the current series
	      refresh_current_view_range(x, y, max_od_y, min_od_y);
	  }
      mCurrentSeries[sensor_num].add(x, y);
      // repaint the chart such as the newly added point to be visible
      mChartView.repaint();
  }
  
  final Handler chart_handler = new Handler() {
  	public void handleMessage(Message msg) {
  		Bundle b = msg.getData();
  		int series_num = b.getInt("series num");
  		int chart_count = b.getInt("chart count");
  		int[] index = b.getIntArray("index");
  		long[] date = b.getLongArray("date");
  		double[] od = b.getDoubleArray("od");
  		
  		for (int i = 0; i < chart_count; i++) {
  			chart_end_time = date[i];
  		    if (od[i] > chart_max_od_value)
       	        chart_max_od_value = od[i];
  		    else if (od[i] < chart_min_od_value)
       	        chart_min_od_value = od[i];
  			
  		    if (index[i] == 0) {
  			    chart_start_time = date[i];
  			    
  			    double margin = (chart_max_od_value-chart_min_od_value)/20.0;
  			    if (margin > 0)
  			        mRenderer.setRange(new double[] {chart_start_time, chart_start_time+20000, chart_min_od_value-margin, chart_max_od_value+margin});
  			    else
  			        mRenderer.setRange(new double[] {chart_start_time, chart_start_time+20000, od[i]-(1.0/20.0), od[i]+(1.0/20.0)});
  			    	
  			    if(null == mCurrentSeries[series_num])
  	                CreateNewSeries(series_num);
  		        mCurrentSeries[series_num].add(new Date(date[i]), od[i]);
  		        if (null != mChartView)
  		        	mChartView.repaint();
  		    } else {
  		    	if (index[i] == 1) {
  		    	     double margin = (chart_max_od_value-chart_min_od_value)/20.0;
	        	     if (margin > 0)
	        	         mRenderer.setRange(new double[] {chart_start_time, chart_start_time+(chart_end_time-chart_start_time)*10, chart_min_od_value-margin, chart_max_od_value+margin});
	        	     else 
	        		     mRenderer.setRange(new double[] {chart_start_time, chart_start_time+(chart_end_time-chart_start_time)*10, od[i]-(1.0/20.0), od[i]+(1.0/20.0)});
  		    	}
  		        SerialAdd(series_num, new Date(date[i]), od[i], chart_max_od_value, chart_min_od_value);
  		    }
  		
  		    String str = "sensor_num: " + series_num + "\n" + "index: " + index[i] + "\n" + "OD: " + od[i] + "\n";
    	    debug_view.setText(str);
  	
		    Log.d(Tag, "chart_thread handler id:"+Thread.currentThread().getId() + "process:" + android.os.Process.myTid());
		    Log.d(Tag, "sensor_num:" + series_num + "index:"+ index[i] + "date:" + date[i] + "od:" + od[i]);
  		}
  		
  		if (sync_chart_handler != null) {
	        synchronized (sync_chart_handler) {
	        	sync_chart_handler.notify();
	        }
	    }
  	}
  };
  
    class chart_thread implements Runnable {
		Handler mHandler;
		
		chart_thread(Handler h){
			mHandler = h;
		}
		
		public void run() {
			Bundle b = new Bundle(1);
			long size = 0;
			FileOperateByteArray read_file;
			byte[] file_data;
			SensorDataComposition one_sensor_data = new SensorDataComposition();
			
			while (chart_thread_run) {
				Log.d(Tag, "data_read_thread  id:"+Thread.currentThread().getId() + "process:" + android.os.Process.myTid());
				synchronized (sync_chart_notify) {
				    try {
				    	sync_chart_notify.wait();
				    	if (SyncData.STATUS_END == sync_chart_notify.get_status())
				    		break;
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				for (int sensor_num = 0; sensor_num < ExperimentalOperationInstruct.EXPERIMENT_MAX_SENSOR_COUNT; sensor_num++) {
				    int file_data_offset = 0;
				    String file_name = SensorDataComposition.sensor_raw_file_name + (sensor_num+1);
				    read_file = new FileOperateByteArray(SensorDataComposition.sensor_raw_folder_name, file_name, true);
		            try {
		        	    size = read_file.open_read_file(read_file.generate_filename_no_date());
		        	    long position = (current_raw_index[sensor_num]+1)*SensorDataComposition.total_size;
		        	    if (((size-position)/SensorDataComposition.total_size) > 0 && 
		        		    ((size-position)%SensorDataComposition.total_size) == 0) {
		        	        read_file.seek_read_file(position);
		        	        file_data = new byte[(int)(size-position)];
	    	                read_file.read_file(file_data);
	    	                one_sensor_data.set_buffer(file_data, file_data_offset, SensorDataComposition.total_size);
	    	                file_data_offset += SensorDataComposition.total_size;
		        	    } else {
		        		    Log.d(Tag, "file data is not complete!");
		        		    continue;
		        	    }
		            } catch (IOException e) {
			            // TODO Auto-generated catch block
			            e.printStackTrace();
			            Log.d(Tag, "file operation exception!");
	        		    continue;
		            }
		        
                    if (one_sensor_data.get_sensor_get_index() == (current_raw_index[sensor_num]+1) || (-1 == current_raw_index[sensor_num])) {
                	    int chart_count = file_data.length/SensorDataComposition.total_size;
                	    int[] index = new int[chart_count];
                	    long[] date = new long[chart_count];
                	    double[] od = new double[chart_count];
                	
                	    for (int i = 0; i < chart_count; i++) {
                	        double od_value = 0;
                	        current_raw_index[sensor_num] = one_sensor_data.get_sensor_get_index();
                	        od_value = one_sensor_data.get_sensor_od_value();   
   		                    // od_value = OD_calculate.calculate_od(one_sensor_data.get_channel_data());      
   		                    index[i] = current_raw_index[sensor_num];
   		                    date[i] = one_sensor_data.get_sensor_measurement_time();
   		                    od[i] = od_value;
   		         
 	                        Log.d(Tag, "thread index:"+current_raw_index + " date:" + one_sensor_data.get_sensor_measurement_time() + " od:" + od_value);
 	                    
 	                        if  ((file_data.length-file_data_offset) >= SensorDataComposition.total_size) {
 	                            one_sensor_data.set_buffer(file_data, file_data_offset, SensorDataComposition.total_size);
 	                            file_data_offset += SensorDataComposition.total_size;
 	                        }
                	    }
                	
                	    Message msg = mHandler.obtainMessage();
                	    b.putInt("series num", sensor_num);
		                b.putInt("chart count", chart_count);
		                b.putIntArray("index", index);
		                b.putLongArray("date", date);
		                b.putDoubleArray("od", od);
		               //  b.putSerializable("chart array", chart_data);
		                msg.setData(b);
	                    mHandler.sendMessage(msg);
                    } else {
                        Log.d(Tag, "sensor get index:"+one_sensor_data.get_sensor_get_index() + "current_raw_index:" + current_raw_index);
                    }
                    
                    synchronized (sync_chart_handler) {
    				    try {
    				    	sync_chart_handler.wait();
    					} catch (InterruptedException e) {
    						// TODO Auto-generated catch block
    						e.printStackTrace();
    					}
    				}
				}
			}
		}
	}
    
    public void od_file_to_series(int sensor_num) {
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
		             mRenderer.setRange(new double[] {chart_start_time, chart_start_time+20000, od_value-(1.0/20.0), od_value+(1.0/20.0)});
				     CreateNewSeries(sensor_num);
		         }
		         
		         chart_end_time = date.getTime();
		         if (od_value > chart_max_od_value)
		        	 chart_max_od_value = od_value;
		         else if (od_value < chart_min_od_value)
		        	 chart_min_od_value = od_value;
		            
		         current_raw_index[sensor_num] = one_sensor_data.get_sensor_get_index();
		         if (1 == current_raw_index[sensor_num]) {
		        	 double margin = (chart_max_od_value-chart_min_od_value)/20.0;
		        	 if (margin > 0)
		        	     mRenderer.setRange(new double[] {chart_start_time, chart_start_time+(chart_end_time-chart_start_time)*10, chart_min_od_value-margin, chart_max_od_value+margin});
		        	 else 
		        		 mRenderer.setRange(new double[] {chart_start_time, chart_start_time+(chart_end_time-chart_start_time)*10, od_value-(1.0/20.0), od_value+(1.0/20.0)});
		         }
		         mCurrentSeries[sensor_num].add(date, od_value);
			}
		    
			refresh_current_view_range(date, chart_max_od_value, chart_max_od_value, chart_min_od_value); 
			refresh_current_view_range(date, chart_min_od_value, chart_max_od_value, chart_min_od_value); 
		} else {
			if (mCurrentSeries[sensor_num] == null) {
			    long init_date = System.currentTimeMillis();
	            mRenderer.setRange(new double[] {init_date, init_date+20000, 0.0-(1.0/20.0), 0.0+(1.0/20.0)});
	            CreateNewSeries(sensor_num);
	        }
		}
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
        renderer.setColor(SERIES_COLOR[sensor_num]);
        renderer.setPointStyle(PointStyle.CIRCLE);
        renderer.setFillPoints(true);
        renderer.setDisplayChartValues(true);
        // renderer.setDisplayChartValuesDistance(10);
        mCurrentRenderer = renderer;
       // mChartView.repaint();
    }

    @Override
    protected void onResume() {
	    Log.d(Tag, "on Resume");
        super.onResume();
        if (mChartView == null) {
            LinearLayout layout = (LinearLayout) findViewById(R.id.chart);
            mChartView = ChartFactory.getTimeChartView(this, mDataset, mRenderer, "MM/dd h:mm:ss a");
            //mChartView = ChartFactory.getLineChartView(this, mDataset, mRenderer);
            // enable the chart click events
            mRenderer.setClickEnabled(true);
            mRenderer.setSelectableBuffer(10);
            mChartView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            // handle the click event on the chart
                Log.d ( Tag, "height: " + Integer.toString( mChartView.getMinimumHeight() ) );
                Log.d ( Tag, "width: " + Integer.toString( mChartView.getMeasuredWidth() ) );
            	refresh_view_range_wait = System.currentTimeMillis();
                SeriesSelection seriesSelection = mChartView.getCurrentSeriesAndPoint();
                if (seriesSelection == null) {
                   // Toast.makeText(ODChartBuilder.this, "No chart element", Toast.LENGTH_SHORT).show();
                } else {
                    // display information of the clicked point
                	Date date = new Date((long)seriesSelection.getXValue());
                	SimpleDateFormat date_format = new SimpleDateFormat("MM/dd h:mm:ss a");
                	Toast.makeText(
                    ODChartBuilder.this,
                    SERIES_NAME + (seriesSelection.getSeriesIndex()+1)
                    + "\nclosest point value X = " + date_format.format(date)
                    + "\nY = " + seriesSelection.getValue(), Toast.LENGTH_SHORT).show();
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
    
    public void notify_chart_thread_end() {
		if (sync_chart_notify != null) {
	        synchronized (sync_chart_notify) {
	        	sync_chart_notify.set_status(SyncData.STATUS_END);
	    	    sync_chart_notify.notify();
	        }
	    }
	}
  
    @Override
    public void onPause() {
  	    Log.d(Tag, "on Pause");
  	    chart_thread_run = false;
  	    notify_chart_thread_end();
  	    super.onPause();
    }
  
	@Override
	public void onDestroy() {
		Log.d(Tag, "on Destory");
		super.onDestroy();
	}
}