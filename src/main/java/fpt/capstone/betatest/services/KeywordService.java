package fpt.capstone.betatest.services;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import fpt.capstone.betatest.entities.Keyword;
import fpt.capstone.betatest.entities.User;
import fpt.capstone.betatest.repositories.KeywordRepository;

@Service
public class KeywordService {
	@Autowired
	private KeywordRepository keywordsRepository;
	
//	@Transactional
//	public Page<Keyword> keywordPaging(int Page) {
//		Pageable page = PageRequest.of((Page - 1), 10);
//		return keywordsRepository.findAll(page);
//	}
//	
	@Transactional
	public Page<Keyword> searchKeyword(String keyword, int Page) {
		Pageable page = PageRequest.of((Page - 1), 10);
		return keywordsRepository.findByKeywordContainingAndAvailable(keyword, page, true);
	}
	
	@Transactional
	public Page<Keyword> searchKeywordByUserAndKeywordContain(String keyword, User user, int Page) {
		Pageable page = PageRequest.of((Page - 1), 10);
		return keywordsRepository.findByUserAndKeywordContainingAndAvailable(user, keyword, page, true);
	}
	
	@Transactional
	public List<Keyword> getAllKeyword() {
		return keywordsRepository.findAll();
	}
	
	@Transactional
	public List<Keyword> getAll(User user) {
		return keywordsRepository.findByUser(user);
	}
	
	@Transactional
	public List<User> getAllUserHaveKeyword() {
		return keywordsRepository.findAllUserHaveKeyword();
	}
	
//	@Transactional
//	public Keyword getByKeywordAndUserId(String keyword, String userId) {
//		return keywordsRepository.findByUserIdAndKeyword(keyword, userId);
//	}
	
	@Transactional
	public Keyword saveKeyword(Keyword kw) {
		return keywordsRepository.save(kw);
	}
	
	@Transactional
	public Keyword getKeywordById(int id) {
		return keywordsRepository.findById(id);
	}
	
//	@Transactional
//	public Keyword updateKeyword(Keyword kw) {
//		return keywordsRepository.save(kw);
//	}
	
	@Transactional
	public void deleteKeyword(Keyword kw) {
		keywordsRepository.delete(kw);
	}
	
	@Transactional
	public List<Keyword> getUserByKeyword(String keyword) {
		return keywordsRepository.findByKeyword(keyword);
	}

}
