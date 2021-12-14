package com.androiddevs.mvvmnewsapp.ui.fragment

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AbsListView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.androiddevs.mvvmnewsapp.R
import com.androiddevs.mvvmnewsapp.adapters.NewsAdapter
import com.androiddevs.mvvmnewsapp.ui.NewsActivity
import com.androiddevs.mvvmnewsapp.util.Constans
import com.androiddevs.mvvmnewsapp.util.Constans.Companion.SEARCH_NEWS_DELAY_TIME
import com.androiddevs.mvvmnewsapp.util.Resource
import com.androiddevs.mvvmnewsapp.viewmodel.NewsViewModel
import kotlinx.android.synthetic.main.fragment_breaking_news.*

import kotlinx.android.synthetic.main.fragment_search_news.*
import kotlinx.android.synthetic.main.fragment_search_news.paginationProgressBar
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class SearchNewsFragment:Fragment(R.layout.fragment_search_news) {

    lateinit var viewModel: NewsViewModel
    lateinit var searchAdapter:NewsAdapter
    val TAG="Braking News fragment"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel= (activity as NewsActivity).viewModel

        viewModel=(activity as NewsActivity ).viewModel

        setUpRecyclerViw()

        searchAdapter.setOnItemClickListener{

            var bundle=Bundle()
            bundle.apply {

                putSerializable("article",it)
            }
            findNavController().navigate(
                R.id.action_searchNewsFragment_to_articleFragment,
                bundle

            )
        }


        var job: Job?=null

        etSearch.addTextChangedListener {editable->

            job?.cancel()

            job= MainScope().launch {
                    delay(SEARCH_NEWS_DELAY_TIME)
                    editable?.let {

                        if (editable.toString().isNotEmpty())
                        {
                            viewModel.searchNew(editable.toString())
                        }
                    }
            }
        }



        viewModel.searchNews.observe(viewLifecycleOwner, Observer { response->
            when(response) {

                is Resource.Success->{
                    hideProgressbar()
                    response.data?.let {newsResponse ->

                            searchAdapter.differ.submitList(newsResponse.articles.toList())
                        val totalPages=newsResponse.totalResults/Constans.QUERY_PAGE_SIZE+2
                        isLastPage=viewModel.searchNewPage==totalPages
                        if (isLastPage)
                        {
                            rvSearchNews.setPadding(0,0,0,0)
                        }
                    }
                }

                is Resource.Error->{
                    hideProgressbar()
                    response.message?.let{ message->

                        Log.e(TAG,"An error occured")
                        Toast.makeText(activity,"An error occured:$message", Toast.LENGTH_SHORT).show()
                    }
                }

                is Resource.Loading->{

                    showProgressbar()

                }

            }
        })


    }
    private fun hideProgressbar()
    {
        paginationProgressBar.visibility=View.INVISIBLE
        isLoading=false

    }
    private fun showProgressbar()
    {
        paginationProgressBar.visibility=View.VISIBLE
        isLoading=true

    }
    var isLoading=false
    var isLastPage=false
    var isScrolling=false

    val scrollListener=object : RecyclerView.OnScrollListener(){
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)

            if(newState== AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL)
            {
                isScrolling=true
            }
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            val layoutManager=recyclerView.layoutManager as LinearLayoutManager
            val firstVisibleItemPosition=layoutManager.findFirstVisibleItemPosition()
            val visibleItemCount=layoutManager.childCount
            val totalItemCount=layoutManager.itemCount

            val isNotLoadingAndNotLastPage=!isLoading&&!isLastPage
            val isAtLastItem=firstVisibleItemPosition+visibleItemCount>=totalItemCount
            val isNotAtBigging=firstVisibleItemPosition>=0
            val isTotalMoreThanVisible=totalItemCount>= Constans.QUERY_PAGE_SIZE

            val shouldPaginate=isNotLoadingAndNotLastPage&&
                    isAtLastItem&&
                    isNotAtBigging&&
                    isTotalMoreThanVisible&&isScrolling

            if(shouldPaginate)
            {
                viewModel.searchNew(etSearch.text.toString())
                isScrolling=false
            }

        }
    }
    private fun setUpRecyclerViw()
    {
        searchAdapter= NewsAdapter()
        rvSearchNews.apply {

            adapter=searchAdapter
            layoutManager=LinearLayoutManager(activity)
            addOnScrollListener(this@SearchNewsFragment.scrollListener)

        }
    }


}