# GeoAlarm — notas para agentes y el equipo

## Qué es

Aplicación Android **nativa (Kotlin, Jetpack Compose)** para alarma por geovalla. El plan de etapas y criterios de cierre está en [`patocodes/PLAN-ETAPAS-ALARMA-GEOVALLA.md`](patocodes/PLAN-ETAPAS-ALARMA-GEOVALLA.md). Convención de capas: [`patocodes/BUENAS-PRACTICAS-AGENTES.md`](patocodes/BUENAS-PRACTICAS-AGENTES.md).

## Paquete y versiones (Etapa 0)

| Campo | Valor |
|--------|--------|
| `applicationId` / `namespace` | `com.patocodes.geoalarm` |
| `minSdk` | 26 |
| `targetSdk` / `compileSdk` | 35 |

`AndroidManifest` no declara aún **permisos de ubicación**; se añaden en la Etapa 2.

## Cómo compilar (después de instalar requisitos locales)

1. Crea un fichero `local.properties` en la raíz del repo con la ruta al SDK, por ejemplo en Windows:  
   `sdk.dir=C\:\\Users\\<tu_usuario>\\AppData\\Local\\Android\\Sdk`  
2. En la raíz: `./gradlew :app:assembleDebug` (Linux/macOS) o `gradlew.bat :app:assembleDebug` (Windows).

## Ramas y PRs

- Preferir ramas `feature/etapa-N-...` o `cursor/...-137a` según el convencionado en el repositorio.
- Referenciar en la descripción de la PR la **etapa** del plan completada o avanzada.

## Estado actual (Etapa 0)

Proyecto Gradle con módulo `app`, pantalla mínima Compose y paquetes `data`, `domain`, `ui`, `location` (placeholders).
