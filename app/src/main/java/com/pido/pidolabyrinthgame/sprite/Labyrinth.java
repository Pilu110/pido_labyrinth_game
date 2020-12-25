package com.pido.pidolabyrinthgame.sprite;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;

import com.pido.pidolabyrinthgame.R;

import java.util.HashMap;
import java.util.Map;

public class Labyrinth implements Sprite {

    public static final int TILE_WIDTH = 128;
    public static final int TILE_HEIGHT = 128;
    public static final int MAP_WIDTH = 16;
    public static final int MAP_HEIGHT = 23;

    public enum Direction {
        NONE,
        UP,
        RIGHT,
        DOWN,
        LEFT
    }

    public enum Tile {
        FLOOR(R.drawable.floor),
        WALL(R.drawable.wall);

        int resourceId;

        Tile(int resourceId) {
            this.resourceId = resourceId;
        }
    }

    public static final int NUM_OF_PLAYERS = 2;

    private final Tile[][] map;
    private static final Map<Tile, Bitmap> tileImage = new HashMap<>();
    private static double scale;
    private static int scaledTileWidth, scaledTileHeight;
    private static int mapCornerX, mapCornerY;

    public Labyrinth(Resources resources) {
        map = new Tile[MAP_WIDTH][MAP_HEIGHT];

        int screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
        int screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;
        double scaleX = (double) screenWidth / (double)(MAP_WIDTH * TILE_WIDTH);
        double scaleY = (double) screenHeight / (double)(MAP_HEIGHT * TILE_HEIGHT);
        scale = Math.min(scaleX, scaleY);

        scaledTileWidth = (int)(scale * TILE_WIDTH);
        scaledTileHeight = (int)(scale * TILE_HEIGHT);

        mapCornerX = (screenWidth - scaledTileWidth * MAP_WIDTH) / 2;
        mapCornerY = (screenHeight - scaledTileHeight * MAP_HEIGHT) / 2;

        loadTileImages(resources);

        generate();
    }

    public void generate() {

        int[][] depthMap = new int[MAP_WIDTH][MAP_HEIGHT];

        for (int x = 0; x < MAP_WIDTH; x++) {
            for (int y = 0; y < MAP_HEIGHT; y++) {
                map[x][y] = Tile.WALL;
                depthMap[x][y] = Integer.MAX_VALUE;
            }
        }

        for (int x = 0; x < 2; x++) {
            for (int y = 0; y < 2; y++) {
                map[x][y] = Tile.FLOOR;
                depthMap[x][y] = 0;
            }
        }


        int iteration = 0;

        int x = 1;
        int y = 1;

        int numOfFloors = 9;

        double p1 = 0.4;

        do {

            Direction selectedDirection = Direction.NONE;
            int maxDepth = 0;

            for(Direction direction : Direction.values()) {
                switch (direction) {
                    case UP:
                        if (y > 1 && x < MAP_WIDTH - 1 && x > 0
                                && ((map[x][y - 1] == Tile.WALL
                                    && map[x - 1][y - 1] == Tile.WALL
                                    && map[x + 1][y - 1] == Tile.WALL
                                    && map[x][y - 2] == Tile.WALL)
                                    || map[x][y - 1] == Tile.FLOOR
                                )
                        ) {
                            if(depthMap[x][y-1] > maxDepth || (depthMap[x][y-1] == maxDepth && Math.random() < p1)) {
                                maxDepth = depthMap[x][y-1];
                                selectedDirection = Direction.UP;
                            }
                        }
                        break;
                    case RIGHT:
                        if (x < MAP_WIDTH - 2 && y < MAP_HEIGHT - 1 && y > 0
                                && ((map[x + 1][y] == Tile.WALL
                                    && map[x + 1][y - 1] == Tile.WALL
                                    && map[x + 1][y + 1] == Tile.WALL
                                    && map[x + 2][y] == Tile.WALL)
                                    || map[x + 1][y] == Tile.FLOOR
                                )
                        ) {
                            if(depthMap[x+1][y] > maxDepth || (depthMap[x+1][y] == maxDepth && Math.random() < p1)) {
                                maxDepth = depthMap[x+1][y];
                                selectedDirection = Direction.RIGHT;
                            }
                        }
                        break;
                    case DOWN:
                        if (y < MAP_HEIGHT - 2 && x < MAP_WIDTH - 1 && x > 0
                                && ((map[x][y + 1] == Tile.WALL
                                    && map[x - 1][y + 1] == Tile.WALL
                                    && map[x + 1][y + 1] == Tile.WALL
                                    && map[x][y + 2] == Tile.WALL)
                                || map[x][y + 1] == Tile.FLOOR
                            )
                        ) {
                            if(depthMap[x][y+1] > maxDepth || (depthMap[x][y+1] == maxDepth && Math.random() < p1)) {
                                maxDepth = depthMap[x][y+1];
                                selectedDirection = Direction.DOWN;
                            }
                        }
                        break;

                    case LEFT:
                        if (x > 1 && y < MAP_HEIGHT -1 && y > 0
                                && ((map[x - 1][y] == Tile.WALL
                                    && map[x - 1][y - 1] == Tile.WALL
                                    && map[x - 1][y + 1] == Tile.WALL
                                    && map[x - 2][y] == Tile.WALL)
                                || map[x - 1][y] == Tile.FLOOR
                            )
                        ) {
                            if(depthMap[x-1][y] > maxDepth || (depthMap[x-1][y] == maxDepth && Math.random() < p1)) {
                                maxDepth = depthMap[x-1][y];
                                selectedDirection = Direction.LEFT;
                            }
                        }
                        break;
                }
            }

            if(selectedDirection == Direction.NONE) {
                break;
            }

            int previousDepth = depthMap[x][y];

            int previousX = x;
            int previousY = y;

            switch (selectedDirection) {
                case UP: y--; break;
                case RIGHT: x++; break;
                case DOWN: y++; break;
                case LEFT: x--; break;
            }

            if(depthMap[x][y] == Integer.MAX_VALUE) {
                depthMap[x][y] = previousDepth + 1;
            }
            else if(map[x][y] == Tile.FLOOR) {
                depthMap[previousX][previousY] = 0;
            }

            map[x][y] = Tile.FLOOR;


            iteration++;
        } while (numOfFloors < MAP_HEIGHT * MAP_WIDTH / 3 && iteration < 5000);
    }

    @Override
    public void draw(Canvas canvas) {
        for (int x = 0; x < MAP_WIDTH; x++) {
            for (int y = 0; y < MAP_HEIGHT; y++) {
                canvas.drawBitmap(tileImage.get(map[x][y]), mapCornerX + x * scaledTileWidth, mapCornerY + y * scaledTileHeight, null);
            }
        }
    }

    @Override
    public void update() {

    }

    public static void loadTileImages(Resources resources) {
        for(Tile tile : Tile.values()) {
            tileImage.put(tile, Bitmap.createScaledBitmap(BitmapFactory.decodeResource(resources, tile.resourceId), scaledTileWidth, scaledTileHeight, false));
        }
    }

}
