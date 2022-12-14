package com.example.timer.fragment.sequencelist

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.timer.R
import com.example.timer.adapter.SequenceAdapter
import com.example.timer.databinding.FragmentSequenceListBinding
import com.example.timer.model.Sequence
import com.example.timer.fragment.timer.utils.PREFS_TIMER


class SequenceListFragment : Fragment() {

    private var _binding: FragmentSequenceListBinding? = null
    private val binding get() = _binding!!

    private lateinit var recyclerView: RecyclerView

    private val sequenceListViewModel: SequenceListViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSequenceListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)
        val timerAdapter = SequenceAdapter(requireContext(), sequenceListViewModel)
        recyclerView.adapter = timerAdapter

        binding.floatingActionButton.setOnClickListener{
            val action = SequenceListFragmentDirections.actionSequenceListFragmentToEditSequenceFragment(
                Sequence(
                    0,
                    "Static title",
                    Color.WHITE,
                    8,
                    7,
                    1,
                    7,
                    1,
                    false
                ),
                true
            )
            findNavController().navigate(action)
        }

        sequenceListViewModel.allTimers.observe(viewLifecycleOwner) { timerList ->
            timerAdapter.setData(timerList)
        }


        val sharedPrefs: SharedPreferences = requireContext().getSharedPreferences(PREFS_TIMER, Context.MODE_PRIVATE)
//        if (sharedPrefs.contains(KEY_SEQUENCE)){
//            Log.d("init", "init2")
//
//            val json = sharedPrefs.getString(KEY_SEQUENCE, "")
//            val sequence = Gson().fromJson(json, Sequence::class.java)
//            val action = SequenceListFragmentDirections.actionSequenceListFragmentToTimerFragment(
//                sequence
//            )
//            findNavController().navigate(action)
//        }
//        else{
//            Toast.makeText(context, "No", Toast.LENGTH_SHORT).show()
//
//        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.sequence_list_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.button_settings -> {
                val action = SequenceListFragmentDirections.actionSequenceListFragmentToSettingsFragment()
                findNavController().navigate(action)
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}