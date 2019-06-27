package com.finalproject.automated.refactoring.tool.refactoring.service.implementation;

import com.finalproject.automated.refactoring.tool.extract.method.refactoring.service.ExtractMethod;
import com.finalproject.automated.refactoring.tool.model.CodeSmellName;
import com.finalproject.automated.refactoring.tool.model.MethodModel;
import com.finalproject.automated.refactoring.tool.refactoring.model.SaveFailsVA;
import com.finalproject.automated.refactoring.tool.refactoring.service.Refactoring;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Faza Zulfika P P
 * @version 1.0.0
 * @since 22 June 2019
 */

@Service
public class RefactoringImpl implements Refactoring {

    @Autowired
    private ExtractMethod extractMethod;

    @Override
    public Map<String, Map<String, List<MethodModel>>> refactoring(@NonNull String path,
                                                                   @NonNull List<MethodModel> methodModels) {
        Map<String, Map<String, List<MethodModel>>> refactoringFailures = new ConcurrentHashMap<>();
        methodModels.parallelStream()
                .forEach(methodModel -> doRefactoring(path, methodModel, refactoringFailures));

        return refactoringFailures;
    }

    @Override
    public Map<String, Map<String, List<MethodModel>>> refactoring(@NonNull Map<String, List<MethodModel>> methods) {
        return methods.entrySet()
                .parallelStream()
                .map(methodEntry -> refactoring(methodEntry.getKey(), methodEntry.getValue()))
                .map(Map::entrySet)
                .flatMap(Collection::stream)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, this::mergeResultPath));
    }

    private Map<String, List<MethodModel>> mergeResultPath(Map<String, List<MethodModel>> result1,
                                                           Map<String, List<MethodModel>> result2) {
        return Stream.concat(result1.entrySet().stream(), result2.entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private void doRefactoring(String path, MethodModel methodModel,
                               Map<String, Map<String, List<MethodModel>>> refactoringFailures) {
        doExtractMethod(path, methodModel, refactoringFailures);
    }

    private void doExtractMethod(String path, MethodModel methodModel,
                                 Map<String, Map<String, List<MethodModel>>> refactoringFailures) {
        if (methodModel.getCodeSmells().contains(CodeSmellName.LONG_METHOD)) {
            Boolean isExtractMethodSuccess = extractMethod.refactoring(path, methodModel);

            if (!isExtractMethodSuccess) {
                SaveFailsVA saveFailsVA = SaveFailsVA.builder()
                        .path(path)
                        .codeSmellName(CodeSmellName.LONG_METHOD.getName())
                        .methodModel(methodModel)
                        .refactoringFailures(refactoringFailures)
                        .build();

                saveRefactoringFailures(saveFailsVA);
            } else {
                methodModel.getCodeSmells()
                        .remove(CodeSmellName.LONG_METHOD);
            }
        }
    }

    private void saveRefactoringFailures(SaveFailsVA saveFailsVA) {
        boolean containsKey = saveFailsVA.getRefactoringFailures()
                .containsKey(saveFailsVA.getCodeSmellName());

        if (containsKey) {
            saveToPathFailures(saveFailsVA);
        } else {
            saveNewCodeSmellFailures(saveFailsVA);
        }
    }

    private void saveToPathFailures(SaveFailsVA saveFailsVA) {
        Map<String, List<MethodModel>> pathFailures = saveFailsVA.getRefactoringFailures().
                get(saveFailsVA.getCodeSmellName());
        boolean containsKey = pathFailures.containsKey(saveFailsVA.getPath());

        if (containsKey) {
            List<MethodModel> methodFailures = pathFailures.get(saveFailsVA.getPath());
            methodFailures.add(saveFailsVA.getMethodModel());
        } else {
            saveNewMethodFailure(pathFailures, saveFailsVA);
        }
    }

    private void saveNewMethodFailure(Map<String, List<MethodModel>> pathFailures, SaveFailsVA saveFailsVA) {
        List<MethodModel> methodFailures = Collections.synchronizedList(new ArrayList<>());
        methodFailures.add(saveFailsVA.getMethodModel());
        pathFailures.put(saveFailsVA.getPath(), methodFailures);
    }

    private void saveNewCodeSmellFailures(SaveFailsVA saveFailsVA) {
        Map<String, List<MethodModel>> pathFailures = new ConcurrentHashMap<>();
        saveNewMethodFailure(pathFailures, saveFailsVA);
        saveFailsVA.getRefactoringFailures()
                .put(saveFailsVA.getCodeSmellName(), pathFailures);
    }
}
