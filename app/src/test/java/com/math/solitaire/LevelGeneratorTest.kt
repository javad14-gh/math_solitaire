package com.math.solitaire

import com.math.solitaire.data.model.WordDatabase
import com.math.solitaire.domain.LevelGenerator
import org.junit.Test
import org.junit.Assert.*

class LevelGeneratorTest {

    private fun evaluateExpression(expr: String): Int {
        val trimmed = expr.trim()
        
        // 1. Square Root: √16, √64
        if (trimmed.startsWith("√")) {
            val num = trimmed.substring(1).toInt()
            val root = Math.round(Math.sqrt(num.toDouble())).toInt()
            return root
        }

        // 2. Square: 4², 5²
        if (trimmed.endsWith("²")) {
            val num = trimmed.substring(0, trimmed.length - 1).toInt()
            return num * num
        }

        // 3. Cube: 2³, 3³
        if (trimmed.endsWith("³")) {
            val num = trimmed.substring(0, trimmed.length - 1).toInt()
            return num * num * num
        }

        // 4. Parentheses: (a + b) × c or (a − b) × c
        if (trimmed.startsWith("(") && trimmed.contains(")")) {
            val closeIdx = trimmed.indexOf(")")
            val inside = trimmed.substring(1, closeIdx)
            val outside = trimmed.substring(closeIdx + 1).trim()
            val insideTokens = inside.split(" ")
            val inA = insideTokens[0].toInt()
            val inOp = insideTokens[1]
            val inB = insideTokens[2].toInt()
            val insideResult = if (inOp == "+") inA + inB else inA - inB

            val outsideTokens = outside.split(" ")
            val outOp = outsideTokens[0]
            val outC = outsideTokens[1].toInt()
            return if (outOp in listOf("×", "*", "x")) insideResult * outC else insideResult / outC
        }

        // 5. Multi-term or simple binary: 1 + 2 + 3 or 5 + 7 or 15 − 3
        val tokens = trimmed.split(" ")
        if (tokens.size == 3) {
            val a = tokens[0].toInt()
            val op = tokens[1]
            val b = tokens[2].toInt()
            return when (op) {
                "+" -> a + b
                "-", "−" -> a - b
                "×", "*", "x" -> a * b
                "÷", "/" -> a / b
                else -> error("Unknown operator: $op")
            }
        }

        if (tokens.size == 5) {
            // a op1 b op2 c
            val a = tokens[0].toInt()
            val op1 = tokens[1]
            val b = tokens[2].toInt()
            val op2 = tokens[3]
            val c = tokens[4].toInt()
            val r1 = if (op1 == "+") a + b else a - b
            return if (op2 == "+") r1 + c else r1 - c
        }

        error("Cannot evaluate expression: $expr")
    }

    @Test
    fun testMathematicalCorrectnessAndNoCommutativeTwins() {
        val generator = LevelGenerator()
        val db = WordDatabase(emptyList(), emptyList())
        
        var totalCardsEvaluated = 0
        var commutativeDuplicatePairs = 0
        val expressionTypesSeen = mutableSetOf<String>()

        println("=== TESTING LEVELS 1 TO 50 ===")
        for (lvl in 1..50) {
            val levelData = generator.generateLevel(db, lvl)
            
            for (cat in levelData.targetCategories) {
                val targetNum = cat.targetNumber
                val catWords = levelData.targetWords.filter { it.categoryId == cat.id }
                
                // Assert all words are distinct
                val distinctTexts = catWords.map { it.wordText }.toSet()
                assertEquals("All cards in category ${cat.name} must be unique", catWords.size, distinctTexts.size)

                val normalizedSet = mutableSetOf<String>()
                for (w in catWords) {
                    totalCardsEvaluated++
                    val eval = evaluateExpression(w.wordText)
                    assertEquals("Expression '${w.wordText}' on level $lvl must equal target $targetNum", targetNum, eval)

                    // Track expression types
                    when {
                        w.wordText.startsWith("√") -> expressionTypesSeen.add("SquareRoot")
                        w.wordText.endsWith("²") -> expressionTypesSeen.add("Square")
                        w.wordText.endsWith("³") -> expressionTypesSeen.add("Cube")
                        w.wordText.contains("(") -> expressionTypesSeen.add("Parentheses")
                        w.wordText.count { it == '+' } == 2 -> expressionTypesSeen.add("ThreeTermAdd")
                        w.wordText.contains("×") -> expressionTypesSeen.add("Multiplication")
                        w.wordText.contains("÷") -> expressionTypesSeen.add("Division")
                        w.wordText.contains("−") -> expressionTypesSeen.add("Subtraction")
                        w.wordText.contains("+") -> expressionTypesSeen.add("TwoTermAdd")
                    }

                    // Check for commutative twins like (3 + 9 and 9 + 3)
                    val tokens = w.wordText.split(" ")
                    if (tokens.size == 3 && (tokens[1] in listOf("+", "×"))) {
                        val norm = if (tokens[0].toInt() <= tokens[2].toInt()) "${tokens[0]}${tokens[1]}${tokens[2]}" else "${tokens[2]}${tokens[1]}${tokens[0]}"
                        if (norm in normalizedSet) {
                            commutativeDuplicatePairs++
                        } else {
                            normalizedSet.add(norm)
                        }
                    }
                }
            }
        }

        println("Total cards evaluated: $totalCardsEvaluated")
        println("Commutative twin duplicates found: $commutativeDuplicatePairs")
        println("Expression types observed: $expressionTypesSeen")

        assertEquals("There should be 0 commutative twin duplicate pairs in the same category", 0, commutativeDuplicatePairs)
        assertTrue("Should observe diverse expression types", expressionTypesSeen.size >= 5)
    }
}
