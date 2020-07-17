package com.shimizukenta.jsoncommunicator;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.AsynchronousSocketChannel;

public interface JsonCommunicator<T> extends Closeable {
	
	public boolean isOpen();
	public boolean isClosed();
	
	public void open() throws IOException;
	
	public void send(CharSequence json) throws InterruptedException, IOException;
	public void send(Object pojo) throws InterruptedException, IOException, JsonCommunicatorParseException;
	public void send(AsynchronousSocketChannel channel, CharSequence json) throws InterruptedException, IOException;
	public void send(AsynchronousSocketChannel channel, Object pojo) throws InterruptedException, IOException, JsonCommunicatorParseException;
	
	public boolean addJsonReceivedListener(JsonCommunicatorJsonReceivedListener l);
	public boolean removeJsonReceivedListener(JsonCommunicatorJsonReceivedListener l);
	
	public boolean addPojoReceivedListener(JsonCommunicatorPojoReceivedListener<T> l);
	public boolean removePojoReceivedListener(JsonCommunicatorPojoReceivedListener<T> l);
	
	public boolean addConnectionStateChangedListener(JsonCommunicatorConnectionStateChangedListener l);
	public boolean removeConnectionStateChangedListener(JsonCommunicatorConnectionStateChangedListener l);
	
	public boolean addLogListener(JsonCommunicatorLogListener l);
	public boolean removeLogListener(JsonCommunicatorLogListener l);

}
