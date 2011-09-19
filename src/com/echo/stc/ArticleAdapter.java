/* 
Software Testing Club Android application
Stefan Klaener
sklaener@gmail.com
*/

package com.echo.stc;

import java.util.ArrayList;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

class ArticleAdapter extends ArrayAdapter<Article> {
	private ArrayList<Article> items;
	private Context c;

    public ArticleAdapter(Context context, int textViewResourceId, ArrayList<Article> items) {
            super(context, textViewResourceId, items);
            this.items = items;
            this.c = context;
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.row, null);
            }
            try {
	            Article story = items.get(position);
	            if (story != null) {
	            	TextView title = (TextView) v.findViewById(R.id.rowText1);
	               	if (title != null) {
	               		title.setText(story.getTitle());                       
	               	}
	
	               	ImageView image = (ImageView) v.findViewById(R.id.rowImage);
	               	TextView centreText = (TextView) v.findViewById(R.id.centreText);
	               	centreText.setText(story.getCentreText());
	               	
	                if (title.getText()!= ""){
	                	if (story.getImageURL().equals("") || story.getImageURL() == null){ //story.getImageBitmap() == null || 
	                    	image.setImageDrawable(c.getResources().getDrawable(R.drawable.icon));
	                	} else {
	                    	image.setImageBitmap(story.getImageBitmap());
	                	}
	                } else {
                    	image.setImageDrawable(c.getResources().getDrawable(R.drawable.blank));
	                }
	                	                
	                FrameLayout back = (FrameLayout) v.findViewById(R.id.storyBackground);
	                back.setBackgroundResource(story.getColor());
	            }
            } catch (ArrayIndexOutOfBoundsException e){
            	Log.e("STC", e.toString());
            	return null;
            }
            return v;
    }
    public int getItemCount(){
    	return items.size();
    }
}
