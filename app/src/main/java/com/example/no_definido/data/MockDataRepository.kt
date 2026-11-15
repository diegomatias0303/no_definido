package com.example.no_definido.data

import kotlin.random.Random

enum class HouseRoomId(val spanishName: String) {
    BEDROOM("la recámara"),       // Arriba-Izquierda
    BATHROOM("el baño"),          // Arriba-Derecha
    KITCHEN("la cocina"),         // Abajo-Izquierda
    LIVING_ROOM("la sala")        // Abajo-Derecha
}

data class VisualHouseItem(
    val id: String,
    val spokenName: String,
    val targetRoom: HouseRoomId,
    val spokenPrompt: String,
    val spokenSuccess: String
)

data class HouseSortingRound(
    val roundNumber: Int,
    val activeRooms: Set<HouseRoomId>,
    val items: List<VisualHouseItem>,
    val spokenIntroduction: String,
    val spokenCompletion: String
)

object MockDataRepository {

    val sampleAreas = listOf(
        HomeArea(id = "bathroom", name = "Baño", description = "Donde nos lavamos las manos y los dientes", colorAccentHex = 0xFF2196F3),
        HomeArea(id = "kitchen", name = "Cocina", description = "Donde preparamos los alimentos", colorAccentHex = 0xFFFF9800),
        HomeArea(id = "bedroom", name = "Habitación", description = "Donde descansamos y dormimos", colorAccentHex = 0xFF9C27B0),
        HomeArea(id = "living_room", name = "Sala", description = "Donde compartimos en familia", colorAccentHex = 0xFF4CAF50)
    )

    val sampleItems = listOf(
        HouseholdItem(
            id = "toothbrush",
            name = "Cepillo de dientes",
            targetAreaId = "bathroom",
            usageDescription = "Sirve para limpiarse los dientes todos los días después de comer."
        ),
        HouseholdItem(
            id = "pan",
            name = "Sartén",
            targetAreaId = "kitchen",
            usageDescription = "Sirve para cocinar alimentos. ¡Usar con ayuda de un adulto!"
        ),
        HouseholdItem(
            id = "pillow",
            name = "Almohada",
            targetAreaId = "bedroom",
            usageDescription = "Sirve para apoyarnos suavemente al dormir."
        ),
        HouseholdItem(
            id = "soap",
            name = "Jabón",
            targetAreaId = "bathroom",
            usageDescription = "Sirve para lavarnos las manos con agua."
        ),
        HouseholdItem(
            id = "tv_remote",
            name = "Control Remoto",
            targetAreaId = "living_room",
            usageDescription = "Sirve para encender y cambiar canales en la televisión."
        )
    )

