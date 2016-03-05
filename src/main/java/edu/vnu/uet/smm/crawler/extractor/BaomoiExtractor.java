package edu.vnu.uet.smm.crawler.extractor;

import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import edu.vnu.uet.smm.common.base.SMMDocument;
import edu.vnu.uet.smm.common.util.JodaTimeParser;
import edu.vnu.uet.smm.common.util.RegexHelper;

public class BaomoiExtractor implements Extractor {
	
	static DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy.HH-mm-ss");
	
    public static String extractTitle(Document doc) {
        String title = "";
        Elements elems = doc.select("article.main-article header > h1");
        if (elems.size() == 0)
            return title;

        title = elems.get(0).text();
        return title;
    }

    public static String extractSummary(Document doc) {
        String summary = "";
        Elements elems = doc.select("div.article-body > div.sapo");
        if (elems.size() == 0)
            return summary;

        summary = elems.get(0).text();
        return summary;
    }

    public static String extractDate(Document doc) {
        String date = "";
        Elements elems = doc.select("article.main-article header time");
        if (elems.size() == 0)
            return date;

        date = elems.get(0).text();
        return date;
    }

    public static String extractContent(Document doc) {
        String content = BaomoiExtractor.extractSummary(doc);
        Elements elems = doc.select("article.main-article > div.article-body > div.body > p.body-text");
        if (elems.size() == 0)
            return content;

        for (Element elem : elems)
            content += elem.text() + "\n";
        return content;
    }

    public static String extractCategory(Document doc) {
        String category = "";
        Elements elems = doc.select(".breadcrumbs a");
        if (elems.size() == 0)
            return category;

        category += elems.get(0).text();

        return category;
    }

    public static String extractId(String url) {
        String id = "";
        List<String> matches = RegexHelper.findStringRegex(url, "(\\d+).epi");
        if (matches.size() == 0)
            return id;

        for (String match : matches)
            id = match.replace(".epi", "");

        return id;
    }

    public static SMMDocument extract(Document doc, boolean analysis) {
        SMMDocument smmdoc = new SMMDocument();

        String title = extractTitle(doc);
        if (title.isEmpty())
            return null;

        String id = extractId(doc.baseUri());
        String content = extractContent(doc);
        String category = extractCategory(doc);
        String date = extractDate(doc);
        
        smmdoc.setId("baomoi_article" + id);
        smmdoc.setLink(doc.baseUri());
        smmdoc.setSource("Baomoi");
        smmdoc.setSourceCategory("News");
        smmdoc.setType("Article");
        smmdoc.setCategory(category);
        smmdoc.setTitle(title);
        smmdoc.setContent(content);
        smmdoc.setDate(date);
        Calendar cal = Calendar.getInstance();
        smmdoc.setLastUpdate(dateFormat.format(cal.getTime()));
        return smmdoc;
    }

    public static String parseTime(String text) {
        String time = JodaTimeParser.now();
        Map<Pattern, String> regexs = new HashMap<Pattern, String>();
        String[] rules = new String[] {
                "([0-3]?\\d)/([0-1]?\\d)/(20\\d{2}) ([0-2]\\d):([0-5]\\d)	d-M-yyyy-HH-mm" };
        RegexHelper.addRegexs(regexs, rules);
        time = RegexHelper.timeParser(regexs, text);
        return time;
    }
}
