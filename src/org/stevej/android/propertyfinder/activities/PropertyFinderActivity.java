package org.stevej.android.propertyfinder.activities;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;
import org.stevej.android.propertyfinder.R;
import org.stevej.android.propertyfinder.adapters.PropertyListAdapter;
import org.stevej.android.propertyfinder.fragments.LocationTrackerFragment;
import org.stevej.android.propertyfinder.fragments.PlaceMenuFragment;
import org.stevej.android.propertyfinder.fragments.PlayServicesErrorDialogFragment;
import org.stevej.android.propertyfinder.fragments.PropertyDetailsFragment;
import org.stevej.android.propertyfinder.fragments.PropertyListFragment;
import org.stevej.android.propertyfinder.fragments.ProviderDisabledDialogFragment;
import org.stevej.android.propertyfinder.interfaces.LocationTrackerListener;
import org.stevej.android.propertyfinder.interfaces.PropertySelectionListener;
import org.stevej.android.propertyfinder.interfaces.TradeMeResponseListener;
import org.stevej.android.propertyfinder.model.PlaceMenuEntry;
import org.stevej.android.propertyfinder.model.Property;
import org.stevej.android.propertyfinder.trademe.JSONParser;
import org.stevej.android.propertyfinder.trademe.TradeMeClientFragment;
import org.stevej.android.propertyfinder.utils.CustomAnimator;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.Notification;
import android.app.Notification.Style;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.ShareActionProvider;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;
import com.google.android.gms.common.ConnectionResult;

import android.preview.support.wearable.notifications.*;
import android.preview.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.BigTextStyle;

