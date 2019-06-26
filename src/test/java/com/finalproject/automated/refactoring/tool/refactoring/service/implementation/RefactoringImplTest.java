package com.finalproject.automated.refactoring.tool.refactoring.service.implementation;

import com.finalproject.automated.refactoring.tool.extract.method.refactoring.service.ExtractMethod;
import com.finalproject.automated.refactoring.tool.model.CodeSmellName;
import com.finalproject.automated.refactoring.tool.model.MethodModel;
import com.finalproject.automated.refactoring.tool.refactoring.service.Refactoring;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * @author Faza Zulfika P P
 * @version 1.0.0
 * @since 22 June 2019
 */

@SpringBootTest
@RunWith(SpringRunner.class)
public class RefactoringImplTest {

    @Autowired
    private Refactoring refactoring;

    @MockBean
    private ExtractMethod extractMethod;

    private static final String PATH = "filePath";
    private static final String FALSE_PATH = "falseFilePath";
    private static final String METHOD_NAME = "getFigureDrawBounds";

    private static final Integer COPIES = 2;

    private MethodModel methodModel;

    @Before
    public void setUp() {
        methodModel = MethodModel.builder()
                .name(METHOD_NAME)
                .codeSmells(Collections.singletonList(CodeSmellName.LONG_METHOD))
                .build();

        when(extractMethod.refactoring(eq(PATH), eq(methodModel)))
                .thenReturn(Boolean.TRUE);
        when(extractMethod.refactoring(eq(FALSE_PATH), eq(methodModel)))
                .thenReturn(Boolean.FALSE);
    }

    @Test
    public void refactoring_success() {
        Map<String, Map<String, List<MethodModel>>> refactoringResult = refactoring.refactoring(
                PATH, Collections.singletonList(methodModel));

        assertEquals(Collections.<String, Map<String, List<MethodModel>>>emptyMap(), refactoringResult);
        verify(extractMethod).refactoring(eq(PATH), eq(methodModel));
        verifyNoMoreInteractions(extractMethod);
    }

    @Test
    public void refactoring_success_methodModelsIsEmpty() {
        Map<String, Map<String, List<MethodModel>>> refactoringResult = refactoring.refactoring(
                PATH, Collections.emptyList());

        assertEquals(Collections.<String, Map<String, List<MethodModel>>>emptyMap(), refactoringResult);
        verifyNoMoreInteractions(extractMethod);
    }

    @Test
    public void refactoring_failed_fileFalse() {
        Map<String, Map<String, List<MethodModel>>> refactoringResult = refactoring.refactoring(
                FALSE_PATH, Collections.singletonList(methodModel));

        assertEquals(createExpectedFalseResult(), refactoringResult);
        verify(extractMethod).refactoring(eq(FALSE_PATH), eq(methodModel));
        verifyNoMoreInteractions(extractMethod);
    }

    @Test
    public void refactoring_failed_multiFileFalse() {
        Map<String, Map<String, List<MethodModel>>> refactoringResult = refactoring.refactoring(
                FALSE_PATH, Collections.nCopies(COPIES, methodModel));

        assertEquals(createExpectedMultiFalseResult(), refactoringResult);
        verify(extractMethod, times(COPIES)).refactoring(eq(FALSE_PATH), eq(methodModel));
        verifyNoMoreInteractions(extractMethod);
    }

    @Test(expected = NullPointerException.class)
    public void refactoring_failed_pathIsNull() {
        refactoring.refactoring(null, Collections.singletonList(methodModel));
    }

    @Test(expected = NullPointerException.class)
    public void refactoring_failed_methodModelsIsNull() {
        refactoring.refactoring(PATH, null);
    }

    private Map<String, Map<String, List<MethodModel>>> createExpectedFalseResult() {
        List<MethodModel> methodModels = Collections.singletonList(methodModel);
        Map<String, List<MethodModel>> methodSmells = Collections.singletonMap(FALSE_PATH, methodModels);

        return Collections.singletonMap(CodeSmellName.LONG_METHOD.getName(), methodSmells);
    }

    private Map<String, Map<String, List<MethodModel>>> createExpectedMultiFalseResult() {
        List<MethodModel> methodModels = Collections.nCopies(COPIES, methodModel);
        Map<String, List<MethodModel>> methodSmells = Collections.singletonMap(FALSE_PATH, methodModels);

        return Collections.singletonMap(CodeSmellName.LONG_METHOD.getName(), methodSmells);
    }
}