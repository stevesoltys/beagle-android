package br.com.zup.beagle.android.action

import android.view.View
import br.com.zup.beagle.android.context.Bind
import br.com.zup.beagle.android.context.ContextData
import br.com.zup.beagle.android.utils.evaluate
import br.com.zup.beagle.android.utils.handleEvent
import br.com.zup.beagle.android.widget.RootView
import br.com.zup.beagle.core.BeagleJson
import br.com.zup.beagle.newanalytics.ActionAnalyticsConfig
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import java.util.concurrent.Executors

/**
 * Sign the user out.
 *
 * @author Steve Soltys
 */
@BeagleJson(name = "while")
class While(
    val condition: Bind<Boolean>,
    val timeInterval: Bind<Int>,
    val onTick: List<Action>? = null,
    val onFinish: List<Action>? = null,
    override var analytics: ActionAnalyticsConfig? = null
) : AnalyticsAction {

    companion object {
        private const val DEFAULT_INTERVAL = 100

        private val executor = Executors.newSingleThreadExecutor()
    }

    override fun execute(rootView: RootView, origin: View) {
        executor.submit {
            val interval = timeInterval.evaluate(rootView, origin, this) ?: DEFAULT_INTERVAL
            Thread.sleep(interval.toLong())

            if (condition.evaluate(rootView, origin, this) == true) {
                tick(rootView, origin)
                execute(rootView, origin)

            } else {
                finish(rootView, origin)
            }
        }
    }

    private fun tick(rootView: RootView, origin: View) {
        onTick?.let {
            handleEvent(
                rootView,
                origin,
                it,
                analyticsValue = "onSuccess"
            )
        }
    }

    private fun finish(rootView: RootView, origin: View) {
        onFinish?.let {
            handleEvent(rootView, origin, it, analyticsValue = "onFinish")
        }
    }
}