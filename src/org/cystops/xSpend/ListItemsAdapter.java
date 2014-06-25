package org.cystops.xSpend;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ListItemsAdapter extends BaseAdapter {
	
	private LayoutInflater layoutInflater;
	private ArrayList<ArrayList<Object>> arrayList;
	private Context context;
	
	public ListItemsAdapter (Context context, ArrayList<ArrayList<Object>> arrayList) {
		arrayList = arrayList;
		layoutInflater = LayoutInflater.from(context);
		this.context = context;
	}
	
	public String getItemLabel(int position) {
		// TODO Auto-generated method stub
//		return list.get(position).get(0).toString().replaceFirst("[a-zA-Z0-9]*_", "");
		return arrayList.get(position).get(0).toString();
	}

	public int getCount() {
		// TODO Auto-generated method stub
		return arrayList.size();
	}

	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return arrayList.get(position);
	}

	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder viewHolder;
		Integer textColor;
		
		if (convertView == null) {
			convertView = layoutInflater.inflate(R.layout.holder_list_item, null);
			viewHolder = new ViewHolder();
			viewHolder.textViewLabel = (TextView) convertView.findViewById(R.id.textView_list_item_label);
			viewHolder.textViewAmount = (TextView) convertView.findViewById(R.id.textView_total_amount);
			
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		
		viewHolder.textViewLabel.setText(arrayList.get(position).get(0).toString().replaceFirst("[a-zA-Z0-9]*_", ""));
		viewHolder.textViewAmount.setText(arrayList.get(position).get(1).toString());
		
		textColor = getTextColor((Integer) arrayList.get(position).get(1));
		
		viewHolder.textViewLabel.setTextColor(textColor);
		viewHolder.textViewAmount.setTextColor(textColor);

		return convertView;
	}

	private Integer getTextColor(Integer integer) {
		String color;
		if(integer > 0) {
			color = context.getString(R.string.my_green);
		} else if (integer < 0) {
			color = context.getString(R.string.my_red);
		} else {
			color = context.getString(R.string.my_grey);
		}
		return Color.parseColor(color);
	}

	public void resetDataStore (ArrayList<ArrayList<Object>> dataStore) {
		arrayList = dataStore;
	}
	
	static class ViewHolder {
		TextView textViewLabel;
		TextView textViewAmount;
	}
	
}
