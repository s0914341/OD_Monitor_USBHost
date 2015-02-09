package od_monitor.app.data;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class ODSqlDatabase {
	public static Context context = null;
	SQLiteDatabase OD_monitor_db = null;
	int id;
	public static final String OD_VALUE_TABLE_NAME = "od";
	private static final String INDEX = "NO";
	private static final String DATE = "date";
	private static final String OD1 = "OD1";
	private static final String OD2 = "OD2";
	private static final String OD3 = "OD3";
	private static final String OD4 = "OD4";
	public static final String OD_CHANNEL_RAW_TABLE_NAME = "raw";
	private static final String CH1 = "CH1";
	private static final String CH2 = "CH2";
	private static final String CH3 = "CH3";
	private static final String CH4 = "CH4";
	private static final String CH5 = "CH5";
	private static final String CH6 = "CH6";
	private static final String CH7 = "CH7";
	private static final String CH8 = "CH8";
	public static final String COLUMNS_OD_CHANNEL_RAW = INDEX + ", " + DATE + ", " + CH1 + ", " + CH2 + ", "  + CH3 + ", " + CH4 + ", "
			 + CH5 + ", " + CH6 + ", " + CH7 + ", " + CH8;
	
	public ODSqlDatabase(Context parent) {
		this.context = parent;
	}
	
	public SQLiteDatabase get_database() {
		return OD_monitor_db;
	}
	
	public void CreateODDataDB() {
		OD_monitor_db = SQLiteDatabase.create(null);
		Log.d("DB Path", OD_monitor_db.getPath());
		String amount = String.valueOf(context.databaseList().length);
		Log.d("DB amount", amount);

		String sql_od_value = "CREATE TABLE " + OD_VALUE_TABLE_NAME + " (" + 
		        INDEX	+ " text not null, " + DATE + " text not null," + OD1 + " text not null," +
		        OD2	+ " text not null, " + OD3 + " text not null," +
		        OD4	+ " text not null "+");";
		
		String sql_od_channel_raw = "CREATE TABLE " + OD_CHANNEL_RAW_TABLE_NAME + " (" + 
		        INDEX	+ " text not null, " + DATE + " text not null," + CH1 + " text not null," +
		        CH2	+ " text not null, " + CH3 + " text not null," + CH4 + " text not null," +
		        CH5	+ " text not null, " + CH6 + " text not null," + CH7 + " text not null," +
		        CH8	+ " text not null "+");";
		try {
			OD_monitor_db.execSQL("DROP TABLE IF EXISTS " + OD_VALUE_TABLE_NAME);
			OD_monitor_db.execSQL(sql_od_value);
			
			OD_monitor_db.execSQL("DROP TABLE IF EXISTS " + OD_CHANNEL_RAW_TABLE_NAME);
			OD_monitor_db.execSQL(sql_od_channel_raw);
		} catch (SQLException e) {}
	}
	
	public void InsertODDateToDB(SensorDataComposition sensor_data) {
		String sql_od_value = "insert into " + OD_VALUE_TABLE_NAME + " (" + 
			INDEX + ", " + DATE + ", " + OD1 + ", " + OD2 + ", " + OD3 + ", " + OD4
					+ ") values('" + sensor_data.get_sensor_get_index_string()
					+ "', '" + sensor_data.get_sensor_measurement_time_string() + "','"
					+ sensor_data.get_sensor_od_value_string() + "','NA','NA','NA');";
		
		int[] channel_data = sensor_data.get_channel_data();
		String[] channel_data_string = {Integer.toString(channel_data[0]), Integer.toString(channel_data[1]), Integer.toString( channel_data[2]),
				                        Integer.toString(channel_data[3]), Integer.toString(channel_data[4]), Integer.toString(channel_data[5]),
				                        Integer.toString(channel_data[6]), Integer.toString(channel_data[7])};
		
		String sql_od_channel_raw = "insert into " + OD_CHANNEL_RAW_TABLE_NAME + " (" + 
				INDEX + ", " + DATE + ", " + CH1 + ", " + CH2 + ", " + CH3 + ", " + CH4
				        + ", " + CH5 + ", " + CH6 + ", " + CH7 + ", " + CH8
						+ ") values('" + sensor_data.get_sensor_get_index_string()
						+ "', '" + sensor_data.get_sensor_measurement_time_string() + "','"
						+ channel_data_string[0] + "','"
						+ channel_data_string[1] + "','"
						+ channel_data_string[2] + "','"
						+ channel_data_string[3] + "','"
						+ channel_data_string[4] + "','"
						+ channel_data_string[5] + "','"
						+ channel_data_string[6] + "','"
						+ channel_data_string[7] + "');";
		try {
			OD_monitor_db.execSQL(sql_od_value);
			OD_monitor_db.execSQL(sql_od_channel_raw);
		} catch (SQLException e) {
		}
	}
}
