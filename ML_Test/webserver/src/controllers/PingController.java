package controllers;

public class PingController extends MainController{
	 
	public void index() {
			setResponseBody("pong");
			setResponseStatus(200);
	}
}
