package fpt.capstone.betatest.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import fpt.capstone.betatest.entities.Keyword_Crawler;

@Repository("keywordCrawlerRepository")
public interface KeywordCrawlerRepository extends JpaRepository<Keyword_Crawler, Integer> {
	Keyword_Crawler findByKeyword(String keyword);
	@Query(nativeQuery=true, value="select * from Keyword_Crawler")
	List<Keyword_Crawler> getAllKeyword();
}