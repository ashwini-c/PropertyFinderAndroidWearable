package org.stevej.android.propertyfinder.fragments;

import org.stevej.android.propertyfinder.R;
import org.stevej.android.propertyfinder.model.Property;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class PropertyDetailsFragment extends Fragment {
	private static final String	TAG			= "PropertyDetailsFragment";

	// the proprerty for which details are currently displayed
	private Property			property	= null;

	View						details_view;

	public PropertyDetailsFragment() {
	}

	// called when the fragment's UI is being constructed. Initialise with current property details
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView()");
		details_view = inflater.inflate(R.layout.property_details, container, false);

		if (property != null) {

			TextView title_view = (TextView) details_view.findViewById(R.id.property_details_title);
			TextView price_view = (TextView) details_view.findViewById(R.id.property_details_price);
			TextView type_view = (TextView) details_view.findViewById(R.id.property_details_type);
			TextView address_view = (TextView) details_view.findViewById(R.id.property_details_address);
			TextView agency_name_view = (TextView) details_view.findViewById(R.id.property_details_agency_name);
			TextView agency_phone_view = (TextView) details_view.findViewById(R.id.property_details_agency_phone);

			title_view.setText(property.Title);
			price_view.setText(property.PriceDisplay);
			type_view.setText(property.PropertyType);
			address_view.setText(property.Address);
			agency_name_view.setText(property.Agency_name);
			agency_phone_view.setText(property.Agency_phone);
		}
		return details_view;
	}

	// update UI with new property details
	public void update(Property property) {
		Log.d(TAG, "setContent()");

		this.property = property;
		TextView title_view = (TextView) details_view.findViewById(R.id.property_details_title);
		TextView price_view = (TextView) details_view.findViewById(R.id.property_details_price);
		TextView type_view = (TextView) details_view.findViewById(R.id.property_details_type);
		TextView address_view = (TextView) details_view.findViewById(R.id.property_details_address);
		TextView agency_name_view = (TextView) details_view.findViewById(R.id.property_details_agency_name);
		TextView agency_phone_view = (TextView) details_view.findViewById(R.id.property_details_agency_phone);

		title_view.setText(property.Title);
		price_view.setText(property.PriceDisplay);
		type_view.setText(property.PropertyType);
		address_view.setText(property.Address);
		agency_name_view.setText(property.Agency_name);
		agency_phone_view.setText(property.Agency_phone);
	}

	public void clear() {
		Log.d(TAG, "clear()");

		TextView title_view = (TextView) details_view.findViewById(R.id.property_details_title);
		TextView price_view = (TextView) details_view.findViewById(R.id.property_details_price);
		TextView type_view = (TextView) details_view.findViewById(R.id.property_details_type);
		TextView address_view = (TextView) details_view.findViewById(R.id.property_details_address);
		TextView agency_name_view = (TextView) details_view.findViewById(R.id.property_details_agency_name);
		TextView agency_phone_view = (TextView) details_view.findViewById(R.id.property_details_agency_phone);

		title_view.setText("");
		price_view.setText("");
		type_view.setText("");
		address_view.setText("");
		agency_name_view.setText("");
		agency_phone_view.setText("");
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate()");
		super.onCreate(savedInstanceState);

		// keep state across config changes ie the current property data
		setRetainInstance(true);
	}

}
