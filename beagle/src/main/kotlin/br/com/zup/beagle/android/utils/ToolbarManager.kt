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

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.AccessibilityDelegateCompat
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import br.com.zup.beagle.R
import br.com.zup.beagle.android.components.layout.NavigationBar
import br.com.zup.beagle.android.components.layout.NavigationBarItem
import br.com.zup.beagle.android.components.layout.ScreenComponent
import br.com.zup.beagle.android.components.layout.SearchBar
import br.com.zup.beagle.android.components.utils.hideKeyboard
import br.com.zup.beagle.android.context.ContextData
import br.com.zup.beagle.android.setup.BeagleEnvironment
import br.com.zup.beagle.android.setup.DesignSystem
import br.com.zup.beagle.android.view.BeagleActivity
import br.com.zup.beagle.android.view.custom.BeagleFlexView
import br.com.zup.beagle.android.view.custom.BeagleNavigator
import br.com.zup.beagle.android.widget.RootView

internal class ToolbarManager(private val toolbarTextManager: ToolbarTextManager = ToolbarTextManager) {

    companion object {
        private const val CONTENT_INSET_ZERO = 0
        private const val CONTENT_INSET_LEFT_ZERO = 0
        private const val CONTENT_INSET_RIGHT_ZERO = 0
    }

    fun configureNavigationBarForScreen(
        context: BeagleActivity,
        navigationBar: NavigationBar,
    ) {
        if (navigationBar.showBackButton) {
            context.getToolbar().apply {
                navigationBar.backButtonAccessibility?.accessibilityLabel?.let { backButtonAccessibilityLabel ->
                    navigationContentDescription = backButtonAccessibilityLabel
                }
                navigationBar.backButtonAccessibility?.isHeader?.let { isHeader ->
                    ViewCompat.setAccessibilityDelegate(
                        this,
                        object : AccessibilityDelegateCompat() {
                            override fun onInitializeAccessibilityNodeInfo(
                                host: View,
                                info: AccessibilityNodeInfoCompat
                            ) {
                                super.onInitializeAccessibilityNodeInfo(host, info)
                                info.isHeading = isHeader
                            }
                        })
                }

                setNavigationOnClickListener {
                    BeagleNavigator.popView(context)
                }

                setupNavigationIcon(context, this)

            }
        }
    }

    fun configureToolbar(
        rootView: RootView,
        navigationBar: NavigationBar,
        container: BeagleFlexView,
        screenComponent: ScreenComponent,
    ) {
        (rootView.getContext() as BeagleActivity).getToolbar().apply {
            visibility = View.VISIBLE
            menu.clear()
            setAttributeToolbar(rootView, rootView.getContext(), this, navigationBar)
            navigationBar.navigationBarItems?.let { items ->
                configToolbarItems(rootView, this, items, container, screenComponent)
            }

            navigationBar.searchBar?.let { searchBar ->
                setSearchView(
                    rootView,
                    rootView.getContext(),
                    this,
                    screenComponent,
                    searchBar
                )
            }
        }
    }

