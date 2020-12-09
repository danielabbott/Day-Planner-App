package danielabbott.personalorganiser.data

class ToDoListTask(
    var id: Long,
    var dateTime: Long?,
    var hasTime: Boolean,
    var name: String,
    var notes: String?,
    var remindOnTime: Boolean,
    var remind30Mins: Boolean,
    var remind1Hr: Boolean,
    var remind2Hrs: Boolean,
    var remindMorning: Boolean,
    var repeat: Repeat,
    var goal_id: Long?,

    // Set when data is loaded from database, ignored when storing in database
    var goal_colour: Int? = null
)