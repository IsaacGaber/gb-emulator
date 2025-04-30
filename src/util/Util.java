package util;

public class Util {
    public static String byteToHexstring(int b){
        return String.format("$%02x", b & 0xFF);
    }

    public static String wordToHexstring(int w) {
        return String.format("$%04x", w & 0xFFFF);
    }

    // byte math operations 
    public static int unsignedAdd(int a, int b){
        return (a + b) & 0xFF;
    }

    public static int unsignedSub(int a, int b){
        int answer = a - b;
        return answer < 0 ? 0 : answer;
    }
    
}
