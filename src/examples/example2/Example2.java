package example2;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import com.shimizukenta.jsoncommunicator.JsonCommunicator;
import com.shimizukenta.jsoncommunicator.JsonCommunicatorParseException;
import com.shimizukenta.jsoncommunicator.JsonCommunicators;

public class Example2 {
	
	public String name;
	
	public Example2(String name) {
		this.name = name;
	}

	public static void main(String[] args) {
		
		final SocketAddress addr = new InetSocketAddress("127.0.0.1", 10000);
		
		try (
				JsonCommunicator<?> server = JsonCommunicators.openServer(addr);
				) {
			
			server.addJsonReceiveListener(json -> {
				System.out.println("receive: \"" + json + "\"");
			});
			
			/* wait until binded */
			Thread.sleep(1000L);
			
			try (
					JsonCommunicator<?> client = JsonCommunicators.openClient(addr);
					) {
				
				/* wait until connected */
				Thread.sleep(1000L);
				
				client.send(new Example2("John"));
				
				/* wait until sended */
				Thread.sleep(1000L);
			}
		}
		catch ( InterruptedException ignore ) {
		}
		catch (IOException | JsonCommunicatorParseException e) {
			e.printStackTrace();
		}
	}

}
