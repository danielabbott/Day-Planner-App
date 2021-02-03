package danielabbott.personalorganiser.ui.timetable

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.withTranslation
import androidx.core.view.GestureDetectorCompat
import androidx.fragment.app.FragmentManager
import danielabbott.personalorganiser.ColourFunctions
import danielabbott.personalorganiser.Notifications
import danielabbott.personalorganiser.R
import danielabbott.personalorganiser.data.DB
import danielabbott.personalorganiser.data.Settings
import kotlin.math.*

class TimetableView : View, GestureDetector.OnGestureListener {

    @SuppressLint("AnimatorKeep")
    var startX = 0.0f

    @SuppressLint("AnimatorKeep")
    var startY = 0.0f
    var events: ArrayList<TimetableEventUI> = ArrayList<TimetableEventUI>()
    var timetableId: Long = -1

    private var pFragmentManager: FragmentManager? = null
    private var columnHeadersHeight = 0
    private var rowHeadersWidth = 0
    private var rowHeight = 0.0f
    private var columnWidth = 0.0f
    private var mDetector: GestureDetectorCompat
    private var minStartX = 0.0f
    private var minStartY = 0.0f

    // Higher value = zoomed in more
    var zoomValueX = 1.0f
    var zoomValueY = 1.0f

    fun setParentFragmentManager(pfm: FragmentManager) {
        pFragmentManager = pfm
    }

    constructor(context: Context) : super(context) {
        mDetector = GestureDetectorCompat(context, this)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        mDetector = GestureDetectorCompat(context, this)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        mDetector = GestureDetectorCompat(context, this)
    }

    private fun validateValues() {
        if (java.lang.Float.isNaN(startX)) {
            startX = 0.0f
        }
        if (java.lang.Float.isNaN(startY)) {
            startY = 0.0f
        }
        if (java.lang.Float.isNaN(zoomValueX)) {
            zoomValueX = 1.0f
        }
        if (java.lang.Float.isNaN(zoomValueY)) {
            zoomValueY = 1.0f
        }
    }

    private val linePaint = Paint()
    private val textPaint = TextPaint()
    private val egTextBounds = Rect()
    private val rectPaint: Paint = Paint()
    private val circlePaint: Paint = Paint()
    private val wednesdayBounds = Rect()
    private var cameraDrawable: Drawable? = null

