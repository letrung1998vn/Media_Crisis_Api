package fpt.capstone.betatest.model;

import java.util.List;

import fpt.capstone.betatest.entities.Keyword;

public class TestModel {
	String keyword;
	List<String> listLinkDetail;
	public List<String> getListKeyWord() {
		return listLinkDetail;
	}
	public void setListKeyWord(List<String> listLinkDetail) {
		this.listLinkDetail = listLinkDetail;
	}
	public String getKeyword() {
		return keyword;
	}
	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}
	
}
