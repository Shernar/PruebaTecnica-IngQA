# 🧪 Serenity BDD - Prueba E2E del Flujo de Compra en Swag Labs

## 📋 Descripción General

Este proyecto automatiza una prueba funcional End-to-End (E2E) del flujo de compra en el sitio [Swag Labs](https://www.saucedemo.com/), utilizando el framework **Serenity BDD** con enfoque **Screenplay Pattern**. El objetivo es validar la experiencia de usuario desde la selección de productos hasta la confirmación de la orden, simulando un proceso de compra como invitado.

## 🎯 Alcance de la Prueba

La automatización cubre los siguientes pasos:

- ✅ Agregar productos al carrito
- 🛒 Visualizar el contenido del carrito
- 👤 Realizar el proceso de Checkout
- 💲 Validar el precio total
- 📦 Finalizar la compra hasta la confirmación: _“Thank you for your order!”_

## 🛠️ Tecnologías y Frameworks

- **Java 21**
- **Serenity BDD 4.2.34**
- **Screenplay Pattern**
- **JUnit Platform**
- **WebDriverManager**
- **Apache POI** (manejo de Excel)
- **JavaFaker** (datos dinámicos)

## 📦 Estructura del Proyecto

```
src/
├── main/
│   └── java/co.com.prueba.bancolombia/
│       ├── enums/               # Enumeraciones reutilizables
│       ├── interactions/waits/  # Lógica de espera personalizada
│       ├── questions/           # Consultas sobre el estado del sistema
│       ├── tasks/               # Acciones que realiza el actor
│       ├── userinterfaces/      # Mapeo de elementos de UI
│       ├── utils/               # Clases reutilizables
├── test/
│   ├── java/us.abstracta.opencart/
│   │   ├── runners/             # Configuración de ejecución de pruebas
│   │   └── stepdefinitions/     # Definición de pasos en Gherkin
│   └── resources/us.abstracta.opencart/
│       ├── features/            # Archivos .feature en Gherkin
│       └── files/               # Recursos adicionales para pruebas

```

## 🚀 Ejecución del Proyecto

### 🔧 Prerrequisitos

- JDK 21 instalado
- Gradle configurado (wrapper o instalado globalmente)
- Acceso a internet para descargar dependencias

### ▶️ Comandos Gradle

Ejecutar pruebas completas con reporte Serenity:

```bash
clean test aggregate
```

Ejecutar un runner específico

```bash
clean :test --tests "us.abstracta.opencart.runners.BuyProductRunner" :aggregate
```

Ejecutar múltiples runners (usando patrón):

```bash
clean :test --tests "us.abstracta.opencart.runners.*" :aggregate
```

Generar solo el reporte Serenity

```bash
aggregate
```

## 🌱 Flujo de Versionamiento (Git Flow)

Para mantener una estrategia de versionamiento clara y colaborativa, se recomienda el siguiente flujo:

```bash
# Inicializar Git Flow
git init

# Enlazar reporitorio remoto con local
git remote add origin "https://github.com/Shernar/PruebaTecnica-IngQA"

# Enlazar credenciales de acceo github
git config --global user.name "TuNombreDeUsuarioGitHub"
git config --global user.email "tuemail@ejemplo.com"

# Revisar cambios en working area
git status

# Agregar todos los cambios agredados
git add .

# Crear commit
git commit -m "Mensaje descriptivo"

# Subir cambios
git push origin Rama

# Traer cambios
git pull origin Rama
```

## 🧠 Recomendaciones de Mantenibilidad

- ✅ **Nomenclatura clara y consistente**: Targets, Tasks y Questions deben reflejar su propósito funcional
- ♻️ **Reutilización de lógica**: Encapsular interacciones comunes (esperas, clics, validaciones) en clases reutilizables
- 🧩 **Separación de responsabilidades**: Mantener los Step Definitions libres de lógica compleja
- 🌐 **Internacionalización**: Preparar el framework para soportar múltiples idiomas si el producto lo requiere
- 📁 **Modularidad**: Agrupar funcionalidades por dominio o flujo para facilitar la escalabilidad
- 🧪 **Datos dinámicos y aislados**: Usar JavaFaker y modelos para evitar colisiones en pruebas concurrentes
- 📊 **Reportes legibles**: Serenity genera evidencia visual y narrativa, útil para QA, Dev y stakeholders

## 📄 Reportes Generados

- **Serenity Report**: HTML interactivo con evidencia de cada paso
- **Capturas de pantalla**: Incluidas en cada paso

## 👨‍💻 Autoría

Desarrollado por: **Santiago Hernández Rojo**  
📧 Contacto: [santiagohr1996@gmail.com](mailto:santiagohr1996@gmail.com)  
🔗 GitHub: [https://github.com/Shernar](https://github.com/Shernar/PruebaTecnica-IngQA)

---