    private fun setSearchView(
        rootView: RootView,
        context: Context,
        toolbar: Toolbar,
        screenComponent: ScreenComponent,
        searchBar: SearchBar
    ) {

        val searchView = SearchView(context).apply {

            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    hideKeyboard()
                    return true
                }

                override fun onQueryTextChange(newText: String): Boolean {

                    searchBar.onQueryUpdated?.let { actions ->
                        screenComponent.handleEvent(
                            rootView,
                            this@apply,
                            actions,
                            context = ContextData(
                                id = "onQueryUpdated",
                                value = mapOf(
                                    "query" to newText
                                )
                            ),
                            analyticsValue = "onQueryUpdated"
                        )
                    }

                    return true
                }

            })
            queryHint = searchBar.placeholder ?: ""
        }

        toolbar.menu.clear()
        val searchItem = toolbar.menu.add("Search")
        searchItem.icon = searchView.findViewById<ImageView>(R.id.search_button).drawable
        searchItem.actionView = searchView
        searchItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW or MenuItem.SHOW_AS_ACTION_ALWAYS)
    }

    private fun setAttributeToolbar(
        rootView: RootView,
        context: Context,
        toolbar: Toolbar,
        navigationBar: NavigationBar
    ) {
        val designSystem = BeagleEnvironment.beagleSdk.designSystem
        val toolbarStyle = navigationBar.styleId?.let { designSystem?.toolbarStyle(it) }
        if (toolbarStyle == null) {
            setTitle(rootView, context, toolbar, navigationBar)
        } else {
            configToolbarStyle(rootView, context, toolbar, navigationBar, toolbarStyle)
        }

        val style = navigationBar.navigationBarStyle

        style?.apply {
            backgroundColor?.toAndroidColor()?.also { toolbar.setBackgroundColor(it) }

            tintColor?.toAndroidColor()?.also { androidColor ->
                toolbar.setTitleTextColor(androidColor)
                toolbar.overflowIcon?.also { DrawableCompat.setTint(it, androidColor) }
            }

            textColor?.toAndroidColor()?.also {
                toolbar.setTitleTextColor(it)
            }

            if (isShadowEnabled == false) {
                ViewCompat.setElevation(toolbar, 0f)
            } else {
                // 4 points https://material.io/design/environment/elevation.html#elevation-shadows-elevation-android
                ViewCompat.setElevation(toolbar, 4f.dp())
            }
        }
    }

    private fun configToolbarStyle(
        rootView: RootView,
        context: Context,
        toolbar: Toolbar,
        navigationBar: NavigationBar,
        toolbarStyle: Int
    ) {
        val typedArray = context.obtainStyledAttributes(
            toolbarStyle,
            R.styleable.BeagleToolbarStyle
        )
        if (navigationBar.showBackButton) {
            typedArray.getDrawable(R.styleable.BeagleToolbarStyle_navigationIcon)?.let {
                toolbar.navigationIcon = it
            }
        } else {
            toolbar.navigationIcon = null
        }
        val textAppearance = typedArray.getResourceId(
            R.styleable.BeagleToolbarStyle_titleTextAppearance, 0
        )
        val isCenterTitle = typedArray.getBoolean(R.styleable.BeagleToolbarStyle_centerTitle, false)
        setTitle(rootView, context, toolbar, navigationBar, textAppearance, isCenterTitle)
        val backgroundColor = typedArray.getColor(
            R.styleable.BeagleToolbarStyle_backgroundColor, 0
        )
        if (backgroundColor != 0) {
            toolbar.setBackgroundColor(backgroundColor)
        }
        typedArray.recycle()
    }

    private fun setTitle(
        rootView: RootView,
        context: Context,
        toolbar: Toolbar,
        navigationBar: NavigationBar,
        textAppearance: Int = 0,
        isCenterTitle: Boolean = false
    ) {
        removePreviousToolbarTitle(toolbar)

        val titleImage = navigationBar.navigationBarStyle?.titleImage

        if (titleImage != null) {
            val imageView = titleImage.buildView(rootView) as ImageView
            imageView.id = R.id.beagle_toolbar_image

            titleImage.style?.size?.let {
                val width = it.width?.value?.toInt()?.dp()
                val height = it.height?.value?.toInt()?.dp()

                if (width != null && height != null) {
                    imageView.layoutParams = LinearLayout.LayoutParams(width, height)
                }
            }

            toolbar.title = ""
            toolbar.addView(imageView)
            toolbarTextManager.centerTitle(toolbar, imageView)
            toolbar.contentInsetStartWithNavigation = CONTENT_INSET_ZERO
            toolbar.setContentInsetsAbsolute(CONTENT_INSET_LEFT_ZERO, CONTENT_INSET_RIGHT_ZERO)

        } else if (isCenterTitle) {
            setCenterTitle(context, toolbar, navigationBar, textAppearance)
        } else {
            setDefaultTitle(context, toolbar, navigationBar, textAppearance)
        }
    }

    private fun setCenterTitle(
        context: Context,
        toolbar: Toolbar,
        navigationBar: NavigationBar,
        textAppearance: Int = 0,
    ) {
        val titleTextView = toolbarTextManager.generateTitle(context, navigationBar, textAppearance)
        toolbar.addView(titleTextView)
        toolbarTextManager.centerTitle(toolbar, titleTextView)
        toolbar.contentInsetStartWithNavigation = CONTENT_INSET_ZERO
        toolbar.setContentInsetsAbsolute(CONTENT_INSET_LEFT_ZERO, CONTENT_INSET_RIGHT_ZERO)
    }

    private fun setDefaultTitle(
        context: Context,
        toolbar: Toolbar,
        navigationBar: NavigationBar,
        textAppearance: Int = 0,
    ) {
        toolbar.title = navigationBar.title
        if (textAppearance != 0) {
            toolbar.setTitleTextAppearance(context, textAppearance)
        }
    }

    private fun removePreviousToolbarTitle(toolbar: Toolbar) {
        val centeredTitle = toolbar.findViewById<View>(R.id.beagle_toolbar_text)
        toolbar.removeView(centeredTitle)

        toolbar.removeView(toolbar.findViewById(R.id.beagle_toolbar_image))
    }

    private fun configToolbarItems(
        rootView: RootView,
        toolbar: Toolbar,
        items: List<NavigationBarItem>,
        container: BeagleFlexView,
        screenComponent: ScreenComponent,
    ) {
        val designSystem = BeagleEnvironment.beagleSdk.designSystem
        for (i in items.indices) {
            toolbar.menu.add(Menu.NONE, items[i].id?.toAndroidId() ?: i, Menu.NONE, items[i].text)
                .apply {
                    setOnMenuItemClickListener {
                        val action = items[i].action
                        action.handleEvent(rootView, toolbar, action)
                        return@setOnMenuItemClickListener true
                    }

                    setContentDescription(items, i)

                    if (items[i].image == null) {
                        setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                    } else {
                        configMenuItem(designSystem, items, i, rootView, container, screenComponent)
                    }
                }
        }
    }

    private fun MenuItem.setContentDescription(
        items: List<NavigationBarItem>,
        i: Int,
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            items[i].accessibility?.accessibilityLabel?.let { accessibilityLabel ->
                this.contentDescription = accessibilityLabel
            }
        }
    }

    @Suppress("LongParameterList")
    private fun MenuItem.configMenuItem(
        design: DesignSystem?,
        items: List<NavigationBarItem>,
        i: Int,
        rootView: RootView,
        container: BeagleFlexView,
        screenComponent: ScreenComponent,
    ) {
        design?.let { designSystem ->
            items[i].image?.let { image ->
                setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)

                val imageView = image.buildView(rootView) as ImageView
                icon = imageView.drawable
            }
        }
    }

    private fun getDrawableFromAttribute(context: Context, attributeId: Int): Drawable? {
        val typedValue = TypedValue().also { context.theme.resolveAttribute(attributeId, it, true) }
        return ContextCompat.getDrawable(context, typedValue.resourceId)
    }

    private fun setupNavigationIcon(context: Context, toolbar: Toolbar) {
        if (toolbar.navigationIcon == null) {
            toolbar.navigationIcon =
                getDrawableFromAttribute(context, androidx.appcompat.R.attr.homeAsUpIndicator)
        }
    }
}