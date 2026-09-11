# PréstamoLab CTMA

Aplicación móvil Android para gestionar solicitudes de préstamo de equipos de formación, permitiendo consultar la disponibilidad, registrar solicitudes, validar reglas de negocio y consultar o cancelar préstamos.

---

## 📌 Product Goal

Mejorar la trazabilidad y consulta de préstamos de recursos de formación mediante una experiencia móvil Android, con validaciones y pruebas reproducibles.

---

## 👥 Usuarios

- **Aprendiz:** consulta el catálogo y solicita préstamos.
- **Instructor:** valida evidencias y observa el proceso.
- **Gestor simulado:** representa cambios de estado durante las pruebas.

---

## 🎯 Alcance del primer incremento

El primer incremento contempla:

- Consultar catálogo de equipos.
- Ver detalle de un equipo.
- Registrar una solicitud con destino, propósito y duración.
- Validar reglas de negocio.
- Validar disponibilidad del equipo.
- Evitar duplicación por doble pulsación.
- Consultar las solicitudes realizadas.
- Consultar el detalle de una solicitud.
- Cancelar solicitudes en estado `SOLICITADA`.
- Actualizar la disponibilidad del equipo.
- Mantener los datos mediante un repositorio simulado en memoria.

---

## 🛠️ Tecnologías

- **Kotlin**
- **Jetpack Compose**
- **Material 3**
- **ViewModel**
- **StateFlow**
- **Repository Pattern**
- **Repositorio InMemory**
- **Navigation Compose**
- **Git**
- **GitHub**
- **Android Studio**

---

## 🏗️ Arquitectura del proyecto

El proyecto está organizado utilizando una separación por capas para mantener la interfaz, la lógica de presentación y los datos desacoplados.

