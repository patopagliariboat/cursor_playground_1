# GeoAlarm — notas para agentes y el equipo

## Qué es

Aplicación Android **nativa (Kotlin, Jetpack Compose)** para alarma por geovalla. El plan de etapas y criterios de cierre está en [`patocodes/PLAN-ETAPAS-ALARMA-GEOVALLA.md`](patocodes/PLAN-ETAPAS-ALARMA-GEOVALLA.md). Convención de capas: [`patocodes/BUENAS-PRACTICAS-AGENTES.md`](patocodes/BUENAS-PRACTICAS-AGENTES.md).

## Documentación para usuarios y diseño

| Documento | Contenido |
|-----------|-----------|
| [`docs/GUIA-USUARIO-GEOALARM.md`](docs/GUIA-USUARIO-GEOALARM.md) | Pasos de **instalación**, **uso** de lo implementado, **requisitos**, **FAQ** e **historial** de cambios orientados al usuario final. |
| [`docs/DISENO-VISUAL-GEOALARM.md`](docs/DISENO-VISUAL-GEOALARM.md) | Dirección **estética**: pantallas, paleta sugerida, layout de ejemplo, checklist visual. |

## Mantenimiento del documento de usuario (obligatorio en cambios de producto)

Cuando un **commit o PR** modifique **comportamiento visible**, **requisitos de instalación**, **permisos**, **flujos de pantalla** o **nombre de acciones** en la app:

1. **Actualizá** [`docs/GUIA-USUARIO-GEOALARM.md`](docs/GUIA-USUARIO-GEOALARM.md) en el **mismo commit** (o en el commit de merge del PR), de forma que quede alineado con el código.
2. Añadí **una fila** a la tabla **Historial** al final de esa guía (fecha en UTC, referencia al commit o al mensaje corto, resumen en lenguaje de usuario).
3. Si el cambio afecta **solo a desarrolladores** (Gradle, claves, `adb`), actualizá también las secciones **Requisitos** o **Cómo instalar** de la guía y, si aplica, este `AGENTS.md`.

Si el cambio es **solo estético** (colores, componentes, sin cambiar pasos de uso), actualizá preferentemente [`docs/DISENO-VISUAL-GEOALARM.md`](docs/DISENO-VISUAL-GEOALARM.md).

## Paquete y versiones (Etapa 0)

| Campo | Valor |
|--------|--------|
| `applicationId` / `namespace` | `com.patocodes.geoalarm` |
| `minSdk` | 26 |
| `targetSdk` / `compileSdk` | 35 |

`AndroidManifest` declara **INTERNET** (mapas). Los **permisos de ubicación** se añaden en la Etapa 2.

### Mapas (Etapa 1)

En `local.properties`, opcional:

`MAPS_API_KEY=tu_clave`

Generá la clave en Google Cloud con **Maps SDK for Android** habilitado, restricción por paquete `com.patocodes.geoalarm` y SHA-1 de firma debug. Sin clave, la pantalla de configuración muestra un aviso y coordenadas por defecto (podés seguir probando radio y “Activar”).

Ver `local.properties.example`.

## Cómo compilar (después de instalar requisitos locales)

1. Crea un fichero `local.properties` en la raíz del repo con la ruta al SDK, por ejemplo en Windows:  
   `sdk.dir=C\:\\Users\\<tu_usuario>\\AppData\\Local\\Android\\Sdk`  
2. (Opcional) Añade `MAPS_API_KEY=...` como arriba.  
3. En la raíz: `./gradlew :app:assembleDebug` (Linux/macOS) o `gradlew.bat :app:assembleDebug` (Windows).

## Ramas y PRs

- Preferir ramas `feature/etapa-N-...` o `cursor/...-137a` según el convencionado en el repositorio.
- Referenciar en la descripción de la PR la **etapa** del plan completada o avanzada.
- Si el PR altera el uso o la instalación, enlazar la actualización de [`docs/GUIA-USUARIO-GEOALARM.md`](docs/GUIA-USUARIO-GEOALARM.md) en la descripción.

## Estado actual (Etapa 1)

- **DataStore** (`AlarmZoneRepository`): lat/lon, radio, `armed`.
- **Navegación** `Setup` ↔ `Armed`; mapa con **Maps Compose** si hay `MAPS_API_KEY`.
- Sin geovalla ni GPS de dispositivo todavía (`location/` reservado).
