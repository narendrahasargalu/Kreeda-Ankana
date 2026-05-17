package com.kreeda.ankana.data.local

import androidx.room.TypeConverter
import com.kreeda.ankana.data.model.ChallengeStatus
import com.kreeda.ankana.data.model.Sport

class Converters {
    @TypeConverter fun sportToString(s: Sport): String = Sport.encode(s)
    @TypeConverter fun sportFromString(s: String): Sport = Sport.decode(s)

    @TypeConverter fun statusToString(s: ChallengeStatus): String = s.name
    @TypeConverter fun statusFromString(s: String): ChallengeStatus =
        runCatching { ChallengeStatus.valueOf(s) }.getOrDefault(ChallengeStatus.OPEN)
}
