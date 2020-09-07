package com.shimizukenta.jsoncommunicatortester;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public enum Command {
	
	UNKNOWN("Unknown"),
	
	MANUAL("Show Manual", "Show Commands, Show Manual with option."),
	
	QUIT("Quit Application", "Quit Application."),
	OPEN("Open Communicator", "Open Communicator.", "If already opened, close and re-open."),
	CLOSE("Close Communicator", "Close Communicator."),
	
	LIST("List JSONs", "List added JSONs."),
	SHOW("Show JSON-content", "Show pretty-print-JSON."),
	
	SEND("Send JSON", "Send compact-JSON.", "Send JSON if communicator opened."),
	
	;
	
	private final String desc;
	private final List<String> details;
	
	private Command(String desc, String... details) {
		this.desc = desc;
		
		List<String> ss = new ArrayList<>();
		for ( String d : details ) {
			ss.add(d);
		}
		this.details = Collections.unmodifiableList(ss);
	}
	
	public String description() {
		return desc;
	}
	
	public List<String> details() {
		return details;
	}
	
}
