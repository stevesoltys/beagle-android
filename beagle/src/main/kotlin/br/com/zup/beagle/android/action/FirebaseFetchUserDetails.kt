package br.com.zup.beagle.android.action

import android.view.View
import br.com.zup.beagle.android.context.ContextData
import br.com.zup.beagle.android.utils.handleEvent
import br.com.zup.beagle.android.widget.RootView
import br.com.zup.beagle.core.BeagleJson
import br.com.zup.beagle.newanalytics.ActionAnalyticsConfig
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

/**
 * Fetch a user's Firebase account information.
 */
@BeagleJson(name = "firebaseFetchUserDetails")
class FirebaseFetchUserDetails(
    val onSuccess: List<Action>? = null,
    val onError: List<Action>? = null,
    val onFinish: List<Action>? = null,
    override var analytics: ActionAnalyticsConfig? = null
) : AnalyticsAction {

    override fun execute(rootView: RootView, origin: View) {
        val user = FirebaseAuth.getInstance().currentUser

        if (user == null) {
            error(rootView, origin, "User is not authenticated.")
            return
        }

        val task = user.reload()
        task.addOnCompleteListener(ReloadListener(this, rootView, origin))
    }

    fun success(rootView: RootView, origin: View) {
        val user = FirebaseAuth.getInstance().currentUser!!

        onSuccess?.let {
            handleEvent(
                rootView,
                origin,
                it,
                ContextData(
                    "onSuccess", mapOf(
                        "email" to user.email,
                        "displayName" to user.displayName,
                        "emailVerified" to user.isEmailVerified
                    )
                ),
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

class ReloadListener(
    private val firebaseFetchUserDetails: FirebaseFetchUserDetails,
    private val rootView: RootView,
    private val origin: View
) : OnCompleteListener<Void> {

    override fun onComplete(task: Task<Void>) {
        firebaseFetchUserDetails.success(rootView, origin)
    }
}