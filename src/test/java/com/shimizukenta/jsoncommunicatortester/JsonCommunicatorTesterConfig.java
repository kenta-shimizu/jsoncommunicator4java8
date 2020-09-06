package com.shimizukenta.jsoncommunicatortester;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.shimizukenta.jsoncommunicator.JsonCommunicatorConfig;
import com.shimizukenta.jsonhub.JsonHub;

public final class JsonCommunicatorTesterConfig implements Serializable {
	
	private static final long serialVersionUID = -576937238234882714L;
	
	private final JsonCommunicatorConfig commConf = new JsonCommunicatorConfig();
	private final Set<Path> jsonPaths = new HashSet<>();
	private boolean autoOpen;
	
	public JsonCommunicatorTesterConfig() {
		this.autoOpen = false;
	}
	
	public JsonCommunicatorConfig communicator() {
		return commConf;
	}
	
	public Set<Path> jsonPaths() {
		return Collections.unmodifiableSet(jsonPaths);
	}
	
	public boolean autoOpen() {
		return this.autoOpen;
	}
	
	private void setByJson(JsonHub jh) throws IOException {
		
		//TODO
	}
	
	public static JsonCommunicatorTesterConfig get(String[] args) throws IOException {
		
		final Map<String, List<String>> map = new HashMap<>();
		
		for (int i = 0, m = args.length; i < m; i += 2) {
			
			String key = args[i].toLowerCase();
			String v = args[i + 1];
			
			map.computeIfAbsent(key, k -> new ArrayList<>()).add(v);
		}
		
		final JsonCommunicatorTesterConfig inst = new JsonCommunicatorTesterConfig();
		
		{
			List<Path> paths = map.getOrDefault("--config", Collections.emptyList()).stream()
					.map(Paths::get)
					.collect(Collectors.toList());
			
			for ( Path path : paths ) {
				inst.setByJson(JsonHub.fromFile(path));
			}
		}
		
		map.getOrDefault("--connect", Collections.emptyList()).stream()
		.map(s -> parseSocketAddress(s))
		.forEach(inst.commConf::addConnect);
		
		map.getOrDefault("--bind", Collections.emptyList()).stream()
		.map(s -> parseSocketAddress(s))
		.forEach(inst.commConf::addBind);
		
		map.getOrDefault("--autoopen", Collections.emptyList()).stream()
		.map(Boolean::parseBoolean)
		.forEach(f -> {
			inst.autoOpen = f.booleanValue();
		});
		
		map.getOrDefault("--json", Collections.emptyList()).stream()
		.map(Paths::get)
		.forEach(inst.jsonPaths::add);
		
		return inst;
	}
	
	private static SocketAddress parseSocketAddress(String addr) {
		String[] a = addr.trim().split(":", 2);
		return new InetSocketAddress(
				a[0].trim(),
				Integer.parseInt(a[1].trim())
				);
	}
}
