package org.stevej.android.propertyfinder.fragments;

import org.stevej.android.propertyfinder.interfaces.LocationTrackerListener;

import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

public class LocationTrackerFragment extends Fragment implements LocationListener, GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener {
	private static final String		TAG										= "LocationTrackerFragment";

	// identifier for when we use Play Services suggested resolution action to resolve a connection problem
	public static final int			CONNECTION_FAILURE_RESOLUTION_REQUEST	= 1;

	// represent state of location providers
	public static final int			ALL_PROVIDERS_ENABLED					= 0;
	public static final int			GPS_PROVIDER_DISABLED					= 1;
	public static final int			NETWORK_PROVIDER_DISABLED				= 2;
	public static final int			ALL_PROVIDERS_DISABLED					= 3;

	// configuration values (criteria) for our location request
	public static final int			MS_PER_SECOND							= 1000;
	public static final int			ACTIVE_UPDATE_INTERVAL_SECONDS			= 60;
	public static final int			PASSIVE_UPDATE_INTERVAL_SECONDS			= 30;
	public static final long		ACTIVE_UPDATE_INTERVAL_MS				= MS_PER_SECOND * ACTIVE_UPDATE_INTERVAL_SECONDS;
	public static final long		PASSIVE_UPDATE_INTERVAL_MS				= MS_PER_SECOND * PASSIVE_UPDATE_INTERVAL_SECONDS;
	public static final float		SMALLEST_LOCATION_DISPLACEMENT_METRES	= 50f;

	// the component that we advise of interesting location related events
	private LocationTrackerListener	listener;

	// the client that connects to Google Location Services
	private LocationClient			location_client							= null;

	// request object provided to Location Services defining the update criteria
	private LocationRequest			location_request;

	// keep current state of location, providers and whether to advise the listener of location updates
	private Location				current_location						= null;
	private Location				paused_location							= null;
	private boolean					updates_paused							= false;
	private int						provider_state							= ALL_PROVIDERS_ENABLED;

	// onReceive invoked when the system broadcasts that the state of the device's location providers has changed
	private BroadcastReceiver		location_provider_change_receiver		= new BroadcastReceiver() {
																				@Override
																				public void onReceive(Context context, Intent intent) {
																					Log.d(TAG, intent.toString());

																					provider_state = checkLocationProviders();
																				}
																			};

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Fragment#onAttach(android.app.Activity)
	 * 
	 * Use the hosting activity as the listener for interesting location related events
	 */
	@Override
	public void onAttach(Activity activity) {
		Log.d(TAG, "onAttach()");
		super.onAttach(activity);
		try {
			listener = (LocationTrackerListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement LocationTrackerListener");
		}

		setupLocationProviderChangeReceiver();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Fragment#onCreate(android.os.Bundle)
	 * 
	 * Create the location client and attempt to connect it to Location Services Initialise the location request object Check the initial state of the location
	 * providers and Play/Location Services
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate()");
		super.onCreate(savedInstanceState);

		location_client = new LocationClient(getActivity().getApplicationContext(), this, this);
		location_client.connect();

		location_request = LocationRequest.create();
		location_request.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
		location_request.setInterval(ACTIVE_UPDATE_INTERVAL_MS);
		location_request.setFastestInterval(PASSIVE_UPDATE_INTERVAL_MS);
		location_request.setSmallestDisplacement(SMALLEST_LOCATION_DISPLACEMENT_METRES);

		checkProvidersAndServices();

		setRetainInstance(true);
	}

	/*
	 * Dynamically register our receiver instance as wanting to receive LocationManager.PROVIDERS_CHANGED_ACTION intents that are broadcast by the system when
	 * the location provider states change
	 */
	private void setupLocationProviderChangeReceiver() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(LocationManager.PROVIDERS_CHANGED_ACTION);
		getActivity().registerReceiver(location_provider_change_receiver, filter);
	}

	/*
	 * Initial check when fragment created. Check if there are any disabled providers and whether we can find Play Services.
	 */
	private void checkProvidersAndServices() {
		provider_state = checkLocationProviders();
		if (provider_state != ALL_PROVIDERS_ENABLED) {
			listener.onDisabledLocationProvider(provider_state);
		}

		int play_services_state = getPlayServicesState();
		if (play_services_state != ConnectionResult.SUCCESS) {
			listener.onServiceUnavailable(new ConnectionResult(play_services_state, null), CONNECTION_FAILURE_RESOLUTION_REQUEST);
		}
	}

