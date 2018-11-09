package services;

import java.util.List;
import utils.QueryReponse;
import utils.ServerCom;

public class SoldItemsService {
	
	public static final String defaultErrorMessage = "could not get soldItems for user ";
	private static final int defaultPoolSize = 3;

	public QueryReponse<List<?>> query(Integer userId){
		QueryReponse<List<?>> soldItems = ServerCom.queryAndHandleRequestBurst	
			("/soldItems/" + userId.toString(), defaultErrorMessage + userId, defaultPoolSize);
		return soldItems;		
	}
	
}