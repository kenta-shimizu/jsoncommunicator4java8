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
	public boolean addJsonReceiveListener(JsonCommunicatorJsonReceiveListener l);
	
	/**
	 * 
	 * @param l
	 * @return true if remove success
	 */
	public boolean removeJsonReceiveListener(JsonCommunicatorJsonReceiveListener l);
	
	/**
	 * 
	 * @param l
	 * @return true if add success
	 */
	public boolean addJsonReceiveListener(JsonCommunicatorJsonReceiveBiListener l);
	
	/**
	 * 
	 * @param l
	 * @return true if remove success
	 */
	public boolean removeJsonReceiveListener(JsonCommunicatorJsonReceiveBiListener l);
	
	/**
	 * 
	 * @param l
	 * @return true if add success
	 */
	public boolean addPojoReceiveListener(JsonCommunicatorPojoReceiveListener<T> l);
	
	/**
	 * 
	 * @param l
	 * @return true if remove success
	 */
	public boolean removePojoReceiveListener(JsonCommunicatorPojoReceiveListener<T> l);
	
	/**
	 * 
	 * @param l
	 * @return true if add success
	 */
	public boolean addPojoReceiveListener(JsonCommunicatorPojoReceiveBiListener<T> l);
	
	/**
	 * 
	 * @param l
	 * @return true if remove success
	 */
	public boolean removePojoReceiveListener(JsonCommunicatorPojoReceiveBiListener<T> l);
	
	/**
	 * This listener is blocking.<br />
	 * Pass through quickly.<br />
	 * 
	 * @param l
	 * @return true if add success.
	 */
	public boolean addConnectionStateChangeListener(JsonCommunicatorConnectionStateChangeListener l);
	
	/**
	 * 
	 * @param l
	 * @return true if remove success
	 */
	public boolean removeConnectionStateChangeListener(JsonCommunicatorConnectionStateChangeListener l);
	
	/**
	 * This listener is blocking.<br />
	 * Pass through quickly.<br />
	 * 
	 * @param l
	 * @return true if add success.
	 */
	public boolean addConnectionStateChangeListener(JsonCommunicatorConnectionStateChangeBiListener l);
	
	/**
	 * 
	 * @param l
	 * @return true if remove success
	 */
	public boolean removeConnectionStateChangeListener(JsonCommunicatorConnectionStateChangeBiListener l);
	
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
