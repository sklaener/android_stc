/* 
Software Testing Club Android application
Stefan Klaener
sklaener@gmail.com
*/

package com.echo.stc;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.SlidingDrawer;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;
import com.echo.stc.CustomHttpClient;

import java.util.ArrayList; 
import java.util.List; 
import org.apache.http.HttpEntity; 
import org.apache.http.HttpResponse; 
import org.apache.http.NameValuePair; 
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity; 
import org.apache.http.client.methods.HttpGet; 
import org.apache.http.client.methods.HttpPost; 
import org.apache.http.cookie.Cookie; 
import org.apache.http.impl.client.DefaultHttpClient; 
import org.apache.http.message.BasicNameValuePair; 
import org.apache.http.protocol.HTTP; 
import org.apache.http.util.EntityUtils;


public class PostSettingsActivity extends Activity {
	Spinner s, SliderDefault;
	ArrayList<CheckBox> checks;
	CheckBox haptic;
	ApplicationSettings settings;
	EditText user, password;
	String usr, pwd;
	private Vibrator myVib;
	//public static final String PREFS_NAME = "STC";
    EditText un,pw;
	TextView error;
    Button ok;
	//SharedPreferences sharedsettings = getSharedPreferences("login",MODE_PRIVATE);

	
	@Override
	public void onCreate(Bundle savedInstanceState){
		Intent resultIntent = new Intent();
		resultIntent.putExtra("changed", true);
		setResult(Activity.RESULT_OK, resultIntent);
		settings = (ApplicationSettings)this.getApplication();
        myVib = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);
        
    	setTheme(android.R.style.Theme_Light_NoTitleBar);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.postsettings);
        s = (Spinner) findViewById(R.id.spinner1);
        SliderDefault = (Spinner) findViewById(R.id.spinner2);
         
        ArrayAdapter<String> fontSizes = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        fontSizes.add("12");
        fontSizes.add("13");
        fontSizes.add("14");
        fontSizes.add("15 (Default)");
        fontSizes.add("16");
        fontSizes.add("17");
        fontSizes.add("18");
        fontSizes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        final SpinnerAdapter spin = fontSizes;
        
        ArrayAdapter<String> slidingCategories = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        slidingCategories.add("None (default)");
        slidingCategories.add("Bloggers");
        slidingCategories.add("Podcasts");
        slidingCategories.add("Videos");
        slidingCategories.add("Communities");
        slidingCategories.add("Companies");
        slidingCategories.add("Events");
        slidingCategories.add("Jobs");
        slidingCategories.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        final SpinnerAdapter spinSlider = slidingCategories;
        
    /*           
        user = (EditText)findViewById(R.id.usr);
        password = (EditText)findViewById(R.id.pwd);
        ok=(Button)findViewById(R.id.btn_login);
        error=(TextView)findViewById(R.id.tv_error);
    */
        
        haptic = (CheckBox) findViewById(R.id.haptic);
        haptic.setOnCheckedChangeListener(new OnCheckedChangeListener(){

			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
			writeSettingsToFile();
			}
        	
        });
        /*
        ok.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
             	ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
            	postParameters.add(new BasicNameValuePair("signin_email", user.getText().toString()));
            	postParameters.add(new BasicNameValuePair("signin_password", password.getText().toString()));

            	String response = null;
            	try {
            	    response = CustomHttpClient.executeHttpPost("http://www.softwaretestingclub.com/main/authorization/signIn", postParameters);
            	    String res=response.toString();
            	    res= res.replaceAll("\\s+","");
            	    if(res.equals("1"))
            	    	error.setText("Correct username or password");
            	    else
            	    	error.setText("Incorrect mail  or password");
            	} catch (Exception e) {
            		un.setText(e.toString());
            	}

            }
        });*/
    
        s.setAdapter(spin);
        s.setOnItemSelectedListener(new OnItemSelectedListener(){

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				writeSettingsToFile();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
			}
        	
        });
        
        SliderDefault.setAdapter(spinSlider);
        SliderDefault.setOnItemSelectedListener(new OnItemSelectedListener(){

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				writeSettingsToFile();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub	
			}
        	
        });
        
		loadSettingsFromFile();


           
        ImageView logo = (ImageView) findViewById(R.id.imageView2);
        logo.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				if (settings.getVibrateEnabled()){
					myVib.vibrate(50);
				}
		//		Intent i2 = new Intent(PostSettingsActivity.this, DashboardActivity.class);
		//		startActivity(i2);
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
				Intent i2 = new Intent(PostSettingsActivity.this, DashboardActivity.class);
				i2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(i2);
				finish();
			}
		});
		ImageButton about = (ImageButton) findViewById(R.id.about);
		about.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (settings.getVibrateEnabled()) {
					myVib.vibrate(50);
				}
				Intent i2 = new Intent(PostSettingsActivity.this, AboutActivity.class);
				startActivity(i2);
			}
		});
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		writeSettingsToFile();
	}

	public void writeSettingsToFile() {
		ApplicationSettings settings = (ApplicationSettings)this.getApplication();
		int position = s.getSelectedItemPosition();
		if (position == 0){
			settings.setFontSize("12");
		} else if (position == 1){
			settings.setFontSize("13");
		} else if (position == 2){
			settings.setFontSize("14");
		} else if (position == 3){
			settings.setFontSize("15");
		} else if (position == 4){
			settings.setFontSize("16");
		} else if (position == 5){
			settings.setFontSize("17");
		} else if (position == 6){
			settings.setFontSize("18");
		}
		Log.d("STC", "Selected font position: " + position);
		
		int position2 = SliderDefault.getSelectedItemPosition();
		if (position2 == 0){
			settings.setSliderCategory("None (default)");
		} else if (position2 == 1){
			settings.setSliderCategory("Bloggers");
		} else if (position2 == 2){
			settings.setSliderCategory("Podcasts");
		} else if (position2 == 3){
			settings.setSliderCategory("Videos");
		} else if (position2 == 4){
			settings.setSliderCategory("Communities");
		} else if (position2 == 5){
			settings.setSliderCategory("Companies");
		} else if (position2 == 6){
			settings.setSliderCategory("Events");
		} else if (position2 == 7){
			settings.setSliderCategory("Jobs");
		}
		
		Log.d("STC", "Selected font position: " + position);
		
		settings.setVibrateEnabled(haptic.isChecked());
		
		/*
        usr = user.getText().toString();
        pwd = password.getText().toString();
        if (usr.contains("@") && pwd.length()>2){
        	SharedPreferences sharedPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        	SharedPreferences.Editor edit = sharedPrefs.edit();
        	edit.putString("usr",usr);
        	edit.putString("pwd",pwd);
        	edit.commit();
        }*/
		
		Log.d("STC", "Settings written to file");
	}
	
	private void loadSettingsFromFile() {
		ApplicationSettings settings = (ApplicationSettings)this.getApplication();
		int fontSize = settings.getFontSize();
		if (fontSize == 12){
			s.setSelection(0);
		} else if (fontSize == 13){
			s.setSelection(1);
		} else if (fontSize == 14){
			s.setSelection(2);
		} else if (fontSize == 15){
			s.setSelection(3);
		} else if (fontSize == 16){
			s.setSelection(4);
		} else if (fontSize == 17){
			s.setSelection(5);
		} else if (fontSize == 18){
			s.setSelection(6);
		}
		String sliderCategory = settings.getSliderCategory();
		if (sliderCategory.equalsIgnoreCase("None (default)")){
			SliderDefault.setSelection(0);
		} else if (sliderCategory.equalsIgnoreCase("Bloggers")){
			SliderDefault.setSelection(1);
		} else if (sliderCategory.equalsIgnoreCase("Podcasts")){
			SliderDefault.setSelection(2);
		} else if (sliderCategory.equalsIgnoreCase("Videos")){
			SliderDefault.setSelection(3);
		} else if (sliderCategory.equalsIgnoreCase("Communities")){
			SliderDefault.setSelection(4);
		} else if (sliderCategory.equalsIgnoreCase("Companies")){
			SliderDefault.setSelection(5);
		} else if (sliderCategory.equalsIgnoreCase("Events")){
			SliderDefault.setSelection(6);
		} else if (sliderCategory.equalsIgnoreCase("Jobs")){
			SliderDefault.setSelection(7);
		} 
		/*
	   	SharedPreferences sharedPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
		String myUsr = sharedPrefs.getString("usr", null);
		String myPwd = sharedPrefs.getString("pwd", null);
		if (myUsr != null && myPwd!= null && myUsr.contains("@") && myPwd.length()>2){
			user.setText(myUsr);
			password.setText(myPwd);
		}*/
		
		haptic.setChecked(settings.getVibrateEnabled());
 
	}
}

