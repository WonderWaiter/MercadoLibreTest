package services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import utils.QueryCallable;
import utils.QueryReponse;

public class ItemsService {
	
	public static final String defaultErrorMessage = "could not get item information";
	private static final int defaultPoolSize = 3;

	public QueryReponse<List<QueryReponse<Map<?, ?>>>> query(List<Long> itemsIds){		
		int itemsIdsSize = itemsIds.size();
		ExecutorService exe = Executors.newFixedThreadPool(itemsIdsSize);
		List<Future<QueryReponse<Map<?, ?>>>> futureResponses = new ArrayList<Future<QueryReponse<Map<?, ?>>>>();
		for(int i=0; i < itemsIdsSize; i++){
			futureResponses.add(exe.submit(new QueryCallable<Map<?, ?>>
				("/items/" + itemsIds.get(i).toString(), defaultErrorMessage, defaultPoolSize)));
		}    						
		exe.shutdown();
		List<QueryReponse<Map<?, ?>>> result = new ArrayList<QueryReponse<Map<?, ?>>>();
		try {
			exe.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);			
			for (Future<QueryReponse<Map<?, ?>>> future : futureResponses) {				
				QueryReponse<Map<?, ?>> itemInfo = future.get();
				if(!itemInfo.IsOk) {
					return new QueryReponse<List<QueryReponse<Map<?, ?>>>>(null, itemInfo.ErrorMessage);
				}
				result.add(itemInfo);
			}	
		} catch (InterruptedException | ExecutionException e) {
			return new QueryReponse<List<QueryReponse<Map<?, ?>>>>(null, e.toString());
		}
		return new QueryReponse<List<QueryReponse<Map<?, ?>>>>(result);	
	}	
}
