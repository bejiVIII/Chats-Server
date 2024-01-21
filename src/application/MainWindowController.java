package application;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
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
		
	private ServerSocket serverSocket = null;
	
	private static final int PORT = 9999;
    
	public ArrayList<ClientHandler> clients;
    
    
	public MainWindowController() {
		this.clients = new ArrayList<ClientHandler>();
	}
	
	// does this run before or after the constructor?
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
	
	// thread that accepts new client connections, and makes threads for them
	public void startServer() {
		
		// is server already open?
		if (serverSocket != null) {
			Text t1 = new Text("Server is already open.\n");
            infoTextFlow.getChildren().add(t1);
            return;
		}
		
        new Thread(() -> {
        	try {
	            serverSocket = new ServerSocket(PORT);
	            
	            System.out.println("is socket closed: " + serverSocket.isClosed());
	            Platform.runLater(new Runnable(){

					@Override
					public void run() {
						Text t2 = new Text("Server is running on port " + PORT + "\n");
			            infoTextFlow.getChildren().add(t2);
					}
            	});
	            
	            while (serverSocket != null) {
	            	
	                Socket clientSocket = serverSocket.accept();
	                
	                // log new connection
	                Platform.runLater(new Runnable() {

						@Override
						public void run() {
							 Text t2 = new Text("New connection established. \n");
					         infoTextFlow.getChildren().add(t2);
						}
	                });
		           
	
	                ClientHandler handler = new ClientHandler(clientSocket, this);
	                clients.add(handler);
	                new Thread(handler).start();
	            }
        	}
        	catch (Exception e) {
        		// ignore
        	}
        }).start();
	}
	
	public void stopServer() {
		
		// is the server already closed?
		if (serverSocket == null) {
			Text t1 = new Text("Server has not yet started.\n");
            infoTextFlow.getChildren().add(t1);
            return;
		}
		
		try {
			// TODO
            // tell others that the server is closed
            broadcast("SERVER: closed.");
			
	        // close all connections
			for (ClientHandler client : clients) {
				client.closeEverything();
			}
            
			// stop accepting new clients
			serverSocket.close();
			serverSocket = null;
			
			Text t2 = new Text("Server closed.\n");
            infoTextFlow.getChildren().add(t2);
		}
		catch (Exception e) {
			
			Platform.runLater(new Runnable(){

				@Override
				public void run() {
					Text t2 = new Text(e.getMessage() + "\n");
		            infoTextFlow.getChildren().add(t2);
				}
        	});
		}
	}
	
	public void broadcast(String message) {
		
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }
	
	// TODO
	// make it so that when the X button for closing the window is pressed
	// run the stop server function first for clean exit
	
	
	// TODO
	// add logging functionality
	public void addMessage(String message) {
		Text text = new Text(message + "\n");
        Platform.runLater(() -> infoTextFlow.getChildren().add(text));
	}
}
