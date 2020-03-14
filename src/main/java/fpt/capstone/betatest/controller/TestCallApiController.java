package fpt.capstone.betatest.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aylien.textapi.TextAPIClient;
import com.aylien.textapi.parameters.*;
import com.aylien.textapi.responses.EntitiesSentiment;
import com.aylien.textapi.responses.Sentiment;

//import fpt.capstone.betatest.entities.Post;
import fpt.capstone.betatest.services.TestCallApiService;

@RestController
@RequestMapping("/testCallApi")
public class TestCallApiController {
	@Autowired
	TestCallApiService apiService;

//	@GetMapping("call")
//	public List<EntitiesSentiment> getData(@RequestBody Post data) throws Exception {
//		TextAPIClient client = new TextAPIClient("43faa103", "f2aaee05b21dabe934b89bd3198801e8");
//		EntityLevelSentimentParams.Builder builder = EntityLevelSentimentParams.newBuilder();
//		List<EntitiesSentiment> entitySentimentList = new ArrayList<>();
//		builder.setText(data.getContent());
//		EntitiesSentiment elsa = client.entityLevelSentiment(builder.build());
//		entitySentimentList.add(elsa);
//		builder.setText(data.getComments().get(0).getContent());
//		elsa = client.entityLevelSentiment(builder.build());
//		entitySentimentList.add(elsa);
//		return entitySentimentList;
//	}
}