/*
<TextView android:paddingTop="20dp" android:layout_width="wrap_content"
android:id="@+id/textView3" android:text="Login credentials"
android:layout_gravity="center_horizontal" android:layout_height="wrap_content"></TextView>
<RelativeLayout android:layout_width="fill_parent"
android:layout_height="210dip" android:layout_marginTop="10dip" android:background="#ffffff">
<TextView android:id="@+id/tv_un" android:layout_width="wrap_content"
	android:layout_height="wrap_content" android:layout_alignParentLeft="true"
	android:layout_marginRight="9dip" android:layout_marginTop="20dip"
	android:layout_marginLeft="10dip" android:text="Mail:               " />
<EditText android:id="@+id/usr" android:layout_width="150dip"
	android:layout_height="wrap_content" android:background="@android:drawable/editbox_background"
	android:layout_toRightOf="@+id/tv_un" android:layout_alignTop="@+id/tv_un" android:inputType="textEmailAddress"
	android:layout_alignParentRight="true"/>
<TextView android:id="@+id/tv_pw" android:layout_width="wrap_content"
	android:layout_height="wrap_content" android:layout_alignParentLeft="true"
	android:layout_below="@id/tv_un" android:layout_marginRight="9dip"
	android:layout_marginTop="15dip" android:layout_marginLeft="10dip"
	android:text="Password:" />
<EditText android:id="@+id/pwd" android:layout_width="150dip"
	android:layout_height="wrap_content" android:background="@android:drawable/editbox_background"
	android:layout_toRightOf="@id/tv_pw" android:layout_alignTop="@+id/tv_pw"
	android:layout_below="@+id/usr" android:layout_marginLeft="17dip"
	android:password="true" android:layout_alignParentRight="true"/>
<Button android:id="@+id/btn_login" android:layout_width="100dip"
	android:layout_height="wrap_content" android:layout_below="@+id/pwd"
	android:layout_alignParentLeft="true" android:layout_marginTop="15dip"
	android:layout_marginLeft="110dip" android:text="Login" android:layout_alignParentRight="true"/>
<TextView android:id="@+id/tv_error" android:layout_width="fill_parent"
	android:layout_height="40dip" android:textSize="7pt"
	android:layout_alignParentLeft="true" android:layout_below="@+id/btn_login"
	android:layout_marginRight="9dip" android:layout_marginTop="15dip"
	android:layout_marginLeft="15dip" android:textColor="#AA0000"
	android:text="" android:background="#ffffff"/>
</RelativeLayout>
*/