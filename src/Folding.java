import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import javax.imageio.ImageIO;


public class Folding {
  Random rand = new Random();

  private int gridX;
  private int gridY;

  private enum RealtiveFoldingOrientation{
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
  private FoldingDirection newFoldingDirection = FoldingDirection.STRAIGHT;
  private RealtiveFoldingOrientation preFoldingOrientation = RealtiveFoldingOrientation.NORTH; //initialized with North!
  private AminoAcid [][][] grid;
  private ArrayList<hhContact> hhConnections = new ArrayList<>();
  private HashMap<Integer, AminoAcid> orderedAminoAcids = new HashMap<>();
  private int gridSize = 0;
  private int gridDepth = 0;
  private int aminoAcidStringLength = 0;
  private int overlapCounter = 0;



  private double fitness = 0.0;
  private int hhCounter = 0;
  private String genotype = "";
  private String aminoAcidString;

  //for drawing
  private int height = 3000;
  private int width = 3000;
  private int cellSize = 30;
  private int lineLength = 30;

  Folding(String aminoAcidString){
    this.aminoAcidString = aminoAcidString;
    generateRandomFolding();
  }

  public void generateRandomFolding(){
    newFoldingDirection = FoldingDirection.STRAIGHT;
    preFoldingOrientation = RealtiveFoldingOrientation.NORTH;
    //1L-0S-1R-0L-1L ?? L -> left R -> right S-> straight ; 0 -> hydrophil  // 1 -> hydrophob
    aminoAcidStringLength = aminoAcidString.length();
    gridSize = aminoAcidStringLength * 2 + 1;
    gridDepth = (aminoAcidStringLength / 4) + 2; //bei länge 20 können maximal 4 überlappungen passieren -> wegen LRSkodierung!
    //TODO: Improve memory usage by making array dynamic
    grid = new AminoAcid[gridSize][gridSize][gridDepth];

    gridX = aminoAcidStringLength;
    gridY = aminoAcidStringLength;

    for (int i = 0; i < aminoAcidStringLength; i++){
      char aminoAcidType = aminoAcidString.charAt(i);

      //first amino acid in middle of grid
      //random folding for testing
      int direction;
      if(i == 0){ //always fold straight first -> first fold is only rotation of the complete folding
                  //1/3 of solution candidates
        direction = 1;
      }
      else{
        direction = rand.nextInt(3);
        //direction = 2; //0:left   1:straight    2:right
      }

      newFoldingDirection = FoldingDirection.values()[direction];
      //add AminoAcid to HashMap -> needed later for drawing connections
      AminoAcid newAminoAcid = calculateCoordiantes(aminoAcidType, i, newFoldingDirection);
      //safe amino acid for drawing connections later
      orderedAminoAcids.put(newAminoAcid.getNrInChain(), newAminoAcid);
      //insert into grid
      insertAminoAcid(newAminoAcid, aminoAcidStringLength);
    }
    fitness = calculateFitness();
  }


