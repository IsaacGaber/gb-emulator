package video;

// import java.awt.BasicStroke;
// import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import memory.Memory;


// renders background to buffered image panel
public class Display extends JPanel {
    
    private static final int _TILEMAP_START = 0x9800;
    private static final int _TILEMAP_END = 0x9C00;
    private static final int _TILE_START = 0x8000;
    private static final int _TILE_START_2= 0x9000;
    private static final int _TILEMAP_SIZE = 32;
    
    public static final int BG_SIZE = 256;
    public static final int SCREEN_SCALE = 4;
    public static final Dimension SCREEN_SIZE = new Dimension(160, 144);

    private Tile[] _tiles;
    private BufferedImage _framebuffer; 
    private Memory _memory;


    public Display(Memory memory) {
        _memory = memory;
        _framebuffer = new BufferedImage(BG_SIZE, BG_SIZE, BufferedImage.TYPE_3BYTE_BGR);
        _tiles = new Tile[_TILEMAP_SIZE * _TILEMAP_SIZE];
    }

    public Dimension getPreferredSize() {
        return new Dimension(SCREEN_SIZE.width * SCREEN_SCALE, SCREEN_SIZE.height * SCREEN_SCALE);
    }

    private void updateTiles() {
        //  iterate through tile map
        // controls where to look for bg tiles
        boolean addressingType = _memory.getBGWTileData();

        for (int i = _TILEMAP_START; i < _TILEMAP_END; i++) {
            int tileIndex = _memory.getByte(i);
            byte[] tileBytes = new byte[Tile.BYTE_LENGTH];
            for (int j = 0; j < Tile.BYTE_LENGTH; j++) {
                int bytePos;
                if (addressingType) {
                    bytePos = (tileIndex * Tile.BYTE_LENGTH) + _TILE_START + j;
                } else {
                    bytePos = (tileIndex * Tile.BYTE_LENGTH) + _TILE_START_2 + j;
                }
                tileBytes[j] = (byte) _memory.getByte(bytePos);    

            }
            Tile t = new Tile(tileBytes);
            _tiles[i - _TILEMAP_START] = t;
        }
    }

    private void drawTiles(BufferedImage target) {
        for (int i = 0; i < _tiles.length; i++) {
            int tileX = i % _TILEMAP_SIZE;
            int tileY = i / _TILEMAP_SIZE;
            Colors[] c = _tiles[i].tileColors();
            // render each tile seperately
            for (int j = 0; j < c.length; j++) {
                int pixelX = j % Tile.SIZE;
                int pixelY = j / Tile.SIZE;

                _framebuffer.setRGB(tileX * Tile.SIZE + pixelX, tileY * Tile.SIZE + pixelY, c[j].COLOR);
            }
        }
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);  
        updateTiles();
        drawTiles(_framebuffer);
        Graphics2D g2d = (Graphics2D) g;
        // draw background viewbuffer scaled to panel size with scroll x and scroll y offsets applied
        g2d.drawImage(_framebuffer.getScaledInstance(BG_SIZE * SCREEN_SCALE, BG_SIZE * SCREEN_SCALE, 0),
                        0 - _memory.getSCX() * SCREEN_SCALE, 0 - _memory.getSCY() * SCREEN_SCALE, null);
    }  


}