    override fun onDraw(canvas: Canvas) {
        validateValues()

        val startHour = Settings.getTimetableStartHour(context)
        val endHour = Settings.getTimetableEndHour(context)
        val totalHours = endHour - startHour
        val fontSize = Settings.getTimetableFontSize(context)


        linePaint.style = Paint.Style.STROKE
        linePaint.color = 0xff000000.toInt()
        linePaint.isAntiAlias = false

        var strokeWidth = max(
            2.0f,
            resources.displayMetrics.density * 1.8f * Settings.getTimetableLineWidth(context!!)
        ).toInt()
        if (strokeWidth % 2 != 0) {
            strokeWidth += 1
        }
        linePaint.strokeWidth = strokeWidth.toFloat()

        textPaint.color = 0xff000000.toInt()
        textPaint.isAntiAlias = true
        textPaint.textAlign = Paint.Align.CENTER

        // Match font size to DPI so it is roughly the same physical size on all screens
        textPaint.textSize = fontSize * resources.displayMetrics.density

        // Figure out how big to make the table headers to fit the text

        textPaint.getTextBounds("by", 0, 2, egTextBounds)
        columnHeadersHeight =
            ceil(egTextBounds.height().toFloat() + 6f * resources.displayMetrics.density).toInt()

        textPaint.getTextBounds("44:444", 0, 6, egTextBounds)
        rowHeadersWidth = egTextBounds.width()

        rectPaint.style = Paint.Style.FILL
        rectPaint.isAntiAlias = false


        circlePaint.style = Paint.Style.FILL
        circlePaint.isAntiAlias = true

        // Width and height of columns and rows

        rowHeight = columnHeadersHeight * 6.0f * zoomValueY
        columnWidth = rowHeadersWidth * 2.5f * zoomValueX


        // Size of timetable (only cells, not overlay)
        val contentWidth = 7 * columnWidth
        val contentHeight = totalHours * rowHeight


        // Stop timetable from being dragged off-screen

        if (startX > 0.0f) {
            startX = 0.0f
        }
        if (startY > 0.0f) {
            startY = 0.0f
        }

        minStartX = -contentWidth - rowHeadersWidth + width.toFloat()
        if (startX < minStartX) {
            startX = minStartX
        }

        minStartY = -contentHeight - columnHeadersHeight + height.toFloat()
        if (startY < minStartY) {
            startY = minStartY
        }

        startX = round(startX)
        startY = round(startY)

        // Start drawing

        // Fill white
        canvas.drawRGB(255, 255, 255)

        // Vertical lines
        canvas.translate(startX + rowHeadersWidth, 0.0f)
        for (i in 1..7) {
            val x = ceil(columnWidth * i.toFloat())
            canvas.drawLine(x, columnHeadersHeight.toFloat(), x, height.toFloat(), linePaint)
        }


        // Horizontal lines

        canvas.translate(-startX - rowHeadersWidth, startY + columnHeadersHeight)
        for (i in 0 until totalHours + 1) {
            val y = ceil(rowHeight * i.toFloat())
            canvas.drawLine(rowHeadersWidth.toFloat(), y, width.toFloat(), y, linePaint)
        }

        canvas.translate(startX + rowHeadersWidth, 0.0f)


        // Timetable events


        textPaint.textSize *= 0.9f

        events.forEach {

            // Rectangle


            it.ui_x = ceil(columnWidth * it.day.toFloat()) + strokeWidth * 0.5f
            it.ui_y =
                ceil(rowHeight * (it.e.startTime / 60.0f - startHour.toFloat())) + strokeWidth * 0.5f

            val nextX = ceil(columnWidth * (it.day.toFloat() + 1)) + strokeWidth * 0.5f
            val nextY =
                ceil(rowHeight * ((it.e.startTime + it.e.duration) / 60.0f - startHour.toFloat())) + strokeWidth * 0.5f

            it.ui_w = (nextX - it.ui_x) - strokeWidth
            it.ui_h = (nextY - it.ui_y) - strokeWidth

            val visible = it.ui_x + startX <= width && it.ui_x + startX + it.ui_w >= 0
                    && it.ui_y + startY <= height && it.ui_y + startY + it.ui_h >= 0


            if (visible) {
                rectPaint.color = ColourFunctions.lightenRGB(it.colour) or 0xff000000.toInt()
                canvas.drawRect(it.ui_x, it.ui_y, it.ui_x + it.ui_w, it.ui_y + it.ui_h, rectPaint)


                // Text

                canvas.save() // push default clip area to stack
                canvas.clipRect(it.ui_x, it.ui_y, it.ui_x + it.ui_w, it.ui_y + it.ui_h)



                if (it.hasNotes) {
                    // 3 dots
                    val scale = resources.displayMetrics.density * 0.5f
                    var x = it.ui_x + it.ui_w - 20 * scale
                    val y = it.ui_y + 12 * scale
                    rectPaint.color = 0xff000000.toInt()
                    canvas.drawCircle(x, y, 5 * scale, circlePaint)
                    x -= 15 * scale
                    canvas.drawCircle(x, y, 5 * scale, circlePaint)
                    x -= 15 * scale
                    canvas.drawCircle(x, y, 5 * scale, circlePaint)
                }

                if (it.hasImages && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    val scale = ceil(resources.displayMetrics.density).toInt()
                    if (cameraDrawable == null) {
                        cameraDrawable = ResourcesCompat.getDrawable(
                            context!!.resources,
                            R.drawable.ic_camera,
                            null
                        )
                        cameraDrawable!!.setTint(0xff000000.toInt())
                    }

                    cameraDrawable!!.setBounds(
                        it.ui_x.toInt() + scale,
                        it.ui_y.toInt() + scale,
                        it.ui_x.toInt() + scale + scale * 16,
                        it.ui_y.toInt() + scale + scale * 16
                    )
                    cameraDrawable!!.draw(canvas)
                }

                val staticLayout = StaticLayout(
                    it.e.name,
                    textPaint,
                    columnWidth.toInt() - (4 * resources.displayMetrics.density).toInt(),
                    Layout.Alignment.ALIGN_NORMAL,
                    1.0f,
                    0.0f,
                    true
                )


                val y =
                    if (staticLayout.height - 6 > (rowHeight * (it.e.duration / 60.0f)).toInt()) {
                        it.ui_y + (2 * resources.displayMetrics.density).toInt()
                    } else {
                        it.ui_y + rowHeight * (it.e.duration / 60.0f) * 0.5f - staticLayout.height / 2
                    }

                canvas.withTranslation(
                    round(it.ui_x + columnWidth / 2),
                    round(y)
                ) {
                    staticLayout.draw(canvas)
                }


                canvas.restore() // pop default clip area back off the stack

                // Top and bottom lines for event (to separate events that are between hours)

                canvas.drawLine(
                    it.ui_x,
                    it.ui_y - strokeWidth * 0.5f,
                    it.ui_x + it.ui_w,
                    it.ui_y - strokeWidth * 0.5f,
                    linePaint
                )

                canvas.drawLine(
                    it.ui_x,
                    it.ui_y + it.ui_h + strokeWidth * 0.5f,
                    it.ui_x + it.ui_w,
                    it.ui_y + it.ui_h + strokeWidth * 0.5f,
                    linePaint
                )
            }
        }

        // Reset translation
        canvas.translate(-startX - rowHeadersWidth, -startY - columnHeadersHeight)




        textPaint.textSize /= 0.9f
        textPaint.textAlign = Paint.Align.CENTER


        // Times on left hand side (overlaid, stays in place when scrolling)


        rectPaint.color = 0xffffffff.toInt()
        canvas.drawRect(0.0f, 0.0f, rowHeadersWidth.toFloat(), height.toFloat(), rectPaint)

        for (i in 0 until totalHours) {
            val y = startY + columnHeadersHeight + rowHeight * i.toFloat()

            if (y < -egTextBounds.height() + columnHeadersHeight) {
                continue
            }

            canvas.drawText(
                (startHour + i).toString() + ":00",
                rowHeadersWidth * 0.5f,
                y,
                textPaint
            )

            if (y >= height) {
                break
            }
        }


        // Days of the week

        val days =
            arrayOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
        val days2 = arrayOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        var daysToUse = days

        canvas.drawRect(0.0f, 0.0f, width.toFloat(), columnHeadersHeight.toFloat(), rectPaint)

        // Decide whether to use short-form dates or not depending on whether
        // wednesday (the day with the longest name) fits in the space
        textPaint.getTextBounds(days[2], 0, days[2].length, wednesdayBounds)

        if (wednesdayBounds.width() > columnWidth) {
            daysToUse = days2
        }

        for (i in 1..7) {
            val x = startX + rowHeadersWidth + columnWidth * i.toFloat()

            if (x < -columnWidth) {
                continue
            }

            canvas.drawText(
                daysToUse[i - 1],
                x - columnWidth * 0.5f,
                columnHeadersHeight - textPaint.descent() * 1.5f,
                textPaint
            )

            if (x > width) {
                break
            }
        }

        canvas.drawRect(
            0.0f,
            0.0f,
            rowHeadersWidth.toFloat(),
            columnHeadersHeight.toFloat(),
            rectPaint
        )
        canvas.drawLine(
            rowHeadersWidth.toFloat(),
            0.0f,
            rowHeadersWidth.toFloat(),
            height.toFloat(),
            linePaint
        )
        canvas.drawLine(
            0.0f,
            columnHeadersHeight.toFloat(),
            width.toFloat(),
            columnHeadersHeight.toFloat(),
            linePaint
        )


    }

