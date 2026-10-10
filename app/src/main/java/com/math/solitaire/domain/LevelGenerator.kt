package com.math.solitaire.domain

import com.math.solitaire.data.model.Category
import com.math.solitaire.data.model.LevelData
import com.math.solitaire.data.model.SolitaireCard
import com.math.solitaire.data.model.Word
import com.math.solitaire.data.model.WordDatabase
import kotlin.random.Random

/**
 * Generates math solitaire levels algorithmically.
 *
 * Category  = a target number  (e.g. 12)
 * Word card = a math expression that equals it  (e.g. "6 × 2", "4 + 8", "15 − 3")
 *
 * Difficulty progression:
 *   Easy   (levels  1-10) : Addition only,         targets 5-20,  operands 1-15
 *   Medium (levels 11-30) : Add + Subtract,        targets 5-50,  operands 1-40
 *   Hard   (levels 31-60) : Add + Sub + Multiply,  targets 2-100, mul tables 2-10
 *   VeryHard (levels 61+) : All four operations,   targets 2-144
 */
class LevelGenerator {

    // ─── Public API ─────────────────────────────────────────────────────────

    fun generateLevel(
        database: WordDatabase,
        levelNumber: Int,
        lastUsedCategoryIds: Set<String> = emptySet()
    ): LevelData {
        val difficultyLevel = getDifficultyForLevel(levelNumber)

        val numCategories = when (difficultyLevel) {
            "Easy"     -> minOf(10, 5 + scaleFor(levelNumber, 1, 8))
            "Medium"   -> minOf(10, 6 + scaleFor(levelNumber, 11, 20))
            "Hard"     -> minOf(10, 7 + scaleFor(levelNumber, 31, 60))
            else       -> 10 // VeryHard
        }

        val wordsPerCategory = when (difficultyLevel) {
            "Easy"     -> 4
            "Medium"   -> 5
            "Hard"     -> 6
            else       -> 6
        }

        val steppedCounts = when (difficultyLevel) {
            "Easy"     -> listOf(2, 3, 4, 5)
            "Medium"   -> listOf(3, 4, 5, 6)
            "Hard"     -> listOf(4, 5, 6, 7)
            else       -> listOf(5, 6, 7, 8)
        }

        var attempts = 0
        while (attempts < 200) {
            val seed = levelNumber.toLong() + (attempts * 1000L)
            val random = Random(seed)

            // 1. Generate target numbers for this level
            val targets = generateTargets(difficultyLevel, numCategories, random, lastUsedCategoryIds)

            // 2. Build Category and Word objects
            val categories = targets.map { num ->
                Category(
                    id = "cat_$num",
                    name = formatNumber(num),
                    difficulty = difficultyLevel,
                    targetNumber = num
                )
            }

            val words = mutableListOf<Word>()
            for (cat in categories) {
                val expressions = generateExpressions(cat.targetNumber, wordsPerCategory, difficultyLevel, random)
                expressions.forEachIndexed { idx, expr ->
                    words.add(
                        Word(
                            id = "${cat.id}_$idx",
                            categoryId = cat.id,
                            wordText = expr,
                            difficulty = difficultyLevel,
                            expression = expr
                        )
                    )
                }
            }

            // 3. Build deck
            val cards = mutableListOf<SolitaireCard>()
            categories.forEach { cat ->
                cards.add(
                    SolitaireCard(
                        id = "cat_${cat.id}",
                        text = cat.name,
                        categoryId = cat.id,
                        isCategory = true,
                        isFaceUp = false,
                        category = cat
                    )
                )
            }
            words.forEach { w ->
                cards.add(
                    SolitaireCard(
                        id = "word_${w.id}",
                        text = w.wordText,
                        categoryId = w.categoryId,
                        isCategory = false,
                        isFaceUp = false,
                        word = w
                    )
                )
            }

            val shuffled = cards.shuffled(random)

            // 4. Deal to 4 tableau columns with stepped counts
            val tableaus = List(4) { mutableListOf<SolitaireCard>() }
            var cardIdx = 0
            for (col in 0..3) {
                val count = steppedCounts[col]
                for (i in 0 until count) {
                    if (cardIdx < shuffled.size) {
                        tableaus[col].add(shuffled[cardIdx++])
                    }
                }
            }

            // 5. Flip bottom card of each column face-up
            for (col in 0..3) {
                val list = tableaus[col]
                if (list.isNotEmpty()) {
                    val lastIdx = list.size - 1
                    list[lastIdx] = list[lastIdx].copy(isFaceUp = true)
                }
            }

            val stock = if (cardIdx < shuffled.size) shuffled.drop(cardIdx) else emptyList()

            // 6. Solvability check
            val categoryWordCounts = words.groupBy { it.categoryId }.mapValues { it.value.size }
            if (verifySolvability(words.size, categoryWordCounts, tableaus, stock)) {
                return LevelData(
                    levelNumber = levelNumber,
                    difficulty = difficultyLevel,
                    targetCategories = categories,
                    targetWords = words,
                    initialTableau = tableaus,
                    initialStock = stock
                )
            }
            attempts++
        }

        // Fallback (non-verified but valid)
        return buildFallback(levelNumber, difficultyLevel, numCategories, wordsPerCategory, steppedCounts)
    }

