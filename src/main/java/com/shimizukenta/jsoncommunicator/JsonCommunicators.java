package com.shimizukenta.jsoncommunicator;

import java.io.IOException;
import java.net.SocketAddress;

public final class JsonCommunicators {

	private JsonCommunicators() {
		/* Nothing */
	}
	
	/**
	 * create JsonCommunicator instance for Server<br />
	 * 
	 * @param addr
	 * @return JsonCommunicator<?> instance
	 */
	public static JsonCommunicator<?> createServer(SocketAddress addr) {
		JsonCommunicatorConfig config = new JsonCommunicatorConfig();
		config.addBind(addr);
		return newInstance(config);
	}
	
	/**
	 * create JsonCommunicator instance for Server<br />
	 * this instance can parse to class of T POJO<br />
	 * 
	 * @param <T>
	 * @param addr
	 * @param classOfT
	 * @return JsonCommunicator<T> instance
	 */
	public static <T> JsonCommunicator<T> createServer(SocketAddress addr, Class<T> classOfT) {
		JsonCommunicatorConfig config = new JsonCommunicatorConfig();
		config.addBind(addr);
		return newInstance(config, classOfT);
	}
	
	/**
	 * create and open JsonCommunicator instance for Server<br />
	 * 
	 * @param addr
	 * @return JsonCommunicator<?> instance
	 * @throws IOException
	 */
	public static JsonCommunicator<?> openServer(SocketAddress addr) throws IOException {
		final JsonCommunicator<?> inst = createServer(addr);
		tryOpen(inst);
		return inst;
	}
	
	/**
	 * create and open JsonCommunicator instance for Server<br />
	 * this instance can parse to class of T POJO<br />
	 * 
	 * @param <T>
	 * @param addr
	 * @param classOfT
	 * @return JsonCommunicator<T> instance
	 * @throws IOException
	 */
	public static <T> JsonCommunicator<T> openServer(SocketAddress addr, Class<T> classOfT) throws IOException {
		final JsonCommunicator<T> inst = createServer(addr, classOfT);
		tryOpen(inst);
		return inst;
	}
	
	/**
	 * create JsonCommunicator instance for Client<br />
	 * 
	 * @param addr
	 * @return JsonCommunicator<?> instance
	 */
	public static JsonCommunicator<?> createClient(SocketAddress addr) {
		JsonCommunicatorConfig config = new JsonCommunicatorConfig();
		config.addConnect(addr);
		return newInstance(config);
	}
	
	/**
	 * create JsonCommunicator instance for Client<br />
	 * this instance can parse to class of T POJO<br />
	 * 
	 * @param <T>
	 * @param addr
	 * @param classOfT
	 * @return JsonCommunicator<T> instance
	 */
	public static <T> JsonCommunicator<T> createClient(SocketAddress addr, Class<T> classOfT) {
		JsonCommunicatorConfig config = new JsonCommunicatorConfig();
		config.addConnect(addr);
		return newInstance(config, classOfT);
	}
	
	/**
	 * create and open JsonCommunicator instance for Client<br />
	 * 
	 * @param addr
	 * @return
	 * @throws IOException
	 */
	public static JsonCommunicator<?> openClient(SocketAddress addr) throws IOException {
		final JsonCommunicator<?> inst = createClient(addr);
		tryOpen(inst);
		return inst;
	}
	
	/**
	 * create and open JsonCommunicator instance for Client<br />
	 * this instance can parse to class of T POJO<br />
	 * 
	 * @param <T>
	 * @param addr
	 * @param classOfT
	 * @return JsonCommunicator<T> instance
	 * @throws IOException
	 */
	public static <T> JsonCommunicator<T> openClient(SocketAddress addr, Class<T> classOfT) throws IOException {
		final JsonCommunicator<T> inst = createClient(addr, classOfT);
		tryOpen(inst);
		return inst;
	}
	
	/**
	 * create JsonCommunicator by JsonCommunicatorConfig
	 * 
	 * @param config
	 * @return JsonCommunicator<?> instance
	 */
	public static JsonCommunicator<?> newInstance(JsonCommunicatorConfig config) {
		return new JsonHubCommunicator<Object>(config);
	}
	
	/**
	 * create JsonCommunicator by JsonCommunicatorConfig
	 * this instance can parse to class of T POJO<br />
	 * 
	 * @param <T>
	 * @param config
	 * @param classOfT
	 * @return JsonCommunicator<T> instance
	 */
	public static <T> JsonCommunicator<T> newInstance(JsonCommunicatorConfig config, Class<T> classOfT) {
		return new JsonHubCommunicator<T>(config, classOfT);
	}
	
	/**
	 * create and open JsonCommunicator by JsonCommunicatorConfig
	 * 
	 * @param config
	 * @return JsonCommunicator<?> instance
	 * @throws IOException
	 */
	public static JsonCommunicator<?> open(JsonCommunicatorConfig config) throws IOException {
		final JsonCommunicator<?> inst = newInstance(config);
		tryOpen(inst);
		return inst;
	}
	
	/**
	 * create and open JsonCommunicator by JsonCommunicatorConfig
	 * this instance can parse to class of T POJO<br />
	 * 
	 * @param <T>
	 * @param config
	 * @param classOfT
	 * @return JsonCommunicator<T> instance
	 * @throws IOException
	 */
	public static <T> JsonCommunicator<T> open(JsonCommunicatorConfig config, Class<T> classOfT) throws IOException {
		final JsonCommunicator<T> inst = newInstance(config, classOfT);
		tryOpen(inst);
		return inst;
	}
	
	private static void tryOpen(JsonCommunicator<?> comm) throws IOException {
		try {
			comm.open();
		}
		catch ( IOException e ) {
			try {
				comm.close();
			}
			catch ( IOException giveup ) {
			}
			throw e;
		}
	}

}
