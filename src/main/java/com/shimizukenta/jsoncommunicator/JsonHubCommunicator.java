package com.shimizukenta.jsoncommunicator;

import java.nio.channels.AsynchronousSocketChannel;

import com.shimizukenta.jsonhub.JsonHub;
import com.shimizukenta.jsonhub.JsonHubParseException;

public class JsonHubCommunicator<T> extends AbstractJsonCommunicator<T> {
	
	private final Class<T> classOfT;
	
	protected JsonHubCommunicator(JsonCommunicatorConfig config) {
		super(config);
		this.classOfT = null;
	}
	
	protected JsonHubCommunicator(JsonCommunicatorConfig config, Class<T> classOfT) {
		super(config);
		this.classOfT = classOfT;
	}

	@Override
	protected void putReceivedBytes(AsynchronousSocketChannel channel, byte[] bs) {
		
		try {
			JsonHub jh = JsonHub.fromBytes(bs);
			receiveJson(channel, jh.toJson());
			if ( this.classOfT != null ) {
				receivePojo(channel, jh.toPojo(classOfT));
			}
		}
		catch ( JsonHubParseException e ) {
			putLog(e);
		}
	}
	
	@Override
	protected byte[] createBytesFromPojo(Object pojo) throws JsonCommunicatorParseException {
		try {
			return JsonHub.fromPojo(pojo).getBytesExcludedNullValueInObject();
		}
		catch ( JsonHubParseException e ) {
			throw new JsonCommunicatorParseException(e);
		}
	}

}
