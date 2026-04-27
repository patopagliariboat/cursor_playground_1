# Plan por etapas — App nativa Android (geovalla con alarma)

**Objetivo de producto (recordatorio):** elegir un punto en el mapa, un radio (ej. 300 m), confirmar, y **activar** una alarma que suene al **entrar** en la zona (distancia al punto ≤ radio), con comprobación de ubicación aun en segundo plano (“modo bolsillo”).

**Stack fijado (primera etapa):** Android **nativo**, **Kotlin**, **Jetpack Compose** (o Views si se decide y se documenta), **Build Gradle (Kotlin DSL)**, **Coroutines** + `lifecycle`, **Fused Location** vía **Google Play services** (salvo requisito explícito de no usar GMS), **GeofencingClient** como camino principal, **ForegroundService** (tipo `location`) como **respaldo** medible, mapas con **Maps SDK** o **MapLibre** según licencias.

**Instalación en esta fase (sin publicación):** APK/AAB de debug, instalación vía `adb install` o compartir APK; flavors `dev`/`internal` con logging extra; firma de debug.

**Consumo (datos, RAM, batería):** no fijar cifras por teoría; **medir** según sección *Datos a recoger* en cada etapa.

---

## Etapa 0 — Repositorio, convención y criterio de hecho

- [x] Estructura de carpetas acordada (p. ej. `data/`, `domain/`, `ui/`, `location/` en `app/src/.../com/patocodes/geoalarm/`).
- [x] Nombre del paquete, `minSdk`/`targetSdk` y notas de permisos en README y `AGENTS.md`.
- [x] Alinear el documento `patocodes/BUENAS-PRACTICAS-AGENTES.md` con flujo de reviews y testeos.
- **Criterio de cierre:** el equipo sabe dónde vive el código, cómo se llama el módulo de ubicación, y qué rama/PRs usar (ver buenas prácticas). *En este repo: módulo `app` + `AGENTS.md`.*

---

## Etapa 1 — App mínima: mapa, radio, activar

- [x] Pantalla (Compose): mapa (`Maps Compose` si `MAPS_API_KEY` en `local.properties`), pin y toque para elegir coordenadas; control de **radio** (slider + presets 100 / 300 / 500 m).
- [x] Botón **Activar** con **DataStore**: latitud, longitud, radio, `armed=true`.
- [x] Navegación a pantalla **Alarma armada** con punto y radio; **Desactivar** vuelve al mapa.

**Tecnologías:** Compose, Navigation, ViewModel, DataStore, Maps Compose + Play services maps (clave opcional).

**Datos a recoger (MVP de instrumentación, opcional ya aquí):** tiempo desde cold start hasta mapa interactivo; fallos al pedir permisos (screenshot o log).

**Criterio de cierre:** flujo a–b–c completo en UI, sin lógica de geovalla aún, con almacenamiento de la intención del usuario. *Hecho en `cursor/etapa-1-map-flow-137a`.*

---

## Etapa 2 — Permisos y modelo de “siempre en segundo plano”

**Qué se construye**

- Flujo por etapas: `ACCESS_FINE_LOCATION` (y `COARSE` si aplica) → luego explicación in-app y solicitud de **“Permitir todo el tiempo”** y `ACCESS_BACKGROUND_LOCATION` en Android 10+.
- Documentación en código o README: por qué hace falta, qué pasa si el usuario niega.
- (Opcional) comprobación de optimización de batería y link a ajustes del sistema.

**Tecnologías:** `ActivityResultContracts`, `PermissionController` patterns, comprobación de `background location` availability.

**Datos a recoger:** tasa de aceptación en dispositivo de prueba; errores en log (`SecurityException`).

**Criterio de cierre:** en un dispositivo real, con negación parcial, la app degrada de forma controlada (mensaje, no crash).

---

## Etapa 3 — Geovalla: `GeofencingClient` (camino A)

**Qué se construye**

- Registro de geocerca circular centrada en el punto, radio = valor elegido, transición `ENTER` (y `DWELL` solo si hace falta y está justificado).
- `PendingIntent` → `BroadcastReceiver` o `Service` que dispare lógica de alarma o actualice un estado.
- Limpieza al desactivar: eliminar geocercas registradas.
- Sincronizar con almacenamiento de Etapa 1.

