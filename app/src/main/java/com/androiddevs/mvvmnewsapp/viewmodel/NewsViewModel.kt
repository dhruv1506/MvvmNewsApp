package com.androiddevs.mvvmnewsapp.viewmodel

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.*
import android.net.NetworkCapabilities.*
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androiddevs.mvvmnewsapp.NewsApplication
import com.androiddevs.mvvmnewsapp.models.Article
import com.androiddevs.mvvmnewsapp.models.NewsResponse
import com.androiddevs.mvvmnewsapp.repositry.NewsRepository
import com.androiddevs.mvvmnewsapp.util.Resource
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException
import java.lang.Error

class NewsViewModel(app:Application,val newsRepository: NewsRepository):AndroidViewModel(app) {

    val breakingNews:MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    var breakingNewPage=1
    var breakingNewsResponse:NewsResponse?=null
    val searchNews:MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    var searchNewPage=1
    var searchNewsResponse:NewsResponse?=null

    init {
        getBreakingNews("us")
    }
    fun getBreakingNews(countryCode:String)=viewModelScope.launch {
        /*breakingNews.postValue(Resource.Loading())

        val response= newsRepository.getBreakingNews(countryCode,searchNewPage)

        breakingNews.postValue(handleBreakingNewsResponse(response))
*/

        safeBreakingNewsCall(countryCode)
    }

    fun searchNew(queryString:String)=viewModelScope.launch {
        /*searchNews.postValue(Resource.Loading())
        val response=newsRepository.searchNews(queryString,breakingNewPage)
        searchNews.postValue(handleSearchNewsResponse(response))*/
        safeSearchNewsCall(queryString)

    }


    fun saveArticle(article: Article)=viewModelScope.launch {

        newsRepository.upsert(article)

    }

    fun deleteArticle(article: Article)=viewModelScope.launch {


        newsRepository.deleteArticle(article)
    }

    fun getSavedNews()=newsRepository.savedNews()



    fun handleBreakingNewsResponse(response:Response<NewsResponse>):Resource<NewsResponse>{

        if (response.isSuccessful)
        {
            response.body()?.let {resultRespose->
                breakingNewPage++

                if(breakingNewsResponse==null)
                {
                    breakingNewsResponse=resultRespose
                }
                else
                {
                    val oldArticle= breakingNewsResponse?.articles
                    val newArticle=resultRespose.articles

                    oldArticle?.addAll(newArticle)
                }
                    return Resource.Success(breakingNewsResponse?:resultRespose)
            }
        }

        return Resource.Error(response.message())
    }
    fun handleSearchNewsResponse(response:Response<NewsResponse>):Resource<NewsResponse>{

        if (response.isSuccessful)
        {
            response.body()?.let {resultRespose->

                searchNewPage++
                if (searchNewsResponse==null)
                {

                    searchNewsResponse=resultRespose

                }
                else
                {
                    val oldArticle=searchNewsResponse?.articles
                    val newArticle= resultRespose.articles
                    oldArticle?.addAll(newArticle)
                }

                    return Resource.Success(searchNewsResponse?:resultRespose)

            }
        }

        return Resource.Error(response.message())
    }

    private suspend fun safeSearchNewsCall(queryString: String)
    {
        searchNews.postValue(Resource.Loading())

        try {
            if (hasInternetConnection())
            {
                val response=newsRepository.searchNews( queryString,searchNewPage)
                searchNews.postValue(handleSearchNewsResponse(response))
            }else
            {

                searchNews.postValue(Resource.Error("No Internet Connectivity"))
            }
        }
        catch (t:Throwable)
        {
            when(t)
            {
                is IOException->searchNews.postValue(Resource.Error("Network failure"))
                else->searchNews.postValue(Resource.Error("Conversion Error"))

            }
        }



    }
    private suspend fun safeBreakingNewsCall(countryCode: String)
    {
        breakingNews.postValue(Resource.Loading())

        try {
            if (hasInternetConnection())
            {
                val response=newsRepository.getBreakingNews(countryCode,breakingNewPage)
                breakingNews.postValue(handleBreakingNewsResponse(response))
            }else
            {

                breakingNews.postValue(Resource.Error("No Internet Connectivity"))
            }
        }
        catch (t:Throwable)
        {
            when(t)
            {
                is IOException->breakingNews.postValue(Resource.Error("Network failure"))
                else->breakingNews.postValue(Resource.Error("Conversion Error"))

            }
        }



    }


    private fun hasInternetConnection():Boolean{

        val connectivityManager=getApplication<NewsApplication>().getSystemService(Context.CONNECTIVITY_SERVICE)
                                as ConnectivityManager

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M)
        {
            val activeNetwork=connectivityManager.activeNetwork?:return false
            val capabilities=connectivityManager.getNetworkCapabilities(activeNetwork)?:return false

            return when {
                capabilities.hasTransport(TRANSPORT_WIFI)->true
                capabilities.hasTransport(TRANSPORT_CELLULAR)->true
                capabilities.hasTransport(TRANSPORT_ETHERNET)->true
                else-> false
            }

        }else
        {
            connectivityManager.activeNetworkInfo.run {
                return when(type)
                {
                    TYPE_WIFI ->true
                    TYPE_MOBILE ->true
                    TYPE_ETHERNET->true
                    else->false

                }
            }

        }


        return false
    }

}