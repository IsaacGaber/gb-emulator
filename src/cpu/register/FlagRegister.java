package cpu.register;

import cpu.Flag;

public class FlagRegister extends ByteRegister {

    public void set(Flag f) {
        _b |= f.VALUE;
    }

    public boolean isSet(Flag f) {
        return (_b & f.VALUE) != 0;
    }

}
