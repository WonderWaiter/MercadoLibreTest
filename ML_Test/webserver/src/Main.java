import controllers.SalesController;
import sockets.HttpSocket;
import sockets.HttpSocketManager;


public class Main {

	public static void main(String[] args) {
		int serverPort = 8889;
		
		System.out.println("Starting socket");
		for(int i=0; i<args.length; i++){
			if(args[i].indexOf("=")>0){
				String key = args[i].split("=")[0];
				String value = args[i].split("=")[1];
				if("serverPort".equalsIgnoreCase(key)){
					serverPort = Integer.valueOf(value);
				}
			}
		}
		
		System.out.println("Listenning on port: " + serverPort + ". Please check it sending a request to http://localhost:" + serverPort + "/ping");
		 
		HttpSocket serverSocket = new HttpSocket(serverPort);
		HttpSocketManager.setMaxThreads(1700);
		HttpSocketManager.init();
		HttpSocketManager.executor.execute(serverSocket);
		
		SalesController.StartConversionRateCache();

	}

}
