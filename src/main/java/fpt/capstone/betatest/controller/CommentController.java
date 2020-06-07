package fpt.capstone.betatest.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fpt.capstone.betatest.entities.Comment;
import fpt.capstone.betatest.entities.Post;
import fpt.capstone.betatest.services.CommentService;
import fpt.capstone.betatest.services.PostService;

@RestController
@RequestMapping("/comment")
public class CommentController {
	@Autowired
	CommentService commentService;
	@Autowired
	PostService postService;

	@PostMapping("getNewComment")
	public Page<Comment> getNewComment(@RequestParam(value = "page") String page) {
		return commentService.getAllNewComment(Integer.parseInt(page));
	}

	@PostMapping("getNegativeComment")
	public Page<Comment> getNegativeComment(@RequestParam(value = "page") String page) {
		return commentService.getAllNegativeComment(true, Integer.parseInt(page));
	}

	@PostMapping("getNegativeCommentByKeyword")
	public List<Comment> getNegativeCommentByKeyword(@RequestParam(value = "keyword") String keyword) {
		List<Post> list = postService.getNegativePostByKeyword(keyword);
		List<Comment> listComment = new ArrayList<>();
		for (int i = 0; i < list.size(); i++) {
			Post post = list.get(i);
			listComment.addAll(commentService.getNegativeCommentByPostId(post.getId()));
		}
		return listComment;
	}

}
