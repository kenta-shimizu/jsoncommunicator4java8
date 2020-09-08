package com.shimizukenta.jsoncommunicatortester;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
	
	private final Map<String, JsonHub> jsonMap = new ConcurrentHashMap<>();
	
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
		
		config.jsonPaths().forEach(this::readJson);
		
		notifyLog("Start-Communicator");
		
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
	
	private static final String readFileExtension = ".json";
	
	private void readJson(Path path) {
		
		try {
			if ( Files.isDirectory(path) ) {
				
				try (
						DirectoryStream<Path> ds = Files.newDirectoryStream(path);
						) {
					
					for ( Path p : ds ) {
						readJsonFile(p);
					}
				}
				
			} else {
				
				readJsonFile(path);
			}
		}
		catch ( IOException | JsonHubParseException e ) {
			notifyLog(e);
		}
	}
	
	private void readJsonFile(Path path) throws IOException {
		
		String fileName = path.getFileName().toString();
		
		if ( fileName.endsWith(readFileExtension) ) {
			
			String key = fileName.substring(0, fileName.length() - readFileExtension.length());
			
			if ( ! key.trim().isEmpty() ) {
				
				JsonHub jh = JsonHub.fromFile(path);
				
				jsonMap.put(key, jh);
			}
		}
	}
	
	
	private static final String BR = System.lineSeparator();
	private final RequestCommandParser commandParser = RequestCommandParser.getInstance();
	
	/**
	 * 
	 * @param commandLine
	 * @return true if quit
	 * @throws InterruptedException
	 */
	private boolean executeCommandLine(String commandLine) throws InterruptedException {
		
		try {
			
			RequestCommand req = commandParser.parse(commandLine);
			
			switch ( req.command() ) {
			case MANUAL: {
				
				String option = req.option(0).orElse(null);
				
				if ( option == null ) {
					
					notifyLog(commandParser.getCommandsManual());
					
				} else {
					
					notifyLog(commandParser.getCommandDetailManual(option));
				}
				
				return false;
				/* break; */
			}
			case QUIT: {
				return true;
				/* break; */
			}
			case OPEN: {
				this.openCommunicator();
				return false;
				/* break; */
			}
			case CLOSE: {
				this.closeCommunicator();
				return false;
				/* break; */
			}
			case LIST: {
				
				String s = jsonMap.keySet().stream()
						.sorted()
						.collect(Collectors.joining(BR));
				
				notifyLog(s);
				return false;
				/* break; */
			}
			case SHOW: {
				
				String key = req.option(0).orElse(null);
				if ( key != null ) {
					
					JsonHub jh = jsonMap.get(key);
					
					if ( jh == null ) {
						
						notifyLog("\"" + key + "\" is not read.");
						
					} else {
						
						notifyLog(jh.prettyPrint());
					}
				}
				return false;
				/* break; */
			}
			case SEND: {
				
				String key = req.option(0).orElse(null);
				if ( key != null ) {
					
					synchronized ( this ) {
						
						if ( jsonComm != null && jsonComm.isOpen() ) {
							
							JsonHub jh = jsonMap.get(key);
							
							if ( jh == null ) {
								
								notifyLog("\"" + key + "\" is not read.");
								
							} else {
								
								jsonComm.send(jh.toJson());
							}
							
						} else {
						
							notifyLog("Communicator not opened.");
						}
					}
				}
				return false;
				/* break; */
			}
			default: {
				
				/* Nothing */
				return false;
				/* break; */
			}
			}
		}
		catch ( IOException | JsonHubParseException e ) {
			notifyLog(e);
		}
		
		return false;
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
				
				try (
						InputStreamReader isr = new InputStreamReader(System.in);
						) {
					
					try (
							BufferedReader br = new BufferedReader(isr);
							) {
						
						for ( ;; ) {
							String line = br.readLine();
							
							if ( line == null ) {
								break;
							}
							
							if ( line.trim().isEmpty() ) {
								continue;
							}
							
							if ( inst.executeCommandLine(line) ) {
								break;
							}
						}
					}
					catch ( InterruptedException ignore ) {
					}
				}
			}
		}
		catch ( Throwable t ) {
			echo(t);
		}

	}

}
