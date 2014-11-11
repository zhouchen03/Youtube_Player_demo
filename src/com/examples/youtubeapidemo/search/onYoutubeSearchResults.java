package com.examples.youtubeapidemo.search;

import java.util.List;

import com.google.api.services.youtube.model.SearchResult;

public interface onYoutubeSearchResults {
	
	public void onResult(List<SearchResult> iteratorSearchResults, String query);
	
}
