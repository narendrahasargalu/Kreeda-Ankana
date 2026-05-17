package com.kreeda.ankana.data.model

/**
 * A sport played on the village ground.
 *
 * Predefined sports live in [Sports.predefined]; villagers can also enter their
 * own (e.g. a regional game). Custom sports use a synthetic id of "custom:<name>".
 */
data class Sport(
    val id: String,
    val displayName: String,
    val emoji: String
) {
    val isCustom: Boolean get() = id.startsWith(CUSTOM_PREFIX)

    companion object {
        const val CUSTOM_PREFIX = "custom:"
        private const val SEP = "|"

        /** Encode for storage in a single string column. */
        fun encode(s: Sport): String = "${s.id}$SEP${s.displayName}$SEP${s.emoji}"

        /** Decode a stored value. Falls back gracefully for legacy enum-name strings. */
        fun decode(stored: String): Sport {
            if (SEP in stored) {
                val parts = stored.split(SEP, limit = 3)
                if (parts.size == 3) return Sport(parts[0], parts[1], parts[2])
            }
            return Sports.byLegacyName(stored) ?: Sports.default
        }

        fun custom(name: String): Sport {
            val clean = name.trim()
            return Sport(id = "$CUSTOM_PREFIX${clean.lowercase()}", displayName = clean, emoji = "🎽")
        }
    }
}

/** Predefined sports available on the picker. */
object Sports {
    val CRICKET = Sport("cricket", "Cricket", "🏏")
    val VOLLEYBALL = Sport("volleyball", "Volleyball", "🏐")
    val FOOTBALL = Sport("football", "Football", "⚽")
    val KABADDI = Sport("kabaddi", "Kabaddi", "🤼")
    val KHO_KHO = Sport("kho_kho", "Kho Kho", "🏃")
    val BADMINTON = Sport("badminton", "Badminton", "🏸")
    val BASKETBALL = Sport("basketball", "Basketball", "🏀")
    val HOCKEY = Sport("hockey", "Hockey", "🏑")
    val TENNIS = Sport("tennis", "Tennis", "🎾")
    val TABLE_TENNIS = Sport("table_tennis", "Table Tennis", "🏓")
    val CHESS = Sport("chess", "Chess", "♟️")
    val CARROM = Sport("carrom", "Carrom", "🎯")

    val predefined: List<Sport> = listOf(
        CRICKET, VOLLEYBALL, FOOTBALL, KABADDI, KHO_KHO, BADMINTON,
        BASKETBALL, HOCKEY, TENNIS, TABLE_TENNIS, CHESS, CARROM
    )

    val default: Sport = CRICKET

    private val byId: Map<String, Sport> = predefined.associateBy { it.id }

    /** Map old enum-name strings (pre-migration) to current sports. */
    internal fun byLegacyName(name: String): Sport? = when (name.uppercase()) {
        "CRICKET" -> CRICKET
        "VOLLEYBALL" -> VOLLEYBALL
        else -> byId[name]
    }
}
