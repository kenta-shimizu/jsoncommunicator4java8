package com.shimizukenta.jsoncommunicatortester;

import java.util.Collections;
import java.util.List;

public class RequestCommand {
	
	private final Command command;
	private final List<String> options;
	
	public RequestCommand(Command command, List<String> options) {
		this.command = command;
		this.options = Collections.unmodifiableList(options);
	}
	
	public Command command() {
		return this.command;
	}
	
	public List<String> options() {
		return options;
	}

}
