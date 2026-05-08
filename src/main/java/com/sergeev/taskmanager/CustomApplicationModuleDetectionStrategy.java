package com.sergeev.taskmanager;

import org.springframework.modulith.core.ApplicationModuleDetectionStrategy;
import org.springframework.modulith.core.ApplicationModuleInformation;
import org.springframework.modulith.core.JavaPackage;
import org.springframework.modulith.core.NamedInterfaces;

import java.util.Set;
import java.util.stream.Stream;

/**
 * Кастомная стратегия обнаружения модулей и именованных интерфейсов для Spring Modulith.
 * <p>Именованные интерфейсы обнаруживаются по именам пакетов:
 * api, dto, request, event</p>
 */
public class CustomApplicationModuleDetectionStrategy implements ApplicationModuleDetectionStrategy {


    // Имена пакетов, которые считаются именованными интерфейсами
    private static final Set<String> NAMED_INTERFACE_NAMES = Set.of(
            "api", "dto", "event", "request"
    );

    public CustomApplicationModuleDetectionStrategy() {
    }

    @Override
    public Stream<JavaPackage> getModuleBasePackages(JavaPackage rootPackage) {
        return rootPackage.getDirectSubPackages().stream();
    }

    @Override
    public NamedInterfaces detectNamedInterfaces(JavaPackage basePackage,
                                                 ApplicationModuleInformation information) {

        return NamedInterfaces.builder(basePackage)
                .recursive()
                .matching(NAMED_INTERFACE_NAMES)
                .build();
    }
}
