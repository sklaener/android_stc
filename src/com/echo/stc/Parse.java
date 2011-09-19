/* 
Software Testing Club Android application
Stefan Klaener
sklaener@gmail.com
*/

package com.echo.stc;

import android.text.Html;
import android.util.Log;

public class Parse {

	public static String toString(String input){
		return Html.fromHtml(input).toString();
	}
	
	public static String removeErrors(String input){
		boolean none = false;
    	while (true){
	    	for (int a = 0; a < input.length(); a++){
	    		int value = (int)input.charAt(a);
	    		if (value == 160){
	    			input = input.substring(0, a) + " " + input.substring(a+1, input.length());
	    			none = true;
	    			break;
	    		} else if (value== 8216 || value == 8217){
	    			input = input.substring(0, a) + "'" + input.substring(a+1, input.length());
	    			none = true;
	    			break;
	    		} else if (value == 8220 || value == 8221){
	    			input = input.substring(0, a) + "\"" + input.substring(a+1, input.length());
	    			none = true;
	    			break;
	    		} else if (value == 8211){
	    			input = input.substring(0, a) + "-" + input.substring(a+1, input.length());
	    			none = true;
	    			break;
	    		} else if (value == 8230){
	    			input = input.substring(0, a) + "..." + input.substring(a+1, input.length());
	    		} else if (value > 8000){
	    			input = input.substring(0,a) + "" + input.substring(a+1, input.length());
	    		}
	    	}
	    	if (!none){
	    		break;
	    	} else {
	    		none = false;
	    	}
    	} 
    	return input;
	}
	
	public static void debugLine(String input){
		for (int a = 0; a<input.length(); a++){
			Log.d(String.valueOf(input.charAt(a)), Integer.toString((int)input.charAt(a)));
		}
	}
}
