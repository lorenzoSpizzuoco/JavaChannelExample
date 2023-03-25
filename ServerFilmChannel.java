import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


public class ServerFilm2 {
	
	
	public static void main(String[] args) throws IOException {
		
		Map <Integer, Integer> ConnectionMap;
		ConnectionMap = new HashMap<Integer, Integer>();
		
		Selector selector = Selector.open();
		ServerSocketChannel serverSocket = ServerSocketChannel.open();
		serverSocket.bind(new InetSocketAddress("localhost", 53535));
		serverSocket.configureBlocking(false);
		serverSocket.register(selector, SelectionKey.OP_ACCEPT);
		
		
		 Map <Integer, String[]> films;
		films = new HashMap<Integer, String[]>();
		films.put(1, new String[] {"il mio vicino totoro", "paprika", "il giardino delle parole"});
		films.put(2, new String[] {"fight club", "film2", "space jam"});
		films.put(3, new String[] {"avatar", "film2", "taxi driver"});
		films.put(4, new String[] {"end game", "film2", "la città incantata"});
		films.put(5, new String[] {"il castello errante di howl", "film2", "film3"});
		films.put(6, new String[] {"le ali della libertà", "film2", "film3"});
		films.put(7, new String[] {"lo chiamavano trinità", "film2", "film3"});
		
		try {
						
			while (true) {
				
				selector.select();
				Set<SelectionKey> selectedKeys = selector.selectedKeys();
				Iterator<SelectionKey> iter = selectedKeys.iterator();
				
				while(iter.hasNext()) {
					SelectionKey key = iter.next();
					
					if (key.isAcceptable()) {
						acceptClientRequest(selector, serverSocket, ConnectionMap);
					}
					
					if (key.isReadable()) {
						SocketChannel client = (SocketChannel) key.channel();
						int clientPort = client.socket().getPort();
						int clientStatus = ConnectionMap.get(clientPort);
						
						switch(clientStatus) {
							case 1:
								readClientBytes(selector, key, films);
								ConnectionMap.put(clientPort, 2);
								break;
							case 2:
								sendFilmsData(selector, key, films);
								ConnectionMap.put(clientPort, 3);
								break;
						}
						
					}
					
					iter.remove();
				}
		
			}
					
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void acceptClientRequest(Selector selector, ServerSocketChannel serverSocket, Map<Integer, Integer> ConnectionMap) throws IOException {
		
		SocketChannel client = serverSocket.accept();
		System.out.println("Client connesso " + client.getRemoteAddress());
		ConnectionMap.put(client.socket().getPort(), 1);
		// setting non blocking channel
		client.configureBlocking(false);
		client.register(selector, SelectionKey.OP_READ);
		
	}
	
	/*
	 * function that checks if client send the correct connection id (1)
	 * 
	 */
	private static void readClientBytes(Selector selector, SelectionKey key, Map<Integer, String[]> f) throws IOException {
		
		
		SocketChannel client = (SocketChannel) key.channel();
		
		int BUFFER_SIZE = 1024;
		
		// allocating reading buffer
		ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
		
		try {
			if (client.read(buffer) == -1) {
				client.close();
				return;
			}
			
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		// decoding buffer content
		buffer.flip();
		Charset charset = Charset.forName("UTF-8");
		CharsetDecoder decoder = charset.newDecoder();
		CharBuffer charBuffer = decoder.decode(buffer);
		
		char c = charBuffer.get();
		charBuffer.clear();
		System.out.println("[server] code received by client: " + (((int)c % 49) + 1));
		
		// allocating sending buffer 
		ByteBuffer toSendBuffer = ByteBuffer.allocate(BUFFER_SIZE);
		
		// checking first character sent by client
		
		if (c == '1') {
			
			System.out.println("[server] right id number.");
			toSendBuffer.put((byte) 1);
			toSendBuffer.put((byte) '\n');
		}
		else {
			System.out.println("[server] wrong id number.");
			toSendBuffer.put((byte) 0);
			toSendBuffer.put((byte) '\n');
			
		}
		toSendBuffer.flip();
		System.out.println("[server] sending buffer to client");
		client.register(selector, SelectionKey.OP_WRITE);
		
		System.out.println("[server] byte written: " + client.write(toSendBuffer));
		client.register(selector, SelectionKey.OP_READ);
	}
	
	
	private static void sendFilmsData (Selector selector, SelectionKey key, Map<Integer, String[]> films) throws ClosedChannelException {
		
		
		SocketChannel client = (SocketChannel) key.channel();
		
		int BUFFER_SIZE = 1024;
		
		// allocating reading buffer
		client.register(selector, SelectionKey.OP_READ);
		ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
		try {
			if (client.read(buffer) == -1) {
				client.close();
				return;
			}
			
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		buffer.flip();
		Charset charset = Charset.forName("UTF-8");
		CharsetDecoder decoder = charset.newDecoder();
		
		CharBuffer charBuffer;
		String FilmString = null;
		try {
			charBuffer = decoder.decode(buffer);
			char dayc = charBuffer.get();
			System.out.println("day id " + dayc);
			String[] dayFilms = films.get(((int) dayc) % 49 + 1);
			FilmString = filmString(dayFilms);
			
			//System.out.println(filmString(dayFilms));
		} catch (CharacterCodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		client.register(selector, SelectionKey.OP_WRITE);
		
		if (FilmString == null) {
			System.out.println("[server] Ops, something went wrong...");
		}
		else {
			// sending FilmString to client
			System.out.println("[server] trying to send film infos to client");
			byte[] FilmStringBytes = FilmString.getBytes();
			ByteBuffer FilmStringBuffer = ByteBuffer.allocate(FilmStringBytes.length + 1);
			FilmStringBuffer.put(FilmStringBytes);
			FilmStringBuffer.flip();
			try {
				System.out.println("[server] byte written: " + client.write(FilmStringBuffer));
				System.out.println("[server] comunication end.");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		// receiving day id from client
	}
	
	
	public static String filmString (String[] films) {
		String output = "";
		
		for (String s: films) {
			output += s + " ";
		}
		
		return output;
		
	}
}
 