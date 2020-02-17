package fpt.capstone.betatest.services;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fpt.capstone.betatest.entities.Keyword;
import fpt.capstone.betatest.repositories.KeywordRepository;

@Service
public class KeywordService {
	@Autowired
	private KeywordRepository keywordsRepository;
	
	@Transactional
	public List<Keyword> searchKeyword(String userId, String keyword) {
		return keywordsRepository.findByUserIdAndKeywordContaining(userId, keyword);
	}
	
	@Transactional
	public List<Keyword> getAllKeyword() {
		return keywordsRepository.findAll();
	}
	
	@Transactional
	public List<Keyword> getAll(String userId) {
		return keywordsRepository.findByUserId(userId);
	}
	
	@Transactional
	public Keyword getByKeywordAndUserId(String keyword, String userId) {
		return keywordsRepository.findByUserIdAndKeyword(keyword, userId);
	}
	
	@Transactional
	public Keyword saveKeyword(Keyword kw) {
		return keywordsRepository.save(kw);
	}
	
	@Transactional
	public void deleteKeyword(int id) {
//		id = keywordsRepository.findUserAndKeywordById(id).getId();
		keywordsRepository.deleteById(id);
	}

}