**Tecnologías:** `GeofencingClient`, `Geofence.Builder`, cuidado con límite de geocercas y IDs.

**Datos a recoger (testeo):**  
- `dumpsys` o logs: eventos de entrada, latencia aproximada.  
- Sesiones: “solo app en bolsillo”, 30 min, sin mapa abierto.

**Criterio de cierre:** al cruzar el umbral (prueba a pie o con mock de ubicación en dev, si se usa), se dispara el handler (incluso sin sonido aún, pero con notificación o log claro).

---

## Etapa 4 — Respaldo: `ForegroundService` + `FusedLocation` (camino B)

**Qué se construye**

- `ForegroundService` con tipo `location`, notificación persistente (“Zona de alarma activa”, acción desactivar).
- `FusedLocationProviderClient.requestLocationUpdates` con `LocationRequest` (intervalo y prioridad documentados y configurables en build dev).
- Evaluación Haversine o equivalente: si distancia ≤ radio → mismo pipeline que el camino A (alarma / estado `DISPARADA`).
- Estrategia: activar B solo si A falla, o si el usuario / settings lo piden, o A/B fijo en builds de benchmark (definir en implementación y anotar aquí al cerrar la etapa).

**Tecnologías:** `ForegroundService`, `ServiceCompat.startForeground`, `LocationRequest`/`LocationRequest.Builder`, notificaciones con canal de alta importancia.

**Datos a recoger (testeo agresivo):**  
- Batería % / hora con Android Profiler, `batterystats` / Historian.  
- RAM: Profiling, `dumpsys meminfo`.  
- Frecuencia de fixes vs intervalo pedido.  
- Datos móviles: con mapa cerrado vs abierto; sin red propia, consumo de mapa ≈ 0 en segundo plano.

**Criterio de cierre:** medición en tabla (dispositivo, escenario, rama) con al menos 2 dispositivos o 2 regímenes (solo geofence vs FGS).

---

## Etapa 5 — Alarma: audio, vibración, UX bajo lock screen

**Qué se construye**

- Reproducción de sonido (criterio: `MediaPlayer`/`ExoPlayer`), vibración, pantalla o full-screen intent según reglas y versiones.
- Acciones: silenciar 30 min, desactivar.
- Atenuación: canal de notificación, DND, volumen, foco de audio (documentar limitaciones reales en prueba).

**Datos a recoger:** latencia entre evento y audio perceptible; falsos negativos por modo silencio del sistema.

**Criterio de cierre:** alarma comprobable en bolsillo y con pantalla apagada (según dispositivo).

---

## Etapa 6 — Consolidación, flavors y cierre de fase 1

**Qué se construye**

- Flavors `dev` (logs verbosos, tal vez mock de GPS) e `internal` o `release` sin ruido.
- README de instalación: `adb install`, requisito de ajuste de batería en OEM problemático.
- Resumen: **recomendación** geofence-only vs híbrido con FGS, basada en **datos** de Etapas 3–4.
- (Opcional) tests instrumentados: reglas de distancia, recepción de `PendingIntent` mockeado.

**Criterio de cierre:** decisión de producto acompañada de evidencia, APK instalable, documento de consumo rellenado (sin prometer batería exacta, sí rangos por escenario).

---

## Anexo: Datos a recoger (resumen)

| Dato / métrica        | Cómo (ejemplos)                    |
|-----------------------|------------------------------------|
| Batería               | Antes/después, Profiler, batterystats |
| RAM                   | Android Profiler, `dumpsys meminfo`   |
| CPU / wake            | Systrace, CPU profiler                |
| Eventos de ubicación  | Logs estructurados, contador          |
| Tráfico de red        | Perfil de red, facturación cero con mapa cerrado |
| Aceptación permisos   | Logs + capturas de flujo            |
| OEM / ahorro batería  | Metadato por sesión de prueba        |

---

## Orden de trabajo sugerido

0 → 1 → 2 → 3 → 4 (benchmark) → 5 → 6.

Cada etapa puede tener su **rama** y **revisión** de otro “agente” o compañero según el documento de buenas prácticas.

**Nota sobre la ruta en Windows:** en este repositorio los archivos viven bajo `patocodes/`. En tu PC podés copiarlos a `C:\Users\boat11\pato_stuff\patocodes\` o mantenerlos en el clon del repo y sincronizar.
