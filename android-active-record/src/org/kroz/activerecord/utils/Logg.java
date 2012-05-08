package org.kroz.activerecord.utils;

import android.util.Log;

/**
 * Extended android logger with 'printf'-style logging and controlled
 * indentation. Use it similar to standard Android Log class. To start or stop
 * indentations of log messages call method indents().<p/>
 * Log messages priorities
 * <ul>
 * <li>VERBOSE 2 (0x00000002)
 * <li>DEBUG 3 (0x00000003)
 * <li>INFO 4 (0x00000004)
 * <li>WARN 5 (0x00000005)
 * <li>ERROR 6 (0x00000006)
 * </ul>
 * @author Vladimir Kroz
 * 
 */
public class Logg {

	static int _startIdentLevel = -1;

	private static boolean _useIdents = false;

	/**
	 * Starts/stops indentation in logs printing
	 * 
	 * @param useIdents
	 *            true to start identation, false - to stop it
	 */
	public static void indents(boolean useIdents) {
		_useIdents = useIdents;
		_startIdentLevel = Math.max(
				Thread.currentThread().getStackTrace().length - 1, 0);
	}

	// public static void startIdents() {
	// _startIdentLevel = Math.max(
	// Thread.currentThread().getStackTrace().length - 1, 0);
	// }

	// public static void resetIdents() {
	// _startIdentLevel = -1;
	// }

	/** Send a DEBUG log message. */
	public static int d(String tag, String format, Object... args) {
		if (_useIdents)
			format = getIdent() + format;
		format = format.replaceAll("%t", Long.toString(Logg.t()));
		return println(Log.DEBUG, tag, format, args);
	}

	/** Send a DEBUG log message and log the exception. */
	public static int d(String tag, Throwable tr, String format, Object... args) {
		if (_useIdents)
			format = getIdent() + format;
		format = format.replaceAll("%t", Long.toString(Logg.t()));
		return Log.d(tag, String.format(format, args), tr);
	}

	/** Send a ERROR log message. */
	public static int e(String tag, String format, Object... args) {
		if (_useIdents)
			format = getIdent() + format;
		format = format.replaceAll("%t", Long.toString(Logg.t()));
		return Log.e(tag, String.format(format, args));
	}

	/** Send a ERROR log message and log the exception. */
	public static int e(String tag, Throwable tr, String format, Object... args) {
		if (_useIdents)
			format = getIdent() + format;
		format = format.replaceAll("%t", Long.toString(Logg.t()));
		return Log.e(tag, String.format(format, args), tr);
	}

	/** Send a INFO log message. */
	public static int i(String tag, String format, Object... args) {
		if (_useIdents)
			format = getIdent() + format;
		format = format.replaceAll("%t", Long.toString(Logg.t()));
		return Log.i(tag, String.format(format, args));
	}

	/** Send a INFO log message and log the exception. */
	public static int i(String tag, Throwable tr, String format, Object... args) {
		if (_useIdents)
			format = getIdent() + format;
		format = format.replaceAll("%t", Long.toString(Logg.t()));
		return Log.i(tag, String.format(format, args), tr);
	}

	/**
	 * Checks to see whether or not a log for the specified tag is loggable at
	 * the specified level.
	 */
	public static boolean isLoggable(String tag, int level) {
		return Log.isLoggable(tag, level);
	}

	/** Low-level logging call. */
	public static int println(int priority, String tag, String format,
			Object... args) {
		if (_useIdents)
			format = getIdent() + format;
		return Log.println(priority, tag, String.format(format, args));
	}

	/** Send a VERBOSE log message. */
	public static int v(String tag, String format, Object... args) {
		if (_useIdents)
			format = getIdent() + format;
		format = format.replaceAll("%t", Long.toString(Logg.t()));
		return println(Log.VERBOSE, tag, format, args);
	}

	/** Send a WARN log message. */
	public static int w(String tag, String format, Object... args) {
		if (_useIdents)
			format = getIdent() + format;
		format = format.replaceAll("%t", Long.toString(Logg.t()));
		return Log.w(tag, String.format(format, args));
	}

	/** Send a WARN log message and log the exception. */
	public static int w(String tag, Throwable tr, String format, Object... args) {
		if (_useIdents)
			format = getIdent() + format;
		format = format.replaceAll("%t", Long.toString(Logg.t()));
		return Log.w(tag, String.format(format, args), tr);
	}

	/** Handy function to get a loggable stack trace from a Throwable */
	public static String getStackTraceString(Throwable tr) {
		return Log.getStackTraceString(tr);
	}

	private static String getIdent() {
		if (_useIdents) {
			int currentIdentLevel = Thread.currentThread().getStackTrace().length - 1;
			currentIdentLevel = Math.min(idents.length - 1,
					Math.max(0, currentIdentLevel - _startIdentLevel));
			return idents[currentIdentLevel];
		} else {
			return "";
		}
	}

	static long t() {
		return Thread.currentThread().getId();
	}

	static String[] idents = { "", " ", "  ", "   ", "    ", "     ", "      ",
			"       ", "        ", "         ", "          ", "           ",
			"            ", "             ", "              ",
			"               " };

}
