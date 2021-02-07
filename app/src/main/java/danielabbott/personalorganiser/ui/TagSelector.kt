package danielabbott.personalorganiser.ui

import android.app.AlertDialog
import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatButton
import danielabbott.personalorganiser.ColourFunctions
import danielabbott.personalorganiser.R
import danielabbott.personalorganiser.data.*

class TagSelector : AppCompatButton {


    enum class SelectedType {
        All, Untagged, Tag
    }

    private var selectedType = SelectedType.All

    private var selectedTag: Tag? = null

    var onItemSelectedListener: ((SelectedType, Tag?) -> Unit)? = null

    fun getSelectedTagID() : Long? {
        return selectedTag?.id
    }

    fun setTag(tagID_: Long) {
        selectedType = SelectedType.Tag
        selectedTag = DB.getTag(tagID_)
        setText()
    }

    fun setTag(tag: Tag) {
        selectedType = SelectedType.Tag
        selectedTag = tag
        setText()
    }

    fun setSelectAll() {
        selectedType = SelectedType.All
        selectedTag = null
        setText()
    }

    fun setSelectUntagged() {
        selectedType = SelectedType.Untagged
        selectedTag = null
        setText()
    }

    fun get() : Pair<SelectedType, Tag?> {
        return Pair(selectedType, selectedTag)
    }

    private fun setText() {
        if(selectedType == SelectedType.All) {
            text = "[All]"
        }
        else if(selectedType == SelectedType.Untagged) {
            text = "[Untagged]"
        }
        else {
            text = selectedTag!!.tag
        }
    }

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init()
    }


    private fun init() {
        setTypeface(null, Typeface.NORMAL)

        setOnClickListener {
            val tagNames = ArrayList<CharSequence>()
            tagNames.add("[All]")
            tagNames.add("[Untagged]")
            val tags = DB.getTags()
            tags.forEach {
                tagNames.add(it.tag)
            }

            AlertDialog.Builder(context)
                .setTitle("Select Tag")
                .setItems(
                    tagNames.toTypedArray()
                ) { _, which ->
                    if(which == 0) {
                        setSelectAll()
                    }
                    else if(which == 1) {
                        setSelectUntagged()
                    }
                    else {
                        setTag(tags[which-2])
                    }
                    if (onItemSelectedListener != null) {
                        onItemSelectedListener!!(selectedType, selectedTag)
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

}