  public void updateFolding(String genotype){
    newFoldingDirection = FoldingDirection.STRAIGHT;
    preFoldingOrientation = RealtiveFoldingOrientation.NORTH;
    //Method for updating a Folding after a Crossover or Mutation
    //create grid from genotype?
    this.genotype = "";
    orderedAminoAcids = new HashMap<>();

    grid = new AminoAcid[gridSize][gridSize][gridDepth];
    gridDepth = (aminoAcidStringLength / 4) + 2;;
    gridX = aminoAcidStringLength;
    gridY = aminoAcidStringLength;

    for (int i = 0; i < aminoAcidStringLength; i++){
      char aminoAcidType = aminoAcidString.charAt(i);
      char directionFromGenotype = genotype.charAt(i);

      switch (directionFromGenotype){
        case 'L':
          newFoldingDirection = FoldingDirection.LEFT;
          break;

        case 'S':
          newFoldingDirection = FoldingDirection.STRAIGHT;
          break;
        case 'R':
          newFoldingDirection = FoldingDirection.RIGHT;
          break;
        default:
          System.out.println("GENOTYPE NOT OK : updateFolding");
          newFoldingDirection = FoldingDirection.LEFT;
          break;
      }

      //add AminoAcid to HashMap -> needed later for drawing connections
      AminoAcid newAminoAcid = calculateCoordiantes(aminoAcidType, i, newFoldingDirection);
      //safe amino acid for drawing connections later
      orderedAminoAcids.put(newAminoAcid.getNrInChain(), newAminoAcid);
      //insert into grid
      insertAminoAcid(newAminoAcid, aminoAcidStringLength);
    }
    fitness = calculateFitness(); //updates hh counter and overlaps
    //grid = null;

  }
  //copy constructor
  public Folding(Folding another) {
    this.rand = another.rand;
    this.gridX = another.gridX;
    this.gridY = another.gridY;

    this.newFoldingDirection = another.newFoldingDirection;
    this.preFoldingOrientation = another.preFoldingOrientation;
    this.grid = another.grid;
    this.hhConnections = another.hhConnections;
    this.orderedAminoAcids = another.orderedAminoAcids;
    this.gridSize = another.gridSize;
    this.gridDepth = another.gridDepth;
    this.aminoAcidStringLength = another.aminoAcidStringLength;
    this.overlapCounter = another.overlapCounter;
    this.fitness = another.fitness;
    this.genotype = another.genotype;
    this.aminoAcidString = another.aminoAcidString;
    this.hhCounter = another.hhCounter;

    this.height = another.height;
    this.width = another.width;
    this.cellSize = another.cellSize;
    this.lineLength = another.lineLength;
  }

  public String printTest(){
    return Arrays.deepToString(grid).replace("], ", "]\n");
  }

  public String printFolding() {
    String s = "";
    char genotypeHelper[] = new char[aminoAcidStringLength - 1];
    String folding[] = new String[gridDepth];
    for (int x = 0; x < gridSize; x++) {
      for (int y = 0; y < gridSize; y++) {
        for (int d = 0; d < gridDepth; d++) {
          if (grid[x][y][d] != null) {
            folding[grid[x][y][d].getNrInChain()] = grid[x][y][d].toFold();

            if(grid[x][y][d].getNrInChain() > 0) genotypeHelper[grid[x][y][d].getNrInChain()-1] = grid[x][y][d].toGenotype(); //ignore first direction

          }
        }
      }
    }
    s += "\n\nFolding: ";
    for (String value : folding) {
      s += value + "-";
    }

    setGenotype(String.valueOf(genotypeHelper));
    s += "\n\nGenotype/solution candidate: " + getGenotype();

    s += "\n\nOverlap Count: " + overlapCounter;
    s += "\nHH Count: " + hhCounter;
    s += "\nFitness: " + fitness;
    return s;
  }

