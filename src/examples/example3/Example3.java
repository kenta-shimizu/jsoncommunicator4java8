package example3;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import com.shimizukenta.jsoncommunicator.JsonCommunicator;
import com.shimizukenta.jsoncommunicator.JsonCommunicatorParseException;
import com.shimizukenta.jsoncommunicator.JsonCommunicators;

public class Example3 {
	
	public String name;
	
	public Example3() {
		this.name = "????";
	}
	
	public Example3(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		return "My name is " + name + ".";
	}
	
	public static void main(String[] args) {

		final SocketAddress addr = new InetSocketAddress("127.0.0.1", 10000);
		
		try (
				JsonCommunicator<Example3> server = JsonCommunicators.openServer(addr, Example3.class);
				) {
			
			server.addPojoReceivedListener(pojo -> {
				System.out.println("receive: " + pojo);
			});
			
			/* wait until binded */
			Thread.sleep(1000L);
			
			try (
					JsonCommunicator<?> client = JsonCommunicators.openClient(addr);
					) {
				
				/* wait until connected */
				Thread.sleep(1000L);
				
				client.send(new Example3("John"));
				
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
