package com.franckrj.jva.forum

import android.app.Application
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.MutableLiveData
import android.text.SpannableString
import com.franckrj.jva.R
import com.franckrj.jva.pagenav.NavigablePageViewModel
import com.franckrj.jva.utils.LoadableValue

class ForumPageViewModel(app: Application) : NavigablePageViewModel(app) {
    private val infosForForumPage: MutableLiveData<LoadableValue<ForumPageInfos?>?> = MutableLiveData()
    private val listOfTopicsShowable: MediatorLiveData<LoadableValue<List<TopicInfosShowable>>> = MediatorLiveData()

    fun cancelGetForumPageInfos() {
        //TODO
    }

    fun clearListOfTopicsShowable() {
        listOfTopicsShowable.value = LoadableValue.loaded(ArrayList())
    }

    fun clearInfosForForumPage() {
        infosForForumPage.value = null
    }

    fun getInfosForForumPage() : LiveData<LoadableValue<ForumPageInfos?>?> = infosForForumPage

    fun getListOfTopicsShowable(): LiveData<LoadableValue<List<TopicInfosShowable>>?> = listOfTopicsShowable

    /* Ne récupère les informations que si aucun topic n'est actuellement affiché ni en cours de chargement. */
    fun getForumPageInfosIfNeeded(formatedTopicUrl: String) {
        val realListOfTopicsShowable: LoadableValue<List<TopicInfosShowable>>? = listOfTopicsShowable.value
        if (realListOfTopicsShowable == null || (realListOfTopicsShowable.value.isEmpty() && realListOfTopicsShowable.status != LoadableValue.STATUS_LOADING)) {
            listOfTopicsShowable.value = LoadableValue.loaded(listOf(
                    TopicInfosShowable(SpannableString("mfr"), SpannableString("xd"), SpannableString("arrr"), getApplication<Application>().getDrawable(R.drawable.smiley_1)),
                    TopicInfosShowable(SpannableString("mfr"), SpannableString("xd"), SpannableString("arrr"), getApplication<Application>().getDrawable(R.drawable.smiley_8)),
                    TopicInfosShowable(SpannableString("mfr"), SpannableString("xd"), SpannableString("arrr"), getApplication<Application>().getDrawable(R.drawable.smiley_3)),
                    TopicInfosShowable(SpannableString("mfr"), SpannableString("xd"), SpannableString("arrr"), getApplication<Application>().getDrawable(R.drawable.smiley_4)),
                    TopicInfosShowable(SpannableString("mfr"), SpannableString("xd"), SpannableString("arrr"), getApplication<Application>().getDrawable(R.drawable.smiley_5)),
                    TopicInfosShowable(SpannableString("mfr"), SpannableString("xd"), SpannableString("arrr"), getApplication<Application>().getDrawable(R.drawable.smiley_6)),
                    TopicInfosShowable(SpannableString("mfr"), SpannableString("xd"), SpannableString("arrr"), getApplication<Application>().getDrawable(R.drawable.smiley_7))
            ))
        }
    }
}
