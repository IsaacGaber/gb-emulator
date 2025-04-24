package cpu.register;


import memory.Memory;

// wrapper around double register -- allows for easy access to memory by instructions
public class IndirectRegister implements Register {
    private DoubleRegister _doubleRegister;
    private Memory _memory;
    private int _inc;

    /**
     * 
     * @param register register that IndirectRegister is constructed around
     * @param increment 
     * @param memory reference to memory bus, necessary for getting and setting
     */
    public IndirectRegister(DoubleRegister register, int increment, Memory memory) {
        _doubleRegister = register;
        _inc = increment;
        _memory = memory;

    }

    @Override
    public void set(int i) {
        _memory.setByte(_doubleRegister.get(), i);
        if (_inc > 0) {
            _doubleRegister.inc();
        } else if (_inc < 0) {
            _doubleRegister.dec();
        }
    }

    @Override
    public int get() {
        int i = _memory.getByte(_doubleRegister.get());
        if (_inc > 0) {
            _doubleRegister.inc();
        } else if (_inc < 0) {
            _doubleRegister.dec();
        }
        return i;
    }

    @Override
    public void inc() {
        _doubleRegister.inc();
    }

    @Override
    public void dec() {
        _doubleRegister.dec();
    }
}
