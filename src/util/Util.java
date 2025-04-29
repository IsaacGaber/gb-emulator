package util;

public class Util {
    public static String byteToHexstring(int b){
        return String.format("$%02x", b & 0xFF);
    }

    public static String wordToHexstring(int w) {
        return String.format("$%04x", w & 0xFFFF);
    }
}
