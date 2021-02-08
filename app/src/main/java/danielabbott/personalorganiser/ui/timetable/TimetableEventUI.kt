package danielabbott.personalorganiser.ui.timetable

import danielabbott.personalorganiser.data.TimetableEvent

class TimetableEventUI(
    var colour: Int,
    var e: TimetableEvent,
    val day: Int,
    val hasNotes: Boolean,
    val hasImages: Boolean,

    // includes this object
    var allDays: ArrayList<TimetableEventUI>? = null
) {
    // ui_x and ui_y are relative to the top left of the timetable cells (add startX/Y to get canvas posiiton)

    var uiX: Float = 0.0f
    var uiY: Float = 0.0f
    var uiW: Float = 0.0f
    var uiH: Float = 0.0f
}
