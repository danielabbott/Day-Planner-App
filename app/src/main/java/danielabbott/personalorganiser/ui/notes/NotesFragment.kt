package danielabbott.personalorganiser.ui.notes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import danielabbott.personalorganiser.MainActivity
import danielabbott.personalorganiser.R
import danielabbott.personalorganiser.data.DB
import danielabbott.personalorganiser.data.Settings
import danielabbott.personalorganiser.ui.SpinnerChangeDetector

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
            adapter = NoteRecyclerViewAdapter(
                DB.getNotesPreviews(if (selected < 0) null else selected),
                fragmentManager!!, activity!!
            )
        }

        var tags = ArrayList<String>()
        var tagIDs = ArrayList<Long>()
        tags.add("All")
        DB.getTags().forEach {
            tags.add(it.tag)
            tagIDs.add(it.id)
        }

        val tagSelect = view.findViewById<Spinner>(R.id.tagSelect)

        tagSelect.adapter = ArrayAdapter<String>(
            context!!,
            android.R.layout.simple_spinner_dropdown_item,
            tags
        )

        val id = Settings.getSelectedTagID(context!!)

        if (id > 0) {
            var i: Int = 1
            for (id_ in tagIDs) {
                if (id_ == id) {
                    tagSelect.setSelection(i)
                    break
                }
                i += 1
            }
        }



        tagSelect.onItemSelectedListener = SpinnerChangeDetector {
            Settings.setSelectedTagID(
                context!!,
                if (tagSelect.selectedItemPosition != 0) tagIDs[tagSelect.selectedItemPosition - 1]
                else -1
            )

            val fragmentTransaction = fragmentManager!!.beginTransaction()
            fragmentTransaction.replace(R.id.fragmentView, NotesFragment())
            fragmentTransaction.commit()
        }


        // On click listener for the add (+) button
        view.findViewById<FloatingActionButton>(R.id.fab_new).setOnClickListener {
            val fragment = EditNoteFragment(null)
            val fragmentTransaction = fragmentManager!!.beginTransaction()
            fragmentTransaction.replace(R.id.fragmentView, fragment).addToBackStack(null)
            fragmentTransaction.commit()
        }


        (activity as MainActivity).setToolbarTitle("Notes")

        return view
    }
}