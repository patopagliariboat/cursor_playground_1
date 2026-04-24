# Buenas prácticas — trabajo con agentes (y revisión cruzada)

Objetivo: que el desarrollo (humano o asistido) sea **predecible**, **auditable** y **fácil de repasar** por distintas personas o agentes, sin perder el hilo de las etapas del `PLAN-ETAPAS-ALARMA-GEOVALLA.md`.

---

## 1. Archivos de utilidad recomendados para agentes y revisores

| Archivo / artefacto | Propósito |
|---------------------|------------|
| `PLAN-ETAPAS-ALARMA-GEOVALLA.md` | Fuente de verdad de la **ruta y criterios de cierre** por etapa. No duplicar el plan en issues largos: enlazar. |
| `AGENTS.md` o `CONTRIBUTING.md` (raíz o `patocodes/`) | Cómo correr, packages, y **regla**: una etapa = una rama, una PR, checklist de la etapa. |
| `.editorconfig` | Indentación, finales de línea, coherencia entre PRs. |
| `build.gradle.kts` / `settings.gradle.kts` | Versiones de AGP, Kotlin, Compose, `compileSdk` — documentar saltos de versión en el cuerpo de la PR. |
| `config/detekt.yml` o `lint` activado en CI (cuando exista) | Misma barra de estilo en cada iteración. |
| `app/src/.../AndroidManifest.xml` (comentario breve) | Resumen de **permisos** y de **foreground service types**; el agente “seguridad” lee esto primero. |
| Plantilla de **PR** (en `.github` o texto en el repo) | Secciones: *Etapa referida*, *Criterio de cierre verificado*, *Cómo probar* (3–6 viñetas con `adb` si aplica), *Métricas/riesgo*. |
| `CHANGELOG.md` o sección al final de este doc | Qué se cerró en cada etapa; útil para el siguiente agente. |

**Evitar:** duplicar el plan en varios .md; si hace falta resumir, enlazar a `PLAN-ETAPAS-ALARMA-GEOVALLA.md#etapa-n`.

---

## 2. Estructuras a definir (código y proceso)

### 2.1 Paquetes / capas (Android)

- **`ui`**: Composables, `ViewModel` fino, estado UI.
- **`domain`**: modelos puros, reglas (ej. *¿está el usuario dentro del radio?*), sin Android.
- **`data`**: repositorios, `DataStore`/`SharedPreferences`, implementaciones de `LocationRepository`, `GeofenceRepository`.
- **`location` o `geofence`**: glue con `GeofencingClient`, `FusedLocationProviderClient`, `BroadcastReceiver`/`Service`.

Convención de nombres: `*State`, `*Event`, `*Repository`, `*UseCase` si el equipo adopta use cases puntuales.

### 2.2 Modelos mínimos (definir pronto, versión 1)

- `GeoPoint(lat, lon)` o `LocationFix`.
- `AlarmZone(center, radiusMeters, id)`.
- Estado de alarma: `IDLE` | `ARMED` | `TRIGGERED` (u otro set cerrado, con comentario de por qué).

### 2.3 Proceso en Git

- **Rama** por etapa: `etapa-1-mapa-activar`, `etapa-2-permisos`, etc., o `feature/etapa-N-...`.
- **PR pequeña** alineada con una sub-etapa; si una etapa se parte, el plan se actualiza con un sub-índice.
- **Merge** solo con checklist de la etapa en `PLAN-ETAPAS-ALARMA-GEOVALLA.md` completada en la descripción de la PR.

### 2.4 Contratos para “agente revisión”

Cada PR debe indicar:
- qué **archivos tocaron** (lista corta, no 50 enlaces);
- qué **no** se tocó a propósito (reduce ruido en review).

---

## 3. Estrategias de test iterativo y reviews constantes de distintos agentes

### 3.1 Roles de revisión (aunque sean el mismo humano, rotar enfoque)

| Rol (mentalidad) | Qué mira en cada PR |
|------------------|---------------------|
| **UI / producto** | Flujo a–b–c, textos, estados de error, accesibilidad básica. |
| **Permisos / privacidad** | Manifest, textos, degradación al denegar, sin exceso de permisos. |
| **Ubicación** | Uso de `GeofencingClient` vs FGS, cancelación, lifecycle. |
| **Rendimiento** | Frecuencia de updates, riesgo de wakelock, notificación justificada. |
| **Estabilidad** | Nulos, `SecurityException`, desregistro de listeners. |

Cada ronda de “otro agente” puede limitarse a **un** rol, no a todo a la vez.

### 3.2 Pirámide de pruebas en esta fase (sin exigir publicación)

1. **Unit tests** (domain): distancia, transiciones de estado, límites de radio.
2. **Instrumented** mínimo: repositorio con fake, o reglas con Robolectric si aplica.
3. **Manuales con protocolo fijo** (etapas 3–4 del plan):
   - Mismo dispositivo, misma ruta, misma duración, anotar % batería inicio/fin.
   - Anexar: modelo de teléfono, Android version, build (`dev`/`release`).

**Regla de oro:** no comparar cifras de batería entre runs distintos sin fijar **mismo dispositivo y mismo escenario** (ver plan: tabla por escenario).

### 3.3 Iteración en corto

- **Criterio de hecho** por PR = una o dos viñetas del plan; si falla, **no** abrir otra etapa.
- Tras merge, **5–10 min** de smoke: instalar, abrir, flujo crítico; anotar en comentario de merge o en CHANGELOG.
- Si un agente propone “refactor grande”, **cortar** en otra rama/PR, sin mezclar con etapa funcional.

### 3.4 Comunicación entre agentes (humans + IA)

- Dejar en la PR: **contexto** de la etapa anterior (copiar una línea del plan, no reescribir el mundo).
- **Preguntas abiertas** al final: solo si bloquean el siguiente merge.
- `CHANGELOG.md` o línea al pie: *Etapa N cerrada: qué se decidió* (p. ej. “solo Geofence en build X; FGS reservado para benchmark”).

### 3.5 Cuándo escalar a más automatización

- Tras la Etapa 6 o al repetir un bug de OEM: añadir job CI que al menos **compile** y **lint**; pruebas instrumentadas de ubicación requieren emulador con Play o dispositivo — documentar límite.

---

## 4. Resumen

- Un **plan por etapas** enlazado, **una PR por cierre lógico**, **roles de review** rotatorios, **misma tabla de medición** para batería/datos.
- Estructura de capas y modelos fijos reducen re-trabajo entre quienes implementan y quienes revisan.
- Los “archivos de utilidad” son sobre todo **convenio** (manifest comentado, plantilla de PR, `CHANGELOG`, linters) más que lógica extra.
