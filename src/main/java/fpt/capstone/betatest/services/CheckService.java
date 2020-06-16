package fpt.capstone.betatest.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import fpt.capstone.betatest.entities.Crisis;
import fpt.capstone.betatest.entities.Keyword_Crawler;

@Service
public class CheckService extends Thread {

	@Autowired
	private CheckMeaningService checkMeaningService;

	@Autowired
	private NotificationService notificationService;

	@Autowired
	private KeywordCrawlerService keywordCrawlerService;

	StanfordCoreNLP engSC;

	StanfordCoreNLP viSC;
	private boolean condition = true;
	private boolean running = false;

	public void setData(StanfordCoreNLP engSC, StanfordCoreNLP viSC) {
		this.engSC = engSC;
		this.viSC = viSC;
	}

	public boolean getRunningCondition() {
		return this.running;
	}

	public boolean getCondition() {
		return this.condition;
	}

	public void setCondition(boolean condition) {
		this.condition = condition;
	}

	@Override
	public synchronized void start() {
		try {
			int a = 0;
			while (condition) {
				running = true;
				System.out.println("Check condition:" + condition);
				System.out.println("Start " + a + "th");
				List<Keyword_Crawler> listKeyword = keywordCrawlerService.getAllKeyword();
				List<Crisis> listCrisis = new ArrayList<>();
				for (int i = 0; i < listKeyword.size(); i++) {
					try {
						Keyword_Crawler keyword = listKeyword.get(i);
						checkMeaningService.calStandard(keyword.getKeyword());
						listCrisis = new ArrayList<>();
						checkMeaningService.detectCrisisInCurrent(listKeyword.get(i).getKeyword(), engSC, viSC,
								listCrisis);
						if (listCrisis.size() > 0) {
							notificationService.sendNotification(listCrisis, listKeyword.get(i).getKeyword());
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				a++;
				System.out.println("Sleep " + a + "th");
				this.sleep(1000 * 60 * 2);
			}
			running = false;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void interrupt() {
		condition = false;
	}
}
