/* 
Software Testing Club Android application
Stefan Klaener
sklaener@gmail.com
*/

package com.echo.stc;

import android.app.Activity;
import android.content.Intent;
import android.os.*;

public class LoadScreenActivity extends Activity {
    /** Called when the activity is first created. */
	final int SPLASH_DISPLAY_TIME = 500;
	ApplicationSettings settings;
    
	@Override
    public void onCreate(Bundle savedInstanceState){
    	setTheme(android.R.style.Theme_Light_NoTitleBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
        settings = (ApplicationSettings) this.getApplication();
        
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                   
                    /* Create an intent that will start the main activity. */
                    Intent mainIntent = new Intent(LoadScreenActivity.this,
                            DashboardActivity.class);
                  //  mainIntent.putExtra("start", true);
                    LoadScreenActivity.this.startActivity(mainIntent);
                   
                    /* Finish splash activity so user cant go back to it. */
                    LoadScreenActivity.this.finish();
                   
                    //overridePendingTransition(R.anim.mainfadein, R.anim.splashfadeout);
            }
    }, SPLASH_DISPLAY_TIME);
        
    }
}