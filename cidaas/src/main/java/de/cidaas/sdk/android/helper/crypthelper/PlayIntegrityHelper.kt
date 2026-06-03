package de.cidaas.sdk.android.helper.crypthelper

import android.content.Context
import android.content.pm.PackageManager
import com.google.android.play.core.integrity.IntegrityManagerFactory
import com.google.android.play.core.integrity.IntegrityTokenRequest
import de.cidaas.sdk.android.helper.general.CidaasConstants
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * Requests a Play Integrity token with nonce = Base64.URL_SAFE | NO_WRAP over the server challenge bytes.
 */
object PlayIntegrityHelper {

    @JvmStatic
    fun requestToken(
        context: Context,
        challengeBytes: ByteArray,
        cloudProjectNumber: Long?,
        listener: PlayIntegrityTokenListener,
    ) {
        val nonce = android.util.Base64.encodeToString(
            challengeBytes,
            android.util.Base64.URL_SAFE or android.util.Base64.NO_WRAP,
        )
        val mgr = IntegrityManagerFactory.create(context.applicationContext)
        val reqBuilder = IntegrityTokenRequest.builder().setNonce(nonce)
        val projectNumber = cloudProjectNumber ?: readCloudProjectNumberFromManifest(context)
        if (projectNumber != null && projectNumber > 0L) {
            reqBuilder.setCloudProjectNumber(projectNumber)
        }
        mgr.requestIntegrityToken(reqBuilder.build())
            .addOnSuccessListener { response -> listener.onSuccess(response.token()) }
            .addOnFailureListener { e -> listener.onFailure(e) }
    }

    suspend fun requestToken(context: Context, challengeBytes: ByteArray, cloudProjectNumber: Long?): String =
        suspendCoroutine { cont ->
            requestToken(
                context,
                challengeBytes,
                cloudProjectNumber,
                object : PlayIntegrityTokenListener {
                    override fun onSuccess(token: String) = cont.resume(token)
                    override fun onFailure(error: Throwable) = cont.resumeWithException(error)
                },
            )
        }

    fun readCloudProjectNumberFromManifest(context: Context): Long? {
        return try {
            val ai = context.packageManager.getApplicationInfo(
                context.packageName,
                PackageManager.GET_META_DATA,
            )
            val raw = ai.metaData?.getString(CidaasConstants.PLAY_INTEGRITY_CLOUD_PROJECT_NUMBER)
            raw?.trim()?.toLongOrNull()?.takeIf { it > 0L }
        } catch (_: Exception) {
            null
        }
    }
}

interface PlayIntegrityTokenListener {
    fun onSuccess(token: String)
    fun onFailure(error: Throwable)
}
