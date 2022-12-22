
import java.io.*;
import java.net.*;
import java.util.*;
import java.awt.*;

//accepts all connections from clients
//reads in messages from clients and broadcasts those messages to all other clients
public class PaintServer
{
	private ArrayList<ClientHandler> allClients;  // used to broadcast messages to all connected clients

	// creates the serverSocket on port 4242.
	// continously attempts to listen for new clients
	// builds a clientHandler thread off the socket and starts that thread.
	// this constructor never ends.
	
	public  PaintServer() {
		
		allClients = new ArrayList<ClientHandler>();
		
		try {
			
			System.out.println("Server: ");
			ServerSocket server = new ServerSocket(4242);
			System.out.println(server.getLocalPort());
			System.out.println(InetAddress.getLocalHost().getHostAddress());

			//infinitely accepts new client and makes a new thread for each
			while(true) {
				
				Socket sock = server.accept();
				ClientHandler toAdd = new ClientHandler(sock);
				allClients.add(toAdd);
				Thread clientThread = new Thread(toAdd);
				clientThread.start();
			}
		}catch(IOException e){
			e.printStackTrace();
		}
	}

	// writes the message to every socket in the ArrayList instance variable.
	public void tellEveryone(String message) {
		
		for(ClientHandler c: allClients) {
			
			try {
				PrintWriter outgoing = new PrintWriter(c.sock.getOutputStream());
				
				outgoing.println(message);
				outgoing.flush();
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	//interacts with a specific client
	public class ClientHandler implements Runnable {

		private Scanner reader;
		private Socket sock;
		private PrintWriter theWriter;
		private String name;				//client's name
		private Color color;				//client color draws with

		// initializes all instance variables
		public ClientHandler(Socket clientSocket) {

			sock = clientSocket;
			
			try {
				theWriter = new PrintWriter(sock.getOutputStream());
				reader = new Scanner(sock.getInputStream());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		
		public String toString() {
			return name +" "+ color.getRed() + " "+color.getGreen()+ " "+color.getBlue();
		}

		public boolean equals(Object o) {
			ClientHandler other = (ClientHandler)o;
			return name.equals(other.name);
		}
		
		
		//continuously checks to see if there is an available message from the client
		// if so broadcasts received message to all other clients
		// via the outer helper method tellEveryone.
		public void run() {
			
			while(reader.hasNextLine()) {
				
				String line = reader.nextLine();
				
				//relays logoff message to all clients if one client logs off
				if(line.contains("logoff")) {
					
					tellEveryone(line);
					closeConnections();
					return;
				}
				
				else {
					
					String[] split = line.split(" ");
					
					//initializes new client if only 4 pieces of information sent
					if(split.length == 4) {
						
						name = split[0];
						color = new Color(Integer.parseInt(split[1]), Integer.parseInt(split[2]), Integer.parseInt(split[3]));
						tellEveryone("joined:" + name + " " + split[1] + " " + split[2] + " " + split[3]);
						
						for(ClientHandler c : allClients) {
							
							if(!c.equals(this)) {
								
								theWriter.println("joined:" + c);
								theWriter.flush();
							}
						}
					}
					
					//client updated new point, informs rest of the clients
					else {
						
						tellEveryone(line);
					}
				}
			}
			
			
			closeConnections();
		}

		private void closeConnections(){

			try{
				synchronized(allClients){

					reader.close();
					theWriter.close();
					sock.close();
					allClients.remove(this);
				}
			}catch(IOException e){
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		new PaintServer();
	}
}
