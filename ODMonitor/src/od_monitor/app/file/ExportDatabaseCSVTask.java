package od_monitor.app.file;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

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

    public ExportDatabaseCSVTask(Context parent, SQLiteDatabase data_base, String table_name) {
    	this.context = parent;
    	myDatabase = data_base;
    	Table_Name = table_name;
    	dialog = new ProgressDialog(context);
    }
    
    @Override
    protected void onPreExecute() {
        this.dialog.setMessage("Exporting database...");
        this.dialog.show();
    }

    protected Boolean doInBackground(final String... args) {
        File dbFile = context.getDatabasePath("myDatabase.db");
        System.out.println(dbFile);  // displays the data base path in your logcat 
        File exportDir = new File(Environment.getExternalStorageDirectory(), "");

        if (!exportDir.exists()) { exportDir.mkdirs(); }

        File file = new File(exportDir, "myfile.csv");
        try {
            file.createNewFile();
            CSVWriter csvWrite = new CSVWriter(new FileWriter(file));
            Cursor curCSV = myDatabase.rawQuery("select * from " + Table_Name,null);
            csvWrite.writeNext(curCSV.getColumnNames());
            while(curCSV.moveToNext()) {
                String arrStr[] ={curCSV.getString(0),curCSV.getString(1),curCSV.getString(2)};
                // curCSV.getString(3),curCSV.getString(4)};
                csvWrite.writeNext(arrStr);
            }
            csvWrite.close();
            curCSV.close();
            return true;
        } catch(SQLException sqlEx) {
            Log.e("MainActivity", sqlEx.getMessage(), sqlEx);
            return false;
        } catch (IOException e) {
            Log.e("MainActivity", e.getMessage(), e);
            return false;
        }
    }

    protected void onPostExecute(final Boolean success) {
        if (this.dialog.isShowing()) { this.dialog.dismiss(); }
        if (success) {
            Toast.makeText(context, "Export csv file successful!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Export csv file failed", Toast.LENGTH_SHORT).show();
        }
    }
}
