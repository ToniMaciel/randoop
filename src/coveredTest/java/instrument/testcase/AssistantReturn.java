package instrument.testcase;

public class AssistantReturn {
  public String text;

  public AssistantReturn(String text){
    this.text = text;
  }

  @Override
  public String toString(){
    return this.text.toString();
  }

}
