package com.example.mcard.repository.source.architecture.view

import androidx.fragment.app.Fragment
import androidx.preference.PreferenceFragmentCompat
import androidx.viewbinding.ViewBinding
import com.example.mcard.repository.di.AppComponent
import com.example.mcard.repository.features.getModuleAppComponent
import com.example.mcard.repository.source.architecture.viewModels.InteractionView
import com.example.mcard.repository.source.architecture.viewModels.LiveViewModel

internal abstract class LiveFragment<T> :
    Fragment(), StructView, InteractionView {

    abstract val viewModel: LiveViewModel<T>

    abstract val viewBinding: ViewBinding

    val appComponent: AppComponent by lazy {
        requireActivity().getModuleAppComponent()
    }
}

internal abstract class LivePreferenceFragment<T> :
    PreferenceFragmentCompat(), StructView, InteractionView {

    abstract val viewModel: LiveViewModel<T>

    val appComponent: AppComponent by lazy {
        requireActivity().getModuleAppComponent()
    }
}