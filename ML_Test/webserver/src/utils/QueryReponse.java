package utils;

public class QueryReponse<T> {
	public T Response;
	public boolean IsOk;
	public String ErrorMessage;
	
	//Response OK
	public QueryReponse(T response)
	{
		Response = response;
		IsOk = true;
		ErrorMessage = null;
	}
	
	//Response not OK
	public QueryReponse(T response, String errorMessage)
	{
		Response = response;
		IsOk = false;
		ErrorMessage = errorMessage;
	}
}