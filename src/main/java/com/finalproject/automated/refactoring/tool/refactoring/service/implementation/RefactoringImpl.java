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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
            }
        }
    }

    private void saveRefactoringFailures(SaveFailsVA saveFailsVA) {
        Boolean containsKey = saveFailsVA.getRefactoringFailures().containsKey(saveFailsVA.getCodeSmellName());

        if (containsKey) {
            Map<String, List<MethodModel>> pathFailures = saveFailsVA.getRefactoringFailures().
                    get(saveFailsVA.getCodeSmellName());
            savePathFailures(saveFailsVA.getPath(), saveFailsVA.getMethodModel(), pathFailures);
        } else {
            Map<String, List<MethodModel>> pathFailures = new ConcurrentHashMap<>();
            List<MethodModel> methodFailures = Collections.synchronizedList(new ArrayList<>());
            methodFailures.add(saveFailsVA.getMethodModel());
            pathFailures.put(saveFailsVA.getPath(), methodFailures);
            saveFailsVA.getRefactoringFailures().put(saveFailsVA.getCodeSmellName(), pathFailures);
        }
    }

    private void savePathFailures(String path, MethodModel methodModel,
                                  Map<String, List<MethodModel>> pathFailures) {
        Boolean containsKey = pathFailures.containsKey(path);

        if (containsKey) {
            List<MethodModel> methodFailures = pathFailures.get(path);
            methodFailures.add(methodModel);
        } else {
            List<MethodModel> methodFailures = Collections.synchronizedList(new ArrayList<>());
            methodFailures.add(methodModel);
            pathFailures.put(path, methodFailures);
        }
    }
}
