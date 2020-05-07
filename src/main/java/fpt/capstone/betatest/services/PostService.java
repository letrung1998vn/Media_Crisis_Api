package fpt.capstone.betatest.services;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import fpt.capstone.betatest.entities.Post;
import fpt.capstone.betatest.repositories.PostRepository;

@Service
public class PostService {
	@Autowired
	private PostRepository postRepository;

	@Autowired
	private CheckMeaningService checkMeaningService;
	
	final long diffenrentDate = 7;
	
	@Transactional
	public List<Post> getEachPostContentWithLatestDate(String keyword) {
		return postRepository.getEachPostContentWithLatestDate(keyword);
	}

	@Transactional
	public List<Post> getPostContentWithTwoLatestDate(String keyword) {
		return postRepository.getPostContentWithTwoLatestDate(keyword);
	}

	@Transactional
	public List<Post> getPostByPostId(BigInteger id) {
		return postRepository.getByPostId(id);
	}

	@Transactional
	public Post getPostById(String id) {
		return postRepository.getById(id);
	}

	@Transactional
	public List<Post> getNewPost(String keyword, Boolean isNew) {
		return postRepository.findByKeywordAndIsNew(keyword, isNew);
	}
	
	@Transactional
	public Page<Post> getAllNewPost(Boolean isNew, int Page) {
		Pageable page = PageRequest.of((Page - 1), 20);
		return postRepository.findByIsNewOrderByCrawlDateDesc(isNew, page);
	}

	@Transactional
	public Page<Post> getAllNegativePost(Boolean isNegative, int Page) {
		Pageable page = PageRequest.of((Page - 1), 20);
		return postRepository.findByIsNegativeOrderByCrawlDateDesc(isNegative, page);
	}
	
	@Transactional
	public Post save(Post post) {
		return postRepository.save(post);
	}

	@Transactional
	public Post getSecondLastNewPost(Date crawlDate, BigInteger postId) {
		return postRepository.getSecondLastNewPost(crawlDate, postId);
	}
	
	@Transactional
	public List<Post> getIncreasePost(String keyword) {
		// Get the list of post with two latest date in DB
		List<Post> listPost = this.getPostContentWithTwoLatestDate(keyword);
		List<Post> resultList = new ArrayList<>();
		List<Post> sameContentPost = new ArrayList<>();
		List<Post> sameContentPostSorted = new ArrayList<>();
		for (int i = 0; i < listPost.size(); i++) {
			if (i < listPost.size() - 1) {
				Post post = listPost.get(i);
				sameContentPost = checkMeaningService.getListSameContent(listPost, post);
				sameContentPostSorted = checkMeaningService.sortByCrawlDate(sameContentPost);
				if (!checkMeaningService.checkExist(resultList, sameContentPostSorted.get(0).getPostId())
						&& sameContentPostSorted.size() == 2) {
					resultList.addAll(sameContentPostSorted);
				}
			}
		}
		return resultList;
	}
	
	@Transactional
	public List<Post> getRecentPost(String keyword) {
		// Get The list of post with latest date in DB
		List<Post> posts = this.getEachPostContentWithLatestDate(keyword);
		List<Post> returnList = new ArrayList<>();
		for (int i = 0; i < posts.size(); i++) {
			Post post = posts.get(i);
			long millis = System.currentTimeMillis();
			Date date = new Date(millis);
			long diffInMillies = Math.abs(date.getTime() - post.getCrawlDate().getTime());
			long diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
			if (diff < diffenrentDate) {
				returnList.add(post);
			}
		}
		return returnList;
	}

}
