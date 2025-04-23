package cpu.register;

import cpu.Flag;

public class FlagRegister extends ByteRegister {

    public void set(Flag f) {
        _b = (_b | (1 << f.POS));
    }

    public boolean flagSet(Flag f) {
        return (_b & (1 << f.POS)) != 0;
    }

}
