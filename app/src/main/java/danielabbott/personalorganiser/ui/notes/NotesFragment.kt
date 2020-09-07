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
import danielabbott.personalorganiser.MainActivity
import danielabbott.personalorganiser.R
import danielabbott.personalorganiser.data.DB
import danielabbott.personalorganiser.data.NotePreview
import danielabbott.personalorganiser.data.Tag

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
            adapter = NoteRecyclerViewAdapter(DB.getNotesPreviews(null), fragmentManager!!, activity!!)
        }

        var tags = ArrayList<String>()
        tags.add("All")
        DB.getTags().forEach {
            tags.add(it.tag)
        }

        view.findViewById<Spinner>(R.id.tagSelect).adapter = ArrayAdapter<String>(
            context!!,
            android.R.layout.simple_spinner_dropdown_item,
            tags
        )
        // TODO add functionality to spinner

        (activity as MainActivity).setToolbarTitle("Notes")

        return view
    }
}