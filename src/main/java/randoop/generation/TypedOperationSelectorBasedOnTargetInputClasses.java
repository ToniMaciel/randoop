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

    public TypedOperationSelectorBasedOnTargetInputClasses(Set<TypedOperation> mandatoryMethodList,
           List<TypedOperation> operations){
        this.classesUnderTestOperations = new LinkedHashMap<>();
        this.classesUnderTest = new ArrayList<>();
        this.mandatoryTypedOperations = new ArrayList<>();
        this.mandatoryTypedOperations.addAll(mandatoryMethodList);
        this.lastSelectedClassUndertestIndex = 0;
        findObjectCreationOperatiosForTargetClasses(operations);
    }

    public TypedOperation selectTypedOperation(TypedOperationSelector operationSelector, int numSteps){
        //1)Quero que todas as target classes e suas dependências, tenham ao menos 2 chamadas a métodos que
        //são de seus respectivos tipos
        //2) Nesse sentido, estas chamadas de métodos aconteceriam em 20%
        //Enquanto as demais, corresponderiam a 80% (aleatório)
        if (numSteps%(10*this.classesUnderTestOperations.size()) < 2*this.classesUnderTestOperations.size()){ // &&
            return getTypedOperationOfClassUnderAnalysis(operationSelector);
        }else {
             return operationSelector.selectOperation();
        }
    }

    private void findObjectCreationOperatiosForTargetClasses(List<TypedOperation> operations){
        for(TypedOperation typedOperation: this.mandatoryTypedOperations){
            for(Type type: typedOperation.getInputTypes()){
                    getTypedOperationOfClass(operations, type.getRuntimeClass());
            }
        }
    }

    private void getTypedOperationOfClass(List<TypedOperation> operations, Class<?> targetClass){
        for(TypedOperation typedOperation: operations){
            if (typedOperation.getOutputType().getRuntimeClass().equals(targetClass) && (typedOperation.isConstructorCall() ||
                (typedOperation.isStatic() && typedOperation.getName().contains("deserialize"))) && !this.classesUnderTestOperations
                .keySet().contains(typedOperation.getClass())){
                System.out.println("Object creation Operation for "+targetClass+": "+typedOperation.toString());
                this.classesUnderTest.add(targetClass);
                if (!this.classesUnderTestOperations.keySet().contains(targetClass)){
                    List<TypedOperation> aux = new ArrayList<>();
                    aux.add(typedOperation);
                    this.classesUnderTestOperations.put(targetClass, aux);
                }else{
                    this.classesUnderTestOperations.get(targetClass).add(typedOperation);
                }
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

}
