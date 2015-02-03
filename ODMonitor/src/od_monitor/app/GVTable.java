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
	protected SimpleAdapter saPageID,saTable;// ?��?�器
	protected ArrayList<HashMap<String, String>> srcPageID,srcTable;// ?��?���?
	
	protected int TableRowCount=10;//??�页?��，�?�页??�Row?�数
	protected int TableColCount=0;//每页col??�数???
	protected SQLiteDatabase db;
	protected String rawSQL="";
	protected Cursor curTable;//??�页?��使用??�Cursor
	protected OnTableClickListener clickListener;//?��个�?�页?��件被?��?��?��??��?��?�函?��
	protected OnPageSwitchListener switchListener;//??�页??�换?��??��?��?�函?��
	
	public GVTable(Context context) {
		super(context);
		this.setOrientation(VERTICAL);//??�直
		
		//----------------------------------------
		gvPage=new GridView(context);
		gvPage.setColumnWidth(80);
		gvPage.setNumColumns(GridView.AUTO_FIT);//??�页??�钮?��??�自?��设置
		addView(gvPage, new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,
		        LayoutParams.WRAP_CONTENT));//宽长式样
		srcPageID = new ArrayList<HashMap<String, String>>();
		saPageID = new SimpleAdapter(context,
				srcPageID,// ?��?��?���?
				R.layout.items,//XML实现
				new String[] { "ItemText" },// ?��?�数组�?�ImageItem对�?��?��?�项
				new int[] { R.id.ItemText });
		// 添�?�并且显�?
		gvPage.setAdapter(saPageID);
	    // 添�?��?�息处�??
		gvPage.setOnItemClickListener(new OnItemClickListener(){
		    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		        LoadTable(arg2);//?��?��???��?�页读�?�对应�?�数?��
			    if(switchListener!=null){//??�页??�换?��
		            switchListener.onPageSwitchListener(arg2,srcPageID.size());
			    }
		    }
	    });
		//----------------------------------------
		gvTable=new GridView(context);
		addView(gvTable, new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,
                LayoutParams.WRAP_CONTENT));//宽长式样
		
		srcTable = new ArrayList<HashMap<String, String>>();
		saTable = new SimpleAdapter(context,
				srcTable,// ?��?��?���?
				R.layout.items,//XML实现
				new String[] { "ItemText" },// ?��?�数组�?�ImageItem对�?��?��?�项
				new int[] { R.id.ItemText });
		// 添�?�并且显�?
		gvTable.setAdapter(saTable);
		gvTable.setOnItemClickListener(new OnItemClickListener(){
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				int y=arg2/curTable.getColumnCount()-1;//??��?��?��?��?��??
				int x=arg2 % curTable.getColumnCount();
				if (clickListener != null//??�页?��?��被点?��
						&& y!=-1) {//?��中�?��?�是??��?��?�时
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
	protected void LoadTable(int pageID)
	{
		if(curTable!=null)//??�放上次??�数?��
			curTable.close();
		
	    String sql= rawSQL+" Limit "+String.valueOf(TableRowCount)+ " Offset " +String.valueOf(pageID*TableRowCount);
	    curTable = db.rawQuery(sql, null);
	    
	    gvTable.setNumColumns(curTable.getColumnCount());//表现为表?��??�关?��?���?
	    TableColCount=curTable.getColumnCount();
	    srcTable.clear();
	    // ??��?��?�段??�称
	    int colCount = curTable.getColumnCount();
		for (int i = 0; i < colCount; i++) {
			HashMap<String, String> map = new HashMap<String, String>();
			map.put("ItemText", curTable.getColumnName(i));
			srcTable.add(map);
		}
		
		// ??�举?��????�数?��
		int recCount=curTable.getCount();
		for (int i = 0; i < recCount; i++) {//定�?�到�??��?��?��
			curTable.moveToPosition(i);
			for(int ii=0;ii<colCount;ii++)//定�?�到�??��?��?��中�?��?�个字段
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
		long recSize=rec.getLong(0);//??��?��?�数
		rec.close();
		int pageNum=(int)(recSize/TableRowCount) + 1;//??��?��?�页?��
		
		srcPageID.clear();
		for (int i = 0; i < pageNum; i++) {
			HashMap<String, String> map = new HashMap<String, String>();
			map.put("ItemText", "Page." + String.valueOf(i));// 添�?�图??��?��?��?�ID
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
