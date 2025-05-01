package dmg;

import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import cpu.CPU;
import memory.Memory;
import video.Video;

public class DMG {
    // timings in hertz
    private static final int CPU_CLOCK = 4294304;
    private static final int FRAMERATE = 60;
    private static final double NANO_FRAMETIME = 1e9/FRAMERATE;
    private static final int LINES = 153;
    private static final int DOTS_LINE = 456;
    private static final int OSCILLATOR_CLOCK = 1048576;
    private final static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    public static void main(String[] args) throws Exception {
        String romPath;
        Scanner in = new Scanner(System.in);
        // load file to run 
        // assumes file will be gameboy binary
        // while (true) {
        //     try {
        //         System.out.print("Input the path of the ROM you would like to run: ");
        //         romPath = in.nextLine();
        //         break;
        //     } catch (Exception e) {
        //         System.out.println("Invalid file path");
        //     }
        // }
        romPath = "assets\\hello-world.gb";

        Memory memory = new Memory(romPath);
        // init vram display and display
        Video video = new Video(memory);
        
        // CPU must be initialized with reference to memory -- imagine conduits connecting elements
        CPU cpu = new CPU(memory);

    
        // handle different run modes
        System.out.println("Would you like to step through the ROM or run until Halted?\nType (S) for Step Mode and (R) for Run Mode\nType anything else to exit:");
        String input = in.nextLine().toLowerCase();
        if (input.equals("s")) {
            boolean answer = true;
            while (answer && cpu.running()) {
                cpu.step();
                System.out.println(cpu);
                System.out.print("Continue stepping CPU? Y/N/R(run for 100 steps)/RR(run for 1000 steps): ");

                input = in.nextLine().toLowerCase();
                if (input.equals("r")) {
                    for (int i = 0; i < 100; i++) {
                        cpu.step();
                    }
                } else if(input.equals("rr")) {
                    for (int i = 0; i < 1000; i++) {
                        cpu.step();
                    }
                } else {
                    answer = input.equals("y");    
                }
            }
        } else if(input.equals("r")) {
            // ensure consistent clockspeed

            final Runnable runner = new Runnable() {
                public void run(){
                    int cpuCycles = 0;
                    while (cpuCycles < DOTS_LINE) {
                        cpuCycles += cpu.step();
                    }
                    int LY = memory.getLY();
                    LY = LY < 154 ? LY + 1 : 0;
                    // update 
                    memory.setLY(LY);
                };
            };    
            scheduler.scheduleAtFixedRate(runner, 0, (long) (CPU_CLOCK / LINES), TimeUnit.NANOSECONDS);
        }
        in.close();
        // System.out.println("Exiting");
        // System.exit(0);

    }
    
}
