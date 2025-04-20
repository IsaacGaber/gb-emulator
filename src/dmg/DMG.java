package dmg;
import cpu.CPU;
import memory.Memory;

public class DMG {

    public static void main(String[] args) throws Exception {
        Memory memory = new Memory();
        // CPU must be initialized with reference to memory -- imagine conduits connecting
        CPU cpu = new CPU(memory);

        while (true) {
            cpu.step();
            break;
        }
        
        System.out.println("Hello, World!");
    }

}
