package ru.geekbrains.gb_kotlin.ui.base

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.firebase.ui.auth.AuthUI
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.consumeEach
import ru.geekbrains.gb_kotlin.R
import ru.geekbrains.gb_kotlin.data.errors.NoAuthException
import kotlin.coroutines.CoroutineContext

abstract class BaseActivity<S> : AppCompatActivity(), CoroutineScope {

    override val coroutineContext: CoroutineContext by lazy {
        Dispatchers.Main + Job()
    }

    companion object{
        private const val RC_SING_IN = 42
    }

    abstract val model: BaseViewModel<S>
    abstract val layoutRes: Int?

    private lateinit var dataJob: Job
    private lateinit var errorJob: Job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        layoutRes?.let {
            setContentView(it)
        }
    }

    override fun onStart() {
        super.onStart()

        dataJob = launch {
            model.getViewState().consumeEach {
                renderData(it)
            }
        }

        errorJob = launch {
            model.getErrorChannel().consumeEach {
                renderError(it)
            }
        }
    }

    private fun renderError(error: Throwable) = error?.let {
        when (error){
            is NoAuthException -> startLogin()
            else -> it.message?.let { message -> showError(message) }
        }
    }

    override fun onStop() {
        super.onStop()
        dataJob.cancel()
        errorJob.cancel()
    }


    override fun onDestroy() {
        super.onDestroy()
        coroutineContext.cancel()
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

    abstract fun renderData(data: S)

    private fun showError(message: String) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

    @SuppressLint("MissingSuperCall")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == RC_SING_IN && resultCode != Activity.RESULT_OK){
            finish()
        }
    }
}