    /**
     * Catálogo expandido de 20 objetos ilustrados del hogar distribuidos en las 4 habitaciones
     * para generar partidas dinámicas y aleatorias sin repetición.
     */
    val allVisualHouseItems: List<VisualHouseItem> = listOf(
        // === BAÑO (5 objetos) ===
        VisualHouseItem(
            id = "toothbrush",
            spokenName = "el cepillo de dientes",
            targetRoom = HouseRoomId.BATHROOM,
            spokenPrompt = "¿Dónde va el cepillo de dientes con pasta? ¡Llévalo a su habitación!",
            spokenSuccess = "¡Muy bien! El cepillo de dientes va en el baño."
        ),
        VisualHouseItem(
            id = "soap",
            spokenName = "el jabón con burbujas",
            targetRoom = HouseRoomId.BATHROOM,
            spokenPrompt = "¿Dónde va el jabón para lavarnos las manos?",
            spokenSuccess = "¡Súper! El jabón con burbujas va en el baño."
        ),
        VisualHouseItem(
            id = "towel",
            spokenName = "la toalla esponjosa",
            targetRoom = HouseRoomId.BATHROOM,
            spokenPrompt = "¿Dónde colgamos la toalla para secarnos las manos?",
            spokenSuccess = "¡Muy bien! La toalla va en el baño."
        ),
        VisualHouseItem(
            id = "rubber_duck",
            spokenName = "el patito de hule",
            targetRoom = HouseRoomId.BATHROOM,
            spokenPrompt = "¿Dónde nada el patito de hule con burbujas?",
            spokenSuccess = "¡Cuac, cuac! El patito de hule va en la bañera del baño."
        ),
        VisualHouseItem(
            id = "shampoo",
            spokenName = "la botella de champú",
            targetRoom = HouseRoomId.BATHROOM,
            spokenPrompt = "¿Dónde guardamos el champú para bañarnos?",
            spokenSuccess = "¡Excelente! El champú va en el baño."
        ),

        // === RECÁMARA (5 objetos) ===
        VisualHouseItem(
            id = "pillow",
            spokenName = "la almohada suave",
            targetRoom = HouseRoomId.BEDROOM,
            spokenPrompt = "¿Dónde va la almohada suave para soñar?",
            spokenSuccess = "¡Excelente! La almohada va en la cama de la recámara."
        ),
        VisualHouseItem(
            id = "teddy",
            spokenName = "el osito de peluche",
            targetRoom = HouseRoomId.BEDROOM,
            spokenPrompt = "¿Dónde duerme el osito de peluche?",
            spokenSuccess = "¡Genial! El osito descansa en la recámara."
        ),
        VisualHouseItem(
            id = "pajamas",
            spokenName = "la pijama calientita",
            targetRoom = HouseRoomId.BEDROOM,
            spokenPrompt = "¿Dónde nos ponemos la pijama para dormir?",
            spokenSuccess = "¡Perfecto! La pijama va en la recámara."
        ),
        VisualHouseItem(
            id = "blanket",
            spokenName = "la cobija doblada",
            targetRoom = HouseRoomId.BEDROOM,
            spokenPrompt = "¿Dónde ponemos la cobija calientita?",
            spokenSuccess = "¡Muy bien! La cobija va sobre la cama en la recámara."
        ),
        VisualHouseItem(
            id = "slippers",
            spokenName = "las pantuflas suaves",
            targetRoom = HouseRoomId.BEDROOM,
            spokenPrompt = "¿Dónde dejamos las pantuflas al acostarnos?",
            spokenSuccess = "¡Súper! Las pantuflas van junto a la cama en la recámara."
        ),

        // === COCINA (5 objetos) ===
        VisualHouseItem(
            id = "pan",
            spokenName = "la sartén con huevo",
            targetRoom = HouseRoomId.KITCHEN,
            spokenPrompt = "¿Dónde va la sartén para cocinar?",
            spokenSuccess = "¡Eso es! La sartén va en la estufa de la cocina."
        ),
        VisualHouseItem(
            id = "plate",
            spokenName = "el plato decorado",
            targetRoom = HouseRoomId.KITCHEN,
            spokenPrompt = "¿Dónde va el plato para servir la comida?",
            spokenSuccess = "¡Genial! El plato va en la cocina."
        ),
        VisualHouseItem(
            id = "mug",
            spokenName = "la taza de chocolate",
            targetRoom = HouseRoomId.KITCHEN,
            spokenPrompt = "¿Dónde preparamos la taza de chocolate caliente?",
            spokenSuccess = "¡Qué rico! La taza va en la cocina."
        ),
        VisualHouseItem(
            id = "teapot",
            spokenName = "la tetera roja",
            targetRoom = HouseRoomId.KITCHEN,
            spokenPrompt = "¿Dónde calentamos el agua en la tetera roja?",
            spokenSuccess = "¡Bravo! La tetera va en la cocina."
        ),
        VisualHouseItem(
            id = "cooking_pot",
            spokenName = "la olla de sopa",
            targetRoom = HouseRoomId.KITCHEN,
            spokenPrompt = "¿Dónde cocinamos con la olla de sopa?",
            spokenSuccess = "¡Excelente! La olla va en la cocina."
        ),

        // === SALA (5 objetos) ===
        VisualHouseItem(
            id = "tv_remote",
            spokenName = "el control de la tele",
            targetRoom = HouseRoomId.LIVING_ROOM,
            spokenPrompt = "¿Dónde va el control remoto de la televisión?",
            spokenSuccess = "¡Eso es! El control va en la sala junto a la tele."
        ),
        VisualHouseItem(
            id = "ball",
            spokenName = "la pelota de colores",
            targetRoom = HouseRoomId.LIVING_ROOM,
            spokenPrompt = "¿Dónde jugamos con la pelota de colores?",
            spokenSuccess = "¡Bien hecho! La pelota va en la sala."
        ),
        VisualHouseItem(
            id = "storybook",
            spokenName = "el libro de cuentos",
            targetRoom = HouseRoomId.LIVING_ROOM,
            spokenPrompt = "¿Dónde leemos el libro de cuentos en el sillón?",
            spokenSuccess = "¡Maravilloso! El libro de cuentos va en la sala."
        ),
        VisualHouseItem(
            id = "cushion",
            spokenName = "el cojín bordado",
            targetRoom = HouseRoomId.LIVING_ROOM,
            spokenPrompt = "¿Dónde acomodamos el cojín del sillón?",
            spokenSuccess = "¡Perfecto! El cojín va en el sillón de la sala."
        ),
        VisualHouseItem(
            id = "toy_car",
            spokenName = "el carrito de juguete",
            targetRoom = HouseRoomId.LIVING_ROOM,
            spokenPrompt = "¿Dónde rodamos el carrito de juguete sobre la alfombra?",
            spokenSuccess = "¡Run, run! El carrito juega en la sala."
        )
    )

