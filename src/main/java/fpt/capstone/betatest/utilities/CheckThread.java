package fpt.capstone.betatest.utilities;



import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.aylien.textapi.TextAPIClient;

import fpt.capstone.betatest.entities.Crisis;
import fpt.capstone.betatest.entities.Keyword_Crawler;
import fpt.capstone.betatest.services.CheckMeaningService;
import fpt.capstone.betatest.services.KeywordCrawlerService;
import fpt.capstone.betatest.services.NotificationService;

public class CheckThread extends Thread {
	
	@Autowired
	private CheckMeaningService checkMeaningService;
	
	@Autowired
	private NotificationService notificationService;
	
	@Autowired
	private KeywordCrawlerService keywordCrawlerService;
	
	TextAPIClient client;
	

	public CheckThread(TextAPIClient client) {
		this.client = client;
	}

	@Override
	public synchronized void start() {
		List<Keyword_Crawler> listKeyword = keywordCrawlerService.getAllKeyword();
		List<Crisis> listCrisis  = new ArrayList<>();
		for (int i = 0; i < listKeyword.size(); i++) {
			try {
				Keyword_Crawler keyword = listKeyword.get(i);
				checkMeaningService.calStandard(keyword.getKeyword());
				checkMeaningService.detectCrisisInCurrent(listKeyword.get(i).getKeyword(), client, listCrisis);
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		this.interrupt();
	}
}
