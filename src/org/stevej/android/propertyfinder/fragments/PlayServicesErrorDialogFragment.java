package org.stevej.android.propertyfinder.fragments;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;

import com.google.android.gms.common.GooglePlayServicesUtil;

public class PlayServicesErrorDialogFragment extends DialogFragment {

	private static final String	TAG	= "PlayServicesErrorDialogFragment";

	public PlayServicesErrorDialogFragment() {
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		int error_code = this.getArguments().getInt("error_code");

		// Play Services can construct an appropriate dialog given the error code. Use this as this dialog fragment's content
		Dialog error_dialog = GooglePlayServicesUtil.getErrorDialog(error_code, getActivity(), 0);

		return error_dialog;
	}
}