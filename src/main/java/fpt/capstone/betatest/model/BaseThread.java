package fpt.capstone.betatest.model;

import java.util.List;

import com.aylien.textapi.TextAPIClient;

import fpt.capstone.betatest.entities.Comment;
import fpt.capstone.betatest.entities.Crisis;
import fpt.capstone.betatest.entities.Post;


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
	public List<Crisis> listCrisis;
	public List<Post> listPost;
	public final double differenceHour = 8;
	public final double ratioLimit = 0.1;
	public void start(TextAPIClient client, String keyword, List<Post> listPost, List<Crisis> listCrisis) {
		// TODO Auto-generated method stub
		
	}
}
