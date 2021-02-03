package com.shimizukenta.test;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import com.shimizukenta.jsoncommunicator.JsonCommunicator;
import com.shimizukenta.jsoncommunicator.JsonCommunicators;
import com.shimizukenta.jsonhub.JsonHub;

public class TestComm {
	
	public int num;
	public String str;
	public boolean bool;
	public Object nul;
//	public Object[] array;
	public String[] array;
	
	public TestComm() {
		num = 0;
		str = "str";
		bool = true;
		nul = null;
//		array = new Object[] {"AAA", Integer.valueOf(100), Boolean.TRUE, null};
		array = new String[] {"AAA", "BBB", "CCC"};
	}
	
	public static void main(String[] args) {
		
		final SocketAddress addr = new InetSocketAddress("127.0.0.1", 10000);
		
		try (
				JsonCommunicator<TestComm> server = JsonCommunicators.createServer(addr, TestComm.class);
				) {
			
			server.addLogListener(log -> {
				staticEcho(log);
			});
			
			server.addPojoReceiveListener(pojo -> {
				staticEcho(JsonHub.fromPojo(pojo).prettyPrint());
			});
			
			server.open();
			
			Thread.sleep(1000L);
			
			try (
					JsonCommunicator<?> client = JsonCommunicators.createClient(addr);
					) {
				
				client.addLogListener(log -> {
					staticEcho(log);
				});
				
				client.open();
				
				Thread.sleep(1000L);
				
				client.send(new TestComm());
				
				Thread.sleep(500L);
			}
		}
		catch ( Throwable t ) {
			staticEcho(t);
		}
	}
	
	private static final Object staticSyncEcho = new Object();
	
	private static void staticEcho(Object o) {
		
		synchronized ( staticSyncEcho ) {
			
			if ( o instanceof Throwable ) {
				
				try (
						StringWriter sw = new StringWriter();
						) {
					
					try (
							PrintWriter pw = new PrintWriter(sw);
							) {
						
						((Throwable) o).printStackTrace(pw);
						pw.flush();
						
						System.out.println(sw.toString());
					}
				}
				catch ( IOException e ) {
					e.printStackTrace();
				}

			} else {
				System.out.println(o);
			}
			
			System.out.println();
		}
	}

}
