# MPAZ PRO 3° — Apoyo didáctico y seguimiento del aprendizaje

Aplicación Android desarrollada para el caso de Vinculación con el Medio **"MPAZ PRO 3°: Apoyo
didáctico y seguimiento del aprendizaje"** de la Escuela Escritora Marcela Paz (Sección 007,
San Bernardo).

La app cubre los tres perfiles del caso: **Jefatura UTP** autoriza contenidos, el **docente**
planifica y asigna contenidos a su curso, y el **estudiante** consulta material, responde
desafíos y rinde la evaluación final dentro de los intentos definidos por el docente.

## Integrantes

| Integrante | Rama de trabajo |
|---|---|
| Raúl Méndez | `Raul-Mendez` |
| Gabriel Marambio | `Gabriel-Marambio` |
| Johana Manríquez | `feature/johana-manriquez` |

## Funcionalidades implementadas por integrante

### Raúl Méndez

- Autenticación por perfil contra los usuarios de prueba (`login()`), con validación de
  credenciales y cierre de sesión.
- Navegación completa por perfil (UTP, Docente, Estudiante) y barra superior reutilizable.
- Panel docente con el resumen de contenidos asignados al curso.
- Planificación docente: asignación de contenidos al curso y definición del máximo de intentos
  de la evaluación final.
- Resultados del curso y exportación a CSV.
- Informe pedagógico: generación del informe en borrador y validación docente.
- Integración de las ramas de los tres integrantes en `main`.

### Gabriel Marambio

- RF-01, autorización de contenidos por UTP: aprobar y rechazar un contenido dejando registrada
  la observación (el rechazo exige un motivo).
- Detalle de contenido para UTP con datos curriculares y recursos didácticos asociados.
- Recursos de la aplicación: incorporación del logo de la escuela y del ícono de la app.
- Corrección de errores de compilación y de referencias a recursos.
- Incorporación de la documentación del caso en `docs/`.

### Johana Manríquez

- Filtros de contenidos por asignatura y por estado de aprobación.
- Separación entre desafíos de práctica y evaluación final (pantallas y consultas al ViewModel).
- Estados vacíos de las pantallas del estudiante (material, desafíos y evaluación final) y
  títulos de pantalla según la asignatura del contenido seleccionado.

## Estado de la 3ª entrega

- Reglas de negocio validadas en el **ViewModel** (no en la UI): curso obligatorio, lista de
  contenidos no vacía, máximo de intentos `>= 1`, contenidos solo si están aprobados por UTP,
  intentos agotados y respuestas completas antes de enviar la evaluación.
- Mensajes de reglas de negocio en la interfaz mediante **Snackbar** y texto condicional.
- Validación por campo en los formularios (login y observación de UTP) con **íconos de error y
  de éxito** de Material Icons y textos en `res/values/strings_validacion.xml`.
- Animaciones con **`AnimatedVisibility`** (mensajes, estados vacíos, retroalimentación de los
  desafíos) y **`animateFloatAsState`** (avance del estudiante, progreso de selección y de
  respuestas, íconos de validación).
- Persistencia: **Room y DataStore siguen configurados en Gradle pero todavía no implementados**
  (ver `data/local/PersistenciaPendiente.kt`). Los datos viven en `MockData`.

## Stack

Kotlin 2.0 · Jetpack Compose · Material 3 · MVVM (ViewModel + StateFlow) · Navigation Compose ·
Room + KSP y DataStore configurados en Gradle. El caso no permite usar cámara, GPS, micrófono ni
datos biométricos.

## Cómo abrir y ejecutar

1. Abrir la carpeta que contiene `settings.gradle.kts` con Android Studio.
2. Esperar el Gradle Sync (la primera vez necesita internet).
3. Ejecutar ▶ **Run 'app'**.

Si Android Studio sugiere actualizar Gradle o el Android Gradle Plugin, **no aceptar**: las
versiones están fijadas para que todo el equipo use exactamente las mismas.

## Usuarios de prueba

