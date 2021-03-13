package randoop.field;

import randoop.generation.SeedSequences;
import randoop.types.Type;

import java.util.concurrent.ThreadLocalRandom;

public class RandomValue {

    public Object getRandomValue(String type){
        switch (type){
            case "int": return getRandomIntValue();
            case "float": return getRandomFloatValue();
            case "double": return getRandomDoubleValue();
            case "long": return getRandomLongValue();
            case "boolean": return getRandomBooleanValue();
            case "class java.lang.String":
            case "java.lang.String":
            case "String":
                return getRandomStringValue();
        }
        return null;
    }

    private int getRandomIntValue(){
        return ThreadLocalRandom.current().nextInt();
    }

    private Object getRandomFloatValue(){
        return ThreadLocalRandom.current().nextFloat();
    }

    private Object getRandomDoubleValue(){
        double aux = ThreadLocalRandom.current().nextDouble();
        return aux;
    }

    private Object getRandomLongValue(){
        long aux = ThreadLocalRandom.current().nextLong();
        return aux;
    }

    private Object getRandomBooleanValue(){
        return ThreadLocalRandom.current().nextBoolean();
    }

    private Object getRandomStringValue(){
        return "aux";
    }


}
