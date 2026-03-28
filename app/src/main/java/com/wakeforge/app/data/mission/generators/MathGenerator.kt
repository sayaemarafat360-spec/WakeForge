package com.wakeforge.app.data.mission.generators

import com.wakeforge.app.data.mission.DifficultyConfigurator
import com.wakeforge.app.data.mission.MathOperation
import com.wakeforge.app.domain.models.MathProblem
import com.wakeforge.app.domain.models.MissionDifficulty
import kotlin.random.Random
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MathGenerator @Inject constructor(
    private val configurator: DifficultyConfigurator
) {

    fun generate(difficulty: MissionDifficulty): List<MathProblem> {
        val count = configurator.getMathProblemCount(difficulty)
        return (1..count).map { generateSingleProblem(difficulty) }
    }

    private fun generateSingleProblem(difficulty: MissionDifficulty): MathProblem {
        return when (difficulty) {
            MissionDifficulty.TRIVIAL -> generateTrivialProblem()
            MissionDifficulty.EASY -> generateEasyProblem()
            MissionDifficulty.MEDIUM -> generateMediumProblem()
            MissionDifficulty.HARD -> generateHardProblem()
            MissionDifficulty.EXTREME -> generateExtremeProblem()
        }
    }

    private fun generateTrivialProblem(): MathProblem {
        val operation = Random.nextInt(2)
        val a = Random.nextInt(1, 10)
        val b = Random.nextInt(1, 10)

        return when (operation) {
            0 -> {
                MathProblem(question = "$a + $b", answer = a + b)
            }
            else -> {
                val max = maxOf(a, b)
                val min = minOf(a, b)
                MathProblem(question = "$max - $min", answer = max - min)
            }
        }
    }

    private fun generateEasyProblem(): MathProblem {
        val operations = configurator.getMathOperationTypes(MissionDifficulty.EASY)
        val operation = operations[Random.nextInt(operations.size)]

        return when (operation) {
            MathOperation.ADD -> {
                val a = Random.nextInt(2, 21)
                val b = Random.nextInt(2, 21)
                MathProblem(question = "$a + $b", answer = a + b)
            }
            MathOperation.SUBTRACT -> {
                val a = Random.nextInt(10, 21)
                val b = Random.nextInt(1, a)
                MathProblem(question = "$a - $b", answer = a - b)
            }
            MathOperation.MULTIPLY -> {
                val a = Random.nextInt(2, 10)
                val b = Random.nextInt(2, 10)
                MathProblem(question = "$a \u00D7 $b", answer = a * b)
            }
            else -> generateTrivialProblem()
        }
    }

    private fun generateMediumProblem(): MathProblem {
        val operations = configurator.getMathOperationTypes(MissionDifficulty.MEDIUM)
        val operation = operations[Random.nextInt(operations.size)]

        return when (operation) {
            MathOperation.ADD -> {
                val a = Random.nextInt(10, 51)
                val b = Random.nextInt(10, 51)
                MathProblem(question = "$a + $b", answer = a + b)
            }
            MathOperation.SUBTRACT -> {
                val a = Random.nextInt(25, 51)
                val b = Random.nextInt(10, a)
                MathProblem(question = "$a - $b", answer = a - b)
            }
            MathOperation.MULTIPLY -> {
                val a = Random.nextInt(5, 13)
                val b = Random.nextInt(3, 10)
                MathProblem(question = "$a \u00D7 $b", answer = a * b)
            }
            MathOperation.DIVIDE -> {
                val b = Random.nextInt(2, 10)
                val answer = Random.nextInt(2, 13)
                val a = b * answer
                MathProblem(question = "$a \u00F7 $b", answer = answer)
            }
            else -> generateEasyProblem()
        }
    }

    private fun generateHardProblem(): MathProblem {
        val operations = configurator.getMathOperationTypes(MissionDifficulty.HARD)
        val operation = operations[Random.nextInt(operations.size)]

        return when (operation) {
            MathOperation.ADD -> {
                val a = Random.nextInt(50, 101)
                val b = Random.nextInt(50, 101)
                val c = Random.nextInt(10, 51)
                MathProblem(question = "$a + $b + $c", answer = a + b + c)
            }
            MathOperation.SUBTRACT -> {
                val a = Random.nextInt(100, 201)
                val b = Random.nextInt(30, 100)
                val c = Random.nextInt(5, 30)
                MathProblem(question = "$a - $b - $c", answer = a - b - c)
            }
            MathOperation.MULTIPLY -> {
                val a = Random.nextInt(10, 20)
                val b = Random.nextInt(5, 13)
                val c = Random.nextInt(2, 6)
                MathProblem(question = "$a \u00D7 $b \u00D7 $c", answer = a * b * c)
            }
            MathOperation.DIVIDE -> {
                val b = Random.nextInt(3, 15)
                val c = Random.nextInt(2, 6)
                val partialAnswer = Random.nextInt(3, 12)
                val a = partialAnswer * b * c
                val answer = partialAnswer * b
                MathProblem(question = "$a \u00F7 $b \u00F7 $c", answer = answer)
            }
            else -> {
                val a = Random.nextInt(10, 20)
                val b = Random.nextInt(5, 13)
                val c = Random.nextInt(2, 6)
                MathProblem(question = "$a \u00D7 $b + $c", answer = a * b + c)
            }
        }
    }

    private fun generateExtremeProblem(): MathProblem {
        val operations = configurator.getMathOperationTypes(MissionDifficulty.EXTREME)
        val operation = operations[Random.nextInt(operations.size)]

        return when (operation) {
            MathOperation.ADD -> {
                val operands = mutableListOf<Int>()
                var sum = 0
                repeat(4) {
                    val n = Random.nextInt(10, 101)
                    operands.add(n)
                    sum += n
                }
                MathProblem(question = operands.joinToString(" + "), answer = sum)
            }
            MathOperation.SUBTRACT -> {
                val a = Random.nextInt(500, 1001)
                val b = Random.nextInt(100, 300)
                val c = Random.nextInt(50, 150)
                val d = Random.nextInt(10, 50)
                MathProblem(question = "$a - $b - $c - $d", answer = a - b - c - d)
            }
            MathOperation.MULTIPLY -> {
                val a = Random.nextInt(11, 25)
                val b = Random.nextInt(7, 15)
                val c = Random.nextInt(3, 8)
                MathProblem(question = "$a \u00D7 $b \u00D7 $c", answer = a * b * c)
            }
            MathOperation.DIVIDE -> {
                val c = Random.nextInt(2, 7)
                val b = Random.nextInt(3, 11)
                val partialAnswer = Random.nextInt(5, 15)
                val a = partialAnswer * b * c
                MathProblem(question = "$a \u00F7 $b \u00F7 $c", answer = partialAnswer)
            }
            MathOperation.MODULO -> {
                val a = Random.nextInt(50, 201)
                val b = Random.nextInt(3, 15)
                MathProblem(question = "$a mod $b", answer = a % b)
            }
            else -> {
                val a = Random.nextInt(10, 20)
                val b = Random.nextInt(5, 12)
                val c = Random.nextInt(10, 50)
                val d = Random.nextInt(2, 6)
                MathProblem(question = "$a \u00D7 $b + $c - $d", answer = a * b + c - d)
            }
        }
    }
}
