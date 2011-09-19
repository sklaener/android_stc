/* 
Software Testing Club Android application
Stefan Klaener
sklaener@gmail.com
 */

package com.echo.stc;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.ByteArrayBuffer;

import com.echo.stc.PullToRefreshListView.OnRefreshListener;
import com.echo.stc.ArticleActivity.LearnGestureListener;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.gesture.GestureOverlayView.OnGestureListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.os.*;
import android.text.BoringLayout.Metrics;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.*;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.RelativeLayout.LayoutParams;

public class CategoryListActivity extends Activity  {
	/** Called when the activity is first created. */
	private PullToRefreshListView listView1;
	// private ProgressBar spinner;
	private ArrayList<Article> articles = new ArrayList<Article>();
	private ArrayList<Article> articlesSaved = new ArrayList<Article>();
	private ArrayList<String> categories = new ArrayList<String>();
	ArticleAdapter adapt;
	int pageNumber = 1;
	Toast toast = null;
	AsyncTask<Void, String, Void> task, task3, task4;
	MyAsyncTask task2;
	String category;
	String nextCategory;
	int categoryNumber;
	private Vibrator myVib;
	static ArrayList<Article> storiesStatic = new ArrayList<Article>();
	ApplicationSettings settings;
	TextView cat;
	TextView nextCat;
	static boolean getMeHome = false;
	boolean isHome = false;
	static boolean isRefreshing = false;
	// ArrayList<Feature> features = new ArrayList<Feature>();
    ImageAdapter adapter;
	private ArrayList<ImageView> selectorDots = new ArrayList<ImageView>();
	Timer timer;
	// Widget_Gallery g;
	// static boolean galleryIsPressed = false;
	DisplayMetrics metrics = new DisplayMetrics();
	String txtFile = "";
	String backupFile = "";
	private static final int SWIPE_MIN_DISTANCE = 120;
	private static final int SWIPE_MAX_OFF_PATH = 250;
	private static final int SWIPE_THRESHOLD_VELOCITY = 200;
	private static final int CAT_NEWS = 0;
	private static final int CAT_COMMUNITY = 1;
	private static final int CAT_CHRONICLES = 2;
	private static final int CAT_PROJECTS = 3;
	private static final int CAT_RESOURCES = 4;
	private static final int CAT_PLANET = 5;
	private static final int CAT_FORUM = 6;
	static Animation enterLeftToRight, exitLeftToRight, enterRightToLeft,
			exitRightToLeft;
	protected MyGestureListener myGestureListener;
	boolean isMainShowing, isAnimating = false, toLeft = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		settings = (ApplicationSettings) this.getApplication();
		setTheme(android.R.style.Theme_Light_NoTitleBar);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		this.getWindow().setFormat(PixelFormat.TRANSPARENT);
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		myVib = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);
		Intent i = this.getIntent();
		category = i.getStringExtra("category").toString();
		nextCategory = i.getStringExtra("nextCategory").toString();
		categoryNumber = i.getIntExtra("categoryNumber",0);
//		pageNumber = 1;
//		category = "News";
//		nextCategory = "Community";
//		categoryNumber = 0;
		selectorDots.add((ImageView) findViewById(R.id.imageView9));
		selectorDots.add((ImageView) findViewById(R.id.imageView8));
		selectorDots.add((ImageView) findViewById(R.id.imageView7));
		selectorDots.add((ImageView) findViewById(R.id.imageView6));
		selectorDots.add((ImageView) findViewById(R.id.imageView5));
		selectorDots.add((ImageView) findViewById(R.id.imageView10));
		selectorDots.add((ImageView) findViewById(R.id.imageView11));
		selectorDots.get(categoryNumber).setImageResource(R.drawable.featured_selected);
		
		categories.add("Testing Planet");
		categories.add("Forum");
		categories.add("Resources");
		categories.add("Projects");
		categories.add("Chronicles");
		categories.add("Community");
		categories.add("News");
		isAnimating = false;

		enterLeftToRight = AnimationUtils.loadAnimation(this,
				R.anim.enterlefttoright);
		exitLeftToRight = AnimationUtils
				.loadAnimation(this, R.anim.lefttoright);
		enterRightToLeft = AnimationUtils.loadAnimation(this,
				R.anim.enterrighttoleft);
		exitRightToLeft = AnimationUtils
				.loadAnimation(this, R.anim.righttoleft);
		
		pageNumber = 1;
		//deleteArticles();
		articles.clear(); // FOR DEBUGGING
		setArticles("start");
		toast = Toast.makeText(this, "Unable to connect", 5);

		isHome = i.getBooleanExtra("start", false);
		
		cat = (TextView) findViewById(R.id.category);
		cat.setText(category);
		nextCat = (TextView) findViewById(R.id.nextCategory);
		nextCat.setText(nextCategory);
