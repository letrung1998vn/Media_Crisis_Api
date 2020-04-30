
package fpt.capstone.betatest.controller;

import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import fpt.capstone.betatest.services.CheckService;

@RestController
@RequestMapping("/checkMeaning")

public class CheckMeaningController {
	@Autowired
	CheckService check;

	@GetMapping("check")
	public void checkMeaning() throws Exception {
		Properties props = new Properties();
		props.setProperty("annotators", "tokenize, ssplit, pos, lemma, parse, ner, sentiment");
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
		check.setData(pipeline);
		check.start();
	}

}
