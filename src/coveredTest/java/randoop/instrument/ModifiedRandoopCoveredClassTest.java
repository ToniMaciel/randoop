package randoop.instrument;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static randoop.reflection.VisibilityPredicate.IS_PUBLIC;

import java.nio.file.Paths;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import org.checkerframework.checker.signature.qual.ClassGetName;
import org.junit.Test;
import randoop.generation.ComponentManager;
import randoop.generation.ForwardGenerator;
import randoop.generation.RandoopListenerManager;
import randoop.generation.SeedSequences;
import randoop.generation.TestUtils;
import randoop.main.ClassNameErrorHandler;
import randoop.main.GenInputsAbstract;
import randoop.main.GenTests;
import randoop.main.ThrowClassNameError;
import randoop.operation.TypedOperation;
import randoop.reflection.DefaultReflectionPredicate;
import randoop.reflection.OperationModel;
import randoop.reflection.ReflectionPredicate;
import randoop.reflection.SignatureParseException;
import randoop.reflection.VisibilityPredicate;
import randoop.sequence.ExecutableSequence;
import randoop.sequence.Sequence;
import randoop.test.ContractSet;
import randoop.test.TestCheckGenerator;
import randoop.util.MultiMap;
import randoop.util.ReflectionExecutor;

/**
 * Test special cases of covered class filtering. Want to ensure behaves well when given abstract
 * class and interface.
 */
public class ModifiedRandoopCoveredClassTest {

  @Test
  public void fullMethodList()
      throws ClassNotFoundException, NoSuchMethodException, SignatureParseException {
    GenInputsAbstract.silently_ignore_bad_class_names = false;
    GenInputsAbstract.classlist = Paths.get("instrument/testcase/special-modified-randoop-allclasses.txt");
    GenInputsAbstract.require_covered_classes =
        Paths.get("instrument/testcase/special-coveredclasses.txt");
    GenInputsAbstract.methodlist = Paths.get("instrument/testcase/special-method-list.txt");
    ReflectionExecutor.usethreads = false;
    GenInputsAbstract.generated_limit = 10000;
    GenInputsAbstract.output_limit = 5000;
    GenInputsAbstract.time_limit = 30;
    randoop.util.Randomness.setSeed(0);

    VisibilityPredicate visibility = IS_PUBLIC;
    Set<@ClassGetName String> classnames = GenInputsAbstract.getClassnamesFromArgs(visibility);
    Set<@ClassGetName String> coveredClassnames =
        GenInputsAbstract.getClassNamesFromFile(GenInputsAbstract.require_covered_classes);
    Set<String> omitFields = new HashSet<>();
    ReflectionPredicate reflectionPredicate = new DefaultReflectionPredicate(omitFields);

    ClassNameErrorHandler classNameErrorHandler = new ThrowClassNameError();

    OperationModel operationModel =
        OperationModel.createModel(
            visibility,
            reflectionPredicate,
            GenInputsAbstract.omit_methods,
            classnames,
            coveredClassnames,
            classNameErrorHandler,
            GenInputsAbstract.literals_file);

    Set<Class<?>> coveredClassesGoal = operationModel.getCoveredClassesGoal();
    assertEquals(4, operationModel.getClassTypes().size());

    List<TypedOperation> model = operationModel.getOperations();

    Set<Sequence> components = new LinkedHashSet<>();
    components.addAll(SeedSequences.defaultSeeds());
    components.addAll(operationModel.getAnnotatedTestValues());

    ComponentManager componentMgr = new ComponentManager(components);
    operationModel.addClassLiterals(
        componentMgr, GenInputsAbstract.literals_file, GenInputsAbstract.literals_level);

    RandoopListenerManager listenerMgr = new RandoopListenerManager();
    Set<TypedOperation> sideEffectFreeMethods = new LinkedHashSet<>();
    ForwardGenerator testGenerator =
        new ForwardGenerator(
            model,
            sideEffectFreeMethods,
            new GenInputsAbstract.Limits(),
            componentMgr,
            listenerMgr,
            operationModel.getClassTypes());
    GenTests genTests = new GenTests();

    assertEquals(4, testGenerator.getClassesUnderTest().size());
    assertEquals(1, testGenerator.getTypedOperationSelectorBasedOnTargetInputClasses().
        getMandatoryTypedOperations().size());
    assertEquals(2, testGenerator.getTypedOperationSelectorBasedOnTargetInputClasses().
        getClassesUnderTestOperations().size());
    assertEquals(2, testGenerator.getTypedOperationSelectorBasedOnTargetInputClasses().
        getClassesUnderTest().size());

    TypedOperation objectConstructor = TypedOperation.forConstructor(Object.class.getConstructor());

    Set<Sequence> excludeSet = new LinkedHashSet<>();
    excludeSet.add(new Sequence().extend(objectConstructor));

    Predicate<ExecutableSequence> isOutputTest =
        genTests.createTestOutputPredicate(
            excludeSet,
            operationModel.getCoveredClassesGoal(),
            GenInputsAbstract.require_classname_in_test);
    testGenerator.setTestPredicate(isOutputTest);
    ContractSet contracts = operationModel.getContracts();
    TestCheckGenerator checkGenerator =
        GenTests.createTestCheckGenerator(
            visibility, contracts, new MultiMap<>(), operationModel.getOmitMethodsPredicate());
    testGenerator.setTestCheckGenerator(checkGenerator);
    testGenerator.setExecutionVisitor(new CoveredClassVisitor(coveredClassesGoal));
    TestUtils.setAllLogs(testGenerator);
    testGenerator.createAndClassifySequences();

  }

