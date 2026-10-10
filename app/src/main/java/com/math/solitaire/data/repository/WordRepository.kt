package com.math.solitaire.data.repository

import android.content.Context
import com.math.solitaire.data.model.WordDatabase

/**
 * In the Math Solitaire app, content is generated algorithmically by [LevelGenerator].
 * This repository is kept for compatibility but returns an empty database —
 * the ViewModel initializes without waiting for any file load.
 */
class WordRepository {

    fun loadDatabase(context: Context): WordDatabase {
        // No JSON file needed — all math content is generated on the fly
        return WordDatabase(categories = emptyList(), words = emptyList())
    }
}
