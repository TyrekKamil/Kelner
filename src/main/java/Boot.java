import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.scene.shape.Rectangle;
import java.awt.*;
import java.awt.Stroke;

public class Boot extends Application{
    public static final int WIDTH = 800, HEIGHT = 600, TILE_SIZE = 40, X_TILES = WIDTH / TILE_SIZE, Y_TILES = HEIGHT / TILE_SIZE;
    private Tile[][] grid = new Tile[X_TILES][Y_TILES];
    //Zczytywanie z pliku tekstowego w formacie Array[x][y] jesli rowne 0-stolik jesli 1-droga...
    private Parent createContent(){
        Pane pane = new Pane();
        pane.setPrefSize(WIDTH, HEIGHT);
        // w 1 rzedzie kuchnia, w ostatniej kolumnie wejscie.
        for(int y = 1; y < Y_TILES; y++){
            for(int x = 0; x < X_TILES -1; x++){
                //white albo black tile, nie wiem jeszcze jak, wstepnie druga metoda...
                if((y == 6  || y == 7) && x == X_TILES -2){
                    Tile tile = new Tile(x, y);
                    grid[x][y] = tile;
                    pane.getChildren().add(tile);
                    x = X_TILES - 1;
                }
                if( y != 2 || x == 0 ) {
                    Tile tile = new Tile(x, y);
                    grid[x][y] = tile;
                    pane.getChildren().add(tile);
                }
            }
        }

        return pane;
    }

    private class Tile extends StackPane{
        private int x, y;
        private Rectangle border = new Rectangle(TILE_SIZE, TILE_SIZE);
        public Tile(int x, int y){
            this.x = x;
            this.y = y;
            border.setStroke(Color.LIGHTGRAY);
            border.setFill(Color.WHITE);

            getChildren().addAll(border);

            setTranslateX(x * TILE_SIZE);
            setTranslateY(y * TILE_SIZE);
        }
    }


    @Override
    public void start(Stage theStage) throws Exception{
        Scene scenes = new Scene(createContent());
        theStage.setTitle("AI Waiter");
        theStage.setScene(scenes);
        theStage.show();
    }
    public static void main(String[] args) {
        launch(args);
    }
}
