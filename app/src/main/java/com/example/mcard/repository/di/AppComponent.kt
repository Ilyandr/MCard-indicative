package com.example.mcard.repository.di

import android.content.Context
import com.example.mcard.domain.viewModels.authorization.ReceptionViewModel
import com.example.mcard.domain.viewModels.authorization.RegistrationViewModel
import com.example.mcard.domain.viewModels.authorization.RestoreViewModel
import com.example.mcard.domain.viewModels.HostViewModel
import com.example.mcard.domain.viewModels.basic.cards.adding.ManuallyCardAddViewModel
import com.example.mcard.domain.viewModels.basic.cards.OpenCardViewModel
import com.example.mcard.domain.viewModels.basic.diverse.BasicViewModel
import com.example.mcard.domain.viewModels.basic.other.LaunchViewModel
import com.example.mcard.domain.viewModels.basic.other.SettingsViewModel
import com.example.mcard.domain.viewModels.features.AdditionallyViewModel
import com.example.mcard.presentation.controllers.authorization.ReceptionFragment
import com.example.mcard.presentation.controllers.authorization.RegistrationFragment
import com.example.mcard.presentation.controllers.authorization.RestoreFragment
import com.example.mcard.presentation.controllers.HostActivity
import com.example.mcard.presentation.controllers.basic.cards.adding.ManuallyCardAddFragment
import com.example.mcard.presentation.controllers.basic.cards.open.GlobalCardFragment
import com.example.mcard.presentation.controllers.basic.cards.open.LocaleCardFragment
import com.example.mcard.presentation.controllers.basic.diverse.BasicFragment
import com.example.mcard.presentation.controllers.basic.diverse.DrawerController
import com.example.mcard.presentation.controllers.basic.diverse.GlobalPlatformFragment
import com.example.mcard.presentation.controllers.basic.other.SettingsFragment
import com.example.mcard.presentation.controllers.features.AdditionallyFragment
import com.example.mcard.repository.di.modules.ModuleFeatures
import com.example.mcard.repository.di.modules.ModuleRest
import com.example.mcard.repository.di.modules.ModuleServices
import com.example.mcard.repository.di.modules.ModuleUI
import com.example.mcard.repository.features.location.dataSource.LocationListenerDataSourse
import com.example.mcard.repository.features.rest.firebase.CaretakerCardsFirebase
import com.example.mcard.repository.features.rest.firebase.FirebaseController
import com.example.mcard.repository.features.rest.firebase.GlobalFirebase
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        ModuleUI::class,
        ModuleServices::class,
        ModuleRest::class,
        ModuleFeatures::class
    ]
)
internal interface AppComponent {

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun contextApplication(context: Context): Builder
        fun build(): AppComponent
    }

    infix fun inject(hostViewModel: HostViewModel)

    infix fun inject(hostActivity: HostActivity)

    infix fun inject(basicFragment: BasicFragment)

    infix fun inject(receptionFragment: ReceptionFragment)

    infix fun inject(registrationFragment: RegistrationFragment)

    infix fun inject(restoreFragment: RestoreFragment)

    infix fun inject(registrationViewModel: RegistrationViewModel)

    infix fun inject(receptionViewModel: ReceptionViewModel)

    infix fun inject(restoreViewModel: RestoreViewModel)

    infix fun inject(launchViewModel: LaunchViewModel)

    infix fun inject(basicViewModel: BasicViewModel)

    infix fun inject(drawerView: DrawerController)

    infix fun inject(firebaseController: FirebaseController)

    infix fun inject(caretakerCardsFirebase: CaretakerCardsFirebase)

    infix fun inject(globalFirebase: GlobalFirebase)

    infix fun inject(manuallyCardAddViewModel: ManuallyCardAddViewModel)

    infix fun inject(manuallyCardAddFragment: ManuallyCardAddFragment)

    infix fun inject(locationListenerDataSourse: LocationListenerDataSourse)

    infix fun inject(openCardViewModel: OpenCardViewModel)

    infix fun inject(localeCardFragment: LocaleCardFragment)

    infix fun inject(localeCardFragment: GlobalCardFragment)

    infix fun inject(globalPlatformFragment: GlobalPlatformFragment)

    infix fun inject(settingsFragment: SettingsFragment)

    infix fun inject(settingsViewModel: SettingsViewModel)

    infix fun inject(additionallyViewModel: AdditionallyViewModel)

    infix fun inject(additionallyFragment: AdditionallyFragment)
}