package randoop.util;

import randoop.ExecutionOutcome;
import randoop.NormalExecution;
import randoop.operation.Operation;
import randoop.sequence.ExecutableSequence;

import java.io.*;
import java.lang.reflect.Field;
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

    /**TODO Detalhar o funcionamento desse método
     *
     * @param rTests Sequencia de teste de regressão para fazer a revisao
     * @throws IllegalAccessException Um lancado pelo metodo de verificacao de fields, com a dependencia externa creio que
     * nao lancara mais essa excecao
     */

    public void generateReport(List<ExecutableSequence> rTests) throws IllegalAccessException {
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

    private boolean IsUniqueObject(Object outcome) throws IllegalAccessException {
        for (Object uniqueObject : uniqueObjects) {
            if(outcome.getClass().equals(uniqueObject.getClass())){
                if (outcome == uniqueObject)
                    return false;
                else if (outcome.equals(uniqueObject))
                    return false;
                else{
                    Class<?> classObject = outcome.getClass();
                    boolean equal = true;

                    do {
                        Field[] fields = classObject.getDeclaredFields();

                        for (Field field : fields) {
                            field.setAccessible(true);

                            Object fieldResultOutcome = field.get(outcome);
                            Object fieldResultObject = field.get(uniqueObject);

                            if (fieldResultOutcome == null) {
                                if (fieldResultObject != null) {
                                    equal = false;
                                    break;
                                }
                            } else if (fieldResultObject == null) {
                                equal = false;
                                break;
                            } else if (!fieldResultOutcome.equals(fieldResultObject)) {
                                equal = false;
                                break;
                            }
                        }

                        if (equal)
                            classObject = classObject.getSuperclass();
                        else
                            break;

                    } while (classObject.getSuperclass() != null);

                    if(equal)
                        return false;
                }
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
            PrintWriter writer = new PrintWriter(new File("methods_report.csv"));

            StringBuilder sb = new StringBuilder();
            sb.append("Metodos chamados");
            sb.append(',');
            sb.append("Quantidade de vezes");
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
            sb.append("Classes dos objetos criados");
            sb.append(',');
            sb.append("Quantidade de objetos criados");
            sb.append(',');
            sb.append("Quantidade de objetos únicos criados");
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
