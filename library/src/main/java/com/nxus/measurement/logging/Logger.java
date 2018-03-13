package com.nxus.measurement.logging;


/**
 * Logger class for internal logging based on set LogLevel.
 * @author TechMpire Ltd.
 */
public class Logger {
	private static LogLevel level = LogLevel.OFF;
	private String p;

	private Logger(String p) {
		this.p = p;
	}

	public static LogLevel getLevel() {
		return level;
	}

	public static void setLevel(LogLevel logLevel) {
		level = logLevel;
	}

	public static Logger getLog(Package p) {
		return new Logger(p.getName());
	}

	public static Logger getLog(Class<?> c) {
		return new Logger (c.getName());
	}

	public void trace(String message, Object... args) {
		if(message != null && level.isTrace()) {
			if(args == null || args.length == 0) {
				android.util.Log.v(p, message);
			} else {
				android.util.Log.v(p, format(message, args));
			}
		}
	}

	public void trace(String message, Throwable t, Object... args) {
		if(message != null && level.isTrace()) {
			if(args == null || args.length == 0) {
				android.util.Log.v(p, message, t);
			} else {
				android.util.Log.v(p, String.format(message, args), t);
			}
		}
	}

	public void debug(String message, Object... args) {
		if(message != null && level.isDebug()) {
			if(args == null || args.length == 0) {
				android.util.Log.d(p, message);
			} else {
				android.util.Log.d(p, format(message, args));
			}
		}
	}

	public void debug(String message, Throwable t, Object... args) {
		if(message != null && level.isDebug()) {
			if(args == null || args.length == 0) {
				android.util.Log.d(p, message, t);
			} else {
				android.util.Log.d(p, format(message, args), t);
			}
		}
	}

	public void info(String message, Object... args) {
		if(message != null && level.isInfo()) {
			if(args == null || args.length == 0) {
				android.util.Log.i(p, message);
			} else {
				android.util.Log.i(p, format(message, args));
			}
		}
	}

	public void info(String message, Throwable t, Object... args) {
		if(message != null && level.isInfo()) {
			if(args == null || args.length == 0) {
				android.util.Log.i(p, message, t);
			} else {
				android.util.Log.i(p, format(message, args), t);
			}
		}
	}

	public void warn(String message, Object... args) {
		if(message != null && level.isWarn()) {
			if(args == null || args.length == 0) {
				android.util.Log.w(p, message);
			} else {
				android.util.Log.w(p, format(message, args));
			}
		}
	}

	public void warn(String message, Throwable t, Object... args){
		if(message != null && level.isWarn()) {
			if(args == null || args.length == 0) {
				android.util.Log.w(p, message, t);
			} else {
				android.util.Log.w(p, format(message, args), t);
			}
		}
	}

	public void error(String message, Object... args) {
		if(message != null && level.isError()) {
			if(args == null || args.length == 0) {
				android.util.Log.e(p, message);
			} else {
				message = format(message, args);
				android.util.Log.e(p, message);
			}
		}
	}

	public void error(String message, Throwable t, Object... args) {
		if(message != null && level.isError()) {
			if(args == null || args.length == 0) {
				android.util.Log.e(p, message, t);
			} else {
				message = format(message, args);
				android.util.Log.e(p, message, t);
			}
		}
	}

	private String format(String message, Object... args) {
		try {
			return String.format(message, args);
		} catch(Exception e) {
			error("Error in arguments for string formating", e);
			for (Object a: args) {
				message += " " + a;
			}
			return message;
		}
	}
}
