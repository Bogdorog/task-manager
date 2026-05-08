package com.sergeev.taskmanager;

import com.sergeev.taskmanager.security.internal.configuration.AuthenticationManagerConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@MockitoSpyBean(types = AuthenticationManagerConfiguration.class)
class ModulithStructureTest {

    @Test
    @DisplayName("Проверка модульной структуры")
    void verifyModularStructure() {
        ApplicationModules modules = ApplicationModules.of(TaskManagerApplication.class);
        modules.verify();
    }

    @Test
    @DisplayName("Создание документации по модульной структуре")
    void createModuleDocumentation() {
        ApplicationModules modules = ApplicationModules.of(TaskManagerApplication.class);
        new Documenter(modules)
                .writeDocumentation()
                .writeIndividualModulesAsPlantUml();
    }

    //@Test
    @DisplayName("Вывести именованные интерфейсы для модуля 'user'")
    void printNamedInterfacesForUserModule() {
        ApplicationModules modules = ApplicationModules.of(TaskManagerApplication.class);

        // Находим конкретный модуль
        var userModule = modules.getModuleByName("user")
                .orElseThrow(() -> new AssertionError("Модуль 'user' не найден"));

        // Выводим информацию о модуле
        System.out.println("\n=== Модуль: " + userModule.getName() + " ===");
        System.out.println("Базовый пакет: " + userModule.getBasePackage().getName());

        // Выводим все именованные интерфейсы
        System.out.println("\n📦 Именованные интерфейсы:");
        userModule.getNamedInterfaces().forEach(namedInterface -> {
            System.out.println("  ├─ 🔹 " + namedInterface.getName());
        });

        // Проверка: хотя бы один именованный интерфейс найден
        assertThat(userModule.getNamedInterfaces())
                .as("Модуль должен иметь именованные интерфейсы")
                .isNotEmpty();
    }

    /* Примеры тестов
    Тест на количество именованных интерфейсов у определенного модуля
    @Test
    void userModuleExposesNamedInterfaces() {
        ApplicationModules modules = ApplicationModules.of(TaskManagerApplication.class);
        assertThat(modules.getModuleByName("user")).hasValueSatisfying(it -> {
            var interfaces = it.getNamedInterfaces();
            var reference = List.of("api", "dto", "event", "request");

            assertThat(interfaces).extracting(NamedInterface::getName)
                    .hasSize(reference.size() + 1)  // +1 for unnamed interface
                    .containsAll(reference);
        });
    }

    Тест на нарушение зависимостей у определенного модуля
    @Test
    void detectsReferenceToUndeclaredNamedInterface() {
        assertThat(modules.getModuleByName("invalid3")).hasValueSatisfying(it -> {
            assertThatExceptionOfType(Violations.class)
                    .isThrownBy(() -> it.verifyDependencies(modules))
                    .withMessageContaining("Allowed targets")
                    .withMessageContaining("complex :: API");
        });
    }
    */
}