    override fun onShowPress(e: MotionEvent?) {
    }

    // Timetable was tapped. Go to edit timetable event page.
    override fun onSingleTapUp(event: MotionEvent?): Boolean {
        if (event == null) {
            return true
        }

        val startHour = Settings.getTimetableStartHour(context)

        var tEvent: TimetableEventUI? = null

        // Search for event that was tapped
        events.forEach {
            if (event.x >= it.ui_x + startX + rowHeadersWidth && event.x < it.ui_x + startX + rowHeadersWidth + it.ui_w
                && event.y >= it.ui_y + startY + columnHeadersHeight && event.y < it.ui_y + startY + columnHeadersHeight + it.ui_h
            ) {
                tEvent = it
            }
        }


        var startTime = 0
        var endTime = 0
        var day = 0

        if (tEvent == null) {
            // No event was tapped, figure out the day and start/end times of the area that was tappd

            // Inverse of calculation done when drawing the timetable
            day = ((event.x - startX - rowHeadersWidth) / columnWidth).toInt()
            val clickedTime =
                ((((event.y - startY - columnHeadersHeight) / rowHeight) + startHour.toFloat()) * 60.0f).toInt()

            startTime = clickedTime - (clickedTime % 60)
            endTime = startTime + 60

            events.forEach {
                if (it.day == day
                    && it.e.startTime + it.e.duration < clickedTime && it.e.startTime + it.e.duration > startTime
                ) {
                    startTime = it.e.startTime + it.e.duration
                } else if (it.day == day
                    && it.e.startTime > clickedTime && it.e.startTime < endTime
                ) {
                    endTime = it.e.startTime
                }

            }

            if (endTime - startTime < 10) {
                // Time window is tiny. Just ignore the tap.
                return true
            }
        }

        val fragment =
            TimetableEditEventFragment(timetableId, tEvent?.e?.id, startTime, endTime, 1 shl day)


        validateValues()
        Settings.setTimetablePosition(context!!, startX, startY)
        Settings.setTimetableZoom(context!!, zoomValueX, zoomValueY)

        val fragmentTransaction = pFragmentManager!!.beginTransaction()
        fragmentTransaction.replace(R.id.fragmentView, fragment)
            .addToBackStack(null)
            .commit()
        return true
    }

