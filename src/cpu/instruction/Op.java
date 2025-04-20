package cpu.instruction;

import java.util.function.Consumer;

// wraps Operand Consumer
public interface Op extends Consumer<Operands> {}
