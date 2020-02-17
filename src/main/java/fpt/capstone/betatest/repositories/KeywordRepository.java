package fpt.capstone.betatest.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import fpt.capstone.betatest.entities.Keyword;

@Repository("keywordsRepository")
public interface KeywordRepository extends JpaRepository<Keyword, Integer> {

//	public final static String GET_KEYWORD_BY_USERID_AND_KEYWORD = "SELECT kw FROM Keyword kw WHERE kw.Keyword = :keyword AND kw.UserId = :userId";
//
//	@Query(GET_KEYWORD_BY_USERID_AND_KEYWORD)
//	Keyword checkKeywordExist(@Param("keyword") String keyword, @Param("userId") String userId);
	
	List<Keyword> findByUserIdAndKeywordContaining(String userId, String keyword);

	Keyword findByUserIdAndKeyword(String keyword, String userId);
	
	List<Keyword> findByUserId(String userId);
	
//	Keyword findUserAndKeywordById(int id);
	 
}
