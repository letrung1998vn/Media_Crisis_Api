package fpt.capstone.betatest.entities;

import java.io.Serializable;
import javax.persistence.*;
import java.sql.Timestamp;


/**
 * The persistent class for the Comment database table.
 * 
 */
@Entity
@Table(name="[Comment]")
public class Comment implements Serializable {
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

	@Column(name="Number_Of_Reply")
	private double number_Of_Reply;

	@Column(name="Numbet_Of_React")
	private double numbet_Of_React;

	//bi-directional many-to-one association to Post
	@ManyToOne
	@JoinColumn(name="PostId")
	private Post post;

	public Comment() {
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

	public double getNumber_Of_Reply() {
		return this.number_Of_Reply;
	}

	public void setNumber_Of_Reply(double number_Of_Reply) {
		this.number_Of_Reply = number_Of_Reply;
	}

	public double getNumbet_Of_React() {
		return this.numbet_Of_React;
	}

	public void setNumbet_Of_React(double numbet_Of_React) {
		this.numbet_Of_React = numbet_Of_React;
	}

	public Post getPost() {
		return this.post;
	}

	public void setPost(Post post) {
		this.post = post;
	}

}