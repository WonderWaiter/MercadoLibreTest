package utils;

import java.util.Map;
import java.util.concurrent.Callable;

public class QueryCallable<T> implements Callable<QueryReponse<T>>{

	private String path;
	private String errorMessage;
	private int poolSize; 
	
	public QueryCallable(String path, String errorMessage) {
		this.path = path;
		this.errorMessage = errorMessage;
		this.poolSize = 0;
	}
	
	public QueryCallable(String path, String errorMessage, int poolSize) {
		this.path = path;
		this.errorMessage = errorMessage;
		this.poolSize = poolSize;
	}
	
	@Override
	public QueryReponse<T> call() throws Exception {			
		QueryReponse<T> queryResult = null;
		if(poolSize > 0) {
			queryResult = ServerCom.queryAndHandleRequestBurst(path, errorMessage, poolSize);
		} else {
			Map<String, Object> requestProperties = ServerCom.getNewGETRequestProperties();
			queryResult = ServerCom.queryAndHandleRequest(requestProperties, path, errorMessage);
		}					
		return queryResult;
	}
	

}