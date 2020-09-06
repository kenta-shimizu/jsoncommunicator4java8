package com.shimizukenta.jsoncommunicatortester;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.shimizukenta.jsoncommunicator.JsonCommunicator;
import com.shimizukenta.jsoncommunicator.JsonCommunicators;
import com.shimizukenta.jsonhub.JsonHub;
import com.shimizukenta.jsonhub.JsonHubParseException;

public final class JsonCommunicatorTester implements Closeable {
	
	private final ExecutorService execServ = Executors.newCachedThreadPool(r -> {
		Thread th = new Thread(r);
		th.setDaemon(true);
		return th;
	});
	
	private final Map<String, String> jsonMap = new HashMap<>();
	
	private final JsonCommunicatorTesterConfig config;
	
	private boolean opened;
	private boolean closed;
	private JsonCommunicator<?> jsonComm;
	
	public JsonCommunicatorTester(JsonCommunicatorTesterConfig config) {
		this.config = config;
		this.opened = false;
		this.closed = false;
		this.jsonComm = null;
	}
	
	public void open() throws IOException {
		
		synchronized ( this ) {
			if ( this.closed ) {
				throw new IOException("Already closed");
			}
			
			if ( this.opened ) {
				throw new IOException("Already opened");
			}
			
			this.opened = true;
		}
		
		execServ.execute(this.createLogQueueTask());
		execServ.execute(this.createInpuTask());
		
		config.jsonPaths().forEach(this::readJson);
		
		if ( config.autoOpen() ) {
			
			try {
				openCommunicator();
			}
			catch ( IOException e ) {
				notifyLog(e);
			}
		}
	}
	
	public void close() throws IOException {
		
		synchronized ( this ) {
			
			if ( this.closed ) {
				return;
			}
			
			this.closed = true;
			
			IOException ioExcept = null;
			
			try {
				closeCommunicator();
			}
			catch ( IOException e ) {
				ioExcept = e;
			}
			
			try {
				execServ.shutdown();
				if ( ! execServ.awaitTermination(1L, TimeUnit.MILLISECONDS) ) {
					execServ.shutdownNow();
					if ( ! execServ.awaitTermination(5L, TimeUnit.SECONDS) ) {
						ioExcept = new IOException("ExecutorService#shutdown failed");
					}
				}
			}
			catch ( InterruptedException giveup ) {
			}
			
			if ( ioExcept != null ) {
				throw ioExcept;
			}
		}
	}
	
	private void openCommunicator() throws IOException {
		
		synchronized ( this ) {
			
			if ( this.jsonComm != null ) {
				closeCommunicator();
			}
			
			this.jsonComm = JsonCommunicators.newInstance(config.communicator());
			this.jsonComm.addLogListener(this::notifyLog);
			this.jsonComm.open();
		}
	}
	
	private void closeCommunicator() throws IOException {
		synchronized ( this ) {
			if ( this.jsonComm != null ) {
				try {
					jsonComm.close();
				}
				finally {
					jsonComm = null;
				}
			}
		}
	}
	
	private void readJson(Path path) {
		
		try {
			if ( Files.isDirectory(path) ) {
				
				//TODO
				
			} else {
				
				readJsonFile(path);
			}
		}
		catch ( IOException | JsonHubParseException e ) {
			notifyLog(e);
		}
	}
	
	private void readJsonFile(Path path) throws IOException {
		
		String json = JsonHub.fromFile(path).toJson();
	}
	
	private Runnable createInpuTask() {
		
		return new Runnable() {
			
			@Override
			public void run() {
				
				try (
						InputStreamReader isr = new InputStreamReader(System.in);
						) {
					
					try (
							BufferedReader br = new BufferedReader(isr);
							) {
						
						Collection<Callable<Void>> tasks = Arrays.asList(() -> {
							
							try {
								RequestCommandParser parser = RequestCommandParser.getInstance();
								
								for ( ;; ) {
									
									RequestCommand r = parser.parse(br.readLine());
									
									//TODO
									
								}
							}
							catch ( IOException giveup) {
							}
							
							return null;
						});
						
						execServ.invokeAny(tasks);
					}
				}
				catch ( IOException e) {
					notifyLog(e);
				}
				catch ( ExecutionException e ) {
					notifyLog(e.getCause());
				}
				catch ( InterruptedException ignore ) {
				}
			}
		};
	}
	
	private interface InterruptedRunnable {
		public void run() throws InterruptedException;
	}
	
	private Runnable createLoopTask(InterruptedRunnable r) {
		
		return new Runnable() {
			
			@Override
			public void run() {
				try {
					for ( ;; ) {
						r.run();
					}
				}
				catch ( InterruptedException ignore ) {
				}
			}
		};
	}
	
	private final BlockingQueue<Object> logQueue = new LinkedBlockingQueue<>();
	
	private void notifyLog(Object o) {
		logQueue.offer(o);
	}
	
	private Runnable createLogQueueTask() {
		return createLoopTask(() -> {
			echo(logQueue.take());
		});
	}
	
	private static final Object syncEcho = new Object();
	
	private static void echo(Object o) {
		synchronized ( syncEcho ) {
			if ( o instanceof Throwable ) {
				((Throwable) o).printStackTrace();
			} else {
				System.out.print(o);
				System.out.println();
			}
		}
	}
	
	public static void main(String[] args) {
		
		try {
			JsonCommunicatorTesterConfig config = JsonCommunicatorTesterConfig.get(args);
			
			try (
					JsonCommunicatorTester inst = new JsonCommunicatorTester(config);
					) {
				
				inst.open();
				
				synchronized ( JsonCommunicatorTester.class ) {
					JsonCommunicatorTester.class.wait();
				}
			}
			catch ( InterruptedException ignore ) {
			}
		}
		catch ( Throwable t ) {
			echo(t);
		}

	}

}
