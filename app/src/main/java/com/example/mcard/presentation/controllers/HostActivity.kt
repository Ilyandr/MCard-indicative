package com.example.mcard.presentation.controllers

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.view.doOnLayout
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import by.kirich1409.viewbindingdelegate.CreateMethod
import by.kirich1409.viewbindingdelegate.viewBinding
import com.example.mcard.domain.factories.viewModels.SupportHostViewModelFactory
import com.example.mcard.domain.viewModels.HostViewModel
import com.example.mcard.presentation.support.setHostActivityActions
import com.example.mcard.presentation.controllers.basic.diverse.DrawerController
import com.example.mcard.R
import com.example.mcard.databinding.HostActivityBinding
import com.example.mcard.repository.features.dataNumberAction
import com.example.mcard.repository.features.getModuleAppComponent
import com.example.mcard.repository.source.usage.DrawerUsageSource
import com.example.mcard.repository.source.architecture.view.StructView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.yoomoney.sdk.gui.utils.extensions.hide
import ru.yoomoney.sdk.gui.utils.extensions.show
import javax.inject.Inject

internal class HostActivity : AppCompatActivity(), StructView {

    @Inject
    lateinit var viewModelFactory: SupportHostViewModelFactory

    private val viewModel: HostViewModel by viewModels {
        viewModelFactory.create(getModuleAppComponent())
    }

    val navController: NavController by lazy {
        (supportFragmentManager.findFragmentById(
            R.id.fragmentContainerView
        ) as NavHostFragment).navController
    }

    private val drawerView: DrawerController by lazy {
        DrawerController(
            this.viewBinding.root,
            this.viewBinding.drawerContent,
            this.viewBinding.content.root
        )
    }

    private val viewBinding by viewBinding(
        viewBindingClass = HostActivityBinding::class.java,
        createMethod = CreateMethod.INFLATE
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)
        basicActions()
    }

    internal fun changeVisibleBottomNavBar(isVisible: Boolean) {
        if (isVisible)
            viewBinding.content.bottomAppBar.show()
        else
            viewBinding.content.bottomAppBar.hide()
    }

    internal inline fun requireHeightBottomNavBar(
        crossinline beforeAction: dataNumberAction,
    ) =
        viewBinding.content.bottomAppBar.doOnLayout {
            beforeAction.invoke(it.height)
        }

    internal fun navigateToHome() {
        viewBinding.content.bottomNavBar.selectedItemId =
            R.id.basicFragment
    }

    override fun basicActions() {
        getModuleAppComponent() inject this
        viewModel registerReceivers this

        changeVisibleBottomNavBar(false)

        viewBinding.setHostActivityActions(
            viewModel, navController, drawerView
        )
    }

    internal fun requireDrawerImpl(): DrawerUsageSource =
        this.drawerView

    internal fun actionBackStackCurrentFragment() =
        lifecycleScope.launch(Dispatchers.Main) {
            navController.popBackStack()
        }
}