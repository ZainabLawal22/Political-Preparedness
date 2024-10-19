package com.example.android.politicalpreparedness.election

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.databinding.FragmentVoterInfoBinding

class VoterInfoFragment : Fragment() {

    private val args: VoterInfoFragmentArgs by navArgs()

    // DONE: Add ViewModel values and create ViewModel
    private val viewModel: VoterInfoViewModel by lazy {
        ViewModelProvider(this, viewModelFactory)[VoterInfoViewModel::class.java]
    }

    // Delegating the ViewModelFactory using lazy as well
    private val viewModelFactory by lazy {
        VoterInfoViewModelFactory(requireNotNull(activity).application)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // DONE: Add binding values
        val binding: FragmentVoterInfoBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_voter_info,
            container,
            false
        )

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        // Fetch election data
        val election = args.argElection
        viewModel.getElection(election.id)

        // Conditionally fetch voter info based on the election division
        val divisionInfo = if (election.division.state.isEmpty()) {
            election.division.country
        } else {
            "${election.division.country} - ${election.division.state}"
        }

        viewModel.getVoterInfo(election.id, divisionInfo)

        // Observe URL changes and handle intent
        viewModel.url.observe(viewLifecycleOwner) { url ->
            url?.let {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(it))
                startActivity(intent)
            }
        }

        return binding.root
    }
}
