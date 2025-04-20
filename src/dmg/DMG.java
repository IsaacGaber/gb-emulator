package dmg;
import cpu.CPU;
import memory.Memory;

public class DMG {

    public static void main(String[] args) throws Exception {
        Memory memory = new Memory();
        // CPU must be initialized with reference to memory -- imagine conduits connecting
        CPU cpu = new CPU(memory);

        for (int i = 0; i < 3; i++) {
            cpu.step();

        }
        // while (true) {
        // }
        
        // System.out.println("Hello, World!");
    }

}
