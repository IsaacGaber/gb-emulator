package cpu.register;

public class ByteRegister implements Register {
    protected int _b;

    @Override
    public void set(int i) {
        _b = i & 0xFF;
    }

    @Override
    public int get() {
        return _b;
    }
    
    // doesn't check bounds or set flags
    @Override
    public void inc() {
        _b++;
    }

    @Override
    public void dec() {
        _b--;
    }
}