```text
com.example.prstamolabctma/
│
├── model/
│   ├── Equipo.kt
│   ├── SolicitudPrestamo.kt
│   └── Estados.kt
│
├── data/
│   └── repository/
│       ├── PrestamoRepository.kt
│       └── InMemoryPrestamoRepository.kt
│
├── viewmodel/
│   └── PrestamoViewModel.kt
│
├── ui/
│   ├── catalogo/
│   │   └── CatalogoScreen.kt
│   │
│   ├── equipo/
│   │   └── EquipoScreen.kt
│   │
│   ├── solicitud/
│   │   └── SolicitudScreen.kt
│   │
│   └── misprestamos/
│       ├── MisPrestamosScreen.kt
│       └── SolicitudDetalleScreen.kt
│
└── navigation/
    └── AppNavigation.kt
````
Capas

Model

Contiene las clases principales del dominio y los estados utilizados por la aplicación.

Data / Repository

Contiene el contrato del repositorio y su implementación en memoria.

ViewModel

Contiene la lógica de presentación, las validaciones y expone el estado mediante StateFlow.

UI

Contiene las pantallas desarrolladas con Jetpack Compose.

Navigation

Controla las rutas de navegación y el paso de identificadores entre pantallas.

# 📏 Reglas de negocio

La aplicación implementa las siguientes reglas:

El ambiente o destino es obligatorio.
El propósito debe tener entre 10 y 180 caracteres.
La duración debe estar entre 1 y 8 horas.
El equipo debe estar disponible para poder solicitarlo.
No se deben crear solicitudes duplicadas por doble pulsación.
Una solicitud creada inicia en estado SOLICITADA.
Al crear una solicitud, el equipo pasa a estado RESERVADO.
Una solicitud SOLICITADA puede ser cancelada.
Al cancelar una solicitud, esta pasa a CANCELADA.
Al cancelar una solicitud, el equipo vuelve a estado DISPONIBLE.
Los identificadores inexistentes deben manejarse sin cerrar abruptamente la aplicación.

# 📖 Historias de Usuario
HU-01 — Consultar catálogo de equipos

Como aprendiz, quiero ver un catálogo de equipos con su disponibilidad para saber qué puedo solicitar.

Criterio de aceptación:

Al abrir el catálogo, se muestran el nombre, categoría y estado de cada equipo.

HU-02 — Registrar solicitud de préstamo

Como aprendiz, quiero registrar una solicitud con destino, propósito y duración para utilizar un equipo en prácticas.

Criterio de aceptación:

Si los datos son válidos, se crea una sola solicitud en estado SOLICITADA y el equipo pasa a RESERVADO.

HU-03 — Cancelar solicitud

Como aprendiz, quiero cancelar una solicitud en estado SOLICITADA para liberar el equipo.

Criterio de aceptación:

Al cancelar, la solicitud pasa a CANCELADA y el equipo vuelve a DISPONIBLE.

HU-04 — Validar propósito mínimo

Como aprendiz, quiero que el sistema me avise si escribo un propósito demasiado corto para asegurar claridad.

Criterio de aceptación:

Si el propósito tiene menos de 10 caracteres, no se guarda y aparece un mensaje específico.

HU-05 — Evitar duplicación por doble pulsación

Como aprendiz, quiero que el sistema ignore pulsaciones repetidas para no generar solicitudes duplicadas.

Criterio de aceptación:

Al presionar dos veces el botón de guardar, solo se crea una solicitud.

HU-06 — Controlar duración máxima

Como aprendiz, quiero que el sistema valide la duración del préstamo para evitar tiempos excesivos.

Criterio de aceptación:

Si la duración supera el límite definido, aparece un mensaje y no se guarda la solicitud.

# ⚠️ Matriz de Riesgos

| Riesgo | Descripción | Probabilidad | Impacto | Mitigación |
|---|---|---|---|---|
| **Duplicación de solicitudes** | El usuario pulsa dos veces el botón y se crean solicitudes duplicadas. | Alta | Medio | Implementar control de doble clic y validación en repositorio. |
| **Propósito demasiado corto** | El aprendiz ingresa un propósito con menos de 10 caracteres. | Media | Bajo | Validar longitud mínima y mostrar mensaje de error. |
| **Duración fuera de rango** | Se ingresan tiempos de préstamo superiores al límite permitido. | Media | Alto | Validar duración máxima y rechazar solicitudes inválidas. |
| **Equipo no disponible** | Se intenta solicitar un equipo que no se encuentra disponible. | Alta | Alto | Validar disponibilidad antes de crear la solicitud. |
| **ID inexistente** | El sistema recibe un ID de equipo o solicitud que no existe. | Baja | Alto | Manejar navegación segura y mostrar mensaje de equipo o solicitud no encontrada. |
| **Accesibilidad limitada** | Usuarios con dificultades visuales pueden tener problemas para utilizar la aplicación. | Media | Medio | Usar textos claros, etiquetas y acciones visibles. |

🎯 Alcance funcional mínimo del incremento
1. Consultar catálogo

Mostrar un catálogo de equipos con nombre, categoría y disponibilidad.

2. Ver detalle de equipo

Abrir el detalle de un equipo utilizando equipoId como argumento de navegación.

3. Registrar solicitud

Permitir registrar una solicitud de préstamo para un equipo disponible.

4. Ingresar datos obligatorios

Solicitar:

Ambiente o destino.
Propósito.
Duración estimada.
5. Validar reglas de negocio

Verificar que los datos cumplan con las restricciones antes de guardar.

6. Mis solicitudes

Mostrar la lista de solicitudes creadas y permitir consultar el detalle de cada una.

7. Evitar duplicación

Prevenir solicitudes duplicadas por doble pulsación del botón de creación.

8. Actualizar disponibilidad

Cambiar el estado del equipo de acuerdo con la solicitud creada o cancelada.

9. Controlar IDs inexistentes

Manejar identificadores inválidos sin que la aplicación se cierre abruptamente.

10. Cancelar solicitud

Permitir cancelar una solicitud que todavía esté en estado SOLICITADA.

11. Repositorio simulado

Mantener los datos durante la ejecución mediante un repositorio InMemory.

12. Accesibilidad básica

Presentar mensajes claros y no depender únicamente del color para comunicar información.

# 🔄 Flujo principal de la aplicación
Catálogo
   │
   ├── Ver detalle de equipo
   │       │
   │       └── Solicitar préstamo
   │               │
   │               ├── Validar datos
   │               │
   │               ├── Validar disponibilidad
   │               │
   │               └── Crear solicitud
   │                       │
   │                       ├── Solicitud: SOLICITADA
   │                       └── Equipo: RESERVADO
   │
   └── Mis préstamos
           │
           └── Detalle de solicitud
                   │
                   └── Cancelar solicitud
                           │
                           ├── Solicitud: CANCELADA
                           └── Equipo: DISPONIBLE

# 📊 Estados del sistema

## Estados de los equipos

| Estado | Descripción |
|---|---|
| `DISPONIBLE` | El equipo puede ser solicitado. |
| `RESERVADO` | El equipo tiene una solicitud activa. |
| `PRESTADO` | El equipo se encuentra actualmente prestado. |

## Estados de las solicitudes

| Estado | Descripción |
|---|---|
| `SOLICITADA` | La solicitud fue creada y está pendiente del proceso correspondiente. |
| `CANCELADA` | La solicitud fue cancelada por el usuario. |

# 🧪 Pruebas y casos de prueba

Las pruebas se enfocan principalmente en validar las reglas de negocio y los flujos principales de la aplicación.

| ID | Caso de prueba | Resultado esperado |
|---|---|---|
| **TC-01** | Consultar catálogo | Se muestran los equipos con nombre, categoría y estado. |
| **TC-02** | Consultar detalle de equipo | Se muestra correctamente la información del equipo seleccionado. |
| **TC-03** | Registrar solicitud válida | Se crea una solicitud en estado `SOLICITADA`. |
| **TC-04** | Ambiente o destino vacío | No se crea la solicitud y se muestra un mensaje de validación. |
| **TC-05** | Propósito menor a 10 caracteres | No se crea la solicitud y se informa el error. |
| **TC-06** | Propósito válido | El sistema permite continuar con la solicitud. |
| **TC-07** | Duración menor al mínimo | No se crea la solicitud. |
| **TC-08** | Duración mayor al máximo | No se crea la solicitud. |
| **TC-09** | Duración válida | El sistema permite crear la solicitud. |
| **TC-10** | Solicitar equipo no disponible | El sistema rechaza la solicitud. |
| **TC-11** | Doble pulsación en Crear solicitud | Solo se crea una solicitud. |
| **TC-12** | Consultar Mis préstamos | Se muestran las solicitudes realizadas. |
| **TC-13** | Consultar detalle de solicitud | Se muestra la información de la solicitud. |
| **TC-14** | Cancelar solicitud `SOLICITADA` | La solicitud pasa a `CANCELADA`. |
| **TC-15** | Cancelar solicitud | El equipo vuelve a `DISPONIBLE`. |
| **TC-16** | Equipo inexistente | La aplicación controla el ID sin cerrarse. |
| **TC-17** | Solicitud inexistente | La aplicación controla el ID sin cerrarse. |
| **TC-18** | Volver al catálogo | El usuario puede regresar correctamente al catálogo. |

## Durante las pruebas manuales se verificaron los principales flujos de la aplicación:

El catálogo muestra los equipos disponibles.
Se puede acceder al detalle de un equipo.
Se puede registrar una solicitud con datos válidos.
Los campos obligatorios son validados.
El propósito se valida entre 10 y 180 caracteres.
La duración se valida entre 1 y 8 horas.
El sistema permite registrar más de una solicitud para equipos disponibles.
Las solicitudes aparecen en Mis préstamos.
Las solicitudes pueden consultarse individualmente.
Una solicitud puede ser cancelada cuando corresponde.
El equipo cambia de disponibilidad después de crear o cancelar una solicitud.
Se controla la duplicación por doble pulsación.
Los IDs inválidos son controlados por la navegación.

# 🧭 Navegación

La aplicación utiliza Navigation Compose para controlar las diferentes pantallas.

Rutas principales
catalogo
   │
   ├── equipo/{equipoId}
   │       │
   │       └── solicitud/{equipoId}
   │
   └── misprestamos
           │
           └── solicitudDetalle/{solicitudId}

Los identificadores equipoId y solicitudId se envían como argumentos de navegación.

# 🧠 Manejo del estado

El PrestamoViewModel utiliza MutableStateFlow internamente y expone un StateFlow de solo lectura para la interfaz.

El estado de la UI contiene:

PrestamoUiState
├── equipos
├── solicitudes
├── mensaje
└── guardando

Esto permite que las pantallas reaccionen a los cambios sin modificar directamente el repositorio.

# 🗄️ Repositorio

La aplicación utiliza un repositorio en memoria:

PrestamoRepository
        │
        └── InMemoryPrestamoRepository

El repositorio se encarga de:

Obtener equipos.
Obtener un equipo por ID.
Obtener solicitudes.
Obtener una solicitud por ID.
Crear solicitudes.
Cancelar solicitudes.
Actualizar los estados correspondientes.

Los datos son simulados y se mantienen únicamente durante la ejecución de la aplicación.

# 🧩 Definition of Done (DoD) mínima

Un incremento se considera terminado cuando cumple con todos estos criterios:

Compilación y ejecución
El proyecto compila y puede ejecutarse en el ambiente definido.
Criterios implementados
Los criterios de aceptación seleccionados están implementados.
UI desacoplada
La interfaz no modifica directamente la fuente de datos.
ViewModel con UiState
El ViewModel expone UiState mediante StateFlow de solo lectura.
Navegación segura
La navegación transporta identificadores y controla IDs inexistentes.
Pruebas ejecutadas
Se ejecutaron los casos acordados y se registraron sus resultados.
Defectos gestionados
Los defectos críticos o altos tienen una decisión explícita.
Confirmación y regresión
Las correcciones relevantes tienen confirmación y pruebas de regresión.
Repositorio actualizado
Git y README están actualizados.
Demostración y autoría
El incremento puede demostrarse y cada integrante puede explicar el funcionamiento implementado.

## 📌 Product Backlog inicial

| ID | Historia / necesidad | Prioridad | Riesgo |
|---|---|---|---|
| **PB-01** | Consultar catálogo de equipos y disponibilidad. | Alta | Alto |
| **PB-02** | Consultar detalle de un equipo. | Alta | Medio |
| **PB-03** | Registrar solicitud de préstamo. | Alta | Alto |
| **PB-04** | Validar propósito, destino y duración. | Alta | Alto |
| **PB-05** | Evitar solicitud sobre equipo no disponible. | Alta | Alto |
| **PB-06** | Evitar duplicación por doble pulsación. | Alta | Alto |
| **PB-07** | Consultar mis solicitudes. | Media | Medio |
| **PB-08** | Consultar detalle de solicitud. | Media | Medio |
| **PB-09** | Cancelar solicitud en estado `SOLICITADA`. | Media | Medio |
| **PB-10** | Manejar IDs inexistentes y estados vacíos. | Media | Medio |
| **PB-11** | Mantener interfaz usable con texto aumentado. | Media | Medio |
| **PB-12** | Documentar arquitectura, pruebas y limitaciones. | Media | Medio |

# 🚧 Limitaciones actuales
El repositorio es completamente simulado y funciona en memoria.
Los datos no persisten después de cerrar la aplicación.
No existe todavía una base de datos local o remota.
No existe autenticación de usuarios.
Los cambios de estado del gestor son simulados.
Las pruebas realizadas son principalmente pruebas manuales sobre el emulador.
🔮 Posibles mejoras futuras
Implementar persistencia con Room.
Agregar autenticación de usuarios.
Implementar roles reales para aprendiz, instructor y gestor.
Agregar una API/backend.
Incorporar notificaciones sobre cambios de estado.
Agregar filtros y búsqueda en el catálogo.
Implementar pruebas automatizadas de UI.
Implementar pruebas unitarias adicionales.
Mejorar accesibilidad y soporte para TalkBack.
Registrar historial completo de préstamos.

# 📁 Estructura general
PrestamoLabCTMA/
│
├── app/
│   └── src/
│       ├── androidTest/
│       │
│       ├── main/
│       │   └── java/
│       │       └── com.example.prstamolabctma/
│       │
│       └── test/
│
├── build.gradle.kts
├── settings.gradle.kts
├── gradlew
├── gradlew.bat
├── gradle.properties
├── local.properties
└── README.md
👨
# 💻 Control de versiones

El proyecto utiliza Git y GitHub para el control de versiones y seguimiento del desarrollo.

Las funcionalidades se desarrollan mediante ramas de trabajo y posteriormente se integran al proyecto principal.

## 📱 Ejecución

Para ejecutar el proyecto:

Abrir el proyecto en Android Studio.
Esperar la sincronización de Gradle.
Seleccionar un dispositivo físico o emulador.
Ejecutar la aplicación.
Ingresar al catálogo de equipos.
Seleccionar un equipo disponible.
Registrar una solicitud con datos válidos.
Consultar la solicitud desde Mis préstamos.

## 📌 Estado del proyecto

Estado: En desarrollo / primer incremento funcional.

El flujo principal de consulta de equipos, registro de solicitudes, consulta de préstamos y cancelación se encuentra implementado y probado mediante el emulador Android.