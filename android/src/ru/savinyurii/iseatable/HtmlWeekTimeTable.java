package ru.savinyurii.iseatable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;

public class HtmlWeekTimeTable extends Activity {
	
	void log(String text){
//		Log.i("myLog",text);
	}
	
	 String pref_group_id;
	 boolean updated=false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.web_page);
		
		// Getting info from preferences
        SharedPreferences sharedPref=getSharedPreferences(getPackageName(), MODE_PRIVATE);
        pref_group_id=sharedPref.getString("group_id", null);
        log("curren group="+pref_group_id);
        if(pref_group_id==null){ // if app running first time
        	startActivity(new Intent(this, ChooseFacultyAndGroup.class));
        	pref_group_id=sharedPref.getString("group_id", null);
        	log("curren group="+pref_group_id);
        }
        
        File file = new File(getFilesDir(), URLs.FILE_HTML_TIMETABLE);
        WebView wv = (WebView) findViewById(R.id.webview);
        wv.getSettings().setJavaScriptEnabled(true);
	    wv.loadUrl(Uri.fromFile(file).toString());
	    
	    // Запуск демонстрации новой версии
	    wv.setOnLongClickListener(new View.OnLongClickListener() {			
			@Override
			public boolean onLongClick(View v) {
				startActivity(new Intent(getBaseContext(), TimeTableActivity.class));
				return false;
			}
		});
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
		
		/*case R.id.settings:
			Intent settingsActivity = new Intent(getBaseContext(), SettingsActivity.class);
			startActivity(settingsActivity);
			return true;*/
//		TODO separate those buttons. Use button refresh
		case R.id.refresh:
			if(!updated){
				new UpdateSchedule().execute(pref_group_id);
				updated=true;
				log("updated");
			}
			else{
//				TODO make alert
				log("not updated");
			}
			return true;
		case R.id.change_group:		
			startActivity(new Intent(getBaseContext(), ChooseFacultyAndGroup.class));
			return true;
		case R.id.share:Intent sendIntent = new Intent();
			sendIntent.setAction(Intent.ACTION_SEND);
			sendIntent.putExtra(Intent.EXTRA_TEXT, getResources().getText(R.string.share_text));
			sendIntent.setType("text/plain");
			startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.share)));
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	/**
	 * Загружает само расписание данной группы, сохраняет его в файл и переходит к главному окну
	 */
	public class UpdateSchedule extends AsyncTask<String, Void, String>{

		@Override
		protected String doInBackground(String... params) {
			try {
				Downloaders downloaders = new Downloaders();
				// FIXME
				//String url=URLs.SCHEDULE+"?group_id="+params[0];
				String url=URLs.HTML+"?group_id="+params[0];
				String result=downloaders.downloadURL(url);
				// writing in file
				// FIXME
				//FileOutputStream fos = openFileOutput(URLs.FILE_TIMETABLE, Context.MODE_PRIVATE);
				FileOutputStream fos = openFileOutput(URLs.FILE_HTML_TIMETABLE, Context.MODE_PRIVATE);
				fos.write(result.getBytes());
				fos.close();
			} catch (FileNotFoundException e) {
				log("FILE NOT FOUND when try to open");
				e.printStackTrace();
			} catch (IOException e) {
				log("IO problem while writing file");
				e.printStackTrace();
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(String s) {			
			onStart();
		}
	}
	
}