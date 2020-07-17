package com.shimizukenta.jsoncommunicator;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Optional;

public class JsonCommunicatorLog {
	
	private static final String BR = System.lineSeparator();
	private static final String SPACE = " ";
	private static DateTimeFormatter DATETIME = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
	
	
	private final String subject;
	private final LocalDateTime timestamp;
	private final Object value;
	
	public JsonCommunicatorLog(CharSequence subject, LocalDateTime timestamp, Object value) {
		this.subject = Objects.requireNonNull(subject).toString();
		this.timestamp = Objects.requireNonNull(timestamp);
		this.value = value;
	}
	
	public JsonCommunicatorLog(CharSequence subject, Object value) {
		this(subject, LocalDateTime.now(), value);
	}
	
	public JsonCommunicatorLog(CharSequence subject) {
		this(subject, null);
	}
	
	public JsonCommunicatorLog(Throwable t) {
		this(createThrowableSubject(t), t);
	}
	
	public String subject() {
		return subject;
	}
	
	public LocalDateTime timestamp() {
		return timestamp;
	}
	
	public Optional<Object> value() {
		return value == null ? Optional.empty() : Optional.of(value);
	}
	
	@Override
	public String toString() {
		
		StringBuilder sb = new StringBuilder()
				.append(toStringTimestamp())
				.append(SPACE)
				.append(subject());

		String v = toStringValue();
		if ( ! v.isEmpty() ) {
			sb.append(BR).append(v);
		}
		
		return sb.toString();
	}
	
	protected String toStringTimestamp() {
		return timestamp().format(DATETIME);
	}
	
	protected String toStringValue() {
		
		return value().map(o -> {
			
			if ( o instanceof Throwable ) {
				
				try (
						StringWriter sw = new StringWriter();
						) {
					
					try (
							PrintWriter pw = new PrintWriter(sw);
							) {
						
						((Throwable) o).printStackTrace(pw);
						pw.flush();
						
						return sw.toString();
					}
				}
				catch ( IOException e ) {
					return e.toString();
				}
				
			} else {
				
				return o.toString();
			}
			
		})
		.orElse("");
	}
	
	public static String createThrowableSubject(Throwable t) {
		return Objects.requireNonNull(t).getClass().getSimpleName();
	}
	
}
