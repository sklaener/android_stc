
package com.echo.stc;

class RSSData {
		private String title;
		private int id;
		private String category;
		private String url;
		private String author;
		private String blogTitle;
		private String itemContent;

		public RSSData(String title, String author, int id,	String url, String blogTitle, String itemContent, String category) {
			this.title = title;
			this.author = author;
			this.category = category;
			this.url = url;
			this.id = id;
			this.blogTitle = blogTitle;
			this.itemContent = itemContent;
		}

		public String getTitle() {
			return title;
		}

		public String getCategory() {
			return category;
		}
		public String getContent() {
			return itemContent;
		}

		public String getUrl() {
			return url;
		}

		public String getBlogTitle() {
			return blogTitle;
		}

		public int getId() {
			return id;
		}

		public String getAuthor() {
			return author;
		}
	}