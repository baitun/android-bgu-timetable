/**
 * 
 */
package ru.savinyurii.iseatable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author SavinYurii
 *
 */
public class Downloaders {
	/**
	 * Downloads file from URL, using downloadURL, and converts string to JSONArray
	 * @param type can be "faculties" or "groups"
	 */
	public JSONArray downloadJsonArray(String urlString, String type){
		JSONArray jsonArray = null;
		try {
			String jsonString=downloadURL(urlString);
			JSONObject jsonObject = new JSONObject(jsonString);
			jsonArray=jsonObject.getJSONArray(type);
		} catch (JSONException e) {
//			log("Error while PARSING JSON");
			e.printStackTrace();
		} catch (IOException e) {
//			log("Error while downloading or reading InputStream");
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
//	        	log("Error while downloading file");
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
