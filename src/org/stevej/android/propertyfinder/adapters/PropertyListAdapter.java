package org.stevej.android.propertyfinder.adapters;

import java.util.List;

import org.stevej.android.propertyfinder.R;
import org.stevej.android.propertyfinder.model.Property;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

public class PropertyListAdapter extends ArrayAdapter<Property> {
	private LayoutInflater	layout_inflater;
	private ImageLoader		image_loader	= null;

	public PropertyListAdapter(Context context, int item_layout_id, int default_text_id, List<Property> properties, ImageLoader image_loader) {
		super(context, item_layout_id, default_text_id, properties);
		layout_inflater = LayoutInflater.from(context);
		this.image_loader = image_loader;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ViewHolder holder;
		if (convertView == null) {
			convertView = layout_inflater.inflate(R.layout.property_list_item, null);

			holder = new ViewHolder();

			// use the Volley library's NetworkImageView
			holder.thumbnail = (NetworkImageView) convertView.findViewById(R.id.property_thumbnail);
			holder.title = (TextView) convertView.findViewById(R.id.property_title);
			holder.price = (TextView) convertView.findViewById(R.id.property_price);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		Property property = this.getItem(position);

		// Volley method to load network image into the view
		holder.thumbnail.setImageUrl(property.ThumbURL, image_loader);

		holder.title.setText(property.Title);
		holder.price.setText(property.PriceDisplay);

		return convertView;
	}

	static class ViewHolder {
		NetworkImageView	thumbnail;
		TextView			title;
		TextView			price;
	}

}
