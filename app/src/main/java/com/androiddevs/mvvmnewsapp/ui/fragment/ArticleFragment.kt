package com.androiddevs.mvvmnewsapp.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import com.androiddevs.mvvmnewsapp.R

import com.androiddevs.mvvmnewsapp.ui.NewsActivity
import com.androiddevs.mvvmnewsapp.viewmodel.NewsViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_article.*

class ArticleFragment:Fragment(R.layout.fragment_article ) {
    lateinit var viewModel: NewsViewModel


   val args:ArticleFragmentArgs by navArgs()
    val TAG="Article Fragment"
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel= (activity as NewsActivity).viewModel

       val article=args.article
        Log.d(TAG,article.url)

        webView.apply {

            webViewClient= WebViewClient()
            loadUrl(article.url)
        }



        fab.setOnClickListener{

            viewModel.saveArticle(article)
            Snackbar.make(view,"Article saved",Snackbar.LENGTH_LONG).show()
        }



    }
}