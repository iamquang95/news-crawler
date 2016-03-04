package edu.vnu.uet.smm.crawler.extractor;

public interface OutputWriter<T> {
	public void write(T doc);
}