| Perfil | Usuario | Clave |
|---|---|---|
| Jefatura UTP | utp@mpazpro3.cl | utp123 |
| Docente 3° básico A | docente.3basicoA@mpazpro3.cl | docente123 |
| Estudiante | est001 | 1234 |

También existe el **"Modo desarrollo"** en el login, que permite entrar con cualquier perfil sin
credenciales. Es una ayuda para probar y debe quitarse antes de la entrega final.

## Flujo de pantallas

```
LOGIN ──(según perfil)──┬─> UTP: Contenidos ──> Revisar contenido (aprobar / rechazar)
                        ├─> DOCENTE: Panel ──> Planificación (objetivos, intentos, asignar)
                        │                 └─> Resultados del curso ──> Informe pedagógico
                        └─> ESTUDIANTE: Mis actividades ──> Material ──> Desafíos
                                        ──> Evaluación final ──> Mis respuestas
Cerrar sesión (cualquier perfil) ──> LOGIN
```

El botón atrás nativo de Android lo maneja el `NavHost`; la flecha "←" de la barra superior hace
lo mismo. Al ingresar, el login sale de la pila y "Cerrar sesión" la limpia completa. Ninguna
pantalla del estudiante navega al informe pedagógico.

## Estructura del código (`app/src/main/java/com/mpazpro3/app/`)

| Carpeta | Contenido |
|---|---|
| `model/` | Entidades del caso: `Contenido`, `RecursoDidactico`, `Actividad`, `Asignacion`, `Estudiante`, `SeguimientoAvance`, `InformeDocente`, `Usuario`, `Perfil` y sus enums. |
| `data/` | `MockData.kt`: datos y textos pedagógicos ficticios de ejemplo. |
| `data/local/` | `PersistenciaPendiente.kt`: guía de entidades, DAOs y base de datos para Room. |
| `viewmodel/` | `MpazProViewModel` (lógica de negocio y reglas, un solo ViewModel compartido) y `MensajeRegla` (mensajes que la UI traduce). |
| `navigation/` | `AppNavigation`: rutas, NavHost, ingreso por perfil y cierre de sesión. |
| `ui/components/` | `BarraSuperior`, `SelectorDesplegable` y `MensajesRegla` (mensaje → recurso de texto). |
| `ui/screen/` | `LoginScreen` y una carpeta por perfil: `utp/`, `docente/`, `estudiante/`. |
| `ui/theme/` y `res/` | Tema, colores, logo e ícono de la aplicación. |

## Reglas de negocio implementadas

| Regla | Dónde se valida | Cómo se informa |
|---|---|---|
| El curso debe existir para asignar contenidos | `MpazProViewModel.asignarAlCurso()` | Snackbar con `regla_curso_vacio` |
| Debe seleccionarse al menos un contenido | `MpazProViewModel.asignarAlCurso()` | Snackbar con `regla_sin_contenidos` |
| El máximo de intentos debe ser `>= 1` | `MpazProViewModel.asignarAlCurso()` | Snackbar con `regla_intentos_invalidos` |
| Un contenido no autorizado por UTP no se asigna | `MpazProViewModel.asignarAlCurso()` | Snackbar con `regla_contenido_no_autorizado` |
| La evaluación final respeta los intentos del docente | `MpazProViewModel.enviarEvaluacion()` | Aviso animado y Snackbar con `regla_intentos_agotados` |
| El informe no emite diagnósticos ni consecuencias académicas | `MpazProViewModel.generarInforme()` | Texto descriptivo del informe |

## Pendientes

- Reemplazar `MockData` por Room y DataStore (Semana 9) para conservar el progreso en el
  dispositivo.
- Reproducir el audio de apoyo del material (hoy es un botón sin acción).
- Quitar el "Modo desarrollo" del login.
- Reemplazar los textos pedagógicos de ejemplo por los definidos con el CITT.

## Documentación del caso

- `docs/Requerimientos_Seccion007.pdf`: requerimientos funcionales, reglas de negocio, RNF y
  supuestos.
- `docs/Consultas_Pendientes_Seccion007.pdf`: preguntas abiertas al CITT (Paz Morales).
