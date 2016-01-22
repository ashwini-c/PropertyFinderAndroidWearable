package org.stevej.android.propertyfinder.fragments;

import org.stevej.android.propertyfinder.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

public class ProviderDisabledDialogFragment extends DialogFragment {

	private static final String	TAG	= "ProviderDisabledDialogFragment";

	public ProviderDisabledDialogFragment() {
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Log.d(TAG, "onCreateDialog()");

		// the argument given when the fragment was instantiated represents the state of the location providers
		int provider_state = getArguments().getInt("provider_state");
		boolean gps_disabled = (provider_state & LocationTrackerFragment.GPS_PROVIDER_DISABLED) == LocationTrackerFragment.GPS_PROVIDER_DISABLED;
		boolean network_disabled = (provider_state & LocationTrackerFragment.NETWORK_PROVIDER_DISABLED) == LocationTrackerFragment.NETWORK_PROVIDER_DISABLED;

		// inflate the dialog's custom UI from an XML layout, get the text view that displays the message and the 'Dont show again' checkbox
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View dialog_layout = inflater.inflate(R.layout.scrollable_text_dialog, null);
		TextView msg_view = (TextView) dialog_layout.findViewById(R.id.dialog_message);
		final CheckBox dont_show_again = (CheckBox) dialog_layout.findViewById(R.id.dialog_checkbox);

		// we are creating an AlertDialog
		AlertDialog.Builder dialog_builder = new AlertDialog.Builder(getActivity());

		// set the dialog's UI to be the custom layout inflated from XML
		dialog_builder.setView(dialog_layout);

		// set dialog's title bar content
		dialog_builder.setTitle("Location settings");

		// construct the message to appear and set it as the content of the TextView that displays it
		String msg = "PropertyFinder can use your location to find nearby properties. The following location sources are currently disabled:\n\n";
		if (gps_disabled) {
			msg += "  GPS\n";
		}
		if (network_disabled) {
			msg += "  WiFi/mobile network\n";
		}
		if (gps_disabled && network_disabled) {
			msg += "\nPropertyFinder's location feature is not available with these settings.\n";
		}
		msg += "\nClick the Settings button to change your Location Settings";
		msg_view.setText(msg);

		// when user clicks "Settings" button send them to the device Location settings
		dialog_builder.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				startActivity(intent);
			}
		});

		// no action to take if the user canceled the dialog
		dialog_builder.setNegativeButton("Done", null);

		// update the user's preference when the state of the 'Dont show again' checkbox changes
		dont_show_again.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
				SharedPreferences.Editor editor = preferences.edit();
				editor.putBoolean("pref_location_alerts", isChecked);
				editor.commit();
			}
		});

		// build the dialog and return it as this dialog fragment's content
		AlertDialog alert_dialog = dialog_builder.create();

		return alert_dialog;
	}
}