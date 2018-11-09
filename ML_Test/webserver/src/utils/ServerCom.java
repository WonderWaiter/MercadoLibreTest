package utils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import controllers.StatusCode;

public class ServerCom {
	
	public static final int backendServerPort = 8888; 	
	private static final int maxThreadCount = 1500;
	
    private static int threadCount = 0;
    private static final Object threadCountLock = new Object();
	
	public static Map<String, Object> getNewGETRequestProperties(){
		LinkedHashMap<String, Object> headers = new LinkedHashMap<String, Object>();	
		Map<String, Object> requestProperties = new LinkedHashMap<String, Object>();
		requestProperties.put("baseUrl", "http://localhost:" + backendServerPort);
		requestProperties.put("method", "GET");
		requestProperties.put("body", new LinkedHashMap<Object, Object>());
		requestProperties.put("headers", headers);
		requestProperties.put("socketTimeout", 15000);
		requestProperties.put("connectionTimeout", 100);	
		return requestProperties;
	}
	
	public static Map<String, Object> getNewPOSTRequestProperties(){
		Object headers = new LinkedHashMap<String, Number>();	
		Map<String, Object> requestProperties = new LinkedHashMap<String, Object>();
		requestProperties.put("baseUrl", "http://localhost:" + backendServerPort);
		requestProperties.put("method", "POST");
		requestProperties.put("headers", headers);
		return requestProperties;
	}
	
    private static void incrementThreadCount() throws InterruptedException {
    	while(!canIncrementThreadCount()) {
    		Thread.sleep(100);
    	}
        synchronized (threadCountLock) {
        	threadCount++;
        }
    }
    
    private static void decrementThreadCount() {
        synchronized (threadCountLock) {
        	threadCount--;
        }
    }
    
    private static boolean canIncrementThreadCount() {
        boolean result = false;
    	synchronized (threadCountLock) {
    		result = threadCount < maxThreadCount;
        }
    	return result;
    }
	
	public static <T> QueryReponse<T> queryAndHandleRequest(Map<String, Object> requestProperties, String path, String errorMessage) {
		requestProperties.put("uriWithQueryString", path);		
		try{
			
			Map<?, ?> response = null;
			try {
				incrementThreadCount();
				response = HttpClient.executeRequest(requestProperties, 1000);
			}
			catch(Exception e){
				decrementThreadCount();
				return new QueryReponse<T>(null, errorMessage + " (" + e + ") ");
			}
			decrementThreadCount();
			Integer responseStatus = (Integer) response.get("status");
			if(responseStatus > 201){
				return new QueryReponse<T>
					(null, errorMessage + " (" + responseStatus + "  " + StatusCode.getStatusDescription(responseStatus)  + ") ");
			}
			@SuppressWarnings("unchecked")
			T castedResponse = (T) response.get("body");
			return new QueryReponse<T>(castedResponse);
		}
		catch(Exception e){
			return new QueryReponse<T>(null, errorMessage + " (" + e + ") ");
		}
	}
	
	public static <T> QueryReponse<T> queryAndHandleRequestBurst(String path, String errorMessage, int poolSize) {
		ExecutorService exe = Executors.newFixedThreadPool(poolSize);
		QueryReponse<T> response = null;
		List<Future<QueryReponse<T>>> futureResponses = new ArrayList<Future<QueryReponse<T>>>();
		for(int i=0; i<poolSize; i++){
			futureResponses.add(exe.submit(new QueryCallable<T>(path, errorMessage)));
		}    			
		exe.shutdown();
		try {
			while(true) {
				exe.awaitTermination(50, TimeUnit.MILLISECONDS);
				for (Future<QueryReponse<T>> future : futureResponses) {	
					QueryReponse<T> threadResponse = null;
					try {
						if(future.isDone()) {
							threadResponse = future.get();			
						} 
						if(threadResponse == null) {
							threadResponse = new QueryReponse<T>(null, errorMessage + " ( Timed Out ) ");
						}										
					} catch (ExecutionException e) {
						threadResponse = new QueryReponse<T>(null, errorMessage + " (" + e + ") ");
					}
					if(threadResponse.IsOk) {
						return threadResponse;
					}
					else {
						response = threadResponse;
					}
				}
			}	
		} catch (InterruptedException e) {
			response = new QueryReponse<T>(null, errorMessage + " (" + e + ") ");
		}
		return response;
	}
}