  public void drawFolding(){

    width = getGridSize() * (cellSize + lineLength);
    height = getGridSize() * (cellSize + lineLength);

    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    Graphics2D g2 = image.createGraphics();
    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

    g2.setColor(Color.LIGHT_GRAY);
    g2.fillRect(0, 0, width, height);

    Font font = new Font("Arial", Font.PLAIN, 27);
    g2.setFont(font);

    int firstAcidPosX;
    int firstAcidPosY;
    int secondAcidPosX;
    int secondAcidPosY;

    //draw AminoAcid chain connections -> draw before rectangles => not overlapping with numbers
    for (int i = 0; i+1 < orderedAminoAcids.size(); i++) {

      firstAcidPosX = (orderedAminoAcids.get(i).getxPos() * (cellSize + lineLength)) + cellSize;
      firstAcidPosY = height - ((orderedAminoAcids.get(i).getyPos() * (cellSize + lineLength)) + cellSize);

      secondAcidPosX = (orderedAminoAcids.get(i+1).getxPos() * (cellSize + lineLength)) + cellSize;
      secondAcidPosY = height - ((orderedAminoAcids.get(i+1).getyPos() * (cellSize + lineLength)) + cellSize);

      //calc center pos
      firstAcidPosX += cellSize / 2;
      firstAcidPosY += cellSize / 2;

      secondAcidPosX += cellSize / 2;
      secondAcidPosY += cellSize / 2;

      //draw line
      g2.setColor(Color.BLACK);
      g2.setStroke(new BasicStroke(2));
      g2.drawLine(firstAcidPosX , firstAcidPosY, secondAcidPosX , secondAcidPosY);

    }

    int x = 0 , y = 0;
    int centerX = width / 2;
    int centerY = height / 2;
    int coordinates[] = new int[2];
    g2.setColor(Color.GREEN);
    g2.fillRect(centerX-4, centerY-4, cellSize+8, cellSize+8);


    for (int xgrid = 0; xgrid < getGridSize(); xgrid++) {
      for (int ygrid = 0; ygrid < getGridSize(); ygrid++)
        if (grid[xgrid][ygrid][0] != null) {
          //draw
          if (grid[xgrid][ygrid][0].getType() == '0'){
            g2.setColor(Color.WHITE);
          }
          else {
            g2.setColor(Color.BLACK);
          }

          if(grid[xgrid][ygrid][1] != null){
            g2.setColor(Color.MAGENTA);
          }



          //calculate coordinates of rectangle
          x = (xgrid * (cellSize + lineLength)) + cellSize;
          y = height - ((ygrid * (cellSize + lineLength)) +cellSize); //buffered image top left is y = 0 -> y++ -> go down y- -> go up
                                                          //height - coordinates inverts this
          g2.fillRect(x, y , cellSize, cellSize);

          String label = grid[xgrid][ygrid][0].getNrInChain() + "";
          if (grid[xgrid][ygrid][0].getType() == '0'){
            g2.setColor(Color.BLACK);
          }
          else{
            g2.setColor(Color.WHITE);
          }
          g2.drawString(label, x , y +cellSize);

        }


    }
    g2.setColor(Color.BLACK);
    Font font2 = new Font("Arial", Font.BOLD, 50);
    g2.setFont(font2);
    String s ="";
    s += "\n\nGenotype/solution candidate: " + getGenotype();
    g2.drawString(s, 10, 60);
    s = "";
    s += "\n\nOverlap Count: " + overlapCounter;
    g2.drawString(s, 10, 120);
    s = "";
    s += "\nHH Count: " + hhCounter;
    g2.drawString(s, 10, 180);
    s ="";
    s += "\nFitness: " + fitness;
    g2.drawString(s, 10, 240);

    //safe picture
    String folder = "foldImages";

    //String filename = genotype + ".png";
    String filename = "ImageFromFold.png";
    if (new File(folder).exists() == false) new File(folder).mkdirs();

    try {
      ImageIO.write(image, "png", new File(folder + File.separator + filename));
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(0);
    }
  }

