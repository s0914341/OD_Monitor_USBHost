package od_monitor.app;

import java.util.ArrayList;
import java.util.HashMap;

import od_monitor.app.R;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.OnItemClickListener;

public class GVTable extends LinearLayout {
	protected GridView gvTable,gvPage;	
	protected SimpleAdapter saPageID,saTable;
	protected ArrayList<HashMap<String, String>> srcPageID,srcTable;
	
	protected int TableRowCount=10;
	protected int TableColCount=0;
	protected SQLiteDatabase db;
	protected String rawSQL="";
	protected Cursor curTable;
	protected OnTableClickListener clickListener;
	protected OnPageSwitchListener switchListener;
	
	public GVTable(Context context) {
		super(context);
		this.setOrientation(VERTICAL);
		
		//----------------------------------------
		gvPage=new GridView(context);
		gvPage.setColumnWidth(80);
		gvPage.setNumColumns(GridView.AUTO_FIT);
		addView(gvPage, new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,
		        LayoutParams.WRAP_CONTENT));
		srcPageID = new ArrayList<HashMap<String, String>>();
		saPageID = new SimpleAdapter(context,
				srcPageID,
				R.layout.items,
				new String[] { "ItemText" },
				new int[] { R.id.ItemText });
		
		gvPage.setAdapter(saPageID);
		gvPage.setOnItemClickListener(new OnItemClickListener(){
		    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		        LoadTable(arg2);
			    if(switchListener!=null){
		            switchListener.onPageSwitchListener(arg2,srcPageID.size());
			    }
		    }
	    });
		//----------------------------------------
		gvTable=new GridView(context);
		addView(gvTable, new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,
                LayoutParams.WRAP_CONTENT));
		
		srcTable = new ArrayList<HashMap<String, String>>();
		saTable = new SimpleAdapter(context,
				srcTable,
				R.layout.items,
				new String[] { "ItemText" },
				new int[] { R.id.ItemText });
		
		gvTable.setAdapter(saTable);
		gvTable.setOnItemClickListener(new OnItemClickListener(){
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				int y=arg2/curTable.getColumnCount()-1;
				int x=arg2 % curTable.getColumnCount();
				if (clickListener != null
						&& y!=-1) {
					clickListener.onTableClickListener(x,y,curTable);
				}
			}
		});
	}
	/**
	 * clear all data
	 */
	public void gvRemoveAll()
	{
		if(this.curTable!=null)
			curTable.close();
		srcTable.clear();
		saTable.notifyDataSetChanged();
	
		srcPageID.clear();
		saPageID.notifyDataSetChanged();
		
	}
	/**
	 * 读�?��?��?�ID??��?�页?��?��,返�?��?��?�页??��?�数?��
	 * SQL:Select * From TABLE_NAME Limit 9 Offset 10;
	 * 表示从TABLE_NAME表获??�数?��，跳�?10行�?��??9�?
	 * @param pageID ??��?��?��?�页ID
	 */
	protected void LoadTable(int pageID) {
		if(curTable!=null)
			curTable.close();
		
	    String sql= rawSQL+" Limit "+String.valueOf(TableRowCount)+ " Offset " +String.valueOf(pageID*TableRowCount);
	    curTable = db.rawQuery(sql, null);
	    
	    gvTable.setNumColumns(curTable.getColumnCount());
	    TableColCount=curTable.getColumnCount();
	    srcTable.clear();
	   
	    int colCount = curTable.getColumnCount();
		for (int i = 0; i < colCount; i++) {
			HashMap<String, String> map = new HashMap<String, String>();
			map.put("ItemText", curTable.getColumnName(i));
			srcTable.add(map);
		}
		
		int recCount=curTable.getCount();
		for (int i = 0; i < recCount; i++) {
			curTable.moveToPosition(i);
			for(int ii=0;ii<colCount;ii++)
			{
				HashMap<String, String> map = new HashMap<String, String>();
				map.put("ItemText", curTable.getString(ii));
				srcTable.add(map);
			}
		}
		
		saTable.notifyDataSetChanged();
	}
	
	public void refresh_last_table() {
		if (srcPageID.size() > 0)
		    LoadTable(srcPageID.size()-1);
	}
	/**
	 * set the max row count for view
	 * @param row
	 */
	public void gvSetTableRowCount(int row)
	{
		TableRowCount=row;
	}
	
	/**
	 * get max row count
	 * @return
	 */
	public int gvGetTableRowCount()
	{
		return TableRowCount;
	}
	
	/**
	 * ??��?��?��?��?�页??�Cursor
	 * @return 当�?��?�页??�Cursor
	 */
	public Cursor gvGetCurrentTable()
	{
		return curTable;
	}
		
	/**
	 * ??��?��?�页?��示数?��
	 * @param rawSQL sql语句
	 * @param db ?��?���?
	 */
	public void gvReadyTable(String rawSQL,SQLiteDatabase db)
	{
		this.rawSQL=rawSQL;
		this.db=db;
	}
	
	/**
	 * ?��?��??�页??��?�更?��??�钮?��???
	 * @param sql SQL语句
	 * @param db ?��?���?
	 */
	public void gvUpdatePageBar(String sql,SQLiteDatabase db)
	{
		Cursor rec = db.rawQuery(sql, null);
		rec.moveToLast();
		long recSize=rec.getLong(0);
		rec.close();
		int pageNum=(int)(recSize/TableRowCount) + 1;
		
		srcPageID.clear();
		for (int i = 0; i < pageNum; i++) {
			HashMap<String, String> map = new HashMap<String, String>();
			map.put("ItemText", "Page." + String.valueOf(i));
			srcPageID.add(map);
		}
		saPageID.notifyDataSetChanged();
	}
	//---------------------------------------------------------
	/**
	 * 表格被点?��?��??��?��?�函?��
	 */
	public void setTableOnClickListener(OnTableClickListener click) {
		this.clickListener = click;
	}
	
	public interface OnTableClickListener {
		public void onTableClickListener(int x,int y,Cursor c);
	}
	//---------------------------------------------------------
	/**
	 * ??�页??�被?��?��?��??��?��?�函?��
	 */
	public void setOnPageSwitchListener(OnPageSwitchListener pageSwitch) {
		this.switchListener = pageSwitch;
	}
	public interface OnPageSwitchListener {
		public void onPageSwitchListener(int pageID,int pageCount);
	}
}
