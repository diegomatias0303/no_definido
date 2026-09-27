package com.example.no_definido.data

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
}
