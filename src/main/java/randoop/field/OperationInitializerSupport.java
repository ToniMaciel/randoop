package randoop.field;

import randoop.operation.TypedClassOperation;
import randoop.operation.TypedOperation;
import randoop.sequence.Sequence;
import randoop.sequence.Variable;
import randoop.types.ClassOrInterfaceType;
import randoop.types.JavaTypes;
import randoop.types.NonParameterizedType;
import randoop.types.Type;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * For each sequence, the class should look for objects created from any of the classes under analysis.
 * These objects must be created by constructor or deserialization calls.
 * For each call, the class must identify the associated type among the list of classes under analysis.
 * This type will be used to guide the creation of set operations.
 * That means, this type will be used in the search of fields and setter methods.
 * The following steps will done using the current implementation.
 * */
public class OperationInitializerSupport {
    private Sequence sequence;
    public Class<?> classUnderAnalysis;
    private List<Class<?>> classesUnderAnalysis;

    public OperationInitializerSupport(Sequence sequence, Set<ClassOrInterfaceType> classesUnderAnalysis){
        this.sequence = sequence;
        this.classesUnderAnalysis = findListOfClassesUnderAnalysis(classesUnderAnalysis);
        this.classUnderAnalysis = this.classesUnderAnalysis.get(this.classesUnderAnalysis.size()-1);
    }

    public Sequence getSequence(){
        return this.sequence;
    }

    private List<Class<?>> findListOfClassesUnderAnalysis(Set<ClassOrInterfaceType> classesUnderAnalysis){
        List<Class<?>> listOfClassesUnderAnalysis = new ArrayList<>();
        for(ClassOrInterfaceType className: classesUnderAnalysis){
            if (!className.getRuntimeClass().equals(Object.class)) {
                listOfClassesUnderAnalysis.add(className.getRuntimeClass());
            }
        }
        return listOfClassesUnderAnalysis;
    }


    public void setNewValuesForFieldsDeserializable() throws NoSuchMethodException, NoSuchFieldException {
        Class<?> currentClassUnderAnalysis = isSequenceValidForSetOperations();
        Field[] fields = currentClassUnderAnalysis.getFields();
        for (Field field : fields) {
            if (isPrimitiveType(field.getType())){
                if (checkForPreviousFieldSetSupportOperation(field.getName()) && !Modifier.isFinal(field.getModifiers()) && Modifier.isPublic(field.getModifiers()) && !Modifier.isStatic(field.getModifiers())) {
                    generateTypedOperationForFieldSerialization(field, currentClassUnderAnalysis);
                }
            }
        }
        Method[] methods = currentClassUnderAnalysis.getMethods();
        for (Method method : methods) {
            if (isPrimitiveType(method.getParameterTypes())) {
                if (checkForPreviousFieldSetSupportOperation(method.getName().replace("set", "")) && isSetter(method)) {
                    generateTypedOperationForSetMethodSerialization(method, currentClassUnderAnalysis);
                }
            }
        }
    }

    private boolean isPrimitiveType(Class<?>[] types){
        List<Class<?>> myTypes = new ArrayList<>();
        for (Type myType: JavaTypes.getPrimitiveTypes()){
            myTypes.add(myType.getRuntimeClass());
        }
        for(Class<?> type: types){
            if (myTypes.contains(type)){
                return true;
            }
        }
        return false;
    }


    private void generateTypedOperationForSetMethodSerialization(Method method, Class<?> classUnderAnalysis) throws NoSuchMethodException {
        TypedClassOperation setterForMethod = TypedOperation.forMethod(method);
        TypedOperation newValue = generateRandomValue(method.getParameterTypes()[0].getName());
        if(newValue != null) {
            this.sequence = sequence.extend(newValue);
            Variable setVariable = variableIndexForSequenceInstantiationBySerialization(classUnderAnalysis);
            if (setVariable != null) {
                this.sequence = sequence.extend(setterForMethod, setVariable, this.sequence.getLastVariable());
            }
        }
    }

