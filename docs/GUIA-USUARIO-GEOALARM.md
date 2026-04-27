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
| **Permisos de ubicación** (Etapa 2) | La app puede pedir **ubicación mientras usás la app** (y en Android 10+ **todo el tiempo / segundo plano** al tocar *Activar alarma*). **Apple Maps / Google** no sustituyen el permiso del sistema. Si no concedés nada, seguís pudiendo escribir coordenadas; el botón *Usar mi ubicación* pide el permiso de primer plano. |

| Permiso (Android) | Cuándo | Para qué (en el producto actual) |
|-------------------|--------|-----------------------------------|
| `ACCESS_FINE` / `COARSE` (primer plano) | Al usar *Usar mi ubicación* o al activar (si aún no lo tenés) | Centrar el mapa y el pin en tu posición, capa *mi ubicación*; base para el futuro seguimiento. |
| `ACCESS_BACKGROUND_LOCATION` (Android 10+, “Permitir todo el tiempo”) | Tras aceptar el primer diálogo, al **Activar** | Preparar la alarma con la app en segundo plano; la lógica de geovalla aún se implementa en etapas posteriores. Podés *Armar de todos modos* si no concedés *siempre*. |

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

## Cómo usar la app (Etapas 1 y 2)

### 1) Pantalla de configuración (mapa o coordenadas)

- **Con mapa** (si hay `MAPS_API_KEY` en tu build):
  - Tocá el mapa para colocar el **marcador** en el lugar deseado.
  - **Usar mi ubicación**: toca el mapa a tu alrededor o usá el botón **Usar mi ubicación**; la primera vez Android pedirá **ubicación** (mientras usás la app). Con permiso, el mapa puede mostrar tu posición aproximada.
  - El mapa se centra en el punto; podés ajustar tocando otra posición.
- **Sin mapa** (sin clave o mientras carga):
  - Ingresá **latitud** y **longitud** (números, coma o punto como separador decimal).
  - Tocá **Aplicar coordenadas** para guardar el punto de referencia.

### 2) Radio

- Mové el **deslizador** entre 50 m y 2000 m, o usá los accesos rápidos **100 m**, **300 m**, **500 m**.

### 3) Activar (con permisos, Etapa 2)

- Tocá **Activar alarma**. La app explica y puede pedir:
  1. **Ubicación** mientras usás la app (si no la concediste antes).
  2. En **Android 10 o superior**, además, **permitir todo el tiempo** (segundo plano) para el uso completo de la alarma; podés tocar *Ahora no* y luego decidir *Armar de todos modos* o abrir **Ajustes** si el sistema dejó el permiso bloqueado.
- Al terminar el flujo (o al elegir *Armar de todos modos*), se guarda el **punto, radio y armada** y pasás a la pantalla **Alarma armada**.

### 4) Pantalla Alarma armada

- Muestra el **radio** y **coordenadas** y, en esta fase, un **resumen** de si tenés o no permisos de **primer plano** y de **segundo plano** (en Android 10+). Desde allí podés tocar acciones para **volver a pedir** un permiso si faltó.

### 5) Desactivar

- Tocá **Desactivar alarma** para volver a la configuración. El estado queda en **no armada**; el último punto y radio siguen guardados.

### 6) Al cerrar y volver a abrir

- Si habías dejado la app en modo **armada**, al reabrirla vuelve a mostrar la **pantalla de alarma armada**; si no, la de **configuración**.

> **Qué aún no hace** la app: no suena al acercarse al punto; no hay aún un servicio de geovalla 24/7. Los permisos de segundo plano preparan el terreno para la Etapa 3+.

---

## Preguntas frecuentes (breve)

| Pregunta | Respuesta |
|----------|------------|
| ¿Lee siempre el GPS? | Hoy la app pide permisos y puede usar Fused/Map para **fijar el punto** o *mi ubicación*; aún no hay rastreo continuo. |
| ¿Gasta batería en segundo plano? | Sólo conceder “siempre” no activa aún un servicio de ubicación en bucle. |
| ¿Hace falta cuenta Google en el móvil? | Para el mapa, hace falta que los servicios de mapas funcionen; la **MAPS_API_KEY** va en el build. |

---

## Historial (actualizar con cada cambio de producto o instalación)

Cada fila: **alinear con el commit** que introduce el cambio (mensaje o hash corto en la columna *Referencia*). No duplicar el changelog completo: solo **lo que el usuario** necesita.

| Fecha (UTC) | Referencia | Resumen del cambio (usuario) |
|--------------|------------|-----------------------------|
| 2026-04-27 | `82feef4` (rama `cursor/etapa-1-map-flow-137a`) | Primera guía. Etapa 1: mapa o coordenadas manuales, radio, activar / desactivar, persistencia. Sin GPS ni alarma acústica. |
| 2026-04-27 | Documentación `docs/` (abril 2026) | Guía ampliada con instalación detallada; `DISENO-VISUAL-GEOALARM.md`; en `AGENTS.md` queda la regla de actualizar esta guía en cada cambio de producto. |
| 2026-04-28 | Etapa 2 (permisos) | Pedidos de ubicación (primer plano; “todo el tiempo” en Android 10+ al activar); *Mi ubicación* en el mapa; resumen y acciones en pantalla armada. Sin geovalla aún. |

*Instrucción para mantenedores del repo:* al hacer merge de un PR que modifique el comportamiento, instalación o requisitos, añadí **una fila** arriba (tabla con fecha; commit o descripción) y, si aplica, actualizá las secciones de **Requisitos** o **Cómo usar**. Ver [`../AGENTS.md`](../AGENTS.md).
