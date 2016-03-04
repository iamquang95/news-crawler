package edu.vnu.uet.smm.crawler.extractor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import edu.vnu.uet.smm.common.base.SMMDocument;
import edu.vnu.uet.smm.common.util.JodaTimeParser;
import edu.vnu.uet.smm.common.util.RegexHelper;
import edu.vnu.uet.smm.crawler.fetcher.URLFetcher;
import edu.vnu.uet.smm.nlp.vntextpro.VnTextProSingleton;
import edu.vnu.uet.smm.nlp.vtools.vnpostagger.VnPOSTaggerSingleton;

public class VnExpressExtractor implements Extractor {
	public static String extractTitle(Document doc) {
		String title = "";
		Elements elems = doc.select(".title_news > h1");
		if (elems.size() == 0)
			return title;

		title = elems.get(0).text();
		return title;
	}

	public static String extractSummary(Document doc) {
		String summary = "";
		Elements elems = doc.select(".short_intro");
		if (elems.size() == 0)
			return summary;

		summary = elems.get(0).text();
		return summary;
	}

	public static String extractDate(Document doc) {
		String date = "";
		Elements elems = doc.select(".block_timer_share > .block_timer");
		if (elems.size() == 0)
			return date;

		date = elems.get(0).text();
		return date;
	}

	public static String extractContent(Document doc) {
		String content = "";
		Elements elems = doc.select(".fck_detail p");
		if (elems.size() == 0) {
			elems = doc.select("#article_content p");
			if (elems.size() == 0)
				return content;
		}
		for (Element elem : elems)
			content += elem.text() + "\n";
		return content;
	}

	public static String extractCategory(Document doc) {
		String category = "";
		Elements elems = doc.select(".list_arrow_breakumb a");
		if (elems.size() == 0)
			return category;

		for (Element elem : elems)
			category += elem.text() + " | ";
		return category;
	}

	public static String extractId(String url) {
		String id = "";

		List<String> matches = RegexHelper.findStringRegex(url, "(\\d+).html");
		if (matches.size() == 0)
			return id;

		for (String match : matches)
			id = match.replace(".html", "");

		return id;
	}

	public static SMMDocument extract(Document doc, boolean analysis) {
		SMMDocument smmdoc = new SMMDocument();
		String title = extractTitle(doc);
		if (title.isEmpty())
			return null;

		String date = extractDate(doc);
		if (analysis) {
			String fmtdate = VnExpressExtractor.parseTime(date);
			if (fmtdate == null)
				return null;
		}		

		String id = extractId(doc.baseUri());
		String summary = extractSummary(doc);
		String content = extractContent(doc);
		String category = extractCategory(doc);

		smmdoc.setId("vne_article" + id);
		smmdoc.setLink(doc.baseUri());
		smmdoc.setSource("VnExpress");
		smmdoc.setSourceCategory("News");
		smmdoc.setType("Article");	
		smmdoc.setCategory(category);
		smmdoc.setTitle(title);
		smmdoc.setContent(content);

		if (analysis) {
			String fullcontent = title + "\n" + summary + "\n" + content;

			String contentWS = "";
			String contentPOS = "";
			try {
				List<List<String>> tokenizedSentences = VnTextProSingleton.getInstance().segment(fullcontent);
				for (List<String> sent : tokenizedSentences)
					contentWS += StringUtils.join(sent, " ") + "\n";

				contentPOS = VnPOSTaggerSingleton.getInstance().tagToString(tokenizedSentences);

			} catch (Exception e) {
				e.printStackTrace();
			}
			smmdoc.setContentPOS(contentPOS);
		}

		return smmdoc;
	}

	public static List<SMMDocument> extractComments(SMMDocument smmdoc, boolean analysis) {
		List<SMMDocument> smmcomments = new ArrayList<SMMDocument>();

		String url = "http://usi.saas.vnexpress.net/index/get?offset=0&objectid="
				+ smmdoc.getId().replace("vne_article", "") + "&objecttype=1&siteid=1000000&limit=100000";
		String content = URLFetcher.fetchByGuava(url);
		JsonParser parser = new JsonParser();
		JsonElement element = parser.parse(content);
		if (element.isJsonObject()) {
			JsonObject commentsObject = element.getAsJsonObject();
			JsonArray comments = commentsObject.get("data").getAsJsonObject().get("items").getAsJsonArray();
			for (int i = 0; i < comments.size(); i++) {
				JsonObject comment = comments.get(i).getAsJsonObject();

				String text = comment.get("content").getAsString();

				SMMDocument smmcomment = new SMMDocument();
				smmcomment.setId("vne_comment" + comment.get("comment_id").getAsString());
				smmcomment.setLink(smmdoc.getLink());
				smmcomment.setSource("VnExpress");
				smmcomment.setSourceCategory("News");
				smmcomment.setType("Comment");
				smmcomment.setCategory(smmdoc.getCategory());
				smmcomment.setContent(text);
				smmcomment.setAuthor(comment.get("full_name").getAsString());

				if (analysis) {
					String contentWS = "";
					String contentPOS = "";
					try {
						List<List<String>> tokenizedSentences = VnTextProSingleton.getInstance().segment(text);
						for (List<String> sent : tokenizedSentences)
							contentWS += StringUtils.join(sent, " ") + "\n";

						contentPOS = VnPOSTaggerSingleton.getInstance().tagToString(tokenizedSentences);

					} catch (Exception e) {
						e.printStackTrace();
					}
					smmcomment.setContentPOS(contentPOS);
				}
				smmcomments.add(smmcomment);
			}
		}
		return smmcomments;
	}

	public static String parseTime(String text) {
		String time = JodaTimeParser.now();
		Map<Pattern, String> regexs = new HashMap<Pattern, String>();
		String[] rules = new String[] {
				"([0-3]?\\d)/([0-1]?\\d)/(20\\d{2}) \\| ([0-2]\\d):([0-5]\\d)	d-M-yyyy-HH-mm" };
		RegexHelper.addRegexs(regexs, rules);
		time = RegexHelper.timeParser(regexs, text);
		return time;
	}
}
