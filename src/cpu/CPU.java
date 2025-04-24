package cpu;

import java.util.*;
import java.util.Map.Entry;

import memory.*;
import util.*;
import cpu.instruction.*;
import cpu.register.*;

public class CPU {
    public final ByteRegister A, B, C, D, E, H, L;
    public final FlagRegister F;
    public final DoubleRegister AF, BC, DE, HL, PC, SP;
    // _HLi and _HLd are increment and decrement versions of indirect HL respectively
    public final IndirectRegister _BC, _DE, _HL, _HLi, _HLd;
    public final TreeMap<String, Register> _registers;

    // private final TreeSet<Register> _registers; 

    private InstructionSet _instructionSet;
    private Memory _memory;

    public enum States {
        HALTED, RUNNING
    }

    private States _state;

    // cpu cycles
    private int _cycles; 

    public CPU(Memory memory) {
        _memory = memory;
        // init register array - used only for toString
        _registers = new TreeMap<>();

        // init _registers
        A = new ByteRegister(); _registers.put("A", A);
        B = new ByteRegister(); _registers.put("B", B);
        C = new ByteRegister(); _registers.put("C", C);
        D = new ByteRegister(); _registers.put("D", D);
        E = new ByteRegister(); _registers.put("E", E);
        H = new ByteRegister(); _registers.put("H", H);
        L = new ByteRegister(); _registers.put("L", L);
        // special flag register
        F = new FlagRegister(); _registers.put("F", F);
        // double _registers
        AF = new DoubleRegister(A, F);  _registers.put("AF", AF);
        BC = new DoubleRegister(B, C);  _registers.put("BC", BC);
        DE = new DoubleRegister(D, E);  _registers.put("DE", DE);
        HL = new DoubleRegister(H, L);  _registers.put("HL", HL);
        // program counter and stack pointer
        PC = new DoubleRegister(new ByteRegister(), new ByteRegister());    _registers.put("PC", PC);
        SP = new DoubleRegister(new ByteRegister(), new ByteRegister());    _registers.put("SP", SP);
        // indirect registers
        _BC = new IndirectRegister(BC, 0, memory);
        _DE = new IndirectRegister(DE, 0, memory);
        _HL = new IndirectRegister(HL, 0, memory);
        _HLi = new IndirectRegister(HL, 1, memory);
        _HLd = new IndirectRegister(HL, -1, memory);

        // init program counter
        PC.set(0);
        SP.set(0); // TODO CHANGE TO ACTUAL START VALUE FOR STACK

        _instructionSet = new InstructionSet(this, _memory);

        _state = States.RUNNING;
    }

    public void step() {
        int curr = nextByte();
        Instruction instruction;
        if (curr == 0xCB) {
            curr = nextByte();
            instruction = _instructionSet.getCBPrefixed(curr);
        } else {
            instruction = _instructionSet.getUnprefixed(curr);
        }
        // try {        
        System.out.println("Byte Value: " + Util.byteToHexstring(curr));
        System.out.println("Instruction: " + instruction);

        _cycles += instruction.run();
        System.out.println(this);
    }

    // returns reference to register if valid
    // public Register reg(String reg) {
    //     return _registers.get(reg);
    // }

    // sets specified register to value
    // public void setReg(String reg, int val) {
    //     _registers.get(reg).set(val);
    // }

    // public int getReg(String reg) {
    //     return _registers.get(reg).get();
    // }

    public void setFlag(Flag f) {
        F.set(f);
    }

    public void clearFlag(Flag f) {
        F.set(BitUtil.setBit(F.get(), f.POS, false));
    }

    public boolean flagSet(Flag f) {
        return (F.get() & (1 << f.POS)) != 0;
    }

    // fetches next byte and incremements program counter
    public int nextByte() {
        int i = _memory.getByte(PC.get());
        PC.inc();
        return i;
    }

    // confirm this is actually how the memory is stored 
    public int nextWord() {
        int i = _memory.getWord(PC.get());
        PC.inc();
        PC.inc();
        return (i);
    }

    public void halt() {
        _state = States.HALTED;
    }

    public boolean running() {
        return _state == States.RUNNING;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        StringBuilder sbTwo = new StringBuilder();
        sb.append("Machine Cycles: " + _cycles);
        sb.append("\nRegisters:\n");

        for (Entry<String, Register> entry : _registers.entrySet()) {
            String name = entry.getKey();
            Register reg = entry.getValue();
            if (reg instanceof DoubleRegister) {
                sbTwo.append(String.format("%s: %10.5s\n", name, Util.wordToHexstring(reg.get())));
            } else if (reg instanceof ByteRegister) {
                sb.append(String.format("%s: %11.3s\n", name, Util.byteToHexstring(reg.get())));
            }
        }
        return sb.toString() + sbTwo.toString();
    }
}
