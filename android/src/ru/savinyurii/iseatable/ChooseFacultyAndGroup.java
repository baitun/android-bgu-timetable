// Окно выбора факультета и группы

package ru.savinyurii.iseatable;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class ChooseFacultyAndGroup extends Activity {
	
	void log(String text){
//		Log.i("myLog",text);
	}
	 
	ListView lvChooser;
	TextView tvTitle;
	SharedPreferences sharedPref;
	boolean facultyChoosed = false;
	Downloaders downloaders = new Downloaders();
	
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
			// TODO make alert dialog
			tvTitle.setText(R.string.connection_error);
		}
	}
	
	/**
	 * Возвращение к выбору факультета по нажатию back, если сейчас выбирается группа
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
			return downloaders.downloadJsonArray(URLs.FACULTIES, "faculties");
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
				log("Error while parsing JSON Array faculties");
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
			return downloaders.downloadJsonArray(URLs.GROUPS+"?faculty_id="+params[0], "groups");
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
				log("Error while parsing JSON Array groups");
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
					}
				}
			});
		}
	}

	/**
	 * Загружает само расписание данной группы, сохраняет его в файл и переходит к главному окну
	 */
	public class LoadSchedule extends AsyncTask<String, Void, String>{

		@Override
		protected String doInBackground(String... params) {
			try {
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
			// FIXME
//			TODO stop this activity instead of start new
			//startActivity(new Intent(getApplicationContext(), TimeTableActivity.class));
			startActivity(new Intent(getApplicationContext(), HtmlWeekTimeTable.class));
//			it doesn't work. I need some update function.
//			finish();
		}
	}
	
	
}