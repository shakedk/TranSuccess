package transSuccess.controller;

import org.apache.http.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import transSuccess.model.Trade;
import transSuccess.service.TransSuccessService;
import transSuccess.service.PublicApiService;

@ComponentScan("transSuccess")
@Controller
@EnableAutoConfiguration
public class TransSuccessController {

	@Autowired
	TransSuccessService btcService;
	@Autowired
	PublicApiService publicApiService;
	
	
    @RequestMapping("/btc")
    @ResponseBody
    String home() {  	
    	HttpResponse result = btcService.authenticatedHTTPRequest("getInfo",null);
    	if(result!=null)
    		return result.toString();
    	else return "Hello World!";    
    }
    
    @RequestMapping("/public/trade")
    @ResponseBody
    String trade() {  	
    	publicApiService.getPublicTrade();
    	return "trade Done";
    }
    
    

    public static void main(String[] args) throws Exception {
        SpringApplication.run(TransSuccessController.class, args);
    	//PublicApiService publicApiService = new PublicApiService();
    	//publicApiService.getPublicTrade();    
    }
}