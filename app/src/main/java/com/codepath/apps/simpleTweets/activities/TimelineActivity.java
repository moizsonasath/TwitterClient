package com.codepath.apps.simpleTweets.activities;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import com.codepath.apps.simpleTweets.helpers.EndlessScrollListener;
import com.codepath.apps.simpleTweets.R;
import com.codepath.apps.simpleTweets.adapters.TweetsArrayAdapter;
import com.codepath.apps.simpleTweets.helpers.TwitterApplication;
import com.codepath.apps.simpleTweets.helpers.TwitterClient;
import com.codepath.apps.simpleTweets.models.Tweet;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class TimelineActivity extends AppCompatActivity {

    private TwitterClient client;
    private ArrayList<Tweet> tweets;
    private TweetsArrayAdapter aTweets;
    private ListView lvTweets;
    private SwipeRefreshLayout swipeContainer;
    public static final int REQ_CODE_COMPOSE_TWEET = 1;
    public static final int DEFAULT_SINCE_ID = 0;
    public static final int DEFAULT_MAX_ID = 0;
    private long oldMaxId = 0;
    private long maxId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);
        // To show logo on action bar
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.drawable.twitter_icon);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        //set title on action bar
        getSupportActionBar().setTitle(R.string.title_activity_timeline);
        //find the listview
        lvTweets = (ListView) findViewById(R.id.lvTweets);
        //create the arraylist (data source)
        tweets = new ArrayList<>();
        //construct the adapter from data source
        aTweets = new TweetsArrayAdapter(this, tweets);
        //connect adapter to listview
        lvTweets.setAdapter(aTweets);
        // get the client
        client = TwitterApplication.getRestClient(); //singleton client

        lvTweets.setOnScrollListener(new EndlessScrollListener() {
            @Override
            public boolean onLoadMore(int page, int totalItemsCount) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to your AdapterView
                customLoadMoreDataFromApi(page);
                // or customLoadMoreDataFromApi(totalItemsCount);
                return true; // ONLY if more data is actually being loaded; false otherwise.
            }
        });

        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.
                refereshTimeline();
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        populateTimeline(DEFAULT_SINCE_ID, DEFAULT_MAX_ID);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.timeline, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.miCompose:
                //Toast.makeText(this, "Compose selected", Toast.LENGTH_SHORT)
                 //       .show();
                composeTweet();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Append more data into the adapter
    public void customLoadMoreDataFromApi(int offset) {
        // This method probably sends out a network request and appends new data items to your adapter.
        // Use the offset value and add it as a parameter to your API request to retrieve paginated data.
        // Deserialize API response and then construct new objects to append to the adapter
        //Log.d("MOIZ MOIZ", "Offset value " +offset);
        Tweet lastTweet = aTweets.getItem(aTweets.getCount() - 1);
        maxId = lastTweet.getUid();
        if (maxId == oldMaxId){
            Log.d("Moiz", "Scroll Listener called twice for same max_id");
            return;
        }
        oldMaxId = maxId;
        //Log.d("MOIZ", "id of last tweet: " + maxId);
        populateTimeline(DEFAULT_SINCE_ID, (maxId - 1));
    }

    //send API request to get JSON response
    //fill the listview by creating tweet objects from JSON
    private void populateTimeline(final long since_id, final long max_id) {
        //first check if network connection is available
        if(isConnectedToNetwork() == false) {
            Toast.makeText(this.getApplicationContext(), "Please check your network connection!", Toast.LENGTH_LONG).show();
            swipeContainer.setRefreshing(false);
            return;
        }
        client.getHomeTimeline(since_id, max_id, new JsonHttpResponseHandler() {
            //sucess
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray json) {
                //Log.d("Moiz", json.toString());
                //Deserialize Json
                //Create models and them to the adapter
                //Load model data into listview
                if (since_id > 0) {
                    ArrayList<Tweet> newTweets = Tweet.fromJSONArray(json);
                    for (int i = newTweets.size() - 1; i >= 0; i--) {
                        aTweets.insert(newTweets.get(i), 0);
                    }
                } else {
                    aTweets.addAll(Tweet.fromJSONArray(json));
                }
                swipeContainer.setRefreshing(false);
            }

            //failure
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Toast.makeText(getApplicationContext(), "HTTP REQUEST FAILED!", Toast.LENGTH_LONG).show();
                swipeContainer.setRefreshing(false);
                //Log.d("DEBUG", errorResponse.toString());
            }
        });
    }

    private void composeTweet() {
        //first check if network connection is available
        if(isConnectedToNetwork() == false) {
            Toast.makeText(this.getApplicationContext(), "Please check your network connection!", Toast.LENGTH_LONG).show();
            return;
        }
            Intent i = new Intent(this, ComposeActivity.class);
            startActivityForResult(i, REQ_CODE_COMPOSE_TWEET);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQ_CODE_COMPOSE_TWEET && resultCode == RESULT_OK) {
            refereshTimeline();
            Toast.makeText(this.getApplicationContext(), "Successfully Tweeted!!", Toast.LENGTH_SHORT).show();
        }
    }

    // Referesh timeline with new tweets at the top
    private void refereshTimeline() {
        Tweet firstTweet = aTweets.getItem(0);
        long sinceId = firstTweet.getUid();
        //Log.d("MOIZ", "id of first tweet: " + sinceId);
        populateTimeline(sinceId, DEFAULT_MAX_ID);
    }
    private boolean isConnectedToNetwork() {
        ConnectivityManager connMgr = (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if(networkInfo != null && networkInfo.isConnected()) {
            Log.e("NETWORK", "isConnectedToNetwork return true");
            return true;
        }
        else {
            Log.e("NETWORK", "isConnectedToNetwork return false");
            return false;
        }
    }
}
