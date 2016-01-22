package org.stevej.android.propertyfinder.adapters;

import java.util.List;

import org.stevej.android.propertyfinder.model.PlaceMenuEntry;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class PlaceMenuListAdapter extends ArrayAdapter<PlaceMenuEntry> {
	private LayoutInflater	layout_inflater;

	public PlaceMenuListAdapter(Context context, int resource, int textViewResourceId, List<PlaceMenuEntry> objects) {
		super(context, resource, textViewResourceId, objects);
		layout_inflater = LayoutInflater.from(context);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.ArrayAdapter#getView(int, android.view.View, android.view.ViewGroup)
	 * 
	 * Invoked when constructing the UI for the nav menu item that is displayed in the action bar itelf
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = layout_inflater.inflate(android.R.layout.simple_list_item_1, null);

			holder = new ViewHolder();
			holder.tv = (TextView) convertView.findViewById(android.R.id.text1);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		PlaceMenuEntry entry = (PlaceMenuEntry) getItem(position);
		holder.tv.setText(entry.place_name);

		return convertView;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.ArrayAdapter#getDropDownView(int, android.view.View, android.view.ViewGroup)
	 * 
	 * Invoked when constructing the UI for an item in the drop down list of places in the action bar nav menu
	 */
	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = layout_inflater.inflate(android.R.layout.simple_list_item_1, null);

			holder = new ViewHolder();
			holder.tv = (TextView) convertView.findViewById(android.R.id.text1);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		PlaceMenuEntry entry = (PlaceMenuEntry) getItem(position);
		holder.tv.setText(entry.place_name);

		return convertView;
	}

	static class ViewHolder {
		TextView	tv;
	}

}
