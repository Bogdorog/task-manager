package com.sergeev.taskmanager;

import com.sergeev.taskmanager.security.internal.configuration.AuthenticationManagerConfiguration;
import com.tngtech.archunit.core.domain.JavaClass;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

@SpringBootTest
@MockitoSpyBean(types = AuthenticationManagerConfiguration.class)
class ModulithStructureTest {

    @Test
    void verifyModularStructure() {
        ApplicationModules modules = ApplicationModules.of(TaskManagerApplication.class,
                JavaClass.Predicates.resideInAPackage("..api.."));
        modules.verify();
    }

    @Test
    void createModuleDocumentation() {
        ApplicationModules modules = ApplicationModules.of(TaskManagerApplication.class,
                JavaClass.Predicates.resideInAPackage("..api.."));
        new Documenter(modules)
                .writeDocumentation()
                .writeIndividualModulesAsPlantUml();
    }
}