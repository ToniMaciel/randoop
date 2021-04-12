package randoop.generation;

import randoop.operation.TypedOperation;
import randoop.types.ClassOrInterfaceType;
import randoop.types.Type;

import java.util.*;

public class TypedOperationSelectorBasedOnTargetInputClasses {
    private LinkedHashMap<Class<?>, List<TypedOperation>> classesUnderTestOperations;
    private List<TypedOperation> mandatoryTypedOperations;
    private List<Class<?>> classesUnderTest;
    private int lastSelectedClassUndertestIndex;

    public TypedOperationSelectorBasedOnTargetInputClasses(Set<ClassOrInterfaceType> classesUnderTest, Set<TypedOperation> mandatoryMethodList,
           List<TypedOperation> operations){
        this.classesUnderTestOperations = new LinkedHashMap<>();
        this.classesUnderTest = new ArrayList<>();
        this.mandatoryTypedOperations = new ArrayList<>();
        this.mandatoryTypedOperations.addAll(mandatoryMethodList);
        this.lastSelectedClassUndertestIndex = 0;
        findObjectCreationOperatiosForTargetClasses(initializeList(classesUnderTest), operations);

        Set<Class<?>> keys = this.classesUnderTestOperations.keySet();
        for(Class<?> myClass: keys){
            System.out.println(myClass);
            System.out.println(this.classesUnderTestOperations.get(myClass).size());
        }
    }

    private ArrayList<Class<?>> initializeList(Set<ClassOrInterfaceType> classesUnderTest){
        ArrayList<Class<?>> aux = new ArrayList<>();
        for(ClassOrInterfaceType classUnderTest: classesUnderTest){
            aux.add(classUnderTest.getRuntimeClass());
        }
        return aux;
    }

    public TypedOperation selectTypedOperation(TypedOperationSelector operationSelector, int numSteps){
        if (numSteps%(10*this.classesUnderTestOperations.size()) < 2*this.classesUnderTestOperations.size()){ // &&
            return getTypedOperationOfClassUnderAnalysis(operationSelector);
        }else {
             return operationSelector.selectOperation();
        }
    }

    private void findObjectCreationOperatiosForTargetClasses(ArrayList<Class<?>> classesUnderTest, List<TypedOperation> operations){
        for (TypedOperation typedOperation : operations) {
            Class<?> aux = typedOperation.getOutputType().getRuntimeClass();
            if(classesUnderTest.contains(aux)) {
                getTypedOperationOfClass(typedOperation, aux);
            }
        }
    }

    private void getTypedOperationOfClass(TypedOperation typedOperation, Class<?> targetClass){
        if (typedOperation.getOutputType().getRuntimeClass().equals(targetClass) &&
            (typedOperation.isConstructorCall() || typedOperation.getName().contains("deserialize"))){ //&& !this.classesUnderTestOperations
            if (!this.classesUnderTest.contains(targetClass)){
                this.classesUnderTest.add(targetClass);
            }
            if (!this.classesUnderTestOperations.keySet().contains(targetClass)){
                List<TypedOperation> aux = new ArrayList<>();
                aux.add(typedOperation);
                this.classesUnderTestOperations.put(targetClass, aux);
            }else if(!this.classesUnderTestOperations.get(targetClass).contains(typedOperation)){
                this.classesUnderTestOperations.get(targetClass).add(typedOperation);
            }
        }
    }

    public TypedOperation selectTargetMethodOperation(TypedOperationSelector operationSelector){
        if (this.mandatoryTypedOperations.size() > 0)
            return this.mandatoryTypedOperations.get(new Random().nextInt(this.mandatoryTypedOperations.size()));
        else{
            return operationSelector.selectOperation();
        }
    }

    private TypedOperation getTypedOperationOfClassUnderAnalysis(TypedOperationSelector operationSelector){
        if (this.classesUnderTestOperations.size() > 0){
            if (lastSelectedClassUndertestIndex == this.classesUnderTestOperations.size()) lastSelectedClassUndertestIndex = 0;
            List<TypedOperation> operationsOfTargetClass = this.classesUnderTestOperations.get(this.classesUnderTest.get(lastSelectedClassUndertestIndex));
            this.lastSelectedClassUndertestIndex++;
            TypedOperation typedOperation = operationsOfTargetClass.get(new Random().nextInt(operationsOfTargetClass.size()));
            return typedOperation;
        }else{
            return operationSelector.selectOperation();
        }
    }

    public LinkedHashMap<Class<?>, List<TypedOperation>> getClassesUnderTestOperations() {
        return classesUnderTestOperations;
    }

    public List<TypedOperation> getMandatoryTypedOperations() {
        return mandatoryTypedOperations;
    }

    public List<Class<?>> getClassesUnderTest() {
        return classesUnderTest;
    }
}
