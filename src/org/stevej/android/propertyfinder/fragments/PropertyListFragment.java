package org.stevej.android.propertyfinder.fragments;

import java.util.ArrayList;
import java.util.Collections;

import org.stevej.android.propertyfinder.R;
import org.stevej.android.propertyfinder.activities.PropertyFinderActivity;
import org.stevej.android.propertyfinder.adapters.PropertyListAdapter;
import org.stevej.android.propertyfinder.interfaces.PropertySelectionListener;
import org.stevej.android.propertyfinder.model.Property;

import android.app.Activity;
import android.app.ListFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

public class PropertyListFragment extends ListFragment {
	private static final String			TAG						= "PropertyListFragment";
	private static final int			NO_SELECTION			= -1;

	private ArrayList<Property>			properties				= new ArrayList<Property>();
	private PropertySelectionListener	property_selection_listener;
	private PropertyListAdapter			property_list_adapter;

	private int							selected_item_position	= NO_SELECTION;

	/*
	 * requires empty constructor
	 */
	public PropertyListFragment() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Fragment#onAttach(android.app.Activity)
	 * 
	 * This fragment has now been added to the activity, which we use as the listener for property list selections
	 */
	@Override
	public void onAttach(Activity activity) {
		Log.d(TAG, "onAttach()");
		super.onAttach(activity);
		try {
			property_selection_listener = (PropertySelectionListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement PropertySelectionListener");
		}

	}

	@Override
	public void onDetach() {
		Log.d(TAG, "onDetach()");
		super.onDetach();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		Log.d(TAG, "onActivityCreated()");
		super.onActivityCreated(savedInstanceState);

		property_list_adapter = new PropertyListAdapter(getActivity(), R.layout.property_list_item, R.id.property_title, properties,
				((PropertyFinderActivity) getActivity()).getImageLoader());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Fragment#onCreate(android.os.Bundle)
	 * 
	 * Set up the data/list view adapter
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate()");
		super.onCreate(savedInstanceState);

		// stops onDestroy() and onCreate() being called when the parent
		// activity is destroyed/recreated on configuration change
		setRetainInstance(true);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Fragment#onResume()
	 * 
	 * The fragment is visible and 'alive'. Now we can do UI operations.
	 */
	public void onResume() {
		Log.d(TAG, "onResume()");
		super.onResume();

		getListView().setAdapter(property_list_adapter);
		getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		Log.d(TAG, "onResume : adapter count = " + property_list_adapter.getCount() + ", selected_item_position = " + selected_item_position);

		setIsLoading(false);

		if (property_list_adapter.getCount() > 0 && selected_item_position != NO_SELECTION) {
			getListView().setItemChecked(selected_item_position, true);
			getListView().smoothScrollToPositionFromTop(selected_item_position, 100, 100);
		}
	}

	public void setIsLoading(boolean is_loading) {
		setListShown(!is_loading);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		selected_item_position = position;

		getListView().setItemChecked(position, true);

		property_selection_listener.onPropertySelected(property_list_adapter.getItem(position));
	}

	public void clear() {
		setIsLoading(false);
		properties.clear();
		property_list_adapter.notifyDataSetChanged();
	}

	// there is new data to display. Stop the loading image, unset the
	// selection, empty the list and add the new data, update the display
	// and move the list to the top
	public void update(ArrayList<Property> retrieved_properties) {
		Log.d(TAG, "update()");

		setIsLoading(false);
		getListView().setItemChecked(selected_item_position, false);

		properties.clear();
		properties.addAll(retrieved_properties);
		property_list_adapter.notifyDataSetChanged();
		getListView().smoothScrollToPositionFromTop(0, 0, 0);
	}

	public Property updatePropertyDetails(Property property) {
		// receive a property with ListingID, Body and image URLs
		// find correct property in list of properties
		// update its fields
		// return the updated property from the list
		for(int i=0;i<properties.size();i++)

		{
			if(properties.get(i).ListingID == property.ListingID)
			{
				properties.get(i).Body= property.Body;
				properties.get(i).photo_large_urls = property.photo_large_urls;
				properties.get(i).photo_thumb_urls = property.photo_thumb_urls;
			}
		}

		property_list_adapter.notifyDataSetChanged();
		Property upatedProp = getSelectedProperty();
		upatedProp.Body = property.Body;


		return upatedProp;
	}

	public int getSelectItem() {

		return selected_item_position;

	}

	/*
	 * Return the currently selected property
	 */
	public Property getSelectedProperty() {
		if (property_list_adapter.getCount() > 0 && selected_item_position != NO_SELECTION) {
			return property_list_adapter.getItem(selected_item_position);
		} else {
			return null;
		}
	}

	public ArrayList<Property> getPropertyList() {
		return properties;
	}

	// sort the list of properties by different fields
	public void sortByTitle() {
		Collections.sort(properties, Property.COMPARE_BY_TITLE);
		property_list_adapter.notifyDataSetChanged();
		getListView().setSelection(0);
	}

	public void sortByPriceAsc() {
		Collections.sort(properties, Property.COMPARE_BY_PRICE_ASC);
		property_list_adapter.notifyDataSetChanged();
		getListView().setSelection(0);
	}

	public void sortByPriceDesc() {
		Collections.sort(properties, Property.COMPARE_BY_PRICE_DESC);
		property_list_adapter.notifyDataSetChanged();
		getListView().setSelection(0);
	}

}
