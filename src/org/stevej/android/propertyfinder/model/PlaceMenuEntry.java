package org.stevej.android.propertyfinder.model;

import android.location.Location;

public class PlaceMenuEntry {
	@Override
	public String toString() {
		return "NavigationEntry [place_name=" + place_name + ", place_id=" + place_id + ", district_id=" + district_id + ", region_id=" + region_id
				+ ", place_type=" + place_type + ", location=" + location + "]";
	}

	public String	place_name	= null;
	public String	place_id	= null;
	public String	district_id	= null;
	public String	region_id	= null;
	public String	place_type	= null;
	public Location	location	= null;

	public PlaceMenuEntry(String name, String region_id, String district_id, String place_id, String type, Location location) {
		this.place_name = name;
		this.region_id = region_id;
		this.district_id = district_id;
		this.place_type = type;
		this.place_id = place_id;
		this.location = location;
	}
}
