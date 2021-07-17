package ch.bbcag.mdriver.connection;

import ch.bbcag.mdriver.commons.*;
import ch.bbcag.mdriver.view.View;
import com.google.gson.Gson;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

	private static Server instance;

	private ServerSocket server;
	private Socket client;

	private DataOutputStream dos;
	private DataInputStream dis;
	private final Gson gson;

	private boolean running = true;
	private boolean connected;
	private final View view = new View();

	private OnCommandListener onCommandListener;

	private Server() {
		gson = GsonCreator.create();
		try {
			server = new ServerSocket(18999);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Gson getGson() {
		return gson;
	}

	public static Server getInstance() {
		if (instance == null) {
			instance = new Server();
		}
		return instance;
	}

	public void acceptClient() {
		try {
			client = server.accept();
			dos = new DataOutputStream(getClient().getOutputStream());
			dis = new DataInputStream(getClient().getInputStream());
			send(new StateMessage(StateCode.CONNECTION_OK));
			running = true;
			connected = true;
			view.connected();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void startListening() {
		while (running) {
			if (!connected)
				continue;
			try {
				String message = dis.readUTF();
				if (onCommandListener != null) {
					onCommandListener.onCommand(gson.fromJson(message, Message.class));
				}
			} catch (IOException e) {
				reconnect();
			}
		}
	}

	public void reconnect() {
		onCommandListener.onCommand(new DriveMessage(0, 0));
		dos = null;
		dis = null;
		client = null;
		connected = false;
		view.awaitConnection();
		acceptClient();
	}

	public void stopListening() {
		running = false;
	}

	public void send(Message message) {
		try {
			if (dos != null) {
				dos.writeUTF(gson.toJson(message));
				dos.flush();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setOnCommandListener(OnCommandListener onCommandListener) {
		this.onCommandListener = onCommandListener;
	}

	public Socket getClient() {
		return client;
	}

	public void close() {
		try {
			server.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
