package fpt.capstone.betatest.services;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fpt.capstone.betatest.entities.NegativeRatio;
import fpt.capstone.betatest.entities.Post;
import fpt.capstone.betatest.model.EmailListContent;
import fpt.capstone.betatest.model.HistoryRatioModel;
import fpt.capstone.betatest.model.LinkDetailModel;
import fpt.capstone.betatest.repositories.NegativeRatioRepository;
import fpt.capstone.betatest.repositories.NotificationContentRepository;

@Service
public class NegativeRatioService {
	public final String detectTypeReact = "react";
	public final String detectTypeShare = "retweet";
	public final String detectTypeComment = "reply";
	public final String detectTypeIncreaseReact = "increaseReact";
	public final String detectTypeIncreaseShare = "increaseRetweet";
	public final String detectTypeIncreasComment = "increaseReply";
	public final String postType = "post";
	public final String commentType = "comment";
	@Autowired
	private NegativeRatioRepository negativeRatioRepository;
	@Autowired
	private NotificationContentRepository notificationContentRepository;
	@Autowired
	private PostService postService;

	@Transactional
	public List<NegativeRatio> getNegativeRatio(String keyword, String type) {
		return negativeRatioRepository.findByKeywordAndTypeOrderByUpdateDateDesc(keyword, type);
	}

	@Transactional
	public List<NegativeRatio> getNegativeRatioByDateAsc(String keyword, String type, Date date) {
		return negativeRatioRepository.findByKeywordAndTypeAndUpdateDateLessThanEqualOrderByUpdateDateAsc(keyword, type,
				date);
	}

	@Transactional
	public void save(NegativeRatio negativeRatio) {
		negativeRatioRepository.save(negativeRatio);
	}

	@Transactional
	public double calNegativeRatio(int listPost, int listPostNegative) {
		return ((double) listPostNegative / (double) listPost);
	}

	private String formatLinkDetail(String link) {
		link = link.replace("', '", "");
		link = link.replace("', ", "");
		link = link.replace("'", "");
		link = link.replace("(", "");
		link = link.replace(")", "");
		return link;
	}

	@Transactional
	public HistoryRatioModel getPostRatio(String keyword) throws Exception {
		HistoryRatioModel hrm = new HistoryRatioModel();
		List<NegativeRatio> lastNegativeRatio = getNegativeRatio(keyword, postType);
		hrm.setKeyword(keyword);
		hrm.setType(postType);
		List<String> listRatio = new ArrayList<>();
		if (lastNegativeRatio.size() > 0) {
			for (int i = 0; i < lastNegativeRatio.size(); i++) {
				NegativeRatio lnr = lastNegativeRatio.get(i);
				String ratio = lnr.getRatio() + "and||and" + lnr.getUpdateDate();
				listRatio.add(ratio);
			}
		}
		hrm.setListRatio(listRatio);
		return hrm;
	}
	@Transactional
	public HistoryRatioModel getCommentRatio(String keyword) throws Exception {
		HistoryRatioModel hrm = new HistoryRatioModel();
		List<NegativeRatio> lastNegativeRatio = getNegativeRatio(keyword, commentType);
		hrm.setKeyword(keyword);
		hrm.setType(commentType);
		List<String> listRatio = new ArrayList<>();
		if (lastNegativeRatio.size() > 0) {
			for (int i = 0; i < lastNegativeRatio.size(); i++) {
				NegativeRatio lnr = lastNegativeRatio.get(i);
				String ratio = lnr.getRatio() + "and||and" + lnr.getUpdateDate();
				listRatio.add(ratio);
			}
		}
		hrm.setListRatio(listRatio);
		return hrm;
	}
}
