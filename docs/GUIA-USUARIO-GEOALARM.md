# Guía de usuario — GeoAlarm

Aplicación Android para elegir un **punto en el mapa**, un **radio** y **activar** un modo de “alarma armada” (la detección real por geovalla llegará en versiones futuras). Esta guía se actualiza con la funcionalidad disponible; para **desarrolladores y agentes**, al fusionar un cambio que afecte el uso o la instalación, añadí una entrada al **Historial** de abajo en el **mismo commit** (ver [`../AGENTS.md`](../AGENTS.md)).

**Paquete de la app:** `com.patocodes.geoalarm`  
**Nombre en el launcher:** GeoAlarm

---

## Requisitos

| Requisito | Notas |
|----------|--------|
| **Android 8+** (API 26) | Mínimo soportado por el proyecto. |
| **Conexión a internet** (recomendada) | Necesaria para **cargar el mapa** y los azulejos. Sin internet el mapa puede no verse; aún podés ingresar coordenadas a mano (modo sin clave o si no hay señal). |
| **Clave de Google Maps** (opcional) | Sin clave, la app muestra un **formulario de latitud/longitud** en lugar del mapa interactivo. Con clave, ves el **mapa** y podés tocar para fijar el punto. |
| **Permisos de ubicación** | **Aún no** pedidos (Etapa 2). La app **no** usa aún el GPS del teléfono para el punto; solo el mapa o el texto. |

---

## Cómo instalar la app (build de desarrollo)

> Publicación en tienda: no prevista aún. El uso es por **APK** generado con Android Studio o Gradle.

1. **Cloná** el repositorio o descargá el código.
2. En la **raíz del repo**, creá el archivo `local.properties` (no se sube a Git) con al menos:
   - `sdk.dir=…` — ruta al **Android SDK** (Android Studio suele generarlo automáticamente al abrir el proyecto).  
   - (Opcional) `MAPS_API_KEY=…` — clave con **Maps SDK for Android** habilitado para el paquete `com.patocodes.geoalarm`. Ver [`../AGENTS.md`](../AGENTS.md) y `local.properties.example`.
3. **Compilá** un APK de debug, por ejemplo:  
   `gradlew.bat :app:assembleDebug` (Windows) o `./gradlew :app:assembleDebug` (macOS / Linux).  
4. **Instalá** el APK en el teléfono:
   - Con cable USB: `adb install -r app/build/outputs/apk/debug/app-debug.apk`  
   - O copiá el `.apk` al dispositivo y abrilo (puede hace falta permitir “fuentes desconocidas” o instalar vía ajustes del fabricante).

**Ubicación típica del APK debug:** `app/build/outputs/apk/debug/app-debug.apk`

---

## Cómo usar la app (hasta la Etapa 1)

### 1) Pantalla de configuración (mapa o coordenadas)

- **Con mapa** (si hay `MAPS_API_KEY` en tu build):
  - Tocá el mapa para colocar el **marcador** en el lugar deseado.
  - El mapa se centra en el punto; podés ajustar tocando otra posición.
- **Sin mapa** (sin clave o mientras carga):
  - Ingresá **latitud** y **longitud** (números, coma o punto como separador decimal).
  - Tocá **Aplicar coordenadas** para guardar el punto de referencia.

### 2) Radio

- Mové el **deslizador** entre 50 m y 2000 m, o usá los accesos rápidos **100 m**, **300 m**, **500 m**.

### 3) Activar

- Tocá **Activar alarma** para guardar el punto, el radio y marcar el modo **armada**.
- Pasás a la pantalla **Alarma armada**, que recuerda el **radio** y las **coordenadas** (texto informativo).

### 4) Desactivar

- En la pantalla **Alarma armada**, tocá **Desactivar alarma** para volver a la configuración. El estado queda en **no armada**; el último punto y radio siguen guardados.

### 5) Al cerrar y volver a abrir

- Si habías dejado la app en modo **armada**, al reabrirla vuelve a mostrar la **pantalla de alarma armada**; si no, la de **configuración**.

> **Qué aún no hace** la app: no suena al acercarse al punto, no usa el GPS en segundo plano, no pide “ubicación en todo el momento”. Eso se documentará en futuras secciones de esta guía.

---

## Preguntas frecuentes (breve)

| Pregunta | Respuesta |
|----------|------------|
| ¿Ve mi ubicación real? | No; solo el mapa o las coordenadas que vos elegís, hasta que haya permisos y lógica de posición. |
| ¿Gasta batería en segundo plano? | En esta fase, no hay seguimiento continuo. |
| ¿Hace falta cuenta Google en el móvil? | Para probar con mapa, el dispositivo suele necesitar acceso a los servicios de mapas; la **clave** se configura en el **build** del desarrollador, no en un formulario de la app. |

---

## Historial (actualizar con cada cambio de producto o instalación)

Cada fila: **alinear con el commit** que introduce el cambio (mensaje o hash corto en la columna *Referencia*). No duplicar el changelog completo: solo **lo que el usuario** necesita.

| Fecha (UTC) | Referencia | Resumen del cambio (usuario) |
|--------------|------------|-----------------------------|
| 2026-04-27 | `82feef4` (rama `cursor/etapa-1-map-flow-137a`) | Primera guía. Etapa 1: mapa o coordenadas manuales, radio, activar / desactivar, persistencia. Sin GPS ni alarma acústica. |
| 2026-04-27 | Documentación `docs/` (abril 2026) | Guía ampliada con instalación detallada; `DISENO-VISUAL-GEOALARM.md`; en `AGENTS.md` queda la regla de actualizar esta guía en cada cambio de producto. |

*Instrucción para mantenedores del repo:* al hacer merge de un PR que modifique el comportamiento, instalación o requisitos, añadí **una fila** arriba (tabla con fecha; commit o descripción) y, si aplica, actualizá las secciones de **Requisitos** o **Cómo usar**. Ver [`../AGENTS.md`](../AGENTS.md).
