package org.drools.modelcompiler.util.lambdareplace;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import org.junit.Before;
import org.junit.Test;

import static com.github.javaparser.StaticJavaParser.parseResource;
import static org.hamcrest.Matchers.equalToIgnoringWhiteSpace;
import static org.junit.Assert.assertThat;

public class ExecModelLambdaPostProcessorTest {

    @Before
    public void configJP() {
        StaticJavaParser.getConfiguration().setCharacterEncoding(Charset.defaultCharset());
    }

    @Test
    public void convertPatternLambda() throws Exception {

        CompilationUnit inputCU = parseResource("org/drools/modelcompiler/util/lambdareplace/PatternTestHarness.java");
        CompilationUnit clone = inputCU.clone();

        new ExecModelLambdaPostProcessor(new HashMap<>(), "mypackage", "rulename", new ArrayList<>(), new ArrayList<>(), clone).convertLambdas();

        String PATTERN_HARNESS = "PatternTestHarness";
        MethodDeclaration expectedResult = getMethodChangingName(inputCU, PATTERN_HARNESS, "expectedOutput");
        MethodDeclaration actual = getMethodChangingName(clone, PATTERN_HARNESS, "inputMethod");

        assertThat(actual.toString(), equalToIgnoringWhiteSpace(expectedResult.toString()));
    }

    @Test
    public void convertFlowLambdaDoNotConvertConsequenceWhenDrools() throws Exception {

        CompilationUnit inputCU = parseResource("org/drools/modelcompiler/util/lambdareplace/FlowConsequenceTestHarness.java");
        CompilationUnit clone = inputCU.clone();

        new ExecModelLambdaPostProcessor(new HashMap<>(), "mypackage", "rulename", new ArrayList<>(), new ArrayList<>(), clone).convertLambdas();

        String FLOW_HARNESS = "FlowTestHarness";
        MethodDeclaration expectedResultNotConverted = getMethodChangingName(inputCU, FLOW_HARNESS, "expectedOutputNotConverted");
        MethodDeclaration actualNotConverted = getMethodChangingName(clone, FLOW_HARNESS, "inputMethodNotConverted");

        assertThat(actualNotConverted.toString(), equalToIgnoringWhiteSpace(expectedResultNotConverted.toString()));

        MethodDeclaration expectedResultConverted = getMethodChangingName(inputCU, FLOW_HARNESS, "expectedOutputConverted");
        MethodDeclaration actualConverted = getMethodChangingName(clone, FLOW_HARNESS, "inputMethodConverted");

        assertThat(actualConverted.toString(), equalToIgnoringWhiteSpace(expectedResultConverted.toString()));
    }

    private MethodDeclaration getMethodChangingName(CompilationUnit inputCU, String className, String methodName) {
        return inputCU.getClassByName(className)
                .map(c -> c.getMethodsByName(methodName))
                .flatMap(methods -> methods.stream().findFirst())
                .map(m -> m.setName("testMethod"))
                .orElseThrow(RuntimeException::new);
    }
}