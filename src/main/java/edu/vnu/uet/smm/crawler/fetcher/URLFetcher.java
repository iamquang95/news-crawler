package edu.vnu.uet.smm.crawler.fetcher;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

public class URLFetcher {
	private String url;
	private Document document;
	private static int timeout = 3000;

	public URLFetcher(String url) {
		if (!url.startsWith("http"))
			url = "http://" + url;
		this.url = url;
		document = fetchByJsoup(url, timeout);
	}

	public String getStringURL() {
		return this.url;
	}

	public URL getURL() {
		try {
			return new URL(url);
		} catch (Exception e) {
			return null;
		}
	}

	public String getSource() {
		return document.html();
	}

	public Document getDocument() {
		return document;
	}

	public List<String> getLinks() {
		return getLinks(document);
	}

	public List<String> getLinks(Document doc) {
		if (doc == null)
			return null;
		List<String> links = new ArrayList<String>();
		Elements elements = doc.select("a[href]");
		for (Element element : elements) {
			String link = element.attr("href");
			if (!link.contains("javascript:"))
				links.add(link);
		}
		return links;
	}

	public static List<String> getLinks(URL url) {
		return getLinks(url.toString());
	}

	public static List<String> getLinks(String url) {
		Document doc = fetchByJsoup(url, timeout);
		if (doc == null)
			return null;
		List<String> links = new ArrayList<String>();
		Elements elements = doc.select("a[href]");
		for (Element element : elements) {
			String link = element.attr("href");
			if (!link.contains("javascript:"))
				links.add(link);
		}
		return links;
	}

	public static Set<URL> getFullLinks(URL url, boolean indomain) {
		Set<URL> fullLinks = new HashSet<URL>();
		List<String> links = getLinks(url);

		if (links == null)
			return null;

		for (String link : links) {
			try {
				URL fullUrl = new URL(url, link);
				if (indomain) {
					if (fullUrl.getHost().contains(url.getHost()))
						fullLinks.add(fullUrl);
				} else {
					fullLinks.add(fullUrl);
				}
			} catch (Exception e) {
			}
		}
		return fullLinks;
	}

	public static Set<URL> getFullLinks(Set<URL> seedLinks, Set<URL> visitedLinks) {
		Set<URL> fullLinks = new HashSet<URL>();
		if (seedLinks == null)
			return null;
		if (seedLinks.size() == 0)
			return null;

		for (URL seed : seedLinks) {
			Set<URL> links = getFullLinks(seed, true);
			if (links != null)
				fullLinks.addAll(links);
		}

		return fullLinks;
	}

	public Set<URL> getFullLinks(boolean indomain) {
		Set<URL> fullLinks = new HashSet<URL>();
		List<String> links = getLinks();

		if (links == null)
			return null;

		URL rootURL = getURL();
		for (String link : links) {
			try {
				URL fullUrl = new URL(rootURL, link);
				if (indomain) {
					if (fullUrl.getHost().contains(rootURL.getHost()))
						fullLinks.add(fullUrl);
				} else {
					fullLinks.add(fullUrl);
				}
			} catch (Exception e) {
			}
		}
		return fullLinks;
	}

	public Set<URL> getFullLinks(int deep) {
		Set<URL> urls = new HashSet<URL>();
		Set<URL> seedLinks = new HashSet<URL>();
		seedLinks.add(getURL());

		for (int i = 0; i <= deep; i++) {
			seedLinks = getFullLinks(seedLinks, urls);
			urls.addAll(seedLinks);
		}

		return urls;
	}

	public static Document fetchByJsoup(String url, int timeout) {
		if (!url.startsWith("http"))
			url = "http://" + url;
		Document doc;
		try {
			doc = Jsoup.connect(url)
					.userAgent(
							"Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.101 Safari/537.36")
					.cookie("auth", "token").timeout(timeout).get();
		} catch (Exception e) {
			return null;
		}
		return doc;
	}

	public static String fetchByGuava(String url) {
		String content = "";
		URL _url;
		try {
			_url = new URL(url);
			content = Resources.toString(_url, Charsets.UTF_8);
		} catch (Exception e) {
			return content;
		}
		return content;
	}
}
