package edu.vnu.uet.smm.crawler.util;

import java.util.List;

import edu.vnu.uet.smm.nlp.vntextpro.util.StrUtil;
import edu.vnu.uet.smm.nlp.vntextpro.vnsentsegmenter.VnSentSegmenter;
import edu.vnu.uet.smm.nlp.vntextpro.vntokenizer.VnTokenizer;
import edu.vnu.uet.smm.nlp.vntextpro.vnwordsegmenter.VnWordSegmenter;
import edu.vnu.uet.smm.nlp.vtools.vnpostagger.VnPOSTaggerSingleton;

public class VnTextProExample {

	public static void main(String[] args) throws Exception {
		VnSentSegmenter sentSegmenter = new VnSentSegmenter();
		sentSegmenter.init();

		VnWordSegmenter wordSegmenter = new VnWordSegmenter();
		wordSegmenter.init();

		String text = "Chuẩn mực!";

		List<String> sents = sentSegmenter.segment(text);
		for (String sent : sents) {
			System.out.println(sent);

			List<String> tokens = wordSegmenter.segment(StrUtil.tokenizeStr(VnTokenizer.tokenize(sent)));
			String[] tags = VnPOSTaggerSingleton.getInstance().tag(tokens.toArray(new String[tokens.size()]));
			
			for (int i = 0; i < tokens.size(); i++)
				System.out.print(tokens.get(i) + "/" + tags[i] + " ");
			System.out.println();
		}
	}

}
