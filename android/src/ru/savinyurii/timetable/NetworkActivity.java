package ru.savinyurii.timetable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

import ru.savinyurii.timetable.R;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.Toast;

/**
 * @author SavinYurii
 * 
 */
public class NetworkActivity extends Activity {
	public static final String WIFI = "Wi-Fi";
	public static final String ANY = "Any";
	private static final String URL = "http://test.savinyurii.ru/getHtmlTable.php?group=14925"; //"http://stackoverflow.com/feeds/tag?tagnames=android&sort=newest";

	// Whether there is a Wi-Fi connection.
	private static boolean wifiConnected = false;
	// Whether there is a mobile connection.
	private static boolean mobileConnected = false;
	// Whether the display should be refreshed.
	public static boolean refreshDisplay = true;

	// The user's current network preference setting.
	public static String sPref = null;

	// The BroadcastReceiver that tracks network connectivity changes.
	private NetworkReceiver receiver = new NetworkReceiver();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d("myLog", "onCreate");
		super.onCreate(savedInstanceState);

		// Register BroadcastReceiver to track connection changes.
		IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
		receiver = new NetworkReceiver();
		this.registerReceiver(receiver, filter);
	}
	@Override
	public void onDestroy() {
		Log.d("myLog", "onDestroy");
		super.onDestroy();
		if (receiver != null) {
			this.unregisterReceiver(receiver);
		}
	}

	// Refreshes the display if the network connection and the pref settings allow it.
	@Override
	public void onStart() {
		Log.d("myLog", "onStart");
		super.onStart();

		// Gets the user's network preference settings
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

		// Retrieves a string value for the preferences. The second parameter
		// is the default value to use if a preference value is not found.
		sPref = sharedPrefs.getString("listPref", WIFI);

		updateConnectedFlags();

		// Only loads the page if refreshDisplay is true. Otherwise, keeps
		// previous
		// display. For example, if the user has set "Wi-Fi only" in prefs and
		// the
		// device loses its Wi-Fi connection midway through the user using the
		// app,
		// you don't want to refresh the display--this would force the display
		// of
		// an error page instead of stackoverflow.com content.
		if (refreshDisplay) {
			loadPage();
		}
	}

	@Override
	protected void onPause() {
		Log.d("myLog", "onPause");
		super.onPause();
	}

	@Override
	protected void onResume() {
		Log.d("myLog", "onResume");
		super.onResume();
	}

	@Override
	protected void onStop() {
		Log.d("myLog", "onStop");
		super.onStop();
	}

	/**
	 * Checks the network connection and sets the wifiConnected and
	 * mobileConnected variables accordingly.
	 */
	private void updateConnectedFlags() {
		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo activeInfo = connMgr.getActiveNetworkInfo();
		if (activeInfo != null && activeInfo.isConnected()) {
			wifiConnected = activeInfo.getType() == ConnectivityManager.TYPE_WIFI;
			mobileConnected = activeInfo.getType() == ConnectivityManager.TYPE_MOBILE;
		} else {
			wifiConnected = false;
			mobileConnected = false;
		}
		Log.d("myLog", "Network connected: " + activeInfo.isConnected());
		Log.d("myLog", "Wifi connected: " + wifiConnected);
		Log.d("myLog", "Mobile connected: " + mobileConnected);
	}

	/**
	 * Uses AsyncTask subclass to download the XML feed from stackoverflow.com.
	 * This avoids UI lock up. To prevent network operations from causing a
	 * delay that results in a poor user experience, always perform network
	 * operations on a separate thread from the UI.
	 */
	private void loadPage() {
		Log.d("myLog", "load page");
		if (((sPref.equals(ANY)) && (wifiConnected || mobileConnected))
				|| ((sPref.equals(WIFI)) && (wifiConnected))) {
			// AsyncTask subclass
			new DownloadTask().execute(URL);
		} else {
			showErrorPage();
		}
	}

	// Displays an error if the app is unable to load content when specified network connection is not available.
	private void showErrorPage() {
		setContentView(R.layout.web_page);
		WebView myWebView = (WebView) findViewById(R.id.webview);
		myWebView.loadData(getResources().getString(R.string.connection_error), "text/html", null);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.mainmenu, menu);
		return true;
	}

	// Handles the user's menu selection.
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.settings:
			Intent settingsActivity = new Intent(getBaseContext(), SettingsActivity.class);
			startActivity(settingsActivity);
			return true;
		case R.id.refresh:
			loadPage();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	// Implementation of AsyncTask used to download XML feed from
	// stackoverflow.com.
	private class DownloadTask extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... urls) {
			try {
				return loadXmlFromNetwork(urls[0]);
			} catch (IOException e) {
				return getResources().getString(R.string.connection_error);
			}
		}

		@Override
		protected void onPostExecute(String result) {
			setContentView(R.layout.web_page);
			WebView myWebView = (WebView) findViewById(R.id.webview);
			//myWebView.loadData(result, "text/html", null);
			myWebView.loadUrl(URL);
		}
	}

	private String loadXmlFromNetwork(String urlString) throws IOException {
		InputStream stream = null;
		
		String resultString=null;
		
		try {
			stream = downloadUrl(urlString);
			resultString=convertInputStreamIntoString(stream);
			Log.i("myLog", resultString);
		} finally {
			if (stream != null) {
				stream.close(); // Makes sure that the InputStream is closed after the app is finished using it.
			}
		}

		return resultString;
	}

	/**
	 * Given a string representation of a URL, sets up a connection and gets an
	 * input stream.
	 * 
	 * @return an input stream
	 */
	private InputStream downloadUrl(String urlString) throws IOException {
		URL url = new URL(urlString);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setReadTimeout(10000 /* milliseconds */);
		conn.setConnectTimeout(15000 /* milliseconds */);
		conn.setRequestMethod("GET");
		conn.setDoInput(true);
		conn.connect(); // Starts the query
		InputStream stream = conn.getInputStream();
		Log.d("myLog", "Size of getting InputStream=" + stream.available());
		return stream;
	}
	
	public String convertInputStreamIntoString(InputStream is){
		StringBuilder theStringBuilder = new StringBuilder(); // Will store the data

		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"), 8);
			String line = null;
			while ((line = reader.readLine()) != null) // Read in the data from the Buffer until nothing is left
			{
				theStringBuilder.append(line /*+ "\n"*/);
			}
		} catch (UnsupportedEncodingException e) {
			Log.e("myLog", "Ошибка с кодировкой получаемого файла");
			e.printStackTrace();
		} catch (IOException e) {
			Log.e("myLog", "Error while read line in reader (BufferReader)");
			e.printStackTrace();
		}
				
		return theStringBuilder.toString();
	}

	/**
	 * This BroadcastReceiver intercepts the
	 * android.net.ConnectivityManager.CONNECTIVITY_ACTION, which indicates a
	 * connection change. It checks whether the type is TYPE_WIFI. If it is, it
	 * checks whether Wi-Fi is connected and sets the wifiConnected flag in the
	 * main activity accordingly.
	 */
	public class NetworkReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

			// Checks the user prefs and the network connection. Based on the
			// result, decides
			// whether to refresh the display or keep the current display.
			// If the userpref is Wi-Fi only, checks to see if the device has a
			// Wi-Fi connection.
			if (WIFI.equals(sPref) && networkInfo != null
					&& networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
				// If device has its Wi-Fi connection, sets refreshDisplay
				// to true. This causes the display to be refreshed when the
				// user
				// returns to the app.
				refreshDisplay = true;
				Toast.makeText(context, R.string.wifi_connected,
						Toast.LENGTH_SHORT).show();

				// If the setting is ANY network and there is a network
				// connection
				// (which by process of elimination would be mobile), sets
				// refreshDisplay to true.
			} else if (ANY.equals(sPref) && networkInfo != null) {
				refreshDisplay = true;

				// Otherwise, the app can't download content--either because
				// there is no network
				// connection (mobile or Wi-Fi), or because the pref setting is
				// WIFI, and there
				// is no Wi-Fi connection.
				// Sets refreshDisplay to false.
			} else {
				refreshDisplay = false;
				Toast.makeText(context, R.string.lost_connection,
						Toast.LENGTH_SHORT).show();
			}
		}
	}
}