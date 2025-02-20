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

package br.com.zup.beagle.core

import br.com.zup.beagle.android.context.Bind
import br.com.zup.beagle.widget.core.EdgeValue
import br.com.zup.beagle.widget.core.Flex
import br.com.zup.beagle.widget.core.Size

/**
 * The style class will enable a few visual options to be changed.
 *
 * @param backgroundColor
 *                          Using a String parameter it sets the background color on this visual component.
 *                          It must be listed as an Hexadecimal color format without the "#".
 *                          For example, for a WHITE background type in "FFFFFF".
 * @param cornerRadius Using a Double parameters it sets the corner of your view to make it round.
 * @param borderColor Sets the color of your view border. Supported formats:#RRGGBB[AA] and #RGB[A].
 * @param borderWidth Sets the width of your view border.
 * @param size add size to current view applying the flex.
 * @param margin
 *                  effects the spacing around the outside of a node.
 *                  A node with margin will offset itself from the bounds of its parent
 *                  but also offset the location of any siblings.
 *                  The margin of a node contributes to the total size of its parent if the parent is auto sized.
 * @param padding
 *                  affects the size of the node it is applied to.
 *                  Padding in Yoga acts as if box-sizing: border-box; was set.
 *                  That is padding will not add to the total size of an element if it has an explicit size set.
 *                  For auto sized nodes padding will increase the size of the
 *                  node as well as offset the location of any children..
 * @param position add padding to position.
 * @param flex
 * @see Flex
 * @param positionType The position type of an element defines how it is positioned within its parent.
 * @param display enables a flex context for all its direct children.
 */
@BeagleJson
data class Style(
    val backgroundColor: String? = null,
    val cornerRadius: CornerRadius? = null,
    val borderColor: String? = null,
    val borderWidth: Double? = null,
    val size: Size? = null,
    val margin: EdgeValue? = null,
    val padding: EdgeValue? = null,
    val position: EdgeValue? = null,
    val flex: Flex? = null,
    val positionType: PositionType? = null,
    val display: Bind<Display>? = null,
)

/**
 * The corner radius apply rounded borders to the widget
 *
 * @param radius defines the default size of the all corner radius
 * @param topLeft defines the size of the top left radius
 * @param topRight defines the size of the top right radius
 * @param bottomLeft defines the size of the bottom left radius
 * @param bottomRight defines the size of the bottom right radius
 */
@BeagleJson
data class CornerRadius(
    val radius: Double? = null,
    val topLeft: Double? = null,
    val topRight: Double? = null,
    val bottomLeft: Double? = null,
    val bottomRight: Double? = null,
)

/**
 * This defines a flex container;
 * inline or block depending on the given value. It enables a flex context for all its direct children.
 *
 * @property FLEX
 * @property NONE
 */
@BeagleJson
enum class Display {
    /**
     * Apply the flex properties.
     */
    FLEX,

    /**
     * No flex properties will be applied to the element.
     */
    NONE
}

/**
 * The position type of an element defines how it is positioned within its parent.
 *
 * @property ABSOLUTE
 * @property RELATIVE
 */
@BeagleJson
enum class PositionType {

    /**
     * This means an element is positioned according to the normal flow of the layout,
     * and then offset relative to that position based on the values of top, right, bottom, and left.
     * The offset does not affect the position of any sibling or parent elements.
     */
    ABSOLUTE,

    /**
     * When positioned absolutely an element doesn't take part in the normal layout flow.
     * It is instead laid out independent of its siblings.
     * The position is determined based on the top, right, bottom, and left values.
     */
    RELATIVE
}
