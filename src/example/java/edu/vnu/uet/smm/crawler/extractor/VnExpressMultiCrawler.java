package edu.vnu.uet.smm.crawler.extractor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import edu.vnu.uet.smm.common.base.SMMDocument;

public class VnExpressMultiCrawler {
	int NUM_THREAD = 6;
	int SIZE_POOL = 1000;
	int VNEXPRESS_STARTID = 2254012;
	int VNEXPRESS_ENDID = 3500000;
	int TIMEOUT = 3000;
	OutputWriter<SMMDocument> writer;

	public VnExpressMultiCrawler(OutputWriter<SMMDocument> writer) {
		this.writer = writer;
	}

	public VnExpressMultiCrawler(int numthread, int sizepool, int startid, int endid,
			OutputWriter<SMMDocument> writer) {
		this.NUM_THREAD = numthread;
		this.SIZE_POOL = sizepool;
		this.VNEXPRESS_STARTID = startid;
		this.VNEXPRESS_ENDID = endid;
		this.writer = writer;
	}

	public void crawl() throws Exception {
		int runner = 0;
		List<Integer> ids = new ArrayList<Integer>();

		for (int i = VNEXPRESS_STARTID; i <= VNEXPRESS_ENDID; i++) {
			runner++;
			ids.add(i);
			if (runner % SIZE_POOL == 0) {
				excuteList(ids, runner, writer);
				ids = new ArrayList<Integer>();
			}
		}
		excuteList(ids, runner, writer);
	}

	void excuteList(List<Integer> ids, int runner, OutputWriter<SMMDocument> writer) throws Exception {
		long startTime = System.currentTimeMillis();
		ExecutorService executor = Executors.newFixedThreadPool(NUM_THREAD);
		List<Future<SMMDocument>> list = new ArrayList<Future<SMMDocument>>();
		for (int id : ids) {
			Callable<SMMDocument> worker = new VnExpressIDFetchCallable(id);
			Future<SMMDocument> collector = executor.submit(worker);
			list.add(collector);
		}
		for (Future<SMMDocument> future : list) {
			try {
				SMMDocument doc = future.get(TIMEOUT, TimeUnit.MILLISECONDS);
				if (doc != null) {
					writer.write(doc);
				}
			} catch (TimeoutException e) {
				future.cancel(true);
			}
		}
		long currentTime = System.currentTimeMillis();
		System.out.println("Fetcher " + runner + "\t" + (currentTime - startTime) + "ms");
		startTime = currentTime;
		executor.shutdown();
	}
}
