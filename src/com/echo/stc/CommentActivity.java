
package com.echo.stc;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebSettings.RenderPriority;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
public class CommentActivity extends Activity {
	AsyncTask<Void, String, Void> task, task2;
	ApplicationSettings settings;
	private Vibrator myVib;
	Button submit;
	EditText name, mail, website, comment;
	TextView error;
	String url="";
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
    	setTheme(android.R.style.Theme_Light_NoTitleBar);
		super.onCreate(savedInstanceState);
        setContentView(R.layout.comment);
		settings = (ApplicationSettings)this.getApplication();
        myVib = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);
        Intent i = this.getIntent();
        url = i.getStringExtra("url");
        submit = (Button) findViewById(R.id.btn_submit);
        name = (EditText)findViewById(R.id.name);
        mail = (EditText)findViewById(R.id.mail);
        website = (EditText)findViewById(R.id.website);
        comment = (EditText)findViewById(R.id.comment);
        error = (TextView)findViewById(R.id.error);
        
		ImageView logo = (ImageView) findViewById(R.id.imageView2);
        logo.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				if (settings.getVibrateEnabled()){
					myVib.vibrate(50);
				}
				//CategoryListActivity.getMeHome = true;
				finish();
			}
        });
        name.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (settings.getVibrateEnabled()) {
					myVib.vibrate(50);
				}
			}
		});
        
        mail.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (settings.getVibrateEnabled()) {
					myVib.vibrate(50);
				}
			}
		});
        
        website.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (settings.getVibrateEnabled()) {
					myVib.vibrate(50);
				}
			}
		});
        
        
        
		submit.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (settings.getVibrateEnabled()) {
					myVib.vibrate(50);
				}
				/*Intent i = new Intent(DashboardActivity.this,CategoryListActivity.class);
				i.putExtra("category", "Resources");
				i.putExtra("nextCategory", "Testing Planet");
				i.putExtra("categoryNumber", 4);
				startActivity(i);*/
				if (!name.getText().toString().equals("") && !mail.getText().toString().equals("") && !comment.getText().toString().equals("")){
					/*url = "http://blog.softwaretestingclub.com/wp-comments-post.php";
					ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
	            	postParameters.add(new BasicNameValuePair("author", name.getText().toString()));
	            	postParameters.add(new BasicNameValuePair("email", mail.getText().toString()));
	            	postParameters.add(new BasicNameValuePair("url", website.getText().toString()));
	            	postParameters.add(new BasicNameValuePair("comment", comment.getText().toString()));
	            	postParameters.add(new BasicNameValuePair("comment_post_ID", "813"));
	      //      	postParameters.add(new BasicNameValuePair("comment_parent","0"));
	      //     	postParameters.add(new BasicNameValuePair("akismet_comment_nonce","4db49fa15f"));

	            	String response = null;
	            	try {
	            	    response = CustomHttpClient.executeHttpPost(url, postParameters);
	            	    String res=response.toString();
	            	    res= res.replaceAll("\\s+","");
	            	    if(res.equals("1")){
	            	    	finish();
							Toast.makeText(CommentActivity.this, "Comment posted", 5).show();
	            	    }
	            	    else{
	            	    	error.setText("Connection problem");
	            	    }
	            	} catch (Exception e) {
	            		name.setText(e.toString());
	            	}*/DefaultHttpClient httpclient = new DefaultHttpClient();
					url = "http://blog.softwaretestingclub.com/wp-comments-post.php";
					HttpPost httppost = new HttpPost(url);
					httppost.setHeader("Content-type","application/x-www-form-urlencoded;charset=UTF-8");
					List<NameValuePair> postParameters = new ArrayList<NameValuePair>();
	            	postParameters.add(new BasicNameValuePair("author", name.getText().toString()));
	            	postParameters.add(new BasicNameValuePair("email", mail.getText().toString()));
	            	postParameters.add(new BasicNameValuePair("url", website.getText().toString()));
	            	postParameters.add(new BasicNameValuePair("comment", comment.getText().toString()));
	            	postParameters.add(new BasicNameValuePair("comment_post_ID", "813"));
	            	postParameters.add(new BasicNameValuePair("comment_parent","0"));
	            	postParameters.add(new BasicNameValuePair("akismet_comment_nonce","4db49fa15f"));
					try {
					    httppost.setEntity(new UrlEncodedFormEntity(postParameters));
					} catch (UnsupportedEncodingException e1) {
					    e1.printStackTrace();
					}

					BasicResponseHandler handler = new BasicResponseHandler();


					try {
						String response = httpclient.execute(httppost,handler);
	            	    String res=response.toString();
	            	    res= res.replaceAll("\\s+","");
					    Log.e("OUTPUT",response);
					} catch (ClientProtocolException e) {               
					    e.printStackTrace();
					} catch (IOException e) {
					    e.printStackTrace();
					}
					String test = "test";
					
				} else {
					String requiredFields = "Required fields:";
					if (name.getText().toString().equals("")){
						requiredFields = requiredFields + " " + "Name";
					}
					if (mail.getText().toString().equals("")){
						requiredFields = requiredFields + " " + "Mail";
					}
					if (comment.getText().toString().equals("")){
						requiredFields = requiredFields + " " + "Comment";
					}
					error.setText(requiredFields);
				}
			}
		});
	}
	private void postComment(){
		// http://fahmirahman.wordpress.com/2011/04/26/the-simplest-way-to-post-parameters-between-android-and-php/
		
		
	/*    EditText nameEdit = (EditText)findViewById(R.id.edit_name);
	    EditText commentEdit = (EditText)findViewById(R.id.edit_comment);
	    String name = nameEdit.getText().toString();
	    String comment = commentEdit.getText().toString();
	 
	    HttpClient client = new DefaultHttpClient();
	    HttpPost post = new HttpPost("http://example.com/data/feedmepostrequests.php");
	 
	    // set values you'd like to send
	    List pairs = new ArrayList();
	    pairs.add(new BasicNameValuePair("name", name));
	    pairs.add(new BasicNameValuePair("comment", comment));
	    pairs.add(new BasicNameValuePair("article_id", articleID));
	 
	    // you probably won't need these two lines below, but I have to work with web service
	    // which has very annoying caching issues, so I usually pass some random argument
	    // to avoid possible failure
	    double r = Math.random();
	    pairs.add(new BasicNameValuePair("rand", "r"+r));
	 
	    try {
	        post.setEntity(new UrlEncodedFormEntity(pairs));
	        // set ResponseHandler to handle String responses
	        ResponseHandler<String> responseHandler = new BasicResponseHandler();
	        String response = client.execute(post, responseHandler);
	        Log.v("HttpPost", "Response: " + response);
	        if (response.contains("SUCCESS")){
	            // express your joy here!
	        } else {
	            // pop a sad Toast message here...
	        }
	    } catch (Exception e) {}*/
	}
}
