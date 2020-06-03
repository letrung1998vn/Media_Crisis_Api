package fpt.capstone.betatest.services;

import java.math.BigInteger;
import java.util.Date;
import java.util.Calendar;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fpt.capstone.betatest.entities.NotificationToken;
import fpt.capstone.betatest.entities.Post;
import fpt.capstone.betatest.entities.User;
import fpt.capstone.betatest.model.MessageOutputModel;
import fpt.capstone.betatest.repositories.NotificationTokenRepository;

@Service
public class NotificationTokenService {
	static final long ONE_MINUTE_IN_MILLIS=60000;//millisecs
	@Autowired
	private NotificationTokenRepository notificationTokenRepository;

	@Transactional
	public List<NotificationToken> getNotiTokenByUserId(String userId) {
		return notificationTokenRepository.findByUserName(userId);
	}

	@Transactional
	public void saveToken(NotificationToken notiToken) {
		notificationTokenRepository.save(notiToken);
	}

	@Transactional
	public NotificationToken getNotiTokenByUserIdAndNotiToken(String userId, String notiToken) {
		return notificationTokenRepository.findByUserNameAndNotiToken(userId, notiToken);
	}

	@Transactional
	public void deleteToken(NotificationToken notiToken) {
		notificationTokenRepository.delete(notiToken);
	}

	@Transactional
	public MessageOutputModel updateNotiToken(User user, String username, String token) {
		MessageOutputModel mod = new MessageOutputModel();
		if (user.isAvailable()) {
			NotificationToken notiToken = this.getNotiTokenByUserIdAndNotiToken(username, token);
			if (notiToken == null) {
				notiToken = new NotificationToken();
				notiToken.setNotiToken(token);
				notiToken.setUserName(username);
				notiToken.setAvailable(true);
				this.saveToken(notiToken);
				mod.setStatusCode(2);
				mod.setStatusMessage("Regist notification for browser success!");
			} else {
				notiToken.setAvailable(true);
				this.saveToken(notiToken);
				mod.setStatusCode(2);
				mod.setStatusMessage("Regist notification for browser success!");
			}

		} else {
			mod.setStatusCode(3);
			mod.setStatusMessage("Your account has been banned permanently, please contact admin for more infomation!");
		}
		return mod;
	}
	
	@Transactional
	public NotificationToken addMoreTimeForToken(String token) {
		NotificationToken updateToken = notificationTokenRepository.findByNotiToken(token);
		Calendar date = Calendar.getInstance();
		long t= date.getTimeInMillis();
		Date afterAddingMins=new Date(t + (30 * ONE_MINUTE_IN_MILLIS));
		//System.out.println(afterAddingMins.toString());
		updateToken.setActiveTime(afterAddingMins);
		return notificationTokenRepository.save(updateToken);
	}
	
	@Transactional
	public NotificationToken addNewToken(String tokenString, String username) {
		NotificationToken token = new NotificationToken();
		token.setNotiToken(tokenString);
		token.setAvailable(true);
		token.setUserName(username);
		Date date = Calendar.getInstance().getTime();
		token.setActiveTime(date);
		return notificationTokenRepository.save(token);
	}

	@Transactional
	public MessageOutputModel checkExist(String username, String token) {
		MessageOutputModel mod = new MessageOutputModel();
		mod.setStatusCode(0);
		NotificationToken notiToken = this.getNotiTokenByUserIdAndNotiToken(username, token);
		if (notiToken != null) {
			if (notiToken.isAvailable()) {
				mod.setStatusCode(1);
			} else {
				mod.setStatusCode(5);
			}
		}
		return mod;
	}

	@Transactional
	public MessageOutputModel disableNotiToken(User user, String username, String token) {
		MessageOutputModel mod = new MessageOutputModel();
		if (user.isAvailable()) {
			NotificationToken notiToken = this.getNotiTokenByUserIdAndNotiToken(username, token);
			notiToken.setAvailable(false);
			this.saveToken(notiToken);
			mod.setStatusCode(2);
			mod.setStatusMessage("Disable notification for browser success!");
		} else {
			mod.setStatusCode(3);
			mod.setStatusMessage("Your account has been banned permanently, please contact admin for more infomation!");
		}
		return mod;
	}

}
