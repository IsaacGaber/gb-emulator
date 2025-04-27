package cpu;

public enum Flag {

    Z (7), // operation resulted in zero
    H (6),  // half-carry
    N (5),  // operation used subtraction
    C (4),   // carry
    NC,          // not carry
    NZ,         // not zero
    NONE; // no flag set

    public final Integer POS;

    private Flag(){
        this(0);
    }

    private Flag(int pos) {
        // this.VALUE = val;
        this.POS = pos;
    }
}
