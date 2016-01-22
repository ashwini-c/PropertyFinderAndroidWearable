package org.stevej.android.propertyfinder.activities;

import org.stevej.android.propertyfinder.fragments.SettingsFragment;

import android.app.Activity;
import android.os.Bundle;

public class SingleScreenSettingsActivity extends Activity{
	private static final String	TAG	= "SingleScreenSettingsActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		SettingsFragment settings_fragment = new SettingsFragment();

		Bundle args = new Bundle();
		args.putString("settings_group", "all_settings");
		settings_fragment.setArguments(args);

		getFragmentManager().beginTransaction().replace(android.R.id.content, settings_fragment).commit();

	}
}
