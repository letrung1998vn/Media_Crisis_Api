package fpt.capstone.betatest.entities;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "Post")
public class Post implements Serializable {
	@Id
	@Column(name = "uuid_post")
	private BigInteger id;
	
	@Column(name = "post_id")
	private BigInteger postId;
	
	@Column(name = "post_content")
	private String postContent;
	
	@Column(name = "create_date")
	private Date createDate;
	
	@Column(name = "link_detail")
	private String linkDetail;
	
	@Column(name = "number_of_react")
	private float numberOfReact;
	
	@Column(name = "number_of_retweet")
	private float numberOfReweet;
	
	@Column(name = "number_of_reply")
	private float numberOfReply;
	
	@Column(name = "crawl_date")
	private Date crawlDate;
	
	@Column(name = "keyword")
	private String keyword;

	public BigInteger getId() {
		return id;
	}

	public void setId(BigInteger id) {
		this.id = id;
	}

	public BigInteger getPostId() {
		return postId;
	}

	public void setPostId(BigInteger postId) {
		this.postId = postId;
	}

	public String getPostContent() {
		return postContent;
	}

	public void setPostContent(String postContent) {
		this.postContent = postContent;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public String getLinkDetail() {
		return linkDetail;
	}

	public void setLinkDetail(String linkDetail) {
		this.linkDetail = linkDetail;
	}

	public float getNumberOfReact() {
		return numberOfReact;
	}

	public void setNumberOfReact(float numberOfReact) {
		this.numberOfReact = numberOfReact;
	}

	public float getNumberOfReweet() {
		return numberOfReweet;
	}

	public void setNumberOfReweet(float numberOfReweet) {
		this.numberOfReweet = numberOfReweet;
	}

	public float getNumberOfReply() {
		return numberOfReply;
	}

	public void setNumberOfReply(float numberOfReply) {
		this.numberOfReply = numberOfReply;
	}

	public Date getCrawlDate() {
		return crawlDate;
	}

	public void setCrawlDate(Date crawlDate) {
		this.crawlDate = crawlDate;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}
	
	
}