	/*
	 * Use LocationManager to find out which providers are enabled. Return a mask representing the state.
	 */
	private int checkLocationProviders() {
		LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
		boolean gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		boolean network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

		int result = ALL_PROVIDERS_ENABLED;
		if (!gps_enabled) {
			result = result | GPS_PROVIDER_DISABLED;
		}
		if (!network_enabled) {
			result = result | NETWORK_PROVIDER_DISABLED;
		}

		return result;
	}

	/*
	 * The host activity invokes this. Advise activity if we have no location providers, or we can't connect to Play Services. Otherwise request location
	 * updates using the configured request object that defines the update criteria.
	 */
	public void startTracking() {
		if (provider_state == ALL_PROVIDERS_DISABLED) {
			listener.onDisabledLocationProvider(provider_state);
		}

		int play_services_state = getPlayServicesState();

		if (play_services_state == ConnectionResult.SUCCESS) {
			location_client.requestLocationUpdates(location_request, this);
		} else {
			listener.onServiceUnavailable(new ConnectionResult(play_services_state, null), CONNECTION_FAILURE_RESOLUTION_REQUEST);
		}
	}

	/*
	 * The host activity invokes this. Remove update request if we are currently connected to the service.
	 */
	public void stopTracking() {
		if (getPlayServicesState() == ConnectionResult.SUCCESS) {
			location_client.removeLocationUpdates(this);
		}
	}

	/*
	 * The host activity invokes this. Set flag, record current location
	 */
	public void pauseUpdates() {
		updates_paused = true;
		paused_location = current_location;
	}

	/*
	 * The host activity invokes this to resume being advised of updated location.
	 */
	public void resumeUpdates() {
		// only resume if we are currently paused
		if (!updates_paused) {
			return;
		}

		// check we have at least on location provider
		if (provider_state == ALL_PROVIDERS_DISABLED) {
			listener.onDisabledLocationProvider(provider_state);
		}

		updates_paused = false;

		// had no location on pause, have no location now, can't do anything
		if (current_location == null && paused_location == null) {
			return;
		}

		// had no location on pause, have one now
		if (paused_location == null && current_location != null) {
			onLocationChanged(current_location);
		}
		// had location on pause, check if have different one now
		if (paused_location != null) {
			if (!current_location.equals(paused_location)) {
				onLocationChanged(current_location);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.google.android.gms.location.LocationListener#onLocationChanged(android.location.Location)
	 * 
	 * Invoked automatically when the location service has a new location. Advise the listener if we aren't paused.
	 */
	@Override
	public void onLocationChanged(Location location) {
		if (!updates_paused) {
			listener.onLocationChanged(location);
		}
		current_location = location;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks#onConnected(android.os.Bundle)
	 * 
	 * Invoked automatically when successfully connected to the service. Advise listener that we have successfully connect to the service
	 */
	@Override
	public void onConnected(Bundle bundle) {
		listener.onTrackerConnected();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Fragment#onDestroy()
	 * 
	 * Tidy up the update request and connection
	 */
	@Override
	public void onDestroy() {
		if (location_client.isConnected()) {
			location_client.removeLocationUpdates(this);
			location_client.disconnect();
		}
		super.onDestroy();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Fragment#onDetach()
	 * 
	 * This fragment is being removed from the host activity - unregister the provider state change receiver
	 */
	@Override
	public void onDetach() {
		getActivity().unregisterReceiver(location_provider_change_receiver);
		super.onDetach();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener#onConnectionFailed(com.google.android.gms.common.ConnectionResult)
	 * 
	 * Invoked automatically when attempt to connect to service failed. Advise listener.
	 */
	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		listener.onServiceUnavailable(connectionResult, CONNECTION_FAILURE_RESOLUTION_REQUEST);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks#onDisconnected()
	 * 
	 * Invoked automatically when disconnected from Services. Attempt a reconnection. We could limit retry attempts.
	 */
	@Override
	public void onDisconnected() {
		location_client.connect();
	}

	/*
	 * Utility to determine if Play Services available on device
	 */
	public int getPlayServicesState() {
		return GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity());
	}

}
