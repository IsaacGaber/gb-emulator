package cpu;

import java.util.TreeMap;

import cpu.register.*;

public class CPU {
    public static ByteRegister A, B, C, D, E, H, L, F;
    public static DoubleRegister AF, BC, DE, HL, PC, SP;
    public static TreeMap<String, Register> registers;
    
    public CPU() {
        // init registers
        A = new ByteRegister(); registers.put("A", A);
        B = new ByteRegister(); registers.put("B", A);
        C = new ByteRegister(); registers.put("C", A);
        D = new ByteRegister(); registers.put("D", A);
        E = new ByteRegister(); registers.put("E", A);
        H = new ByteRegister(); registers.put("H", A);
        L = new ByteRegister(); registers.put("L", A);
        // special flag register
        F = new FlagRegister(); registers.put("F", F);
        // double registers
        AF = new DoubleRegister(A, F);  registers.put("AF", AF);
        BC = new DoubleRegister(B, C);  registers.put("BC", BC);
        DE = new DoubleRegister(D, E);  registers.put("DE", DE);
        HL = new DoubleRegister(H, L);  registers.put("HL", HL);
        // program counter and stack pointer
        PC = new DoubleRegister(new ByteRegister(), new ByteRegister());
        SP = new DoubleRegister(new ByteRegister(), new ByteRegister());
    }

    public void step() {
        PC.set(PC.get() + 1);
        System.out.println(this);
    }

    public String toString() {
        return "";
    }
}
