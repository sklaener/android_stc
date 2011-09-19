/* 
Software Testing Club Android application
Stefan Klaener
sklaener@gmail.com
*/

package com.echo.stc;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class FlingWebView extends WebView {
	Context context;
	private GestureDetector mGestureDetector;
	private Bitmap Screenshot;
	WebViewClient client;

    
	public FlingWebView(Context context) {
		super(context);
		this.context = context;
	}
	
	public void setGestureDetector(GestureDetector m){
		this.mGestureDetector = m;
	}
	

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		super.onTouchEvent(event);
		if (mGestureDetector.onTouchEvent(event)) {
			return true;
		} else {
			return false;
		}
	}
	

		
	public Bitmap getScreenshot(){
		return Screenshot;
	}
	
	public void setScreenshot(Bitmap i){
		this.Screenshot=i;
	}
}

