package com.shimizukenta.jsoncommunicator;

import java.io.IOException;
import java.net.SocketAddress;

public final class JsonCommunicators {

	private JsonCommunicators() {
		/* Nothing */
	}
	
	public static JsonCommunicator<?> createServer(SocketAddress addr) {
		JsonCommunicatorConfig config = new JsonCommunicatorConfig();
		config.addBind(addr);
		return newInstance(config);
	}
	
	public static <T> JsonCommunicator<T> createServer(SocketAddress addr, Class<T> classOfT) {
		JsonCommunicatorConfig config = new JsonCommunicatorConfig();
		config.addBind(addr);
		return newInstance(config, classOfT);
	}
	
	public static JsonCommunicator<?> openServer(SocketAddress addr) throws IOException {
		final JsonCommunicator<?> inst = createServer(addr);
		tryOpen(inst);
		return inst;
	}
	
	public static <T> JsonCommunicator<T> openServer(SocketAddress addr, Class<T> classOfT) throws IOException {
		final JsonCommunicator<T> inst = createServer(addr, classOfT);
		tryOpen(inst);
		return inst;
	}
	
	public static JsonCommunicator<?> createClient(SocketAddress addr) {
		JsonCommunicatorConfig config = new JsonCommunicatorConfig();
		config.addConnect(addr);
		return newInstance(config);
	}
	
	public static <T> JsonCommunicator<T> createClient(SocketAddress addr, Class<T> classOfT) {
		JsonCommunicatorConfig config = new JsonCommunicatorConfig();
		config.addConnect(addr);
		return newInstance(config, classOfT);
	}
	
	public static JsonCommunicator<?> openClient(SocketAddress addr) throws IOException {
		final JsonCommunicator<?> inst = createClient(addr);
		tryOpen(inst);
		return inst;
	}
	
	public static <T> JsonCommunicator<T> openClient(SocketAddress addr, Class<T> classOfT) throws IOException {
		final JsonCommunicator<T> inst = createClient(addr, classOfT);
		tryOpen(inst);
		return inst;
	}
	
	public static JsonCommunicator<?> newInstance(JsonCommunicatorConfig config) {
		return new JsonHubCommunicator<Object>(config);
	}
	
	public static <T> JsonCommunicator<T> newInstance(JsonCommunicatorConfig config, Class<T> classOfT) {
		return new JsonHubCommunicator<T>(config, classOfT);
	}
	
	public static JsonCommunicator<?> open(JsonCommunicatorConfig config) throws IOException {
		final JsonCommunicator<?> inst = newInstance(config);
		tryOpen(inst);
		return inst;
	}
	
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
