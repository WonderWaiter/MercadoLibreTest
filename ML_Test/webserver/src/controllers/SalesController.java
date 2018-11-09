package controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONObject;

import services.CurrencyService;
import services.ItemsService;
import services.NotificationsService;
import services.SoldItemsService;
import services.UserService;
import utils.QueryCallable;
import utils.QueryReponse;

public class SalesController extends MainController{	

	private static Double conversionRate = 0.0;
	private static final Object conversionRateLock = new Object();
	
	public void setUserService(UserService userService) {
		this.userService = userService;
	}
	public void setSoldItemsService(SoldItemsService soldItemsService) {
		this.soldItemsService = soldItemsService;
	}
	public void setItemsService(ItemsService itemsService) {
		this.itemsService = itemsService;
	}
	public void setCurrencyService(CurrencyService currencyService) {
		SalesController.currencyService = currencyService;
	}
	public void setNotificationsService(NotificationsService notificationsService) {
		this.notificationsService = notificationsService;
	}
	
	private UserService userService = new UserService();
	private SoldItemsService soldItemsService = new SoldItemsService();
	private ItemsService itemsService = new ItemsService();
	private static CurrencyService currencyService = new CurrencyService();
	private NotificationsService notificationsService = new NotificationsService();

	public void index() {		
		
		JSONObject finalResponse = new JSONObject();
		Double totalAmount = 0D;
		
		try{
			JSONObject parameters = request.getJSONObject("parameters");
			JSONArray userIdArray = parameters.getJSONArray("userId");
			Integer userId = Integer.valueOf(userIdArray.get(0).toString());
			finalResponse.put("sellerId", userId);		
			final Integer finaUserId = userId;			
			
		    ExecutorService exe = Executors.newFixedThreadPool(2);
		    Callable userTask = new Callable<QueryReponse<Map<?, ?>>>() {
				@Override
				public QueryReponse<Map<?, ?>> call() {
					QueryReponse<Map<?, ?>> result = userService.query(finaUserId);
					return result;
				}
		    };
		    Callable soldItemsTask = new Callable<QueryReponse<List<?>>>() {
				@Override
				public QueryReponse<List<?>> call() {
					QueryReponse<List<?>> result = soldItemsService.query(finaUserId);
					return result;
				}
		    };
		    Future<QueryReponse<Map<?, ?>>> userFuture = exe.submit(userTask);
		    Future<QueryReponse<List<?>>> soldItemsFuture = exe.submit(soldItemsTask);
		    exe.shutdown();
		    QueryReponse<List<?>> soldItems = null;
			try {
				exe.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);			
				QueryReponse<Map<?, ?>> responseBody = userFuture.get();
				if(!responseBody.IsOk) {
					setServiceFailedResponse(responseBody.ErrorMessage);
					return;
				}
				//Validación: si el usuario no es de tipo seller, entonces devuelve error, 
				//porque no es necesario devolver el monto de los items.
				if(!"seller".equals((String) responseBody.Response.get("user_type"))){
					setServiceFailedResponse("user is not seller");
					return;
				}
				soldItems = soldItemsFuture.get();
				if(!soldItems.IsOk) {
					setServiceFailedResponse(soldItems.ErrorMessage);
					return;
				}								
			} catch (InterruptedException | ExecutionException e) {
				setServiceFailedResponse(e.toString());
				return;
			}
			
			List<Long> itemsIds = new ArrayList<Long>();			
			int soldItemsSize = soldItems.Response.size();
			//Iterar para obtener el precio de cada uno de esos items vendidos	
			for(int i=0; i < soldItemsSize; i++){
				Map<?, ?> itemJson = (Map<?, ?>) soldItems.Response.get(i);
				Long itemId = (Long) itemJson.get("id");
				itemsIds.add(itemId);
			} 
			QueryReponse<List<QueryReponse<Map<?, ?>>>> items = itemsService.query(itemsIds);
			if(!items.IsOk) {
				setServiceFailedResponse(items.ErrorMessage);
				return;
			}
			for (QueryReponse<Map<?, ?>> itemInfo : items.Response) {				
				if(!itemInfo.IsOk) {
					setServiceFailedResponse(itemInfo.ErrorMessage);
					return;
				}
				totalAmount += (Double) itemInfo.Response.get("price");
			}
			
			//Obtener rate de conversion desde cache
			Double totalAmountUSD = totalAmount/getConversionRate();
			finalResponse.put("totalAmountUSD", totalAmountUSD);
			
			//------------------------------------------------------------------- 
			//Notificación del monto total que se va a devolver y Seteo de respuesta	
			
			final Double finalTotalAmountUSD = totalAmountUSD;
		    ExecutorService executor = Executors.newSingleThreadExecutor();
		    Runnable notifyTask = new Runnable() {
				@Override
				public void run() {
					QueryReponse<Map<?, ?>> notificationResult = notificationsService.notify(finaUserId, finalTotalAmountUSD);
					if(!notificationResult.IsOk) {
						System.out.println("Notification result Error: " + notificationResult.ErrorMessage);
					} else {
						System.out.println("Notification result: " + notificationResult.Response.toString());
					}
				}
		    };
		    executor.submit(notifyTask);
		    executor.shutdown();
			
			setServiceBodyResponse(finalResponse, 200);
		}
		catch(Exception e){
			System.out.println("Exception " + e);
			setServiceFailedResponse("Exception ", e);
		}		
	}
	
    public static Double getConversionRate() {
    	Double result = 0.0; 
        synchronized (conversionRateLock) {
        	result = conversionRate;
        }
        return result;
    } 
    
    public static void StartConversionRateCache() {
    	ExecutorService exe = Executors.newSingleThreadExecutor();
	    Runnable task = new Runnable() {
			@Override
			public void run() {
				while(true) {
					try {
						QueryReponse<Map<?, ?>> currencyInfo = currencyService.query();
						if(currencyInfo.IsOk) {
							synchronized (conversionRateLock) {
					        	conversionRate = (Double) currencyInfo.Response.get("rate");
					        }
						}	
						Thread.sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}					
				}				
			}
	    };
	    exe.submit(task);
	    exe.shutdown();
    }
	
	private void setServiceFailedResponse(String message, Object backendMessage){
		setServiceMessageResponse(message + " (" + backendMessage + ") ", 500);
	}	
	
	private void setServiceFailedResponse(String message){
		setServiceMessageResponse(message, 500);
	}

	private void setServiceMessageResponse(String message, int status){
		setServiceBodyResponse("{'message':'" + message + "'}", status);
	}
	
	private void setServiceBodyResponse(Object body, int status){
		setResponseBody(body);
		setResponseStatus(status);
		setResponseHeader("content-type", "application/json");
	}
}




