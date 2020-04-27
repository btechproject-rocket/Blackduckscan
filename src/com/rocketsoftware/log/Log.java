package com.rocketsoftware.log;

import java.util.logging.ConsoleHandler;
import java.util.logging.Logger;

public class Log {
	public static final Logger log = Logger.getLogger(Log.class.getName());
	
	public static void configureLog() {
		ConsoleHandler console = new ConsoleHandler();
		console.setFormatter(new LogFormatter());
		log.setUseParentHandlers(false);
		log.addHandler(console);
		log.setFilter(new LogFilter());
	}
}
