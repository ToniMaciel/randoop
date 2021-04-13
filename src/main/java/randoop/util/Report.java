package randoop.util;

import org.apache.commons.lang3.builder.EqualsBuilder;
import randoop.ExecutionOutcome;
import randoop.NormalExecution;
import randoop.operation.Operation;
import randoop.sequence.ExecutableSequence;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Report {

    public Report() {
        this.methodsCalled = new HashMap<>();
        this.objectsCreated = new HashMap<>();
        this.uniqueObjects = new ArrayList<>();
        this.uniqueObjectsCreated = new HashMap<>();
    }

    Map<Operation, Integer> methodsCalled;
    Map<Class<?>, Integer> objectsCreated;
    ArrayList<Object> uniqueObjects;
    Map<Class<?>, Integer> uniqueObjectsCreated;

    /**TODO Detalhar o funcionamento desse metodo
     *
     * @param rTests Sequencia de teste de regressao para fazer a revisao
     * 
     */
    public void generateReport(List<ExecutableSequence> rTests) {
        for (ExecutableSequence test : rTests) {
            for (int i = 0; i < test.sequence.size(); i++) {
                Operation operation = test.sequence.getStatement(i).getOperation().getOperation();
                if(!operation.isNonreceivingValue()){
                    methodsCalled.put(operation, methodsCalled.getOrDefault(operation, 0) + 1);
                    Object outcome = executionValue(test, i);
                    if(outcome != null){
                        objectsCreated.put(outcome.getClass(), objectsCreated.getOrDefault(outcome.getClass(), 0) + 1);
                        if (IsUniqueObject(outcome)){
                            uniqueObjects.add(outcome);
                            uniqueObjectsCreated.put(outcome.getClass(), uniqueObjectsCreated.getOrDefault(outcome.getClass(), 0) + 1);
                        }
                    }
                }
            }
        }
        generateCSV();
    }

    private boolean IsUniqueObject(Object outcome)  {
        for (Object uniqueObject : uniqueObjects){
            if (outcome.getClass().equals(uniqueObject.getClass())) {
                if(EqualsBuilder.reflectionEquals(outcome, uniqueObject, true))
                    return false;
            }
        }
    
        return true;
    }

    private Object executionValue(ExecutableSequence test, int i) {
        ExecutionOutcome result = test.getResult(i);
        if (result instanceof NormalExecution ) {
            return ((NormalExecution) result).getRuntimeValue();
        }
        return null;
    }

    @SuppressWarnings({"DefaultCharset", "CatchAndPrintStackTrace"})
    private void generateCSV(){
        try {
            PrintWriter writer = new PrintWriter(new File("methods_report.csv(ModifiedRandoop)"));

            StringBuilder sb = new StringBuilder();
            sb.append("Methods called");
            sb.append(',');
            sb.append("Number of times");
            sb.append('\n');

            for (Map.Entry<Operation, Integer> entry : methodsCalled.entrySet()) {
                Operation operation = entry.getKey();
                Integer value = entry.getValue();
                sb.append("\"" + operation.toString() + "\"");
                sb.append(',');
                sb.append(value.toString());
                sb.append('\n');
            }

            writer.write(sb.toString());

            writer.close();
        } catch (FileNotFoundException e){
            System.out.println(e.getMessage());
        }

        try {
            PrintWriter writer = new PrintWriter(new File("objects_report.csv(ModifiedRandoop)"));

            StringBuilder sb = new StringBuilder();
            sb.append("Classes of objects created");
            sb.append(',');
            sb.append("Number of objects created");
            sb.append(',');
            sb.append("Number of unique objects manipulated");
            sb.append('\n');

            for (Map.Entry<Class<?>, Integer> entry : objectsCreated.entrySet()) {
                Class<?> objectClass = entry.getKey();
                Integer value = entry.getValue();
                Integer uniqueValue = uniqueObjectsCreated.get(objectClass);
                sb.append(objectClass.toString());
                sb.append(',');
                sb.append(value.toString());
                sb.append(',');
                sb.append(uniqueValue.toString());
                sb.append('\n');
            }

            writer.write(sb.toString());

            writer.close();
        } catch (FileNotFoundException e){
            System.out.println(e.getMessage());
        }
    }
}
