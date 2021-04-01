package instrument.testcase;

public class AuxTwo {
  public int numberOne;

  public AuxTwo(int i){
    this.numberOne = i;
  }

  public AuxTwo(int i, int o){
    this.numberOne = i+o;
  }

  public String getNumberOneAsString(String text){
    return String.valueOf(this.numberOne) + ":"+ text;
  }

}
