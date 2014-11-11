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

import java.util.List;

import android.os.AsyncTask;
import android.util.Log;
import com.google.api.services.youtube.model.SearchResult;

/**
 * Asynchronous task that also takes care of common needs, such as displaying progress,
 * authorization, exception handling, and notifying UI when operation succeeded.
 * 
 * @author Yaniv Inbar
 */
abstract class YoutubeAsyncTask extends AsyncTask<Void, Integer, List<SearchResult>> {
	
  static final String TAG = "YoutubeSampleActivity";
  final onYoutubeSearchResults listener;
  final String queryTerm;
  
  protected com.google.api.services.youtube.YouTube youtube;
  
  YoutubeAsyncTask(onYoutubeSearchResults listener, String queryTerm ) {
    this.listener = listener;
    this.queryTerm = queryTerm;
  }
  
  @Override
  protected void onPreExecute() {
    super.onPreExecute();
  }


  @Override
  protected void onProgressUpdate(Integer... values) {
	Log.d("","onProgressUpdate: " + values[0]);      
  }
  
  @Override
  protected final List<SearchResult> doInBackground(Void... Ignored) {
      return doInBackground();
  }


  @Override
  protected final void onPostExecute(List<SearchResult> returnedVideoList) {  
    super.onPostExecute(returnedVideoList);
    if (listener!=null) {
    	listener.onResult(returnedVideoList, queryTerm);
    }
  }

  abstract protected List<SearchResult> doInBackground();
  
}
