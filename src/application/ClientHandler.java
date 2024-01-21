package application;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

import javafx.scene.text.Text;

public class ClientHandler implements Runnable {

	private Socket clientSocket;
    private BufferedWriter outputStream;
    private BufferedReader inputStream;
    private MainWindowController mainWindowController;
    
    public ClientHandler(Socket clientSocket, MainWindowController mainWindowController) {
    	this.mainWindowController = mainWindowController;
    	
        this.clientSocket = clientSocket;
        try {
            this.outputStream = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			inputStream = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            while (true) {
                String message = (String) inputStream.readLine();
                
                mainWindowController.addMessage(message);
                

                // Broadcast the message to all clients
                mainWindowController.broadcast(message, this);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	public void sendMessage(String message) {
        try {
            outputStream.write(message);
            outputStream.newLine();
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }
	
	public void closeEverything()
	{
		try {
			clientSocket.close();
			outputStream.close();
			inputStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


}
