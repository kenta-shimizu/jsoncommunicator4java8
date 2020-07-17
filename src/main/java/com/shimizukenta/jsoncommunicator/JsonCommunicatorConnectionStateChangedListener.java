package com.shimizukenta.jsoncommunicator;

import java.util.EventListener;

public interface JsonCommunicatorConnectionStateChangedListener extends EventListener {
	public void changed(JsonCommunicatorConnectionState state);
}
