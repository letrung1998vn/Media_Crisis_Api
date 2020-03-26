package fpt.capstone.betatest.entities;

import java.io.Serializable;
import java.math.BigInteger;
import java.sql.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "Comment")
public class Comment implements Serializable {
	@Id
	@Column(name = "uuid_comment")
	private String id;
	
	@Column(name = "comment_id")
	private BigInteger commentId;
	
	@Column(name = "comment_content")
	private String commentContent;
	
	@Column(name = "post_id")
	private BigInteger postId;
	
	@Column(name = "create_date")
	private Date createDate;
	
	@Column(name = "number_of_react")
	private float numberOfReact;
	
	@Column(name = "link_detail")
	private String linkDetail;
	
	@Column(name = "number_of_reply")
	private float numberOfReply;
	
	@Column(name = "crawl_date")
	private Date crawlDate;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public BigInteger getCommentId() {
		return commentId;
	}

	public void setCommentId(BigInteger commentId) {
		this.commentId = commentId;
	}

	public String getCommentContent() {
		return commentContent;
	}

	public void setCommentContent(String commentContent) {
		this.commentContent = commentContent;
	}

	public BigInteger getPostId() {
		return postId;
	}

	public void setPostId(BigInteger postId) {
		this.postId = postId;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public float getNumberOfReact() {
		return numberOfReact;
	}

	public void setNumberOfReact(float numberOfReact) {
		this.numberOfReact = numberOfReact;
	}

	public String getLinkDetail() {
		return linkDetail;
	}

	public void setLinkDetail(String linkDetail) {
		this.linkDetail = linkDetail;
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
}
