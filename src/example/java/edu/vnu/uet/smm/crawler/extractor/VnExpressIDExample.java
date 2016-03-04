package edu.vnu.uet.smm.crawler.extractor;

import org.jsoup.nodes.Document;

import edu.vnu.uet.smm.common.base.SMMDocument;
import edu.vnu.uet.smm.crawler.fetcher.URLFetcher;

public class VnExpressIDExample {

	public static void main(String[] args) {
		int begin = 1868787;
		for (int i = begin;; i++) {
			String url = "http://giaitri.vnexpress.net/tin-tuc/a-b/a-b-" + i + ".html";
			Document doc = URLFetcher.fetchByJsoup(url, 3000);
			if (doc == null)
				continue;
			SMMDocument smmdoc = VnExpressExtractor.extract(doc, false);
			if (smmdoc == null)
				continue;
			System.out.println(i + "\t" + smmdoc.getOriginalDate());
		}

	}

}
