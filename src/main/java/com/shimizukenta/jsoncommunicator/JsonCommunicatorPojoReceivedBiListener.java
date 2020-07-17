package com.shimizukenta.jsoncommunicator;

import java.nio.channels.AsynchronousSocketChannel;
import java.util.EventListener;

public interface JsonCommunicatorPojoReceivedBiListener<T> extends EventListener {
	public void receive(AsynchronousSocketChannel channel, T pojo);
}
