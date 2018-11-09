package services;

import java.util.Map;
import utils.QueryReponse;
import utils.ServerCom;

public class UserService {
	
	public static final String defaultErrorMessage = "could not get user information";
	private static final int defaultPoolSize = 3;

	public QueryReponse<Map<?, ?>> query(Integer userId){
		QueryReponse<Map<?, ?>> user = ServerCom.queryAndHandleRequestBurst
			("/users/" + userId, defaultErrorMessage, defaultPoolSize);
		return user;		
	}
	
}
