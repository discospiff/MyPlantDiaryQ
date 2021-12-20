package edu.uc.jonesbr.myplantdiary

import edu.uc.jonesbr.myplantdiary.ui.main.MainViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

@JvmField
val appModule = module {
    viewModel { MainViewModel() }
}