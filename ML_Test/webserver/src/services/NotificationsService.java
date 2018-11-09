package services;

import java.util.LinkedHashMap;
import java.util.Map;
import utils.QueryReponse;
import utils.ServerCom;

public class NotificationsService {	

	public static final String defaultErrorMessage = "could not notify result";
	
	public QueryReponse<Map<?, ?>> notify(Integer userToNotify, Double amountToNotify){
		Map<String, Number> bodyToNotify = new LinkedHashMap<String, Number>();
		bodyToNotify.put("id", userToNotify);
		bodyToNotify.put("amount", amountToNotify);		
		Map<String, Object> requestProperties = ServerCom.getNewPOSTRequestProperties();
		requestProperties.put("body", bodyToNotify);		
		QueryReponse<Map<?, ?>> response = ServerCom.queryAndHandleRequest
			(requestProperties, "/notifications", defaultErrorMessage);
		return response;
	}
	
}
