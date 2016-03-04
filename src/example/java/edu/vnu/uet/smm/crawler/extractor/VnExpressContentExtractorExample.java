package edu.vnu.uet.smm.crawler.extractor;

import java.io.IOException;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import edu.vnu.uet.smm.common.base.SMMDocument;

public class VnExpressContentExtractorExample {

	public static void main(String[] args) {
		try {
			Document doc = Jsoup.connect("http://ione.vnexpress.net/tin-tuc/nhip-song/nhung-co-gai-mat-mun-lot-xac-xinh-dep-sau-makeup-3338663.html").get();
			System.out.println(doc.baseUri());
			String title = VnExpressExtractor.extractTitle(doc);
			String summary = VnExpressExtractor.extractSummary(doc);
			String content = VnExpressExtractor.extractContent(doc);
			String category = VnExpressExtractor.extractCategory(doc);
			String date = VnExpressExtractor.extractDate(doc);
			System.out.println(title);
			System.out.println(category);
			System.out.println(date);
			String fmtdate = VnExpressExtractor.parseTime(date);
			if (fmtdate != null)
			System.out.println(fmtdate);
			System.out.println(summary);
			System.out.println(content);
			
			SMMDocument smmdoc = VnExpressExtractor.extract(doc, false);
			System.out.println(smmdoc.printJson());
			List<SMMDocument> smmcomments = VnExpressExtractor.extractComments(smmdoc, false);
			for (SMMDocument comment : smmcomments)
				System.out.println(comment.printJson());				
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
