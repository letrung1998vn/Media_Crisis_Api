package fpt.capstone.betatest.model;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class NotificationWebModel {
	String url;
	String jsonString;

	public NotificationWebModel(String url, String jsonString) {
		this.url = url;
		this.jsonString = jsonString;
	}

	public String connect() {
		String output = "";
		try {
			URL urlForGetRequest = new URL(url);
			String readLine = null;
			HttpURLConnection connection = (HttpURLConnection) urlForGetRequest.openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/json; utf-8");
			connection.setRequestProperty("Authorization",
					"key=AAAAp-dnpL8:APA91bFDDPB76FpvMZ-mROjiZIxPXSzpuIqfxUt9icD1llXhM1mZ4GKzkNVqCGoY88FDKiuolfhkbxYGWe3s_Ku7Z22h0gjat6cqav4UbDYcGos5wZmcoQhCeSaJAbjbLM1CAh527Uyt");
			connection.setDoOutput(true);
			String jsonInputString = jsonString;
			try (OutputStream os = connection.getOutputStream()) {
				byte[] input = jsonInputString.getBytes("utf-8");
				os.write(input, 0, input.length);
			}
			int responeCod = connection.getResponseCode();
			StringBuffer rp = new StringBuffer();

			if (responeCod == HttpURLConnection.HTTP_OK) {
				output = "OK!!!";
			}
		} catch (Exception e) {
			System.out.println("Error at call webhook");
			e.printStackTrace();
		}
		return output;
	}
}