    private boolean isSetter(Method method) {
        return Modifier.isPublic(method.getModifiers()) &&
                method.getReturnType().equals(void.class) &&
                method.getParameterTypes().length == 1 &&
                method.getName().matches("^set[A-Z].*") &&
                !Modifier.isStatic(method.getModifiers());
    }

    private Class<?> isSequenceValidForSetOperations(){
        Class<?> objectTypeVariable = findClassTypeAmongClassesUnderAnalysis(this.classesUnderAnalysis, this.sequence.getLastVariable().getType());

        if (objectTypeVariable != null){
            return objectTypeVariable;
        }
        return NullPointerException.class;
    }

    private Class<?> findClassTypeAmongClassesUnderAnalysis(List<Class<?>> classesUnderAnalysis, Type currentStatementType){
        for (Class<?> classUnderAnalysis: classesUnderAnalysis){
            if (classUnderAnalysis.getTypeName().equals(currentStatementType.toString())) return classUnderAnalysis;
        }
        return null;
    }

    private boolean isPrimitiveType(Class<?> type){
        List<Class<?>> myTypes = new ArrayList<>();
        for (Type myType: JavaTypes.getPrimitiveTypes()){
            myTypes.add(myType.getRuntimeClass());
        }
        if (myTypes.contains(type) || type.equals(String.class)){
            return true;
        }else{
            return false;
        }
    }

    private boolean checkForPreviousFieldSetSupportOperation(String targetSet){
        int i = 0;
        while (i < this.sequence.statements.size()){
            String aux = this.sequence.statements.get(i).getOperation().getSignatureString();
            if(aux.contains("set") && aux.toLowerCase().contains(targetSet.toLowerCase())){
                return false;
            }
            i++;
        }
        return true;
    }

    private void generateTypedOperationForFieldSerialization(Field field, Class<?> classUnderAnalysis) throws NoSuchMethodException, IllegalArgumentException {
        if (isPrimitiveType(field.getType())) {
            TypedClassOperation setterForField = TypedOperation.createSetterForField(field, new NonParameterizedType(classUnderAnalysis));
            TypedOperation newValue = generateRandomValue(field.getType().getTypeName());
            if (newValue != null) {
                this.sequence = this.sequence.extend(newValue);
                Variable setVariable = variableIndexForSequenceInstantiationBySerialization(classUnderAnalysis);
                if (setVariable != null) {
                    this.sequence = this.sequence.extend(setterForField, setVariable, this.sequence.getLastVariable());
                }
            }
        }
    }

    private Variable variableIndexForSequenceInstantiationBySerialization(Class<?> classUnderAnalysis) throws NoSuchMethodException {
        int index=-1, i = 0;
        while(i < this.sequence.statements.size()){
            Type myAux = this.sequence.getVariable(i).getType();
            Type aux = this.sequence.statements.get(i).getOperation().getOutputType();
            if (myAux.toString().equals(classUnderAnalysis.getTypeName())
                    && aux.toString().equals(classUnderAnalysis.getTypeName())){
                index = i;
                break;
            }
            i++;
        }
        if (index != -1) {
            return this.sequence.getVariable(index);
        }else{
            return null;
        }
    }

    private TypedOperation generateRandomValue(String type){
        if (getRelatedType(type) != null){
            RandomValue randomValue = new RandomValue();
            return TypedOperation.createPrimitiveInitialization(getRelatedType(type), randomValue.getRandomValue(type));
        }
        return null;
    }

    private Type getRelatedType(String type){
        List<Type> myTypes = new ArrayList<>();
        for (Type myType: JavaTypes.getPrimitiveTypes()){
            myTypes.add(myType);
        }
        myTypes.add(JavaTypes.STRING_TYPE);

        for (Type myType: myTypes){
            if (type.toString().equals(myType.toString())){
                return myType;
            }
        }
        return null;
    }
}
