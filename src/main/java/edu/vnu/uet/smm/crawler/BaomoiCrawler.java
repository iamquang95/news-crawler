package edu.vnu.uet.smm.crawler;

import edu.vnu.uet.smm.common.base.SMMDocument;
import edu.vnu.uet.smm.crawler.extractor.BaomoiExtractor;
import edu.vnu.uet.smm.crawler.fetcher.URLFetcher;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import edu.vnu.uet.smm.common.util.PropertyLoader;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class BaomoiCrawler {
    static final String HOSTNAME = PropertyLoader.getInstance().getStringProperty("ELASTIC_HOSTNAME");
    static final String INDEXNAME = PropertyLoader.getInstance().getStringProperty("ELASTIC_INDEXNAME");
    static final int CRAWLER_TIMEOUT = PropertyLoader.getInstance().getIntegerProperty("CRAWLER_TIMEOUT");
    static final int CRAWLER_LEVEL = PropertyLoader.getInstance().getIntegerProperty("CRAWLER_LEVEL");
    static final boolean IS_ANALYSIS = PropertyLoader.getInstance().getBooleanProperty("IS_ANALYSIS");
    static final boolean GET_COMMENT = PropertyLoader.getInstance().getBooleanProperty("GET_COMMENT");
    
    static final int startID = 16010368;
    static final int BATCH_SIZE = 5;
    
    public static void start() {
        String url_form = "http://www.baomoi.com/abc/c/";

        int currentID = startID-1;
        String url;
        int count_error = 0;
        int count = 0;
        // Connection.Response response = null;
        
        
        // Init to write to xml file
        long startTime = System.currentTimeMillis();
        long endTime;
        ArrayList<SMMDocument> smmdocs = new ArrayList<SMMDocument>();
        String path = "/Users/TLMN/Desktop/crawled_data/baomoi/baomoi.com.";
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy.HH-mm-ss");
        Calendar calender;
   
        // Start crawling
        while(true) {
            /*
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
            else if (response.statusCode() == 200) {
                try {
                    Document doc = URLFetcher.fetchByJsoup(url, CRAWLER_TIMEOUT);
                    if (doc == null){
                        currentID++;
                        continue;
                    }
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
                System.out.println("ID: " + currentID + " Count: " + count);
            }
            if (count_404 == 10000)
                break;         
            */
            if (count_error == 20000)
                break;
            currentID++;
            url = url_form + currentID + ".epi";
            try {
                Document doc = URLFetcher.fetchByJsoup(url, CRAWLER_TIMEOUT);
                if (doc == null){
                    count_error++;
                    continue;    
                }
                if (Integer.parseInt(BaomoiExtractor.extractId(doc.baseUri())) != currentID)
                    continue;
                // System.out.println(BaomoiExtractor.parseTime("17/05/2016 16:59"));
                if (BaomoiExtractor.extractCategory(doc).contains("Giáo dục") == false)
                    continue;
                
                SMMDocument smmdoc = BaomoiExtractor.extract(doc, IS_ANALYSIS);
                if (smmdoc == null){
                    count_error++;
                    continue;
                }
                else {
                    smmdocs.add(smmdoc);
                    count++;
                    System.out.println("ID: " + currentID + ", Crawled: " + count + ", Error: " + count_error);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (smmdocs.size() == BATCH_SIZE){
                calender = Calendar.getInstance();
                String file = path + dateFormat.format(calender.getTime()) + ".xml";
                XMLWriter.writeToFile(smmdocs, file);
                smmdocs.clear();
                endTime = System.currentTimeMillis();
                System.out.println("Crawled " + BATCH_SIZE +" documents in " + (endTime - startTime)/60000.0 + " minutes");
                startTime = endTime;
            }
            // if (currentID - startID >= 50) break;   
        }
      
        // long endTime = System.currentTimeMillis();
        /*
        System.out.println("Crawled " + count + " articles "); 
        System.out.println("Elapsed time: " + (endTime - startTime)/1000.0 + " seconds");
        System.out.println("Error count: " + count_error);
        */
    }
    
    public static void main(String[] args) {
        start();
    }
}
