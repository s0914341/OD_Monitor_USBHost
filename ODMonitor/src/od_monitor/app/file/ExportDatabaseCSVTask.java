package od_monitor.app.file;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import od_monitor.app.ODMonitorActivity;
import od_monitor.app.data.ODSqlDatabase;
import od_monitor.app.data.SensorDataComposition;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;
import com.opencsv.CSVWriter;

public class ExportDatabaseCSVTask extends AsyncTask<String, Void, Boolean> {
	public SQLiteDatabase myDatabase = null;
	public String Table_Name;
	public static Context context = null;
    private final ProgressDialog dialog;
    private String file_dir;
    private String file_name;
    
    public ExportDatabaseCSVTask(Context parent, SQLiteDatabase data_base, String table_name) {
    	this.context = parent;
    	myDatabase = data_base;
    	Table_Name = table_name;
    	dialog = new ProgressDialog(context);
    }
    
    public String get_file_dir() {
		return file_dir;
	}
	
	public String get_file_name() {
		return file_name;
	}
	
	public void set_data_base_table_name(final String table_name) {
		Table_Name = table_name;
	}
	
	public boolean ExportDatabaseCSVImmediately() {
		boolean ret = false;
		
	    File dbFile = context.getDatabasePath("myDatabase.db");
	    System.out.println(dbFile);  // displays the data base path in your logcat 
	        
	    File sdcard = Environment.getExternalStorageDirectory();
	    String file_Dir = sdcard.getPath() + FileOperation.work_directory; 
	    file_dir = file_Dir + SensorDataComposition.sensor_raw_folder_name;
	    File exportDir =  new File(file_dir);
	        
	    if (!exportDir.exists()) { exportDir.mkdirs(); }

	    SimpleDateFormat file_date = new SimpleDateFormat("yyyyMMdd_HHmmss");
	    if (Table_Name.equals(ODSqlDatabase.OD_CHANNEL_RAW_TABLE_NAME)) 
	        file_name = "ODChannelRawData" + file_date.format(new Date()) + ".csv";
	    else
	    	file_name = "ODExperimentData" + file_date.format(new Date()) + ".csv";
	    File file = new File(exportDir, file_name);
	    try {
	        file.createNewFile();
	        CSVWriter csvWrite = new CSVWriter(new FileWriter(file));
	        Cursor curCSV = myDatabase.rawQuery("select * from " + Table_Name, null);
	        csvWrite.writeNext(curCSV.getColumnNames());
	        int column = curCSV.getColumnCount();
	        String[] arrStr = new String[column];
	        while(curCSV.moveToNext()) {
	        	for (int i = 0; i < column; i++) {
	        		arrStr[i] = curCSV.getString(i);
	        	}
	           // String arrStr[] ={curCSV.getString(0),curCSV.getString(1),curCSV.getString(2)};
	            // curCSV.getString(3),curCSV.getString(4)};
	            csvWrite.writeNext(arrStr);
	        }
	        csvWrite.close();
	        curCSV.close();
	        ret = true;
	    } catch(SQLException sqlEx) {
	        Log.e("MainActivity", sqlEx.getMessage(), sqlEx);
	    } catch (IOException e) {
	        Log.e("MainActivity", e.getMessage(), e);
	    }
	    
	    if (ret) {
            Toast.makeText(context, "Export csv file successful!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Export csv file failed", Toast.LENGTH_SHORT).show();
        }
	    
	    return ret;
	}
    
    @Override
    protected void onPreExecute() {
        this.dialog.setMessage("Exporting database...");
        this.dialog.show();
    }

    protected Boolean doInBackground(final String... args) {
    	return ExportDatabaseCSVImmediately();
    }

    protected void onPostExecute(final Boolean success) {
        if (this.dialog.isShowing()) { this.dialog.dismiss(); }
    }
}
