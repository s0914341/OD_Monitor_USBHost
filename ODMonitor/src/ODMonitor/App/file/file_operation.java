package ODMonitor.App.file;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.os.Environment;
import android.util.Log;

public class file_operation {
	private String Tag = "file_operation";
	public File sdcard = Environment.getExternalStorageDirectory();
	private String file_Dir = sdcard.getPath() + "/"; 
	protected String CreateFileName = "Default";
	protected File file_MetaData;
	protected File file;
	private BufferedWriter file_buf;
	private BufferedReader file_buf_read;
	//SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd-HHmmss");
	protected SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
	protected static SimpleDateFormat df1 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	protected static String Flush_File = ""; 
	protected boolean file_append = false;
	
	public file_operation(String dir_name, String file_name, boolean append) {
	    file_Dir = file_Dir + dir_name;
	    file_MetaData =  new File(file_Dir);
	    CreateFileName = file_name;
	    file_append = append;
	}
	
/*	public void Show_Toast_Msg(String msg ) {
		Toast mToastMsg;
		
		if (msg.contains("flush log file: "))
			mToastMsg = Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG);
		else
			mToastMsg = Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT);
    	TextView v = (TextView) mToastMsg.getView().findViewById(android.R.id.message);
    	v.setTextColor(Color.YELLOW);
    	v.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
    	//LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) v.getLayoutParams();
    	//v.setGravity(Gravity.CENTER);
		//mToastMsg.setGravity(Gravity.LEFT | Gravity.TOP, 300,100);
    	if (msg.equals(I_Tracker_Device_Conn) || msg.equals(I_Tracker_Device_DisCon))
    	  mToastMsg.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP, 0, 250);
    	else
    		mToastMsg.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP, 0, 125);
		mToastMsg.show();
    }*/
	
	public void flush_close_file() {
		try {
			if (file_buf != null) {
				file_buf.flush();
				file_buf.close();
				Flush_File = "log file saved: " + file.getPath(); 
			//	Show_Toast_Msg(Flush_File);
				file_buf = null;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void write_file_with_date(String line) {
		if (file_buf != null) {
			String line_text = df1.format(new Date()) + "  " + line;
			try {
				file_buf.write(line_text, 0, line_text.length());
				file_buf.newLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void write_file(String line) {
		if (file_buf != null) {
			try {
				file_buf.write(line, 0, line.length());
				file_buf.newLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	 /* file naming format logyyyymmdd-hhmmss.txt*/
	public String generate_filename() {
		String filename;
		filename = CreateFileName + df.format(new Date()) + ".txt";
		Log.d(Tag, filename);
		return filename;
	}
	
	public String generate_filename_no_date() {
		String filename;
		filename = CreateFileName + ".txt";
		Log.d(Tag, filename);
		return filename;
	}
	
	public void create_file(String filename) throws IOException {
		if (sdcard.exists()) {
			if (!file_MetaData.exists()) {
		        file_MetaData.mkdirs();
				Log.d(Tag, "sdcard directory exist:" + Boolean.toString(file_MetaData.exists()));
			}
			
			if (file_MetaData.exists()) {
			    file = new File(file_MetaData, filename);
			    file_buf = new BufferedWriter(new FileWriter(file, file_append));
		    } else {
				file_buf = null;
				Log.d(Tag, "Can't create the log file");
			}
		} else {
			file_buf = null;
			Log.d(Tag, "Can't found external sdcard ");
		}
	}
	
	public void delete_file(String filename) throws IOException {
		if (sdcard.exists()) {
			if (!file_MetaData.exists()) {
				Log.d(Tag, "sdcard directory exist:" + Boolean.toString(file_MetaData.exists()));
			} else {
			    file = new File(file_MetaData, filename);
			    Boolean deleted = file.delete();
			    
			    Log.d(Tag, "delete file:" + deleted.toString());
		    }
		} else {
			Log.d(Tag, "Can't found external sdcard ");
		}
	}
	
	public long open_read_file(String filename) throws IOException {
		long ret = -1;
		
		if (sdcard.exists()) {
			if (!file_MetaData.exists()) {
		        file_MetaData.mkdirs();
				Log.d(Tag, "sdcard directory exist:" + Boolean.toString(file_MetaData.exists()));
			}
			
			if (file_MetaData.exists()) {
			    file = new File(file_MetaData, filename);
			    file_buf_read = new BufferedReader(new FileReader(file));
			    ret = 0;
		    } else {
		    	file_buf_read = null;
				Log.d(Tag, "Can't open read file");
			}
		} else {
			file_buf_read = null;
			Log.d(Tag, "Can't found external sdcard ");
		}
		
		return ret;
	}
	
	public String read_file() throws IOException {
		String s = "";
		if (file_buf_read != null) {
		    try {
			    s = file_buf_read.readLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return s;
	}
	
}
