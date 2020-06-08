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
	public NotificationToken findByToken(String token, String username) {
		return notificationTokenRepository.findByUserNameAndNotiToken(username, token);
	}
	
	@Transactional
	public boolean addMoreTimeForToken(String token, String username) {
		boolean result = false;
		NotificationToken updateToken = notificationTokenRepository.findByUserNameAndNotiToken(username, token);
		long t = updateToken.getActiveTime().getTime();
		Date activeTime = new Date(t);
		Date activeTimePlus30Min = new Date(t + (30 * ONE_MINUTE_IN_MILLIS));
		Calendar date = Calendar.getInstance();
//		System.out.println("Db Time: " + activeTime);
//		System.out.println("Db Time + 30 min: " + activeTimePlus30Min);
//		System.out.println("Now: " + date.getTime());
//		System.out.println(date.getTime().compareTo(activeTime));
//		System.out.println(date.getTime().compareTo(activeTimePlus30Min));
		if ((date.getTime().compareTo(activeTimePlus30Min) == -1) && (date.getTime().compareTo(activeTime) == 1)) {
			updateToken.setActiveTime(date.getTime());
			result = true;
		}
		notificationTokenRepository.save(updateToken);
		return result;
	}
	
	@Transactional
	public NotificationToken disableToken(String tokenString, String username) {
		NotificationToken token = notificationTokenRepository.findByUserNameAndNotiToken(username, tokenString);
		token.setAvailable(false);
		return notificationTokenRepository.save(token);
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
	public void disableUnnecessaryToken(String tokenString, String username) {
		List<NotificationToken> listNotiToken = notificationTokenRepository.findByUserName(username);
		for (NotificationToken notificationToken : listNotiToken) {
			disableToken(notificationToken.getNotiToken(), username);
		}
		listNotiToken = notificationTokenRepository.findByNotiToken(tokenString);
		for (NotificationToken notificationToken : listNotiToken) {
			disableToken(notificationToken.getNotiToken(), username);
		}
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
