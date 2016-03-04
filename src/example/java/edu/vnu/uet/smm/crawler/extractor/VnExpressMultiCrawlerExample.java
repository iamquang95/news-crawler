package edu.vnu.uet.smm.crawler.extractor;

public class VnExpressMultiCrawlerExample {

	public static void main(String[] args) throws Exception {
		VnExpressMultiCrawler crawler = new VnExpressMultiCrawler(new JsonFileOutputWriter("temp/data"));
		crawler.crawl();
	}

}
