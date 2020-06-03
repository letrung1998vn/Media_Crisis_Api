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
	private String id;

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

	@Column(name = "isNew")
	private boolean isNew;

	@Column(name = "isNegative")
	private Boolean isNegative;
	
	@Column(name = "language")
	private String language;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
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

	public boolean isNew() {
		return isNew;
	}

	public void setNew(boolean isNew) {
		this.isNew = isNew;
	}

	public Boolean isNegative() {
		return isNegative;
	}

	public void setNegative(Boolean isNegative) {
		this.isNegative = isNegative;
	}

	public Boolean getIsNegative() {
		return isNegative;
	}

	public void setIsNegative(Boolean isNegative) {
		this.isNegative = isNegative;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	@Override
	public String toString() {
		return "Post [id=" + id + ", postId=" + postId + ", postContent=" + postContent + ", createDate=" + createDate
				+ ", linkDetail=" + linkDetail + ", numberOfReact=" + numberOfReact + ", numberOfReweet="
				+ numberOfReweet + ", numberOfReply=" + numberOfReply + ", crawlDate=" + crawlDate + ", keyword="
				+ keyword + ", isNew=" + isNew + ", isNegative=" + isNegative + ", language=" + language + "]";
	}

	
}
