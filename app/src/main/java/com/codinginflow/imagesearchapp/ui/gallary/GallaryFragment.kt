package com.codinginflow.imagesearchapp.ui.gallary

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import com.codinginflow.imagesearchapp.R
import com.codinginflow.imagesearchapp.data.UnsplashPhoto
import com.codinginflow.imagesearchapp.databinding.FragmentGallaryBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GallaryFragment : Fragment(R.layout.fragment_gallary), UnsplashAdapter.onItemClickListener {
    private val TAG = "GallaryFragment"
    private val viewModel by viewModels<GallaryViewModel>()
    private var _binding: FragmentGallaryBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentGallaryBinding.bind(view)
        val unsplashAdapter = UnsplashAdapter(this)
        binding.apply {
            recyclerView.apply {
                itemAnimator=null
                setHasFixedSize(true)
//                adapter = unsplashAdapter
                adapter = unsplashAdapter.withLoadStateHeaderAndFooter(
                    header = UnsplashPhotoLoadStateAdapter {
                        unsplashAdapter.retry()
                    },
                    footer = UnsplashPhotoLoadStateAdapter {
                        unsplashAdapter.retry()
                    }
                )
            }
            buttonRetry.setOnClickListener {
                unsplashAdapter.retry()
            }
        }
        viewModel.photos.observe(viewLifecycleOwner) {
            unsplashAdapter.submitData(viewLifecycleOwner.lifecycle, it)
        }
        unsplashAdapter.addLoadStateListener { loadState ->
            binding.apply {
                progressBar.isVisible = loadState.source.refresh is LoadState.Loading
                recyclerView.isVisible = loadState.source.refresh is LoadState.NotLoading
                textViewError.isVisible = loadState.source.refresh is LoadState.Error
                buttonRetry.isVisible = loadState.source.refresh is LoadState.Error

                //for empty view
                if (loadState.source.refresh is LoadState.NotLoading
                    && loadState.append.endOfPaginationReached
                    && unsplashAdapter.itemCount < 1
                ) {
                    recyclerView.isVisible = false
                    textViewEmpty.isVisible = true

                } else {
                    textViewEmpty.isVisible = false
                }

            }
        }
        setHasOptionsMenu(true)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onItemClick(photo: UnsplashPhoto) {
        //This is compile time safe than just passing them in bundle
        val action = GallaryFragmentDirections.actionGallaryFragmentToDetailFragment(photo)
        findNavController().navigate(action)

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_gallary, menu)
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    binding.recyclerView.scrollToPosition(0)
                    viewModel.searchPhotos(query)
                    searchView.clearFocus()
                }
                return true
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                return true
            }

        })
    }


}