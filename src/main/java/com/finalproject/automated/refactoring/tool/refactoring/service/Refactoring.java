package com.finalproject.automated.refactoring.tool.refactoring.service;

import com.finalproject.automated.refactoring.tool.model.MethodModel;
import lombok.NonNull;

import java.util.List;
import java.util.Map;

/**
 * @author Faza Zulfika P P
 * @version 1.0.0
 * @since 22 June 2019
 */

public interface Refactoring {

    Map<String, Map<String, List<MethodModel>>> refactoring(@NonNull String path,
                                                            @NonNull List<MethodModel> methodModels);
}
