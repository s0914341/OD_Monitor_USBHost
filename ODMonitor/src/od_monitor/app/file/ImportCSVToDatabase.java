package od_monitor.app.file;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import od_monitor.app.data.ODSqlDatabase;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class ImportCSVToDatabase {
	private SQLiteDatabase myDatabase = null;
	private static Context context = null;
	private String columns;
	
	public ImportCSVToDatabase(Context parent, SQLiteDatabase data_base) {
    	this.context = parent;
    	myDatabase = data_base;
    }
	
	public void ImportRawChannelCSVToDatabaseImmediately(String file_path) {
		FileReader file;
		try {
			file = new FileReader(file_path);
			BufferedReader buffer = new BufferedReader(file);
			String line = "";
			String str1 = "INSERT INTO " + ODSqlDatabase.OD_CHANNEL_RAW_TABLE_NAME + " (" + ODSqlDatabase.COLUMNS_OD_CHANNEL_RAW + ") values(";
			String str2 = ");";

			myDatabase.beginTransaction();
			while ((line = buffer.readLine()) != null) {
				    StringBuilder sb = new StringBuilder(str1);
				    String[] str = line.split(",");
				    sb.append("'" + str[0] + "',");
				    sb.append(str[1] + "',");
				    sb.append(str[2] + "',");
				    sb.append(str[3] + "'");
				    sb.append(str[4] + "'");
				    sb.append(str2);
				    myDatabase.execSQL(sb.toString());
			}
			myDatabase.setTransactionSuccessful();
			myDatabase.endTransaction();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
