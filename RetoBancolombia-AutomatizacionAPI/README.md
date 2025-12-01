# 🧪 API Test Automation

## 📋 Descripción General

Este proyecto contiene pruebas automatizadas para la API de [reqres.in](https://reqres.in/), utilizando el framework [Karate DSL](https://github.com/intuit/karate) y reportes enriquecidos con [Cucumber Reporting](https://github.com/damianszczepanik/cucumber-reporting).


- `POST`: Crear Nuevo Usuario.
- `GET`: Consultar Usuarios.
- `PUT`: Actualizar Usuarios.


## 📦 Estructura del Proyecto

- `src/test/java`: Contiene los runners y utilidades Java.
- `src/test/resources`: Contiene los archivos `.feature`, configuraciones y cuerpos de solicitud.
- `karate-config.js`: Configuración dinámica por entorno (dev, qa, production).
- `build.gradle`: Configuración de dependencias y tareas de test.

## 🚀 Ejecución del Proyecto

### 🔧 Prerrequisitos

- JDK 21 instalado
- Gradle configurado (wrapper o instalado globalmente)
- Acceso a internet para descargar dependencias

### ▶️ Comandos Gradle

Ejecutar con el ParallelRunner:

```bash
gradle clean test --tests ParallelRunner --info -DX_API_KEY=reqres-free-v1
```

Ejecutar un runner específico

```bash
gradle clean test --tests "NombreDelRunner" --info -DX_API_KEY=reqres-free-v1
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

- **Cucumber Report**: HTML interactivo con evidencia de cada paso

## 👨‍💻 Autoría

Desarrollado por: **Santiago Hernández Rojo**  
📧 Contacto: [santiagohr1996@gmail.com](mailto:santiagohr1996@gmail.com)  
🔗 GitHub: [https://github.com/Shernar](https://github.com/Shernar/PruebaTecnica-IngQA)

---