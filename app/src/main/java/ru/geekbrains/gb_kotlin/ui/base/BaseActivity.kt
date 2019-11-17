package ru.geekbrains.gb_kotlin.ui.base

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.firebase.ui.auth.AuthUI
import ru.geekbrains.gb_kotlin.R
import ru.geekbrains.gb_kotlin.data.errors.NoAuthException

abstract class BaseActivity<T, S : BaseViewState<T>> : AppCompatActivity(){

    companion object{
        private const val RC_SING_IN = 42
    }

    abstract val viewModel: BaseViewModel<T, S>
    abstract val layoutRes: Int?

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        layoutRes?.let { setContentView(it) }
        viewModel.getViewState().observe(this, Observer<S> {
            it ?: return@Observer
            it.error?.let {
                renderError(it)
                return@Observer
            }
            renderData(it.data)
        })
    }

    protected fun renderError(error: Throwable) = error?.let {
        when (error){
            is NoAuthException -> startLogin()
            else -> it.message?.let { message -> showError(message) }
        }
        it.message?.let { message -> showError(message) }
    }

    private fun startLogin() {
        val providers = listOf(
            AuthUI.IdpConfig.GoogleBuilder().build(), AuthUI.IdpConfig.EmailBuilder().build()
        )
        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setLogo(R.drawable.android_robot)
                .setTheme(R.style.LoginStyle)
                .setAvailableProviders(providers)
                .build()
                , RC_SING_IN
        )
    }

    abstract fun renderData(data: T)

    protected fun showError(message: String) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

    @SuppressLint("MissingSuperCall")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == RC_SING_IN && resultCode != Activity.RESULT_OK){
            finish()
        }
    }

}
