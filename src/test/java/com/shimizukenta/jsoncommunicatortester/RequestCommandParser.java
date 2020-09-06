package com.shimizukenta.jsoncommunicatortester;

public class RequestCommandParser {

	private RequestCommandParser() {
		/* Nothing */
	}
	
	private static final class SingletonHolder {
		private static final RequestCommandParser inst = new RequestCommandParser();
	}
	
	public static final RequestCommandParser getInstance() {
		return SingletonHolder.inst;
	}
	
	public RequestCommand parse(String line) {
		
		//TODO
		
		return null;
	}
}
