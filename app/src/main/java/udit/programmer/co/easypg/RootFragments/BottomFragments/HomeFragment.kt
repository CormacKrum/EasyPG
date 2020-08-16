package udit.programmer.co.easypg.RootFragments.BottomFragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import udit.programmer.co.easypg.ChatActivities.ChatActivity
import udit.programmer.co.easypg.R

class HomeFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val fab: FloatingActionButton = requireActivity().findViewById(R.id.root_fab)
        fab.setOnClickListener {
            requireActivity().startActivity(Intent(requireContext(), ChatActivity::class.java))
        }
    }

}