//		getThumbnails();
		refreshArticles(1, "start");




		myGestureListener = new MyGestureListener(this);

		// setting listener. 
		((PullToRefreshListView) findViewById(R.id.listView))
				.setOnTouchListener(myGestureListener);

		listView1 = (PullToRefreshListView) findViewById(R.id.listView);
		adapt = new ArticleAdapter(this, R.layout.row, articles);

	
		listView1.setAdapter(adapt);

		listView1.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				if (!isAnimating){
					if (settings.getVibrateEnabled()) {
						myVib.vibrate(50);
					}
	
					arg2 = arg2 - 1;
					if (articles.get(arg2).getCentreText().equals("")) {
						Intent i = new Intent(CategoryListActivity.this,
								ArticleActivity.class);
						i.putExtra("storyNumber", arg2);
						storiesStatic = articles;
						i.putExtra("storyCount", storiesStatic.size() - 1);
						startActivity(i);
					} else {
						articles.get(arg2).setCentreText("Loading more ...");
						handler.sendEmptyMessage(0);
						refreshArticles(pageNumber, "old");
					}
				}
			}
		});

		listView1.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				refreshArticles(1, "new");
			}

		});
		//refreshArticles(1, "new");

		ImageView logo = (ImageView) findViewById(R.id.imageView2);
		logo.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (settings.getVibrateEnabled()) {
					myVib.vibrate(50);
				}
			//	Intent i2 = new Intent(CategoryListActivity.this, DashboardActivity.class);
			//	startActivity(i2);
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
			//	Intent i2 = new Intent(CategoryListActivity.this, DashboardActivity.class);
			//	startActivity(i2);
				finish();
			}
		});
		ImageButton refresh = (ImageButton) findViewById(R.id.refresh);
		refresh.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (settings.getVibrateEnabled()) {
					myVib.vibrate(50);
				}
			//	Intent i2 = new Intent(CategoryListActivity.this, DashboardActivity.class);
			//	startActivity(i2);
				deleteArticles();
				refreshArticles(1, "new");
			}
		});

		ImageButton prefs = (ImageButton) findViewById(R.id.settings);
		prefs.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (settings.getVibrateEnabled()) {
					myVib.vibrate(50);
				}
				Intent i2 = new Intent(CategoryListActivity.this, PostSettingsActivity.class);
				startActivity(i2);
			}
		});
		
		AnimationListener al = new AnimationListener() {
			@Override
			public void onAnimationEnd(Animation arg0) {
				isAnimating = false;
				//deleteArticles();
				adapt.clear();
				listView1.setVisibility(View.GONE);
				
				int i = adapt.getItemCount();
				while (i < 0){
					adapt.remove(adapt.getItem(0));
				}
				//deleteArticles();
				refreshArticles(1, "start");
				listView1.setVisibility(View.VISIBLE);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}

			@Override
			public void onAnimationStart(Animation animation) {
			//listView1.clearChoices();
			//deleteStories();

			}

		};

		exitLeftToRight.setAnimationListener(al);
		exitRightToLeft.setAnimationListener(al);

	}

	@Override
	public void onResume() {
		super.onResume();
		if (getMeHome) {
			if (category != null && !category.equals("home") && isHome == false) {
				finish();
			} else {
				getMeHome = false;
			}
		}
		articles.clear(); // FOR DEBUGGING
		deleteArticles();
		setArticles("start");
		refreshArticles(1, "start");
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		settings = (ApplicationSettings) this.getApplication();
		switch (requestCode) {
		case (1): {
			boolean changed = data.getBooleanExtra("changed", false);

			if (changed) {

				task = new AsyncTask<Void, String, Void>() {

					@Override
					protected Void doInBackground(Void... params) {
						getThumbnails();
						return null;
					}

				};
				task.execute(null);
				PullToRefreshListView listView1 = (PullToRefreshListView) findViewById(R.id.listView);
				listView1.onRefreshComplete(true);

			}
		}
			break;
		}
	}

	private void refreshArticles(final int pageNumber2, final String newOrOld) {
		task = new AsyncTask<Void, String, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				
				try {
					if (!isRefreshing) {
						isRefreshing = true;
						//getThumbnails();
						if (newOrOld.equals("new")||newOrOld.equals("old")){
							getHtml(pageNumber2);
							if (pageNumber2 == pageNumber) {
								pageNumber++;
							}
						}
						setArticles(newOrOld);
						getThumbnails();
						Message m = new Message();
						m.arg1 = 1;
						handler.sendMessageDelayed(m, 10);
						isRefreshing = false;
					}

					return null;
				} catch (ClientProtocolException e) {
					isRefreshing = false;
					toast.show();
					e.printStackTrace();
					return null;
				} catch (IOException e) {
					isRefreshing = false;
					toast.show();
					e.printStackTrace();
					return null;
				}
			}

			@Override
			protected void onPostExecute(Void result) {
				super.onPostExecute(result);
				if (newOrOld.equals("old")) {
					listView1.onRefreshComplete(false);
				} else {

				}
				handler.sendEmptyMessage(0);

			}
		};
		task.execute(null);
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, R.id.news, 0, "News");
		menu.getItem(0).setIcon(R.drawable.news); // TODO icons
		menu.add(1, R.id.community, 1, "Community");
		menu.getItem(1).setIcon(R.drawable.community);
		menu.add(2, R.id.chronicles, 2, "Chronicles");
		menu.getItem(2).setIcon(R.drawable.chronicles);
		menu.add(3, R.id.projects, 3, "Projects");
		menu.getItem(3).setIcon(R.drawable.projects);
		menu.add(4, R.id.resources, 4, "Resources");
		menu.getItem(4).setIcon(R.drawable.resources);
		menu.add(5, R.id.planet, 5, "Testing Planet");
		menu.getItem(5).setIcon(android.R.drawable.ic_menu_preferences);
		menu.add(6, R.id.forum, 6, "Forum");
		menu.getItem(6).setIcon(android.R.drawable.ic_menu_preferences);
		menu.add(7, R.id.clear, 7, "Clear");
		menu.getItem(7).setIcon(android.R.drawable.ic_menu_delete);
		return true;
	}
	
	public void setCategoryOnMenuSelection(int chosenCategory, int catNumber){
		switch (chosenCategory) {
		case CAT_NEWS:
			category = "News";					
			nextCategory ="Community";
			break;
		case CAT_COMMUNITY:
			category = "Community";
			nextCategory ="Chronicles";
			break;
		case CAT_CHRONICLES:
			category = "Chronicles";
			nextCategory ="Projects";
			break;
		case CAT_PROJECTS:
			category = "Projects";
			nextCategory ="Resources";
			break;
		case CAT_RESOURCES:
			category = "Resources";
			nextCategory ="Testing Planet";
			break;
		case CAT_PLANET:
			category = "Testing Planet";
			nextCategory ="Forum";
			break;
		case CAT_FORUM:
			category = "Forum";
			nextCategory =" ";
			break;
		}
		cat.setText(category);
		nextCat.setText(nextCategory);
		selectorDots.get(categoryNumber).setImageResource(R.drawable.featured_unselected);
		categoryNumber=chosenCategory;
		selectorDots.get(categoryNumber).setImageResource(R.drawable.featured_selected);
		pageNumber=1;
		if (chosenCategory > catNumber){ // Animation "next"
			Message m = new Message();
			m.arg1 = 4;
			m.arg2 = 0;
			handler.sendMessage(m);
		} else  { // Animation "previous"
			Message m = new Message();
			m.arg1 = 4;
			m.arg2 = 	1;
			handler.sendMessage(m);
		}		
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		if (settings.getVibrateEnabled()) {
			myVib.vibrate(50);
		}
		switch (item.getItemId()) {
		case R.id.news:
			setCategoryOnMenuSelection(CAT_NEWS,categoryNumber);
			return true;
		case R.id.community:
			setCategoryOnMenuSelection(CAT_COMMUNITY,categoryNumber);
			return true;
		case R.id.chronicles:
			setCategoryOnMenuSelection(CAT_CHRONICLES,categoryNumber);
			return true;
		case R.id.projects:
			setCategoryOnMenuSelection(CAT_PROJECTS,categoryNumber);
			return true;
		case R.id.resources:
			setCategoryOnMenuSelection(CAT_RESOURCES,categoryNumber);
			return true;
		case R.id.planet:
			setCategoryOnMenuSelection(CAT_PLANET,categoryNumber);
			return true;
		case R.id.forum:
			setCategoryOnMenuSelection(CAT_FORUM,categoryNumber);
			return true;
		case R.id.clear:
			deleteArticles();
			//refreshArticles(1, "new");
			return true;
		}
		return false;

	}

	@SuppressWarnings("unused")
	public void getThumbnails() {
		try {
			int count = 0;

			for (Article story : articles) {
				if (story.getURL() != "" && story.getImageBitmap() == null) {
					Boolean done1 = (new File(
							Environment.getExternalStorageDirectory()
									+ "/Android/")).mkdir();
					Boolean done2 = (new File(
							Environment.getExternalStorageDirectory()
									+ "/Android/data/")).mkdir();
					Boolean done3 = (new File(
							Environment.getExternalStorageDirectory()
									+ "/Android/data/com.echo.stc/")).mkdir();
					try {
						Boolean done = (new File(
								Environment.getExternalStorageDirectory()
										+ "/Android/data/com.echo.stc/files"))
								.mkdir();
						File file = Environment.getExternalStorageDirectory();
						File f = new File(file,
								"Android/data/com.echo.stc/files/.nomedia");
						f.createNewFile();

						try {
							file = new File(file,
									"Android/data/com.echo.stc/files/"
											+ story.getURL().split("/")[5]
											+ ".png");
						} catch (ArrayIndexOutOfBoundsException e) {
							file = new File(file,
									"Android/data/com.echo.stc/files/"
											+ story.getURL().split("/")[4]
											+ ".png");
						}
						if (!file.exists()) { // if the thumbnail does not
												// exist, go and download it
							URL url = new URL(story.getImageURL());
							URLConnection ucon = url.openConnection();
							InputStream is = ucon.getInputStream();
							BufferedInputStream bis = new BufferedInputStream(
									is);
							ByteArrayBuffer baf = new ByteArrayBuffer(50);

							int current = 0;
							while ((current = bis.read()) != -1) {
								baf.append((byte) current);
							}

							FileOutputStream fos = new FileOutputStream(file);
							fos.write(baf.toByteArray());
							fos.close();

							float scale;
							double scale2;

							DisplayMetrics metrics = new DisplayMetrics();
							getWindowManager().getDefaultDisplay().getMetrics(
									metrics);
							
							if (metrics.densityDpi == DisplayMetrics.DENSITY_MEDIUM) {
								scale2 = 10;
							} else if (metrics.densityDpi == DisplayMetrics.DENSITY_HIGH) {
								scale2 = 20;
							} else {
								scale2 = 0;
							}

							Bitmap original = BitmapFactory
									.decodeFile("/sdcard/Android/data/com.echo.stc/files/"
											+ story.getURL().split("/")[5]
											+ ".png");
							float height = original.getHeight();
							float width = original.getWidth();
							if (original.getWidth() <= original.getHeight()) {
								scale = (float) ((48.0 + scale2) / width);
							} else {
								scale = (float) ((48.0 + scale2) / height);
							}

							int width2 = (int) (original.getWidth() * scale);
							int height2 = (int) (original.getHeight() * scale);

							Bitmap resized = Bitmap.createScaledBitmap(
									original, width2, height2, true);
							FileOutputStream out = new FileOutputStream(file);
							resized.compress(Bitmap.CompressFormat.PNG, 90, out);

						}

						task2 = new MyAsyncTask(count);
						task2.execute(null);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				count++;
			}
		} catch (RuntimeException e) {
			e.printStackTrace();
		}
	}

	public void setArticles(String newOrOld) {
		try {
			FileInputStream actualfile;
			String backupFile = "";
			String txtFile = "";
			if (newOrOld.equals("start")) {
				switch (categoryNumber) {
				case 1:
					backupFile = "backupNews.txt";
					break;
				case 2:
					backupFile = "backupCommunity.txt";
					break;
				case 3:
					backupFile = "backupChronicles.txt";
					break;
				case 4:
					backupFile = "backupProjects.txt";
					break;
				case 5:
					backupFile = "backupResources.txt";
					break;
				case 6:
					backupFile = "backupPlanet.txt";
					break;
				case 7:
					backupFile = "backupForum.txt";
					break;
				default:
					backupFile = "backupNews.txt";
					break;
				}
				actualfile = openFileInput(backupFile);
			} else {
				switch (categoryNumber) {
				case 1:
					txtFile = "news.txt";
					break;
				case 2:
					txtFile = "community.txt";
					break;
				case 3:
					txtFile = "chronicles.txt";
					break;
				case 4:
					txtFile = "projects.txt";
					break;
				case 5:
					txtFile = "resources.txt";
					break;
				case 6:
					txtFile = "planet.txt";
					break;
				case 7:
					txtFile = "forum.txt";
					break;
				default:
					txtFile = "news.txt";
					break;
				}
				actualfile = openFileInput(txtFile);
			}
			InputStreamReader sr = new InputStreamReader(actualfile);
			BufferedReader fr = new BufferedReader(sr);
			String line = "";
			String title = "";
			String url = "";
			String author = "";
			String imageUrl = "";
			String cat = "";

			int count = 0;
			boolean color = false;
			while ((line = fr.readLine()) != null) {

				boolean dup = false;
				title = Parse.toString(line);
				url = fr.readLine();
				author = fr.readLine();
				imageUrl = fr.readLine();
				cat = fr.readLine();

				for (Article story : articles) { // check to see if the story already exists
					if (story.getTitle().equals(title)) {
						dup = true;
					}
				}
				for (Article story : articlesSaved) { // check to see if the story already exists
					if (story.getTitle().equals(title)) {
						dup = true;
					}
				}

				if (dup == false) { // if it's a new story
					Article story1 = new Article();
					story1.setTitle(title);
					story1.setURL(url);
					story1.setImageURL(imageUrl);
					story1.setAuthor(author);
					story1.setImage(R.drawable.icon);
					story1.setBackground(R.drawable.shape);
					story1.setCategory(cat);

					if (color) {
						story1.setColour(R.drawable.back1);
						color = false;
					} else {
						story1.setColour(R.drawable.back2);
						color = true;
					}
					if (story1.getTitle() != null){
						if (newOrOld.equals("new") || newOrOld.equals("start")) { 
							articlesSaved.add(count, story1);
							count++;
						} else {
							articlesSaved.add(articlesSaved.size(), story1);
						}
					}
				}

			}
			actualfile.close();

		} catch (IOException e) {
			Log.e("STC",
					"Stories file does not exist or cannot be read: "
							+ e.toString());
		}
		filterArticles();
	}

	public void getHtml(int pageNumber) throws ClientProtocolException,IOException {
		String site = "";
		boolean isForum = false;
		boolean isPlanet = false;
		String cats = this.category;
		if (cats.equals("Forum")) {
			//site = "http://www.softwaretestingclub.com/forum/categories/751045:Category:5684/listForCategory";
			site = "http://www.softwaretestingclub.com/forum/categories/751045:Category:5684/listForCategory?categoryId=751045%3ACategory%3A5684&page="+pageNumber;
			isForum = true;
		} else if (cats.equals("Testing Planet")) {
			site = "http://www.thetestingplanet.com/page/"+pageNumber;
			isPlanet = true;
		}  else if ((cats != null && !cats.equals("home")) && pageNumber == 1) {
			site = "http://blog.softwaretestingclub.com/category/" + cats;
		} else if (cats != null && !cats.equals("home")) {
			site = "http://blog.softwaretestingclub.com/category/" + cats	+ "/page/" + pageNumber;
		}

		BufferedReader reader = new BufferedReader(new InputStreamReader(
				new URL(site + "?wpmp_switcher=desktop").openStream()));
		String result = "";

		try {
			switch (categoryNumber) {
			case 1:
				txtFile = "news.txt";
				backupFile = "backupNews.txt";
				break;
			case 2:
				txtFile = "community.txt";
				backupFile = "backupCommunity.txt";
				break;
			case 3:
				txtFile = "chronicles.txt";
				backupFile = "backupChronicles.txt";
				break;
			case 4:
				txtFile = "projects.txt";
				backupFile = "backupProjects.txt";
				break;
			case 5:
				txtFile = "resources.txt";
				backupFile = "backupResources.txt";
				break;
			case 6:
				txtFile = "planet.txt";
				backupFile = "backupPlanet.txt";
				break;
			case 7:
				txtFile = "forum.txt";
				backupFile = "backupForum.txt";
				break;
			default:
				txtFile = "news.txt";
				backupFile = "backupNews.txt";
				break;
			}
			FileOutputStream file = openFileOutput(txtFile, 0);
			FileOutputStream fileBackup = openFileOutput(backupFile, 0);
			String line = "";

			while (true) {

				line = reader.readLine();
				if (line == null) {
					break;
				}

				if (line.contains("<div class=\"post-thumb") || line.contains("class=\"xg_lightborder\">") || line.contains("post type-post")) {
					String title = "", url = "", imageUrl = "";
					String author = "";

					while (true) {
						line = reader.readLine();
						if (line == null) {
							break;
						}
						if (isPlanet){
							if (line.contains("<h2 class=\"title\">")){
								imageUrl = "http://blog.softwaretestingclub.com/wp-content/uploads/ttp-icon.png";
								if (line.contains("<a href=\"")) { // getting url
									url = line.substring(line.indexOf("http://"));
									url = url.substring(0,url.indexOf("\" rel=\"bookmark"));
								}
								if (line.contains("title=\"")) { // getting title
									title = line.substring(line.indexOf("title=\"") + 7);
									title = title.substring(0, title.indexOf("\">"));
								}
								if (line.contains("</div>")) {
									result += title + "\n" + url + "\n" + " " + "\n" + imageUrl + "\n" + cats	+ "\n";
									break;
								}			
							}
						} else if (isForum) {
							if (line.contains("src=\"")) {
								author = line.substring(line.indexOf("title=\"")+7);
								author = author.substring(0, author.indexOf("\""));
								imageUrl = line.substring(line
										.indexOf("src=\"") + 5);
								imageUrl = imageUrl.substring(0,
										imageUrl.indexOf("\" height"));

								url = line.substring(line
										.indexOf("a href=\"http://"));
								url = url.substring(8, url.indexOf("\" _snid"));

								title = line.substring(line.indexOf("<h3>")+5);
								title = title.substring(title.indexOf("\">")+2);
								title = title.substring(0,title.indexOf("</a>"));
							}
							if (line.contains("</p")) {
								if (!(title == "") || !(title==null) && !title.equalsIgnoreCase("")) {
									result += title + "\n" + url + "\n" + author+ "\n" + imageUrl + "\n" + "Forum"+ "\n";
								}
								break;
							}
						} else {
							if (line.contains("<a href=\"")) { // getting url
								url = line.substring(line.indexOf("http://"));
								url = url.substring(0,
										url.indexOf("\" rel=\"bookmark"));
							}
							if (line.contains("title=\"Permanent Link to ")) { // getting title
								title = line
										.substring(line.indexOf("Link to") + 8);
								title = title
										.substring(0, title.indexOf("\">"));

							}
							if (line.contains("src=\"")) { // getting imageURL  for thumbnail
								imageUrl = line.substring(line
										.indexOf("src=\"") + 5);
								imageUrl = imageUrl.substring(0,
										imageUrl.indexOf("\" class"));
							}
							if (line.contains("</div>")) {
								result += title + "\n" + url + "\n" + " "
										+ "\n" + imageUrl + "\n" + cats + "\n";
								break;
							}
						}
					}
				}
			}
			file.write((result).getBytes());
			file.close();

			if (pageNumber == 1) {
				fileBackup.write((result).getBytes());
				fileBackup.flush();
				fileBackup.close();
			}
		} catch (IOException e) {
			Log.e("STC", "Could not save site HTML: " + e.toString());
		}
	}

	// }

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.arg1 == 1) {
				listView1.onRefreshComplete(true);
			} else if (msg.arg1 == 2) {
				adapter.notifyDataSetChanged();
			} else if (msg.arg1 == 4) {
				if (msg.arg2 == 0) {
					toLeft = true;
					//deleteStories();
				} else if (msg.arg2 == 1) {
					toLeft = false;
					//deleteStories();
				}
				if (toLeft) {
					listView1.startAnimation(exitRightToLeft);
				} else {
					listView1.startAnimation(exitLeftToRight);
				}
			} else {
				adapt.notifyDataSetChanged();
			}

		}
	};

	public void filterArticles() {
		ArrayList<Article> newArticle = new ArrayList<Article>();
		boolean pass = false;
		for (Article article : articlesSaved) {
				if (article.getCategory().equalsIgnoreCase(category)){
					pass = true;
				} else {
					pass = false;
				}
				if (pass) {
					newArticle.add(article);
				}
		}
		articles.clear();
		for (Article story : newArticle) {

				articles.add(story);
			
		}

		if (articles.size() == 0
				|| !articles.get(articles.size() - 1).getCentreText()
						.equals("Load more ...")) {
			Article lastStory = new Article();
			lastStory.setCentreText("Load more ...");
			lastStory.setImage(R.drawable.blank);
			lastStory.setBackground(0);
			articles.add(lastStory);
			handler.sendEmptyMessage(0);

		}
		handler.sendEmptyMessage(0);
	}

	public void deleteArticles() {
		try {
			int temp = articlesSaved.size();
			while (temp > 0){
				articlesSaved.remove(0);
				temp--;
			}
		} catch (Exception e) {
			Log.e("STC_DELETE_ART","Problem deleting saved articles: "	+ e.toString());
		}
	}

	private class MyAsyncTask extends AsyncTask<Void, Void, Void> {
		int storyIndex;

		public MyAsyncTask(int storyIndex) {
			super();
			this.storyIndex = storyIndex;
		}

		@Override
		protected Void doInBackground(Void... params) {
			Article story = articles.get(storyIndex);
			if (story != null) {
				try {
					story.setBitmap(BitmapFactory.decodeFile("/sdcard/Android/data/com.echo.stc/files/"	+ story.getURL().split("/")[5] + ".png"));
					articles.set(storyIndex, story);
					handler.sendEmptyMessage(0);
				} catch (ArrayIndexOutOfBoundsException e) {
					Log.e("STC", e.toString());
				}
			}

			return null;
		}
	}

	private void nextCategory() {
		if (!category.equals("Forum")) {
			if (category.equals("News")) {
				category = "Community";
				nextCategory = "Chronicles";
			} else if (category.equals("Community")) {
				category = "Chronicles";
				nextCategory = "Projects";
			} else if (category.equals("Chronicles")) {
				category = "Projects";
				nextCategory = "Ressources";
			} else if (category.equals("Projects")) {
				category = "Resources";
				nextCategory = "Testing Planet";
			} else if (category.equals("Resources")) {
				category = "Testing Planet";
				nextCategory = "Forum";
			}	else if (category.equals("Testing Planet")) {
				category = "Forum";
				nextCategory = " ";
			}
			categoryNumber += 1;
			cat.setText(category);
			nextCat.setText(nextCategory);
			selectorDots.get(categoryNumber - 1).setImageResource(
					R.drawable.featured_unselected);
			selectorDots.get(categoryNumber).setImageResource(
					R.drawable.featured_selected);
			pageNumber=1;
		
			Message m = new Message();
			m.arg1 = 4;
			m.arg2 = 0;
			handler.sendMessage(m);
		}
	}

	private void previousCategory() {
		if (!category.equals("News")) {
			nextCategory = category;
			if (category.equals("Community")) {
				category = "News";
			} else if (category.equals("Chronicles")) {
				category = "Community";
			} else if (category.equals("Projects")) {
				category = "Chronicles";
			} else if (category.equals("Resources")) {
				category = "Projects";
			} else if (category.equals("Testing Planet")) {
				category = "Resources";
			} else if (category.equals("Forum")) {
				category = "Testing Planet";
			} 
			cat.setText(category);
			nextCat.setText(nextCategory);
			categoryNumber -= 1;
			selectorDots.get(categoryNumber + 1).setImageResource(
					R.drawable.featured_unselected);
			selectorDots.get(categoryNumber).setImageResource(
					R.drawable.featured_selected);
			pageNumber=1;
			Message m = new Message();
			m.arg1 = 4;
			m.arg2 = 1;
			handler.sendMessage(m);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// or implement in activity or component. When your not assigning to a
		// child component.
		return myGestureListener.getDetector().onTouchEvent(event);
	}

	class MyGestureListener extends SimpleOnGestureListener implements
			OnTouchListener {
		Context context;
		GestureDetector gDetector;

		public MyGestureListener() {
			super();
		}

		public MyGestureListener(Context context) {
			this(context, null);
		}

		public MyGestureListener(Context context, GestureDetector gDetector) {

			if (gDetector == null)
				gDetector = new GestureDetector(context, this);

			this.context = context;
			this.gDetector = gDetector;
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {

			try {
				if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
					return false;
				// right to left swipe
				if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE
						&& Math.abs(velocityX) > (SWIPE_THRESHOLD_VELOCITY/2)) {
					isAnimating = true;
					if (settings.getVibrateEnabled()) {
						myVib.vibrate(50);
					}

					nextCategory();
				} else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
						&& Math.abs(velocityX) > (SWIPE_THRESHOLD_VELOCITY/2)) {
					isAnimating = true;
					if (settings.getVibrateEnabled()) {
						myVib.vibrate(50);
					}
					previousCategory();
				}
			} catch (Exception e) {
				// nothing// nothing
			
			}
			return super.onFling(e1, e2, velocityX, velocityY);
		}

		@Override
		public boolean onSingleTapConfirmed(MotionEvent e) {

			return super.onSingleTapConfirmed(e);
		}

		public boolean onTouch(View v, MotionEvent event) {

			// Within the MyGestureListener class you can now manage the
			// event.getAction() codes.

			// Note that we are now calling the gesture Detectors onTouchEvent.
			// And given we've set this class as the GestureDetectors listener
			// the onFling, onSingleTap etc methods will be executed.
			return gDetector.onTouchEvent(event);
		}

		public GestureDetector getDetector() {
			return gDetector;
		}
	}
}