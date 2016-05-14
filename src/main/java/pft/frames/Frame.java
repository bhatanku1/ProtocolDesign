package pft.frames;


public abstract class Frame {

  private final int identifier;

  public Frame(int identifier) {
    this.identifier = identifier;
  }

  public int identifier() {
    return identifier;
  }

  public abstract byte type();

}
