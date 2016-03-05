package edu.vnu.uet.smm.crawler;

import edu.vnu.uet.smm.common.base.SMMDocument;
import edu.vnu.uet.smm.crawler.extractor.BaomoiExtractor;
import edu.vnu.uet.smm.crawler.fetcher.URLFetcher;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import edu.vnu.uet.smm.common.util.PropertyLoader;

import java.io.IOException;
import java.util.ArrayList;

public class BaomoiCrawler {
	static final String HOSTNAME = PropertyLoader.getInstance().getStringProperty("ELASTIC_HOSTNAME");
	static final String INDEXNAME = PropertyLoader.getInstance().getStringProperty("ELASTIC_INDEXNAME");
	static final int CRAWLER_TIMEOUT = PropertyLoader.getInstance().getIntegerProperty("CRAWLER_TIMEOUT");
	static final int CRAWLER_LEVEL = PropertyLoader.getInstance().getIntegerProperty("CRAWLER_LEVEL");
	static final boolean IS_ANALYSIS = PropertyLoader.getInstance().getBooleanProperty("IS_ANALYSIS");
	static final boolean GET_COMMENT = PropertyLoader.getInstance().getBooleanProperty("GET_COMMENT");
	
	static final int startID = 15500000;
	
	public static void start() {
        String url_form = "http://www.baomoi.com/abc/c/";

        int currentID = startID;
        String url;
        int count_404 = 0;
        Connection.Response response = null;
        
        int count = 0;
        // Start crawling
        long startTime = System.currentTimeMillis();
        ArrayList<SMMDocument> smmdocs = new ArrayList<SMMDocument>();
        while(true) {
            url = url_form + currentID + ".epi";
            try {
                response = Jsoup.connect(url)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.101 Safari/537.36")
                        .cookie("auth", "token").timeout(CRAWLER_TIMEOUT)
                        .ignoreHttpErrors(true)
                        .execute();

            } catch (IOException e) {
                // System.out.println("io - " + e);
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (response.statusCode() == 404)
                count_404++;
            
            if (response.statusCode() == 200) {
                try {
                    Document doc = URLFetcher.fetchByJsoup(url, CRAWLER_TIMEOUT);
                    if (doc == null)
                        break;
                    if (Integer.parseInt(BaomoiExtractor.extractId(doc.baseUri())) != currentID){
                        currentID++;
                        continue;
                    }
                    if (BaomoiExtractor.extractCategory(doc).contains("Giáo dục") == false){
                    	currentID++;
                    	continue;
                    }
                    
                    // System.out.println("Crawled: " + url);
                    SMMDocument smmdoc = BaomoiExtractor.extract(doc, IS_ANALYSIS);
                    smmdocs.add(smmdoc);
                    count++;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (count_404 == 10000)
                break;
            // System.out.println("ID: " + currentID);
            currentID++;
            // if (currentID - startID >= 50) break;   
        }
        XMLWriter.writeToFile(smmdocs, "/Users/TLMN/Desktop/crawled_data/test.xml");
        long endTime = System.currentTimeMillis();
        /*
        System.out.println("Crawled " + count + " articles "); 
        System.out.println("Elapsed time: " + (endTime - startTime)/1000.0 + " seconds");
        System.out.println("404 Error count: " + count_404);
        */
    }
	
	public static void main(String[] args) {
		start();
	}
}
