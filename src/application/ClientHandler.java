package application;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.regex.Pattern;

import javafx.scene.text.Text;

public class ClientHandler implements Runnable {

	private Socket clientSocket;
    private BufferedWriter outputStream;
    private BufferedReader inputStream;
    private MainWindowController mainWindowController;
    
    private String nickname;
    
    public final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
    
    public ClientHandler(Socket clientSocket, MainWindowController mainWindowController) {
    	this.mainWindowController = mainWindowController;
    	
        this.clientSocket = clientSocket;
        try {
        	inputStream = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            this.outputStream = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
        } 
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
	@Override
	public void run() {
		
		String clientInput;
        String[] message;
       
		try {
			
			// get user name
			// TODO
			// check that no one else has this at the time
			clientInput = inputStream.readLine();
			message = clientInput.split(Pattern.quote(">8^("), 2);
			nickname = message[1];
			
			if (mainWindowController.usernames.contains(nickname)) {
				sendMessage("99>8^(Username already in use");
				throw new Exception();
			}
			else {
				sendMessage("100>8^(OK");
				mainWindowController.usernames.add(nickname);
			}
			
			// tell others a new user just joined
			String time = timeFormat.format(new Timestamp(System.currentTimeMillis()));
			mainWindowController.broadcast(String.format("5>8^(%s>8^(%s", time, nickname));
			mainWindowController.addMessage(String.format("[%s] (%s) joined the chat!", time, nickname));
			
			// process next messages
            while (!clientSocket.isClosed()) {
                
            	clientInput = inputStream.readLine();
                message = clientInput.split(Pattern.quote(">8^("), 3);
                
                time = timeFormat.format(new Timestamp(System.currentTimeMillis()));
                
                switch (Integer.parseInt(message[0])) {
                
                // broadcast message
				case 1:	
					mainWindowController.broadcast(String.format("0>8^(%s>8^(%s>8^(%s", time, nickname, message[1]));
					mainWindowController.addMessage(String.format("[%s] (%s) %s", time, nickname, message[1]));
					break;
					
				// user wants to change nickname
				case 2:
					// nick
					break;
					
				// client whispers something to another user
				case 3:
					// nick, message
					break;
					
				// user quits
				case 4:
					mainWindowController.broadcast(String.format("6>8^(%s>8^(%s", time, nickname));
					mainWindowController.addMessage(String.format("[%s] (%s) just quit", time, nickname));
					closeEverything();
					break;
					
				default:
					System.out.println("Unknown opcode!");
                }
            }
        } 
		catch (Exception e) {
            // ignore
        }
	}
	
	public void sendMessage(String message) {
        try {
            outputStream.write(message);
            outputStream.newLine();
            outputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
	
	public void closeEverything()
	{
		try {
			mainWindowController.usernames.remove(nickname);
			
			// socket closes the streams as well
			clientSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


}
