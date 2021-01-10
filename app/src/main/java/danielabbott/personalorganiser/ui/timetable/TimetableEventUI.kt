package danielabbott.personalorganiser.ui.timetable

import danielabbott.personalorganiser.data.TimetableEvent

class TimetableEventUI(
    var colour: Int,
    val e: TimetableEvent,
    val day: Int,
    val hasNotes: Boolean
) {
    // ui_x and ui_y are relative to the top left of the timetable cells (add startX/Y to get canvas posiiton)

    var ui_x: Float = 0.0f
    var ui_y: Float = 0.0f
    var ui_w: Float = 0.0f
    var ui_h: Float = 0.0f
}
