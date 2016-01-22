package org.stevej.android.propertyfinder.interfaces;

import org.json.JSONObject;

public interface TradeMeResponseListener {
	public void onTradeMePropertyListResponse(JSONObject json_object);

	public void onTradeMePropertyDetailsResponse(JSONObject json_object);
}