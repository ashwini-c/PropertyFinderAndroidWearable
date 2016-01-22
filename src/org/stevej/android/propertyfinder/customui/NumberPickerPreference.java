package org.stevej.android.propertyfinder.customui;

import org.stevej.android.propertyfinder.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.NumberPicker;

public class NumberPickerPreference extends DialogPreference {
	private static final String	TAG				= "NumberPickerPreference";
	private final int			default_value	= 10;
	private NumberPicker		number_picker;
	private int					current_value;

	public NumberPickerPreference(Context context, AttributeSet attrs) {
		super(context, attrs);

		setDialogLayoutResource(R.layout.numberpicker_dialog);
		setPositiveButtonText(android.R.string.ok);
		setNegativeButtonText(android.R.string.cancel);

		setDialogIcon(null);
		Log.d(TAG, "NumberPickerPreference()");
	}

	@Override
	protected void onBindDialogView(View view) {
		super.onBindDialogView(view);
		Log.d(TAG, "onBindDialogView()");

		number_picker = (NumberPicker) view.findViewById(R.id.num_to_load_picker);
		number_picker.setMinValue(1);
		number_picker.setMaxValue(50);
		number_picker.setValue(current_value);
		number_picker.setWrapSelectorWheel(true);
		number_picker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		if (positiveResult) {
			current_value = number_picker.getValue();
			persistInt(current_value);
		}
	}

	private void storeInitialValue(Integer value) {
		current_value = value;
		persistInt(current_value);
	}

	@Override
	protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
		if (restorePersistedValue) {
			storeInitialValue(getPersistedInt(current_value));
		} else {
			storeInitialValue((Integer) defaultValue);
		}
	}

	@Override
	protected Object onGetDefaultValue(TypedArray a, int index) {
		return a.getInteger(index, default_value);
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		final Parcelable superState = super.onSaveInstanceState();
		// Check whether this Preference is persistent (continually saved)
		if (isPersistent()) {
			// No need to save instance state since it's persistent, use
			// superclass state
			return superState;
		}

		// Create instance of custom BaseSavedState
		final SavedState myState = new SavedState(superState);
		// Set the state's value with the class member that holds current
		// setting value
		myState.value = current_value;
		return myState;
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		// Check whether we saved the state in onSaveInstanceState
		if (state == null || !state.getClass().equals(SavedState.class)) {
			// Didn't save the state, so call superclass
			super.onRestoreInstanceState(state);
			return;
		}

		// Cast state to custom BaseSavedState and pass to superclass
		SavedState myState = (SavedState) state;
		super.onRestoreInstanceState(myState.getSuperState());

		// Set this Preference's widget to reflect the restored state
		number_picker.setValue(myState.value);
	}

	private static class SavedState extends BaseSavedState {
		// Member that holds the setting's value
		// Change this data type to match the type saved by your Preference
		int	value;

		public SavedState(Parcelable superState) {
			super(superState);
		}

		public SavedState(Parcel source) {
			super(source);
			// Get the current preference's value
			value = source.readInt(); // Change this to read the appropriate
										// data type
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			super.writeToParcel(dest, flags);
			// Write the preference's value
			dest.writeInt(value); // Change this to write the appropriate data
									// type
		}

		// Standard creator object using an instance of this class
		public static final Parcelable.Creator<SavedState>	CREATOR	= new Parcelable.Creator<SavedState>() {

																		public SavedState createFromParcel(Parcel in) {
																			return new SavedState(in);
																		}

																		public SavedState[] newArray(int size) {
																			return new SavedState[size];
																		}
																	};
	}
}