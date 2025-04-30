package cpu.register;

public interface Register {

    public void set(int i);

    public int get();

    // only used by some registers
    // returns old value
    public void inc(); // increment

    public void dec(); // decrement
    
}
