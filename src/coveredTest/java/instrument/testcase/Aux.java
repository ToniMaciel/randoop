package instrument.testcase;

public class Aux {
  public MyInterface sum;
  public AuxTwo auxTwo;
  public AuxThree auxThree;
  public String name;

  public Aux(){
    this.auxTwo = new AuxTwo(1);
    this.name = "Hi!";
  }

  public void setSum(MyInterface sum){
    this.sum = sum;
  }

  public AuxReturn gluetext(String one, String two){
    return new AuxReturn(one + " : " + two + " : "+this.name);
  }

  @Override
  public String toString(){
    return this.sum.toString() + " : " + this.auxTwo.toString() + " : " + this.auxThree.toString();
  }

}