    fun getSampleActivitySteps(): List<ActivityStep> {
        val areasList = sampleAreas.map { it.id }
        return sampleItems.mapIndexed { index, item ->
            ActivityStep(
                id = "step_${index + 1}",
                title = "¿Dónde va este objeto?",
                instruction = "Selecciona o arrastra al lugar correcto de la casa para: ${item.name}",
                activityType = ActivityType.CATEGORIZATION,
                item = item,
                availableTargetAreaIds = areasList,
                correctAreaId = item.targetAreaId
            )
        }
    }

    /**
     * Genera 3 rondas progresivas.
     * Cuando [randomize] es true, selecciona habitaciones y objetos al azar del catálogo de 20 elementos
     * y los mezcla para que cada partida sea fresca y diferente.
     */
    fun getHouseSortingRounds(randomize: Boolean = false, random: Random = Random.Default): List<HouseSortingRound> {
        if (!randomize) {
            return listOf(
                // Ronda 1: 4 objetos en 2 habitaciones (Baño y Recámara)
                HouseSortingRound(
                    roundNumber = 1,
                    activeRooms = setOf(HouseRoomId.BEDROOM, HouseRoomId.BATHROOM),
                    items = listOf(
                        allVisualHouseItems.first { it.id == "toothbrush" },
                        allVisualHouseItems.first { it.id == "pillow" },
                        allVisualHouseItems.first { it.id == "soap" },
                        allVisualHouseItems.first { it.id == "teddy" }
                    ),
                    spokenIntroduction = "¡Ronda 1! Acomoda los objetos en el baño y en la recámara.",
                    spokenCompletion = "¡Completaste la ronda uno! ¡Ganaste una estrella!"
                ),
                // Ronda 2: 6 objetos en 3 habitaciones (Baño, Recámara y Cocina)
                HouseSortingRound(
                    roundNumber = 2,
                    activeRooms = setOf(HouseRoomId.BEDROOM, HouseRoomId.BATHROOM, HouseRoomId.KITCHEN),
                    items = listOf(
                        allVisualHouseItems.first { it.id == "pan" },
                        allVisualHouseItems.first { it.id == "towel" },
                        allVisualHouseItems.first { it.id == "pajamas" },
                        allVisualHouseItems.first { it.id == "plate" },
                        allVisualHouseItems.first { it.id == "soap" },
                        allVisualHouseItems.first { it.id == "pillow" }
                    ),
                    spokenIntroduction = "¡Ronda 2! Ahora también usamos la cocina. ¡A ordenar!",
                    spokenCompletion = "¡Ronda dos completada! ¡Dos estrellas!"
                ),
                // Ronda 3: 8 objetos en las 4 habitaciones
                HouseSortingRound(
                    roundNumber = 3,
                    activeRooms = setOf(HouseRoomId.BEDROOM, HouseRoomId.BATHROOM, HouseRoomId.KITCHEN, HouseRoomId.LIVING_ROOM),
                    items = listOf(
                        allVisualHouseItems.first { it.id == "tv_remote" },
                        allVisualHouseItems.first { it.id == "pan" },
                        allVisualHouseItems.first { it.id == "toothbrush" },
                        allVisualHouseItems.first { it.id == "teddy" },
                        allVisualHouseItems.first { it.id == "ball" },
                        allVisualHouseItems.first { it.id == "mug" },
                        allVisualHouseItems.first { it.id == "towel" },
                        allVisualHouseItems.first { it.id == "pajamas" }
                    ),
                    spokenIntroduction = "¡Ronda 3! ¡Acomoda todos los objetos en las cuatro habitaciones de la casa!",
                    spokenCompletion = "¡Felicidades! ¡Acomodaste toda la casa y ganaste las tres estrellas!"
                )
            )
        }

        val itemsByRoom = allVisualHouseItems.groupBy { it.targetRoom }

        // Ronda 1 aleatoria: 2 habitaciones activas (2 objetos de cada una = 4 objetos mezclados)
        val r1Rooms = setOf(HouseRoomId.BEDROOM, HouseRoomId.BATHROOM)
        val r1Items = r1Rooms.flatMap { room ->
            itemsByRoom[room].orEmpty().shuffled(random).take(2)
        }.shuffled(random)

        // Ronda 2 aleatoria: 3 habitaciones activas (2 objetos de cada una = 6 objetos mezclados)
        val r2Rooms = setOf(HouseRoomId.BEDROOM, HouseRoomId.BATHROOM, HouseRoomId.KITCHEN)
        val r2Items = r2Rooms.flatMap { room ->
            itemsByRoom[room].orEmpty().shuffled(random).take(2)
        }.shuffled(random)

        // Ronda 3 aleatoria: 4 habitaciones activas (2 objetos de cada una = 8 objetos mezclados)
        val r3Rooms = HouseRoomId.entries.toSet()
        val r3Items = r3Rooms.flatMap { room ->
            itemsByRoom[room].orEmpty().shuffled(random).take(2)
        }.shuffled(random)

        return listOf(
            HouseSortingRound(
                roundNumber = 1,
                activeRooms = r1Rooms,
                items = r1Items,
                spokenIntroduction = "¡Ronda 1! Acomoda los objetos en el baño y en la recámara.",
                spokenCompletion = "¡Completaste la ronda uno! ¡Ganaste una estrella!"
            ),
            HouseSortingRound(
                roundNumber = 2,
                activeRooms = r2Rooms,
                items = r2Items,
                spokenIntroduction = "¡Ronda 2! Ahora también usamos la cocina. ¡A ordenar!",
                spokenCompletion = "¡Ronda dos completada! ¡Dos estrellas!"
            ),
            HouseSortingRound(
                roundNumber = 3,
                activeRooms = r3Rooms,
                items = r3Items,
                spokenIntroduction = "¡Ronda 3! ¡Acomoda todos los objetos en las cuatro habitaciones de la casa!",
                spokenCompletion = "¡Felicidades! ¡Acomodaste toda la casa y ganaste las tres estrellas!"
            )
        )
    }
}
