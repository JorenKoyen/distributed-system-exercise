package be.kdg.distrib.logger;

import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;

public class Logger {
    private final String name;
    private final int color;

    // -- FACTORY ----------------
    public static Logger getLogger(String name) {
        return new Logger(name);
    }

    // -- CONSTRUCTOR --------------
    private Logger(String name) {
        this.name = name;
        this.color = 1 + ThreadLocalRandom.current().nextInt(AnsiCode.values().length - 1);
    }

    // -- METHODS ----------------
    public void info(String message) {
        System.out.println(formatMessage(AnsiCode.WHITE, message));
    }
    public void error(String message) {
        System.out.println(formatMessage(AnsiCode.RED, message));
    }
    public void debug(String message) {
        System.out.println(formatMessage(AnsiCode.GREEN, message));
    }
    public void info(String format, Object... args) {
        System.out.println(formatMessage(AnsiCode.WHITE, format, args));
    }
    public void error(String format, Object... args) {
        System.out.println(formatMessage(AnsiCode.RED, format, args));
    }
    public void debug(String format, Object... args) {
        System.out.println(formatMessage(AnsiCode.GREEN, format, args));
    }

    // -- PRIVATE METHODS --------
    private String formatMessage(AnsiCode textColor, String format, Object... args) {
        String message = String.format(format, args);
        return formatMessage(textColor, message);
    }
    private String formatMessage(AnsiCode textColor, String message) {
        return String.format("%-30s %s\t\t%s",
                formatName(),
                TextColor.asColor(AnsiCode.YELLOW, new Date().toString()),
                TextColor.asColor(textColor, message));
    }
    private String formatName() {
        return String.format("[%s]", TextColor.asColor(AnsiCode.values()[this.color], this.name));
    }
}
