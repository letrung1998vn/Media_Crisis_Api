package fpt.capstone.betatest.services;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import fpt.capstone.betatest.entities.Keyword;
import fpt.capstone.betatest.repositories.KeywordRepository;

@Service
public class KeywordService {
	@Autowired
	private KeywordRepository keywordsRepository;
	
	@Transactional
	public Page<Keyword> keywordPaging(int Page) {
		Pageable page = PageRequest.of((Page - 1), 10);
		return keywordsRepository.findAll(page);
	}
	
	@Transactional
	public Page<Keyword> searchKeyword(String keyword, int Page) {
		Pageable page = PageRequest.of((Page - 1), 10);
		return keywordsRepository.findByKeywordContaining(keyword, page);
	}
	
	@Transactional
	public Page<Keyword> searchKeywordByUserIdAndKeywordContain(String keyword, String userId, int Page) {
		Pageable page = PageRequest.of((Page - 1), 10);
		return keywordsRepository.findByKeywordContainingAndUserIdContaining(keyword, userId, page);
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
	public List<String> getAllUserHaveKeyword() {
		return keywordsRepository.findAllUserHaveKeyword();
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
