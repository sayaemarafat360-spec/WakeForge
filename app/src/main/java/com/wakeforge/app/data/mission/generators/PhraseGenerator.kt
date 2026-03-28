package com.wakeforge.app.data.mission.generators

import com.wakeforge.app.data.mission.DifficultyConfigurator
import com.wakeforge.app.domain.models.Mission
import com.wakeforge.app.domain.models.MissionDifficulty
import kotlin.random.Random
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PhraseGenerator @Inject constructor(
    private val configurator: DifficultyConfigurator
) {

    private val trivialPhrases = listOf(
        "Wake up now",
        "Rise and shine",
        "Good morning",
        "Time to go",
        "Get up early",
        "Hello world",
        "New day now",
        "Stay awake"
    )

    private val easyPhrases = listOf(
        "The early bird catches the worm",
        "Every day is a new beginning",
        "Rise up and start the day",
        "Seize the day with joy",
        "Morning light brings new hope",
        "Time to face the world",
        "Start fresh this morning",
        "Wake up and be awesome"
    )

    private val mediumPhrases = listOf(
        "The sun is up and it is time to rise",
        "Every great morning starts with a smile",
        "Today is the perfect day to be amazing",
        "New opportunities await you this fine day",
        "Your morning sets the tone for the whole day",
        "Embrace the morning and chase your big dreams",
        "Wake with purpose and let ambition guide you",
        "A positive mind finds opportunity in everything"
    )

    private val hardPhrases = listOf(
        "The early morning has gold in its mouth so wake up and claim it today",
        "Success comes to those who rise early and work hard every single day",
        "Do not hit the snooze button because greatness awaits you beyond comfort",
        "Your future self will thank you for waking up and making the most of today",
        "Every accomplishment starts with the decision to wake up and try once more",
        "The best time to start working on your goals was yesterday but today works too",
        "Morning people are not born they are made through discipline and strong habits",
        "An alarm clock is a device that reminds you that you have things to accomplish"
    )

    private val extremePhrases = listOf(
        "The only way to do great work is to love what you do, so wake up with passion today, embrace the challenge, and never look back because your determination will define your destiny.",
        "Success is not final, failure is not fatal; it is the courage to continue that counts, so rise and shine, face the day, overcome every obstacle, and prove to yourself that you are stronger than any excuse.",
        "Wake up early, work hard, stay focused, and surround yourself with good people who push you to be your best, because the path to greatness requires discipline, dedication, and an unwavering commitment to excellence.",
        "Today is the day you stop making excuses and start making progress; your dreams do not work unless you do, so get up, get out there, and show the world what you are truly capable of achieving!",
        "The greatest pleasure in life is doing what people say you cannot do, so when that alarm rings, remember that every moment you spend sleeping is a moment you could spend building the future you deserve."
    )

    fun generate(difficulty: MissionDifficulty): Mission.TypePhraseMission {
        val targetLength = configurator.getPhraseLength(difficulty)
        val phrases = getPhrasesForDifficulty(difficulty)
        val selectedPhrase = selectClosestPhrase(phrases, targetLength)

        return Mission.TypePhraseMission(
            id = java.util.UUID.randomUUID().toString(),
            type = com.wakeforge.app.domain.models.MissionType.TYPE_PHRASE,
            difficulty = difficulty,
            isTimed = true,
            timeLimitMs = 0L,
            phrase = selectedPhrase,
            requiredAccuracy = 1.0f
        )
    }

    private fun getPhrasesForDifficulty(difficulty: MissionDifficulty): List<String> {
        return when (difficulty) {
            MissionDifficulty.TRIVIAL -> trivialPhrases
            MissionDifficulty.EASY -> easyPhrases
            MissionDifficulty.MEDIUM -> mediumPhrases
            MissionDifficulty.HARD -> hardPhrases
            MissionDifficulty.EXTREME -> extremePhrases
        }
    }

    private fun selectClosestPhrase(phrases: List<String>, targetLength: Int): String {
        if (phrases.isEmpty()) return "Wake up"

        val scored = phrases.map { phrase ->
            val wordCount = phrase.split("\\s+".toRegex()).size
            val diff = kotlin.math.abs(wordCount - targetLength)
            phrase to diff
        }

        scored.sortedBy { it.second }.let { sorted ->
            val bestScore = sorted.first().second
            val candidates = sorted.filter { it.second == bestScore }.map { it.first }
            return candidates[Random.nextInt(candidates.size)]
        }
    }
}