  private int calculateEnergy(){
    //calculate count of HH - contacts
    int hhcontacts = 0;
    for (int x = 0; x < gridSize; x++){
      for (int y = 0; y < gridSize; y++){
        for (int d = 0; d < gridDepth; d++){ //jede Überlappung muss seperat betrachtet werden

          if(grid[x][y][d] == null){
            break; //escape d loop-> no need to iterate over empty cells -> all null when 0 is null
          }
          else if(grid[x][y][d] != null){

            if(grid[x][y][d].getType() == '1'){ //is type 1?
              int nrInChain = grid[x][y][d].getNrInChain();
              int neighbourOffsetX = 0;
              int neighbourOffsetY = 0;

              for (int neighbourDepthOffset = gridDepth-d-1; (neighbourDepthOffset+d)  >= 0 ; neighbourDepthOffset--){
                for (int i = 0; i < 4; i++) {
                  //look at neighbours -> 4 -> NORTH, EAST, SOUTH, WEST

                  int offset[] = orientationToCoordinates(RealtiveFoldingOrientation.values()[i]);

                  //check if neighbour is not out of bounds
                  if ((x + offset[0]) < gridSize) neighbourOffsetX = offset[0];
                  else break;

                  if ((y + offset[1]) < gridSize) neighbourOffsetY = offset[1];
                  else break;

                  if (grid[x + neighbourOffsetX][y + neighbourOffsetY][d+neighbourDepthOffset] != null) { //is not null?
                    if (grid[x + neighbourOffsetX][y + neighbourOffsetY][d+neighbourDepthOffset].getType() == '1') {
                      if ((grid[x + neighbourOffsetX][y + neighbourOffsetY][d+neighbourDepthOffset].getNrInChain() - 1)
                          != nrInChain) {
                        if ((grid[x + neighbourOffsetX][y + neighbourOffsetY][d+neighbourDepthOffset].getNrInChain() + 1)
                            != nrInChain) {

                          hhcontacts++;
//                          hhConnections.add(new hhContact(grid[x][y][d+neighbourDepthOffset],
//                              grid[x + neighbourOffsetX][y + neighbourOffsetY][d+neighbourDepthOffset]));

                        }
                      }
                    }
                  }
                }
              }
            }

          }

        }
      }
    }
    hhcontacts /= 2; // divide by 2 -> counts twice when pair is found -> still only one pair
    hhCounter = hhcontacts;
    return hhcontacts;
  }

  private double countOverlap(){
    //jede Überlappung muss seperat betrachtet werden!
    // z.B.: 1-2 1-3 1-4 2-3 2-4 3-4 bei Stabel 1234
    int overlapAcidsCounter = 0;
    double overlap = 0;
    for (int x = 0; x < gridSize; x++) {
      for (int y = 0; y < gridSize; y++) {
        overlapAcidsCounter = 0;
        for (int d = 0; d < gridDepth; d++) {
          if (grid[x][y][d] == null) {
            break; //escape d loop-> no need to iterate over empty cells -> all null when 0 is null
          }
          else if(grid[x][y][d] != null){
            overlapAcidsCounter++;
          }
        }
        overlap += (overlapAcidsCounter-1) * overlapAcidsCounter;

      }

    }
    overlap/=2;
    overlapCounter = (int)overlap;
    return overlap;
  }

  private double calculateFitness(){
    //fitness = HHcontactCount / (1 + overlaps)
    return calculateEnergy() / ( 1 + countOverlap());
  }

  private void insertAminoAcid(AminoAcid a, int proteinStringLength){
    //check if coordinates are already occupied -> Overlapping
    for (int d = 0; d < proteinStringLength; d++) {
      if (grid[gridX][gridY][d] == null) { //not occupied
        genotype += a.getDirection();
        a.setxPos(gridX);
        a.setyPos(gridY);
        a.setDepth(d);
        grid[gridX][gridY][d] = a;
        break;
      } // else loop until list is not occupied by amino acid
    }
  }

  private int[] orientationToCoordinates(RealtiveFoldingOrientation orientation){
    int []coords = new int[2];
    switch (orientation){
      case NORTH:
        coords[0] = 0;
        coords[1] = 1;
        break;
      case EAST:
        coords[0] = 1;
        coords[1] = 0;
        break;
      case SOUTH:
        coords[0] = 0;
        coords[1] = -1;
        break;
      case WEST:
        coords[0] = -1;
        coords[1] = 0;
        break;
    }
    return coords;
  }

  private void updateGridCoordinates(RealtiveFoldingOrientation orientation){
    switch (orientation){
      case NORTH:
        gridY++;
        break;
      case EAST:
        gridX++;
        break;
      case SOUTH:
        gridY--;
        break;
      case WEST:
        gridX--;
        break;
      }
  }

