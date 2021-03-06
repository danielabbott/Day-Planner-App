package danielabbott.personalorganiser.data

class TimetableEvent(
    var id: Long,
    var timetable_id: Long,
    var startTime: Int,
    var duration: Int, // In minutes
    var days: Int,
    var name: String,
    var notes: String?,
    var remindOnTime: Boolean,
    var remind30Mins: Boolean,
    var remind1Hr: Boolean,
    var remind2Hrs: Boolean,
    var remindMorning: Boolean,
    var goal_id: Long?,

    // Set when loading multiple timetable events from the DB, ignored when otherwise
    var hasImages: Boolean = false,

    // Set when data is loaded from database, ignored when storing in database
    var goal_colour: Int? = null
)
