package fpt.capstone.betatest.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fpt.capstone.betatest.entities.Keyword_Crawler;

@Repository("keywordCrawlerRepository")
public interface KeywordCrawlerRepository extends JpaRepository<Keyword_Crawler, Integer>{
Keyword_Crawler findByKeyword(String keyword);

}