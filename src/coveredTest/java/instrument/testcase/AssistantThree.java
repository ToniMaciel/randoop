package instrument.testcase;

public abstract class AssistantThree implements MyInterface{
  public int numberOne;

  public AssistantThree(int i){
    this.numberOne = i;
  }

  public int getSumTwoNumber(int i){
    return this.numberOne + i;
  }

}
