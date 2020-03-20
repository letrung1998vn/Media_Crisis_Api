package fpt.capstone.betatest.services;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fpt.capstone.betatest.entities.Keyword_Crawler;
import fpt.capstone.betatest.repositories.KeywordCrawlerRepository;

@Service
public class KeywordCrawlerService {
	@Autowired
	private KeywordCrawlerRepository keywordCrawlerRepository;

	@Transactional
	public Keyword_Crawler saveKeyword(Keyword_Crawler kw) {
		return keywordCrawlerRepository.save(kw);
	}

	@Transactional
	public boolean checkExist(String keyword) {
		boolean existed = false;
		Keyword_Crawler kc = keywordCrawlerRepository.findByKeyword(keyword);
		if (kc != null) {
			existed = true;
		}
		return existed;
	}

	@Transactional
	public List<Keyword_Crawler> getAllKeyword() {
		return keywordCrawlerRepository.getAllKeyword();
	}
}
