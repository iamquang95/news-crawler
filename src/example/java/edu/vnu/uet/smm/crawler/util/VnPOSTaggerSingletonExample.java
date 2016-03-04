package edu.vnu.uet.smm.crawler.util;

import java.util.List;

import edu.vnu.uet.smm.nlp.vtools.vnpostagger.VnPOSTaggerSingleton;
import opennlp.tools.tokenize.WhitespaceTokenizer;

public class VnPOSTaggerSingletonExample {

	public static void main(String[] args) throws Exception {
		String sentenceString = "1 . Bản_chất của Nhà_nước ta là nhà_nước của nhân_dân , do nhân_dân , vì nhân_dân , là sự thể_hiện quy�?n làm_chủ của nhân_dân dưới sự lãnh_đạo của �?ảng .";
		String[] tokens = WhitespaceTokenizer.INSTANCE.tokenize(sentenceString);
		String[] tags = VnPOSTaggerSingleton.getInstance().tag(tokens);

		for (int i = 0; i < tokens.length; i++)
			System.out.print(tokens[i] + "/" + tags[i] + " ");
		System.out.println();

		List<String> kws = VnPOSTaggerSingleton.getInstance().getKeywords(tokens);
		System.out.println(kws);

	}

}
