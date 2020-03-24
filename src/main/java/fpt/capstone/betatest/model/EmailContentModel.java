package fpt.capstone.betatest.model;

import java.util.List;

import fpt.capstone.betatest.entities.Keyword;

public class EmailContentModel {
	String keyword;
	List<String> listLinkDetail;
	
	public String getKeyword() {
		return keyword;
	}
	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}
	public List<String> getListLinkDetail() {
		return listLinkDetail;
	}
	public void setListLinkDetail(List<String> listLinkDetail) {
		this.listLinkDetail = listLinkDetail;
	}
	
}
