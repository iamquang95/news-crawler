package edu.vnu.uet.smm.crawler.extractor;

import java.util.concurrent.Callable;

import org.jsoup.nodes.Document;

import edu.vnu.uet.smm.common.base.SMMDocument;
import edu.vnu.uet.smm.crawler.fetcher.URLFetcher;

public class VnExpressIDFetchCallable implements Callable<SMMDocument> {
	private int id;

	public VnExpressIDFetchCallable(int baomoiID) {
		this.id = baomoiID;
	}

	public SMMDocument call() throws Exception {
		String url = "http://giaitri.vnexpress.net/tin-tuc/a-b/a-b-" + id + ".html";
		Document doc = URLFetcher.fetchByJsoup(url, 3000);
		if (doc == null)
			return null;
		SMMDocument smmdoc = VnExpressExtractor.extract(doc, false);
		if (smmdoc == null)
			return null;
		return smmdoc;
	}
}
