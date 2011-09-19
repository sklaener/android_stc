/* 
Software Testing Club Android application
Stefan Klaener
sklaener@gmail.com
*/

package com.echo.stc;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.Gallery;

public class AdGallery extends Gallery {

	public static float MOVE_RIGHT = 20000f;
	public static float MOVE_LEFT = 20001f;

	
	public AdGallery(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
	
	public AdGallery(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public AdGallery(Context context, AttributeSet attrs, int i) {
		super(context, attrs, i);
		// TODO Auto-generated constructor stub
	}

	private boolean isScrollingLeft(MotionEvent e1, MotionEvent e2){
		  return e2.getX() > e1.getX();
	}
	
	
	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY){
		if (e1 != null && e2 != null){
			int kEvent;
			  if(isScrollingLeft(e1, e2)){ //Check if scrolling left
			    kEvent = KeyEvent.KEYCODE_DPAD_LEFT;
			  }
			  else{ //Otherwise scrolling right
			    kEvent = KeyEvent.KEYCODE_DPAD_RIGHT;
			  }
			  onKeyDown(kEvent, null);

		} else {
			return super.onFling(e1, e2, velocityX, velocityY);
		}
	  
	  return true;  
	}
	
	@Override
	public boolean onDown(MotionEvent e){
	//	StoryListActivity.galleryIsPressed = true;
		return super.onDown(e);
	}
	
}
