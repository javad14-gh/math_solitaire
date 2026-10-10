package com.math.solitaire.presentation.util

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

object LocaleHelper {

    fun isPersian(context: Context): Boolean {
        return context.packageName.contains("persian", ignoreCase = true) ||
                (try {
                    val buildConfigClass = Class.forName("com.math.solitaire.BuildConfig")
                    val flavorField = buildConfigClass.getField("FLAVOR")
                    val flavor = flavorField.get(null) as? String
                    flavor == "bazaar" || flavor == "myket"
                } catch (e: Exception) {
                    false
                })
    }

    fun toPersianDigits(text: String): String =
        text.map { char ->
            when (char) {
                '0' -> '۰'
                '1' -> '۱'
                '2' -> '۲'
                '3' -> '۳'
                '4' -> '۴'
                '5' -> '۵'
                '6' -> '۶'
                '7' -> '۷'
                '8' -> '۸'
                '9' -> '۹'
                else -> char
            }
        }.joinToString("")

    fun formatNumber(number: Int, isPersian: Boolean): String =
        if (isPersian) toPersianDigits(number.toString()) else number.toString()

    fun formatNumber(text: String, isPersian: Boolean): String =
        if (isPersian) toPersianDigits(text) else text

    // Localized Strings
    fun playButton(isPersian: Boolean): String =
        if (isPersian) "شروع بازی" else "PLAY"

    fun levelTitle(level: Int, isPersian: Boolean): String =
        if (isPersian) "مرحله ${formatNumber(level, true)}" else "LEVEL $level"

    fun difficulty(diff: String, isPersian: Boolean): String =
        if (!isPersian) diff else when (diff) {
            "Easy" -> "آسان"
            "Medium" -> "متوسط"
            "Hard" -> "سخت"
            "VeryHard" -> "خیلی سخت"
            else -> diff
        }

    fun remainingMovesTitle(isPersian: Boolean): String =
        if (isPersian) "حرکات باقی‌مانده" else "MOVES LEFT"

    fun emptySlot(isPersian: Boolean): String =
        if (isPersian) "خالی" else "Empty"

    fun settingsTitle(isPersian: Boolean): String =
        if (isPersian) "تنظیمات" else "Settings"

    fun privacyPolicy(isPersian: Boolean): String =
        if (isPersian) "حریم خصوصی" else "Privacy Policy"

    fun storeTitle(isPersian: Boolean): String =
        if (isPersian) "فروشگاه" else "Store"

    fun storeHeader(isPersian: Boolean): String =
        if (isPersian) "فروشگاه و جوایز" else "STORE & REWARDS"

    fun removeAdsTitle(isPersian: Boolean): String =
        if (isPersian) "حذف تبلیغات" else "REMOVE ADS"

    fun removeAdsDesc(isPersian: Boolean): String =
        if (isPersian) "حذف دائمی تمام تبلیغات درون بازی!\u200F" else "Permanently remove all ads from the game!"

    fun removeAdsPrice(isPersian: Boolean): String =
        if (isPersian) "۳۹,۰۰۰ تومان" else "$1.99"

    fun activeStatus(isPersian: Boolean): String =
        if (isPersian) "فعال" else "ACTIVE"

    fun coinPacksTitle(isPersian: Boolean): String =
        if (isPersian) "بسته‌های سکه" else "COIN PACKS"

    fun freeCoinsTitle(isPersian: Boolean): String =
        if (isPersian) "سکه رایگان" else "FREE COINS"

    fun freeCoinsDesc(isPersian: Boolean): String =
        if (isPersian) "با تماشای ویدیوی کوتاه ۵۰ سکه هدیه بگیرید!\u200F" else "Watch a short video and earn 50 Coins!"

    fun watchAndEarn(isPersian: Boolean): String =
        if (isPersian) "تماشا و دریافت" else "WATCH & EARN"

    fun freeBadge(isPersian: Boolean): String =
        if (isPersian) "رایگان" else "Free"

    fun freeCoinsRewardText(isPersian: Boolean): String =
        if (isPersian) "+۵۰ سکه" else "+50 Coins"

    // Coin Packs
    fun pack1Title(isPersian: Boolean): String = if (isPersian) "کیسه سکه" else "Coin Pouch"
    fun pack1Price(isPersian: Boolean): String = if (isPersian) "۱۹,۰۰۰ تومان" else "$0.99"

    fun pack2Title(isPersian: Boolean): String = if (isPersian) "صندوقچه سکه" else "Coin Chest"
    fun pack2Price(isPersian: Boolean): String = if (isPersian) "۴۹,۰۰۰ تومان" else "$2.49"

    fun pack3Title(isPersian: Boolean): String = if (isPersian) "گنجینه سکه" else "Treasure Vault"
    fun pack3Price(isPersian: Boolean): String = if (isPersian) "۹۹,۰۰۰ تومان" else "$4.99"

    // Gameplay Dialogs
    fun outOfMovesTitle(isPersian: Boolean): String =
        if (isPersian) "فرصت‌ها تمام شد!\u200F" else "Out of Moves!"

    fun outOfMovesPrompt(isPersian: Boolean): String =
        if (isPersian) "آیا می‌خواهید با ۷۵ سکه، ۵ حرکت دیگر دریافت کنید؟\u200F" else "Spend 75 Coins to get 5 extra moves?"

