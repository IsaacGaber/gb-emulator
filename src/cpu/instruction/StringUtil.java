package cpu.instruction;

public class StringUtil {
    
    public static String byteToHexstring(int b){
        return String.format("$%02x", b);
    }

    public static String wordToHexstring(int w) {
        return String.format("$%04x", w);
    }

}