    private var zooming = false

    override fun onDown(e: MotionEvent?): Boolean {
        return true
    }

    private var scrollTime: Long = 0
    private var animatorX: ObjectAnimator? = null
    private var animatorY: ObjectAnimator? = null

    // When the user swipes their finger fast and lifts off
    // The timetable keeps scrolling for a short while after the finger leaves the screen
    @SuppressLint("AnimatorKeep")
    override fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent?,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        if (zooming) {
            // If the user is doing a zoom guesture then any 'fling's are invalid
            return true
        }

        // Length of time that timetable will keep moving for (in seconds)
        // The longer the time the more distance is moved
        val time = 0.3f

        // The calculations below use the equations for linear motion (SUVAT equations)
        // The calculations are done twice - once in the x axis, once in y

        val x = startX + (System.currentTimeMillis() - scrollTime) * 0.001f * velocityX
        val distX = velocityX * time * 0.5f // Total distance the timetable will move in the x axis
        val accelerationX =
            (-distX * 2.0f) / (time * time) // How fast it slows down (value is negative)
        animatorX = ObjectAnimator.ofFloat(this, "startX", x, x + distX).apply {
            duration = (time * 1000.0f).toLong()
            setInterpolator {
                // it is between 0 and 1
                // t is between 0 and time
                val t = it * time
                val s = accelerationX * t * t * 0.5f + velocityX * t

                // Convert distance to animator time value
                s / distX
            }
            addUpdateListener {
                invalidate()
            }
            start()
        }
        val y = startY + (System.currentTimeMillis() - scrollTime) * 0.001f * velocityY
        val distY = velocityY * time * 0.5f
        val accelerationY = (-distY * 2.0f) / (time * time)
        animatorY = ObjectAnimator.ofFloat(this, "startY", y, y + distY).apply {
            duration = (time * 1000.0f).toLong()
            setInterpolator {
                val t = it * time
                val s = accelerationY * t * t * 0.5f + velocityY * t
                s / distY
            }
            addUpdateListener {
                invalidate()
            }
            start()
        }

