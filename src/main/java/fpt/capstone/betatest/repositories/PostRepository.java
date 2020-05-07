package fpt.capstone.betatest.repositories;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import fpt.capstone.betatest.entities.Keyword;
import fpt.capstone.betatest.entities.Post;

@Repository("postRepository")
public interface PostRepository extends JpaRepository<Post, Integer> {
	@Query(nativeQuery = true, value = "select p.uuid_post, p.post_id, p.post_content, p.create_date, p.link_detail, "
			+ "p.number_of_react, p.number_of_retweet, p.number_of_reply, p.crawl_date, p.keyword, p.isNew, p.isNegative from Post p,"
			+ " (select uuid_post, MAX(crawl_date) as date from Post GROUP BY uuid_post) w "
			+ "where p.uuid_post = w.uuid_post and p.crawl_date = w.date and keyword = ?1")
	List<Post> getEachPostContentWithLatestDate(String keyword);

	@Query(nativeQuery = true, value = "select uuid_post, post_id, post_content, create_date, link_detail, "
			+ "number_of_react, number_of_retweet, number_of_reply, crawl_date, "
			+ "keyword, isNew, isNegative from Post where keyword=?1 order by post_content")
	List<Post> getPostContentWithTwoLatestDate(String keyword);

	List<Post> getByPostId(BigInteger id);

	Post getById(String id);

	List<Post> findByKeywordAndIsNew(String keyword, Boolean isNew);

	@Query(nativeQuery = true, value = "select TOP 1 uuid_post, post_id, post_content, "
			+ "create_date, link_detail, number_of_react, number_of_retweet, number_of_reply, "
			+ "crawl_date, keyword, isNew, isNegative from Post where crawl_date < ?1 "
			+ "and post_id = ?2 order by crawl_date asc")
	Post getSecondLastNewPost(Date crawlDate, BigInteger postId);
	Page<Post> findByIsNewOrderByCrawlDateDesc(boolean isNew, Pageable pageable);
	Page<Post> findByIsNegativeOrderByCrawlDateDesc(boolean isNegative, Pageable pageable);
}
