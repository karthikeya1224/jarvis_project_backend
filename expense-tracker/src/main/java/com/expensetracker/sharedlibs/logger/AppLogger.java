package com.expensetracker.sharedlibs.logger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppLogger {
    
    public static Logger getLogger(Class<?> clazz) {
        return LoggerFactory.getLogger(clazz);
    }
    
    public static void logInfo(Logger logger, String method, String message) {
        logger.info("[{}] {}", method, message);
    }
    
    public static void logError(Logger logger, String method, String message, Exception e) {
        logger.error("[{}] {} - Error: {}", method, message, e.getMessage(), e);
    }
    
    public static void logDebug(Logger logger, String method, String message) {
        logger.debug("[{}] {}", method, message);
    }
}
