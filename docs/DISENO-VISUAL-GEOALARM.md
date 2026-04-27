# Dirección estética — GeoAlarm (borrador)

Documento de **marca y UI** de referencia. No reemplaza al código: sirve para alinear **paleta**, **tipografías** y **layout** antes y durante el rediseño. Se puede ir completando a medida que el producto toma forma.

---

## 1) Pantallas a cubrir (elementos a crear o pulir)

| Pantalla / bloque | Elementos clave a definir o mejorar | Idea de trato visual (borrador) |
|-------------------|------------------------------------|----------------------------------|
| **Configuración (mapa / coords)** | Barra o título, mapa, marcador, panel inferior fijo o expandible, slider de radio, chips de atajos, botón primario “Activar” | Tono *calm / focus*: mapa a pantalla con panel inferior *sheet* con esquinas redondeadas; CTA con alto contraste. |
| **Configuración (fallback sin mapa)** | Título, texto de ayuda, campos de texto, botón “Aplicar” | Misma jerarquía que arriba: ayuda en tono *secondary*, campos *outlined* alineados, botón bajo *safe area*. |
| **Alarma armada** | Título, estado, datos (radio, coordenadas), CTA de desarmar, opcional: icono de “escudo / zona” | Sensación *seguro / confirmado*; icono simple (monocromo o acento) y mucho aire. |
| **(Futuro) alarma sonando** | Fullscreen o notificación, botón *Silenciar*, contraste mínimo de lectura, accesible en bloqueo | *Alert* sin terror: color de acento reservado para “zona alcanzada”, sin rojo puro a pantalla completa. |
| **(Futuro) permisos / onboarding** | Ilustración o iconos, bullets cortos, botón a ajustes del sistema | Ilustración ligera o diagrama en 1–2 pasos; alineado con [Material 3 *Education* y permisos](https://m3.material.io/) |

---

## 2) Paleta de colores (propuesta inicial — a fijar)

| Token sugerido | Uso | Valor de partida (hex) | Nota |
|----------------|-----|------------------------|------|
| `background` | Fondo de app / detrás de mapas (modo actual oscuro) | `#101418` | Ya en `Color.kt` como base; unificar con `MaterialTheme` |
| `primary` / acento | Botones, slider activo, links | `#2B6CB0` (azul) | Sustituir por marca final si se define logotipo |
| `surface` | Tarjetas, panel inferior | Variante más clara que `background` o *elevation* M3 | Definir en `ColorScheme` |
| `onPrimary` / texto sobre acento | Texto en botones | Blanco o casi | Ver contraste WCAG (≥4.5:1) |
| `success` o `zone` (futuro) | “Dentro de zona / armado” | TBD (verde apagado o teñido del primary) | No usar rojo puro salvo *error* real |
| `warning` (futuro) | Cerca de umbral, no crítico | TBD (ámbar) | Diferenciar de alarma *disparada* si hay dos niveles |

*Útil concretar:* exportar una fila a **Figma** o a `Theme.kt` con nombres `primary`, `onPrimary`, `surface`, `error`, `outline`.

---

## 3) Tipografía y numeración

- **Cuerpo y títulos:** `Material3` (actual por defecto en Compose). *Alternativa* si se busca identidad: una sola **custom** (display) para títulos + **Roboto** o **Geist** para cuerpo (definir con licencia y pesos).
- **Números (coordenadas, metros):** fuente *tabular* o `fontFeatureSettings` para que las cifras no “salten” al cambiar (monospace ligera o variante *tabular* de la misma familia).
- **Tamaño mínimo tocable:** 48 dp de altura de botones (Material ya lo acerca; verificar en chips pequeños en landscape).

---

## 4) Layout de ejemplo (pantalla de configuración)

Boceto lógico (no escala a píxel):

```
+------------------------------------------+
|  [ <- ]   GeoAlarm         [ ··· ]        |  <- app bar (opcional)
+------------------------------------------+
|                                          |
|              MAPA (flex 1)               |
|            · marcador                    |
|                                          |
+------------------------------------------+
|  Radio: 300 m                            |
|  [====|----------]  slider              |
|  [100m] [300m] [500m]  chips            |
|  Coordenadas: -34.60370, -58.38160        |
|  +--------------------------------------+ |
|  |     Activar alarma (primario)        | |
|  +--------------------------------------+ |
+------------------------------------------+
```

- **Mapa** ocupa el **espacio sobrante**; el **panel** es fijo al fondo con **esquinas superiores redondeadas** (Material *Surface* 16–24 dp) para no competir con el mapa.
- **Safe area:** respetar `WindowInsets` (bordes de notch, navigation bar) — ya vía `safeDrawingPadding` o equivalente al refinar.

---

## 5) Recursos y “checklist” útiles para fijar lo visual

| Definir | Por qué ayuda |
|---------|----------------|
| **Paleta 5–7 colores** con roles (M3) | Un solo `Theme` coherente y accesible |
| **Modo claro (opcional) + oscuro** | Mapas de día; usuario en exteriores de noche |
| **Icono de app** (adaptive) | Launcher y notificación (FGS en etapas futuras) |
| **Ilustración 1:1 o 16:9** vacía (onboarding / permisos) | Comunica “zona alrededor tuyo” sin saturar |
| **Motion mínima** (transición Config → Armada) | *Shared element* sutil o fade 200–300 ms |
| **Strings de voz/tono** (formal / vs vos) | Unificar con `strings.xml` (ES + EN si hay i18n) |
| **Referencia de competencia o moodboard** 3–5 capturas (no copiar) | Criterio de “demasiado gamificado” vs “herramienta seria” |

---

## 6) Referencias técnicas (Compose / Material 3)

- [Material 3 for Compose](https://m3.material.io/develop/android/jetpack-compose) — `ColorScheme`, `Typography`, `Shape`.
- Mapas: estilo de mapa claro/oscuro en **Google Maps** para no chocar con el tema (configurar `MapProperties` o estilo JSON cuando toque).
- [WCAG 2.1](https://www.w3.org/WAI/WCAG21/quickref/) — contraste y tamaño de toque.

---

*Última edición (doc):* 2026-04-28 — añadido hilo de flujos de **permisos** (diálogos, pantalla armada con estado) como parte del sistema visual.

## 7) Flujos de permisos (Etapa 2, referencia UX)

- **Rationale** antes de `Activity` del sistema: título + texto corto + *Continuar* / *Ahora no*.
- Tras “Activar” en Android 10+: segundo diálogo para **“Permitir todo el tiempo”**; si el usuario elige *Ahora no*, diálogo **Armar de todos modos** con salida a Ajustes si el permiso quedó bloqueado.
- **Pantalla armada:** bloque de **estado** (líneas de texto) + botones *Conceder…* en línea con Material 3; no rivalizar con el CTA *Desactivar* (mantener *Desactivar* como acción de salida clara).
