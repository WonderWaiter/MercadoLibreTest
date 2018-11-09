package controllers;

import org.junit.Test;

import services.*;
import utils.QueryReponse;

import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SalesControllerTest {

	private static SalesController salesController;
	
	@Test
	public void internalErrorGettingUser () throws JSONException {
		IniController();		
		salesController.setRequest(getRequest(1001));
		salesController.index();				
		assertResponseStatusAndBody(500, "{'message':'could not get user information'}");
	}	
	
	@Test
	public void userIsNotSeller () throws JSONException {
		IniController();		
		salesController.setRequest(getRequest(1002));
		salesController.index();		
		assertResponseStatusAndBody(500, "{'message':'user is not seller'}");
	}
	
	@Test
	public void internalErrorGettingSoldItems () throws JSONException {
		IniController();		
		salesController.setRequest(getRequest(1003));
		salesController.index();		
		assertResponseStatusAndBody(500, "{'message':'could not get soldItems for user '}");
	}
	
	@Test
	public void internalErrorGettingItem () throws JSONException {
		IniController();		
		salesController.setRequest(getRequest(1004));
		salesController.index();		
		assertResponseStatusAndBody(500, "{'message':'could not get item information'}");
	}
	
	@Test
	public void executionOk () throws JSONException {
		IniController();		
		salesController.setRequest(getRequest(1005));
		salesController.index();
		assertResponseStatusAndBody(200, "{\"sellerId\":1005,\"totalAmountUSD\":15}");
	}
	
	private void assertResponseStatusAndBody(int assertStatus, String assertBody) {
		Integer status = salesController.getResponseStatus();
		String stringBody = salesController.getResponseBody().toString();		
		System.out.println(status + "  " + stringBody);
		org.junit.Assert.assertEquals(new Integer(assertStatus), status);
		org.junit.Assert.assertEquals(assertBody, stringBody);
	}
	
	private static JSONObject getRequest(Integer userId) throws JSONException {
		JSONArray requestParameterUserId = new JSONArray();
		requestParameterUserId.put(userId);
		JSONObject requestParameters = new JSONObject();
		requestParameters.put("userId", requestParameterUserId);
		JSONObject request = new JSONObject();
		request.put("parameters", requestParameters);		
		return request;
	}
	
	private static synchronized void IniController() {
		if(salesController == null) {
			salesController = new SalesController();
			
			CurrencyService currencyService = mock(CurrencyService.class);
			salesController.setCurrencyService(currencyService);
			ItemsService itemsService = mock(ItemsService.class);
			salesController.setItemsService(itemsService);
			NotificationsService notificationsService = mock(NotificationsService.class);
			salesController.setNotificationsService(notificationsService);
			SoldItemsService soldItemsService = mock(SoldItemsService.class);
			salesController.setSoldItemsService(soldItemsService);
			UserService userService = mock(UserService.class);
			salesController.setUserService(userService);
			
			LinkedHashMap currencyOkResponse = new LinkedHashMap();
			currencyOkResponse.put("rate", 40.0);
			when(currencyService.query()).thenReturn(new QueryReponse<Map<?, ?>>(currencyOkResponse));
			
			when(userService.query(1001)).thenReturn(new QueryReponse<Map<?, ?>>(null, UserService.defaultErrorMessage));
			LinkedHashMap userOkResponse_notSeller = new LinkedHashMap();
			userOkResponse_notSeller.put("user_type", "buyer");
			when(userService.query(1002)).thenReturn(new QueryReponse<Map<?, ?>>(userOkResponse_notSeller));
			LinkedHashMap userOkResponse_seller = new LinkedHashMap();
			userOkResponse_seller.put("user_type", "seller");
			when(userService.query(1003)).thenReturn(new QueryReponse<Map<?, ?>>(userOkResponse_seller));
			when(userService.query(1004)).thenReturn(new QueryReponse<Map<?, ?>>(userOkResponse_seller));
			when(userService.query(1005)).thenReturn(new QueryReponse<Map<?, ?>>(userOkResponse_seller));
			when(userService.query(1006)).thenReturn(new QueryReponse<Map<?, ?>>(userOkResponse_seller));
			
			when(soldItemsService.query(1003)).thenReturn(new QueryReponse<List<?>>(null, SoldItemsService.defaultErrorMessage));			
			LinkedHashMap soldItem1 = new LinkedHashMap();
			soldItem1.put("id", new Long(1111111));
			LinkedHashMap soldItem2 = new LinkedHashMap();
			soldItem2.put("id", new Long(2222222));
			LinkedHashMap soldItem3 = new LinkedHashMap();
			soldItem3.put("id", new Long(3333333));
			LinkedHashMap soldItem4 = new LinkedHashMap();
			soldItem4.put("id", new Long(4444444));
			List<LinkedHashMap> soldItemsList_Ok = new ArrayList<LinkedHashMap>
				(Arrays.asList(soldItem1, soldItem2, soldItem3));
			List<LinkedHashMap> soldItemsList_buggedItem = new ArrayList<LinkedHashMap>
				(Arrays.asList(soldItem1, soldItem2, soldItem4));
			when(soldItemsService.query(1004)).thenReturn(new QueryReponse<List<?>>(soldItemsList_buggedItem));
			when(soldItemsService.query(1005)).thenReturn(new QueryReponse<List<?>>(soldItemsList_Ok));
			when(soldItemsService.query(1006)).thenReturn(new QueryReponse<List<?>>(soldItemsList_Ok));
			
			when(itemsService.query(Arrays.asList(new Long(1111111), new Long(2222222), new Long(4444444))))
				.thenReturn(new QueryReponse<List<QueryReponse<Map<?, ?>>>>(null, ItemsService.defaultErrorMessage));
			List<QueryReponse<Map<?, ?>>> itemsOkResponse = new ArrayList<QueryReponse<Map<?, ?>>>();
			LinkedHashMap item1 = new LinkedHashMap();
			item1.put("price", new Double(100.00));
			itemsOkResponse.add(new QueryReponse<Map<?,?>>(item1));
			LinkedHashMap item2 = new LinkedHashMap();
			item2.put("price", new Double(200.00));
			itemsOkResponse.add(new QueryReponse<Map<?,?>>(item2));
			LinkedHashMap item3 = new LinkedHashMap();
			item3.put("price", new Double(300.00));
			itemsOkResponse.add(new QueryReponse<Map<?,?>>(item3));
			when(itemsService.query(Arrays.asList(new Long(1111111), new Long(2222222), new Long(3333333))))
				.thenReturn(new QueryReponse<List<QueryReponse<Map<?, ?>>>>(itemsOkResponse));
			
			SalesController.StartConversionRateCache();
		}	
	}
	
}