  @Test
  public void partialMethodList()
      throws ClassNotFoundException, NoSuchMethodException, SignatureParseException {
    GenInputsAbstract.silently_ignore_bad_class_names = false;
    GenInputsAbstract.classlist = Paths.get("instrument/testcase/special-modified-randoop-oneclass.txt");
    GenInputsAbstract.require_covered_classes =
        Paths.get("instrument/testcase/special-coveredclasses.txt");
    GenInputsAbstract.methodlist = Paths.get("instrument/testcase/special-method-list.txt");
    ReflectionExecutor.usethreads = false;
    GenInputsAbstract.generated_limit = 10000;
    GenInputsAbstract.output_limit = 5000;
    GenInputsAbstract.time_limit = 30;
    randoop.util.Randomness.setSeed(0);

    VisibilityPredicate visibility = IS_PUBLIC;
    Set<@ClassGetName String> classnames = GenInputsAbstract.getClassnamesFromArgs(visibility);
    Set<@ClassGetName String> coveredClassnames =
        GenInputsAbstract.getClassNamesFromFile(GenInputsAbstract.require_covered_classes);
    Set<String> omitFields = new HashSet<>();
    ReflectionPredicate reflectionPredicate = new DefaultReflectionPredicate(omitFields);

    ClassNameErrorHandler classNameErrorHandler = new ThrowClassNameError();

    OperationModel operationModel =
        OperationModel.createModel(
            visibility,
            reflectionPredicate,
            GenInputsAbstract.omit_methods,
            classnames,
            coveredClassnames,
            classNameErrorHandler,
            GenInputsAbstract.literals_file);

    Set<Class<?>> coveredClassesGoal = operationModel.getCoveredClassesGoal();
    assertEquals(5, operationModel.getClassTypes().size());

    List<TypedOperation> model = operationModel.getOperations();

    Set<Sequence> components = new LinkedHashSet<>();
    components.addAll(SeedSequences.defaultSeeds());
    components.addAll(operationModel.getAnnotatedTestValues());

    ComponentManager componentMgr = new ComponentManager(components);
    operationModel.addClassLiterals(
        componentMgr, GenInputsAbstract.literals_file, GenInputsAbstract.literals_level);

    RandoopListenerManager listenerMgr = new RandoopListenerManager();
    Set<TypedOperation> sideEffectFreeMethods = new LinkedHashSet<>();
    ForwardGenerator testGenerator =
        new ForwardGenerator(
            model,
            sideEffectFreeMethods,
            new GenInputsAbstract.Limits(),
            componentMgr,
            listenerMgr,
            operationModel.getClassTypes());
    GenTests genTests = new GenTests();

    assertEquals(5, testGenerator.getClassesUnderTest().size());
    assertEquals(1, testGenerator.getTypedOperationSelectorBasedOnTargetInputClasses().
        getMandatoryTypedOperations().size());
    assertEquals(4, testGenerator.getTypedOperationSelectorBasedOnTargetInputClasses().
        getClassesUnderTestOperations().size());
    assertEquals(4, testGenerator.getTypedOperationSelectorBasedOnTargetInputClasses().
        getClassesUnderTest().size());

    TypedOperation objectConstructor = TypedOperation.forConstructor(Object.class.getConstructor());

    Set<Sequence> excludeSet = new LinkedHashSet<>();
    excludeSet.add(new Sequence().extend(objectConstructor));

    Predicate<ExecutableSequence> isOutputTest =
        genTests.createTestOutputPredicate(
            excludeSet,
            operationModel.getCoveredClassesGoal(),
            GenInputsAbstract.require_classname_in_test);
    testGenerator.setTestPredicate(isOutputTest);
    ContractSet contracts = operationModel.getContracts();
    TestCheckGenerator checkGenerator =
        GenTests.createTestCheckGenerator(
            visibility, contracts, new MultiMap<>(), operationModel.getOmitMethodsPredicate());
    testGenerator.setTestCheckGenerator(checkGenerator);
    testGenerator.setExecutionVisitor(new CoveredClassVisitor(coveredClassesGoal));
    TestUtils.setAllLogs(testGenerator);
    testGenerator.createAndClassifySequences();

  }
}
