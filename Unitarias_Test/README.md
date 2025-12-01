# Prueba Técnica: Testing con JUnit 5

## Información General
- **Tecnologías:** Java 17, JUnit 5, Mockito, AssertJ

---

## Instrucciones

Se le proporciona un proyecto con varias clases que simulan un sistema de gestión bancaria. Su tarea es escribir pruebas unitarias completas para cada clase siguiendo las mejores prácticas.

### Criterios de Evaluación

| Criterio | Peso | Descripción |
|----------|------|-------------|
| Cobertura de código | 20% | Mínimo 80% de cobertura en líneas y ramas |
| Casos de borde | 20% | Manejo de nulls, valores límite, colecciones vacías |
| Uso correcto de JUnit 5 | 20% | Lifecycle, assertions, parametrized tests |
| Mocking efectivo | 15% | Uso apropiado de Mockito para dependencias |
| Legibilidad y estructura | 15% | Naming, organización AAA, documentación |
| Tests de excepción | 10% | Validación correcta de excepciones |

---

## Ejercicios por Nivel

### Nivel 1: Básico (AccountValidator)
- Tests básicos de validación
- Uso de `@Test`, `@DisplayName`
- Assertions básicas

### Nivel 2: Intermedio (TransactionService)
- Tests con dependencias (Mocking)
- Tests parametrizados (`@ParameterizedTest`)
- Verificación de interacciones

### Nivel 3: Avanzado (PaymentProcessor)
- Escenarios complejos de negocio
- Manejo de excepciones personalizadas
- Tests con múltiples mocks
- Verificación de orden de llamadas

### Nivel 4: Experto (TransferOrchestrator)
- Orquestación de múltiples servicios
- Rollback y compensación
- Tests de concurrencia (opcional)
- Custom assertions

---

## Entregables

1. Clases de test en `src/test/java` siguiendo la estructura del código fuente
2. Cada clase de test debe tener al menos:
   - Tests para el happy path
   - Tests para casos de error
   - Tests parametrizados donde aplique
   - Tests de excepciones

---

## Bonus Points

- Uso de `@Nested` para organizar tests relacionados
- Implementación de `@TestFactory` para tests dinámicos
- Custom `ArgumentsProvider` para tests parametrizados
- Uso de `@ExtendWith` con extensiones personalizadas
- Tests de timeout y repetición

---

## Cómo ejecutar

```bash
# Ejecutar todos los tests
./gradlew test

# Ejecutar con reporte de cobertura
./gradlew test jacocoTestReport

# Ver reporte en build/reports/jacoco/html/index.html

# Verificar cobertura mínima (80%)
./gradlew jacocoTestCoverageVerification

# Limpiar y ejecutar tests
./gradlew clean test
```

---

## Notas Importantes

1. NO modifique el código fuente de producción. Este es sólo ilustrativo para la realización de la prueba.
2. Puede agregar dependencias de test si lo considera necesario.
3. Documente cualquier decisión de diseño importante.
4. El código debe compilar y todos los tests deben pasar.

¡Buena suerte!
