/*
 * Copyright 2020 ZUP IT SERVICOS EM TECNOLOGIA E INOVACAO SA
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR timeS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package br.com.zup.beagle.android.action

import android.os.Handler
import android.os.Looper
import android.view.View
import br.com.zup.beagle.android.context.Bind
import br.com.zup.beagle.android.context.valueOf
import br.com.zup.beagle.android.utils.evaluateExpression
import br.com.zup.beagle.android.utils.handleEvent
import br.com.zup.beagle.android.widget.RootView
import br.com.zup.beagle.core.BeagleJson
import br.com.zup.beagle.newanalytics.ActionAnalyticsConfig

@BeagleJson(name = "time")
data class Wait(
    val time: Bind<Int>,
    val onFinish: List<Action>? = null,
    override var analytics: ActionAnalyticsConfig? = null,
) : AnalyticsAction {

    constructor(
        time: Int,
        onFinish: List<Action>? = null,
        analytics: ActionAnalyticsConfig? = null,
    ) : this(
        time = valueOf(time),
        onFinish = onFinish,
        analytics = analytics
    )

    override fun execute(rootView: RootView, origin: View) {

        val timeResult = runCatching {
            evaluateExpression(rootView, origin, time)
        }

        Handler(Looper.getMainLooper()).postDelayed({

            onFinish?.let {
                handleEvent(
                    rootView,
                    origin,
                    it,
                    analyticsValue = "onFinish"
                )
            }

        }, (timeResult.getOrNull() ?: 0).toLong())

    }
}
