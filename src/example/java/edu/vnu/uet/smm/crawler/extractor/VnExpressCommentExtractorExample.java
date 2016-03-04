package edu.vnu.uet.smm.crawler.extractor;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import edu.vnu.uet.smm.crawler.fetcher.URLFetcher;

public class VnExpressCommentExtractorExample {

	public static void main(String[] args) {
		String url = "http://usi.saas.vnexpress.net/index/get?offset=0&objectid=3323800&objecttype=1&siteid=1000000&limit=100000";
		String content = URLFetcher.fetchByGuava(url);
		JsonParser parser = new JsonParser();
		JsonElement element = parser.parse(content);
		if (element.isJsonObject()) {
			JsonObject commentsObject = element.getAsJsonObject();
			JsonArray comments = commentsObject.get("data").getAsJsonObject().get("items").getAsJsonArray();
			for (int i = 0; i < comments.size(); i++) {
				JsonObject comment = comments.get(i).getAsJsonObject();
				System.out.println(comment);
				System.out.println(comment.get("comment_id"));
				System.out.println(comment.get("content").getAsString());
			}
			
		}
	}
}
