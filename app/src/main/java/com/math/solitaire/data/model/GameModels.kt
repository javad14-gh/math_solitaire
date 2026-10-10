package com.math.solitaire.data.model

import kotlinx.serialization.Serializable

/**
 * Represents a target number category.
 * e.g. Category(id="12", name="12", targetNumber=12, difficulty="Easy")
 */
@Serializable
data class Category(
    val id: String,
    val name: String,
    val difficulty: String,
    val group: String? = null,
    val targetNumber: Int = 0
)

/**
 * Represents a math expression card.
 * e.g. Word(id="12_0", categoryId="12", wordText="6 × 2", difficulty="Easy")
 */
@Serializable
data class Word(
    val id: String,
    val categoryId: String,
    val wordText: String,       // display text: "6 × 2"
    val difficulty: String,
    val expression: String = wordText // raw expression same as display
)

@Serializable
data class WordDatabase(
    val categories: List<Category>,
    val words: List<Word>
)

@Serializable
data class SolitaireCard(
    val id: String,
    val text: String,
    val categoryId: String,
    val isCategory: Boolean,
    val isFaceUp: Boolean,
    val word: Word? = null,
    val category: Category? = null
)

@Serializable
data class FoundationSlot(
    val id: Int,
    val activeCategory: Category? = null,
    val matchedWords: List<Word> = emptyList()
)

@Serializable
data class LevelData(
    val levelNumber: Int,
    val difficulty: String = "Easy",
    val targetCategories: List<Category>,
    val targetWords: List<Word>,
    val initialTableau: List<List<SolitaireCard>>,
    val initialStock: List<SolitaireCard>
)

@Serializable
data class SavedGameSession(
    val levelNumber: Int,
    val levelData: LevelData,
    val foundationSlots: List<FoundationSlot>,
    val tableauPiles: List<List<SolitaireCard>>,
    val stockPile: List<SolitaireCard>,
    val wastePile: List<SolitaireCard>,
    val score: Int,
    val movesRemaining: Int,
    val totalMatchedWordsCount: Int
)
