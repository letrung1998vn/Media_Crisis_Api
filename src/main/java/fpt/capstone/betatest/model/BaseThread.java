package fpt.capstone.betatest.model;

import java.util.List;

import com.aylien.textapi.TextAPIClient;

import fpt.capstone.betatest.entities.Comment;
import fpt.capstone.betatest.entities.Crisis;
import fpt.capstone.betatest.entities.Post;
import fpt.capstone.betatest.services.CommentService;
import fpt.capstone.betatest.services.CrisisService;
import fpt.capstone.betatest.services.KeywordService;
import fpt.capstone.betatest.services.NegativeRatioService;
import fpt.capstone.betatest.services.NotificationContentService;
import fpt.capstone.betatest.services.NotificationService;
import fpt.capstone.betatest.services.NotificationTokenService;
import fpt.capstone.betatest.services.PostService;
import fpt.capstone.betatest.services.UserInfoService;
import fpt.capstone.betatest.services.UserService;

public abstract class BaseThread extends Thread {
	public final double lowerConfidence = 0.5;
	public final String negative = "negative";
	public final int lowMean = 10000;
	public final int lowStandard = 5000;
	public final String type = "post";
	public int totalCount = 60;
	public int entity_sentiment_count = 3;
	public int sentiment_count = 1;
	public int countHit = 0;
	public TextAPIClient client;
	public String keyword;
	public List<Comment> listComment;
	public CrisisService crisisService;
	public KeywordService keywordService;
	public NotificationService notificationService;
	public NotificationContentService notificationContentService;
	public UserInfoService userInfoService;
	public UserService userService;
	public CommentService commentService;
	public PostService postService;
	public List<Crisis> listCrisis;
	public List<Post> listPost;
	public NotificationTokenService notificationTokenService;
	public NegativeRatioService negativeRatioService;
	public final double differenceHour = 8;
}
