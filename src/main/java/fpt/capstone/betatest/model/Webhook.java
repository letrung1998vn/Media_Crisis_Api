package fpt.capstone.betatest.model;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class Webhook {
	String url;
    String jsonString;
    
    public Webhook(String url, String jsonString) {
        this.url = url;
        this.jsonString = jsonString;
    }
    public Webhook(String url) {
        this.url = url;
    }
    public String connectWebServer() {
    	 String output = "Call Noti";
    	 try {
             URL urlForGetRequest = new URL(url);
             String readLine = null;
             HttpURLConnection connection = (HttpURLConnection) urlForGetRequest.openConnection();
         } catch (Exception e) {
             System.out.println("Error at call webhook");
             e.printStackTrace();
         }
    	 return output;
    }
    public String connect() {
        String output = "";
        try {
            URL urlForGetRequest = new URL(url);
            String readLine = null;
            HttpURLConnection connection = (HttpURLConnection) urlForGetRequest.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; utf-8");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);
            String jsonInputString = jsonString;
            try(OutputStream os = connection.getOutputStream()) {
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
