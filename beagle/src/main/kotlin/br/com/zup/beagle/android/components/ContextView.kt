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
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package br.com.zup.beagle.android.components

import android.view.View
import br.com.zup.beagle.android.context.Bind
import br.com.zup.beagle.android.context.ContextComponent
import br.com.zup.beagle.android.context.ContextData
import br.com.zup.beagle.android.data.serializer.BeagleSerializer
import br.com.zup.beagle.android.utils.observeBindChanges
import br.com.zup.beagle.android.view.ViewFactory
import br.com.zup.beagle.android.widget.RootView
import br.com.zup.beagle.android.widget.WidgetView
import br.com.zup.beagle.annotation.RegisterWidget

@RegisterWidget("contextView")
data class ContextView(
    override val context: ContextData?,
    val component: Bind<String>
) : WidgetView(), ContextComponent {

    private val serializer: BeagleSerializer = BeagleSerializer()

    override fun buildView(rootView: RootView): View {

        return ViewFactory.makeBeagleFlexView(rootView).apply {

            observeBindChanges(
                rootView = rootView,
                view = this,
                bind = component
            ) { json ->
                json?.also {
                    removeAllViews()
                    addServerDrivenComponent(serializer.deserializeComponent(json))
                }
            }
        }
    }
}
