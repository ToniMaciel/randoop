package instrument.testcase;

public class Assistant {
  public MyInterface sum;
  public AssistantTwo auxTwo;
  public AssistantThree auxThree;
  public String name;

  public Assistant(){
    this.auxTwo = new AssistantTwo(1);
    this.name = "Hi!";
  }

  public void setSum(MyInterface sum){
    this.sum = sum;
  }

  public AssistantReturn gluetext(String one, String two){
    return new AssistantReturn(one + " : " + two + " : "+this.name);
  }

  @Override
  public String toString(){
    return this.sum.toString() + " : " + this.auxTwo.toString() + " : " + this.auxThree.toString();
  }

}
