/* 
Software Testing Club Android application
Stefan Klaener
sklaener@gmail.com
*/

package com.echo.stc;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;

public class ImageAdapter extends BaseAdapter {
    int mGalleryItemBackground;
    private Context mContext;
    private ArrayList<Bitmap> mImageIds = new ArrayList<Bitmap>();
    

    public ImageAdapter(Context c) {
        mContext = c;
    }

    public int getCount() {
        return mImageIds.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView i = new ImageView(mContext);

        i.setImageBitmap(mImageIds.get(position));
        i.setScaleType(ImageView.ScaleType.FIT_START);
        i.setLayoutParams(new Gallery.LayoutParams(Gallery.LayoutParams.WRAP_CONTENT, Gallery.LayoutParams.FILL_PARENT));
        i.setBackgroundResource(mGalleryItemBackground);

        return i;
    }
    
    public void addImage(Bitmap bm){
    	mImageIds.add(bm);
    }
    
    public void addImage (Bitmap bm, int position){
    	try {
    		mImageIds.add(position, bm);
    	} catch (Exception e){
    		e.printStackTrace();
    	}
    }
    
    public void removeImage(int position){
    	mImageIds.remove(position);
    }
    
    public void removeAllImages(){
    	mImageIds.clear();
    }
}
