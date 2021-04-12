package instrument.testcase;

public class AssistantTwo {
  public int numberOne;

  public AssistantTwo(int i){
    this.numberOne = i;
  }

  public AssistantTwo(int i, int o){
    this.numberOne = i+o;
  }

  public String getNumberOneAsString(String text){
    return String.valueOf(this.numberOne) + ":"+ text;
  }

}
