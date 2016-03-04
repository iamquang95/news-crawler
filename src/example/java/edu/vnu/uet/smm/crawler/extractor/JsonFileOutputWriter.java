package edu.vnu.uet.smm.crawler.extractor;

import java.io.File;
import java.nio.charset.Charset;

import edu.vnu.uet.smm.common.base.SMMDocument;
import edu.vnu.uet.smm.common.util.FileHelper;

public class JsonFileOutputWriter implements OutputWriter<SMMDocument> {
	String outFolder;

	public JsonFileOutputWriter(String outFolder) {
		this.outFolder = outFolder;
	}

	public void write(SMMDocument doc) {
		try {
			FileHelper.writeToFile(doc.printJson(), new File(outFolder + "/" + doc.getId() + ".json"),
					Charset.forName("UTF-8"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
