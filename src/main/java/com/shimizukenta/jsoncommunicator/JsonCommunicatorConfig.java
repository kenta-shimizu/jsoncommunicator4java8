package com.shimizukenta.jsoncommunicator;

import java.net.SocketAddress;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class JsonCommunicatorConfig {

	public static final float REBIND_SECONDS = 10.0F;
	public static final float RECONNECT_SECONDS = 10.0F;
	
	private final Set<SocketAddress> binds = new CopyOnWriteArraySet<>();
	private final Set<SocketAddress> connects = new CopyOnWriteArraySet<>();
	private float rebindSeconds;
	private float reconnectSeconds;
	
	public JsonCommunicatorConfig() {
		this.rebindSeconds = REBIND_SECONDS;
		this.reconnectSeconds = RECONNECT_SECONDS;
	}
	
	public Set<SocketAddress> binds() {
		return Collections.unmodifiableSet(binds);
	}
	
	public Set<SocketAddress> connects() {
		return Collections.unmodifiableSet(connects);
	}
	
	public float rebindSeconds() {
		synchronized ( this ) {
			return rebindSeconds;
		}
	}
	
	public float reconnectSeconds() {
		synchronized ( this ) {
			return reconnectSeconds;
		}
	}
	
	public boolean addBind(SocketAddress addr) {
		return binds.add(addr);
	}
	
	public boolean removeBind(SocketAddress addr) {
		return binds.remove(addr);
	}
	
	public boolean addConnect(SocketAddress addr) {
		return connects.add(addr);
	}
	
	public boolean removeConnect(SocketAddress addr) {
		return connects.remove(addr);
	}
	
	public void rebindSeconds(float v) {
		synchronized ( this ) {
			this.rebindSeconds = v;
		}
	}
	
	public void reconnectSeconds(float v) {
		synchronized ( this ) {
			this.reconnectSeconds = v;
		}
	}
	
}
