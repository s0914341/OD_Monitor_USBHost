package ODMonitor.App;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class SpinnerArrayAdapter extends ArrayAdapter<String>{
  //  private int[] ItemPositionIsDisable;
    private List<Boolean> list_enable = new ArrayList<Boolean>();
    
    public SpinnerArrayAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }
    
    public SpinnerArrayAdapter(Context context, int resource, int textViewResourceId) {
        super(context, resource, textViewResourceId);
    }
    
    public SpinnerArrayAdapter(Context context, int textViewResourceId, String[] objects) {
        super(context, textViewResourceId,objects);
       // ItemPositionIsDisable = new int[objects.length];
    }
    
    public SpinnerArrayAdapter(Context context, String[] objects){
        this(context, android.R.layout.simple_spinner_item,objects);
        this.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    }

    public SpinnerArrayAdapter(int stringArrayResourceId, Context context){
        this(context, context.getResources().getStringArray(stringArrayResourceId));
    }
    
    @Override
    public void add(String object) {
    	super.add(object);
    	Boolean enalble = new Boolean(true);
    	list_enable.add(enalble); 
    }
    
    @Override
    public void clear() {
    	super.clear();
    	list_enable.clear();
    }
    
	/*public boolean isEnabled(int position) {
        if (ItemPositionIsDisable[position]==1)
            return false;
        return true;
    }*/
    
    public void setItemPositionEnable(int position, boolean enable) {

    	list_enable.set(position, enable);
       // ItemPositionIsDisable[position] = (IsDisable) ? 1 : 0;
    }
    
    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {

        View mView = super.getDropDownView(position, convertView, parent);
        TextView mTextView = (TextView) mView;

        if (list_enable.get(position) == false) {
         //   mTextView.setTextColor(Color.GRAY);
            mTextView.setEnabled(false);
        } else if (list_enable.get(position) == true) {
           // mTextView.setTextColor(Color.BLACK);
            mTextView.setEnabled(true);
        }
        
        return mView;
        
    }
}
