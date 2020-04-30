package fpt.capstone.betatest.services;

import java.util.ArrayList;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aylien.textapi.TextAPIClient;

import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import fpt.capstone.betatest.entities.Crisis;
import fpt.capstone.betatest.entities.Keyword_Crawler;
import fpt.capstone.betatest.services.CheckMeaningService;
import fpt.capstone.betatest.services.KeywordCrawlerService;
import fpt.capstone.betatest.services.NotificationService;

@Service
public class CheckService extends Thread {

	@Autowired
	private CheckMeaningService checkMeaningService;

	@Autowired
	private NotificationService notificationService;

	@Autowired
	private KeywordCrawlerService keywordCrawlerService;
	StanfordCoreNLP pipeline;

	public void setData(StanfordCoreNLP pipeline) {
		this.pipeline = pipeline;
	}

	@Override
	public synchronized void start() {
		try {
			int a = 0;
			while (true) {
				System.out.println("Start " + a + "th");
				List<Keyword_Crawler> listKeyword = keywordCrawlerService.getAllKeyword();
				List<Crisis> listCrisis = new ArrayList<>();
				for (int i = 0; i < listKeyword.size(); i++) {
					try {
						Keyword_Crawler keyword = listKeyword.get(i);
						checkMeaningService.calStandard(keyword.getKeyword());
						listCrisis = new ArrayList<>();
						checkMeaningService.detectCrisisInCurrent(listKeyword.get(i).getKeyword(), pipeline, listCrisis);
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
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
