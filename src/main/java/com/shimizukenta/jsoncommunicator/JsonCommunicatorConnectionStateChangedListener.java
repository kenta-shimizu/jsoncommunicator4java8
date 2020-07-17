package com.shimizukenta.jsoncommunicator;

import java.nio.channels.AsynchronousSocketChannel;
import java.util.EventListener;

public interface JsonCommunicatorConnectionStateChangedListener extends EventListener {
	
	public void put(AsynchronousSocketChannel channel, JsonCommunicatorConnectionState state);
}
