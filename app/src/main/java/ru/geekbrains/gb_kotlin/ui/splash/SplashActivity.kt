package ru.geekbrains.gb_kotlin.ui.splash

import androidx.lifecycle.ViewModelProviders
import ru.geekbrains.gb_kotlin.ui.base.BaseActivity
import ru.geekbrains.gb_kotlin.ui.main.MainActivity

class SplashActivity : BaseActivity<Boolean?, SplashViewState>() {
    override val viewModel by lazy {
        ViewModelProviders.of(this).get(SplashViewModel::class.java)
    }

    override val layoutRes: Int? = null

    override fun renderData(data: Boolean?) {
        data?.takeIf { it }?.let {
            MainActivity.start(this)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.requestUser()
    }

}