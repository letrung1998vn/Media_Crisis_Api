package fpt.capstone.betatest.model;

import java.util.List;

public class EmailListContent {
	String keyword;
	List<String> listContentAndLink;
	List<String> listRatio;
	public String getKeyword() {
		return keyword;
	}
	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}
	public List<String> getListContentAndLink() {
		return listContentAndLink;
	}
	public void setListContentAndLink(List<String> listContentAndLink) {
		this.listContentAndLink = listContentAndLink;
	}
	public List<String> getListRatio() {
		return listRatio;
	}
	public void setListRatio(List<String> listRatio) {
		this.listRatio = listRatio;
	}
	
}
