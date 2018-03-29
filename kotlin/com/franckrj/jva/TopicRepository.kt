package com.franckrj.jva

import android.arch.lifecycle.MutableLiveData
import android.os.AsyncTask

class TopicRepository private constructor() {
    companion object {
        val instance: TopicRepository by lazy { TopicRepository() }
    }

    private val serviceForWeb: WebService = WebService.instance
    private val parserForTopic: TopicParser = TopicParser.instance

    fun getListOfMessages(): MutableLiveData<ArrayList<MessageInfos>> {
        val listOfMessages: MutableLiveData<ArrayList<MessageInfos>> = MutableLiveData()

        TopicGetter(serviceForWeb, parserForTopic, listOfMessages).execute()

        return listOfMessages
    }
}

private class TopicGetter(private val webServiceToUse: WebService, private val topicParserToUse: TopicParser,
                          private val listOfMessagesLiveData: MutableLiveData<ArrayList<MessageInfos>>) : AsyncTask<Void, Void, ArrayList<MessageInfos>>() {
    override fun doInBackground(vararg voids: Void): ArrayList<MessageInfos> {
        val sourceOfWebPage: String?
        val webInfos: WebService.WebInfos = WebService.WebInfos()
        webInfos.followRedirects = false

        sourceOfWebPage = webServiceToUse.sendRequest("http://www.jeuxvideo.com/forums/42-1000021-50996951-1-0-1-0-actu-un-blabla-est-ne.htm",
                                                      "GET", "", "", webInfos)

        return if (sourceOfWebPage == null) {
            ArrayList()
        } else {
            topicParserToUse.getListOfMessagesFromPageSource(sourceOfWebPage)
        }
    }

    override fun onPostExecute(listOfMessages: ArrayList<MessageInfos>) {
        listOfMessagesLiveData.value = listOfMessages
    }
}
