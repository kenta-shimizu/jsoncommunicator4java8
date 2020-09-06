package com.shimizukenta.jsoncommunicatortester;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public enum Command {
	
	QUIT("quit"),
	OPEN("open"),
	CLOSE("close"),
	
	LIST("list"),
	SHOW("show"),
	SEND("send"),
	
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
