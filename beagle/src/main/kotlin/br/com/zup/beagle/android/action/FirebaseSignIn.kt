package br.com.zup.beagle.android.action

import android.util.Log
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import br.com.zup.beagle.R
import br.com.zup.beagle.android.context.Bind
import br.com.zup.beagle.android.context.ContextData
import br.com.zup.beagle.android.utils.evaluate
import br.com.zup.beagle.android.utils.handleEvent
import br.com.zup.beagle.android.view.BeagleFragment
import br.com.zup.beagle.android.widget.ActivityRootView
import br.com.zup.beagle.android.widget.FragmentRootView
import br.com.zup.beagle.android.widget.RootView
import br.com.zup.beagle.core.BeagleJson
import br.com.zup.beagle.newanalytics.ActionAnalyticsConfig
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

/**
 * Sign the user in via Firebase authentication.
 *
 * @author Steve Soltys
 */
@BeagleJson(name = "firebaseSignIn")
class FirebaseSignIn(
    val type: FirebaseSignInType,
    val email: Bind<String>? = null,
    val password: Bind<String>? = null,
    val onSuccess: List<Action>? = null,
    val onError: List<Action>? = null,
    val onFinish: List<Action>? = null,
    override var analytics: ActionAnalyticsConfig? = null
) : AnalyticsAction {

    companion object {
        internal val TAG = FirebaseSignIn::class.java.simpleName
    }

    override fun execute(rootView: RootView, origin: View) {

        val fragment = if (
            rootView is FragmentRootView &&
            rootView.fragment is BeagleFragment
        ) {
            rootView.fragment
        } else null

        val componentActivity: ComponentActivity? = when (rootView) {
            is ActivityRootView -> rootView.activity
            is FragmentRootView -> rootView.fragment.activity
            else -> null
        }

        if (componentActivity == null) {
            error(rootView, origin, "Component activity is null.")
            return

        } else if (fragment == null) {
            error(rootView, origin, "Fragment is null.")
            return
        }

        when (type) {
            FirebaseSignInType.EMAIL_PASSWORD -> emailSignIn(rootView, origin)
            FirebaseSignInType.GOOGLE -> googleSignIn(rootView, origin, componentActivity, fragment)

            FirebaseSignInType.APPLE -> {
                // TODO
            }
        }
    }

    private fun emailSignIn(rootView: RootView, origin: View) {
        val evaluatedEmail = email?.evaluate(rootView, origin, this)
        val evaluatedPassword = password?.evaluate(rootView, origin, this)

        if (evaluatedEmail != null && evaluatedPassword != null) {
            val listener = FirebaseSignInListener(this, rootView, origin)

            FirebaseAuth.getInstance()
                .signInWithEmailAndPassword(evaluatedEmail, evaluatedPassword)
                .addOnCompleteListener(listener)
        }
    }

    private fun googleSignIn(
        rootView: RootView,
        origin: View,
        activity: ComponentActivity,
        fragment: BeagleFragment
    ) {

        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(activity.getString(R.string.google_sign_in_client_id))
            .requestId()
            .requestEmail()
            .requestProfile()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(activity, signInOptions)
        val callback = GoogleSignInCallback(this, rootView, origin)

        fragment.activityResultListeners.add(callback)
        fragment.activityResultLauncher.launch(googleSignInClient.signInIntent)
    }

    fun success(rootView: RootView, origin: View) {

        onSuccess?.let {
            handleEvent(
                rootView,
                origin,
                it,
                analyticsValue = "onSuccess"
            )
        }

        finish(rootView, origin)
    }

    fun error(rootView: RootView, origin: View, message: String) {

        onError?.let {
            handleEvent(
                rootView,
                origin,
                it,
                ContextData("onError", mapOf("message" to message)),
                analyticsValue = "onError"
            )
        }

        finish(rootView, origin)
    }

    private fun finish(rootView: RootView, origin: View) {
        onFinish?.let {
            handleEvent(rootView, origin, it, analyticsValue = "onFinish")
        }
    }
}

class FirebaseSignInListener(
    private val firebaseSignIn: FirebaseSignIn,
    private val rootView: RootView,
    private val origin: View
) : OnCompleteListener<AuthResult> {

    override fun onComplete(task: Task<AuthResult>) {
        if (task.isSuccessful) {
            Log.d(FirebaseSignIn.TAG, "signInWithCredential:success")
            firebaseSignIn.success(rootView, origin)

        } else {
            Log.w(FirebaseSignIn.TAG, "signInWithCredential:failure", task.exception)

            firebaseSignIn.error(
                rootView,
                origin,
                "signInWithCredential:failure: ${task.exception?.message}"
            )
        }
    }

}

class GoogleSignInCallback(
    private val firebaseSignIn: FirebaseSignIn,
    private val rootView: RootView,
    private val origin: View
) : ActivityResultCallback<ActivityResult> {

    override fun onActivityResult(result: ActivityResult) {

        try {
            val account = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                .getResult(ApiException::class.java)

            if (account?.idToken != null) {
                val credential = GoogleAuthProvider.getCredential(account.idToken!!, null)

                FirebaseAuth.getInstance().signInWithCredential(credential)
                    .addOnCompleteListener(
                        FirebaseSignInListener(firebaseSignIn, rootView, origin)
                    )

            } else {
                val message = "signInResult: id token is null"
                Log.w(FirebaseSignIn.TAG, message)

                firebaseSignIn.error(rootView, origin, message)
            }

        } catch (e: ApiException) {
            val message = "signInResult:failed code=${e.statusCode}"
            Log.w(FirebaseSignIn.TAG, message)

            firebaseSignIn.error(rootView, origin, message)
        }
    }
}

enum class FirebaseSignInType {
    EMAIL_PASSWORD, APPLE, GOOGLE
}
