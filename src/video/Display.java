package video;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.*;
import java.util.ArrayList;


public class Display extends JPanel {
    private ArrayList<Tile> _tiles;
    BufferedImage _framebuffer; 

    public Display() {  
        _framebuffer = new BufferedImage(256, 256, BufferedImage.TYPE_3BYTE_BGR);
        setBorder(BorderFactory.createLineBorder(Color.black));

        Tile testTile = new Tile(new byte[]{0x3C, 0x7E, 0x42, 0x42, 0x42, 0x42, 0x42, 0x42, 
                                            0x7E, 0x5E, 0x7E, 0x0A, 0x7C, 0x56, 0x38, 0x7C});

        _tiles = new ArrayList<>();
        _tiles.add(testTile);
    }

    public Dimension getPreferredSize() {
        return new Dimension(256,256);
    }

    public void drawTiles(ArrayList<Tile> tiles, BufferedImage target) {
        Tile tile = tiles.get(0);
        Colors[] tileColors = tile.tileColors();
        for (int i = 0; i < tileColors.length; i++) {
            _framebuffer.setRGB(i % tile.SIZE, i / tile.SIZE, tileColors[i].COLOR);
        }
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);       
        // setBackground(getBackground());
        drawTiles(_tiles, _framebuffer);
        ((Graphics2D)g).drawImage(_framebuffer.getScaledInstance(1024, 1024, 0), 1, 1, null);
        // Draw Text
        // g.drawString("This is my custom Panel!",10,20);
    }  
}