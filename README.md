# 🏛️ Cal-mé-cac: Mundo Interactivo de Aprendizaje Infantil

**Cal-mé-cac** (proyecto `no_definido`) es una aplicación educativa interactiva para Android desarrollada en **Kotlin** y **Jetpack Compose**, diseñada especialmente para niñas y niños en edad preescolar y primeros años de primaria.

Inspirada en la tradición formativa del antiguo *Calmécac*, la aplicación combina un entorno visual acogedor en primera persona (sin menús de texto complejos) con **guía por voz en español (`TextToSpeech`)**, **efectos de sonido sintéticos (`AudioTrack`)**, **física táctil de arrastrar y soltar (`Drag & Drop`)** y **8 minijuegos progresivos con lógica aleatoria dinámica**.

---

## ✨ Características Principales y Mejoras Recientes

### 1. 🎥 Sala Panorámica Deslizable en Primera Persona (Estilo Cámara FNAF 3)
* **Recorrido Panorámico Amplio (`FirstPersonPanoramicRoomScreen.kt`)**: El salón principal cuenta con un lienzo panorámico extendido que permite **deslizar horizontalmente con un solo dedo** de extremo a extremo o utilizar los **botones laterales de cámara (`◀` / `▶`)** y el **radar panorámico inferior** al estilo *Five Nights at Freddy's 3*.
* **Ambientación Viva e Interactiva en la Sala**: Además de los objetos que abren minijuegos, los elementos decorativos del fondo (`back.jpeg`) reaccionan al tacto:
  1. **Lámpara Colgante del Techo**: Alterna entre **Luz de Día** y **Modo Noche Mágica Estrellada** con estrellas titilantes y voz.
  2. **Lamparita Rosa de Escritorio**: Enciende y apaga un haz de luz cálida de lectura sobre el librero.
  3. **Carritos en la Repisa Superior**: Al tocarlos, aceleran animadamente por la repisa con sonido y voz (`"¡Brum, brum!"`).
  4. **Cojines del Sillón**: Rebotan con física de resorte (`Spring`) al presionarlos.
  5. **Ventana Panorámica Derecha**: Despliega un arcoíris animado en el cielo al tocarla.
* **Cero Menús de Texto**: Toda la navegación ocurre tocando objetos vivos y brillantes dentro de la habitación.

### 2. 🎨 Ilustraciones Vectoriales 3D Cartoon de Alta Definición (`InteractiveObjectsCanvas.kt`)
Todos los objetos interactivos fueron rediseñados desde cero con **gradientes 3D, sombras proyectadas, brillos especulares, contornos suaves y detalles ricos** a la altura de los fondos ilustrados:
* **Baño e Higiene**: Cepillo de dientes ergonómico con mango curvo, cerdas biseladas, pasta tricolor y burbujas (`toothbrush`), jabón artesanal en jabonera con medallón y 7 burbujas iridiscentes (`soap`), toalla afelpada colgada en barra cromada con cenefa bordada y flecos (`towel`), patito de hule 3D (`rubber_duck`), botella de shampoo con dispensador (`shampoo`).
* **Recámara y Sala**: Almohada acolchada con esquinas pellizcadas y luna bordada (`pillow`), osito de peluche completo con moño rojo (`teddy`), pijama estrellada con cuello y bolsillo (`pajamas`), cobija tejida (`blanket`), pantuflas de conejito (`slippers`), control remoto 3D con botones de colores (`tv_remote`), libro mágico (`storybook`), cojín bordado (`cushion`), carrito deportivo (`toy_car`), pelota de playa multicolor (`ball`).
* **Cocina y Tren**: Leche, queso suizo, pescado plateado, corte de carne marmoleado, jugo de naranja, plátano, zanahoria, sandía, manzana roja, huevos en nido, tetera, olla de cobre, mazorca de maíz con hojas, vasija de barro con grecas mexicas, gema de jade tallada, cacao real y girasol dorado.

### 3. 🎲 Motor de Aleatoriedad Dinámica en Todos los Juegos
Cada vez que el jugador entra a un minijuego o presiona **Reiniciar / Jugar de Nuevo** (`resetGame(randomize = true)`), el sistema baraja dinámicamente:
* Las cantidades objetivo de cada ronda (manteniendo progresión creciente de dificultad entre la Ronda 1, 2 y 3).
* Los objetos, alimentos, flores, rutinas horarias o cargamentos seleccionados del catálogo ampliado.
* Las posiciones en el árbol, el orden de las bandejas inferiores y la disposición de las cartas o burbujas para que ninguna partida sea repetitiva.

---

## 🎮 Los 8 Minijuegos Interactivos

Cada minijuego consta de **3 rondas progresivas** (otorgando de **1 a 3 estrellas doradas ⭐**) y guarda el avance automáticamente:

