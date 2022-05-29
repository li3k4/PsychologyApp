package com.game.app.containers.home.fragments.note.categories.deprecated

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.asLiveData
import androidx.recyclerview.widget.RecyclerView
import com.game.app.constants.data.value.CategoryTitleConstants
import com.game.app.containers.base.BaseFragment
import com.game.app.containers.home.adapters.CategoryHighAdapterV
import com.game.app.containers.home.models.HomeViewModel
import com.game.app.data.CategoryPreferences
import com.game.app.data.content.NoteCoursePreferences
import com.game.app.data.courses.CourseChildPreferences
import com.game.app.data.courses.CourseHomePreferences
import com.game.app.databinding.FragmentCategoryChildVBinding
import com.game.app.models.category.CategoryModel
import com.game.app.models.content.ContentDataModel
import com.game.app.models.course.CourseDataModel
import com.game.app.models.course.CourseItemModel
import com.game.app.models.course.CourseNoteDataModel
import com.game.app.network.apis.UserApi
import com.game.app.repositories.UserRepository
import com.game.app.utils.recycler.setAdapter
import com.google.gson.Gson
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking


class CategoryChildVFragment : BaseFragment<HomeViewModel, FragmentCategoryChildVBinding, UserRepository>(){
    private lateinit var noteCoursePreferences: NoteCoursePreferences
    private lateinit var courseChildPreferences: CourseChildPreferences
    private lateinit var courseHomePreferences: CourseHomePreferences
    private lateinit var categoryPreferences: CategoryPreferences
    private lateinit var courseAdapterV: CategoryHighAdapterV

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        noteCoursePreferences = NoteCoursePreferences(requireContext())
        courseChildPreferences = CourseChildPreferences(requireContext())
        courseHomePreferences = CourseHomePreferences(requireContext())
        categoryPreferences = CategoryPreferences(requireContext())
        courseAdapterV = CategoryHighAdapterV(requireContext(), categories = arrayListOf())

        binding.recyclerViewChildV.setAdapter(
            requireContext(),
            courseAdapterV,
            RecyclerView.VERTICAL
        )

        noteCoursePreferences.courses.asLiveData().observe(viewLifecycleOwner){
            if(it != null){
                val data = Gson().fromJson(
                    it,
                    CourseNoteDataModel::class.java
                )

                if((data == null) || (data.coursesNotes!!.isEmpty())){
                    return@observe
                }

                val coursesChild = runBlocking {
                    courseChildPreferences.courses.first()
                }

                val coursesHome = runBlocking {
                    courseHomePreferences.courses.first()
                }

                val coursesChildData = Gson().fromJson(
                    coursesChild,
                    CourseDataModel::class.java
                )

                val coursesHomeData = Gson().fromJson(
                    coursesHome,
                    CourseDataModel::class.java
                )

                val courses = arrayListOf<CourseItemModel?>()

                if((coursesChildData != null)
                    && (coursesChildData.courses != null)
                    && (coursesChildData.courses.isNotEmpty())){
                    courses.addAll(coursesChildData.courses)
                }

                if((coursesHomeData != null)
                    && (coursesHomeData.courses != null)
                    && (coursesHomeData.courses.isNotEmpty())){
                    courses.addAll(coursesHomeData.courses)
                }

                if(courses.isNotEmpty()){
                    val categories = Gson().fromJson(
                        runBlocking {
                            categoryPreferences.categories.first()
                        },
                        CategoryModel::class.java
                    )

                    val categoryId = categories.categories[
                            categories.categories.indexOfFirst { it ->
                                it.title == CategoryTitleConstants.CATEGORY_CHILD_TITLE
                            }
                    ].id

                    val coursesAdapter = data.coursesNotes!!.map{ it ->
                        val findObj = it
                        val index = courses.indexOfFirst { it ->
                            (it!!.id == findObj.coursesId) && (it.categoryId == categoryId)
                        }

                        if(index >= 0){
                            val course = courses[index]

                            return@map ContentDataModel(
                                id = course!!.id,
                                categoryId = course.categoryId,
                                subcategoryId = course.subcategoryId,
                                title = course.title,
                                count = course.sounds.size,
                                subscription = course.subscribe,
                                description = course.description,
                                titleImgPath = course.titleImgPath,
                                type = course.type
                            )
                        }else{
                            null
                        }
                    } as ArrayList<ContentDataModel?>

                    coursesAdapter.removeIf { it ->
                        it == null
                    }

                    courseAdapterV.setCategories(
                        coursesAdapter.map { it ->
                            return@map ContentDataModel(
                                id = it!!.id,
                                categoryId = it.categoryId,
                                subcategoryId = it.subcategoryId,
                                title = it.title,
                                count = it.count,
                                subscription = it.subscription,
                                description = it.description,
                                titleImgPath = it.titleImgPath,
                                type = it.type
                            )
                        } as ArrayList<ContentDataModel>
                    )
                }else{
                    courseAdapterV.setCategories(arrayListOf())
                }
            }
        }
    }

    override fun getViewModel() = HomeViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentCategoryChildVBinding.inflate(inflater, container, false)

    override fun getFragmentRepository(): UserRepository {
        return UserRepository(remoteDataSource.buildApi(UserApi::class.java, userPreferences))
    }
}