    // ─── Math Expression Generator ───────────────────────────────────────────

    /**
     * Generates [count] distinct math expressions that equal [target].
     * Uses allowed operations based on difficulty.
     */
    private fun generateExpressions(
        target: Int,
        count: Int,
        difficulty: String,
        random: Random
    ): List<String> {
        val results = mutableSetOf<String>()
        val candidates = buildCandidates(target, difficulty)
        val shuffled = candidates.shuffled(random)

        for (expr in shuffled) {
            if (results.size >= count) break
            results.add(expr)
        }

        // If not enough unique expressions, pad with addition variants
        var extra = 1
        while (results.size < count && extra < 200) {
            val a = extra
            val b = target - a
            if (b > 0 && a != b) {
                results.add("$a + $b")
            }
            extra++
        }

        return results.take(count)
    }

    private fun buildCandidates(target: Int, difficulty: String): List<String> {
        val list = mutableListOf<String>()

        // Addition: a + b = target  (a from 1 to target-1)
        for (a in 1 until target) {
            val b = target - a
            if (b > 0) list.add("$a + $b")
        }

        if (difficulty in listOf("Medium", "Hard", "VeryHard")) {
            // Subtraction: a − b = target  (a from target+1 to target*2)
            val maxA = when (difficulty) {
                "Medium"   -> minOf(50, target * 2)
                "Hard"     -> minOf(100, target * 2)
                else       -> minOf(200, target * 3)
            }
            for (a in (target + 1)..maxA) {
                val b = a - target
                if (b > 0 && b != a) list.add("$a − $b")
            }
        }

        if (difficulty in listOf("Hard", "VeryHard")) {
            // Multiplication: a × b = target
            for (a in 2..12) {
                if (target % a == 0) {
                    val b = target / a
                    if (b >= 2 && b <= 12 && a != b) {
                        list.add("$a × $b")
                    } else if (b >= 2 && b <= 12 && a <= b) {
                        list.add("$a × $b")
                    }
                }
            }
        }

        if (difficulty == "VeryHard") {
            // Division: a ÷ b = target
            for (b in 2..12) {
                val a = target * b
                if (a <= 144) list.add("$a ÷ $b")
            }
        }

        return list.distinct()
    }

    // ─── Target Number Generator ─────────────────────────────────────────────

    private fun generateTargets(
        difficulty: String,
        count: Int,
        random: Random,
        lastUsedIds: Set<String>
    ): List<Int> {
        val range = when (difficulty) {
            "Easy"     -> 5..20
            "Medium"   -> 5..50
            "Hard"     -> 4..100
            else       -> 4..144
        }

        val pool = range.toMutableList().shuffled(random).toMutableList()
        // Prefer not repeating last level's numbers
        val preferred = pool.filter { "cat_$it" !in lastUsedIds }
        val source = if (preferred.size >= count) preferred else pool

        return source.take(count).sorted()
    }

    // ─── Helper: format target number for display ─────────────────────────────

