package danielabbott.personalorganiser.ui.notes

import android.app.Activity
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import danielabbott.personalorganiser.R
import danielabbott.personalorganiser.data.NotePreview

class NoteRecyclerViewAdapter(
    private val values: List<NotePreview>,
    private val parentFragmentManager: FragmentManager,
    private val activity: Activity
) : RecyclerView.Adapter<NoteRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_notes, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]

        // No more than 2 newline characters in a row
        val s1 = item.contents.replace(Regex("\n\n\n+"), "\n\n")

        // Limit to 7 lines

        var s2 = ""
        var lines = 0

        for (c in s1) {
            if(c == '\n') {
                lines++;

                if(lines > 7) {
                    break
                }
            }
            s2 += c

            if(lines > 7) {
                break
            }
        }

        holder.contentView.text = s2
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val contentView: TextView = view.findViewById(R.id.content)
    }
}