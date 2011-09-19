/* 
Software Testing Club Android application
Stefan Klaener
sklaener@gmail.com
*/

package com.echo.stc;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class AboutActivity extends Activity {
	ApplicationSettings settings;
	private Vibrator myVib;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
    	setTheme(android.R.style.Theme_Light_NoTitleBar);
		super.onCreate(savedInstanceState);
        setContentView(R.layout.about);
        settings = (ApplicationSettings) this.getApplication();
        myVib = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);
		ImageView logo = (ImageView) findViewById(R.id.imageView2);
        logo.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				Intent i2 = new Intent(AboutActivity.this, DashboardActivity.class);
				startActivity(i2);
				finish();
				//startActivity(new Intent(AboutActivity.this,DashboardActivity.class));
			}
        	
        });
    	ImageButton home = (ImageButton) findViewById(R.id.home);
		home.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (settings.getVibrateEnabled()) {
					myVib.vibrate(50);
				}
				Intent i2 = new Intent(AboutActivity.this, DashboardActivity.class);
				i2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(i2);
				finish();
			}
		});
		ImageButton prefs = (ImageButton) findViewById(R.id.settings);
		prefs.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (settings.getVibrateEnabled()) {
					myVib.vibrate(50);
				}
				Intent i2 = new Intent(AboutActivity.this, PostSettingsActivity.class);
				startActivity(i2);
				finish();
			}
		});
        
        TextView version = (TextView) findViewById(R.id.textView2);
        String versionName = "";
		try {
			versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}

        version.setText(versionName);
        TextView about = (TextView) findViewById(R.id.textView6);
        about.setText("This is an official Android application for the Software Testing Club. http://www.softwaretestingclub.com");
	}

}
