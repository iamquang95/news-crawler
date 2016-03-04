package edu.vnu.uet.smm.crawler;

import java.net.URL;
import java.util.List;
import java.util.Set;

import org.jsoup.nodes.Document;

import edu.vnu.uet.smm.common.base.SMMDocument;
import edu.vnu.uet.smm.common.util.PropertyLoader;
import edu.vnu.uet.smm.crawler.extractor.VnExpressExtractor;
import edu.vnu.uet.smm.crawler.XMLWriter;

import edu.vnu.uet.smm.crawler.fetcher.URLFetcher;
import edu.vnu.uet.smm.database.elastic.ElasticSearchConnection;

public class VnExpressCrawler {
	static final String HOSTNAME = PropertyLoader.getInstance().getStringProperty("ELASTIC_HOSTNAME");
	static final String INDEXNAME = PropertyLoader.getInstance().getStringProperty("ELASTIC_INDEXNAME");
	static final int CRAWLER_TIMEOUT = PropertyLoader.getInstance().getIntegerProperty("CRAWLER_TIMEOUT");
	static final int CRAWLER_LEVEL = PropertyLoader.getInstance().getIntegerProperty("CRAWLER_LEVEL");
	static final boolean IS_ANALYSIS = PropertyLoader.getInstance().getBooleanProperty("IS_ANALYSIS");
	static final boolean GET_COMMENT = PropertyLoader.getInstance().getBooleanProperty("GET_COMMENT");

	static ElasticSearchConnection es;

	static {
		try {
			es = new ElasticSearchConnection(HOSTNAME);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		String url = "http://vnexpress.net/tin-tuc/thoi-su/tp-hcm-don-tau-hon-54-000-tan-cap-cang-2991803.html";
		try{
			Document doc = URLFetcher.fetchByJsoup(url, CRAWLER_TIMEOUT);
			if (doc != null) {
//				System.out.println(VnExpressExtractor.extractContent(doc));
				SMMDocument smmdoc = VnExpressExtractor.extract(doc, false); // IS_ANALYSIS = False
				System.out.println(smmdoc.getLink());
//				System.out.println(smmdoc.getCategory());
//				//System.out.println(smmdoc.getLastUpdate());
//				System.out.println(smmdoc.getTitle());
//				System.out.println(smmdoc.getLike());
				XMLWriter.writeToFile(smmdoc, "/home/quangle/crawled_data/vnexpress/test.xml");
				
			}
		}
		catch (Exception e){
			e.printStackTrace();
		}
	
//		String url = "http://vnexpress.net";
//		int loop = 1;
//		boolean isLoop = true;
//		while (isLoop) {
//			System.out.println("Iterator " + loop);
//			try {
//				URLFetcher fetcher = new URLFetcher(url);
//				Set<URL> links = fetcher.getFullLinks(CRAWLER_LEVEL);
//				for (URL link : links) {
//					if (link.getRef() == null && link.getFile().endsWith(".html")) {
//						Document doc = URLFetcher.fetchByJsoup(link.toString(), CRAWLER_TIMEOUT);
//						if (doc == null)
//							continue;
//
//						SMMDocument smmdoc = VnExpressExtractor.extract(doc, IS_ANALYSIS);
//						if (smmdoc != null) {
//							es.createIndexResponse(INDEXNAME, "article", smmdoc.getId(), smmdoc.printJson());
//							if (GET_COMMENT) {
//								List<SMMDocument> smmcomments = VnExpressExtractor.extractComments(smmdoc, IS_ANALYSIS);
//								for (SMMDocument comment : smmcomments)
//									es.createIndexResponse(INDEXNAME, "comment", comment.getId(), comment.printJson());
//							}
//						}
//					}
//				}
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//			loop++;
//		}
	}
}
