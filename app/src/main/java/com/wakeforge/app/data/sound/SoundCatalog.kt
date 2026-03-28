package com.wakeforge.app.data.sound

data class SoundItem(
    val id: String,
    val name: String,
    val description: String,
    val category: SoundCategory
)

enum class SoundCategory(val displayName: String) {
    GENTLE("Gentle"),
    MODERATE("Moderate"),
    INTENSE("Intense"),
    ELECTRONIC("Electronic")
}

object SoundCatalog {

    val builtinSounds: List<SoundItem> = listOf(
        SoundItem(
            id = "builtin_dawn",
            name = "Dawn",
            description = "Gentle ascending melody that gradually builds, like the first light of morning",
            category = SoundCategory.GENTLE
        ),
        SoundItem(
            id = "builtin_rise",
            name = "Rise",
            description = "Moderate rhythmic pattern with warm tones, designed to gently pull you from sleep",
            category = SoundCategory.MODERATE
        ),
        SoundItem(
            id = "builtin_forge",
            name = "Forge",
            description = "Intense industrial alarm with strong, powerful tones to break through deep sleep",
            category = SoundCategory.INTENSE
        ),
        SoundItem(
            id = "builtin_crystal",
            name = "Crystal",
            description = "Clear, bright bell tones with crystalline resonance, refreshing and attention-grabbing",
            category = SoundCategory.GENTLE
        ),
        SoundItem(
            id = "builtin_digital",
            name = "Digital",
            description = "Electronic retro-style beeps and tones with a modern digital aesthetic",
            category = SoundCategory.ELECTRONIC
        )
    )

    fun getSoundById(id: String): SoundItem? {
        return builtinSounds.find { it.id == id }
    }

    fun getSoundsByCategory(category: SoundCategory): List<SoundItem> {
        return builtinSounds.filter { it.category == category }
    }

    fun getCategories(): List<SoundCategory> {
        return SoundCategory.values().toList()
    }

    fun getDefaultSound(): SoundItem {
        return builtinSounds.first()
    }
}
