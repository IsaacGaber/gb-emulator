package video;

import javax.swing.*;

import memory.Memory;

// video unit, main class of package
public class Video {
    private Memory _memory; 
    private Display _display;
    private VRAMDisplay _vramDisplay;

    @SuppressWarnings("unused")
    private Video(){};


    public Video(Memory memory) {
        _memory = memory;

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                // init display and frame
                JFrame f = new JFrame("GB Emulator");
                _display = new Display(memory);
                f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                f.add(_display);
                f.pack();
                f.setResizable(false);
                f.setVisible(true);
                // init VRAM display and frame
                JFrame f2 = new JFrame("VRAM tile view");
                // can close tile view without exiting emulator -- no need to set close operation
                // f2.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                _vramDisplay = new VRAMDisplay(_memory);
                f2.add(_vramDisplay);
                f2.pack();
                f2.setResizable(false);
                f2.setVisible(true);
            }});

    }

    /** redraw video elements.
     * 
     */
    public void redraw(){
        _display.repaint();
        _vramDisplay.repaint();
    }
    
}