    fun giveUp(isPersian: Boolean): String =
        if (isPersian) "انصراف" else "Give Up"

    fun getMoreMoves(isPersian: Boolean): String =
        if (isPersian) "+۵ حرکت بگیر" else "+5 Moves"

    fun categoryCompleted(isPersian: Boolean): String =
        if (isPersian) "دسته تکمیل شد!\u200F" else "Target Complete!"

    fun nextLevel(isPersian: Boolean): String =
        if (isPersian) "مرحله بعدی" else "Next Level"

    fun retry(isPersian: Boolean): String =
        if (isPersian) "تلاش مجدد" else "Retry"

    fun mainMenu(isPersian: Boolean): String =
        if (isPersian) "منوی اصلی" else "Main Menu"

    fun victoryTitle(isPersian: Boolean): String =
        if (isPersian) "عالی بود!\u200F" else "Awesome!"

    fun victorySubtitle(level: Int, isPersian: Boolean): String =
        if (isPersian) "مرحله ${formatNumber(level, true)} تکمیل شد\u200F" else "Level $level Completed"

    fun defeatTitle(isPersian: Boolean): String =
        if (isPersian) "فرصت‌ها تمام شد!\u200F" else "Out of Moves!"

    fun defeatSubtitle(level: Int, isPersian: Boolean): String =
        if (isPersian) "فرصت‌های شما در مرحله ${formatNumber(level, true)} به پایان رسید.\u200F" else "You ran out of moves on level $level."

    fun continueForCoins(isPersian: Boolean): String =
        if (isPersian) "ادامه بازی (+۱۵ فرصت)" else "Continue (+15 Moves)"

    fun dailyRewardTitle(isPersian: Boolean): String =
        if (isPersian) "جایزه ورود روزانه" else "Daily Reward"

    fun dailyRewardSubtitle(cycle: Int, day: Int, isPersian: Boolean): String =
        if (isPersian) "دوره ${formatNumber(cycle, true)} • روز ${formatNumber(day, true)} از ۳۰" else "Cycle $cycle • Day $day of 30"

    fun claim(isPersian: Boolean): String =
        if (isPersian) "دریافت هدیه" else "Claim"

    fun claim2x(isPersian: Boolean): String =
        if (isPersian) "۲ برابر با ویدیو 🎬" else "Claim 2x 🎬"

    fun comeBackTomorrow(isPersian: Boolean): String =
        if (isPersian) "فردا بازگردید!" else "Come Back Tomorrow!"

    fun dayLabel(day: Int, isPersian: Boolean): String =
        if (isPersian) "روز ${formatNumber(day, true)}" else "Day $day"

    fun milestoneChestLabel(isPersian: Boolean): String =
        if (isPersian) "صندوقچه هدیه" else "Special Chest"

    fun claimedText(isPersian: Boolean): String =
        if (isPersian) "دریافت شد" else "Claimed"

    fun doubleReward(isPersian: Boolean): String =
        if (isPersian) "۲ برابر کردن پاداش" else "Double Reward"

    fun rewardDoubled(isPersian: Boolean): String =
        if (isPersian) "✓ پاداش ۲ برابر دریافت شد" else "✓ Reward Doubled"

    fun restartDialogTitle(isPersian: Boolean): String =
        if (isPersian) "شروع مجدد مرحله" else "Restart Level"

    fun restartDialogPrompt(isPersian: Boolean): String =
        if (isPersian) "شروع مجدد اول رایگان بود! برای شروع دوباره این مرحله یکی از روش‌های زیر را انتخاب کنید:" else "First restart was free! Choose an option to restart this level:"

    fun restartWithCoinsBtn(cost: Int, isPersian: Boolean): String =
        if (isPersian) "پرداخت ${formatNumber(cost, true)} سکه 🪙" else "Pay $cost Coins 🪙"

    fun restartWithAdBtn(isPersian: Boolean): String =
        if (isPersian) "تماشای ویدیو 🎬 (رایگان)" else "Watch Video 🎬 (Free)"

    fun continuePlayingBtn(isPersian: Boolean): String =
        if (isPersian) "ادامه بازی" else "Keep Playing"

    fun exitDialogTitle(isPersian: Boolean): String =
        if (isPersian) "خروج از مرحله" else "Exit Level"

    fun exitDialogPrompt(isPersian: Boolean): String =
        if (isPersian) "آیا می‌خواهید از این مرحله خارج شوید؟" else "Do you want to exit this level?"

    fun exitDialogDesc(isPersian: Boolean): String =
        if (isPersian) "پیشرفت کلی و سکه‌های شما ذخیره است." else "Your overall progress and coins are saved."

    fun exitStayBtn(isPersian: Boolean): String =
        if (isPersian) "ادامه بازی" else "Continue"

    fun exitLeaveBtn(isPersian: Boolean): String =
        if (isPersian) "خروج" else "Exit"
}

@Composable
fun rememberAppFont(): FontFamily {
    val context = LocalContext.current
    val isPersian = remember(context) { LocaleHelper.isPersian(context) }
    return remember(isPersian) {
        com.math.solitaire.presentation.ui.theme.getAppFontFamily(isPersian)
    }
}