        return true
    }

    override fun onScroll(
        event1: MotionEvent,
        event2: MotionEvent,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        if (zooming) {
            return true
        }

        // User can pan in 8 directions

        if (abs(abs(distanceX) - abs(distanceY)) / max(
                abs(distanceX),
                abs(distanceY)
            ) < 0.5f
        ) {
            // Diagonal movement
            val d = (abs(distanceX) + abs(distanceY)) * 0.5f
            startX -= d * sign(distanceX)
            startY -= d * sign(distanceY)
        } else {
            // Movement along an axis
            if (abs(distanceX) > abs(distanceY)) {
                startX -= distanceX
            } else {
                startY -= distanceY
            }
        }



        scrollTime = System.currentTimeMillis()

        invalidate()
        return true
    }

    override fun onLongPress(event: MotionEvent?) {
        if (event == null) {
            return
        }

        val startHour = Settings.getTimetableStartHour(context)

        var tEvent: TimetableEventUI? = null

        // Search for event that was tapped
        events.forEach {
            if (event.x >= it.ui_x + startX + rowHeadersWidth && event.x < it.ui_x + startX + rowHeadersWidth + it.ui_w
                && event.y >= it.ui_y + startY + columnHeadersHeight && event.y < it.ui_y + startY + columnHeadersHeight + it.ui_h
            ) {
                tEvent = it
            }
        }

        if (tEvent == null) {
            return
        }

        var days = 0

        for (day in 0..6) {
            if ((tEvent!!.e.days and (1 shl day)) != 0) {
                days += 1
            }
        }

        var name = tEvent!!.e.name
        if (name.length > 20) {
            name = name.substring(0, 17) + "..."
        }

        val showDeleteDialog = {
            val msg = if(name.trim().isEmpty())
                "Are you sure you want to delete this event? This cannot be undone."
                else "Are you sure you want to delete the event '$name'? This cannot be undone."

            AlertDialog.Builder(context)
                .setTitle("Delete event")
                .setMessage(msg)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Delete") { _, _ ->
                    Notifications.unscheduleNotificationsForTTEvent(context!!, tEvent!!.e.id)
                    DB.deleteTimetableEvent(tEvent!!.e.id)

                    if (days == 1) {
                        events.remove(tEvent)
                    } else {
                        val toDelete = tEvent!!.allDays!!
                        toDelete.forEach {
                            it.allDays = null
                            events.remove(it)
                        }
                    }


                    invalidate()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        if (days == 1) {
            showDeleteDialog()
        } else {
            val title = if(name.trim().isEmpty())
                "Actions for event"
                else "Actions for '$name'"
            AlertDialog.Builder(context)
                .setTitle(title)
                .setItems(
                    arrayOf("Split", "Delete")
                ) { _, which ->
                    if (which == 0) {
                        // SPLIT

                        val newEvent = DB.splitTTEvent(tEvent!!.e.id, tEvent!!.day)
                        tEvent!!.allDays!!.remove(tEvent!!)
                        tEvent!!.allDays = null
                        tEvent!!.e = newEvent

                    } else {
                        // DELETE
                        showDeleteDialog()
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

    }


    private var fingerStartZoomDistanceX = 0.0f
    private var fingerStartZoomDistanceY = 0.0f

    // Set when the second finger touches the screen
    private var finger2Id = 0

    private var zoomXOld = 0.0f
    private var zoomYOld = 0.0f
    private var startXOld = 0.0f
    private var startYOld = 0.0f

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event == null) {
            return true
        }
        mDetector.onTouchEvent(event)
        if (zooming) {
            val finger2Index = event.findPointerIndex(finger2Id)
            if ((event.actionMasked == MotionEvent.ACTION_POINTER_UP &&
                        (event.actionIndex == finger2Index || event.actionIndex == 0))
                || event.actionMasked == MotionEvent.ACTION_UP
            ) {

                // back to scrolling

                zooming = false
                invalidate()
            } else if (event.actionMasked == MotionEvent.ACTION_MOVE) {
                // A finger moved

                val finger1X = event.x
                val finger1Y = event.y
                val finger2X = event.getX(finger2Index)
                val finger2Y = event.getY(finger2Index)


                val newFingerDistanceX = abs(finger2X - finger1X)
                val newFingerDistanceY = abs(finger2Y - finger1Y)


                val zoomDeltaX = (newFingerDistanceX - fingerStartZoomDistanceX) * 0.001f
                val zoomDeltaY = (newFingerDistanceY - fingerStartZoomDistanceY) * 0.001f

                if (abs(zoomDeltaX) > abs(zoomDeltaY)) {
                    zoomValueX = zoomXOld + zoomDeltaX
                } else {
                    zoomValueY = zoomYOld + zoomDeltaY
                }



                if (zoomValueX < 0.5f) {
                    zoomValueX = 0.5f
                }
                if (zoomValueY < 0.5f) {
                    zoomValueY = 0.5f
                }

                if (zoomValueX > 1.8f) {
                    zoomValueX = 1.8f
                }
                if (zoomValueY > 1.5f) {
                    zoomValueY = 1.5f
                }

                val newStartY = startYOld * (zoomValueY / zoomYOld)
                val newStartX = startXOld * (zoomValueX / zoomXOld)

                if (zoomValueY >= zoomYOld) {
                    if (newStartY < startY) {
                        startY = newStartY
                    }
                } else {
                    if (newStartY > startY) {
                        startY = newStartY
                    }
                }

                if (zoomValueX >= zoomXOld) {
                    if (newStartX < startX) {
                        startX = newStartX
                    }
                } else {
                    if (newStartX > startX) {
                        startX = newStartX
                    }
                }


                invalidate()

            }
        } else {
            if (event.actionMasked == MotionEvent.ACTION_POINTER_DOWN && !zooming) {
                // Second finger on screen, now we are zooming in/out
                zooming = true

                finger2Id = event.getPointerId(event.actionIndex)

                val finger1X = event.x
                val finger1Y = event.y
                val finger2X = event.getX(event.actionIndex)
                val finger2Y = event.getY(event.actionIndex)

                fingerStartZoomDistanceX = abs(finger2X - finger1X)
                fingerStartZoomDistanceY = abs(finger2Y - finger1Y)

                zoomXOld = zoomValueX
                zoomYOld = zoomValueY
                startXOld = startX
                startYOld = startY

            }
        }
        return true
    }

}

