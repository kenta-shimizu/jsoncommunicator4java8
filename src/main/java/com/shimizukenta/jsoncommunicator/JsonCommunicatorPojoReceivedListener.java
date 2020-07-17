package com.shimizukenta.jsoncommunicator;

import java.util.EventListener;

public interface JsonCommunicatorPojoReceivedListener<T> extends EventListener {
	public void receive(T pojo);
}
