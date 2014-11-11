package com.examples.youtubeapidemo.api;

import java.io.IOException;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;

public class ApiHelper {

	  public static final int REQUEST_GOOGLE_PLAY_SERVICES = 0;	
	  public static final int REQUEST_AUTHORIZATION = 1;	
	  public static final int REQUEST_ACCOUNT_PICKER = 2;
		  
	  /**
	   * Please replace this with a valid API key which is enabled for the 
	   * YouTube Data API v3 service. Go to the 
	   * <a href="https://code.google.com/apis/console/">Google APIs Console</a> to
	   * register a new developer key.
	   */
	  public static final String DEVELOPER_KEY = "AIzaSyBvqXzt9wMYh6WGL27hpAZGmCwacUA5q5U";

	  /** Global instance of the HTTP transport. */
	  private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

	  /** Global instance of the JSON factory. */
	  private static final JsonFactory JSON_FACTORY = new JacksonFactory();
	  
	  private static com.google.api.services.youtube.YouTube youtube;
	  
	  public static com.google.api.services.youtube.YouTube getYouTubeService() throws IOException
	  {
		  if (youtube == null) {
			  try {
		    	youtube = new YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY, new HttpRequestInitializer() {
		            public void initialize(HttpRequest request) throws IOException {}
		          }).setApplicationName("API Project").build();
			  }
			  catch (Exception e)
			  {
				  throw new IOException("YouTube.Builder " + e.getMessage());
			  }
		  }
		  return youtube;
	  }
}
