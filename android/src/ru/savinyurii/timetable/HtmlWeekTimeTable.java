package ru.savinyurii.timetable;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;

public class HtmlWeekTimeTable extends Activity {
	WebView wv;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.web_page);
		
		// Getting info from preferences
        SharedPreferences sharedPref=getSharedPreferences(getPackageName(), MODE_PRIVATE);
        String pref_group_id=sharedPref.getString("group_id", null);
        Log.i("myLog","curren group="+pref_group_id);
        if(pref_group_id==null){ // if app running first time
        	startActivity(new Intent(this, ChooseFacultyAndGroup.class));
        }
        
        
	    wv = (WebView) findViewById(R.id.webview);
	    
	    // open next version when Long click
	    wv.setOnLongClickListener(new View.OnLongClickListener() {			
			@Override
			public boolean onLongClick(View v) {
				startActivity(new Intent(getBaseContext(), TimeTableActivity.class));
				return false;
			}
		});
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		File file = new File(getFilesDir(), URLs.FILE_HTML_TIMETABLE);
		wv.loadUrl(Uri.fromFile(file).toString());
		Log.i("myLog", Uri.fromFile(file).toString());
		Log.i("myLog", file.toString());
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
		case R.id.change_group:
			startActivity(new Intent(getBaseContext(), ChooseFacultyAndGroup.class));
			return true;
		case R.id.refresh:
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
}