### 1. 🧺 El Huerto de Manzanas (`AppleBasketGameScreen.kt`)
* **Objeto en la sala**: Cesta de manzanas sobre la mesa central.
* **Mecánica**: Arrastra las manzanas que se mecen en la copa del árbol hacia la cesta. Las posiciones en el árbol y la meta de cada ronda se generan aleatoriamente mientras la voz cuenta cada manzana en español.

### 2. 🧸 Ordena las 4 Habitaciones de la Casa (`ActivityEngineScreen.kt`)
* **Objeto en la sala**: Osito de peluche y objetos del hogar en la repisa izquierda.
* **Mecánica**: Clasifica un catálogo de **20 objetos ilustrados** arrastrándolos hacia la **Recámara**, el **Baño**, la **Cocina** o la **Sala**.

### 3. 🕰️ El Reloj del Sol y la Luna (`InteractiveClockGameScreen.kt`)
* **Objeto en la sala**: Reloj de pared junto a la ventana izquierda.
* **Mecánica**: Gira la manecilla horaria o toca los números del reloj analógico para ayudar al Sol y a la Luna con **10 rutinas diarias aleatorias**. El cielo se transforma dinámicamente entre *Amanecer*, *Mediodía*, *Tarde*, *Atardecer* y *Noche estrellada*.

### 4. 🧊 Clasificación en la Cocina (`KitchenSortingGameScreen.kt`)
* **Objeto en la sala**: Refrigerador y estantería a la derecha del sillón.
* **Mecánica**: Arrastra alimentos frescos (leche, queso, pescado, carne, jugo, sandía, zanahoria, manzana) al **Refrigerador** y utensilios o despensa (plato, taza, olla, sartén, cuchara, pan, plátano, tetera) a la **Estantería de Madera**.

### 5. 🚂 El Tren de los Números (`NumberTrainGameScreen.kt`)
* **Objeto en la sala**: Locomotora de madera sobre la alfombra.
* **Mecánica**: Carga el vagón del tren mexica con la cantidad solicitada de **mazorcas de maíz**, **vasijas de barro**, **gemas de jade**, **cacao real** o **girasoles dorados**.

### 6. 🦋 Jardín de Colores y Figuras (`ColorFlowerGardenScreen.kt`)
* **Objeto en la sala**: Mariposa multicolor sobre las macetas derechas.
* **Mecánica**: Empareja cada flor de la bandeja inferior (mezclada aleatoriamente) con su maceta según su color y figura geométrica (*círculo, cuadrado, triángulo, rombo, estrella, óvalo, corazón y pentágono*).

### 7. 📚 Memorama del Calmécac (`MemoryMatchGameScreen.kt`) — *¡NUEVO!*
* **Objeto en la sala**: Libros mágicos brillantes en el librero izquierdo.
* **Mecánica**: Voltea los códices mágicos con medallón solar mexica para encontrar todas las parejas de objetos ilustrados (4 cartas en Ronda 1, 6 cartas en Ronda 2 y 8 cartas en Ronda 3, con selección y tablero aleatorios en cada partida).

### 8. 🫧 Burbujas Mágicas (`BubblePopGameScreen.kt`) — *¡NUEVO!*
* **Objeto en la sala**: Pelota de playa con burbujas en la esquina derecha de la sala.
* **Mecánica**: Observa el medallón objetivo y revienta las burbujas iridiscentes que flotan por el jardín conteniendo el objeto solicitado, evitando las burbujas distractoras mientras cuentas en voz alta.

---

## 🏗️ Arquitectura y Tecnologías

* **Lenguaje**: Kotlin 2.0+
* **UI Toolkit**: Jetpack Compose (Material 3, `Canvas` 2D/3D vectorial, `Animatable`, `rememberInfiniteTransition`, `pointerInput`)
* **Arquitectura**: MVVM (`ViewModel` + `StateFlow` inmutable)
* **Audio y Voz**:
  * `TextToSpeech` nativo en español (`es-MX` / `es-ES`) para guía hablada libre de lectura.
  * Síntesis PCM en tiempo real (`AudioTrack`) para efectos de sonido sin latencia.
* **Persistencia**:
  * **Firebase Realtime Database** (`FirebaseProgressRepository`) con respaldo local automático en memoria para funcionamiento *offline-first*.

---

## 🧪 Ejecución y Pruebas

Para compilar e instalar la aplicación o ejecutar la suite completa de pruebas unitarias:

```bash
# Ejecutar todas las pruebas unitarias (JUnit 4 + Coroutines Test)
.\gradlew.bat testDebugUnitTest

# Compilar el APK de depuración
.\gradlew.bat assembleDebug
```
