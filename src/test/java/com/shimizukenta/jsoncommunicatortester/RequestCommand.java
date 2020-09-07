package com.shimizukenta.jsoncommunicatortester;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class RequestCommand {
	
	private final Command command;
	private final List<String> options;
	
	public RequestCommand(Command command) {
		this.command = command;
		this.options = Collections.emptyList();
	}
	
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
	
	public Optional<String> option(int index) {
		if (index < options.size()) {
			return Optional.of(options.get(index));
		} else {
			return Optional.empty();
		}
	}

}
