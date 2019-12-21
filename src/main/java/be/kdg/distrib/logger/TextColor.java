package be.kdg.distrib.logger;

public class TextColor {
    public static String asColor(AnsiCode code, String text) {
        return asColor(code, text, true);
    }
    public static String asColor(AnsiCode code, String text, boolean reset) {
        return String.format("%s%s%s", code.getCode(), text, reset ? AnsiCode.RESET.getCode() : "");
    }
}
