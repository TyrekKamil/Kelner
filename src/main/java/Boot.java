import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.scene.shape.Rectangle;

import java.util.Arrays;

public class Boot extends Application {
    private static final int WIDTH = 800, HEIGHT = 600, TILE_SIZE = 40, X_TILES = WIDTH / TILE_SIZE, Y_TILES = HEIGHT / TILE_SIZE;
    private Integer[][] grid = new Integer [X_TILES][Y_TILES];

    //Zczytywanie z pliku tekstowego w formacie Array[x][y] jesli rowne 0-stolik jesli 1-droga...
    private Parent createContent() {
        Pane pane = new Pane();
        pane.setPrefSize(WIDTH, HEIGHT);

        for (Integer row[]: grid) {
            Arrays.fill(row, 1);
        }

        for (int x = 0; x < X_TILES; x++){
            for (int y =1; y < Y_TILES; y++){
                // tiles wewnatrz scian
                if( y != 1) {
                    Tile tile = new Tile(x, y);
                    pane.getChildren().add(tile);
                }
                // przykladowy stolik, z pliku wystarczyloby zczytywac x i y i je tutaj sprawdzac w ifkach w liscie czy cus
                if((x==5 || x==4) && (y==7 || y==8)){
                    Table table = new Table(x, y);
                    pane.getChildren().add(table);
                    grid[x][y] = 2;
                }
                // przykladowy stolik
                if((x==6 || x==7) && (y==10 || y==11)){
                    Table table = new Table(x, y);
                    pane.getChildren().add(table);
                    grid[x][y] = 2;
                }
                // sciana
                if(x==X_TILES-1 || x == 0){
                    Wall wall = new Wall(x,y);
                    pane.getChildren().add(wall);
                    grid[x][y]=4;
                }
                // sciana
                if(y==Y_TILES-1 || (y==1 && x != 1)){
                    Wall wall = new Wall(x,y);
                    pane.getChildren().add(wall);
                    grid[x][y]=4;
                }
                // wyjscie wejscie
                if(x==X_TILES-1 && (y==8 || y==9)){
                    Exit exit = new Exit(x, y);
                    grid[x][y] = 3;
                    pane.getChildren().add(exit);
                }

            }
        }

        for (int x = 0; x < X_TILES; x++){
            grid[x][0] = 5;
        }
        for (int i = 0; i < Y_TILES; i++){
            for ( int j = 0; j < X_TILES; j++){
                System.out.print(grid[j][i]);
            }
            System.out.println("\n");
        }
        return pane;
    }

    private class Tile extends StackPane {
        private int x, y;
        private Rectangle border = new Rectangle(TILE_SIZE, TILE_SIZE);
        protected Tile(int x, int y) {
            this.x = x;
            this.y = y;

            border.setStroke(Color.LIGHTGRAY);
            border.setFill(Color.WHITE);

            getChildren().addAll(border);

            setTranslateX(x * TILE_SIZE);
            setTranslateY(y * TILE_SIZE);
        }
    }

    private class Wall extends StackPane {
        private int x, y;
        private Rectangle border = new Rectangle(TILE_SIZE, TILE_SIZE);
        protected Wall(int x, int y) {
            this.x = x;
            this.y = y;

            border.setStroke(Color.LIGHTGRAY);
            border.setFill(Color.BROWN);

            getChildren().addAll(border);

            setTranslateX(x * TILE_SIZE);
            setTranslateY(y * TILE_SIZE);
        }
    }

    private class Table extends StackPane {
        private int x, y;
        private Rectangle border = new Rectangle(TILE_SIZE, TILE_SIZE);

        protected Table(int x, int y) {
            this.x = x;
            this.y = y;

            border.setStroke(Color.BLACK);
            border.setFill(Color.BROWN);

            getChildren().addAll(border);

            setTranslateX(x * TILE_SIZE);
            setTranslateY(y * TILE_SIZE);
        }

    }


    private class Exit extends StackPane {
        private int x, y;
        private Rectangle border = new Rectangle(TILE_SIZE, TILE_SIZE);

        protected Exit(int x, int y) {
            this.x = x;
            this.y = y;

            border.setStroke(Color.GREEN);
            border.setFill(Color.LIGHTGRAY);

            getChildren().addAll(border);

            setTranslateX(x * TILE_SIZE);
            setTranslateY(y * TILE_SIZE);
        }

    }

    @Override
    public void start(Stage theStage) throws Exception {
        Scene scenes = new Scene(createContent());
        theStage.setTitle("AI Waiter");
        theStage.setScene(scenes);
        theStage.show();
    }

    public static void main(String[] args) {
        launch(args);

    }
}
