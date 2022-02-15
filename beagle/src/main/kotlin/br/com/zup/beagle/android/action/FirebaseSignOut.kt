package br.com.zup.beagle.android.action

import android.view.View
import br.com.zup.beagle.android.context.ContextData
import br.com.zup.beagle.android.utils.handleEvent
import br.com.zup.beagle.android.widget.RootView
import br.com.zup.beagle.core.BeagleJson
import br.com.zup.beagle.newanalytics.ActionAnalyticsConfig
import com.google.firebase.auth.FirebaseAuth

/**
 * Sign the user out.
 *
 * @author Steve Soltys
 */
@BeagleJson(name = "firebaseSignOut")
class FirebaseSignOut(
    val onSuccess: List<Action>? = null,
    val onError: List<Action>? = null,
    val onFinish: List<Action>? = null,
    override var analytics: ActionAnalyticsConfig? = null
) : AnalyticsAction {

    override fun execute(rootView: RootView, origin: View) {
        FirebaseAuth.getInstance().signOut()
        success(rootView, origin)
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

    private fun finish(rootView: RootView, origin: View) {
        onFinish?.let {
            handleEvent(rootView, origin, it, analyticsValue = "onFinish")
        }
    }
}