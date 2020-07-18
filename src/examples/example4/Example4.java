package example4;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import com.shimizukenta.jsoncommunicator.JsonCommunicator;
import com.shimizukenta.jsoncommunicator.JsonCommunicatorParseException;
import com.shimizukenta.jsoncommunicator.JsonCommunicators;

import example3.Example3;

public class Example4 {
	
	public String name;
	public int count;
	
	public Example4() {
		this.name = "????";
		this.count = 0;
	}
	
	public Example4(String name) {
		this.name = name;
		this.count = 0;
	}
	
	@Override
	public String toString() {
		return "My name is " + name + ". I said hello to you " + count + " times.";
	}
	
	public static void main(String[] args) {
		
		final SocketAddress addr = new InetSocketAddress("127.0.0.1", 10000);
		
		try (
				JsonCommunicator<?> server = JsonCommunicators.openServer(addr);
				) {
			
			server.addConnectionStateChangedListener((channel, state) -> {
				
				switch ( state ) {
				case CONNECTED: {
					
					Thread th = new Thread(() -> {
						
						try {
							Example4 pojo = new Example4("Yoko");
							
							for ( ;; ) {
								Thread.sleep(1000L);
								pojo.count ++;
								server.send(channel, pojo);
							}
						}
						catch ( InterruptedException ignore ) {
						}
						catch ( IOException | JsonCommunicatorParseException e ) {
							e.printStackTrace();
						}
					});
					
					th.setDaemon(true);
					th.start();
					
					break;
				}
				default:
					/* Nothing */
				}
			});
			
			/* wait until binded */
			Thread.sleep(1000L);
			
			try (
					JsonCommunicator<Example4> client = JsonCommunicators.openClient(addr, Example4.class);
					) {
				
				client.addPojoReceivedListener(pojo -> {
					System.out.println("receive: " + pojo);
				});
				
				/* receiveing 10 seconds */
				Thread.sleep(10000L);
			}
		}
		catch ( InterruptedException ignore ) {
		}
		catch (IOException e) {
			e.printStackTrace();
		}

	}

}
