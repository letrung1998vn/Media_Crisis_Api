package fpt.capstone.betatest.model;

import java.util.List;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

public class Webhook {
	String url;
    String jsonString;
    
    public Webhook(String url, String jsonString) {
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
