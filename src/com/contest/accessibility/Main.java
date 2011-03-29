package com.contest.accessibility;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.contest.activities.LazyActivity;
import com.tedx.objects.SearchResult;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;

public class Main extends LazyActivity {	
	public EditText txtCriteria;
	public String criteria;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		
		if(extras != null && extras.containsKey("Criteria") && !extras.getString("Criteria").equalsIgnoreCase(""))
		{
			criteria = extras.getString("Criteria");
		}
		else
		{
			criteria = "Google Developer";
		}
		
        mFrom = new String[] {
				SearchResult.TITLE,
				SearchResult.CONTENT,
				SearchResult.PHOTOURL
		};

		mTo = new int[] {
				android.R.id.text1,
				android.R.id.text2,
				android.R.id.icon
		};
		
		super.onCreate(savedInstanceState, R.layout.main, R.layout.searchresultrow);		
    	txtCriteria = (EditText)findViewById(R.id.txtCriteria);
    }
    
    public void btnSearch_Click(View target)
    {
    	if(!txtCriteria.getText().toString().equalsIgnoreCase(""))
    	{
			Intent intent = new Intent(this, Main.class);
			intent.putExtra("Criteria", txtCriteria.getText().toString());
			startActivity(intent);	
    	}
	}

	public void onItemClick(AdapterView<?> list, View row, int position, long id) {
		startActivityForPosition(Main.class, position);
	}

	@Override
	protected LoadTask newLoadTask() {
		return new LoadSearchResultTask();
	}

	@Override
	protected void setTaskActivity() {
		mLoadTask.activity = this;
	}

	protected static class LoadSearchResultTask extends LoadTask {
		@Override
		protected Boolean doInBackground(String... params) {
			JSONArray collection = new JSONArray();
			Main activity = (Main) super.activity;

			URL url;
		    try {
		    		String featuredFeed = "http://gdata.youtube.com/feeds/api/videos?orderby=published&start-index=11&max-results=20&v=2&caption&format=6&q="+ URLEncoder.encode(activity.criteria);

		            url = new URL(featuredFeed);

		            URLConnection connection;
		            connection = url.openConnection();

		            HttpURLConnection httpConnection = (HttpURLConnection)connection; 


		            int responseCode = httpConnection.getResponseCode(); 


		            if (responseCode == HttpURLConnection.HTTP_OK) { 
		                  InputStream in = httpConnection.getInputStream(); 

		                  DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		                  DocumentBuilder db = dbf.newDocumentBuilder();

		                  Document dom = db.parse(in);      
		                  Element docEle = dom.getDocumentElement();

		                  NodeList nl = docEle.getElementsByTagName("entry");
		                  if (nl != null && nl.getLength() > 0) {
		                    for (int i = 0 ; i < nl.getLength(); i++) {
		                      Element entry = (Element)nl.item(i);
		                      Element title = (Element)entry.getElementsByTagName("title").item(0);

		                      String titleStr = title.getFirstChild().getNodeValue();
		                      
		                      Element content = (Element)entry.getElementsByTagName("content").item(0);
		                      String contentStr = content.getAttribute("src");
		                      
		                      Element photoUrl = (Element)entry.getElementsByTagName("media:thumbnail").item(0);
		                      String photoStr = photoUrl.getAttribute("url");
		                      
		                      
		                      JSONObject temp = new JSONObject();
		                      try {
								temp.put("Title", titleStr);
								temp.put("Content", contentStr);
								temp.put("PhotoUrl", photoStr);
		                      } catch (JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
		                      }
		                      
		                      collection.put(temp);

		                      //VideoCell cell = new VideoCell(titleStr);
		                    }
		                  }
		                }
		              } catch (MalformedURLException e) {
		                e.printStackTrace();
		              } catch (IOException e) {
		                e.printStackTrace();
		              } catch (ParserConfigurationException e) {
		                e.printStackTrace();
		              } catch (SAXException e) {
		                e.printStackTrace();
		              }
		              finally {
		              }
			
			return loadResultsByCollection(collection);	
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			if (!result) {
				activity.showDialog(DIALOG_ERROR_LOADING);
			}
		}
	}

	@Override
	protected HashMap<String, String> loadJSON(JSONObject data) throws JSONException {
		HashMap<String, String> SearchResults = new HashMap<String, String>();
		SearchResults.put(SearchResult.TITLE, data.getString("Title"));
		SearchResults.put(SearchResult.CONTENT, data.getString("Content"));		
		SearchResults.put(SearchResult.PHOTOURL, data.getString("PhotoUrl"));
		return SearchResults;
	}
}