  private AminoAcid calculateCoordiantes(char type, int index, FoldingDirection newRandomFoldingDirection){

    switch (preFoldingOrientation){
      case NORTH:
        switch(newRandomFoldingDirection){
          case LEFT: //left -> x same, y +1
            //NORTH + LEFT
            preFoldingOrientation = RealtiveFoldingOrientation.WEST;
            updateGridCoordinates(preFoldingOrientation);
            return new AminoAcid(type, index, 'L');

          case STRAIGHT: //straight -> x +1 , y same
            //preFoldingOrientation = preFoldingOrientation; //stays the same
            updateGridCoordinates(preFoldingOrientation);
            return new AminoAcid(type, index, 'S');

          case RIGHT: //right -> x same , y - 1
            preFoldingOrientation = RealtiveFoldingOrientation.EAST;
            updateGridCoordinates(preFoldingOrientation);
            return new AminoAcid(type, index, 'R');

        }
        break;


      case EAST:
        switch(newRandomFoldingDirection){
          case LEFT: //left -> x same, y +1
            //NORTH + LEFT
            preFoldingOrientation = RealtiveFoldingOrientation.NORTH;
            updateGridCoordinates(preFoldingOrientation);
            return new AminoAcid(type, index, 'L');

          case STRAIGHT: //straight -> x +1 , y same
            //preFoldingOrientation = preFoldingOrientation; //stays the same
            updateGridCoordinates(preFoldingOrientation);
            return new AminoAcid(type, index, 'S');

          case RIGHT: //right -> x same , y - 1
            preFoldingOrientation = RealtiveFoldingOrientation.SOUTH;
            updateGridCoordinates(preFoldingOrientation);
            return new AminoAcid(type, index, 'R');
        }
        break;


      case SOUTH:
        switch(newRandomFoldingDirection){
          case LEFT: //left -> x same, y +1
            //NORTH + LEFT
            preFoldingOrientation = RealtiveFoldingOrientation.EAST;
            updateGridCoordinates(preFoldingOrientation);
            return new AminoAcid(type, index, 'L');

          case STRAIGHT: //straight -> x +1 , y same
            //preFoldingOrientation = preFoldingOrientation; //stays the same
            updateGridCoordinates(preFoldingOrientation);
            return new AminoAcid(type, index, 'S');

          case RIGHT: //right -> x same , y - 1
            preFoldingOrientation = RealtiveFoldingOrientation.WEST;
            updateGridCoordinates(preFoldingOrientation);
            return new AminoAcid(type, index, 'R');

        }
        break;


      case WEST:
        switch(newRandomFoldingDirection){
          case LEFT: //left -> x same, y +1
            //NORTH + LEFT
            preFoldingOrientation = RealtiveFoldingOrientation.SOUTH;
            updateGridCoordinates(preFoldingOrientation);
            return new AminoAcid(type, index, 'L');

          case STRAIGHT: //straight -> x +1 , y same
            //preFoldingOrientation = preFoldingOrientation; //stays the same
            updateGridCoordinates(preFoldingOrientation);
            return new AminoAcid(type, index, 'S');

          case RIGHT: //right -> x same , y - 1
            preFoldingOrientation = RealtiveFoldingOrientation.NORTH;
            updateGridCoordinates(preFoldingOrientation);
            return new AminoAcid(type, index, 'R');

        }
        break;

    }
    return new AminoAcid(type, index, 'Z');
  }

  public void setGenotype(String genotype) {
    this.genotype = genotype;
  }

  public String getGenotype(){
    return genotype;
  }

  public String getAminoAcidString(){
    return aminoAcidString;
  }

  public double getFitness() {
    return fitness;
  }

  public int getGridSize() {
    return gridSize;
  }

  public int getGridDepth() {
    return gridDepth;
  }

  public int getOverlapCounter() {
    return overlapCounter;
  }

  public int getHhCounter() {
    return hhCounter;
  }

}


