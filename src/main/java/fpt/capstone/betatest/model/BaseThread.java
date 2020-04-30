package fpt.capstone.betatest.model;

import java.util.List;

import com.aylien.textapi.TextAPIClient;

import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import fpt.capstone.betatest.entities.Comment;
import fpt.capstone.betatest.entities.Crisis;
import fpt.capstone.betatest.entities.Post;


public abstract class BaseThread extends Thread {
	public final double lowerConfidence = 0.5;
	public final String negative = "negative";
	public final String postType = "post";
	public final String commentType = "comment";
	public StanfordCoreNLP pipeline;
	public String keyword;
	public List<Comment> listComment;
	public List<Crisis> listCrisis;
	public List<Post> listPost;
	public final double differenceHour = 8;
	public final double ratioLimit = 0.05;
}
