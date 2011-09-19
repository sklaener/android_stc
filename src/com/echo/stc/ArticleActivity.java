package com.echo.stc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.util.EncodingUtils;

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
import android.util.Log;
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

public class ArticleActivity extends Activity {
	private Article story;
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
	private final ArrayList<Article> finalStories = CategoryListActivity.storiesStatic;
	ProgressBar refreshProgress;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setTheme(android.R.style.Theme_Light_NoTitleBar);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.article);

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

		FrameLayout l = (FrameLayout) findViewById(R.id.webContainer);
		web.setVisibility(View.INVISIBLE);
		l.addView(web);

		refreshProgress = (ProgressBar)findViewById(R.id.dashprogress);
		
		Intent i = this.getIntent();
		storyNum = i.getIntExtra("storyNumber", -1);
		storyCount = i.getIntExtra("storyCount", 0);
		
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
				Intent i2 = new Intent(ArticleActivity.this, DashboardActivity.class);
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
				String title = story.getTitle();
				if (story.getTitle().length() > 140) {
					title = title.substring(0, 137) + "...";
				}
				share(title, story.getURL());
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
		if (storyNum > -1) {
			story = finalStories.get(storyNum);

		} else {
			story = new Article();
			story.setURL(this.getIntent().getStringExtra("url"));
		}

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
			boolean isForum = false;
			boolean isPlanet = false;
			String line = null;
			String line2 = null;
			File file = new File(Environment.getExternalStorageDirectory()
					+ "/Android/data/com.echo.stc/files/"
					+ story.getURL().split("/")[5] + ".html");
			File file2 = new File(Environment.getExternalStorageDirectory()
					+ "/Android/data/com.echo.stc/files/"
					+ story.getURL().split("/")[5] + "_data.html");
			String url = "";
			if  (file.exists()){
				file.delete();
			}
			
			if (!file.exists() || story.getCategory().equals("Forum") || story.getCategory()=="Forum") {

				if (story.getCategory().equals("Forum") || story.getCategory()=="Forum"){
					url = story.getURL();

				} else{
					url = story.getURL()+"?wpmp_switcher=desktop" ;
				}
				
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(
								new URL(url).openStream()));
				FileWriter f = new FileWriter(file);
				FileWriter f2 = new FileWriter(file2);
					
				while (true) {
					line = reader.readLine();

					if (line != null	&& (line.contains("<div class=\"entry-content\">") || line	.contains("<div class=\"discussion\">") || line.contains("<div class=\"entry\">"))) {
						if (line.contains("<div class=\"discussion\">")) {
							isForum = true;
							isPlanet = false;
						} else if (line.contains("<div class=\"entry\">")) {
							isPlanet = true;
							isForum = false;
						}else {
							isForum = false;
						}

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
						if (forumPageNr==1){
							f.append("<h1>" + Parse.removeErrors(story.getTitle())+ "</h1>");
							if(isForum ){
								f.append("<table align=\"left\" width=\"48\" height=\""+ (48) +"\">");
								f.append("<tr><td><img src=\""+story.getImageURL()+"\" </td></tr>");
								f.append("</table>");
							}
							if (story.getAuthor() != null && story.getAuthor() != ""){
								f.append("<author>" + story.getAuthor() + "</author>");
							}else{
								f.append("<author>" + "" + "</author>");
							}
						}

						while ((line = reader.readLine()) != null) {
							try {
								if (line.contains("<!--END .author-bio-->")) {
									while (!(line.contains("<!-- You can start editing here. -->"))) {
										line = reader.readLine();
										// if comments are there, take em
									}
								} else if (line.contains("<h2>Leave a Comment")){
									break;
									
								} else if (isPlanet && line.contains("<div style=\"display: none\">")){
									while (!(line.contains("<div id=\"comments\">"))) {
										line = reader.readLine();
										// if comments are there, take em
									}
									while (!line.contains("<h3 id=\"reply-title\">Leave a Reply")){
										f.append(Parse.removeErrors(line + "\n"));
										line = reader.readLine();
									}
									break;									
							    }else if (isForum &&line.contains("<p class=\"small\" id=\"tagsList\">")){
									while (!(line	.contains("<dl id=\"cf\" class=\"discussion noindent\">"))) {
										line = reader.readLine();
										// if comments are there, take em
									}
								} else if (isForum &&line.contains("<p class=\"small\" id=\"tagsList\">")){
									while (!(line	.contains("<dl id=\"cf\" class=\"discussion noindent\">"))) {
										line = reader.readLine();
										// if comments are there, take em
									}
								} 
	
								else if (isForum && line != null && line.contains("<ul class=\"pagination easyclear \">")){
									while(!line.contains("dojoType=\"Pagination\"")){
										line=reader.readLine();
									}
									String maxPage = line.substring(line.indexOf("_maxPage=\"")+10);
									maxPage = maxPage.substring(0, maxPage.indexOf("\""));
									int numberOfPages = Integer.parseInt(maxPage);
									while (forumPageNr < numberOfPages) {
										String tempUrl = line.substring(line.indexOf("_gotoUrl=\"")+10);
										tempUrl = tempUrl.substring(0, tempUrl.indexOf("\">"));
										String firstPart =  tempUrl.substring(0, tempUrl.indexOf("__PAGE___"));
										firstPart = firstPart.substring(0,firstPart.indexOf("&amp"));
										forumPageNr++;
										tempUrl = firstPart +"&page="+ String.valueOf(forumPageNr);
										//tempUrl ="http://www.softwaretestingclub.com/forum/topics/what-do-you-think-about?id=751045%3ATopic%3A70694&page=2";
										
										BufferedReader reader2 = new BufferedReader( new InputStreamReader(new URL(tempUrl+"?wpmp_switcher=desktop").openStream()));
	
										while (true) {
											line2 = reader2.readLine();
											if (line2 == null) {
												break;
											}
											if (line2.contains("<h3 id=\"comments\" _scrollTo=\"cid-\">Replies to This Discussion</h3>")) {
												while (true) {
													line2 = reader2.readLine();
													if (line2 == null) {
														break;
													}
													if (line2.contains("<ul class=\"pagination easyclear \">")){
														break;
													} else {
														f.append(Parse.removeErrors(line2 + "\n"));
													}
												}
											}
										}
									}
									/*	while (!line.contains("<ul class=\"pagination easyclear \">")) {
											//line = reader.readLine();
											while (!line.contains("<h3 id=\"comments\" _scrollTo=\"cid-\">Replies to This Discussion</h3>")){
													line = reader.readLine();
												}	if (line != null){
													f.append(line + "\n");

												} line = reader.readLine();
												while (line == null){
													line = reader.readLine();//do nothing
												}
										}*/	
									//	break;
								} else if (line != null && line.contains("http://www.gravatar.com/avatar/")){
			                		f2.append("authorPicUrl " + (line.substring(line.indexOf("http://www.gravatar.com/avatar/"), line.lastIndexOf("'")-2))+"\n");
								} else if (line.contains("navigation single-page-navigation") || line.contains(" <div class=\"xg_module_foot\">") || line.contains("<div id=\"sidebar\">")) {
									break;
								} else {
									if (line.contains("<img")) {
										String imageLine = line;
										imageLine = imageLine.substring(imageLine.indexOf("<img"));
										imageLine = imageLine.substring(0,imageLine.indexOf("/>") + 2);
										line.replace(imageLine, "<center>"+ imageLine + "</center>");
									}
									f.append(Parse.removeErrors(line + "\n"));
								}
							} catch (NullPointerException e) {
								Log.e("STC", e.toString());
							}

						}
					} else if (line != null && line.contains("entry-title")) {
						String title = line.substring(line.indexOf(">") + 1);
						title = title.substring(0, title.indexOf("<"));
						story.setTitle(Parse.removeErrors(Parse.toString(title)));
					} else if (line != null && line.contains("title=\"Posts by")) {
						String author = line.substring(line.indexOf("Posts by ") + 9);
						author = author.substring(0, author.indexOf("\""));
						story.setAuthor(Parse.removeErrors(Parse	.toString(author)));
					} else if (line != null && line.contains("<div class=\"post-utility\">")) {
						String author = line.substring(line.indexOf("rel=\"tag\">") + 9);
						author = author.substring(0, author.indexOf("</a>,"));
						story.setAuthor(Parse.removeErrors(Parse	.toString(author)));
					} else if (line == null) {
						break;
					}
				}
				f.append("</body>");
				f.append("</HTML>");
				f.flush();
				f.close();
				f2.flush();
				f2.close();
			}

		} catch (Exception e) {
			Log.e("STC", "Could not access site file: " + e.toString());
			Message m = new Message();
			m.arg1 = 3;
			handler.sendMessage(m);
		}

	}

	public void loadHtml() {
		try {
			web.loadUrl("file:///sdcard/Android/data/com.echo.stc/files/"	+ story.getURL().split("/")[5] + ".html");
		//http://www.softwaretestingclub.com/main/authorization/signIn?target=
		} catch (Exception e) {
			e.printStackTrace();
			Log.e("Site with issue: ", story.getURL());
		}
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, R.id.comment, 0, "Comment");
		menu.getItem(0).setIcon(R.drawable.comments);
		menu.add(1, R.id.prevArt, 1, "Previous");
		menu.getItem(1).setIcon(R.drawable.ic_menu_back);
		menu.add(2, R.id.nextArt, 2, "Next");
		menu.getItem(2).setIcon(R.drawable.ic_menu_forward);
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
		case R.id.comment:
			Intent i = new Intent(Intent.ACTION_VIEW);  
			i.setData(Uri.parse(story.getURL()));  
			startActivity(i);  
			return true;
		case R.id.prevArt:
			nextStory();
			return true;
		case R.id.nextArt:
			previousStory();
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
				Toast.makeText(ArticleActivity.this, "Unable to load page",
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
			toast = Toast.makeText(ArticleActivity.this,
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
					previousStory();
				} else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
						&& Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY*2) {
					nextStory();
				}
			} catch (Exception e) {
				// nothing
			}
			return true;
		}

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

}