package com.echo.stc;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.http.client.ClientProtocolException;
import org.jsoup.Jsoup;

import com.echo.stc.ArticleActivity.LearnGestureListener;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.Html;
import android.text.TextUtils;
import android.text.InputFilter.LengthFilter;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebViewClient;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebSettings.RenderPriority;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SlidingDrawer;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.ViewFlipper;

import org.jsoup.safety.Whitelist;
import org.apache.commons.*;

public class DashboardActivity extends Activity {
	AsyncTask<Void, String, Void> task, task2;
	ApplicationSettings settings;
	Button feat1, feat2, feat3, feat4, feat5, feat6, feat7;
	ImageButton bloggers, podcasts, videos;
	private ListView mListView;
	private Vibrator myVib;
	private Context c;
	SlidingDrawer slider;
	boolean isBloggers = false;
	boolean isPodcasts = false;
	boolean isVideos = false;
	boolean isNeither = false;
	boolean hasbeenLoadedBloggers = false;
	boolean hasbeenLoadedPodcasts = false;
	ArrayList<RSSData> myBloggersList;
	ArrayList<RSSData> myPodcastsList;
	ArrayList<RSSData> myVideosList;
	ArrayList<RSSData> myNeitherList;
	Toast toast;
	private final int BLOGGERS = 0;
	private final int PODCASTS = 1;
	private final int VIDEOS = 2;
	private final int COMMUNITIES = 3;
	private final int COMPANIES = 4;
	private final int EVENTS = 5;
	private final int JOBS = 6;
	private final int EVENTCALENDAR = 7;
	private int defaultCategory = 0;
	private ProgressBar refreshProgress;
	private String defCat;
	private static final int SWIPE_MIN_DISTANCE = 120;
	private static final int SWIPE_MAX_OFF_PATH = 250;
	private static final int SWIPE_THRESHOLD_VELOCITY = 200;
	ViewFlipper flipper;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setTheme(android.R.style.Theme_Light_NoTitleBar);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dashboard);
		InitializeUI();

	}

	@Override
	public void onBackPressed() {
		 if (slider.isOpened()){
			slider.animateClose();
		}else
			finish();
		
	// do something on back.
	return;
	}
	
	@Override
	public void onResume() {
		super.onResume();

	}

	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, R.id.rsscommunities, 0, "Communities");
		menu.getItem(0).setIcon(R.drawable.blog);
		menu.add(1, R.id.rsscompanies, 1, "Companies");
		menu.getItem(1).setIcon(R.drawable.blog);
		menu.add(2, R.id.rssevents, 2, "Events");
		menu.getItem(2).setIcon(R.drawable.blog);
		menu.add(3, R.id.rssjobs, 3, "Jobs");
		menu.getItem(3).setIcon(R.drawable.blog);
		menu.add(4, R.id.rsseventcal, 4, "Upcoming Events");
		menu.getItem(4).setIcon(android.R.drawable.ic_menu_my_calendar);
		menu.add(5, R.id.options, 5, "Settings");
		menu.getItem(5).setIcon(android.R.drawable.ic_menu_preferences);
		menu.add(6, R.id.about, 6, "About");
		menu.getItem(6).setIcon(android.R.drawable.ic_menu_info_details);
		return true; 
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		if (settings.getVibrateEnabled()) {
			myVib.vibrate(50);
		}
		switch (item.getItemId()) {
		case R.id.rsscommunities:
			openFeed(COMMUNITIES);
			return true;
		case R.id.rsscompanies:
			openFeed(COMPANIES);
			return true;
		case R.id.rssevents:
			openFeed(EVENTS);
			return true;
		case R.id.rssjobs:
			try {
				if (slider.isOpened()){
					slider.animateClose();
				} else {
					myNeitherList = new ArrayList<RSSData>();
					getJobs(JOBS);
					mListView.setAdapter(new MyListAdapter(this, R.layout.rssrow,	myNeitherList));
					slider.animateOpen();
				}
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return true;
		case R.id.rsseventcal:
			try {
				if (slider.isOpened()){
					slider.animateClose();
				} else {
					myNeitherList = new ArrayList<RSSData>();
					getEventCal(EVENTCALENDAR);
					mListView.setAdapter(new MyListAdapter(this, R.layout.rssrow,	myNeitherList));
					slider.animateOpen();
				}
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return true;
		case R.id.options:
			Intent i = new Intent(this, PostSettingsActivity.class);
			startActivity(i);
			return true;
		case R.id.about:
			Intent i2 = new Intent(this, AboutActivity.class);
			startActivity(i2);
			return true;
		}
		return false;

	}

	
	public void openDefaultFeed(){
		defCat = settings.getSliderCategory();
		if (!defCat.equalsIgnoreCase("None (default)")){
			mListView = (ListView) findViewById(R.id.listView_1);
			if (defCat.equalsIgnoreCase("Bloggers")){
				defaultCategory = BLOGGERS;
			} else if (defCat.equalsIgnoreCase("Podcasts")){
				defaultCategory = PODCASTS;
			} else if (defCat.equalsIgnoreCase("Videos")){
				defaultCategory = VIDEOS;
			} else if (defCat.equalsIgnoreCase("Communities")){
				defaultCategory = COMMUNITIES;
			} else if (defCat.equalsIgnoreCase("Companies")){
				defaultCategory = COMPANIES;
			} else if (defCat.equalsIgnoreCase("Events")){
				defaultCategory = EVENTS;
			} else if (defCat.equalsIgnoreCase("EventCalendar")){
				defaultCategory = EVENTCALENDAR;
			} 
			if (defaultCategory == VIDEOS){
				try {
					getVideos(defaultCategory);
					mListView.setAdapter(new MyListAdapter(DashboardActivity.this, R.layout.rssrow,myVideosList));
				} catch (ClientProtocolException e) {
					toast.show();
					e.printStackTrace();
				} catch (IOException e) {
					toast.show();
					e.printStackTrace();
				}
			}else if (defaultCategory == PODCASTS){
				try {
					getFeed(defaultCategory);
					mListView.setAdapter(new MyListAdapter(DashboardActivity.this, R.layout.rssrow,myPodcastsList));
				} catch (ClientProtocolException e) {
					toast.show();
					e.printStackTrace();
				} catch (IOException e) {
					toast.show();
					e.printStackTrace();
				}
			}else if (defaultCategory == BLOGGERS){
				try {
					getFeed(defaultCategory);
					mListView.setAdapter(new MyListAdapter(DashboardActivity.this, R.layout.rssrow,myBloggersList));
				} catch (ClientProtocolException e) {
					toast.show();
					e.printStackTrace();
				} catch (IOException e) {
					toast.show();
					e.printStackTrace();
				}
			}else if (defaultCategory == JOBS){
				try {
					getJobs(JOBS);
					mListView.setAdapter(new MyListAdapter(DashboardActivity.this, R.layout.rssrow,myNeitherList));
				} catch (ClientProtocolException e) {
					toast.show();
					e.printStackTrace();
				} catch (IOException e) {
					toast.show();
					e.printStackTrace();
				}
			} else {
				try {
					getFeed(defaultCategory);
					mListView.setAdapter(new MyListAdapter(DashboardActivity.this, R.layout.rssrow,myNeitherList));
				} catch (ClientProtocolException e) {
					toast.show();
					e.printStackTrace();
				} catch (IOException e) {
					toast.show();
					e.printStackTrace();
				}
			}
		}
	}
	
	public void openFeed(int cat){
		if (settings.getVibrateEnabled()) {
			myVib.vibrate(50);
		}
		try {
			if (slider.isOpened()){
				slider.animateClose();
			} else {
				myNeitherList = new ArrayList<RSSData>();
				getFeed(cat);
				mListView.setAdapter(new MyListAdapter(this, R.layout.rssrow,	myNeitherList));
				slider.animateOpen();
			}
		} catch (ClientProtocolException e) {
			toast.show();
			e.printStackTrace();
		} catch (IOException e) {
			toast.show();
			e.printStackTrace();
		}
	}
	
	public void getFeed(int cat) throws ClientProtocolException,	IOException {
		String site = "";
		refreshProgress.setVisibility(View.VISIBLE);
		String category= "";
		if (cat == BLOGGERS){
			site = "http://feeds.feedburner.com/stcfeedsbloggers";
			category = "Bloggers";
		}
		else if (cat == PODCASTS){
			site = "http://feeds.feedburner.com/stcfeedspodcasts";
			category = "Podcasts";
		}
		else if (cat == COMMUNITIES){
			site = "http://feeds.feedburner.com/softwaretesters-communities";
			category = "Communities";
		}
		else if (cat == COMPANIES){
			site = "http://feeds.feedburner.com/stcfeedcompanies";
			category = "Companies";
		}
		else if (cat == EVENTS){
			site = "http://feeds.feedburner.com/stcfeedsevents";
			category = "Events";
		}
		else if (cat == EVENTCALENDAR){
			site = "http://www.softwaretestingclub.com/event/event/feed";
			category = "EventCalendar";
		}
		BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(site + "?wpmp_switcher=desktop").openStream(), "utf8"),8192);
		try {
			String line = "";
			while (true) {
				line = reader.readLine();
				if (line == null) {
					break;
				}
				if (line.contains("<entry")) {
					line = line.substring(line.indexOf("<entry"), line.length());
					String title = "", url = "", author = "", blogTitle = "", itemContent="";;
					int feedCounter = 0;
					while (feedCounter <= 20) {
						if (line.contains("<entry")) { // getting url

							if (line.contains("<title type=\"html\">")) {
								title = line.substring(line	.indexOf("<title type=\"html\">") + 19);
								if (!(title.contains("</title>"))){
									line = reader.readLine();
									if(title.equalsIgnoreCase("Blog -")){
										title = line.substring(0, line.indexOf("<"));
										title = title.replaceAll("^\\s+", "");
										
										
										//Toast.makeText(c, title, 5).show();
										//title = title.substring(title.indexOf(title.split("[^a-zA-Z]")[0].length()), title.length());
//										title = title.split("[^a-zA-Z]")[0].length();
										
									} else {
										title += line.substring(0, line.indexOf("<"));
									}
								} else {
									title = title.substring(0, title.indexOf("<"));
								}								
								line = line.substring(line.indexOf("</title>"),	line.length());
							}
						if (line.contains("<summary")	|| line.contains("<content")) {
								int index = 0;
								int iSummary = line.indexOf("<summary");
								int iContent= line.indexOf("<content");
								if ((iSummary>0 &&  iSummary < iContent) || !line.contains("<content")) {
									index = 0;
								} else {
									index = 1;
								}
					/*			switch (index) {
								case 0:
									while (!line.contains("</summary")) {
										line = reader.readLine();
									}
									line = line.substring(line.indexOf("</summary>"),	line.length());
									break;
								case 1:
									while (!line.contains("</content")) {
										line = reader.readLine();
									}
									line = line.substring(line.indexOf("</content>"),	line.length());
									break;
								default:
									line = line.substring(line.indexOf("</content>"),	line.length());
									break;
								}
						}*/
								switch (index) {
								case 0:
									String tempContent = "";
									itemContent = line.substring(line.indexOf("<summary") + 8);
									itemContent = itemContent.substring(itemContent.indexOf(">") + 1);
									if (itemContent.contains("</summary>")) {
										itemContent = itemContent.substring(0,itemContent.indexOf("</summary>"));
									} else {
										while (!line.contains("</summary")) {
											line = reader.readLine();
											itemContent += line;
										}
										itemContent += line.substring(0,line.indexOf("</summary>"));
										itemContent = itemContent.substring(0,itemContent.indexOf("</summary>"));										
										line = line.substring(line.indexOf("</summary>"),	line.length());
									}
									break;
								case 1:
									String tempContent1 = "";
									itemContent = line.substring(line.indexOf("<content") + 8);
									itemContent = itemContent.substring(itemContent.indexOf(">") + 1);
									if (itemContent.contains("</content>")) {
										itemContent = itemContent.substring(0,itemContent.indexOf("</content>"));
									} else {
										//tempContent1 += itemContent;
										while (!line.contains("</content")) {
											line = reader.readLine();
											itemContent += line;
										}
										itemContent += line.substring(0,line.indexOf("</content>"));
										itemContent = itemContent.substring(0,itemContent.indexOf("</content>"));
										//tempContent1 += itemContent;
										//itemContent = tempContent1;
										line = line.substring(line.indexOf("</content>"),line.length());
									}
									break;
								default:
									break;
								}

							}
							if (line.contains("<name>(author unknown)")) {
								author = "unknown";
								line = line.substring(line.indexOf("</name></author>"),line.length());
							} else if (line.contains("<author><name>")) {
								author = line.substring(line.indexOf("<author><name>") + 14);
								author = author.substring(0,author.indexOf("<"));
								line = line.substring(line.indexOf("</name></author>"),line.length());
							}

							if (line.contains("<title type=\"html\">")) {
								blogTitle = line.substring(line	.indexOf("<title type=\"html\">") + 19);
								if (!(blogTitle.contains("</title>"))){
									line = reader.readLine();
									blogTitle += line.substring(0,	line.indexOf("<"));
								} else {
									blogTitle = blogTitle.substring(0,	blogTitle.indexOf("<"));
								}
								line = line.substring(line.indexOf("</title>"),	line.length());

							}
							if (line.contains("</source><feedburner:origLink>")) {
								url = line.substring(line.indexOf("</source><feedburner:origLink>") + 30);
								url = url.substring(0, url.indexOf("<"));
								line = line.substring(line.indexOf("</feedburner:origLink>"),line.length());
							}
							if (line.contains("</entry>")) {
								blogTitle = Html.fromHtml(Html.fromHtml(blogTitle).toString()).toString();
								title = Html.fromHtml(Html.fromHtml(title).toString()).toString();
								if (cat == BLOGGERS){
									RSSData data = new RSSData(title, author,	feedCounter, url, blogTitle,itemContent, category);
									myBloggersList.add(data);
									feedCounter++;			
									isBloggers=true;
									isPodcasts=false;
									isVideos=false;
									isNeither=false;
								}
								else if (cat == PODCASTS){
									RSSData data = new RSSData(title, author,	feedCounter, url, blogTitle,"", category);
									myPodcastsList.add(data);
									feedCounter++;
									isBloggers=false;
									isPodcasts=true;
									isVideos=false;
									isNeither=false;
								}
								else {
									RSSData data = new RSSData(title, author,	feedCounter, url, blogTitle,itemContent, category);
									myNeitherList.add(data);
									feedCounter++;
									isBloggers=false;
									isPodcasts=false;
									isVideos=false;;
									isNeither=true;
								}
								
								// break;
								if (!line.contains("<entry")) {
									line = reader.readLine();
								}
							}
						}
						if (line == null) {
							break;
						}
					}
				}
			}
			refreshProgress.setVisibility(View.GONE);
			} catch (IOException e) {
			Log.e("STC", "Could not save site HTML: " + e.toString());
		}
	}
	
	public void getJobs(int cat) throws ClientProtocolException,IOException {
		String site = "";
		if (cat == JOBS) {
			site = "http://feeds.feedburner.com/StcJobsBoard";
		}
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				new URL(site + "?wpmp_switcher=desktop").openStream()));
		String line = "";
		int feedCounter = 0;
		try{
			
			while (true) {
				line = reader.readLine();
				if (line == null) {
					break;
				}

				if (line.contains("<item>")) {
					String title = "", url = "", author = "unknown", blogTitle = "", itemContent = "";
					while (true) {
						line = reader.readLine();
						if (line == null) {
							break;
						}
						if (line.contains("<title>")) {
							title = line.substring(line	.indexOf("<title>") + 7);
							title = title.substring(0,title.indexOf("</title>"));
							line = line.substring(line.indexOf("</title>"),line.length());
						}
						if (line.contains("<description>")) {
							blogTitle = line.substring(line	.indexOf("<description>") + 13);
							blogTitle = blogTitle.substring(0,blogTitle.indexOf("&lt;img src"));
							line = line.substring(line.indexOf("</description>"),line.length());
						}
						if (line.contains("<feedburner:origLink>")) {
							url = line.substring(line	.indexOf("<feedburner:origLink>") + 21);
							url = url.substring(0,url.indexOf("</feedburner:origLink>"));
							line = line.substring(line.indexOf("</feedburner:origLink>"),line.length());
						}
						if (line.contains("</item>")) {
							itemContent = blogTitle;
							blogTitle = Html.fromHtml(Html.fromHtml(blogTitle).toString()).toString();
							title = Html.fromHtml(Html.fromHtml(title).toString()).toString();

							RSSData data = new RSSData(title, author,	feedCounter, url, blogTitle," ", "Jobs");
							myNeitherList.add(data);
							feedCounter++;
							isBloggers=false;
							isPodcasts=false;
							isVideos=false;;
							isNeither=true;
						}
					}
				}
			}
		} catch (IOException e) {
			Log.e("STC", "Could not save site HTML: " + e.toString());
		}
	}
	
	public void getEventCal(int cat) throws ClientProtocolException,IOException {
		String site = "";
		if (cat == EVENTCALENDAR) {
			site = "http://www.softwaretestingclub.com/events/event/feed";
		}
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				new URL(site + "?wpmp_switcher=desktop").openStream()));
		String line = "";
		int feedCounter = 0;
		try{
			
			while (true) {
				line = reader.readLine();
				if (line == null) {
					break;
				}

				if (line.contains("<entry>")) {
					String title = "", url = "", author = "", blogTitle = "", itemContent = "";
					while (true) {
						line = reader.readLine();
						if (line == null) {
							break;
						}
						if (line.contains("<title>")) {
							title = line.substring(line	.indexOf("<title>") + 7);
							title = title.substring(0,title.indexOf("</title>"));
							line = line.substring(line.indexOf("</title>"),line.length());
						}
						if (line.contains("<link ")) {
							url = line.substring(line	.indexOf("href=\"") + 6);
							url = url.substring(0,url.indexOf("\""));
						}
						if (line.contains("<name>")) {
							author = line.substring(line	.indexOf("<name>") + 6);
							author = author.substring(0,author.indexOf("</name>"));
							line = line.substring(line.indexOf("</name>"),line.length());
						}
						if (line.contains("<content ")) {
							itemContent = line.substring(line.indexOf("<content type=\"html\">") + 21);
							while (!line.contains("</content>")){ 
								line = reader.readLine();
								itemContent+=line;
							}		
							itemContent = itemContent.substring(0,itemContent.indexOf("</content>"));
							line = line.substring(line.indexOf("</content>"),line.length());
						}
						if (line.contains("</entry>")) {
							if (itemContent.contains("Time:")) {
								blogTitle = itemContent.substring(itemContent.indexOf("Time:&lt;a href=&quot;http://www.softwaretestingclub.com/events/event/listByDate?date=") + 5);
								blogTitle = blogTitle.substring(0,blogTitle.indexOf("Location:"));
							}
							blogTitle = Html.fromHtml(Html.fromHtml(blogTitle).toString()).toString();
							title = Html.fromHtml(Html.fromHtml(title).toString()).toString();

							RSSData data = new RSSData(title, blogTitle,	feedCounter, url, blogTitle, itemContent, "EventCalendar");
							myNeitherList.add(data);
							feedCounter++;
							isBloggers=false;
							isPodcasts=false;
							isVideos=false;;
							isNeither=true;
						}
					}
				}
			}
		} catch (IOException e) {
			Log.e("STC", "Could not save site HTML: " + e.toString());
		}
	}
	
	public void getVideos(int cat) throws ClientProtocolException,IOException {
		String site = "";
		if (cat == VIDEOS) {
			site = "http://www.softwaretestingclub.com/video?page=1";
		}
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				new URL(site + "?wpmp_switcher=desktop").openStream()));
		String line = "";
		int feedCounter = 0;
		try{
			while (!line.contains("<h1>All Videos")){
				line = reader.readLine();
			}
			while (true) {
				line = reader.readLine();
				if (line == null) {
					break;
				}

				if (line.contains("<h3>")) {
					String title = "", url = "", author = "", blogTitle = "";
					while (true) {
						line = reader.readLine();
						if (line == null) {
							break;
						}
						if (line.contains("class=\"title\">")){
							url = line.substring(line.indexOf("http://"));
							url = url.substring(0,url.indexOf("\""));
												
							title = line.substring(line.indexOf("class=\"title\">") + 14);
							title = title.substring(0, title.indexOf("</a>"));
							
							while (!line.contains("video/listTagged?")){ 
								line = reader.readLine();
							}							
							author = line.substring(line.indexOf("\">") + 2);
							author = author.substring(0, author.indexOf("</a>"));
		
							RSSData data = new RSSData(title, author,	feedCounter, url, ""," ","Videos");
							myVideosList.add(data);
							feedCounter++;
							isBloggers=false;
							isPodcasts=false;
							isVideos=true;
							isNeither=false;
							break;		
						}
					}
				}
			}
		} catch (IOException e) {
			Log.e("STC", "Could not save site HTML: " + e.toString());
		}
	}
	
	private class MyListAdapter extends ArrayAdapter<RSSData> implements
			OnClickListener {

		private ArrayList<RSSData> items;
		private Context c;

		public MyListAdapter(Context context, int textViewResourceId,
				ArrayList<RSSData> items) {
			super(context, textViewResourceId, items);
			this.items = items;
			this.c = context;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater) c
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.rssrow, null);
			}

			RSSData myData = items.get(position);
			if (myData != null) {
				TextView title = (TextView) v.findViewById(R.id.rssRowText1);
				if(!myData.getCategory().equals("Videos") ){
					if (title != null) {
						String Title = myData.getTitle();
						title.setText(Title);
					}
					TextView title2 = (TextView) v.findViewById(R.id.rssRowText2);
					if (title2 != null) {
						String temp = myData.getBlogTitle() + myData.getAuthor();
						if (myData.getBlogTitle().equalsIgnoreCase(myData.getAuthor())) {
							title2.setText(myData.getBlogTitle());
						} else if (myData.getAuthor().equalsIgnoreCase("unknown")) {
							title2.setText(myData.getBlogTitle());
						} else {
							title2.setText(myData.getBlogTitle() + " by "	+ myData.getAuthor());
						}
					}
				}
				else if (myData.getCategory().equalsIgnoreCase("Videos")){
					if (title != null) {
						String Title = myData.getTitle();
						title.setText(Title);
					}
					TextView title2 = (TextView) v.findViewById(R.id.rssRowText2);
					if (title2 != null) {
						title2.setText(myData.getAuthor());
					}
				}
			}
			
			return v;
		}

		@Override
		public void onClick(View arg0) {
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
			//web.setVisibility(View.GONE);
			//web.clearView();
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
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
	  super.onConfigurationChanged(newConfig);
	  setContentView(R.layout.dashboard);
	  
	  InitializeUI();

	}
	public void InitializeUI()
	{
		settings = (ApplicationSettings) this.getApplication();
		myVib = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);
		Intent i = this.getIntent();
		myBloggersList = new ArrayList<RSSData>();
		myPodcastsList = new ArrayList<RSSData>();
		myVideosList = new ArrayList<RSSData>();
		myNeitherList = new ArrayList<RSSData>();
		toast = Toast.makeText(this, "Unable to connect", 5);
		
		//flipper = (ViewFlipper)findViewById(R.id.dashflipper);
		
		feat1 = (Button) findViewById(R.id.home_btn_feature1);
		feat2 = (Button) findViewById(R.id.home_btn_feature2);
		feat3 = (Button) findViewById(R.id.home_btn_feature3);
		feat4 = (Button) findViewById(R.id.home_btn_feature4);
		feat5 = (Button) findViewById(R.id.home_btn_feature5);
		feat6 = (Button) findViewById(R.id.home_btn_feature6);
		feat7 = (Button) findViewById(R.id.home_btn_feature7);
		slider = (SlidingDrawer)findViewById(R.id.drawer);
		bloggers = (ImageButton)findViewById(R.id.dashblog);
		podcasts =(ImageButton)findViewById(R.id.dashpodcasts);
		videos =(ImageButton)findViewById(R.id.dashvideos);
					
	/*	FrameLayout l = (FrameLayout) findViewById(R.id.webContainer2);
		web.setVisibility(View.GONE);
		l.addView(web);*/
		
		//loading.setVisibility(8);
		
		refreshProgress = (ProgressBar)findViewById(R.id.dashprogress);
		refreshProgress.setVisibility(View.GONE);
		mListView = (ListView) findViewById(R.id.listView_1);
		mListView.setAdapter(new MyListAdapter(this, R.layout.rssrow,	myBloggersList));
		openDefaultFeed();

		ImageView logo = (ImageView) findViewById(R.id.imageView2);
		logo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (settings.getVibrateEnabled()) {
					myVib.vibrate(50);
				}
				if (slider.isOpened()){
					slider.animateClose();
				} else {
				finish();
				}
			}
		});
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				if (settings.getVibrateEnabled()) {
					myVib.vibrate(50);
				}
				// Toast.makeText(DashboardActivity.this, "Clicked on "
				// +temp.getText(), 3).show();
				String url = "";
			//	findViewById(R.id.load2).setVisibility(View.VISIBLE);
				
				
				
				if (isBloggers){
					Intent i = new Intent(DashboardActivity.this, RSSActivity.class);
					i.putExtra("author", myBloggersList.get(arg2).getAuthor());
					i.putExtra("blog", myBloggersList.get(arg2).getBlogTitle());
					i.putExtra("title", myBloggersList.get(arg2).getTitle());
					i.putExtra("category", myBloggersList.get(arg2).getCategory());
					i.putExtra("content", myBloggersList.get(arg2).getContent());
					i.putExtra("url", myBloggersList.get(arg2).getUrl());
				 	startActivity(i);
				} else if (isPodcasts) {
					Intent i = new Intent(Intent.ACTION_VIEW);
					i.setData(Uri.parse(myPodcastsList.get(arg2).getUrl()));
					startActivity(i);
				} else if (isVideos) { 
					Intent i = new Intent(Intent.ACTION_VIEW);
					i.setData(Uri.parse(myVideosList.get(arg2).getUrl()));
					startActivity(i);
				 	//client.shouldOverrideUrlLoading(web, url);	
				} else if (isNeither) {
					if (myNeitherList.get(arg2).getCategory().equalsIgnoreCase("Jobs")){
						Intent i = new Intent(Intent.ACTION_VIEW);
						i.setData(Uri.parse(myNeitherList.get(arg2).getUrl()));
						startActivity(i);
					} else{
						Intent i = new Intent(DashboardActivity.this, RSSActivity.class);
						i.putExtra("author", myNeitherList.get(arg2).getAuthor());
						i.putExtra("blog", myNeitherList.get(arg2).getBlogTitle());
						i.putExtra("title", myNeitherList.get(arg2).getTitle());
						i.putExtra("category", myNeitherList.get(arg2).getCategory());
						i.putExtra("content", myNeitherList.get(arg2).getContent());
						i.putExtra("url", myNeitherList.get(arg2).getUrl());
					 	startActivity(i);
					}
				}

			//	web.setVisibility(0);
			//	 findViewById(R.id.load2).setVisibility(8);
			}
		});
		
		mListView.setOnItemLongClickListener(new OnItemLongClickListener(){
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
			
			 {
				if (settings.getVibrateEnabled()) {
					myVib.vibrate(50);
				}
				// Toast.makeText(DashboardActivity.this, "Clicked on "
				// +temp.getText(), 3).show();
				Intent i = new Intent(Intent.ACTION_VIEW);
				if (isBloggers)
					i.setData(Uri.parse(myBloggersList.get(arg2).getUrl()));
				if (isPodcasts)
					i.setData(Uri.parse(myPodcastsList.get(arg2).getUrl()));
				if (isVideos)
					i.setData(Uri.parse(myVideosList.get(arg2).getUrl()));
				if (isNeither)
					i.setData(Uri.parse(myNeitherList.get(arg2).getUrl()));
				startActivity(i);
				return false;
			 }
			
		});
		
		feat1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (settings.getVibrateEnabled()) {
					myVib.vibrate(50);
				}
				Intent i = new Intent(DashboardActivity.this,
						CategoryListActivity.class);
				i.putExtra("category", "News");
				i.putExtra("nextCategory", "Community");
				i.putExtra("categoryNumber", 0);
				startActivity(i);
			}
		});
		feat2.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (settings.getVibrateEnabled()) {
					myVib.vibrate(50);
				}
				Intent i = new Intent(DashboardActivity.this,
						CategoryListActivity.class);
				i.putExtra("category", "Community");
				i.putExtra("nextCategory", "Chronicles");
				i.putExtra("categoryNumber", 1);
				startActivity(i);
			}
		});
		feat3.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (settings.getVibrateEnabled()) {
					myVib.vibrate(50);
				}
				Intent i = new Intent(DashboardActivity.this,
						CategoryListActivity.class);
				i.putExtra("category", "Chronicles");
				i.putExtra("nextCategory", "Projects");
				i.putExtra("categoryNumber", 2);
				startActivity(i);
			}
		});
		feat4.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (settings.getVibrateEnabled()) {
					myVib.vibrate(50);
				}
				Intent i = new Intent(DashboardActivity.this,
						CategoryListActivity.class);
				i.putExtra("category", "Projects");
				i.putExtra("nextCategory", "Resources");
				i.putExtra("categoryNumber", 3);
				startActivity(i);
			}
		});
		feat5.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (settings.getVibrateEnabled()) {
					myVib.vibrate(50);
				}
				Intent i = new Intent(DashboardActivity.this,
						CategoryListActivity.class);
				i.putExtra("category", "Resources");
				i.putExtra("nextCategory", "Testing Planet");
				i.putExtra("categoryNumber", 4);
				startActivity(i);
			}
		});
		feat6.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (settings.getVibrateEnabled()) {
					myVib.vibrate(50);
				}
				Intent i = new Intent(DashboardActivity.this,
						CategoryListActivity.class);
				i.putExtra("category", "Forum");
				i.putExtra("nextCategory", " ");
				i.putExtra("categoryNumber", 6);
				startActivity(i);
			}
		});
		feat7.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (settings.getVibrateEnabled()) {
					myVib.vibrate(50);
				}
				Intent i = new Intent(DashboardActivity.this,
						CategoryListActivity.class);
				i.putExtra("category", "Testing Planet");
				i.putExtra("nextCategory", "Forum");
				i.putExtra("categoryNumber", 5);
				startActivity(i);
			}
		});
		
		bloggers.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (settings.getVibrateEnabled()) {
					myVib.vibrate(50);
				}
				try {
					if (slider.isOpened()){
						slider.animateClose();
					} else {
						if (!isBloggers){
							myBloggersList = new ArrayList<RSSData>();
							getFeed(BLOGGERS);
							mListView.setAdapter(new MyListAdapter(DashboardActivity.this, R.layout.rssrow,myBloggersList));
						}
						slider.animateOpen();
					}	
				} catch (ClientProtocolException e) {
					toast.show();
					e.printStackTrace();
				} catch (IOException e) {
					toast.show();
					e.printStackTrace();
				}
			}
		});
		podcasts.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (settings.getVibrateEnabled()) {
					myVib.vibrate(50);
				}
				try {
					if (slider.isOpened()){
						slider.animateClose();
					} else {
						if (!isPodcasts){
							myPodcastsList = new ArrayList<RSSData>();
							getFeed(PODCASTS);
							mListView.setAdapter(new MyListAdapter(DashboardActivity.this, R.layout.rssrow,myPodcastsList));
						}
						slider.animateOpen();
					}
				} catch (ClientProtocolException e) {
					toast.show();
					e.printStackTrace();
				} catch (IOException e) {
					toast.show();
					e.printStackTrace();
				}
			}
		});
		videos.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (settings.getVibrateEnabled()) {
					myVib.vibrate(50);
				}
				try {
					if (slider.isOpened()){
						slider.animateClose();
					} else {
						if (!isVideos){
							myVideosList = new ArrayList<RSSData>();
							getVideos(VIDEOS);
							mListView.setAdapter(new MyListAdapter(DashboardActivity.this, R.layout.rssrow,myVideosList));
						}
						slider.animateOpen();
					}
				} catch (ClientProtocolException e) {
					toast.show();
					e.printStackTrace();
				} catch (IOException e) {
					toast.show();
					e.printStackTrace();
				}
			}
		});

	}
}
