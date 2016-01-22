package org.stevej.android.propertyfinder.trademe;

import org.json.JSONObject;
import org.stevej.android.propertyfinder.interfaces.TradeMeResponseListener;
import org.stevej.android.propertyfinder.model.PlaceMenuEntry;
import org.stevej.android.propertyfinder.utils.BitmapCache;

import android.app.Activity;
import android.app.Fragment;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

public class TradeMeClientFragment extends Fragment {
	private static final String		TAG					= "TradeMeClientFragment";

	public static final String		BASE_SEARCH_URL		= "http://api.trademe.co.nz/v1/Search/Property/Residential.json?";
	public static final String		BASE_LISTING_URL	= "http://api.trademe.co.nz/v1/Listings/";

	private static final String		REGION_TYPE			= "1";
	private static final String		DISTRICT_TYPE		= "2";
	private static final String		SUBURB_TYPE			= "3";

	private static final double		ONE_KM_LAT_LNG		= 0.009009009;

	// Volley queue and image loader
	private RequestQueue			request_queue		= null;
	private ImageLoader				image_loader		= null;

	private TradeMeResponseListener	response_listener;


	public TradeMeClientFragment() {
	}

	// ensure that the hosting activity implements the response listener interface
	@Override
	public void onAttach(Activity activity) {
		Log.d(TAG, "onAttach()");
		super.onAttach(activity);
		try {
			response_listener = (TradeMeResponseListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement TradeMeResponseListener");
		}
	}

	/**
	 * This method is called only once when the Fragment is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "onCreate(Bundle)");
		super.onCreate(savedInstanceState);

		// initialise the Volley queue and image loader
		request_queue = Volley.newRequestQueue(getActivity());
		image_loader = new ImageLoader(request_queue, new BitmapCache());

		// keep state across config changes (we don't lose the queue and loader)
		setRetainInstance(true);
	}

	public ImageLoader getImageLoader() {
		return image_loader;
	}

	public void cancelAllRequests() {
		request_queue.cancelAll(this);

	}

	@Override
	public void onStop() {
		Log.d(TAG, "onStop");
		super.onStop();
		cancelAllRequests();
	}

	// issue requests to trademe and return responses to the registered listener

	private void sendPropertyListRequest(String request_url) {
		Log.d(TAG, "sendPropertyListRequest : " + request_url);
		JsonObjectRequest request = new JsonObjectRequest(Method.GET, request_url, null, new Listener<JSONObject>() {
			public void onResponse(JSONObject json_object) {
				Log.d(TAG, "onResponse");
				response_listener.onTradeMePropertyListResponse(json_object);
			}
		}, new ErrorListener() {
			public void onErrorResponse(VolleyError error) {
				Log.d(TAG, "getPropertyList : onErrorResponse : " + error.getMessage());
				response_listener.onTradeMePropertyListResponse(null);
			}
		});

		request_queue.add(request);
	}

	public void getPropertyList(PlaceMenuEntry place, double distance, String num_properties) {
		Location location = place.location;

		if (location != null) {
			getPropertyList(location.getLatitude(), location.getLongitude(), distance, num_properties);
		} else {
			getPropertyList(place.place_type, place.place_id, place.district_id, place.region_id, num_properties);
		}
	}

	public void getPropertyList(String place_type, String suburb_id, String district_id, String region_id, String num_properties) {
		String request_url = BASE_SEARCH_URL + "region=" + region_id;

		if (place_type.equals(DISTRICT_TYPE) || place_type.equals(SUBURB_TYPE)) {
			request_url += "&district=" + district_id;
		}
		if (place_type.equals(SUBURB_TYPE)) {
			request_url += "&suburb=" + suburb_id;
		}
		request_url += "&rows=" + num_properties;
		sendPropertyListRequest(request_url);
	}

	public void getPropertyList(double latitude, double longitude, double distance, String num_properties) {
		double scope = ONE_KM_LAT_LNG * distance;
		double lat_min = latitude - scope;
		double lat_max = latitude + scope;
		double long_min = longitude - scope;
		double long_max = longitude + scope;

		final String request_url = BASE_SEARCH_URL + "latitude_min=" + lat_min + "&latitude_max=" + lat_max + "&longitude_min=" + long_min + "&longitude_max="
				+ long_max + "&rows=" + num_properties;

		sendPropertyListRequest(request_url);
	}

	public void getPropertyDetails(int property_id) {
		String request_url = BASE_LISTING_URL + property_id + ".json";
		JsonObjectRequest request = new JsonObjectRequest(Method.GET, request_url, null, new Listener<JSONObject>() {
			public void onResponse(JSONObject json_object) {
				response_listener.onTradeMePropertyDetailsResponse(json_object);
			}
		}, new ErrorListener() {

			public void onErrorResponse(VolleyError error) {
				Log.d(TAG, "getPropertyDetails : onErrorResponse : " + error.getMessage());
				response_listener.onTradeMePropertyDetailsResponse(null);

			}
		});
		request_queue.add(request);
	}

}
