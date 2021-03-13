package randoop.generation;

import randoop.operation.TypedOperation;
import randoop.sequence.Sequence;
import randoop.types.ClassOrInterfaceType;
import randoop.types.Type;

import java.util.*;

public class TypedOperationSelectorBasedOnTargetInputClasses {
    private List<TypedOperation> classesUnderTest;
    private List<TypedOperation> mandatoryTypedOperations;
    private int lastSelectedClassUndertestIndex;

    public TypedOperationSelectorBasedOnTargetInputClasses(Set<ClassOrInterfaceType> classesUnderTest, Set<TypedOperation> mandatoryMethodList,
           List<TypedOperation> operations){
        this.classesUnderTest = new ArrayList<>();
        this.mandatoryTypedOperations = new ArrayList<>();
        this.mandatoryTypedOperations.addAll(mandatoryMethodList);
        this.lastSelectedClassUndertestIndex = 0;
        findTargetClassesBasedOnMandatoryMethods(operations);
    }

    public TypedOperation selectTypedOperation(TypedOperationSelector operationSelector, int numSteps){
        if (numSteps%(10*this.classesUnderTest.size()) < this.classesUnderTest.size()){ // &&
            return getTypedOperationOfClassUnderAnalysis(operationSelector);
        }else {
             return operationSelector.selectOperation();
        }
    }

    public void findTargetClassesBasedOnMandatoryMethods(List<TypedOperation> operations){
        for(TypedOperation typedOperation: this.mandatoryTypedOperations){
            for(Type type: typedOperation.getInputTypes()){
                System.out.println(type.getRuntimeClass());
                getTypedOperationOfClass(operations, type.getRuntimeClass());
            }
        }
    }

    public void findTargetClassesBasedOnMandatoryMethodsTwo(Set<ClassOrInterfaceType> classesUnderTest, List<TypedOperation> operations){
        for(ClassOrInterfaceType classOrInterfaceType: classesUnderTest){
            getTypedOperationOfClassTwo(operations, classOrInterfaceType);
        }
    }

    public void getTypedOperationOfClass(List<TypedOperation> operations, Class<?> targetClass){
        System.out.println("\n\n**** Useful Method Calls ***\n");
        for(TypedOperation typedOperation: operations){
            if ((typedOperation.isConstructorCall() || typedOperation.isStatic()) && typedOperation.getOutputType().getRuntimeClass().equals(targetClass)
                    && !this.classesUnderTest.contains(typedOperation)){
                System.out.println(typedOperation.toString());
                this.classesUnderTest.add(typedOperation);
            }
        }
    }

    public void getTypedOperationOfClassTwo(List<TypedOperation> operations, ClassOrInterfaceType targetClass){
        for(TypedOperation typedOperation: operations){
            //|| typedOperation.isStatic()
            if (typedOperation.isConstructorCall() && typedOperation.getOutputType().getRuntimeClass().toString().equals(targetClass.toString())
                    && !this.classesUnderTest.contains(typedOperation)){
                this.classesUnderTest.add(typedOperation);
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

    public void initializeListOfTargetClasses(List<TypedOperation> operations, Set<ClassOrInterfaceType> targetClasses){
        ArrayList<ClassOrInterfaceType> listTargetClasses = new ArrayList<>();
        listTargetClasses.addAll(targetClasses);
        for (ClassOrInterfaceType classOrInterfaceType: listTargetClasses) {
            for (TypedOperation typedOperation : operations) {
                if (isConstrucotOperationOfClassUnderTest(classOrInterfaceType, typedOperation)){
                    this.classesUnderTest.add(typedOperation);
                }
            }
        }
    }

    public TypedOperation getTypedOperationOfClassUnderAnalysis(TypedOperationSelector operationSelector){
        if (this.classesUnderTest.size() > 0){
            //if (lastSelectedClassUndertestIndex == this.classesUnderTest.size()-1) lastSelectedClassUndertestIndex = 0;
            if (lastSelectedClassUndertestIndex == this.classesUnderTest.size()) lastSelectedClassUndertestIndex = 0;
            return this.classesUnderTest.get(lastSelectedClassUndertestIndex++);
        }else{
            return operationSelector.selectOperation();
        }
    }

    private boolean isConstrucotOperationOfClassUnderTest(ClassOrInterfaceType classUnderTest, TypedOperation typedOperation){
        if((typedOperation.isConstructorCall() || typedOperation.isStatic()) && isTypedOperationOutputTypeOfClassUnderTest(classUnderTest, typedOperation.getOutputType())){
            return true;
        }
        return false;
    }

    private boolean isTypedOperationOutputTypeOfClassUnderTest(ClassOrInterfaceType classUnderTest, Type outputType){
        return classUnderTest.equals(outputType);
    }

}
