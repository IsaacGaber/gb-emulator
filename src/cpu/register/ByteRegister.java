package cpu.register;

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
    
}
