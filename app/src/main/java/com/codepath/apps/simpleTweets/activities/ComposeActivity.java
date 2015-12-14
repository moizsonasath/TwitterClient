package com.codepath.apps.simpleTweets.activities;

import org.apache.http.Header;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.codepath.apps.simpleTweets.R;
import com.codepath.apps.simpleTweets.helpers.TwitterApplication;
import com.codepath.apps.simpleTweets.helpers.TwitterClient;
import com.loopj.android.http.JsonHttpResponseHandler;

public class ComposeActivity extends AppCompatActivity {
    private TwitterClient client;
    private static final int LENGTH_BOUND = 140;
    private EditText etCompose;
    private TextView tvCharCount;
    private Button btnCompose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose);
        // To show back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // To show Logo on Action bar
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.drawable.twitter_icon);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        //Replace the back arrow with icon
        //getSupportActionBar().setDisplayUseLogoEnabled(true);
        //getSupportActionBar().setHomeAsUpIndicator(R.drawable.twitter_icon);
        //Set the Title
        getSupportActionBar().setTitle(R.string.title_activity_Compose);

        // get the client
        client = TwitterApplication.getRestClient(); //singleton client
        etCompose = (EditText) findViewById(R.id.etcompose);
        etCompose.setSelection(0);
        etCompose.requestFocus();
        tvCharCount = (TextView) findViewById(R.id.tvcharCount);
        btnCompose = (Button) findViewById(R.id.btnCompose);

        etCompose.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable text) {
                // Do nothing.
            }

            @Override
            public void beforeTextChanged(CharSequence text, int start, int count, int after) {
                // Do nothing.
            }

            @Override
            public void onTextChanged(CharSequence text, int start, int before, int count) {
                int length = text.length();
                tvCharCount.setText(String.valueOf(140-length));
                if (length > LENGTH_BOUND) {
                    tvCharCount.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                    btnCompose.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
                    btnCompose.setEnabled(false);
                } else {
                    tvCharCount.setTextColor(getResources().getColor(android.R.color.black));
                    btnCompose.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_light));
                    btnCompose.setEnabled(true);
                }
            }
        });
    }

    public void onClickCompose(View view) {
        String body = etCompose.getText().toString();
        client.postTweet(body, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject json) {
                //Tweet tweet = new Tweet();
                Intent data = new Intent();
                //data.putExtra("tweet", tweet);
                setResult(RESULT_OK, data);
                finish();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Toast.makeText(ComposeActivity.this, "Unable to send tweet. Try again", Toast.LENGTH_LONG).show();
                Log.d("MOIZ", errorResponse.toString());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.compose, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            // overridePendingTransition(R.animator.anim_left, R.animator.anim_right);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
