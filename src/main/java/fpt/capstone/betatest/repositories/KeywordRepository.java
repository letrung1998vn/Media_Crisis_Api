package fpt.capstone.betatest.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import fpt.capstone.betatest.entities.Keyword;
import fpt.capstone.betatest.entities.User;

@Repository("keywordsRepository")
public interface KeywordRepository extends JpaRepository<Keyword, Integer> {

//	public final static String GET_KEYWORD_BY_USERID_AND_KEYWORD = "SELECT kw FROM Keyword kw WHERE kw.Keyword = :keyword AND kw.UserId = :userId";
//
//	@Query(GET_KEYWORD_BY_USERID_AND_KEYWORD)
//	Keyword checkKeywordExist(@Param("keyword") String keyword, @Param("userId") String userId);
	
	Page<Keyword> findByKeywordContainingAndAvailable(String keyword, Pageable pageable, Boolean available);
	Page<Keyword> findByUserAndKeywordContainingAndAvailable(User user, String keyword, Pageable pageable, Boolean available);

//	Keyword findByUserIdAndKeyword(String keyword, String userId);
//	
	Keyword findById(int id);
//	
	List<Keyword> findByUser(User user);
//	
	@Query("SELECT user from Keyword")
    List<User> findAllUserHaveKeyword();
//	Keyword findUserAndKeywordById(int id);
	 
}
