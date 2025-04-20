package cpu.register;

import cpu.Flag;

public class FlagRegister extends ByteRegister {

    public void set(Flag f) {
        _b |= f.VALUE;
    }

    public boolean flagSet(Flag f) {
        return (_b & f.VALUE) != 0;
    }

}
