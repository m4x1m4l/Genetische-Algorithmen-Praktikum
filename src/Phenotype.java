
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.ImageIO;

public class Phenotype {
  private int height = 1500;
  private int width = 1500;
  private int cellSize = 30;
  private int lineLength = 30;
  private RealtiveOrientation orientation = RealtiveOrientation.NORTH; //start with up -> first L or R or S just rotates folding -> removed it
  private FoldingDirection foldingDirection = FoldingDirection.STRAIGHT;
  private ArrayList<Coordinate> savedCoordinates = new ArrayList<>();

  private enum RealtiveOrientation{
    NORTH, //0: North -> y + 1
    EAST,  //1: East -> x +1
    SOUTH, //2: South -> y - 1 in grid
    WEST   //3: West -> x - 1
  }
  private enum FoldingDirection{
    LEFT,
    STRAIGHT,
    RIGHT
  }


  public void drawPhenotype(String genotype, String aminoAcidString){
    width  =aminoAcidString.length() * (cellSize + lineLength) /2;
    height = aminoAcidString.length() * (cellSize + lineLength)/2;
    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    Graphics2D g2 = image.createGraphics();
    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

    g2.setColor(Color.ORANGE);
    g2.fillRect(0, 0, width, height);

    Font font = new Font("Arial", Font.PLAIN, 27);
    g2.setFont(font);

    int x = 0 , y = 0;
    int lastX = width / 2;
    int lastY = height / 2;
    int coordinates[] = new int[2];

    g2.setColor(Color.GREEN);
    g2.fillRect(lastX, lastY , cellSize+4, cellSize+4);

    for (int i = 0; i < aminoAcidString.length(); i++){

      char type = aminoAcidString.charAt(i);
      char direction;

      if(i > 0){ //index 0 is in reality index 1 -> removed first index in genotype
        direction = genotype.charAt(i);

      }else{ // first is straight
        direction = 'S';
      }

      //convert char to foldingDirection
      if (direction == 'L') foldingDirection = foldingDirection.LEFT;
      else if (direction == 'S') foldingDirection = foldingDirection.STRAIGHT;
      else if(direction == 'R') foldingDirection = foldingDirection.RIGHT;
      else { System.out.println("Fehler Zeile 84");}


      coordinates = calculateXYposition(lastX, lastY);
      x = coordinates[0];
      y = coordinates[1];

      //draw chain lines
      g2.setColor(Color.BLACK);
      g2.setStroke(new BasicStroke(2));
      g2.drawLine(lastX + (cellSize/2), lastY + (cellSize/2), x + (cellSize/2) , y + (cellSize/2));

      //draw rectangles
      if (type == '0'){
        g2.setColor(Color.WHITE);
      }
      else{
        g2.setColor(Color.BLACK);
      }
      //check if overlap -> overlap is marked with magenta rectangle
      for(Coordinate c : savedCoordinates) {
        if (c.getX() == x) {
          if (c.getY() == y) {
            g2.setColor(Color.MAGENTA);
            break;
          }
        }
      }
      savedCoordinates.add(new Coordinate(x,y));

      g2.fillRect(x, y , cellSize, cellSize);

      //put index number in rectangle
      String label = i + "";
      if (type == '0'){
        g2.setColor(Color.BLACK);
      }
      else{
        g2.setColor(Color.WHITE);
      }
      g2.drawString(label, x , y+cellSize);


      lastX = x;
      lastY = y;

    }



    //safe picture

    String folder = "foldImages";
    String filename = "ImageFromGenotype.png";
    if (new File(folder).exists() == false) new File(folder).mkdirs();

    try {
      ImageIO.write(image, "png", new File(folder + File.separator + filename));
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(0);
    }

  }
  public int[] calculateXYposition(int lastX, int lastY){
    int xy[] = new int[2];
    switch (orientation){
      case NORTH:
        switch(foldingDirection){
          case LEFT: //left -> x - , y same Y IS D !!!!!!-> TOP LEFT CORNER 0.0 -> runter ist y++ und rechts bleibt x++
            xy[0] = lastX - (cellSize + lineLength);
            xy[1] = lastY;
            orientation = RealtiveOrientation.WEST;
            return xy;
          case STRAIGHT: //x same , y +
            xy[0] = lastX;
            xy[1] = lastY - (cellSize + lineLength);
            return xy;
          case RIGHT: //x + , y same
            xy[0] = lastX + (cellSize + lineLength);
            xy[1] = lastY;
            orientation = orientation.EAST;
            return xy;

        }
        break;


      case EAST:
        switch(foldingDirection){
          case LEFT: //x same, y+
            xy[0] = lastX;
            xy[1] = lastY - (cellSize + lineLength);
            orientation = orientation.NORTH;
            return xy;
          case STRAIGHT: //x+ , y same
            xy[0] = lastX + (cellSize + lineLength);
            xy[1] = lastY;
            return xy;
          case RIGHT: //x same, y -
            xy[0] = lastX;
            xy[1] = lastY + (cellSize + lineLength);
            orientation = orientation.SOUTH;
            return xy;
        }
        break;


      case SOUTH:
        switch(foldingDirection){
          case LEFT: //x+, y same
            xy[0] = lastX + (cellSize + lineLength);
            xy[1] = lastY;
            orientation = orientation.EAST;
            return xy;

          case STRAIGHT: //x same, y-
            xy[0] = lastX;
            xy[1] = lastY + (cellSize + lineLength);
            return xy;

          case RIGHT: //-x , y same
            orientation = orientation.WEST;
            xy[0] = lastX - (cellSize + lineLength);
            xy[1] = lastY;
            return xy;

        }
        break;


      case WEST:
        switch(foldingDirection){
          case LEFT: //x same, y-
            xy[0] = lastX;
            xy[1] = lastY + (cellSize + lineLength);
            orientation = orientation.SOUTH;
            return xy;

          case STRAIGHT: //x- , y same
            xy[0] = lastX- (cellSize + lineLength);
            xy[1] = lastY;
            return xy;
          case RIGHT: //x same, y+
            xy[0] = lastX;
            xy[1] = lastY - (cellSize + lineLength);
            orientation = orientation.NORTH;
            return xy;

        }
        break;

    }
    return xy;
  }
}
