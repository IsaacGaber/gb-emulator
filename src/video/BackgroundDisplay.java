package video;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import memory.Memory;


// renders background to buffered image panel
public class BackgroundDisplay extends JPanel {
    
    private static int TILEMAP_START = 0x9800;

    private static int TILEMAP_END = 0x9C00;
    private static int TILE_START = 0x8000;
    private static int TILE_START_2= 0x9000;

    private static int TILEMAP_SIZE = 32;
    private Tile[] _tiles;
    private BufferedImage _framebuffer; 
    private Memory _memory;

    public static final int BG_SIZE = 256;
    public static final int SCREEN_SCALE = 3;
    public static final Dimension SCREEN_SIZE = new Dimension(160, 144);

    public BackgroundDisplay(Memory memory) {
        _memory = memory;
        _framebuffer = new BufferedImage(BG_SIZE, BG_SIZE, BufferedImage.TYPE_3BYTE_BGR);
        _tiles = new Tile[TILEMAP_SIZE * TILEMAP_SIZE];
    }

    public Dimension getPreferredSize() {
        return new Dimension(BG_SIZE * SCREEN_SCALE, BG_SIZE * SCREEN_SCALE);
    }

    private void updateTiles() {
        // System.out.println(_memory.getByte(TILE_START));
        //  iterate through tile map
        
        boolean unsignedAddressing = _memory.getBGWTileData();

        for (int i = TILEMAP_START; i < TILEMAP_END; i++) {
            int tileIndex = _memory.getByte(i);
            byte[] tileBytes = new byte[Tile.BYTE_LENGTH];
            for (int j = 0; j < Tile.BYTE_LENGTH; j++) {
                int bytePos;
                if (unsignedAddressing) {
                    bytePos = (tileIndex * Tile.BYTE_LENGTH) + TILE_START + j;
                } else {
                    bytePos = (tileIndex * Tile.BYTE_LENGTH) + TILE_START_2 + j;
                }
                tileBytes[j] = (byte) _memory.getByte(bytePos);    

            }
            Tile t = new Tile(tileBytes);
            _tiles[i - TILEMAP_START] = t;
        }
    }

    private void drawTiles(BufferedImage target) {
        for (int i = 0; i < _tiles.length; i++) {
            int tileX = i % TILEMAP_SIZE;
            int tileY = i / TILEMAP_SIZE;
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
        g2d.drawImage(_framebuffer.getScaledInstance(getWidth(), getHeight(), 0), 1, 1, null);
        g2d.setColor(Color.RED);    
        g2d.setStroke(new BasicStroke(5));
        g2d.drawRect(0, _memory.getSCY() * SCREEN_SCALE, SCREEN_SIZE.width * SCREEN_SCALE, SCREEN_SIZE.height * SCREEN_SCALE);

    }  


}
