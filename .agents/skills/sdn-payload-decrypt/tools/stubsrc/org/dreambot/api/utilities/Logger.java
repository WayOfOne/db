package org.dreambot.api.utilities;

/**
 * Offline probe stub: shadows the client logger so framework classes can be
 * loaded without client context (javap-verified: Logger.<clinit> pulls log4j
 * and client-core singletons; the framework classes only call error()).
 * Compile to stub/ and put stub/ BEFORE the client jar on the classpath.
 */
public class Logger {
    public static void error(String s, Throwable t) { System.out.println("[stub-logger] " + s + " :: " + t); }
    public static void error(String s) { System.out.println("[stub-logger] " + s); }
    public static void info(Object o) { }
    public static void debug(Object o) { }
    public static void warn(Object o) { }
    public static String getCurrentLogPath() { return "/tmp"; }
}
