package org.stevej.android.propertyfinder.interfaces;

import android.location.Location;

import com.google.android.gms.common.ConnectionResult;


public interface LocationTrackerListener {
	public void onTrackerConnected();

	public void onLocationChanged(Location location);

	public void onServiceUnavailable(ConnectionResult connection_result, int resolution_request_id);

	public void onDisabledLocationProvider(int provider_state);
}