    private fun formatNumber(n: Int): String = n.toString()

    // ─── Difficulty Curve ─────────────────────────────────────────────────────

    companion object {
        fun getDifficultyForLevel(levelNumber: Int): String {
            return when {
                levelNumber <= 10  -> "Easy"
                levelNumber <= 30  -> "Medium"
                levelNumber <= 60  -> "Hard"
                else               -> "VeryHard"
            }
        }
    }

    private fun scaleFor(levelNumber: Int, rangeStart: Int, rangeEnd: Int): Int {
        val span = (rangeEnd - rangeStart).coerceAtLeast(1)
        val pos = (levelNumber - rangeStart).coerceIn(0, span)
        return (pos.toFloat() / span * 2).toInt()
    }

    // ─── Fallback Level ───────────────────────────────────────────────────────

    private fun buildFallback(
        levelNumber: Int,
        difficultyLevel: String,
        numCategories: Int,
        wordsPerCategory: Int,
        steppedCounts: List<Int>
    ): LevelData {
        val random = Random(levelNumber.toLong())
        val targets = generateTargets(difficultyLevel, numCategories, random, emptySet())
        val categories = targets.map { num ->
            Category(id = "cat_$num", name = formatNumber(num), difficulty = difficultyLevel, targetNumber = num)
        }
        val words = mutableListOf<Word>()
        for (cat in categories) {
            val expressions = generateExpressions(cat.targetNumber, wordsPerCategory, difficultyLevel, random)
            expressions.forEachIndexed { idx, expr ->
                words.add(Word("${cat.id}_$idx", cat.id, expr, difficultyLevel, expr))
            }
        }
        val cards = mutableListOf<SolitaireCard>()
        categories.forEach { cat ->
            cards.add(SolitaireCard("cat_${cat.id}", cat.name, cat.id, true, false, category = cat))
        }
        words.forEach { w ->
            cards.add(SolitaireCard("word_${w.id}", w.wordText, w.categoryId, false, false, word = w))
        }
        val shuffled = cards.shuffled(random)
        val tableaus = List(4) { mutableListOf<SolitaireCard>() }
        var idx = 0
        for (col in 0..3) {
            repeat(steppedCounts[col]) {
                if (idx < shuffled.size) tableaus[col].add(shuffled[idx++])
            }
        }
        for (col in 0..3) {
            val list = tableaus[col]
            if (list.isNotEmpty()) {
                val li = list.size - 1
                list[li] = list[li].copy(isFaceUp = true)
            }
        }
        val stock = if (idx < shuffled.size) shuffled.drop(idx) else emptyList()
        return LevelData(levelNumber, difficultyLevel, categories, words, tableaus, stock)
    }

    // ─── Solvability Simulation (DFS) ────────────────────────────────────────

    private fun verifySolvability(
        totalWordsToMatch: Int,
        categoryWordCounts: Map<String, Int>,
        initialTableaus: List<List<SolitaireCard>>,
        initialStock: List<SolitaireCard>
    ): Boolean {
        val visited = mutableSetOf<String>()
        return solveDfs(
            tableaus = initialTableaus,
            stock = initialStock,
            waste = emptyList(),
            activeSlots = emptyMap(),
            completedCategories = emptySet(),
            matchedCount = 0,
            visited = visited,
            totalWordsToMatch = totalWordsToMatch,
            categoryWordCounts = categoryWordCounts
        )
    }

    private fun generateStateKey(
        tableaus: List<List<SolitaireCard>>,
        stock: List<SolitaireCard>,
        waste: List<SolitaireCard>,
        activeSlots: Map<String, Int>
    ): String {
        val tStr = tableaus.joinToString(";") { col ->
            col.joinToString(",") { card -> "${card.id}:${if (card.isFaceUp) "U" else "D"}" }
        }
        val sStr = stock.joinToString(",") { it.id }
        val wStr = waste.joinToString(",") { it.id }
        val aStr = activeSlots.entries.sortedBy { it.key }.joinToString(",") { "${it.key}:${it.value}" }
        return "T:$tStr|S:$sStr|W:$wStr|A:$aStr"
    }

