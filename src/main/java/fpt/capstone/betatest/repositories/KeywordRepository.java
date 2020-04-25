package fpt.capstone.betatest.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import fpt.capstone.betatest.entities.Keyword;
import fpt.capstone.betatest.entities.User;

@Repository("keywordsRepository")
public interface KeywordRepository extends JpaRepository<Keyword, Integer> {


	Page<Keyword> findByKeywordContainingAndAvailable(String keyword, Pageable pageable, Boolean available);

	Page<Keyword> findByUserAndKeywordContainingAndAvailable(User user, String keyword, Pageable pageable,
			Boolean available);

	Keyword findById(int id);

	List<Keyword> findByUser(User user);

	@Query("SELECT user from Keyword")
	List<User> findAllUserHaveKeyword();

	@Query(nativeQuery = true, value = "SELECT * from Keyword")
	List<Keyword> getAllKeyword();
	
	List<Keyword> findByKeyword(String keyword);

}
