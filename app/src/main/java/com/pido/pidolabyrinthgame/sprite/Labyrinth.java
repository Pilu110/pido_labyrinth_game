package com.pido.pidolabyrinthgame.sprite;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.Log;
import android.util.Pair;

import com.pido.pidolabyrinthgame.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.ContentValues.TAG;

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
        WALL(R.drawable.wall),
        SKULL(R.drawable.skull);

        int resourceId;

        Tile(int resourceId) {
            this.resourceId = resourceId;
        }
    }

    public static final int NUM_OF_PLAYERS = 2;

    private final Tile[][] map;
    private final int[][] depthMap;
    private int maxDepth;

    private static final Map<Tile, Bitmap> tileImage = new HashMap<>();

    private static double scale;
    private static int scaledTileWidth, scaledTileHeight;
    private static int mapCornerX, mapCornerY;

    private final Bitmap arrowImage;

    public Labyrinth(Resources resources) {
        map = new Tile[MAP_WIDTH][MAP_HEIGHT];
        depthMap = new int[MAP_WIDTH][MAP_HEIGHT];

        int screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
        int screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;
        double scaleX = (double) screenWidth / (double)(MAP_WIDTH * TILE_WIDTH);
        double scaleY = (double) screenHeight / (double)(MAP_HEIGHT * TILE_HEIGHT);
        scale = Math.min(scaleX, scaleY);

        scaledTileWidth = (int)(scale * TILE_WIDTH);
        scaledTileHeight = (int)(scale * TILE_HEIGHT);

        mapCornerX = (screenWidth - scaledTileWidth * MAP_WIDTH) / 2;
        mapCornerY = (screenHeight - scaledTileHeight * MAP_HEIGHT) / 2;

        arrowImage = loadArrowImage(resources);
        loadTileImages(resources);

        generateWalls();
        addSkulls();
    }

    public void addSkulls() {
        int skulls = 0;

        while (skulls < 5) {
            int x = (int)(Math.random() * (MAP_WIDTH - 4)) + 3;
            int y = (int)(Math.random() * (MAP_HEIGHT - 4)) + 3;

            if(map[x][y] == Tile.FLOOR && depthMap[x][y] > maxDepth/2) {
                map[x][y] = Tile.SKULL;
                skulls++;
            }

        }
    }

    public void generateWalls() {

        boolean [][] visited = new boolean[MAP_WIDTH][MAP_HEIGHT];

        for (int x = 0; x < MAP_WIDTH; x++) {
            for (int y = 0; y < MAP_HEIGHT; y++) {
                map[x][y] = Tile.WALL;
                depthMap[x][y] = Integer.MAX_VALUE;
                visited[x][y] = false;
            }
        }

        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                map[x][y] = Tile.FLOOR;
                depthMap[x][y] = 0;
            }
        }


        int iteration = 0;

        int numOfFloors = 9;

        double p1 = 0.4;

        List<Point> branches = new ArrayList<>();

        int x = 2;
        int y = 2;

        do {
            if (iteration % 10 == 0) {
                branches.add(new Point(x , y));
            }

            for(Point branch : branches) {

                x = branch.x;
                y = branch.y;

                Direction selectedDirection = Direction.NONE;
                int maxDepthNearby = 0;

                for (Direction direction : Direction.values()) {
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
                                if (!visited[x][y - 1] && depthMap[x][y - 1] > maxDepthNearby || (depthMap[x][y - 1] == maxDepthNearby && Math.random() < p1)) {
                                    maxDepthNearby = depthMap[x][y - 1];
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
                                if (!visited[x + 1][y] && depthMap[x + 1][y] > maxDepthNearby || (depthMap[x + 1][y] == maxDepthNearby && Math.random() < p1)) {
                                    maxDepthNearby = depthMap[x + 1][y];
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
                                if (!visited[x][y + 1] && depthMap[x][y + 1] > maxDepthNearby || (depthMap[x][y + 1] == maxDepthNearby && Math.random() < p1)) {
                                    maxDepthNearby = depthMap[x][y + 1];
                                    selectedDirection = Direction.DOWN;
                                }
                            }
                            break;

                        case LEFT:
                            if (x > 1 && y < MAP_HEIGHT - 1 && y > 0
                                    && ((map[x - 1][y] == Tile.WALL
                                    && map[x - 1][y - 1] == Tile.WALL
                                    && map[x - 1][y + 1] == Tile.WALL
                                    && map[x - 2][y] == Tile.WALL)
                                    || map[x - 1][y] == Tile.FLOOR
                            )
                            ) {
                                if (!visited[x - 1][y] && depthMap[x - 1][y] > maxDepthNearby || (depthMap[x - 1][y] == maxDepthNearby && Math.random() < p1)) {
                                    maxDepthNearby = depthMap[x - 1][y];
                                    selectedDirection = Direction.LEFT;
                                }
                            }
                            break;
                        }
                    }

                    if (selectedDirection == Direction.NONE) {
                        break;
                    }

                    int previousDepth = depthMap[x][y];

                    int previousX = x;
                    int previousY = y;

                    switch (selectedDirection) {
                        case UP:
                            y--;
                            break;
                        case RIGHT:
                            x++;
                            break;
                        case DOWN:
                            y++;
                            break;
                        case LEFT:
                            x--;
                            break;
                    }

                    if (depthMap[x][y] == Integer.MAX_VALUE) {
                        depthMap[x][y] = previousDepth + 1;

                        if (depthMap[x][y] > maxDepth) {
                            maxDepth = depthMap[x][y];
                        }
                    } else if (map[x][y] == Tile.FLOOR) {
                        visited[previousX][previousY] = true;
                    }

                    map[x][y] = Tile.FLOOR;

                    branch.set(x, y);
            }

            iteration++;
        } while (numOfFloors < MAP_HEIGHT * MAP_WIDTH / 3 && iteration < 10000);
    }

    @Override
    public void draw(Canvas canvas) {
        for (int x = 0; x < MAP_WIDTH; x++) {
            for (int y = 0; y < MAP_HEIGHT; y++) {
                //TODO: optimize by merging floor into sprite at loadTileImages
                canvas.drawBitmap(tileImage.get(Tile.FLOOR), mapCornerX + x * scaledTileWidth, mapCornerY + y * scaledTileHeight, null);
                canvas.drawBitmap(tileImage.get(map[x][y]), mapCornerX + x * scaledTileWidth, mapCornerY + y * scaledTileHeight, null);
            }
        }

        canvas.drawBitmap(arrowImage, mapCornerX, mapCornerY, null);
    }

    @Override
    public void update() {

    }

    public static void loadTileImages(Resources resources) {
        for(Tile tile : Tile.values()) {
            tileImage.put(tile, Bitmap.createScaledBitmap(BitmapFactory.decodeResource(resources, tile.resourceId), scaledTileWidth, scaledTileHeight, false));
        }
    }

    public static Bitmap loadArrowImage(Resources resources){
        return Bitmap.createScaledBitmap(BitmapFactory.decodeResource(resources, R.drawable.arrow), 3*scaledTileWidth, 3*scaledTileHeight, false);
    }

}
