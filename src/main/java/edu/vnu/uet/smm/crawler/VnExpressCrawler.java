package edu.vnu.uet.smm.crawler;

import java.net.URL;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.Date;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

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
		String url = "http://vnexpress.net/tin-tuc/giao-duc/";
		String pathFolder = "/home/quangle/crawled_data/vnexpress/";
		int noPages = 1;
		int documentPerFile = 5;
		//Crawl noPages first pages
		ArrayList<SMMDocument> smmdocs = new ArrayList<SMMDocument>();
		int countDoc = 0;
		for(int i = 1; i <= noPages; ++i) {
			System.out.println("Crawling page: " + i);
			try {
				String urlPage = url + "page/" + Integer.toString(i) + ".html"; 
				Document doc = URLFetcher.fetchByJsoup(urlPage, CRAWLER_TIMEOUT);
				if (doc != null) {
					ArrayList<String> smmdocsURL = VnExpressExtractor.extractListDocOnePage(doc, url);
					for(String docURL : smmdocsURL) {
						try {
							countDoc++;
							Document newsDoc = URLFetcher.fetchByJsoup(docURL, CRAWLER_TIMEOUT);
							SMMDocument smmdoc = VnExpressExtractor.extract(newsDoc, IS_ANALYSIS);
							ArrayList<SMMDocument> smmcomments = new ArrayList<SMMDocument>(VnExpressExtractor.extractComments(smmdoc, IS_ANALYSIS));
							smmdoc.setComments(smmcomments);
							Date date = new Date();
							DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy.HH-mm-ss");
							String lastUpdate = dateFormat.format(date);
							smmdoc.setLastUpdate(lastUpdate);
							smmdocs.add(smmdoc);
							if (countDoc % documentPerFile == 0) {
								String pathFile = pathFolder + "vnexpress." + lastUpdate + ".xml";
								XMLWriter.writeToFile(smmdocs, pathFile);
								smmdocs.clear();
							}
						} catch (Exception e){
							e.printStackTrace();
						}
					}
				}
			} catch (Exception e){
				e.printStackTrace();
			}
			// Print remaining documents
			if (countDoc % documentPerFile > 0) {
				Date date = new Date();
				DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy.HH-mm-ss");
				String lastUpdate = dateFormat.format(date);
				String pathFile = pathFolder + "vnexpress." + lastUpdate + ".xml";
				XMLWriter.writeToFile(smmdocs, pathFile);
				smmdocs.clear();
			}
			System.out.println("    Crawled: " + countDoc);
		}
//		//Test crawler
//		// Link without comment
//		//String url = "http://vnexpress.net/tin-tuc/the-gioi/nguoi-viet-5-chau/cac-tu-nhan-vuot-nguc-goc-viet-bi-truy-to-toi-bat-coc-3365031.html";
//		// Link has comments
//		String url = "http://vnexpress.net/tin-tuc/giao-duc/uoc-mo-tro-thanh-bac-si-cua-co-be-xuong-thuy-tinh-3364832.html";
//		try{
//			Document doc = URLFetcher.fetchByJsoup(url, CRAWLER_TIMEOUT);
//			if (doc != null) {
//				SMMDocument smmdoc = VnExpressExtractor.extract(doc, IS_ANALYSIS);
//				ArrayList<SMMDocument> smmdocs = new ArrayList<SMMDocument>();
//				smmdocs.add(smmdoc);
//				ArrayList<SMMDocument> smmcomments = new ArrayList<SMMDocument>(VnExpressExtractor.extractComments(smmdoc, IS_ANALYSIS));
//				smmdoc.setComments(smmcomments);
//				XMLWriter.writeToFile(smmdocs, "/home/quangle/crawled_data/vnexpress/test.xml");
//				System.out.println("Done Crawl");
//			}
//		}
//		catch (Exception e){
//			e.printStackTrace();
//		}
	}
}
