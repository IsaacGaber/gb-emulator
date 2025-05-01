package cpu.register;

import util.Util;

// Integer bounds checked, overflow simulated
public class ByteRegister implements Register {
    protected int _b;

    public ByteRegister(int i) {
        _b = i;
    }

    public ByteRegister() {
        this(0);
    }

    @Override
    public void set(int i) {
        _b = i & 0xFF;
    }

    @Override
    public int get() {
        return _b;
    }
    
    // checks bounds, doesn't set flags
    @Override
    public void inc() {
        _b = Util.unsignedAdd(_b, 1);
    }

    @Override
    public void dec() {
        _b = Util.unsignedSub(_b, 1);
    }

}
