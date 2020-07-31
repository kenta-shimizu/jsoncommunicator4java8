package com.shimizukenta.jsoncommunicator;

import java.io.Serializable;
import java.net.SocketAddress;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class JsonCommunicatorConfig implements Serializable {
	
	private static final long serialVersionUID = 5336238572932335370L;
	
	public static final float REBIND_SECONDS = 10.0F;
	public static final float RECONNECT_SECONDS = 10.0F;
	
	private final Set<SocketAddress> binds = new CopyOnWriteArraySet<>();
	private final Set<SocketAddress> connects = new CopyOnWriteArraySet<>();
	private float rebindSeconds;
	private float reconnectSeconds;
	private String logSubjectHeader;
	
	public JsonCommunicatorConfig() {
		this.rebindSeconds = REBIND_SECONDS;
		this.reconnectSeconds = RECONNECT_SECONDS;
		this.logSubjectHeader = "";
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
		return binds.add(Objects.requireNonNull(addr));
	}
	
	public boolean removeBind(SocketAddress addr) {
		return binds.remove(Objects.requireNonNull(addr));
	}
	
	public boolean addConnect(SocketAddress addr) {
		return connects.add(Objects.requireNonNull(addr));
	}
	
	public boolean removeConnect(SocketAddress addr) {
		return connects.remove(Objects.requireNonNull(addr));
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
	
	public void logSubjectHeader(CharSequence cs) {
		synchronized ( this ) {
			this.logSubjectHeader = Objects.requireNonNull(cs).toString();
		}
	}
	
	public Optional<String> logSubjectHeader() {
		synchronized ( this ) {
			return this.logSubjectHeader.isEmpty() ? Optional.empty() : Optional.of(this.logSubjectHeader);
		}
	}
	
}
