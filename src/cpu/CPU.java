package cpu;

import java.util.*;
import memory.*;
import util.BitUtil;
import util.Util;
import cpu.instruction.Instruction;
import cpu.instruction.InstructionSet;
import cpu.register.*;

public class CPU {
    private final ByteRegister A, B, C, D, E, H, L;
    private final FlagRegister F;
    private final DoubleRegister AF, BC, DE, HL, PC, SP;
    private final TreeMap<String, Register> _registers;

    private InstructionSet _instructionSet;
    private Memory _memory;

    public CPU(Memory memory) {
        _memory = memory;
        _instructionSet = new InstructionSet(this, _memory);

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
        // init program counter
        PC.set(0);
        SP.set(0); // TODO CHANGE TO ACTUAL START VALUE FOR STACK
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
        System.out.println(Util.byteToHexstring(curr));

        System.out.println(instruction);

        instruction.run();
        // } catch (Exception e) {
        //     throw new RuntimeException("invalid/unimplemented instruction: " + Util.byteToHexstring(curr));
        // }
        System.out.println(this);
    }

    // returns reference to register if valid
    public Register reg(String reg) {
        return _registers.get(reg);
    }

    // sets specified register to value
    public void setReg(String reg, int val) {
        _registers.get(reg).set(val);
    }

    public int getReg(String reg) {
        return _registers.get(reg).get();
    }

    public void setFlag(Flag f) {
        F.set(f);
    }

    public void clearFlag(Flag f) {
        F.set(BitUtil.setBit(F.get(), f.POS, false));
    }

    public boolean flagSet(Flag f) {
        return (F.get() & f.VALUE) != 0;
    }

    // fetches next byte and incremements program counter
    public int nextByte() {
        int i = _memory.getByte(PC.get());
        PC.inc();
        return i;
    }

    public int nextWord() {
        return (nextByte() << 8) | nextByte();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Register> entry : _registers.entrySet()) {
            sb.append(entry.getKey() + ": ");
            Register reg = entry.getValue();
            if (reg instanceof DoubleRegister) {
                sb.append(Util.wordToHexstring(entry.getValue().get()));
            } else if (reg instanceof ByteRegister) {
                sb.append(String.format("%6.6s",Util.byteToHexstring(entry.getValue().get())));
            }
            sb.append('\n');
        }
        return sb.toString();
    }
}
