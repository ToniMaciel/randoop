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

    /**The operations and the number of times that they are called in tests */
    public Map<Operation, Integer> methodsCalled;

    /**The classes of objects created in tests and their number of times */
    public Map<Class<?>, Integer> objectsCreated;

    /**A list of distinct objects at the end of each test */
    public ArrayList<Object> uniqueObjects;

    /**The classes of distinct objects at the end of each test and their number of times */
    public Map<Class<?>, Integer> uniqueObjectsCreated;

    /**
     * Iterate over all ExecutableSequence in the given list, in each of this ExecutableSequence, will iterate over their sequences. 
     * Then, will get the operation of the sequence and if it doesn't an non-receiving type, increments the number of times that this 
     * operations is called along the tests. For the last, will analyses the runtimeValue of the execution of this sequence, and then, 
     * if does't null, the number of times that objects of the class of the  sequence's outcome are created will be incremented and it 
     * will be verified if there is an object with the same values of the oucome in previous tests execution's and, depending on the result,
     * the analysis of single objects will also be increased.
     * 
     * Observation: This method is called in gentest's class. For now, it only generates reports of regression tests. 
     *  
     * @param rTests List of ExecutableSequences of a test
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

    /**
     * Will iterate over the list of unique objects created and check with the EqualsBuilder 
     * if there is an object that is equal to outcome.
     * 
     * @param outcome The runtimeValue of a statement output 
     * @return true, if there isn't a object equals outcome created in previous statements, otherwise, it is false.
     */

    private boolean IsUniqueObject(Object outcome)  {
        for (Object uniqueObject : uniqueObjects){
            if (outcome.getClass().equals(uniqueObject.getClass())) {
                if(EqualsBuilder.reflectionEquals(outcome, uniqueObject, true))
                    return false;
            }
        }
    
        return true;
    }

    /**
     * It gets the result of executing the index-th element of the sequence, if this execution is a normal execution. 
     *
     * @param test The ExecutableSequence under analysis
     * @param i The index of the sequence under analysis
     * @return The runtimeTime value of the execution of the sequence that are being analysed (which can be null), or null if this execution isn't a normal execution
     */

    private Object executionValue(ExecutableSequence test, int i) {
        ExecutionOutcome result = test.getResult(i);
        if (result instanceof NormalExecution ) {
            return ((NormalExecution) result).getRuntimeValue();
        }
        return null;
    }

     /** Generates two csv files: One with the report for methods called and other with the report for objects created in the tests.
     * 
     * Observation: Those csvs will be in the directory where the randoop was executaded.
     */
    @SuppressWarnings({"DefaultCharset", "CatchAndPrintStackTrace"})
    private void generateCSV(){
        try {
            PrintWriter writer = new PrintWriter(new File("methods_report.csv"));

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
            PrintWriter writer = new PrintWriter(new File("objects_report.csv"));

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
