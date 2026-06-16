# Trabajo Práctico Segunda Entrega — Interacción Hombre-Computadora (HCI)

## Gestión de Casas Inteligentes - SmartHome

---

## Integrantes

- **Juan Francisco Palermo**
  - Mail: juanpalermo@itba.edu.ar
  - Git id: juan43963
  - Legajo: 63014

- **Jeronimo Di Leo Muntaner**
  - Mail: jdileomuntaner@itba.edu.ar
  - Git id: JeroDiLeo
  - Legajo: 63051

- **Mateo Pirola Paulovich**
  - Mail: mpirolapaulovich@itba.edu.ar
  - Git id: matepirola
  - Legajo: 62810

- **Lucas Rodriguez Nahuel**
  - Mail: lrodrigueznahuel@itba.edu.ar
  - Git id: 1ucasrn
  - Legajo: 63335

---

## Instructivo de instalación

### Dispositivos y API Level utilizados durante las pruebas

La aplicación se probó tanto en **dispositivos físicos** como en **emulador**:

- **Dispositivo físico:** Samsung Galaxy A7 (SM-A750G).
- **Emulador:** Android Virtual Device (AVD) con **Android 16 (API Level 36)**.

Niveles de API soportados por la aplicación:

- **minSdk:** 29 (Android 10) — versión mínima requerida.
- **targetSdk / compileSdk:** 36 (Android 16).

La aplicación requiere permiso de **cámara**, utilizado para el escaneo de códigos QR.

### Instalación en un dispositivo físico con Android

El instructivo está orientado exclusivamente a la **instalación y uso** de la aplicación
mediante el archivo `.apk`, sin contemplar su ejecución en modo desarrollo desde Android
Studio.

1. **Obtener el archivo `app-release.apk`** (o `app-debug.apk`) provisto junto con la
   entrega y copiarlo al almacenamiento del dispositivo (por cable USB, correo,
   descarga, etc.).

2. **Habilitar la instalación de orígenes desconocidos.** Al ser una aplicación que no
   proviene de Google Play, el sistema solicitará permiso para instalarla:
   - En **Ajustes → Aplicaciones → Acceso especial → Instalar apps desconocidas**,
     habilitar el permiso para la aplicación desde la que se abrirá el `.apk`
     (por ejemplo, el explorador de archivos o el navegador).
   - La ubicación exacta de esta opción puede variar según la marca y versión de Android.

3. **Instalar la aplicación.** Abrir el archivo `.apk` desde el explorador de archivos y
   pulsar **Instalar**. Esperar a que finalice el proceso.

4. **Otorgar permisos.** Al iniciar la aplicación por primera vez y utilizar el escáner
   de códigos QR, conceder el permiso de **cámara** cuando se solicite.

5. **Conectividad.** El dispositivo debe contar con **acceso a Internet** para
   comunicarse con la API del servidor de hogares inteligentes.

6. **Iniciar la aplicación.** Una vez instalada, abrir **SmartHome** desde el menú de
   aplicaciones y registrarse o iniciar sesión para comenzar a utilizarla.

### Ejecución desde el emulador de Android Studio

Para correr la aplicación en modo desarrollo directamente desde Android Studio:

1. **Abrir el proyecto.** En Android Studio, seleccionar **File → Open** y elegir la
   carpeta raíz del proyecto. Esperar a que Gradle sincronice las dependencias.

2. **Crear o seleccionar un emulador.** Abrir el **Device Manager** y crear un Android
   Virtual Device (AVD). Se recomienda un dispositivo con **API Level 36 (Android 16)**,
   o cualquier nivel a partir de **API Level 29 (Android 10)**, que es el mínimo soportado.

3. **Iniciar el emulador.** Lanzar el AVD desde el Device Manager y esperar a que el
   dispositivo virtual arranque por completo.

4. **Ejecutar la aplicación.** Seleccionar el módulo **app** y el emulador como destino,
   y pulsar **Run ▶**. Gradle compilará e instalará la aplicación en el
   emulador automáticamente.

5. **Otorgar permisos.** Al utilizar el escáner de códigos QR, conceder el permiso de
   **cámara** cuando se solicite (el emulador puede emular la cámara mediante la webcam
   del equipo).

6. **Conectividad.** El emulador utiliza la conexión a Internet del equipo anfitrión, la
   cual es necesaria para comunicarse con la API del servidor de hogares inteligentes.

---

## Tecnologías principales

- **Lenguaje:** Kotlin
- **UI:** Jetpack Compose (Material 3)
- **Navegación:** Navigation Compose
- **Networking:** Ktor Client
- **Persistencia local:** DataStore Preferences
- **Escaneo de QR:** CameraX + ML Kit Barcode Scanning
- **Tiempo real:** Socket.IO

---

### Registro y correos

Para verificar correctamente el envío y la recepción de los correos correspondientes a las
funcionalidades RF1 a RF6 vinculadas a la gestión de usuarios, se deja disponible una cuenta
de prueba de Ethereal. Esta cuenta permite consultar los correos generados por el sistema
durante la corrección:

- Email: sam28@ethereal.email
- Contraseña: 6TrvQfSbRQmdsCHSTJ
