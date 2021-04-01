package instrument.testcase;

public class AuxReturn {
  public String text;

  public AuxReturn(String text){
    this.text = text;
  }

  @Override
  public String toString(){
    return this.text.toString();
  }

}
