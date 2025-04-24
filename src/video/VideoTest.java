package video;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class VideoTest {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI(); 
            }
        });
    }

    private static void createAndShowGUI() {
        JFrame f = new JFrame();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.add(new Display());
        f.pack();
        // d.repaint();
        f.setVisible(true);   
        
    }
}
