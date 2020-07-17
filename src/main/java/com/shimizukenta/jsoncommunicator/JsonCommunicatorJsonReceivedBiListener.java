package com.shimizukenta.jsoncommunicator;

import java.nio.channels.AsynchronousSocketChannel;
import java.util.EventListener;

public interface JsonCommunicatorJsonReceivedBiListener extends EventListener {
	public void receive(AsynchronousSocketChannel channel, String json);
}
