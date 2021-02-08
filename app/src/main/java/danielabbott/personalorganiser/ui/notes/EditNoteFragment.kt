package danielabbott.personalorganiser.ui.notes

import android.os.Bundle
import android.view.*
import android.widget.*
import danielabbott.personalorganiser.MainActivity
import danielabbott.personalorganiser.R
import danielabbott.personalorganiser.data.DB
import danielabbott.personalorganiser.data.Note
import danielabbott.personalorganiser.data.Settings
import danielabbott.personalorganiser.data.Tag
import danielabbott.personalorganiser.ui.DataEntryFragmentBasic

// text_from_share is only used if noteId == null. It is the text provided by a share intent
class EditNoteFragment(
    private val noteId: Long? = null,
    private val text_from_share: String? = null,
    private var tagsToAdd: ArrayList<Tag>? = null
) :
    DataEntryFragmentBasic() {


    private lateinit var textArea: EditText
    private lateinit var tagsll: LinearLayout


    private var tags = ArrayList<Tag>()
    private var newTags = ArrayList<Tag>() // tags to be added to the note (tag IDs are -1)
    private var tagsToRemove = ArrayList<Tag>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        DB.init(context!!)
        val root = inflater.inflate(R.layout.fragment_edit_note, container, false)
        (activity!! as MainActivity).setToolbarTitle("Edit Note")

        textArea = root.findViewById(R.id.textArea)
        tagsll = root.findViewById(R.id.tagsll)

        var originalNote: Note? = null

        if (noteId == null) {
            if (text_from_share != null) {
                textArea.setText(text_from_share)
            }

            tagsToAdd?.forEach {
                newTags.add(it)
                addTagTV(it)
            }
        } else {
            try {
                originalNote = DB.getNote(noteId)
            } catch (e: Exception) {
                val fragmentTransaction = fragmentManager!!.beginTransaction()
                fragmentTransaction.replace(R.id.fragmentView, NotesFragment())
                fragmentTransaction.commit()
                return root
            }

            textArea.setText(originalNote.contents)

            tags = originalNote.tags
            tags.forEach {
                addTagTV(it)
            }
        }

        root.findViewById<Button>(R.id.addTag).setOnClickListener {
            DialogAddTag { tagName ->
                var tagAlreadyExists = false
                for (i in 0 until tags.size) {
                    if (tags[i].tag.equals(tagName, ignoreCase = true)) {
                        tagAlreadyExists = true
                        break
                    }
                }
                for (i in 0 until newTags.size) {
                    if (newTags[i].tag.equals(tagName, ignoreCase = true)) {
                        tagAlreadyExists = true
                        break
                    }
                }

                if (!tagAlreadyExists) {
                    var tag: Tag? = null
                    for (i in 0 until tagsToRemove.size) {
                        if (tagsToRemove[i].tag.equals(tagName, ignoreCase = true)) {
                            tag = tagsToRemove[i]
                            tagsToRemove.removeAt(i)
                            tags.add(tag)
                            break
                        }
                    }

                    if (tag == null) {
                        tag = Tag(-1, tagName)
                        newTags.add(tag)
                    }
                    addTagTV(tag)
                }
            }.show(fragmentManager!!, null)
        }

        val save: ImageView = root.findViewById(R.id.save)
        save.setOnClickListener { _ ->
            val notes = textArea.text.toString()

            val e = Note(noteId ?: -1, notes, tags)
            val newNoteId = DB.updateOrCreateNote(e)

            newTags.forEach {
                val tagID = DB.createOrGetTag(it.tag)
                DB.addTagToNote(newNoteId, tagID)
            }

            tagsToRemove.forEach { tagObj ->
                DB.removeTagFromNote(noteId!!, tagObj.id)
            }

            (activity!! as MainActivity).hideKeyboard()

            exitWithoutUnsavedChangesWarning = true
            (activity as MainActivity).onBackPressed()
        }

        anyUnsavedChanges = {
            if (noteId == null) {
                textArea.text.toString().trim().isNotEmpty() ||
                        (tagsToAdd == null && newTags.size > 0)
                (tagsToAdd != null && !tagsAreSame(tagsToAdd!!, newTags))

            } else if (newTags.size > 0) true
            else if (tagsToRemove.size > 0) true
            else originalNote!!.contents.trim() != textArea.text.toString().trim()
        }

        setHasOptionsMenu(true)
        return root
    }

    private fun tagsAreSame(tagsToAdd: ArrayList<Tag>, newTags: ArrayList<Tag>): Boolean {
        if (tagsToAdd.size != newTags.size) {
            return false
        }
        tagsToAdd.forEach { a ->
            var found = false
            for (i in 0 until newTags.size) {
                if (a.tag.equals(newTags[i].tag, ignoreCase = true)) {
                    found = true
                    break
                }
            }
            if (!found) {
                return false
            }
        }
        return true
    }

    private fun addTagTV(tagObj: Tag) {
        val tv = LayoutInflater.from(context!!).inflate(R.layout.tag, tagsll, false) as TextView
        tv.text = tagObj.tag
        tv.setOnLongClickListener {
            // Remove tag
            tagsll.removeView(tv)

            if (tags.contains(tagObj)) {
                tags.remove(tagObj)
                tagsToRemove.add(tagObj)
            } else {
                newTags.remove(tagObj)
            }


            true
        }
        tagsll.addView(tv)
    }

    private lateinit var qieMenuItem: MenuItem
    private lateinit var replaceMenuItem: MenuItem
    private var deleteNoteMenuItem: MenuItem? = null

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()

        val qit = Settings.getQIT(context!!)

        qieMenuItem = menu.add(qit.substring(0, qit.length.coerceAtMost(4)))
        qieMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)


        replaceMenuItem = menu.add("Replace")

        if (noteId != null) {
            deleteNoteMenuItem = menu.add("Delete")
        }

        activity!!.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item == qieMenuItem) {
            textArea.text.insert(textArea.selectionEnd, Settings.getQIT(context!!))
        } else if (item == replaceMenuItem) {
            var find: String? = null
            if (textArea.selectionStart != textArea.selectionEnd) {
                find = textArea.text.toString()
                    .substring(textArea.selectionStart, textArea.selectionEnd)
            }
            DialogReplace(textArea.text.toString(), find) { new_text ->
                textArea.setText(new_text)
            }.show(fragmentManager!!, null)
        } else if (deleteNoteMenuItem != null && item == deleteNoteMenuItem) {
            android.app.AlertDialog.Builder(activity)
                .setTitle("Delete note")
                .setMessage("Are you sure you want to delete this note? This cannot be undone.")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Delete") { _, _ ->
                    DB.deleteNote(noteId!!)

                    // Reload notes page
                    val fragmentTransaction = fragmentManager!!.beginTransaction()
                    fragmentTransaction.replace(R.id.fragmentView, NotesFragment())
                    fragmentTransaction.commit()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
        return super.onOptionsItemSelected(item)
    }
}