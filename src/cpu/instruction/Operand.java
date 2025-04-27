package cpu.instruction;

public enum Operand {
    // descriptions according to pandocs
    R8,  // Any of the 8-bit registers (A, B, C, D, E, H, L).
    R16, // Any of the general-purpose 16-bit registers (BC, DE, HL).
    N8,  // 8-bit integer constant (signed or unsigned, -128 to 255).
    N16, // 16-bit integer constant (signed or unsigned, -32768 to 65535).
    A16, // 16-bit address
    E8,  // 8-bit signed offset (-128 to 127).
    U3, // 3-bit unsigned bit index (0 to 7, with 0 as the least significant bit).
    CC; // conditional codes

}