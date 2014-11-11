/*
 * Copyright 2012 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.examples.youtubeapidemo;

import com.examples.youtubeapidemo.adapter.PageAdapter;
import com.examples.youtubeapidemo.adapter.PageAdapter.VideoEntry;
import com.examples.youtubeapidemo.api.ApiHelper;
import com.examples.youtubeapidemo.search.AsyncSearchYoutube;
import com.examples.youtubeapidemo.search.onYoutubeSearchResults;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayer.OnFullscreenListener;
import com.google.android.youtube.player.YouTubePlayer.OnInitializedListener;
import com.google.android.youtube.player.YouTubePlayer.Provider;
import com.google.android.youtube.player.YouTubePlayerFragment;
import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Thumbnail;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.app.SearchManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A sample Activity showing how to manage multiple YouTubeThumbnailViews in an adapter for display
 * in a List. When the list items are clicked, the video is played by using a YouTubePlayerFragment.
 * <p>
 * The demo supports custom fullscreen and transitioning between portrait and landscape without
 * rebuffering.
 */
@TargetApi(13)
public final class VideoListDemoActivity extends Activity
{

  private static String DEFAULT_KEYWORD = "";
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
	Window win = getWindow();
	win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	
    setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);
    setContentView(R.layout.video_list_demo);
    
	Intent intent = getIntent();		
	handleIntent( intent );
  }

  /**
   * This is a secondary activity, to show what the user has selected
   * when the screen is not large enough to show it all in one activity.
   */

  public static class DetailsActivity extends Activity {

      @Override
      protected void onCreate(Bundle savedInstanceState) {
          super.onCreate(savedInstanceState);

          if (getResources().getConfiguration().orientation
                  == Configuration.ORIENTATION_LANDSCAPE) {
              // If the screen is now in landscape mode, we can show the
              // dialog in-line with the list so we don't need this activity.
              finish();
              return;
          }

          if (savedInstanceState == null) {
              // During initial setup, plug in the details fragment.
        	  VideoFragment details = new VideoFragment();
              details.setArguments(getIntent().getExtras());
              getFragmentManager().beginTransaction().add(android.R.id.content, details).commit();
          }
      }
      
      @Override
      public void onDestroy() {
        super.onDestroy();
      }
  }

  	//single top
  	@Override
	protected void onNewIntent(Intent intent) {
		handleIntent(intent);
	}

	private void handleIntent( Intent intent ) {
		
		if( intent != null ) {
			String action = intent.getAction();		
			if( action.equals(Intent.ACTION_SEARCH) ) {
				VideoListFragment.mSearchQuery = intent.getStringExtra(SearchManager.QUERY);
			}
		}
	}
	
  @Override
  public boolean onCreateOptionsMenu(Menu menu){
  	MenuInflater inflater = getMenuInflater();
  	inflater.inflate(R.menu.list_search_menu, menu);
  	return super.onCreateOptionsMenu(menu);
  }
  
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
	if (super.onOptionsItemSelected(item)) {
		return true;
	}
    switch (item.getItemId()) {
    case R.id.menu_search:
    	onSearchRequested();
    	break;
    }
    return true;
  }
	   
  /**
   * A fragment that shows a static list of videos.
   */
  public static final class VideoListFragment extends ListFragment implements onYoutubeSearchResults {

	public static String mSearchQuery = DEFAULT_KEYWORD;
	public static String mLastQuery = null;

    private boolean mDualPane;
    private int mCurCheckPosition = 0;
    private PageAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      adapter = new PageAdapter(getActivity());
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
      super.onActivityCreated(savedInstanceState);

      setListAdapter(adapter);
      
      View detailsFrame = getActivity().findViewById(R.id.details);
      mDualPane = detailsFrame != null && detailsFrame.getVisibility() == View.VISIBLE;
      if (savedInstanceState != null) {
          // Restore last state for checked position.
          mCurCheckPosition = savedInstanceState.getInt("curChoice", 0);
      }
      if (mDualPane) {
          // In dual-pane mode, the list view highlights the selected item.
          getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
          // Make sure our UI is in the correct state.
          showDetails(mCurCheckPosition);
      }
      if (checkGooglePlayServicesAvailable()) {
    	  searchByKeyword();
      }
   }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("curChoice", mCurCheckPosition);
    }

    @Override
    public void onPause() {
    	super.onPause();
        VideoFragment videoFragment = (VideoFragment) getFragmentManager().findFragmentById(R.id.details);
        if (videoFragment != null) {
      	  videoFragment.pause();
        }    	
    }
    
    @Override
    public void onResume() {
    	super.onResume();
        VideoFragment videoFragment = (VideoFragment) getFragmentManager().findFragmentById(R.id.details);
        if (videoFragment != null) {
      	  videoFragment.resume();
        }    	
    }
    
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        showDetails(position);
    }
    
    public void showDetails(int index) {
	  if (adapter == null || index>=adapter.getCount()) {
		  mCurCheckPosition = 0;
		  return;
	  }
		
	  mCurCheckPosition = index;
	  String videoId = adapter.getVideoId(mCurCheckPosition);
	  if (videoId == null || videoId.equals("")) {
		  return;
	  }
	  
      if (mDualPane) {
	      VideoFragment videoFragment = (VideoFragment) getFragmentManager().findFragmentById(R.id.details);
          if (videoFragment == null || !videoId.equals(videoFragment.getShownVideoId())) {
              // Make new fragment to show this selection.
        	  videoFragment = VideoFragment.newInstance(videoId);

              // Execute a transaction, replacing any existing fragment
              // with this one inside the frame.
              FragmentTransaction ft = getFragmentManager().beginTransaction();
              ft.replace(R.id.details, videoFragment);
              ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
              ft.commit();
          }
      }
      else {
          // Otherwise we need to launch a new activity to display
          // the dialog fragment with selected text.
          Intent intent = new Intent();
          intent.setClass(getActivity(), DetailsActivity.class);
          intent.putExtra("videoId", videoId);
          startActivity(intent);
      }
    }

    @Override
    public void onDestroyView() {
      super.onDestroyView();

      adapter.releaseLoaders();
    }

    public void setLabelVisibility(boolean visible) {
      adapter.setLabelVisibility(visible);
    }

    /** Check that Google Play services APK is installed and up to date. */
    private boolean checkGooglePlayServicesAvailable() {
/*
      final int connectionStatusCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity());
      if (GooglePlayServicesUtil.isUserRecoverableError(connectionStatusCode)) {
        showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        return false;
      }
*/
      return true;
    }

    private void searchByKeyword() {
      if (mLastQuery==null || !mLastQuery.equals(mSearchQuery)) {
    	  mLastQuery = mSearchQuery;
    	  AsyncSearchYoutube.run(VideoListFragment.this, mSearchQuery);
      }
    }
    
    /*
     * Prints out all SearchResults in the Iterator. Each printed line includes title, id, and
     * thumbnail.
     *
     * @param iteratorSearchResults Iterator of SearchResults to print
     *
     * @param query Search query (String)
     */
    public void onResult(List<SearchResult> searchResults, String query) {

    	Iterator<SearchResult> iteratorSearchResults = searchResults.iterator();
    	List<VideoEntry> list = new ArrayList<VideoEntry>();

    	while (iteratorSearchResults.hasNext()) {

	        SearchResult singleVideo = iteratorSearchResults.next();
	        ResourceId rId = singleVideo.getId();
	
	        // Double checks the kind is video.
	        if (rId.getKind().equals("youtube#video")) {
	          Thumbnail thumbnail = (Thumbnail) singleVideo.getSnippet().getThumbnails().get("default");
	          list.add(new VideoEntry(singleVideo.getSnippet().getTitle(), rId.getVideoId(), thumbnail.getUrl()));
	        }
    	}
    	
		FragmentManager mgr = getFragmentManager();
		if (mgr != null) {
	    	VideoListFragment listFragment = (VideoListFragment) mgr.findFragmentById(R.id.list_fragment);
	    	if (listFragment!=null) {
	    		listFragment.updateAdapter(list);
	    	}
		}
    }
 /*   
    void showGooglePlayServicesAvailabilityErrorDialog(final int connectionStatusCode) {
    	getActivity().runOnUiThread(new Runnable() {
  	      public void run() {
  	        Dialog dialog =
  	            GooglePlayServicesUtil.getErrorDialog(connectionStatusCode, getActivity(),
  	                ApiHelper.REQUEST_GOOGLE_PLAY_SERVICES);
  	        dialog.show();
  	      }
  	    });
	}
*/ 
    public void updateAdapter(List<VideoEntry> list) {
        if (adapter!=null) {
    		adapter.updateAdapter(list);
    	}
    }
  }

  public static final class VideoFragment extends YouTubePlayerFragment
      implements OnInitializedListener, OnFullscreenListener
{
	private static boolean playerPending;  
    private YouTubePlayer player;
    private String videoId;
    private boolean paused;
    
    public static VideoFragment newInstance(String videoId) {
    	VideoFragment f = new VideoFragment();

        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putString("videoId", videoId);
        f.setArguments(args);

        return f;
    }

    public String getShownVideoId() {
    	Bundle bundle = getArguments();
    	if (bundle==null) {
    		return "";
    	}
        return getArguments().getString("videoId", "");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      
      if (player == null && !playerPending) {
    	  playerPending = true;
    	  initialize(ApiHelper.DEVELOPER_KEY, this);
      }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
      super.onActivityCreated(savedInstanceState);
      String videoId = getShownVideoId();
      if (!videoId.equals("")) {
    	  setVideoId(videoId);
      }
	}
    
    @Override
    public void onDestroy() {
      if (player != null) {
        player.release();
        player = null;
      }
      playerPending = false;
      super.onDestroy();
    }

    public void setVideoId(String videoId) {
    	
      if (videoId != null && !videoId.equals(this.videoId)) {
    	this.paused = false;
        this.videoId = videoId;
        if (player != null) {
          player.cueVideo(videoId);
        }
      }
    }

    public void pause() {
      if (player != null && player.isPlaying()) {
    	paused = true;
        player.pause();
      }
    }

    public void resume() {
      if (player != null && paused) {
    	paused = false;
        player.play();
      }
    }
    
    @Override
    public void onInitializationSuccess(Provider provider, YouTubePlayer player, boolean restored) {
      this.player = player;
      if (player != null) {
	      player.addFullscreenControlFlag(YouTubePlayer.FULLSCREEN_FLAG_CUSTOM_LAYOUT);
	      player.setOnFullscreenListener(VideoFragment.this);
	      if (!restored && videoId != null) {
	        player.cueVideo(videoId);
	      }
      }
    }

    @Override
    public void onInitializationFailure(Provider provider, YouTubeInitializationResult result) {
      player = null;
      playerPending = false;
    }

    @Override
    public void onFullscreen(boolean arg0) {
    }
    
  }

}
