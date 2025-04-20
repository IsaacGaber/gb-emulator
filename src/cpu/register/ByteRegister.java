package cpu.register;

import cpu.CPU;
import cpu.Flag;

public class ByteRegister implements Register {
    protected int _b;

    @Override
    public void set(int i) {
        _b = (byte) i;
    }

    @Override
    public int get() {
        return _b;
    }
    
    // doesn't check or set flags
    @Override
    public void inc() {
        _b++;
        // _b = (_b + 1) % 256;
    }

    @Override
    public void dec() {
        _b--;
        // _b = (_b - 1) % 256;
    }
}
