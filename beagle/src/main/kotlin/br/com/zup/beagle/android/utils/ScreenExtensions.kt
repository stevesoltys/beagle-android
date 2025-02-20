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

package br.com.zup.beagle.android.utils

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import br.com.zup.beagle.android.components.layout.Screen
import br.com.zup.beagle.android.components.layout.ScreenComponent

@Deprecated(
    "It was deprecated in version 1.10.0 and will be removed in a future version." +
            " Use the load view with screenJson.",
    ReplaceWith(
        "viewGroup.loadView(activity, screenJson, screenId)"
    )
)
fun Screen.toView(activity: AppCompatActivity) = this.toComponent().toView(activity)

@Deprecated(
    "It was deprecated in version 1.10.0 and will be removed in a future version." +
            " Use the load view with screenJson.",
    ReplaceWith(
        "viewGroup.loadView(activity, screenJson, screenId)"
    )
)
fun Screen.toView(fragment: Fragment) = this.toComponent().toView(fragment)

internal fun Screen.toComponent() = ScreenComponent(
    identifier = this.identifier,
    safeArea = this.safeArea,
    navigationBar = this.navigationBar,
    child = this.child,
    screenAnalyticsEvent = this.screenAnalyticsEvent,
    context = this.context
).apply {
    id = this@toComponent.id
    style = this@toComponent.style
}
