package cpu.register;

// Integer bounds checked, overflow simulated
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
        if (_b > 0xFF) {
            _b = 0;
        }
    }

    @Override
    public void dec() {
        _b--;
        if (_b < 0) {
            _b = 0xFF;
        }

    }
}
