package application;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import org.json.JSONException;
import org.json.JSONObject;

public class ChatroomLayoutModel {
	
	private ChatroomLayoutController controller;
	private PrintWriter toServer;
	private BufferedReader fromServer;
	
	private String userName;
	
	public ChatroomLayoutModel(String userName) {
		this.userName = userName;
	}
	
	public void setConnection() {
		try {
			Socket socket = new Socket("localhost", 8000);
			DataInputStream is = new DataInputStream(socket.getInputStream());
			DataOutputStream os = new DataOutputStream(socket.getOutputStream());
			toServer = new PrintWriter(os);
			fromServer = new BufferedReader(new InputStreamReader(is, "UTF-8"));
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		
		new Thread(() -> {
			String messageFromServer = null;
			try {
				while (true) {
					messageFromServer = fromServer.readLine();
					JSONObject jsonObject = new JSONObject(messageFromServer);
					if (messageFromServer.indexOf("image") != -1) 
			            controller.updateImageLater(jsonObject.get("userName").toString(), jsonObject.get("image").toString(), jsonObject.get("nowTime").toString());
					else
						controller.updateLabelLater(jsonObject.get("userName").toString(), jsonObject.get("message").toString(), jsonObject.get("nowTime").toString());
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			} catch (JSONException e2) {
				e2.printStackTrace();
			}
		}).start();
    }
	public void setController(ChatroomLayoutController controller) {
		this.controller = controller;
	}
	public void sendServer(JSONObject jsonObject) {
		toServer.write(jsonObject.toString() + "\n");
		toServer.flush();
	}
	//public String getUserName() {return userName;}
}
