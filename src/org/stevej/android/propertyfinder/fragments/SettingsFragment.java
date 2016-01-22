package org.stevej.android.propertyfinder.fragments;

import org.stevej.android.propertyfinder.R;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;

public class SettingsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {
	private static final String	TAG	= "SettingsFragment";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate");

		String settings = getArguments().getString("settings_group");
		if ("all_settings".equals(settings)) {
			addPreferencesFromResource(R.xml.settings_all);
			setTradeMeSummary();
		}
	}

	private void setTradeMeSummary() {
		Log.d(TAG, "setTradeMeSummary");

		Resources r = getResources();
		Preference num_properties_preference = findPreference(r.getString(R.string.pref_num_to_load_key));

		int val = PreferenceManager.getDefaultSharedPreferences(getActivity()).getInt(r.getString(R.string.pref_num_to_load_key),
				r.getInteger(R.integer.default_num_to_load));
		String summary = r.getString(R.string.pref_num_to_load_summary);
		num_properties_preference.setSummary(summary + " (currently " + val + ")");

	}

	@Override
	public void onResume() {
		super.onResume();
		PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		PreferenceManager.getDefaultSharedPreferences(getActivity()).unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		Log.d(TAG, "Setting changed : " + key);

		Resources r = getResources();
		Preference changed_preference = findPreference(key);

		if (changed_preference != null) {
			if (key.equals(r.getString(R.string.pref_num_to_load_key))) {
				int val = sharedPreferences.getInt(key, r.getInteger(R.integer.default_num_to_load));
				String summary = r.getString(R.string.pref_num_to_load_summary);
				changed_preference.setSummary(summary + " (currently " + val + ")");
			}
		}
	}

}
