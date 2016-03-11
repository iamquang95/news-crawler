package edu.vnu.uet.smm.crawler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.jsoup.nodes.Document;

import edu.vnu.uet.smm.common.base.SMMDocument;
import edu.vnu.uet.smm.common.util.PropertyLoader;
import edu.vnu.uet.smm.crawler.extractor.DantriExtractor;
import edu.vnu.uet.smm.crawler.XMLWriter;

import edu.vnu.uet.smm.crawler.fetcher.URLFetcher;
import edu.vnu.uet.smm.database.elastic.ElasticSearchConnection;

public class DantriCrawler {
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
		String url = "http://dantri.com.vn/giao-duc-khuyen-hoc/";
		String pathFolder = "/home/quangle/crawled_data/dantri/";
		String pathSavedURL = "/home/quangle/crawled_data/dantri_crawled_url.txt";
		int noPages = 500;
		int documentPerFile = 100;
		int maxNoRefetchPage = 100;
		int maxNoRefetchNews = 100;
		//Crawl noPages first pages
		ArrayList<SMMDocument> smmdocs = new ArrayList<SMMDocument>();
		HashSet<String> visitedURLs = new HashSet<String>();
		// Load crawled URL from file
		try {
			BufferedReader in = new BufferedReader(new FileReader(pathSavedURL));
			String savedURL;
			while ((savedURL = in.readLine()) != null)
				visitedURLs.add(savedURL);
		} catch (IOException e) {
			e.printStackTrace();
		}
		int countDoc = 0;
		int countError = 0;
		//for(int i = 1; i <= noPages; ++i) {
		for (int i = noPages; i >= 1; --i) {
			System.out.println("Crawling page: " + i);
			try {
				String urlPage = url + "trang-" + Integer.toString(i) + ".htm"; 
				Document doc = URLFetcher.fetchByJsoup(urlPage, CRAWLER_TIMEOUT);
				int countNoOfRefetchPage = 0;
				// Use this technique to avoid 
				while (doc == null && countNoOfRefetchPage++ <= maxNoRefetchPage) {
					doc = URLFetcher.fetchByJsoup(urlPage, CRAWLER_TIMEOUT);
					if (countNoOfRefetchPage >= 80) {
						Thread.sleep(5000);
						System.out.println("Pausing crawler to avoid banning IP");
					}
				}
				if (doc != null) {
					ArrayList<String> smmdocsURL = DantriExtractor.extractListDocOnePage(doc, "");
					for(String docURL : smmdocsURL) {
						try {
							Document newsDoc = URLFetcher.fetchByJsoup(docURL, CRAWLER_TIMEOUT);
							int countNoRefetchNews = 0;
							while (newsDoc == null && countNoRefetchNews++ <= maxNoRefetchNews) {
								newsDoc = URLFetcher.fetchByJsoup(docURL, CRAWLER_TIMEOUT);
								if (countNoRefetchNews >= maxNoRefetchNews - 5){
									Thread.sleep(5000);
									System.out.println("Pausing crawler to avoid banning IP");
								}
							}
							System.out.println(newsDoc.baseUri());
							SMMDocument smmdoc = DantriExtractor.extract(newsDoc, IS_ANALYSIS);
							if (visitedURLs.contains(smmdoc.getId())) 
								continue;
							else 
								visitedURLs.add(smmdoc.getId());
//							ArrayList<SMMDocument> smmcomments = new ArrayList<SMMDocument>(DantriExtractor.extractComments(smmdoc, IS_ANALYSIS));
//							smmdoc.setComments(smmcomments);
							Date date = new Date();
							DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy.HH-mm-ss");
							String lastUpdate = dateFormat.format(date);
							smmdoc.setLastUpdate(lastUpdate);
							smmdocs.add(smmdoc);
							countDoc++;
							if (countDoc % documentPerFile == 0) {
								String pathFile = pathFolder + "dantri.com.vn." + lastUpdate + ".xml";
								XMLWriter.writeToFile(smmdocs, pathFile);
								smmdocs.clear();
							}
						} catch (Exception e){
							e.printStackTrace();
							Date date = new Date();
							DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy.HH-mm-ss");
							String lastUpdate = dateFormat.format(date);
							String pathFile = pathFolder + "dantri.com.vn." + lastUpdate + ".xml";
							XMLWriter.writeToFile(smmdocs, pathFile);
							smmdocs.clear();
							System.out.println("Flush all data after exception");
							countError++;
						}
					}
				}
			} catch (Exception e){
				e.printStackTrace();
				countError++;
			}
			//// Print remaining documents
			if (countDoc % documentPerFile > 0) {
				Date date = new Date();
				DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy.HH-mm-ss");
				String lastUpdate = dateFormat.format(date);
				String pathFile = pathFolder + "dantri.com.vn." + lastUpdate + ".xml";
				XMLWriter.writeToFile(smmdocs, pathFile);
				smmdocs.clear();
			}
			System.out.println("    Crawled: " + countDoc);
			System.out.println("    Error: " + countError);
		}
		System.out.println("Total Error : " + countError);
		try {
			PrintWriter out = new PrintWriter( new BufferedWriter(new FileWriter("/home/quangle/crawled_data/dantri_crawled_url.txt", false)));
			for (String visitedURL : visitedURLs) {
				out.println(visitedURL);
			}
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
//		//Test crawler
//		String url = "http://dantri.com.vn/giao-duc-khuyen-hoc/hon-6000-sinh-vien-co-nguy-co-bi-ky-luat-vi-khong-dong-bao-hiem-20160309204715645.htm";
//		//url = "http://dantri.com.vn/giao-duc-khuyen-hoc/trang-1.htm";
//		try{
//			Document doc = URLFetcher.fetchByJsoup(url, CRAWLER_TIMEOUT);
//			if (doc != null) {
//				SMMDocument smmdoc = DantriExtractor.extract(doc, IS_ANALYSIS);
//				ArrayList<SMMDocument> smmdocs = new ArrayList<SMMDocument>();
//				smmdocs.add(smmdoc);
//				XMLWriter.writeToFile(smmdocs, "/home/quangle/crawled_data/dantri/test.xml");
//				System.out.println("Done Crawl");
////				ArrayList<String> smmdocsURLs = DantriExtractor.extractListDocOnePage(doc, "");
////				for(String s:smmdocsURLs)
////					System.out.println(s);
//			}
//		}
//		catch (Exception e){
//			e.printStackTrace();
//		}
	}
}
