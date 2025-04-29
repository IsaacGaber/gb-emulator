package cpu.register;

import util.BitUtil;

/** wrapper around two byte regisers
 * 
 */
public class DoubleRegister implements Register {

    private ByteRegister _a, _b;

    public DoubleRegister(ByteRegister a, ByteRegister b) {
        if (a instanceof ByteRegister && b instanceof ByteRegister) {
            _a = a;
            _b = b;
        } else {
            throw new RuntimeException("Can only create a double register out of Byte Registers");
        }
    }

    @Override
    public void set(int i) {
        _a.set((i >> 8) & 0xFF);
        _b.set(i & 0xFF);
    }

    @Override
    public int get() {
        return _a.get() << 8 | _b.get();
    }

    @Override
    public void inc() {
        int d = get();
        d++;
        set(d);
    }

    @Override
    public void dec() {
        int d = get();
        d--;
        set(d);
    }
}
