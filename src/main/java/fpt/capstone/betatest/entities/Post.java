package fpt.capstone.betatest.entities;

import java.io.Serializable;
import javax.persistence.*;
import java.sql.Timestamp;
import java.util.List;


/**
 * The persistent class for the Post database table.
 * 
 */
@Entity
@Table(name="[Post]")
@NamedQuery(name="Post.findAll", query="SELECT p FROM Post p")
public class Post implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="Id")
	private String id;

	@Column(name="Content")
	private String content;

	@Column(name="CrawlDate")
	private Timestamp crawlDate;

	@Column(name="Link_Detail")
	private String link_Detail;

	@Column(name="Number_Of_Comment")
	private double number_Of_Comment;

	@Column(name="Number_of_Share")
	private double number_of_Share;

	@Column(name="Numbet_Of_React")
	private double numbet_Of_React;

	//bi-directional many-to-one association to Comment
	@OneToMany(mappedBy="post")
	private List<Comment> comments;

	//bi-directional many-to-one association to Keyword
	@ManyToOne
	@JoinColumn(name="Keyword")
	private Keyword keywordBean;

	public Post() {
	}

	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getContent() {
		return this.content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Timestamp getCrawlDate() {
		return this.crawlDate;
	}

	public void setCrawlDate(Timestamp crawlDate) {
		this.crawlDate = crawlDate;
	}

	public String getLink_Detail() {
		return this.link_Detail;
	}

	public void setLink_Detail(String link_Detail) {
		this.link_Detail = link_Detail;
	}

	public double getNumber_Of_Comment() {
		return this.number_Of_Comment;
	}

	public void setNumber_Of_Comment(double number_Of_Comment) {
		this.number_Of_Comment = number_Of_Comment;
	}

	public double getNumber_of_Share() {
		return this.number_of_Share;
	}

	public void setNumber_of_Share(double number_of_Share) {
		this.number_of_Share = number_of_Share;
	}

	public double getNumbet_Of_React() {
		return this.numbet_Of_React;
	}

	public void setNumbet_Of_React(double numbet_Of_React) {
		this.numbet_Of_React = numbet_Of_React;
	}

	public List<Comment> getComments() {
		return this.comments;
	}

	public void setComments(List<Comment> comments) {
		this.comments = comments;
	}

	public Comment addComment(Comment comment) {
		getComments().add(comment);
		comment.setPost(this);

		return comment;
	}

	public Comment removeComment(Comment comment) {
		getComments().remove(comment);
		comment.setPost(null);

		return comment;
	}

	public Keyword getKeywordBean() {
		return this.keywordBean;
	}

	public void setKeywordBean(Keyword keywordBean) {
		this.keywordBean = keywordBean;
	}

}