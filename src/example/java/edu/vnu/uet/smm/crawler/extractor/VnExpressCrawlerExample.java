package edu.vnu.uet.smm.crawler.extractor;

import java.net.URL;
import java.util.Set;

import org.jsoup.nodes.Document;

import edu.vnu.uet.smm.crawler.fetcher.URLFetcher;

public class VnExpressCrawlerExample {
	public static void main(String[] args) {
		String url = "http://vnexpress.net";
		URLFetcher fetcher = new URLFetcher(url);
		Set<URL> links = fetcher.getFullLinks(0);
		for (URL link : links)
			if (link.getRef() == null && link.getFile().endsWith(".html")) {
				Document doc = URLFetcher.fetchByJsoup(link.toString(), 3000);
				if (doc == null)
					continue;
				String title = VnExpressExtractor.extractTitle(doc);
				if (title.isEmpty())
					continue;
				String date = VnExpressExtractor.extractDate(doc);
				String summary = VnExpressExtractor.extractSummary(doc);
				System.out.println(link + "\t" + date + "\t" + title + "\t" + summary);
			}
	}
}
