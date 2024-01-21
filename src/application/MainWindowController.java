package application;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class MainWindowController implements Initializable {
	
	@FXML
	private ScrollPane infoScrollpane;
	
	@FXML
	private TextFlow infoTextFlow;
	
	@FXML
	private Button startServerButton;
	
	@FXML
	private Button closeServerButton;
		
	private ServerSocket serverSocket;
	private Socket clientSocket;
	
	private ClientHandler clientHandler;
	private static final int PORT = 9999;
    public static List<ClientHandler> clients = new ArrayList<>();
    
    
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		
		
		infoTextFlow.heightProperty().addListener(new ChangeListener<Number>() {

			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				System.out.println(newValue);
				infoScrollpane.setVvalue((Double) newValue);
			}
			//auto-scroll
		});
		
	}
	
	public void startServer()
	{
        new Thread(() -> {
        	try {
	            serverSocket = new ServerSocket(PORT);
	            //textToAppend.add("Server is running on port " + PORT + "\n");
	            //System.out.println("Server is running on port " + PORT);
	            
	            System.out.println("is socket closed: " + serverSocket.isClosed());
	            Platform.runLater(new Runnable(){

					@Override
					public void run() {
						Text t2 = new Text("Server is running on port " + PORT + "\n");
			            infoTextFlow.getChildren().add(t2);
					}
	            	});
	            while (true) { //TODO: nullpointerexception if "clientSocket.isClosed()" is used...
	                clientSocket = serverSocket.accept();
	                //can't modify fx ui elements on another thread....
	                Platform.runLater(new Runnable(){

						@Override
						public void run() {
							 Text t2 = new Text("New connection established. \n");
					         infoTextFlow.getChildren().add(t2);
						}
	                });
		           
	
	                clientHandler = new ClientHandler(clientSocket, this);
	                clients.add(clientHandler);
	
	                new Thread(clientHandler).start();
	            }
        	} catch (IOException e) {
        		//e.printStackTrace();
        		Platform.runLater(new Runnable(){

					@Override
					public void run() {
						Text t2 = new Text(e.getMessage() + "\n");
			            infoTextFlow.getChildren().add(t2);
					}
	            	});
        	}
        }).start();

	}
	
	public void stopServer()
	{
		try {
			clientSocket.close();
			Text t1 = new Text("Client socket closed.\n");
            infoTextFlow.getChildren().add(t1);
            
			serverSocket.close();
			Text t2 = new Text("Server socket closed.\n");
            infoTextFlow.getChildren().add(t2);
            
            broadcast("SERVER: closed.", clientHandler);
		} catch (Exception e) {
			
			if(clientSocket == null)
			{
				Text t1 = new Text("Server was not started.\n");
	            infoTextFlow.getChildren().add(t1);
	            return;
			}
			//e.printStackTrace();
			Platform.runLater(new Runnable(){

				@Override
				public void run() {
					Text t2 = new Text(e.getMessage() + "\n");
		            infoTextFlow.getChildren().add(t2);
				}
            	});
		}
	}
	
	public void broadcast(String message, ClientHandler sender) {
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.sendMessage(message);
            }
        }
        
    }
	
	public void addMessage(String message)
	{
		Text text = new Text(message + "\n");
        Platform.runLater(() -> infoTextFlow.getChildren().add(text));

	}
	
}
