package danielabbott.personalorganiser.ui.notes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import danielabbott.personalorganiser.MainActivity
import danielabbott.personalorganiser.R
import danielabbott.personalorganiser.data.DB
import danielabbott.personalorganiser.data.Settings
import danielabbott.personalorganiser.data.Tag
import danielabbott.personalorganiser.ui.TagSelector

class NotesFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_notes_list, container, false)

        val recyclerView = view.findViewById<RecyclerView>(R.id.list)
        with(recyclerView) {
            layoutManager = LinearLayoutManager(context)
            val selected = Settings.getSelectedTagID(context!!)
            var previews =
                if (selected == -2L) DB.getNotesPreviewsUntagged() else (DB.getNotesPreviews(if (selected < 0) null else selected))
            if (previews.isEmpty() && selected != -1L) {
                Settings.setSelectedTagID(context!!, -1)
                previews = DB.getNotesPreviews(null)
            }
            adapter = NoteRecyclerViewAdapter(
                previews,
                fragmentManager!!, activity!!
            )
        }

        val tagSelect = view.findViewById<TagSelector>(R.id.tagSelect)

        val id = Settings.getSelectedTagID(context!!)

        if (id == -1L) {
            tagSelect.setSelectAll()
        } else if (id == -2L) {
            tagSelect.setSelectUntagged()
        } else {
            tagSelect.setTag(id)
        }


        tagSelect.onItemSelectedListener = { selectType, tag ->
            Settings.setSelectedTagID(
                context!!,
                if (selectType == TagSelector.SelectedType.All) {
                    -1
                } else if (selectType == TagSelector.SelectedType.Untagged) {
                    -2
                } else {
                    tag!!.id
                }
            )

            val fragmentTransaction = fragmentManager!!.beginTransaction()
            fragmentTransaction.replace(R.id.fragmentView, NotesFragment())
            fragmentTransaction.commit()
        }


        // On click listener for the add (+) button
        view.findViewById<FloatingActionButton>(R.id.fab_new).setOnClickListener {
            val x = tagSelect.get()
            var tagsAutoAdded: ArrayList<Tag>? = null
            if (x.first == TagSelector.SelectedType.Tag) {
                tagsAutoAdded = ArrayList()
                tagsAutoAdded.add(x.second!!)
            }
            val fragment = EditNoteFragment(null, null, tagsAutoAdded)
            val fragmentTransaction = fragmentManager!!.beginTransaction()
            fragmentTransaction.replace(R.id.fragmentView, fragment).addToBackStack(null)
            fragmentTransaction.commit()
        }


        (activity as MainActivity).setToolbarTitle("Notes")

        return view
    }
}