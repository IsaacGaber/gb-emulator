package dmg;

import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import cpu.CPU;
import memory.Memory;
import video.Video;

public class DMG {
    // timings in hertz
    private static final int CPU_CLOCK = 4294304;
    private static final int FRAMERATE = 60;
    private static final double NANO_FRAMETIME = 1e9/FRAMERATE;
    private static final int LINES = 153; // scanlines per frame
    private static final int DOTS_LINE = 456;
    private static final int OSCILLATOR_CLOCK = 1048576; // base clock, CPU clock derived from this
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
        // set to hello-world by default
        romPath = "assets\\hello-world.gb";

        Memory memory = new Memory(romPath);

        // init vram display and display -- no connection between video elements and CPU -- inaccurate but simplified
        Video video = new Video(memory);
        
        // CPU must be initialized with reference to memory -- imagine conduits connecting elements
        CPU cpu = new CPU(memory);

    
        // handle different run modes
        System.out.println("Would you like to step through the ROM instruction by instruction or run until Halted?\nType (S) for Step Mode and (R) for Run Mode\nType anything else to exit:");
        String input = in.nextLine().toLowerCase();
        if (input.equals("s")) {
            boolean answer = true;
            while (answer && cpu.running()) {
                cpu.step();
                System.out.println(cpu);
                System.out.print("Continue stepping CPU? Y/N/R(run for 100 steps)/RR(run for 10,000 steps)/RRR(run for 1,000,000 steps): ");

                input = in.nextLine().toLowerCase();
                if (input.equals("r")) {
                    for (int i = 0; i < 100; i++) {
                        cpu.step();
                    }
                } else if(input.equals("rr")) {
                    for (int i = 0; i < 10000; i++) {
                        cpu.step();
                    }
                } else if(input.equals("rrr")) {
                    for (int i = 0; i < 1000000; i++) {
                        cpu.step();
                    }
                } else {
                    answer = input.equals("y");    
                }
            }
        } else if(input.equals("r")) {
            // assumes time to process frame never takes longer than frametime - otherwise I don't know what happens
            // ensures consistent clockspeed between frames
            final Runnable runner = new Runnable() {
                // runs for one frame, then pauses until time for next frame
                public void run(){
                    int cpuCycles = 0;
                    int lineCycles = 0;
                    int LY = memory.getLY();

                    while (cpuCycles <= CPU_CLOCK / FRAMERATE) {
                        cpuCycles += cpu.step();
                        lineCycles += cpu.step();
                        // triggers each scanline
                        if (lineCycles <= DOTS_LINE) {
                            // increment LY -- or set to zero
                            LY = LY < 154 ? LY + 1 : 0;
                            // update register and cycles per scanline
                            memory.setLY(LY);
                            lineCycles -= DOTS_LINE;
                        }
                    }
                    // redraw screen at the end of every frame
                    // ensure screen stays in sync with CPU 
                    video.redraw();
                };
            };    
            scheduler.scheduleAtFixedRate(runner, 0, (long) (NANO_FRAMETIME), TimeUnit.NANOSECONDS);
        }
        in.close();
    }
    
}
