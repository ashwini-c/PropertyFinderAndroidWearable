package org.stevej.android.propertyfinder.model;

import java.util.ArrayList;
import java.util.Comparator;

public class Property {
	public int					ListingID;
	public String				Title;
	public String				Category;
	public String				PriceDisplay;
	public int					PriceNumeric;
	public String				ThumbURL;

	public boolean				HasGallery;

	public boolean				HasLocation;
	public boolean				HasAgency;
	public double				Latitude;
	public double				Longitude;
	public String				Address;
	public String				PropertyType;
	public String				Agency_name;
	public String				Agency_phone;

	public String				Body;
	public ArrayList<String>	photo_thumb_urls	= new ArrayList<String>();
	public ArrayList<String>	photo_large_urls	= new ArrayList<String>();

	public Property() {

	}

	public static Comparator<Property>	COMPARE_BY_TITLE		= new Comparator<Property>() {
																	public int compare(Property one, Property other) {
																		return one.Title.compareTo(other.Title);
																	}
																};
	public static Comparator<Property>	COMPARE_BY_PRICE_ASC	= new Comparator<Property>() {
																	public int compare(Property one, Property other) {
																		return one.PriceNumeric - other.PriceNumeric;
																	}
																};
	public static Comparator<Property>	COMPARE_BY_PRICE_DESC	= new Comparator<Property>() {
																	public int compare(Property one, Property other) {
																		return other.PriceNumeric - one.PriceNumeric;
																	}
																};

}
