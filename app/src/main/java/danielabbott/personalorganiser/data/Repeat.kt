package danielabbott.personalorganiser.data

enum class Repeat(val n: Int) {
    NEVER(0), DAILY(1), EVERY_OTHER_DAY(2), WEEKLY(3), MONTHLY(4);

    fun days(): Int =
        when (this) {
            NEVER -> -9999
            DAILY -> 1
            EVERY_OTHER_DAY -> 2
            WEEKLY -> 7
            MONTHLY -> -9999
        }

    override fun toString(): String {
        return arrayOf(
            "Don't Repeat",
            "Repeat Daily",
            "Repeat Every other day",
            "Repeat Weekly",
            "Repeat Monthly"
        )[n]
    }

    companion object {
        fun fromInt(value: Int) = values().first { it.n == value }

    }
}