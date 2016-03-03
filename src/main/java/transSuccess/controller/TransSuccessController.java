package transSuccess.controller;

import org.apache.http.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import transSuccess.service.PublicApiService;
import transSuccess.service.TransSuccessService;

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
    
    @RequestMapping(value = "/map", method=RequestMethod.GET)
    //public String customerFormSubmit(@RequestParam(value = "fName", required=false) String fName, @RequestParam(value = "lName", required=false) String lName, @RequestParam(value = "sPhone" , required=false) String sPhone, @RequestParam(value = "email", required=false) String sEmail ){
    public String customerFormSubmit(){
/*        Customer customer = new Customer();
        customer.setfName(fName);
        customer.setlName(lName);
        customer.setsPhone(sPhone);
        customer.setEmail(sEmail);
        System.out.println("Customer Object = " + customer );*/
        return "map.html";
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(TransSuccessController.class, args);
    	//PublicApiService publicApiService = new PublicApiService();
    	//publicApiService.getPublicTrade();    
    }
}