public class PropertyFinderActivity extends Activity implements TradeMeResponseListener, PropertySelectionListener, ActionBar.OnNavigationListener,
LocationTrackerListener, OnFocusChangeListener, OnQueryTextListener {

	public static final String EXTRA_REPLY =
			"com.example.android.preview.support.wearable.notifications.REPLY";

	private static final String		TAG								= "PropertyFinderActivity";

	// tags to identify fragments
	private static final String		TRADEME_CLIENT_FRAGMENT_TAG		= "TradeMeClientFragment";
	private static final String		PROPERTY_LIST_FRAGMENT_TAG		= "PropertyListFragment";
	private static final String		PROPERTY_DETAILS_FRAGMENT_TAG	= "PropertyDetailsFragment";
	private static final String		LOCATION_TRACKER_FRAGMENT_TAG	= "LocationTrackerFragment";
	private static final String		PLACE_MENU_FRAGMENT_TAG			= "PlaceMenuFragment";
	private static final String		ERROR_DIALOG_FRAGMENT_TAG		= "ErrorDialogFragment";
	private static final String		ALERT_DIALOG_FRAGMENT_TAG		= "AlertDialogFragment";

	// keys for saving instance state data
	private static final String		SELECTED_SUBURB_IDX				= "SELECTED_SUBURB_IDX";
	private static final String		SEARCH_VIEW_TEXT				= "SEARCH_VIEW_TEXT";
	private static final String		SEARCH_VIEW_EXPANDED			= "SEARCH_VIEW_EXPANDED";

	// no initial nav menu selection
	private static final int		NO_SELECTION					= -1;

	// intent action we handle when user selects a place suggestion from the search widget
	private static final String		ADD_PLACE_ACTION				= "org.stevej.android.propertyfinder.action.ADD_PLACE";

	// stores position of currently selected place from menu so that we can store/restore it on config change
	private int						selected_place_position			= NO_SELECTION;

	// stores device orientation
	private int						orientation;

	// fragment manager and fragments
	private FragmentManager			fragment_manager;
	private PropertyListFragment	property_list_fragment			= null;
	private PropertyDetailsFragment	property_details_fragment		= null;
	private TradeMeClientFragment	trademe_client					= null;
	private LocationTrackerFragment	location_tracker				= null;
	private PlaceMenuFragment		place_menu						= null;

	// we set these up but aren't using them (yet)
	private ShareActionProvider		share_action_provider;
	private Intent					share_intent;

	// holds action bar search widget text that we save/restore on configuration change
	private CharSequence			query_text						= "";

	// holds action bar search widget state that we save/restore on configuration change
	boolean							search_view_expanded			= false;

	// action bar components
	private MenuItem				favourite_menu_item;
	private MenuItem				search_menu_item;


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.stevej.android.propertyfinder.activities.NavigationEnabledActivity #onCreate(android.os.Bundle)
	 * 
	 * Call superclass's onCreate. Set the content view (UI layout) of the superclass (NavigationEnabledActivity) to whatever is appropriate for this activity.
	 * 
	 * Do any initialisation that is specific to this activity, but does not require the UI to have been initialised.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "\n\nonCreate()");
		super.onCreate(savedInstanceState);

		// set UI layout from resource file
		super.setContentView(R.layout.property_finder);

		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

		StrictMode.setThreadPolicy(policy);

		// set preferences defaults if they have never been set in the past
		PreferenceManager.setDefaultValues(this, R.xml.settings_all, true);

		// get the activity's fragment manager
		fragment_manager = getFragmentManager();

		// get current orientation
		orientation = getResources().getConfiguration().orientation;

		// add the trademe client, property list, property details, place menu and location tracker fragments to this activity
		addFragments();

		// if we have saved state after a configuration change we can restore the previous suburb selection and search widget state in the
		// menu
		if (savedInstanceState != null) {
			selected_place_position = savedInstanceState.getInt(SELECTED_SUBURB_IDX);

			if (savedInstanceState.containsKey(SEARCH_VIEW_TEXT)) {
				query_text = savedInstanceState.getCharSequence(SEARCH_VIEW_TEXT);
			}

			if (savedInstanceState.containsKey(SEARCH_VIEW_EXPANDED)) {
				search_view_expanded = savedInstanceState.getBoolean(SEARCH_VIEW_EXPANDED);
			}
		}

		// set up the intent for sharing data (sharing not yet implemented)
		configureShareIntent();

		logAssetsAndAppInfo();

	}

	public void logAssetsAndAppInfo() {
		AssetManager asset_manager = getAssets();
		String[] assets;
		try {
			assets = asset_manager.list("/");
			for (int i = 0; i < assets.length; i++) {
				Log.d(TAG, assets[i]);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		Log.d(TAG, "app data dir = " + getApplicationInfo().dataDir);
		traverse(new File(getApplicationInfo().dataDir));
	}

	public void traverse(File f) {
		Log.d(TAG, f.getAbsolutePath() + "/");
		File files[] = f.listFiles();
		for (int i = 0; i < files.length; i++) {
			if (files[i].isDirectory()) {
				traverse(files[i]);
			} else {
				Log.d(TAG, files[i].getAbsolutePath());
			}
		}
	}

	/*
	 * Invoked when the system has identified this activity as able to handle a particular intent In this case it is when the user selects a search suggestion
	 * (a place) and the required action is to add it to the action bar nav menu. The intent data will be of the form
	 * content://org.stevej.android.propertyfinder.providers.TradeMePlaceProvider/places/<id> identifying the content provider that can provide the place data
	 * and the id of the place to add
	 */
	@Override
	protected void onNewIntent(Intent intent) {
		String intent_action = intent.getAction();
		if (ADD_PLACE_ACTION.equals(intent_action)) {
			Uri data_uri = intent.getData();

			ContentResolver cr = getContentResolver();
			Cursor cursor = cr.query(data_uri, null, null, null, null);

			if (cursor.getCount() != 0) {
				cursor.moveToFirst();
				String place_name = cursor.getString(cursor.getColumnIndexOrThrow("Name"));
				String place_type = cursor.getString(cursor.getColumnIndexOrThrow("Type"));
				String place_id = cursor.getString(cursor.getColumnIndexOrThrow("LocationId"));
				String district_id = cursor.getString(cursor.getColumnIndexOrThrow("DistrictID"));
				String region_id = cursor.getString(cursor.getColumnIndexOrThrow("RegionId"));

				PlaceMenuEntry entry = new PlaceMenuEntry(place_name, region_id, district_id, place_id, place_type, null);

				if (!place_menu.containsEntry(entry)) {
					selected_place_position = place_menu.addEntry(entry);
				}
			}
			search_menu_item.collapseActionView();
		}
	}

	/*
	 * get references to the fragments. If we haven't already got the fragments (this is a 'clean' start of the activity) then create the fragment instances and
	 * add them to their UI containers
	 */
	private void addFragments() {
		Log.d(TAG, "addFragments()");

		property_list_fragment = (PropertyListFragment) fragment_manager.findFragmentByTag(PROPERTY_LIST_FRAGMENT_TAG);
		property_details_fragment = (PropertyDetailsFragment) fragment_manager.findFragmentByTag(PROPERTY_DETAILS_FRAGMENT_TAG);
		trademe_client = (TradeMeClientFragment) fragment_manager.findFragmentByTag(TRADEME_CLIENT_FRAGMENT_TAG);
		location_tracker = (LocationTrackerFragment) fragment_manager.findFragmentByTag(LOCATION_TRACKER_FRAGMENT_TAG);
		place_menu = (PlaceMenuFragment) fragment_manager.findFragmentByTag(PLACE_MENU_FRAGMENT_TAG);

		FragmentTransaction ft = fragment_manager.beginTransaction();

		if (place_menu == null) {
			place_menu = new PlaceMenuFragment();
			ft.add(place_menu, PLACE_MENU_FRAGMENT_TAG);
		}

		if (location_tracker == null) {
			location_tracker = new LocationTrackerFragment();
			ft.add(location_tracker, LOCATION_TRACKER_FRAGMENT_TAG);
		}

		if (trademe_client == null) {
			trademe_client = new TradeMeClientFragment();
			ft.add(trademe_client, TRADEME_CLIENT_FRAGMENT_TAG);
		}

		if (property_list_fragment == null) {
			property_list_fragment = new PropertyListFragment();
			ft.add(R.id.property_list_container, property_list_fragment, PROPERTY_LIST_FRAGMENT_TAG);
		}

		if (property_details_fragment == null) {
			property_details_fragment = new PropertyDetailsFragment();
			ft.add(R.id.property_details_container, property_details_fragment, PROPERTY_DETAILS_FRAGMENT_TAG);
		}

		ft.commit();
		fragment_manager.executePendingTransactions();

	}

	/*
	 * We will eventually add sharing functionality via the Share ActionBar item. Create an initial Intent that we'll use to launch the app that will be used to
	 * share data.
	 */
	private void configureShareIntent() {
		share_intent = new Intent(Intent.ACTION_SEND);
		share_intent.setType("*/*");
	}

	/*
	 * Configure the ActionBar appearance and behaviour. Instead of a predefined list of items use our PlaceMenu's adapter to provide the entries for the
	 * ActionBar navigation drop down list. Register this activity to handle navigation selections. Set the initial selection.
	 */
	private void configureActionBar() {
		Log.d(TAG, "configureActionBar()");

		ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setDisplayHomeAsUpEnabled(false);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		if (place_menu != null) {
			actionBar.setListNavigationCallbacks(place_menu.getAdapter(), this);
		}

		actionBar.setSelectedNavigationItem(selected_place_position);
	}

	/******************************************
	 * 
	 * Handlers for responses returned by the requests to the Trademe service
	 * 
	 ******************************************/

	// Parse the trademe response and update the list fragment with the new data
	@Override
	public void onTradeMePropertyListResponse(JSONObject json_object) {
		Log.d(TAG, "onTradeMePropertyListResponse()");
		if (json_object != null) {

			property_list_fragment.update(JSONParser.parsePropertyListJSON(json_object));
		} else {
			property_list_fragment.clear();
			Toast.makeText(this, "Could not load properties", Toast.LENGTH_LONG).show();
		}
	}

	// not needed yet. We'll use it when we need more info about a property (eg its photos)
	@Override
	public void onTradeMePropertyDetailsResponse(JSONObject json_object) {
		// TODO Auto-generated method stub
		if(json_object!=null)
		{
			property_list_fragment.updatePropertyDetails(JSONParser.parsePropertyDetailsJSON(json_object));
		}
	}

	/******************************************
	 * 
	 * User input handlers
	 * 
	 ******************************************/

	// hide the search widget if it loses focus so that the action bar title is visible
	@Override
	public void onFocusChange(View arg0, boolean has_focus) {
		if (!has_focus) {
			search_menu_item.collapseActionView();
		}
	}

	@Override
	public boolean onQueryTextChange(String newText) {
		query_text = newText;
		return true;
	}

	@Override
	public boolean onQueryTextSubmit(String query) {
		// TODO Auto-generated method stub
		return true;
	}

	/*
	 * ActionBar drop down nav list selection handler. Issue a request to the trademe service for properties in the given suburb
	 */
	@Override
	public boolean onNavigationItemSelected(int position, long id) {
		Log.d(TAG, "onNavigationItemSelected : " + position);

		selected_place_position = position;
		PlaceMenuEntry entry = place_menu.getEntry(position);

		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		String num_to_load = Integer.toString(preferences.getInt("pref_num_to_load", 10));
		if (trademe_client != null) {
			trademe_client.getPropertyList(entry, 5, num_to_load);
		}

		if (property_list_fragment != null) {
			property_list_fragment.setIsLoading(true);
		}

		if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
			property_details_fragment.clear();
		}
		return true;
	}

	/*
	 * ActionBar items selection handler
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_sort_alpha:
			property_list_fragment.sortByTitle();
			return true;
		case R.id.action_sort_price_asc:
			property_list_fragment.sortByPriceAsc();
			return true;
		case R.id.action_sort_price_desc:
			property_list_fragment.sortByPriceDesc();
			return true;
		case R.id.action_legal:
			Toast.makeText(this, "Legal", Toast.LENGTH_SHORT).show();
			return true;
		case R.id.action_map:
			Toast.makeText(this, "Map", Toast.LENGTH_SHORT).show();
			return true;
		case R.id.action_single_screen_settings:
			startActivity(new Intent(this, SingleScreenSettingsActivity.class));
			return true;
		case R.id.action_dump_sensors:
			SensorManager sensor_manager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

			List<Sensor> device_sensors = sensor_manager.getSensorList(Sensor.TYPE_ALL);

			for (int i = 0; i < device_sensors.size(); i++) {
				Sensor sensor = device_sensors.get(i);
				Log.d(TAG, "      name : " + sensor.getName());
				Log.d(TAG, "    vendor : " + sensor.getVendor());
				Log.d(TAG, "      type : " + sensor.getType());
				Log.d(TAG, " max range : " + sensor.getMaximumRange());
				Log.d(TAG, "     power : " + sensor.getPower());
				Log.d(TAG, " min delay : " + sensor.getMinDelay());
				Log.d(TAG, "   version : " + sensor.getVersion());
				Log.d(TAG, "resolution : " + sensor.getResolution());
			}
			return true;
		case R.id.action_send_notofication:
			if(property_list_fragment.getSelectItem() == NO_SELECTION)
			{
				Toast.makeText(getApplicationContext(), "No Property Selected", Toast.LENGTH_SHORT).show();
			}
			else
			{


				Property prop = property_list_fragment.getSelectedProperty();

				Bitmap bitmap = getBitmapFromURL(prop.photo_large_urls.get(0));

				int notificationId = 001;
				// Build intent for notification content
				Intent viewIntent = new Intent(this, PropertyFinderActivity.class);
				//viewIntent.putExtra(EXTRA_EVENT_ID, eventId);
				PendingIntent viewPendingIntent =
						PendingIntent.getActivity(this, 0, viewIntent, 0);

				Log.d("SendNotification",prop.photo_large_urls.get(0) + "......." +bitmap +"........");

				NotificationCompat.InboxStyle inboxStyle =
						new NotificationCompat.InboxStyle();
				String[] events = new String[3];
				events[0] = prop.Address;
				events[1]=prop.PriceDisplay;
				events[2]=prop.PropertyType;
				// Sets a title for the Inbox style big view
				inboxStyle.setBigContentTitle(prop.Title);

				// Moves events into the big view
				for (int i=0; i < events.length; i++) {

					inboxStyle.addLine(events[i]);
				}



				String uriBegin = "geo:" + prop.Latitude + "," + prop.Longitude;
				String query = prop.Latitude + "," + prop.Longitude + "(" + prop.Title + ")";
				String encodedQuery = Uri.encode(query);
				String uriString = uriBegin + "?q=" + encodedQuery + "&z=16";
				Uri uri = Uri.parse(uriString);



				Intent mapIntent = new Intent(Intent.ACTION_VIEW,uri);

				PendingIntent mapPendingIntent =
						PendingIntent.getActivity(this, 0, mapIntent, 0);



				RemoteInput remoteInput = new RemoteInput.Builder(EXTRA_REPLY)
				.setLabel("Do you like it?")
				.setChoices(new String[] { "Yes",
						"No", "Not Sure" })
						.build();

				WearableNotifications.Action action = new WearableNotifications.Action.Builder(
						R.drawable.ic_full_reply,
						"Do you like it?",
						viewPendingIntent)
				.addRemoteInput(remoteInput)
				.build();


				NotificationCompat.Builder notificationBuilder =
						new NotificationCompat.Builder(this)
				.setSmallIcon(R.drawable.ic_launcher)
				.setLargeIcon(bitmap)
				.setStyle(inboxStyle)
				.addAction(R.drawable.ic_action_map,
						"View on map", mapPendingIntent);


				BigTextStyle secondPageStyle = new NotificationCompat.BigTextStyle();
				secondPageStyle.setBigContentTitle("Description")
				.bigText(prop.Body);

				// Create second page notification
				Notification secondPageNotification =
						new NotificationCompat.Builder(this)
				.setStyle(secondPageStyle)
				.build();

				// Create main notification and add the second page
				Notification twoPageNotification =
						new WearableNotifications.Builder(notificationBuilder)
				.addPage(secondPageNotification)
				.addAction(action)
				.build();

				// Get an instance of the NotificationManager service
				NotificationManagerCompat notificationManager =
						NotificationManagerCompat.from(this);

				// Build the notification and issues it with notification manager.
				notificationManager.notify(notificationId, twoPageNotification);


			}
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public static Bitmap getBitmapFromURL(String src) {
		try {
			URL url = new URL(src);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setDoInput(true);
			connection.connect();
			InputStream input = connection.getInputStream();
			Bitmap myBitmap = BitmapFactory.decodeStream(input);
			return myBitmap;
		} catch (IOException e) {
			return null;
		}
	}


	/*
	 * If we are in portrait orientation and showing the property details we treat a click on the back button as returning to the property list - reverse the
	 * previous animation to redisplay the property list. Otherwise use the default behaviour.
	 */
	@Override
	public void onBackPressed() {
		if (CustomAnimator.hasHistory()) {
			CustomAnimator.reversePrevious();
		} else {
			super.onBackPressed();
		}
	}

	/*
	 * Invoked from property list fragment when item is clicked
	 */
	@Override
	public void onPropertySelected(Property property) {
		property_details_fragment.update(property);
		trademe_client.getPropertyDetails(property.ListingID);
		if (orientation == Configuration.ORIENTATION_PORTRAIT) {
			showPropertyDetails();
		}
	}

	/******************************************
	 * 
	 * Utility methods
	 * 
	 ******************************************/

	private void showPropertyDetails() {
		View property_list_container = findViewById(R.id.property_list_container);
		View property_details_container = findViewById(R.id.property_details_container);

		CustomAnimator.slide(property_details_container, property_list_container, CustomAnimator.DIRECTION_LEFT, 400);
	}

	public ImageLoader getImageLoader() {
		if (trademe_client != null) {
			return trademe_client.getImageLoader();
		} else {
			return null;
		}
	}

	/******************************************
	 * 
	 * Overridden lifecycle methods
	 * 
	 ******************************************/

	/*
	 * Pause receipt of location updates.
	 */
	@Override
	protected void onPause() {
		Log.d(TAG, "onPause");
		super.onPause();

		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		boolean use_location = preferences.getBoolean("pref_allow_location", true);

		if (use_location && location_tracker != null) {
			location_tracker.pauseUpdates();
		}
	}

	// now the UI exists and is visible to the user. We can configure the action bar and catch up with location updates
	@Override
	protected void onResume() {
		Log.d(TAG, "onResume()");
		super.onResume();
		configureActionBar();

		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		boolean use_location = preferences.getBoolean("pref_allow_location", true);

		if (use_location && location_tracker != null) {
			location_tracker.resumeUpdates();
		}
	}

	// make sure all pending network requests are cancelled when this activity stops
	@Override
	protected void onStop() {
		Log.d(TAG, "onStop");
		super.onStop();
		if (trademe_client != null) {
			trademe_client.cancelAllRequests();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onSaveInstanceState(android.os.Bundle)
	 * 
	 * Save the current suburb selection in the action bar nav menu, and the state of the search widget
	 */
	@Override
	public void onSaveInstanceState(Bundle outState) {
		Log.d(TAG, "onSaveInstanceState");
		int selected_suburb_idx = getActionBar().getSelectedNavigationIndex();
		outState.putInt(SELECTED_SUBURB_IDX, selected_suburb_idx);

		outState.putCharSequence(SEARCH_VIEW_TEXT, query_text);

		if (search_menu_item != null) {
			outState.putBoolean(SEARCH_VIEW_EXPANDED, search_menu_item.isActionViewExpanded());
		}

		super.onSaveInstanceState(outState);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 * 
	 * Configure the action bar action entries. Set up the search widget to to use the configuration in searchable.xml which enables search suggestions as the
	 * user types.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Log.d(TAG, "onCreateOptionsMenu()");

		getMenuInflater().inflate(R.menu.action_bar, menu);

		share_action_provider = (ShareActionProvider) menu.findItem(R.id.action_share).getActionProvider();
		share_action_provider.setShareHistoryFileName(ShareActionProvider.DEFAULT_SHARE_HISTORY_FILE_NAME);
		share_action_provider.setShareIntent(share_intent);

		search_menu_item = (MenuItem) menu.findItem(R.id.action_search);
		SearchView search_view = (SearchView) search_menu_item.getActionView();

		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		ComponentName activity_name = getComponentName();
		SearchableInfo searchable_info = searchManager.getSearchableInfo(activity_name);
		search_view.setSearchableInfo(searchable_info);

		search_view.setSubmitButtonEnabled(false);
		search_view.setQueryHint("Suburb name");

		// if it was expanded before a config change, expand it again and hide the keyboard
		if (search_view_expanded) {
			search_menu_item.expandActionView();
			search_view.clearFocus();
			search_view.setQuery(query_text, false);
		}

		search_view.setOnQueryTextListener(this);
		search_view.setOnQueryTextFocusChangeListener(this);

		return true;
	}

	/**************************************************************
	 * 
	 * Location related callbacks for the LocationTrackerFragment
	 * 
	 *************************************************************/

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.stevej.android.propertyfinder.fragments.LocationTrackerFragment.LocationTrackerListener#onTrackerConnected()
	 * 
	 * There has been a successful connection to Google Play Services/Location Services Start receiving location updates
	 */
	public void onTrackerConnected() {
		Log.d(TAG, "onTrackerConnected()");
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		boolean use_location = preferences.getBoolean("pref_allow_location", true);

		if (use_location) {
			location_tracker.startTracking();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.stevej.android.propertyfinder.fragments.LocationTrackerFragment.LocationTrackerListener#onLocationChanged(android.location.Location )
	 * 
	 * The location tracker has provided an updated location for the 'Nearby' menu entry. Create and add it if it doesn't exist, otherwise update it.
	 */
	public void onLocationChanged(Location new_location) {
		Log.d(TAG, "onLocationChanged()");

		if (place_menu == null) {
			return;
		}
		if (place_menu.getEntry("Nearby") == null) {
			PlaceMenuEntry nearby = new PlaceMenuEntry("Nearby", null, null, null, null, new_location);
			place_menu.addEntry(0, nearby);
		} else {
			place_menu.getEntry("Nearby").location = new_location;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.stevej.android.propertyfinder.fragments.LocationTrackerFragment.LocationTrackerListener#onDisabledLocationProvider(int)
	 * 
	 * Invoked when the tracker has identified that at least one location source (GPS, WiFi/mobile network) is disabled. Alert the user if necessary.
	 */
	public void onDisabledLocationProvider(int provider_state) {
		Log.d(TAG, "onDisabledLocationProvider()");

		// only show the alert if the user has not selected 'Dont show again' in a previous dialog, or chosen to not see them in the app
		// Settings
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		boolean show_alerts = preferences.getBoolean("pref_location_alerts", true);
		if (!show_alerts) {
			return;
		}

		// we may be showing a previous dialog which is now out of date. Dismiss it.
		ProviderDisabledDialogFragment pddf = (ProviderDisabledDialogFragment) fragment_manager.findFragmentByTag(ALERT_DIALOG_FRAGMENT_TAG);
		if (pddf != null) {
			pddf.dismiss();
		}

		// create and show a new dialog, providing it with the current state of the location providers
		pddf = new ProviderDisabledDialogFragment();
		Bundle args = new Bundle();
		args.putInt("provider_state", provider_state);
		pddf.setArguments(args);
		pddf.show(fragment_manager, ALERT_DIALOG_FRAGMENT_TAG);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
	 * 
	 * If Play Services was unavailable and there was a possible resolution identified by the Play Services framework we will have started the resolution
	 * action. This will provide a result to this method.
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		Log.d(TAG, "onActivityResult()");
		switch (requestCode) {
		case LocationTrackerFragment.CONNECTION_FAILURE_RESOLUTION_REQUEST:
			switch (resultCode) {
			case Activity.RESULT_OK:
				break;
			default:
				break;
			}
		default:
			break;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.stevej.android.propertyfinder.fragments.LocationTrackerFragment.LocationTrackerListener#onServiceUnavailable(com.google.android .gms.common.
	 * ConnectionResult)
	 * 
	 * Invoked when the tracker has identified that Play Services is not available. Either launch the the provided resolution action or show a dialog alerting
	 * the user.
	 */
	public void onServiceUnavailable(ConnectionResult connection_result, int resolution_request_id) {
		Log.d(TAG, "onServiceUnavailable()");

		// dismiss an existing dialog that may be visible
		PlayServicesErrorDialogFragment pedf = (PlayServicesErrorDialogFragment) fragment_manager.findFragmentByTag(ERROR_DIALOG_FRAGMENT_TAG);
		if (pedf != null) {
			pedf.dismiss();
		}

		if (connection_result.hasResolution()) {
			try {
				connection_result.startResolutionForResult(this, resolution_request_id);
			} catch (IntentSender.SendIntentException e) {
				e.printStackTrace();
			}
		} else {
			pedf = new PlayServicesErrorDialogFragment();
			Bundle args = new Bundle();
			args.putInt("error_code", connection_result.getErrorCode());
			pedf.setArguments(args);
			pedf.show(fragment_manager, ERROR_DIALOG_FRAGMENT_TAG);
		}
	}

}
