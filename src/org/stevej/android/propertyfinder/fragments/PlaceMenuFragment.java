package org.stevej.android.propertyfinder.fragments;

import java.util.ArrayList;

import org.stevej.android.propertyfinder.adapters.PlaceMenuListAdapter;
import org.stevej.android.propertyfinder.model.PlaceMenuEntry;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;

/*
 * Manages the data/adapter for the items that appear in the action bar nav menu (list of places)
 */
public class PlaceMenuFragment extends Fragment {
	private static final String			TAG	= "PlaceMenuFragment";

	private PlaceMenuListAdapter		menu_list_adapter;
	private ArrayList<PlaceMenuEntry>	menu_entries;

	public PlaceMenuFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate()");
		super.onCreate(savedInstanceState);
		menu_entries = new ArrayList<PlaceMenuEntry>();


		setRetainInstance(true);
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

		menu_list_adapter = new PlaceMenuListAdapter(getActivity().getActionBar().getThemedContext(), android.R.layout.simple_list_item_1, android.R.id.text1,
				menu_entries);
	}

	public PlaceMenuListAdapter getAdapter() {
		return menu_list_adapter;
	}

	public void addAll(ArrayList<PlaceMenuEntry> entries) {
		menu_entries.addAll(entries);
		menu_list_adapter.notifyDataSetChanged();
	}

	public int addEntry(PlaceMenuEntry entry) {
		menu_entries.add(entry);
		menu_list_adapter.notifyDataSetChanged();

		Log.d(TAG, "addEntry() : size = " + menu_entries.size());

		return menu_entries.size() - 1;
	}

	public void addEntry(int position, PlaceMenuEntry entry) {
		menu_entries.add(position, entry);
		menu_list_adapter.notifyDataSetChanged();
	}

	public int lastIndexOf(PlaceMenuEntry entry) {
		return menu_entries.lastIndexOf(entry);
	}

	public ArrayList<PlaceMenuEntry> getEntries() {
		return menu_entries;
	}

	public PlaceMenuEntry getEntry(int position) {
		return menu_entries.get(position);
	}

	public PlaceMenuEntry getEntry(String name) {
		for (int i = 0; i < menu_entries.size(); i++) {
			PlaceMenuEntry pme = menu_entries.get(i);
			if (pme.place_name.equals(name)) {
				return pme;
			}
		}
		return null;
	}

	public int size() {
		return menu_entries.size();
	}

	public boolean containsEntry(PlaceMenuEntry entry) {

		for (int i = 0; i < menu_entries.size(); i++) {
			PlaceMenuEntry pme = menu_entries.get(i);
			if (pme.place_name.equals(entry.place_name) && pme.place_id.equals(entry.place_id)) {
				return true;
			}
		}
		return false;
	}

}
