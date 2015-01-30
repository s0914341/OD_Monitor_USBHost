package od_monitor.app;

import java.util.ArrayList;
import java.util.HashMap;

import ODMonitor.App.R;
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
	protected SimpleAdapter saPageID,saTable;// ?‚é?å™¨
	protected ArrayList<HashMap<String, String>> srcPageID,srcTable;// ?•°?®æº?
	
	protected int TableRowCount=10;//??†é¡µ?—¶ï¼Œæ?é¡µ??„Row?»æ•°
	protected int TableColCount=0;//æ¯é¡µcol??„æ•°???
	protected SQLiteDatabase db;
	protected String rawSQL="";
	protected Cursor curTable;//??†é¡µ?—¶ä½¿ç”¨??„Cursor
	protected OnTableClickListener clickListener;//?•´ä¸ªå?†é¡µ?§ä»¶è¢«?‚¹?‡»?—¶??„å?è?ƒå‡½?•°
	protected OnPageSwitchListener switchListener;//??†é¡µ??‡æ¢?—¶??„å?è?ƒå‡½?•°
	
	public GVTable(Context context) {
		super(context);
		this.setOrientation(VERTICAL);//??‚ç›´
		
		//----------------------------------------
		gvPage=new GridView(context);
		gvPage.setColumnWidth(80);
		gvPage.setNumColumns(GridView.AUTO_FIT);//??†é¡µ??‰é’®?•°??è‡ª?Š¨è®¾ç½®
		addView(gvPage, new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,
		        LayoutParams.WRAP_CONTENT));//å®½é•¿å¼æ ·
		srcPageID = new ArrayList<HashMap<String, String>>();
		saPageID = new SimpleAdapter(context,
				srcPageID,// ?•°?®?¥æº?
				R.layout.items,//XMLå®ç°
				new String[] { "ItemText" },// ?Š¨?æ•°ç»„ä?ImageItemå¯¹å?”ç?„å?é¡¹
				new int[] { R.id.ItemText });
		// æ·»å? å¹¶ä¸”æ˜¾ç¤?
		gvPage.setAdapter(saPageID);
	    // æ·»å? æ?ˆæ¯å¤„ç??
		gvPage.setOnItemClickListener(new OnItemClickListener(){
		    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		        LoadTable(arg2);//? ¹?®???‰å?†é¡µè¯»å?–å¯¹åº”ç?„æ•°?®
			    if(switchListener!=null){//??†é¡µ??‡æ¢?—¶
		            switchListener.onPageSwitchListener(arg2,srcPageID.size());
			    }
		    }
	    });
		//----------------------------------------
		gvTable=new GridView(context);
		addView(gvTable, new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,
                LayoutParams.WRAP_CONTENT));//å®½é•¿å¼æ ·
		
		srcTable = new ArrayList<HashMap<String, String>>();
		saTable = new SimpleAdapter(context,
				srcTable,// ?•°?®?¥æº?
				R.layout.items,//XMLå®ç°
				new String[] { "ItemText" },// ?Š¨?æ•°ç»„ä?ImageItemå¯¹å?”ç?„å?é¡¹
				new int[] { R.id.ItemText });
		// æ·»å? å¹¶ä¸”æ˜¾ç¤?
		gvTable.setAdapter(saTable);
		gvTable.setOnItemClickListener(new OnItemClickListener(){
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				int y=arg2/curTable.getColumnCount()-1;//??‡é?˜æ?ç?„ä?ç??
				int x=arg2 % curTable.getColumnCount();
				if (clickListener != null//??†é¡µ?•°?®è¢«ç‚¹?‡»
						&& y!=-1) {//?‚¹ä¸­ç?„ä?æ˜¯??‡é?˜æ?æ—¶
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
	 * è¯»å?–æ?‡å?šID??„å?†é¡µ?•°?®,è¿”å?å?“å?é¡µ??„æ?»æ•°?®
	 * SQL:Select * From TABLE_NAME Limit 9 Offset 10;
	 * è¡¨ç¤ºä»TABLE_NAMEè¡¨è·??–æ•°?®ï¼Œè·³è¿?10è¡Œï?Œå??9è¡?
	 * @param pageID ??‡å?šç?„å?†é¡µID
	 */
	protected void LoadTable(int pageID)
	{
		if(curTable!=null)//??Šæ”¾ä¸Šæ¬¡??„æ•°?®
			curTable.close();
		
	    String sql= rawSQL+" Limit "+String.valueOf(TableRowCount)+ " Offset " +String.valueOf(pageID*TableRowCount);
	    curTable = db.rawQuery(sql, null);
	    
	    gvTable.setNumColumns(curTable.getColumnCount());//è¡¨ç°ä¸ºè¡¨? ¼??„å…³?”®?‚¹ï¼?
	    TableColCount=curTable.getColumnCount();
	    srcTable.clear();
	    // ??–å?—å?—æ®µ??ç§°
	    int colCount = curTable.getColumnCount();
		for (int i = 0; i < colCount; i++) {
			HashMap<String, String> map = new HashMap<String, String>();
			map.put("ItemText", curTable.getColumnName(i));
			srcTable.add(map);
		}
		
		// ??—ä¸¾?‡º????‰æ•°?®
		int recCount=curTable.getCount();
		for (int i = 0; i < recCount; i++) {//å®šä?åˆ°ä¸??¡?•°?®
			curTable.moveToPosition(i);
			for(int ii=0;ii<colCount;ii++)//å®šä?åˆ°ä¸??¡?•°?®ä¸­ç?„æ?ä¸ªå­—æ®µ
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
	 * ??–å?—å?“å?å?†é¡µ??„Cursor
	 * @return å½“å?å?†é¡µ??„Cursor
	 */
	public Cursor gvGetCurrentTable()
	{
		return curTable;
	}
		
	/**
	 * ??†å?‡å?†é¡µ?˜¾ç¤ºæ•°?®
	 * @param rawSQL sqlè¯­å¥
	 * @param db ?•°?®åº?
	 */
	public void gvReadyTable(String rawSQL,SQLiteDatabase db)
	{
		this.rawSQL=rawSQL;
		this.db=db;
	}
	
	/**
	 * ?ˆ·?–°??†é¡µ??ï?Œæ›´?–°??‰é’®?•°???
	 * @param sql SQLè¯­å¥
	 * @param db ?•°?®åº?
	 */
	public void gvUpdatePageBar(String sql,SQLiteDatabase db)
	{
		Cursor rec = db.rawQuery(sql, null);
		rec.moveToLast();
		long recSize=rec.getLong(0);//??–å?—æ?»æ•°
		rec.close();
		int pageNum=(int)(recSize/TableRowCount) + 1;//??–å?—å?†é¡µ?•°
		
		srcPageID.clear();
		for (int i = 0; i < pageNum; i++) {
			HashMap<String, String> map = new HashMap<String, String>();
			map.put("ItemText", "Page." + String.valueOf(i));// æ·»å? å›¾??è?„æ?ç?„ID
			srcPageID.add(map);
		}
		saPageID.notifyDataSetChanged();
	}
	//---------------------------------------------------------
	/**
	 * è¡¨æ ¼è¢«ç‚¹?‡»?—¶??„å?è?ƒå‡½?•°
	 */
	public void setTableOnClickListener(OnTableClickListener click) {
		this.clickListener = click;
	}
	
	public interface OnTableClickListener {
		public void onTableClickListener(int x,int y,Cursor c);
	}
	//---------------------------------------------------------
	/**
	 * ??†é¡µ??è¢«?‚¹?‡»?—¶??„å?è?ƒå‡½?•°
	 */
	public void setOnPageSwitchListener(OnPageSwitchListener pageSwitch) {
		this.switchListener = pageSwitch;
	}
	public interface OnPageSwitchListener {
		public void onPageSwitchListener(int pageID,int pageCount);
	}
}
