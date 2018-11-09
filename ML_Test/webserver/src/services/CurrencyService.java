package services;

import java.util.Map;
import utils.QueryReponse;
import utils.ServerCom;

public class CurrencyService {
	
	public static final String defaultErrorMessage = "could not get currency information";
	
	public QueryReponse<Map<?, ?>> query(){
		QueryReponse<Map<?, ?>> currencyInfo = ServerCom.queryAndHandleRequest
			(ServerCom.getNewGETRequestProperties(), "/currency_conversions?from=USD&to=ARS", defaultErrorMessage);
		return currencyInfo;		
	}
}