    private fun solveDfs(
        tableaus: List<List<SolitaireCard>>,
        stock: List<SolitaireCard>,
        waste: List<SolitaireCard>,
        activeSlots: Map<String, Int>,
        completedCategories: Set<String>,
        matchedCount: Int,
        visited: MutableSet<String>,
        totalWordsToMatch: Int,
        categoryWordCounts: Map<String, Int>
    ): Boolean {
        if (matchedCount >= totalWordsToMatch) return true
        if (visited.size > 4000) return false

        val stateKey = generateStateKey(tableaus, stock, waste, activeSlots)
        if (visited.contains(stateKey)) return false
        visited.add(stateKey)

        // 1. Play bottom Tableau card
        for (colIdx in 0..3) {
            val col = tableaus[colIdx]
            val bottomCard = col.lastOrNull() ?: continue
            if (bottomCard.isFaceUp) {
                if (bottomCard.isCategory) {
                    if (activeSlots.size < 4 && !activeSlots.containsKey(bottomCard.categoryId) && !completedCategories.contains(bottomCard.categoryId)) {
                        val nextTableaus = tableaus.mapIndexed { idx, list ->
                            if (idx == colIdx) {
                                val nl = list.toMutableList().also { it.removeAt(it.size - 1) }
                                if (nl.isNotEmpty()) nl[nl.size - 1] = nl[nl.size - 1].copy(isFaceUp = true)
                                nl
                            } else list
                        }
                        if (solveDfs(nextTableaus, stock, waste, activeSlots + (bottomCard.categoryId to 0), completedCategories, matchedCount, visited, totalWordsToMatch, categoryWordCounts)) return true
                    }
                } else {
                    if (activeSlots.containsKey(bottomCard.categoryId)) {
                        val nextTableaus = tableaus.mapIndexed { idx, list ->
                            if (idx == colIdx) {
                                val nl = list.toMutableList().also { it.removeAt(it.size - 1) }
                                if (nl.isNotEmpty()) nl[nl.size - 1] = nl[nl.size - 1].copy(isFaceUp = true)
                                nl
                            } else list
                        }
                        val cur = activeSlots[bottomCard.categoryId] ?: 0
                        val req = categoryWordCounts[bottomCard.categoryId] ?: 4
                        val newM = cur + 1
                        val isComplete = newM >= req
                        val nextSlots = if (isComplete) activeSlots - bottomCard.categoryId else activeSlots + (bottomCard.categoryId to newM)
                        val nextCompleted = if (isComplete) completedCategories + bottomCard.categoryId else completedCategories
                        if (solveDfs(nextTableaus, stock, waste, nextSlots, nextCompleted, matchedCount + 1, visited, totalWordsToMatch, categoryWordCounts)) return true
                    }
                }
            }
        }

        // 2. Play top Waste card
        val topWaste = waste.lastOrNull()
        if (topWaste != null) {
            if (topWaste.isCategory) {
                if (activeSlots.size < 4 && !activeSlots.containsKey(topWaste.categoryId) && !completedCategories.contains(topWaste.categoryId)) {
                    val nw = waste.toMutableList().also { it.removeAt(it.size - 1) }
                    if (solveDfs(tableaus, stock, nw, activeSlots + (topWaste.categoryId to 0), completedCategories, matchedCount, visited, totalWordsToMatch, categoryWordCounts)) return true
                }
            } else {
                if (activeSlots.containsKey(topWaste.categoryId)) {
                    val nw = waste.toMutableList().also { it.removeAt(it.size - 1) }
                    val cur = activeSlots[topWaste.categoryId] ?: 0
                    val req = categoryWordCounts[topWaste.categoryId] ?: 4
                    val newM = cur + 1
                    val isComplete = newM >= req
                    val nextSlots = if (isComplete) activeSlots - topWaste.categoryId else activeSlots + (topWaste.categoryId to newM)
                    val nextCompleted = if (isComplete) completedCategories + topWaste.categoryId else completedCategories
                    if (solveDfs(tableaus, stock, nw, nextSlots, nextCompleted, matchedCount + 1, visited, totalWordsToMatch, categoryWordCounts)) return true
                }
            }
        }

        // 3. Tableau-to-Tableau moves
        for (colIdx in 0..3) {
            val col = tableaus[colIdx]
            if (col.isEmpty()) continue
            val lastCard = col.last()
            if (!lastCard.isFaceUp) continue
            val targetCatId = lastCard.categoryId
            var startIdx = col.size - 1
            while (startIdx > 0) {
                val prevCard = col[startIdx - 1]
                if (!prevCard.isFaceUp || prevCard.categoryId != targetCatId) break
                startIdx--
            }
            val groupToMove = col.subList(startIdx, col.size)
            val hasFaceDownUnder = startIdx > 0 && !col[startIdx - 1].isFaceUp
            val hasDifferentFaceUpUnder = startIdx > 0 && col[startIdx - 1].isFaceUp && col[startIdx - 1].categoryId != targetCatId

            for (tIdx in 0..3) {
                if (tIdx == colIdx) continue
                val targetCol = tableaus[tIdx]
                if (targetCol.isEmpty()) {
                    if (hasFaceDownUnder || hasDifferentFaceUpUnder) {
                        val nt = tableaus.mapIndexed { idx, list ->
                            when (idx) {
                                colIdx -> { val nl = list.subList(0, startIdx).toMutableList(); if (nl.isNotEmpty()) nl[nl.size - 1] = nl[nl.size - 1].copy(isFaceUp = true); nl }
                                tIdx   -> groupToMove.toList()
                                else   -> list
                            }
                        }
                        if (solveDfs(nt, stock, waste, activeSlots, completedCategories, matchedCount, visited, totalWordsToMatch, categoryWordCounts)) return true
                    }
                } else {
                    val tb = targetCol.last()
                    if (tb.isFaceUp && tb.categoryId == targetCatId && (hasFaceDownUnder || hasDifferentFaceUpUnder)) {
                        val nt = tableaus.mapIndexed { idx, list ->
                            when (idx) {
                                colIdx -> { val nl = list.subList(0, startIdx).toMutableList(); if (nl.isNotEmpty()) nl[nl.size - 1] = nl[nl.size - 1].copy(isFaceUp = true); nl }
                                tIdx   -> { val nl = list.toMutableList().also { it.addAll(groupToMove) }; nl }
                                else   -> list
                            }
                        }
                        if (solveDfs(nt, stock, waste, activeSlots, completedCategories, matchedCount, visited, totalWordsToMatch, categoryWordCounts)) return true
                    }
                }
            }
        }

        // 4. Waste-to-Tableau
        if (topWaste != null && !topWaste.isCategory) {
            for (tIdx in 0..3) {
                val targetCol = tableaus[tIdx]
                val tb = targetCol.lastOrNull()
                val canStack = (tb != null && tb.isFaceUp && tb.categoryId == topWaste.categoryId) || tb == null
                if (canStack) {
                    val nw = waste.toMutableList().also { it.removeAt(it.size - 1) }
                    val nt = tableaus.mapIndexed { idx, list ->
                        if (idx == tIdx) list.toMutableList().also { it.add(topWaste) } else list
                    }
                    if (solveDfs(nt, stock, nw, activeSlots, completedCategories, matchedCount, visited, totalWordsToMatch, categoryWordCounts)) return true
                }
            }
        }

        // 5. Draw from stock
        if (stock.isNotEmpty()) {
            val ns = stock.toMutableList()
            val drawn = ns.removeAt(ns.size - 1)
            val nw = waste.toMutableList().also { it.add(drawn.copy(isFaceUp = true)) }
            if (solveDfs(tableaus, ns, nw, activeSlots, completedCategories, matchedCount, visited, totalWordsToMatch, categoryWordCounts)) return true
        }

        // 6. Recycle waste
        if (stock.isEmpty() && waste.isNotEmpty()) {
            val ns = waste.reversed().map { it.copy(isFaceUp = false) }
            if (solveDfs(tableaus, ns, emptyList(), activeSlots, completedCategories, matchedCount, visited, totalWordsToMatch, categoryWordCounts)) return true
        }

        return false
    }
}
