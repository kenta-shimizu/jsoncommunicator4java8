package com.shimizukenta.jsoncommunicatortester;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RequestCommandParser {
	
	private static final String BR = System.lineSeparator();
	
	private RequestCommandParser() {
		/* Nothing */
	}
	
	private static final class SingletonHolder {
		private static final RequestCommandParser inst = new RequestCommandParser();
		private static final RequestCommand unknown = new RequestCommand(Command.UNKNOWN);
		
		private static final String manualsString = Stream.of(Inner.values())
				.filter(v -> v != Inner.UNKNOWN)
				.map(v -> {
					return v.strs.stream().collect(Collectors.joining(" | ", "[ ", " ]"))
							+ " "
							+ v.command.description();
				})
				.collect(Collectors.joining(BR));
		
		private static final Map<Inner, String> detailsMap = new ConcurrentHashMap<>();
	}
	
	public static final RequestCommandParser getInstance() {
		return SingletonHolder.inst;
	}
	
	public RequestCommand parse(String line) {
		
		String lt = line.trim();
		
		String cmd = (lt.split("\\s+", 2))[0];
		
		for ( Inner v : Inner.values() ) {
			for ( String s : v.strs ) {
				if ( s.equalsIgnoreCase(cmd) ) {
					
					if ( v.optionSize > 0 ) {
						
						String[] ss = lt.split("\\s+", v.optionSize + 1);
						
						List<String> options = new ArrayList<>();
						for ( int i = 1, m = ss.length; i < m; ++i ) {
							options.add(ss[i]);
						}
						
						return new RequestCommand(v.command, options);
						
					} else {
						
						return new RequestCommand(v.command);
					}
				}
			}
		}
		
		return SingletonHolder.unknown;
	}
	
	public String getCommandsManual() {
		return SingletonHolder.manualsString;
	}
	
	public String getCommandDetailManual(String command) {
		
		for ( Inner v : Inner.values() ) {
			for ( String s : v.strs) {
				if ( s.equalsIgnoreCase(command) ) {
					
					return SingletonHolder.detailsMap.computeIfAbsent(v, key -> {
						
						return key.command.details().stream()
								.collect(Collectors.joining(BR));
					});
				}
			}
		}
		
		return "\"" + command + "\" is unknown command";
	}
	
	private static enum Inner {
		
		UNKNOWN(Command.UNKNOWN, 0),
		
		MANUAL(Command.MANUAL, 1, "?", "man"),
		
		QUIT(Command.QUIT, 0, "quit", "exit"),
		OPEN(Command.OPEN, 0, "open"),
		CLOSE(Command.CLOSE, 0, "close"),
		
		LIST(Command.LIST, 0, "list"),
		SHOW(Command.SHOW, 1, "show"),
		
		SEND(Command.SEND, 1, "send"),
		
		;
		
		private Command command;
		private int optionSize;
		private List<String> strs;
		
		private Inner(Command command, int optionSize, String... strs) {
			this.command = command;
			this.optionSize = optionSize;
			List<String> ll = new ArrayList<>();
			for ( String s : strs ) {
				ll.add(s);
			}
			this.strs = Collections.unmodifiableList(ll);
		}
	}
	
}
