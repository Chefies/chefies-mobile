package com.fransbudikashira.chefies.ui.main.history

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.fransbudikashira.chefies.data.factory.RecipeViewModelFactory
import com.fransbudikashira.chefies.data.local.entity.HistoryEntity
import com.fransbudikashira.chefies.data.model.MLResultModel
import com.fransbudikashira.chefies.databinding.FragmentHistoryBinding
import com.fransbudikashira.chefies.ui.adapter.ListRecipeAdapter
import com.fransbudikashira.chefies.ui.result.ResultActivity
import com.fransbudikashira.chefies.util.await
import kotlinx.coroutines.launch

class HistoryFragment : Fragment() {
    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private val historyViewModel: HistoryViewModel by viewModels {
        RecipeViewModelFactory.getInstance(requireContext())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpRecyclerView()

        historyViewModel.getHistories().observe(viewLifecycleOwner){
            setHistoryRecipe(it)
        }

    }

    private fun setUpRecyclerView() {
        val layoutManager = LinearLayoutManager(requireContext())
        binding.rvSaveRecipe.layoutManager = layoutManager
        binding.rvSaveRecipe.setHasFixedSize(true)
    }

    private fun setHistoryRecipe(histories: List<HistoryEntity>?) {

        val adapter = ListRecipeAdapter()
        adapter.submitList(histories)
        binding.rvSaveRecipe.adapter = adapter

        adapter.setOnItemClickback(object : ListRecipeAdapter.OnItemClickCallback {
            override fun onItemClicked(history: HistoryEntity) {
                viewLifecycleOwner.lifecycleScope.launch {
                    val recipeBahasa = historyViewModel.getAllRecipeBahasaById(history.id!!).await()
                    val recipeEnglish = historyViewModel.getAllRecipeEnglishById(history.id).await()

                    val bundle = MLResultModel(
                        historyEntity = history,
                        recipeBahasaEntity = recipeBahasa!!,
                        recipeEnglishEntity = recipeEnglish!!
                    )

                    val intent = Intent(requireActivity(), ResultActivity::class.java).apply {
                        putExtra(ResultActivity.EXTRA_RESULT, bundle)
                    }
                    startActivity(intent)
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "HistoryFragment"
    }
}