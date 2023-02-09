package com.xplore.paymobile

import android.app.Application
import com.clearent.idtech.android.wrapper.ClearentWrapper
import com.clearent.idtech.android.wrapper.offline.config.OfflineModeConfig
import com.xplore.paymobile.data.datasource.EncryptedSharedPrefsDataSource
import com.xplore.paymobile.data.datasource.SharedPreferencesDataSource
import com.xplore.paymobile.interactiondetection.AppLifecycleCallbacks
import com.xplore.paymobile.util.Constants
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import java.util.*
import javax.inject.Inject

@HiltAndroidApp
class App : Application() {

    private val clearentWrapper = ClearentWrapper.getInstance()

    @Inject
    lateinit var encryptedPrefs: EncryptedSharedPrefsDataSource

    @Inject
    lateinit var sharedPreferencesDataSource: SharedPreferencesDataSource

    @Inject
    lateinit var appLifecycleCallbacks: AppLifecycleCallbacks

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        logoutWebView()

        encryptedPrefs = EncryptedSharedPrefsDataSource(applicationContext)
        registerActivityLifecycleCallbacks(appLifecycleCallbacks)
        generatePassphrase()
        initSdkWrapper()
    }

    private fun logoutWebView() {
        sharedPreferencesDataSource.setAuthToken(null)
    }

    private fun initSdkWrapper() {
        val apiKey = encryptedPrefs.getApiKey()
        val publicKey = resources.getString(R.string.public_key)

        clearentWrapper.initializeSDK(
            applicationContext,
            Constants.BASE_URL_SANDBOX,
            publicKey,
            apiKey,
            OfflineModeConfig(encryptedPrefs.getDbPassphrase())
        )

        // set up the sdk store and forward mode once so we don't override user preferences
        if (sharedPreferencesDataSource.isSdkSetUp())
            return

        sharedPreferencesDataSource.sdkSetupComplete()
    }

    private fun generatePassphrase() {
        if (encryptedPrefs.getDbPassphrase().isEmpty()) {
            encryptedPrefs.setDbPassphrase(UUID.randomUUID().toString())
        }
    }
}