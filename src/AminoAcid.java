public class AminoAcid {

  private char type;
  private byte nrInChain;
  private char direction;
  private byte xPos;
  private byte yPos;
  private byte depth;



  public AminoAcid(char t, int nr, char d){
    type = t;
    nrInChain = (byte)nr;
    direction = d;
    xPos = 0;
    yPos = 0;
    depth = 0;
  }

  public String toString(){
    return "nrInChain: " + nrInChain + " | type: " + type + "| direction: " + direction;
  }


  public int getxPos() {
    return xPos;
  }

  public void setxPos(int xPos) {
    this.xPos = (byte)xPos;
  }

  public int getyPos() {
    return yPos;
  }

  public void setyPos(int yPos) {
    this.yPos = (byte)yPos;
  }

  public int getDepth() {
    return depth;
  }

  public void setDepth(int depth) {
    this.depth =(byte)depth;
  }

  public char toGenotype(){
    return direction;

  }

  public String toFold(){
    return type + String.valueOf(direction) + "";
  }
  public char getDirection() {
    return direction;
  }

  public void setDirection(char direction) {
    this.direction = direction;
  }

  public char getType() {
    return type;
  }

  public void setType(char type) {
    this.type = type;
  }

  public int getNrInChain() {
    return nrInChain;
  }

  public void setNrInChain(int nrInChain) {
    this.nrInChain = (byte)nrInChain;
  }

}

