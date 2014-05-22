package ru.savinyurii.timetable;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ru.savinyurii.timetable.R;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class ChooseFacultyAndGroup extends Activity {
	 
	ListView lvChooser;
	TextView tvTitle;
	SharedPreferences sharedPref;
	boolean facultyChoosed = false;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chosing_faculty_and_group);
		
		sharedPref=getSharedPreferences(getPackageName(), MODE_PRIVATE);

		tvTitle = (TextView) findViewById(R.id.tvTitle);
		lvChooser = (ListView) findViewById(R.id.lvChooser);		
		
		lvChooser.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		
		// check the Internet connection
		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {
			//start choosing
			new ChooseFaculty().execute();
		} else {
			Log.e("myLog", "No Internet on device");
			// TODO make alert dialog
			tvTitle.setText(R.string.connection_error);
		}
	}
	
	/**
	 * If pressed Back when choosing group, return to choosing faculty
	 */
	@Override
	public void onBackPressed() {
		if(facultyChoosed){
			new ChooseFaculty().execute();
		} else super.onBackPressed(); 
	}
	
	public class ChooseFaculty extends AsyncTask<String, Void, JSONArray>{

		@Override
		protected JSONArray doInBackground(String... params) {
			return downloadJsonArray(URLs.FACULTIES, "faculties");
		}
		
		@Override
		protected void onPostExecute(final JSONArray facultiesJSONArray) {
			facultyChoosed=false;
			tvTitle.setText(R.string.choose_faculty);
			final String[] facultiesNames = new String[facultiesJSONArray.length()];
			
			try{
				for(int i=0;i<facultiesJSONArray.length(); i++){
					facultiesNames[i]=facultiesJSONArray.getJSONObject(i).getString("faculty_name");
				}
			} catch(JSONException e){
				Log.e("myLog", "Error while parsing JSON Array faculties");
				e.printStackTrace();
			}
			
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, facultiesNames);
			
			lvChooser.setAdapter(adapter);
			
			lvChooser.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					facultyChoosed=true;
					String faculty_id=null;
					try{
						faculty_id=facultiesJSONArray.getJSONObject(position).getString("faculty_id");
					} catch(JSONException e){
						e.printStackTrace();
					}
					//safe in preferences
					SharedPreferences.Editor editor = sharedPref.edit();
					editor.putString("faculty_id", faculty_id);
					editor.commit();
					if(faculty_id!=null) new ChooseGroup().execute(faculty_id);
				}
			});
		}
	}

	public class ChooseGroup extends AsyncTask<String, Void, JSONArray>{

		@Override
		protected JSONArray doInBackground(String... params) {
			return downloadJsonArray(URLs.GROUPS+"?faculty_id="+params[0], "groups");
		}
		
		@Override
		protected void onPostExecute(final JSONArray groupsJSONArray) {
			tvTitle.setText(R.string.choose_group);
			final String[] groupsNames = new String[groupsJSONArray.length()];
			
			try{
				for(int i=0;i<groupsJSONArray.length(); i++){
					groupsNames[i]=groupsJSONArray.getJSONObject(i).getString("group_name");
				}
			} catch(JSONException e){
				Log.e("myLog", "Error while parsing JSON Array groups");
				e.printStackTrace();
			}
			
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, groupsNames);
			
			lvChooser.setAdapter(adapter);
			
			lvChooser.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					String group_id=null;
					try{
						group_id=groupsJSONArray.getJSONObject(position).getString("group_id");
					} catch(JSONException e){
						e.printStackTrace();
					}
					if(group_id!=null){
						SharedPreferences.Editor editor = sharedPref.edit();
						editor.putString("group_id", group_id);
						editor.commit();
						
						new LoadSchedule().execute(group_id);
						//Intent browserActivity =  new Intent(Intent.ACTION_VIEW, Uri.parse(URLs.HTML+"?group="+group_id));
						//startActivity(browserActivity); 
					}
				}
			});
		}
	}

	/**
	 * Load schedule, save it in file, and go to main activity
	 */
	public class LoadSchedule extends AsyncTask<String, Void, String>{

		@Override
		protected String doInBackground(String... params) {
			try {
				// Download and save as HTML
				String urlHtml=URLs.HTML+"?group_id="+params[0];
				String resultHtml=downloadURL(urlHtml);				
				FileOutputStream fosHtml = openFileOutput(URLs.FILE_HTML_TIMETABLE, Context.MODE_PRIVATE); // writing in file								
				fosHtml.write(resultHtml.getBytes());
				fosHtml.close();
				
				// Download and save as JSON
				String urlJson=URLs.SCHEDULE+"?group_id="+params[0];
				String resultJson=downloadURL(urlJson);
				FileOutputStream fosJson = openFileOutput(URLs.FILE_TIMETABLE, Context.MODE_PRIVATE);
				fosJson.write(resultJson.getBytes());
				fosJson.close();
				
			} catch (FileNotFoundException e) {
				Log.e("myLog", "FILE NOT FOUND whet try to open");
				e.printStackTrace();
			} catch (IOException e) {
				Log.e("myLog", "IO problem while writing file");
				e.printStackTrace();
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(String s) {
			Log.i("myLog", "Schedule downloaded");
			finish();
		}
	}
	
	/**
	 * @param type can be "faculties" or "groups"
	 */
	JSONArray downloadJsonArray(String urlString, String type){
		JSONArray jsonArray = null;
		try {
			String jsonString=downloadURL(urlString);
			JSONObject jsonObject = new JSONObject(jsonString);
			jsonArray=jsonObject.getJSONArray(type);
		} catch (JSONException e) {
			Log.e("myLog", "Error while PARSING JSON");
			e.printStackTrace();
		} catch (IOException e) {
			Log.e("myLog", "Error while downloading or reading InputStream");
			e.printStackTrace();
		}
		return jsonArray;
	}
	
	/**
	 * Downloads file from URL as a String
	 */
	public String downloadURL(String urlString) throws IOException{
		InputStream is = null;
		StringBuilder theStringBuilder = new StringBuilder();
		
		try {
			// Download from URL in InputStream
			URL url = new URL(urlString);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setReadTimeout(10000 /* milliseconds */);
			conn.setConnectTimeout(15000 /* milliseconds */);
			conn.setRequestMethod("GET");
			conn.setDoInput(true);
			conn.connect();
			int response = conn.getResponseCode();
	        if(response!=200){
	        	Log.e("myLog", "Error while downloading file");
	        }
	        is = conn.getInputStream();
			// end downloading
			
			// Convert InputStream into string
	        BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"), 8);
			String line = null;
			while ((line = reader.readLine()) != null) // Read in the data from the Buffer until nothing is left
			{
				theStringBuilder.append(line /*+ "\n"*/);
			}
			// end converting
			
		} finally {
			if (is != null) is.close(); 
		}

		return theStringBuilder.toString();	
	}
	
}