package org.stevej.android.propertyfinder.providers;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

public class TradeMePlaceProvider extends ContentProvider {
	private static final String	TAG					= "TradeMePlaceProvider";

	private static final String	DATABASE_NAME		= "TradeMePlaces.db";
	private static final int	DATABASE_VERSION	= 1;
	private static final String	AUTHORITY			= "org.stevej.android.propertyfinder.providers.TradeMePlaceProvider";

	private static final int	GET_PLACE			= 0;
	private static final int	SEARCH_SUGGEST		= 1;

	private UriMatcher			uri_matcher			= null;
	private SQLiteAssetHelper	sqlite_asset_helper;

	@Override
	public boolean onCreate() {
		sqlite_asset_helper = new SQLiteAssetHelper(getContext(), DATABASE_NAME, null, DATABASE_VERSION);

		uri_matcher = new UriMatcher(UriMatcher.NO_MATCH);
		uri_matcher.addURI(AUTHORITY, "places/#", GET_PLACE);
		uri_matcher.addURI(AUTHORITY, "search_suggest_query", SEARCH_SUGGEST);

		return sqlite_asset_helper != null;
	}

	private Cursor getSuggestions(String user_text) {
		Log.d(TAG, "getSuggestions(" + user_text + ")");

		String sql = "SELECT LocationId AS " + BaseColumns._ID + ", Name AS " + SearchManager.SUGGEST_COLUMN_TEXT_1 + ", DistrictName AS "
				+ SearchManager.SUGGEST_COLUMN_TEXT_2 + ", LocationId AS " + SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID + " FROM places WHERE Name MATCH '"
				+ user_text + "*'";

		Cursor cursor = sqlite_asset_helper.getReadableDatabase().rawQuery(sql, null);
		Log.d(TAG, sql);

		return cursor;
	}

	private Cursor getPlace(String id) {
		Log.d(TAG, "getPlace(" + id + ")");
		String sql = "SELECT * FROM places WHERE LocationId=" + id + " LIMIT 1";
		Log.d(TAG, "getLocation: " + sql);
		Cursor cursor = sqlite_asset_helper.getReadableDatabase().rawQuery(sql, null);
		return cursor;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		Log.d(TAG, "          uri = " + uri.toString());
		Log.d(TAG, "        limit = " + uri.getQueryParameter(SearchManager.SUGGEST_PARAMETER_LIMIT));
		Log.d(TAG, "last path seg = " + uri.getLastPathSegment());

		if (selectionArgs != null) {
			for (int i = 0; i < selectionArgs.length; i++) {
				Log.d(TAG, "       arg[" + i + "] = " + selectionArgs[i]);
			}
		}
		Log.d(TAG, "    selection = " + selection);

		switch (uri_matcher.match(uri)) {
		case SEARCH_SUGGEST:
			return getSuggestions(selectionArgs[0]);
		case GET_PLACE:
			return getPlace(uri.getLastPathSegment());
		default:
			return null;
		}
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}
}
