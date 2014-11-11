/*
 * Copyright (c) 2012 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.examples.youtubeapidemo.search;

import java.io.IOException;
import java.util.List;

import com.examples.youtubeapidemo.api.ApiHelper;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;

/**
 * Asynchronously load the tasks.
 * 
 * @author Yaniv Inbar
 */
public class AsyncSearchYoutube extends YoutubeAsyncTask {
	
	static final String TAG = "YoutubeSampleActivity";

	/** Global instance of the max number of videos we want returned (50 = upper limit per page). */
	private static final long NUMBER_OF_VIDEOS_RETURNED = 25;
	
	protected AsyncSearchYoutube(onYoutubeSearchResults listener, String queryTerm) {
		super(listener, queryTerm);
	}

	@Override
	protected List<SearchResult> doInBackground() {	  
		List<SearchResult> searchResultList = null;
		try {
			
		  com.google.api.services.youtube.YouTube youtube = ApiHelper.getYouTubeService();
		  YouTube.Search.List search = youtube.search().list("id,snippet");
	
	      /*
	       * It is important to set your developer key from the Google Developer Console for
	       * non-authenticated requests (found under the API Access tab at this link:
	       * code.google.com/apis/). This is good practice and increased your quota.
	       */
	      search.setKey(ApiHelper.DEVELOPER_KEY);
	      search.setQ(queryTerm);
	      /*
	       * We are only searching for videos (not playlists or channels). If we were searching for
	       * more, we would add them as a string like this: "video,playlist,channel".
	       */
	      search.setType("video");
	      /*
	       * This method reduces the info returned to only the fields we need and makes calls more
	       * efficient.
	       */
	      search.setFields("items(id/kind,id/videoId,snippet/title,snippet/thumbnails/default/url)");
	      search.setMaxResults(NUMBER_OF_VIDEOS_RETURNED);
	      SearchListResponse searchResponse = search.execute();
	
	      searchResultList = searchResponse.getItems();
	
	    } catch (GoogleJsonResponseException e) {
	      System.err.println("There was a service error: " + e.getDetails().getCode() + " : "
	          + e.getDetails().getMessage());
	    } catch (IOException e) {
	      System.err.println("There was an IO error: " + e.getCause() + " : " + e.getMessage());
	    } catch (Throwable t) {
	      t.printStackTrace();
	    }
		return searchResultList;
  }
  
  public static void run(onYoutubeSearchResults listener, String queryTerm) {
    new AsyncSearchYoutube(listener, queryTerm).execute();
  }
  

}
