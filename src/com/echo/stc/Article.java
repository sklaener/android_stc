/* 
Software Testing Club Android application
Stefan Klaener
sklaener@gmail.com
 */

package com.echo.stc;

import java.util.ArrayList;

import android.graphics.Bitmap;

public class Article {
	private String title = "";
	private String descrip = "";
	private int picture;
	private Bitmap pictureBitmap;
	private String author = "";
	private String url = "";
	private String imgUrl = "";
	private String centreText = "";
	private int colour = R.color.menuColor1;
	private int background;
	private ArrayList<String> categories = new ArrayList<String>();
	private String category = "";


	public void setTitle(String title) {
		this.title = title;
	}

	public void addCategory(String category) {
		this.categories.add(category);
	}

	public void setBackground(int back) {
		this.background = back;
	}

	public void setColour(int colour) {
		this.colour = colour;
	}

	public void setCentreText(String text) {
		this.centreText = text;
	}

	public void setDescription(String des) {
		this.descrip = des;
	}

	public void setImage(int id) {
		this.picture = id;
	}

	public void setBitmap(Bitmap pic) {
		this.pictureBitmap = pic;
	}

	public void setImageURL(String imgUrl) {
		this.imgUrl = imgUrl;
	}

	public void setAuthor(String aut) {
		this.author = aut;
	}

	public void setCategory(String cat) {
		this.category = cat;
	}

	public void setURL(String url) {
		this.url = url;
	}

	public String getTitle() {
		return this.title;
	}

	public String getCentreText() {
		return this.centreText;
	}

	public String getDescrip() {
		return this.descrip;
	}

	public int getImage() {
		return this.picture;
	}

	public String getAuthor() {
		return this.author;
	}

	public String getURL() {
		return this.url;
	}

	public String getImageURL() {
		return this.imgUrl;
	}

	public Bitmap getImageBitmap() {
		return this.pictureBitmap;
	}

	public int getColor() {
		return this.colour;
	}

	public int getBackground() {
		return this.background;
	}

	public ArrayList<String> getCategories() {
		return this.categories;
	}

	public String getCategory() {
		return this.category;
	}
}
