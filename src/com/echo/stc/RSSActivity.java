package com.echo.stc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.CharsetEncoder;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.lang.Object;
import org.apache.*;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.util.EncodingUtils;
import org.jsoup.Jsoup;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml.Encoding;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebSettings.RenderPriority;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

public class RSSActivity extends Activity {
	Toast toast = null;
	FlingWebView web = null;
	// ArrayList<String> categories = new ArrayList<String>();
	// String authorPicUrl;
	AsyncTask<Void, String, Void> task, task2;
	// String id, id2 = "";
	private Vibrator myVib;
	private int storyNum, storyCount;
	private static final int SWIPE_MIN_DISTANCE = 120;
	private static final int SWIPE_MAX_OFF_PATH = 250;
	private static final int SWIPE_THRESHOLD_VELOCITY = 200;
	ApplicationSettings settings;
	static Handler mHandler = new Handler();
	boolean isMainShowing, isAnimating = false, toLeft = false;
	// ImageView newer;
	// ImageView older;
	ImageView storyBitmap, storyBitmap2;
	static Animation enterLeftToRight, exitLeftToRight, enterRightToLeft,
			exitRightToLeft;
	TextView loadingText;
	int numberOfCategories = 0;
	String dashRSS = "";
	int forumPageNr = 1; //initialize with 2, cause 1 is not needed anyways
	//private final ArrayList<RSSData> finalBloggers =DashboardActivity.my
	String author, category, blogTitle, title, content, url;
	ProgressBar refreshProgress;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setTheme(android.R.style.Theme_Light_NoTitleBar);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.rssarticle);

		myVib = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);
		settings = (ApplicationSettings) this.getApplication();
		enterLeftToRight = AnimationUtils.loadAnimation(this,
				R.anim.enterlefttoright);
		exitLeftToRight = AnimationUtils
				.loadAnimation(this, R.anim.lefttoright);

		enterRightToLeft = AnimationUtils.loadAnimation(this,
				R.anim.enterrighttoleft);
		exitRightToLeft = AnimationUtils
				.loadAnimation(this, R.anim.righttoleft);
		toast = Toast.makeText(this, "Unable to connect", 5);
		loadingText = (TextView) findViewById(R.id.load);
		
		web = new FlingWebView(this);
		LayoutParams params = new LayoutParams(
				RelativeLayout.LayoutParams.FILL_PARENT,
				RelativeLayout.LayoutParams.FILL_PARENT);
		web.getSettings().setJavaScriptEnabled(true);
		web.getSettings().setPluginsEnabled(true);
		web.getSettings().setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
		web.setHorizontalScrollBarEnabled(false);
		web.setVerticalScrollBarEnabled(true);
		web.getSettings().setDefaultFontSize(settings.getFontSize());
		web.getSettings().setSupportZoom(false);
		web.getSettings().setRenderPriority(RenderPriority.HIGH);
		web.setDrawingCacheEnabled(true);
		web.setGestureDetector(new GestureDetector(this,
				new LearnGestureListener()));
		web.setLayoutParams(params);
		web.setAlwaysDrawnWithCacheEnabled(true);
		
		refreshProgress = (ProgressBar)findViewById(R.id.dashprogress);
		
		FrameLayout l = (FrameLayout) findViewById(R.id.webContainer);
		web.setVisibility(View.INVISIBLE);
		l.addView(web);
					
		AnimationListener al = new AnimationListener() {

			@Override
			public void onAnimationEnd(Animation arg0) {
				web.clearView();
				web.setVisibility(View.GONE);
				loadStory();
			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}

			@Override
			public void onAnimationStart(Animation animation) {

			}

		};

		exitLeftToRight.setAnimationListener(al);
		exitRightToLeft.setAnimationListener(al);

		ImageView logo = (ImageView) findViewById(R.id.imageView2);
		logo.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (settings.getVibrateEnabled()) {
					myVib.vibrate(50);
				}
				
				finish();
			}

		});
    	ImageButton home = (ImageButton) findViewById(R.id.home);
		home.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (settings.getVibrateEnabled()) {
					myVib.vibrate(50);
				}
				Intent i2 = new Intent(RSSActivity.this, DashboardActivity.class);
				i2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(i2);
				finish();
			}
		});
		ImageButton share = (ImageButton) findViewById(R.id.share);
		share.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (settings.getVibrateEnabled()) {
					myVib.vibrate(50);
				}
				share(title, url);
			}
		});
		ImageButton browser = (ImageButton) findViewById(R.id.browser);
		browser.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (settings.getVibrateEnabled()) {
					myVib.vibrate(50);
				}
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(url));
				startActivity(i);
			}
		});
		try {

				loadStory();	
			
			
		} catch (IndexOutOfBoundsException e) {

		}

	}

	@Override
	public void onResume() {
		super.onResume();
		if (CategoryListActivity.getMeHome) {
			finish();
		}
	}

	public void loadStory() {
		Intent i = this.getIntent();
		
		author = i.getStringExtra("author");
		blogTitle = i.getStringExtra("blog");
		title = i.getStringExtra("title");
		category  = i.getStringExtra("category");
		content = i.getStringExtra("content");
		url = i.getStringExtra("url");

		task = new AsyncTask<Void, String, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				try {
					getHtml();
				} catch (ClientProtocolException e) {
					toast.show();
					return null;
				} catch (IOException e) {
					toast.show();
					e.printStackTrace();
					return null;
				} finally {
					loadHtml();
					Message m = new Message();
					m.arg1 = 2;
					handler.sendMessage(m);
				}
				return null;

			}
		};

		task.execute(null);
	}

	public void getHtml() throws ClientProtocolException, IOException {

		try {
			String itemContent = "";
			String line2 = null;
			if( !(new File(Environment.getExternalStorageDirectory()+ "/Android/")).exists())
				(new File(Environment.getExternalStorageDirectory()+ "/Android/")).mkdir();
			if(!(new File(Environment.getExternalStorageDirectory()+ "/Android/data/")).exists())
				(new File(Environment.getExternalStorageDirectory()+ "/Android/data/")).mkdir();
			if(!(new File(Environment.getExternalStorageDirectory()+ "/Android/data/com.echo.stc/")).exists())
				(new File(Environment.getExternalStorageDirectory()+ "/Android/data/com.echo.stc/")).mkdir();
			
			File file = new File(Environment.getExternalStorageDirectory()
					+ "/Android/data/com.echo.stc/files/rss.html");
					//+ "rss" + ".html");
			File file2 = new File(Environment.getExternalStorageDirectory()
					+ "/Android/data/com.echo.stc/files/rss_data.html");
		
	
				FileWriter f = new FileWriter(file);
				FileWriter f2 = new FileWriter(file2);
				f.append("<HTML>");
				f.append("<head>");
						f.append("<style type=\"text/css\">");
						f.append("h1 {");
						f.append("font-size: 1.3em;");
						f.append("color:#375816;\npadding-top: 13px;\nline-height: 1.3; }");
						f.append("author {\ncolor:#898989;\nfont-size: .8em;\npadding-bottom: 5px;\nline-height: 1; }\n");
						f.append("body {\ncolor: #4F4F4F;\npadding-left: 2px; \n font-family:georgia;}"); // font-family:georgia,garamond,serif;
						f.append("h1, h2, .entry-title a {\n"
								+ "	font-size: 18px;\n"
								+ "	color: #375816;\n"
								+ "}\n"
								+ "h2 {\n"
								+ "	color: #558822;\n"
								+ "}\n"
								+ "h3 {\n"
								+ "	font-size: 16px;\n"
								+ "	color: #558822;\n"
								+ "	margin-top: 0.5em;\n"
								+ "	margin-bottom: 0.5em;\n"
								+ "}\n"
								+ "h4 {\n"
								+ "	font-size: 16px;\n"
								+ "	color: #375816;\n"
								+ "	margin-top: 0.5em;\n"
								+ "	margin-bottom: 0.5em;\n"
								+ "}\n"
								+ "h5 {\n"
								+ "	font-size: 13px;\n"
								+ "	color: #558822;\n"
								+ "	margin-top: 0.5em;\n"
								+ "	margin-bottom: 0.5em;\n"
								+ "}\n"
								+ "h6 {\n"
								+ "	font-size: 13px;\n"
								+ "	color: #375816;\n"
								+ "	margin-top: 0.5em;\n"
								+ "	margin-bottom: 0.5em;\n"
								+ "}\n"
								+ "@font-face {\n"
								+ "font-family: serif;\n"
								+ "}"
								+ "a {\n"
								+ "	color: #558822;\n"
								+ "	text-decoration: none;\n"
								+ "	-webkit-transition:color 0.15s ease-in;\n"
								+ "	-moz-transition:color 0.15s ease-in;\n"
								+ "	-o-transition:color 0.15s ease-in;\n"
								+ "}\n"
								+ "a:visited {"
								+ "color: #237989;"
								+ "}"
								+ "a:hover {"
								+ "	color: #08a8c5;"
								+ "}"
								+ "p {"
								+ "	margin-bottom: 1.5em;"
								+ "}"
								+ "ul {"
								+ "	margin: 0 0 1.5em 2.5em;"
								+ "}"
								+ "ol {"
								+ "	margin: 0 0 1.5em 2.5em;"
								+ "}"
								+ "ul {"
								+ "	list-style:disc;"
								+ "}"
								+ "ol {"
								+ "	list-style-type: decimal;"
								+ "}"
								+ "ol ol {"
								+ "	list-style:upper-alpha;"
								+ "}"
								+ "ol ol ol {"
								+ "	list-style:lower-roman;"
								+ "}"
								+ "ol ol ol ol {"
								+ "	list-style:lower-alpha;"
								+ "}"
								+ "ul ul, ol ol, ul ol, ol ul {"
								+ "margin-bottom:0;"
								+ "}"
								+ "dl	{"
								+ "	margin:0 1.5em;"
								+ "}"
								+ "dt {"
								+ "	font-weight: bold;"
								+ "}"
								+ "dd {"
								+ "	margin-bottom: 1.5em;"
								+ "}"
								+ "strong {"
								+ "	font-weight: bold;"
								+ "}"
								+ "cite, em, i {"
								+ "	font-style: italic;"
								+ "}"
								+ "blockquote {"
								+ "	margin: 0 3em;"
								+ "}"
								+ "blockquote em, blockquote i, blockquote cite {"
								+ "	font-style: normal;"
								+ "}"
								+ "pre {"
								+ "	font: 11px Monaco, monospace;"
								+ "	line-height: 1.5;"
								+ "	margin-bottom: 1.5em;"
								+ "}"
								+ "code {"
								+ "	font: 11px Monaco, monospace;"
								+ "}"
								+ "abbr, acronym {"
								+ "  border-bottom: 1px dotted #666;"
								+ "  cursor: help;"
								+ "}"
								+ "ins {"
								+ "	text-decoration: none;"
								+ "}"
								+ "sup,"
								+ "sub {"
								+ "	height: 0;"
								+ "	line-height: 1;"
								+ "	vertical-align: baseline;"
								+ "	position: relative;"
								+ "	"
								+ "}"
								+ "sup {"
								+ "	bottom: 1ex;"
								+ "}"
								+ "sub {"
								+ "	top: .5ex;"
								+ "}"
								+ ".entry-content img {"
								+ "                			margin: 0 0 1.5em 0;"
								+ "                		}"
								+ "                		.alignleft,"
								+ "                		img.alignleft {"
								+ "                			display: inline;"
								+ "                			float: left;"
								+ "                		  margin-right: 1.5em;"
								+ "                		}"
								+ "                		.alignright,"
								+ "                		img.alignright {"
								+ "                			display: inline;"
								+ "                			float: right;"
								+ "                		  margin-left: 1.5em;"
								+ "                		}"
								+ "                		.aligncenter,"
								+ "                		img.aligncenter {"
								+ "                			clear: both;"
								+ "                			display: block;"
								+ "                			margin-left: auto;"
								+ "                			margin-right: auto;"
								+ "                		}"
								+ "                		.wp-caption {"
								+ "                			text-align: center;"
								+ "                			margin-bottom: 1.5em;"
								+ "                		}"
								+ "                		.wp-caption img {"
								+ "                			border: 0 none;"
								+ "                			margin: 0;"
								+ "               			padding: 0;"
								+ "                		}"
								+ "                		.wp-caption p.wp-caption-text {"
								+ "                			margin: 0;"
								+ "                		}"
								+ "                		.wp-smiley {"
								+ ""
								+ "                			max-height: 1em;"
								+ "                			margin:0 !important;"
								+ "                		}"
								+ "                		.gallery dl {"
								+ "                			margin: 0;"
								+ "                		}"
								+ "                		.gallery-caption {"
								+ "                			margin:-1.5em 0 0 0;"
								+ "                		}"
								+ ""
								+ "                		/* Pullquotes"
								+ "                		-------------------------------------------------------------- */"
								+ "" + "                		blockquote.left {"
								+ "                			float: left;"
								+ "                			margin-left: 0;"
								+ "                			margin-right: 20px;"
								+ "                			text-align: right;"
								+ "                			width: 33%;"
								+ "                		}"
								+ "                		blockquote.right {"
								+ "                			float: right;"
								+ "                			margin-left: 20px;"
								+ "                			margin-right: 0;"
								+ "                			text-align: left;"
								+ "                			width: 33%;"
								+ "                		}                				");
						f.append("</style></head>");
						f.append("");
						f.append("<body>");
						f.append("<h1>" +unicode2Html(title)+ "</h1>");
						if (blogTitle.equalsIgnoreCase(author)){
							f.append("<author>" + unicode2Html(blogTitle)  + "</author>");
						} else if (author.equalsIgnoreCase("unknown")){
							f.append("<author>" + unicode2Html(blogTitle)  + "</author>");
						}	
						else{
							f.append("<author>" +unicode2Html(blogTitle) + " by  "+unicode2Html(author)+ "</author>");
						}
				
				f.append("\n<br>");
				String temp = html2text(content);
			//	String temp = android.text.Html.fromHtml(content).toString();
				//f.append(	Html.fromHtml(Html.fromHtml(content).toString()).toString());
				f.append(unicode2Html(temp));

				f.append("</body>");
				f.append("</HTML>");
				f.flush();
				f.close();
				f2.flush();
				f2.close();
			
		} catch (Exception e) {
			Log.e("STC", "Could not access site file: " + e.toString());
			Message m = new Message();
			m.arg1 = 3;
			handler.sendMessage(m);
		}

	}

	public void loadHtml() {
		try {
			web.loadUrl("file:///sdcard/Android/data/com.echo.stc/files/"	+ "rss"+ ".html");
		//http://www.softwaretestingclub.com/main/authorization/signIn?target=
		} catch (Exception e) {
			e.printStackTrace();
			Log.e("Site with issue: ", url);
		}
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, R.id.share, 0, "Share");
		menu.getItem(0).setIcon(android.R.drawable.ic_menu_share);
		menu.add(1, R.id.comment, 1, "Comment");
		menu.getItem(1).setIcon(R.drawable.comments);
		return true;
		// <RelativeLayout android:layout_alignParentBottom="true"
		// android:layout_height="40dp" android:layout_width="fill_parent"
		// android:id="@+id/footer">
		// <ImageView android:background="@drawable/textbar"
		// android:scaleType="fitXY" android:layout_height="fill_parent"
		// android:id="@+id/footerBar" android:layout_width="fill_parent"
		// android:layout_alignParentBottom="true"></ImageView>
		// <ImageView android:layout_alignParentLeft="true"
		// android:scaleType="fitCenter" android:id="@+id/share"
		// android:layout_height="wrap_content"
		// android:src="@drawable/ic_menu_share"
		// android:layout_width="wrap_content"
		// android:layout_centerVertical="true"></ImageView>
		// <ImageView android:layout_alignParentRight="true"
		// android:scaleType="fitStart" android:id="@+id/forward"
		// android:layout_height="wrap_content"
		// android:src="@drawable/ic_menu_forward"
		// android:layout_width="wrap_content"
		// android:layout_centerVertical="true"></ImageView>
		// <ImageView android:layout_toLeftOf="@+id/forward"
		// android:layout_alignParentBottom="true" android:scaleType="fitStart"
		// android:id="@+id/back" android:layout_height="wrap_content"
		// android:src="@drawable/ic_menu_back"
		// android:layout_width="wrap_content"
		// android:layout_centerVertical="true"></ImageView>
		// </RelativeLayout>
	}

	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.share:
			String temp = html2text(content);
			//	String temp = android.text.Html.fromHtml(content).toString();
			share(title, url);
			return true;
		case R.id.comment:
			Intent i = new Intent(Intent.ACTION_VIEW);  
			i.setData(Uri.parse(url));  
			startActivity(i);  
			return true;
		}
		return false;
	}

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.arg1 == 1) { // load site with pending animation

				if (msg.arg2 == 0) { // run to left
					toLeft = true;
				} else if (msg.arg2 == 1) { // left to right
					toLeft = false;
				}

				if (toLeft) {
					web.startAnimation(exitRightToLeft);
					loadingText.startAnimation(enterRightToLeft);
				} else {
					web.startAnimation(exitLeftToRight);
					loadingText.startAnimation(enterLeftToRight);
				}

			} else if (msg.arg1 == 2) {
				web.setVisibility(View.VISIBLE);
				refreshProgress.setVisibility(View.GONE);
			} else if (msg.arg1 == 3) {
				Toast.makeText(RSSActivity.this, "Unable to load page",
						Toast.LENGTH_LONG).show();
			} else {
				web.removeAllViews();
			}
		}
	};

	public void share(String subject, String text) {
		final Intent intent = new Intent(Intent.ACTION_SEND);

		intent.setType("text/plain");
		intent.putExtra(Intent.EXTRA_SUBJECT, subject);
		intent.putExtra(Intent.EXTRA_TEXT, text);

		startActivity(Intent.createChooser(intent, "Share this story"));
	}

	private void nextStory() {
		if (storyNum != 0) {
			storyNum = storyNum - 1;
			if (settings.getVibrateEnabled()) {
				myVib.vibrate(50);
			}
			Message m = new Message();
			m.arg1 = 1;
			m.arg2 = 1;
			handler.sendMessage(m);
		}
	}

	private void previousStory() {
		if (settings.getVibrateEnabled()) {
			myVib.vibrate(50);
		}
		if (storyNum + 1 < CategoryListActivity.storiesStatic.size() - 1) {
			storyNum = storyNum + 1;
			Message m = new Message();
			m.arg1 = 1;
			m.arg2 = 0;
			handler.sendMessage(m);
		} else {
			toast = Toast.makeText(RSSActivity.this,
					"Return to article page and load more stories", 10);
			toast.show();
		}
	}

	class LearnGestureListener extends GestureDetector.SimpleOnGestureListener {

		@Override
		public boolean onSingleTapUp(MotionEvent ev) {
			// Log.d("onSingleTapUp",ev.toString());
			return true;
		}

		@Override
		public void onShowPress(MotionEvent ev) {
			// Log.d("onShowPress",ev.toString());
		}

		@Override
		public void onLongPress(MotionEvent ev) {

			// Log.d("onLongPress",ev.toString());
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			// Log.d("onScroll",e1.toString());
			return true;
		}

		@Override
		public boolean onDown(MotionEvent ev) {
			// Log.d("onDownd",ev.toString());
			return true;
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {

			try {
				if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
					return false;
				// right to left swipe
				if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE
						&& Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY*2) {

				} else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
						&& Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY*2) {

				}
			} catch (Exception e) {
				// nothing
			}
			return true;
		}

	}
	public static String html2text(String html) {
	    return Jsoup.parse(html).text();
	}
	   public static String unicode2Html(String temp){
		    temp = temp.replaceAll( "[\\u0022]", "&quot;" );
		    temp = temp.replaceAll( "[\\u201C]", "&quot;" );
		    temp = temp.replaceAll( "[\\u201D]", "&quot;" );
		    temp = temp.replaceAll( "[\\u201E]", "&quot;" );
		    temp = temp.replaceAll( "[\\u201F]", "&quot;" );
		    temp = temp.replaceAll( "[\\u2033]", "&quot;" );
		    temp = temp.replaceAll( "[\\u3003]", "&quot;" );
		    temp = temp.replaceAll( "[\\u0022]", "&#39;" );
		    temp = temp.replaceAll( "[\\u2018]", "&#39;" );
		    temp = temp.replaceAll( "[\\u2019]", "&#39;" );
		    temp = temp.replaceAll( "[\\u201A]", "&#39;" );
		    temp = temp.replaceAll( "[\\u201B]", "&#39;" );
		    temp = temp.replaceAll( "[\\u2032]", "&#39;" );
		    temp = temp.replaceAll( "[\\u0301]", "&#39;" );
		    temp = temp.replaceAll( "[\\u0300]", "&#39;" );
		    temp = temp.replaceAll( "[\\u2010]", "&#8208;" );
		    temp = temp.replaceAll( "[\\u002D]", "&#8208;" );
		    temp = temp.replaceAll( "[\\u00AD]", "&#8208;" );
		    temp = temp.replaceAll( "[\\u2011]", "&#8208;" );
		    temp = temp.replaceAll( "[\\u2012]", "&#8208;" );
		    temp = temp.replaceAll( "[\\u2013]", "&#8208;" );
		    temp = temp.replaceAll( "[\\u2014]", "&#8208;" );
		    temp = temp.replaceAll( "[\\u2015]", "&#8208;" );
		    temp = temp.replaceAll( "[\\u2043]", "&#8208;" );
		    temp = temp.replaceAll( "[\\u2212]", "&#8208;" );
		    temp = temp.replaceAll( "[\\u0020]", "&#32;" );
		    temp = temp.replaceAll( "[\\u00A0]", "&#32;" );
		    temp = temp.replaceAll( "[\\u2003]", "&#32;" );
		    temp = temp.replaceAll( "[\\u2002]", "&#32;" );
		    temp = temp.replaceAll( "[\\u2026]", "&#8230;" );
		    return temp;
		  }
	   
	   @Override
	   public boolean onKeyDown(int keyCode, KeyEvent event) {
	       Log.v("STC key event: ", event.toString());
	       if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN){
	    	   web.pageDown(false);
	           return true;
	       }
	       else if(keyCode == KeyEvent.KEYCODE_VOLUME_UP){
	    	   web.pageUp(false);
	           return true;
	       }
	       else if(keyCode == KeyEvent.KEYCODE_BACK){
	    	   finish();
	           return true;
	       }
	       return false;
	   }
		@Override
		public void onBackPressed() {
				finish();
		// do something on back.
		return;
		}
		  
}