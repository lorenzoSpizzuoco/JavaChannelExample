import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

public class ClientFilmChannel {
	
	private Socket socket;
	
	private ClientFilmChannel (InetAddress serverAddress, int serverPort) throws IOException {
		this.socket = new Socket(serverAddress, serverPort);
		
	}
	
	public static void main(String[] args) throws UnknownHostException, IOException, InterruptedException {
		
		ClientFilmChannel client = new ClientFilmChannel(InetAddress.getByName(null), 53535);
		System.out.println("connected to " + client.socket.getInetAddress());
		client.start();
	}
	
	private void start() throws IOException, InterruptedException {
		
		Scanner s = new Scanner(System.in);
		PrintWriter out = new PrintWriter(this.socket.getOutputStream(), true);
		DataInputStream in = new DataInputStream(this.socket.getInputStream());
		
		
		out.print(1);
		out.print('\n');
		out.flush();
		
		int BUFFER_SIZE = 1024;
		int message;
		byte[] b = new byte[BUFFER_SIZE];
		
		in.read(b, 0, 2);
		
		message = b[0];
		
		// checking code sent by server
		if(message == 1) {
			System.out.println("[client] all good.");
			System.out.print("[client] insert day id: ");
			int dayId = s.nextInt();
			out.print(dayId);
			out.flush();
			byte[] FilmBytes = new byte[BUFFER_SIZE]; 
			in.read(FilmBytes); 
			
			String res = "";
			if (FilmBytes != null) {
				for (int i = 0; i < FilmBytes.length; i++) {
					res += (char) FilmBytes[i];
				}
			}
			
			System.out.println("[client] film infos:\n" + res);
			
		}
		else if(message == 0) {
			System.out.println("[client] Error. Server sent code 0.");
		}
		
		
		System.out.println("[client] client terminated");
		socket.close();
		s.close();
	}
}
