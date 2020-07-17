package com.shimizukenta.jsoncommunicator;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.SocketAddress;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public abstract class AbstractJsonCommunicator<T> implements JsonCommunicator<T> {
	
	private static final byte DELIMITER = (byte)0x0;
	
	private final ExecutorService execServ = Executors.newCachedThreadPool(r -> {
		Thread th = new Thread(r);
		th.setDaemon(true);
		return th;
	});
	
	private final JsonCommunicatorConfig config;
	
	private boolean opened;
	private boolean closed;
	
	public AbstractJsonCommunicator(JsonCommunicatorConfig config) {
		this.config = config;
		this.opened = false;
		this.closed = false;
	}
	
	protected ExecutorService executorService() {
		return this.execServ;
	}
	
	@Override
	public boolean isOpen() {
		synchronized ( this ) {
			return this.opened;
		}
	}
	
	@Override
	public boolean isClosed() {
		synchronized ( this ) {
			return this.closed;
		}
	}
	
	@Override
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
		
		execServ.execute(this.createRecvJsonTask());
		execServ.execute(this.createRecvPojoTask());
		execServ.execute(this.createLogTask());
		
		config.binds().forEach(addr -> {
			execServ.execute(createLoopTask(() -> {
				this.openBind(addr);
				long t = (long)(config.rebindSeconds() * 1000.0F);
				if ( t > 0 ) {
					TimeUnit.MILLISECONDS.sleep(t);
				}
			}));
		});
		
		config.connects().forEach(addr -> {
			execServ.execute(createLoopTask(() -> {
				this.openConnect(addr);
				long t = (long)(config.reconnectSeconds() * 1000.0F);
				if ( t > 0 ) {
					TimeUnit.MILLISECONDS.sleep(t);
				}
			}));
		});
	}
	
	@Override
	public void close() throws IOException {
		
		synchronized ( this ) {
			if ( this.closed ) {
				return;
			}
			
			this.closed = true;
		}
		
		try {
			execServ.shutdown();
			if ( ! execServ.awaitTermination(1L, TimeUnit.MILLISECONDS) ) {
				execServ.shutdownNow();
				if ( ! execServ.awaitTermination(5L, TimeUnit.SECONDS) ) {
					throw new IOException("ExecutorService#shutdown failed");
				}
			}
		}
		catch ( InterruptedException giveup ) {
		}
	}
	
	private Collection<AsynchronousSocketChannel> channels = new CopyOnWriteArrayList<>();
	
	private void openBind(SocketAddress addr) throws InterruptedException {
		
		try (
				AsynchronousServerSocketChannel server = AsynchronousServerSocketChannel.open();
				) {
			
			server.bind(addr);
			
			putLog("server-binded", addr);
			
			server.accept(null, new CompletionHandler<AsynchronousSocketChannel, Void>() {

				@Override
				public void completed(AsynchronousSocketChannel channel, Void attachment) {
					
					server.accept(null, this);
					
					final String channelStr = channel.toString();
					
					try {
						channels.add(channel);
						putLog("channel-accepted", channelStr);
						stateChanged(channel, JsonCommunicatorConnectionState.CONNECTED);
						
						reading(channel);
					}
					catch ( InterruptedException ignore ) {
					}
					finally {
						
						channels.remove(channel);
						
						try {
							channel.shutdownOutput();
						}
						catch ( IOException giveup ) {
						}
						
						try {
							channel.close();
						}
						catch ( IOException e ) {
							putLog(e);
						}
						
						putLog("channel-closed", channelStr);
						stateChanged(channel, JsonCommunicatorConnectionState.NOT_CONNECTED);
					}
				}

				@Override
				public void failed(Throwable t, Void attachment) {
					putLog(t);
					synchronized ( server ) {
						server.notifyAll();
					}
				}
			});
			
			synchronized ( server ) {
				server.wait();
			}
		}
		catch ( IOException e ) {
			putLog(e);
		}
		finally {
			putLog("server-closed", addr);
		}
	}
	
	private void openConnect(SocketAddress addr) throws InterruptedException {
		
		try (
				AsynchronousSocketChannel channel = AsynchronousSocketChannel.open();
				) {
			
			putLog("try-connect", addr);
			
			channel.connect(addr, null, new CompletionHandler<Void, Void>() {

				@Override
				public void completed(Void result, Void attachment) {
					
					final String channelStr = channel.toString();
					
					try {
						channels.add(channel);
						putLog("channel-connected", channelStr);
						stateChanged(channel, JsonCommunicatorConnectionState.CONNECTED);
						
						reading(channel);
					}
					catch ( InterruptedException ignore ) {
					}
					finally {
						
						channels.remove(channel);
						putLog("channel-disconnected", channelStr);
						
						try {
							channel.shutdownOutput();
						}
						catch ( IOException giveup ) {
						}
						
						stateChanged(channel, JsonCommunicatorConnectionState.NOT_CONNECTED);
						
						synchronized ( channel ) {
							channel.notifyAll();
						}
					}
				}

				@Override
				public void failed(Throwable t, Void attachment) {
					putLog(t);
					synchronized ( channel ) {
						channel.notifyAll();
					}
				}
			});
			
			synchronized ( channel ) {
				channel.wait();
			}
		}
		catch ( IOException e ) {
			putLog(e);
		}
	}
	
	private void reading(AsynchronousSocketChannel channel) throws InterruptedException {
		
		final Collection<Callable<Object>> tasks = Arrays.asList(createReadingTask(channel));
		
		try {
			execServ.invokeAny(tasks);
		}
		catch ( ExecutionException e ) {
			putLog(e.getCause());
		}
	}
	
	private Callable<Object> createReadingTask(AsynchronousSocketChannel channel) {
		
		return new Callable<Object>() {
			
			@Override
			public Object call() throws Exception {
				
				try (
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						) {
					
					final ByteBuffer buffer = ByteBuffer.allocate(1024);
					
					for ( ;; ) {
						
						((Buffer)buffer).clear();
						
						final Future<Integer> f = channel.read(buffer);
						
						try {
							int r = f.get().intValue();
							
							if ( r < 0 ) {
								return null;
							}
							
							((Buffer)buffer).flip();
							
							while ( buffer.hasRemaining() ) {
								byte b = buffer.get();
								if ( b == DELIMITER ) {
									byte[] bs = baos.toByteArray();
									putReceivedBytes(channel, bs);
									baos.reset();
								} else {
									baos.write(b);
								}
							}
						}
						catch ( ExecutionException e ) {
							putLog(e.getCause());
							return null;
						}
						catch ( InterruptedException e ) {
							f.cancel(true);
							throw e;
						}
					}
				}
				catch ( IOException e ) {
					putLog(e);
				}
				catch ( InterruptedException ignore ) {
				}
				
				return null;
			}
		};
	}
	
	abstract protected void putReceivedBytes(AsynchronousSocketChannel channel, byte[] bs);
	
	private final Object syncSend = new Object();
	
	@Override
	public void send(CharSequence json) throws InterruptedException, IOException {
		synchronized ( syncSend ) {
			send(channels, json);
		}
	}
	
	@Override
	public void send(Object pojo) throws InterruptedException, IOException, JsonCommunicatorParseException {
		synchronized ( syncSend ) {
			send(channels, pojo);
		}
	}
	
	@Override
	public void send(AsynchronousSocketChannel channel, CharSequence json) throws InterruptedException, IOException {
		synchronized ( syncSend ) {
			if ( channel != null ) {
				send(Arrays.asList(channel), json);
			}
		}
	}
	
	@Override
	public void send(AsynchronousSocketChannel channel, Object pojo) throws InterruptedException, IOException, JsonCommunicatorParseException {
		synchronized ( syncSend ) {
			if ( channel != null ) {
				send(Arrays.asList(channel), pojo);
			}
		}
	}
	
	private void send(Collection<AsynchronousSocketChannel> channels, CharSequence json) throws InterruptedException, IOException {
		if ( json != null ) {
			byte[] bs = json.toString().getBytes(StandardCharsets.UTF_8);
			send(channels,bs);
		}
	}
	
	private void send(Collection<AsynchronousSocketChannel> channels, Object pojo) throws InterruptedException, IOException, JsonCommunicatorParseException {
		byte[] bs = createBytesFromPojo(pojo);
		send(channels, bs);
	}
	
	private void send(Collection<AsynchronousSocketChannel> channels, byte[] bs) throws InterruptedException, IOException {
		
		final Collection<Callable<Object>> tasks = channels.stream()
				.map(ch -> createSendTask(ch, bs))
				.collect(Collectors.toList());
		
		final List<Future<Object>> results = execServ.invokeAll(tasks);
		
		IOException ioExcept = null;
		
		for ( Future<Object> f : results ) {
			
			try {
				f.get();
			}
			catch ( ExecutionException e ) {
				Throwable t = e.getCause();
				putLog(t);
				if ( t instanceof IOException ) {
					ioExcept = (IOException)t;
				}
			}
		}
		
		if ( ioExcept != null ) {
			throw ioExcept;
		}
	}
	
	private Callable<Object> createSendTask(AsynchronousSocketChannel channel, byte[] bs) {
		
		return new Callable<Object>() {
			
			@Override
			public Object call() throws Exception {
				
				final ByteBuffer buffer = ByteBuffer.allocate(bs.length + 1);
				buffer.put(bs);
				buffer.put(DELIMITER);
				((Buffer)buffer).flip();
				
				try {
					while ( buffer.hasRemaining() ) {
						
						final Future<Integer> f = channel.write(buffer);
						
						try {
							int w = f.get().intValue();
							
							if ( w <= 0 ) {
								return null;
							}
						}
						catch ( InterruptedException e ) {
							f.cancel(true);
							throw e;
						}
						catch ( ExecutionException e ) {
							Throwable t = e.getCause();
							if (t instanceof Exception) {
								throw (Exception)t;
							} else {
								putLog(t);
								return null;
							}
						}
					}
				}
				catch ( InterruptedException ignore ) {
				}
				
				return null;
			}
		};
	}
	
	abstract protected byte[] createBytesFromPojo(Object pojo) throws JsonCommunicatorParseException;
	
	
	private final Collection<JsonCommunicatorJsonReceivedListener> recvJsonLstnrs = new CopyOnWriteArrayList<>();
	
	@Override
	public boolean addJsonReceivedListener(JsonCommunicatorJsonReceivedListener l) {
		return recvJsonLstnrs.add(l);
	}
	
	@Override
	public boolean removeJsonReceivedListener(JsonCommunicatorJsonReceivedListener l) {
		return recvJsonLstnrs.remove(l);
	}
	
	private final Collection<JsonCommunicatorJsonReceivedBiListener> recvJsonBiLstnrs = new CopyOnWriteArrayList<>();
	
	@Override
	public boolean addJsonReceivedListener(JsonCommunicatorJsonReceivedBiListener l) {
		return recvJsonBiLstnrs.add(l);
	}
	
	@Override
	public boolean removeJsonReceivedListener(JsonCommunicatorJsonReceivedBiListener l) {
		return recvJsonBiLstnrs.remove(l);
	}
	
	private class RecvJsonPack {
		private final AsynchronousSocketChannel channel;
		private final String json;
		private RecvJsonPack(AsynchronousSocketChannel channel, String json) {
			this.channel = channel;
			this.json = json;
		}
	}
	
	private final BlockingQueue<RecvJsonPack> recvJsonPackQueue = new LinkedBlockingQueue<>();
	
	private Runnable createRecvJsonTask() {
		return createLoopTask(() -> {
			final RecvJsonPack p = recvJsonPackQueue.take();
			recvJsonLstnrs.forEach(l -> {
				l.receive(p.json);
			});
			recvJsonBiLstnrs.forEach(l -> {
				l.receive(p.channel, p.json);
			});
		});
	}
	
	protected void receiveJson(AsynchronousSocketChannel channel, String json) {
		recvJsonPackQueue.offer(new RecvJsonPack(channel, json));
	}
	
	private final Collection<JsonCommunicatorPojoReceivedListener<T>> recvPojoLstnrs = new CopyOnWriteArrayList<>();
	
	public boolean addPojoReceivedListener(JsonCommunicatorPojoReceivedListener<T> l) {
		return recvPojoLstnrs.add(l);
	}
	
	public boolean removePojoReceivedListener(JsonCommunicatorPojoReceivedListener<T> l) {
		return recvPojoLstnrs.remove(l);
	}
	
	private final Collection<JsonCommunicatorPojoReceivedBiListener<T>> recvPojoBiLstnrs = new CopyOnWriteArrayList<>();
	
	public boolean addPojoReceivedListener(JsonCommunicatorPojoReceivedBiListener<T> l) {
		return recvPojoBiLstnrs.add(l);
	}
	
	public boolean removePojoReceivedListener(JsonCommunicatorPojoReceivedBiListener<T> l) {
		return recvPojoBiLstnrs.remove(l);
	}
	
	private class RecvPojoPack {
		private final AsynchronousSocketChannel channel;
		private final T pojo;
		private RecvPojoPack(AsynchronousSocketChannel channel, T pojo) {
			this.channel = channel;
			this.pojo = pojo;
		}
	}
	
	private final BlockingQueue<RecvPojoPack> recvPojoQueue = new LinkedBlockingQueue<>();
	
	private Runnable createRecvPojoTask() {
		return createLoopTask(() -> {
			final RecvPojoPack p = recvPojoQueue.take();
			recvPojoLstnrs.forEach(l -> {
				l.receive(p.pojo);
			});
			recvPojoBiLstnrs.forEach(l -> {
				l.receive(p.channel, p.pojo);
			});
		});
	}
	
	protected void receivePojo(AsynchronousSocketChannel channel, T pojo) {
		recvPojoQueue.offer(new RecvPojoPack(channel, pojo));
	}
	
	private final Collection<JsonCommunicatorConnectionStateChangedListener> stateChangedLstnrs = new CopyOnWriteArrayList<>();
	
	@Override
	public boolean addConnectionStateChangedListener(JsonCommunicatorConnectionStateChangedListener l) {
		return stateChangedLstnrs.add(l);
	}
	
	@Override
	public boolean removeConnectionStateChangedListener(JsonCommunicatorConnectionStateChangedListener l) {
		return stateChangedLstnrs.remove(l);
	}
	
	private final Collection<JsonCommunicatorConnectionStateChangedBiListener> stateChangedBiLstnrs = new CopyOnWriteArrayList<>();
	
	@Override
	public boolean addConnectionStateChangedListener(JsonCommunicatorConnectionStateChangedBiListener l) {
		return stateChangedBiLstnrs.add(l);
	}
	
	@Override
	public boolean removeConnectionStateChangedListener(JsonCommunicatorConnectionStateChangedBiListener l) {
		return stateChangedBiLstnrs.remove(l);
	}
	
	protected void stateChanged(AsynchronousSocketChannel channel, JsonCommunicatorConnectionState state) {
		stateChangedLstnrs.forEach(l -> {
			l.changed(state);
		});
		stateChangedBiLstnrs.forEach(l -> {
			l.changed(channel, state);
		});
	}
	
	private final Collection<JsonCommunicatorLogListener> logLstnrs = new CopyOnWriteArrayList<>();
	
	public boolean addLogListener(JsonCommunicatorLogListener l) {
		return logLstnrs.add(l);
	}
	
	public boolean removeLogListener(JsonCommunicatorLogListener l) {
		return logLstnrs.remove(l);
	}
	
	private final BlockingQueue<JsonCommunicatorLog> logQueue = new LinkedBlockingQueue<>();
	
	private Runnable createLogTask() {
		return createLoopTask(() -> {
			JsonCommunicatorLog log = logQueue.take();
			logLstnrs.forEach(l -> {
				l.put(log);
			});
		});
	}
	
	protected void putLog(JsonCommunicatorLog log) {
		logQueue.offer(log);
	}
	
	protected void putLog(CharSequence subject) {
		putLog(new JsonCommunicatorLog(subject));
	}
	
	protected void putLog(CharSequence subject, Object value) {
		putLog(new JsonCommunicatorLog(subject, value));
	}
	
	protected void putLog(Throwable t) {
		putLog(new JsonCommunicatorLog(t));
	}
	
	protected static interface InterruptableRunnable {
		public void run() throws InterruptedException;
	}
	
	protected static Runnable createLoopTask(InterruptableRunnable r) {
		return new Runnable() {
			@Override
			public void run() {
				try {
					for ( ;; ) {
						r.run();
					}
				}
				catch (InterruptedException ignore) {
				}
			}
		};
	}

}
