/* 
Software Testing Club Android application
Stefan Klaener
sklaener@gmail.com
*/

package com.echo.stc;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import android.app.Application;
import android.util.Log;

public class ApplicationSettings extends Application {

	  public void setFontSize(String fontSize) {
		  try {
			  	Log.d("STC", "FONTSIZE WRITTEN : " + fontSize);
				FileOutputStream file = openFileOutput("fontSize.txt", 0);
				file.write(fontSize.getBytes());
				file.close();
			} catch (IOException e) {
			}
	  }
	  
	  public void setVibrateEnabled(boolean enable) {
		  try {
			  FileOutputStream file = openFileOutput("vibrate.txt", 0);
			  if (enable){
				  file.write("y".getBytes());
			  } else {
				  file.write("n".getBytes());
			  }
			  file.close();
		  } catch (IOException e) {
			  
		  }
		  
	  }
	  
	  public boolean getVibrateEnabled(){
		String line;
		try {
			FileInputStream actualfile;
            actualfile = openFileInput("vibrate.txt");
            InputStreamReader sr = new InputStreamReader(actualfile);
            BufferedReader fr = new BufferedReader(sr);
            line = fr.readLine();
            actualfile.close();
		} catch (IOException e) {
			return true;
		}
		if (line.equals("y")){
			return true;
		} else {
			return false;
		}
	  }
	  
	  public int getFontSize(){
		  String line = null;
		  try {
	            FileInputStream actualfile;
	            actualfile = openFileInput("fontSize.txt");
	            InputStreamReader sr = new InputStreamReader(actualfile);
	            BufferedReader fr = new BufferedReader(sr);
	            line = fr.readLine();
	            actualfile.close();
			} catch (IOException e) {
				return 15;
			} catch (NullPointerException e){
				return 15;
			}
			return Integer.parseInt(line);
	  }

	public void setSliderCategory(String sliderCategory) {
		  try {
			  	Log.d("STC", "SLIDER CATEGORY WRITTEN : " + sliderCategory);
				FileOutputStream file = openFileOutput("sliderCategory.txt", 0);
				file.write(sliderCategory.getBytes());
				file.close();
			} catch (IOException e) {
			}
	  }
	
	  public String getSliderCategory(){
		  String line = null;
		  try {
	            FileInputStream actualfile;
	            actualfile = openFileInput("sliderCategory.txt");
	            InputStreamReader sr = new InputStreamReader(actualfile);
	            BufferedReader fr = new BufferedReader(sr);
	            line = fr.readLine();
	            actualfile.close();
			} catch (IOException e) {
				return "None (default)";
			} catch (NullPointerException e){
				return "None (default)";
			}
			return line;
	  }
}