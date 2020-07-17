package com.shimizukenta.jsoncommunicator;

import java.util.EventListener;

public interface JsonCommunicatorJsonReceivedListener extends EventListener {
	public void receive(String json);
}
