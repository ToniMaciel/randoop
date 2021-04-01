package instrument.testcase;

public abstract class AuxThree implements MyInterface{
  public int numberOne;

  public AuxThree(int i){
    this.numberOne = i;
  }

  public int getSumTwoNumber(int i){
    return this.numberOne + i;
  }

}
