package com.shimizukenta.jsoncommunicator;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.AsynchronousSocketChannel;

public interface JsonCommunicator<T> extends Closeable {
	
	/**
	 * 
	 * @return true if open
	 */
	public boolean isOpen();
	
	/**
	 * 
	 * @return true if closed
	 */
	public boolean isClosed();
	
	/**
	 * 
	 * @throws IOException
	 */
	public void open() throws IOException;
	
	/**
	 * send JSON to all connected channels.<br />
	 * 
	 * @param json
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public void send(CharSequence json) throws InterruptedException, IOException;
	
	/**
	 * send JSON parsed POJO to all connected channels.<br />
	 * POJO is parsed by JsonHub#fromPojo.<br />
	 * 
	 * @param pojo
	 * @throws InterruptedException
	 * @throws IOException
	 * @throws JsonCommunicatorParseException
	 */
	public void send(Object pojo) throws InterruptedException, IOException, JsonCommunicatorParseException;
	
	/**
	 * send JSON to target channel.<br />
	 * 
	 * @param channel
	 * @param json
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public void send(AsynchronousSocketChannel channel, CharSequence json) throws InterruptedException, IOException;
	
	/**
	 * send JSON parsed POJO to target channel.<br />
	 * POJO is parsed by JsonHub#fromPojo<br />
	 * 
	 * @param channel
	 * @param pojo
	 * @throws InterruptedException
	 * @throws IOException
	 * @throws JsonCommunicatorParseException
	 */
	public void send(AsynchronousSocketChannel channel, Object pojo) throws InterruptedException, IOException, JsonCommunicatorParseException;
	
	/**
	 * 
	 * @param l
	 * @return true if add success
	 */
	public boolean addJsonReceivedListener(JsonCommunicatorJsonReceivedListener l);
	
	/**
	 * 
	 * @param l
	 * @return true if remove success
	 */
	public boolean removeJsonReceivedListener(JsonCommunicatorJsonReceivedListener l);
	
	/**
	 * 
	 * @param l
	 * @return true if add success
	 */
	public boolean addJsonReceivedListener(JsonCommunicatorJsonReceivedBiListener l);
	
	/**
	 * 
	 * @param l
	 * @return true if remove success
	 */
	public boolean removeJsonReceivedListener(JsonCommunicatorJsonReceivedBiListener l);
	
	/**
	 * 
	 * @param l
	 * @return true if add success
	 */
	public boolean addPojoReceivedListener(JsonCommunicatorPojoReceivedListener<T> l);
	
	/**
	 * 
	 * @param l
	 * @return true if remove success
	 */
	public boolean removePojoReceivedListener(JsonCommunicatorPojoReceivedListener<T> l);
	
	/**
	 * 
	 * @param l
	 * @return true if add success
	 */
	public boolean addPojoReceivedListener(JsonCommunicatorPojoReceivedBiListener<T> l);
	
	/**
	 * 
	 * @param l
	 * @return true if remove success
	 */
	public boolean removePojoReceivedListener(JsonCommunicatorPojoReceivedBiListener<T> l);
	
	/**
	 * This listener is blocking.<br />
	 * Pass through quickly.<br />
	 * 
	 * @param l
	 * @return true if add success.
	 */
	public boolean addConnectionStateChangedListener(JsonCommunicatorConnectionStateChangedListener l);
	
	/**
	 * 
	 * @param l
	 * @return true if remove success
	 */
	public boolean removeConnectionStateChangedListener(JsonCommunicatorConnectionStateChangedListener l);
	
	/**
	 * This listener is blocking.<br />
	 * Pass through quickly.<br />
	 * 
	 * @param l
	 * @return true if add success.
	 */
	public boolean addConnectionStateChangedListener(JsonCommunicatorConnectionStateChangedBiListener l);
	
	/**
	 * 
	 * @param l
	 * @return true if remove success
	 */
	public boolean removeConnectionStateChangedListener(JsonCommunicatorConnectionStateChangedBiListener l);
	
	/**
	 * 
	 * @param l
	 * @return true if add success
	 */
	public boolean addLogListener(JsonCommunicatorLogListener l);
	
	/**
	 * 
	 * @param l
	 * @return true if remove success
	 */
	public boolean removeLogListener(JsonCommunicatorLogListener l);

}
