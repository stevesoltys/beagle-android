package br.com.zup.beagle.android.components

import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color.BLACK
import android.graphics.Color.GRAY
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.StateListDrawable
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.core.view.ViewCompat
import androidx.lifecycle.lifecycleScope
import br.com.zup.beagle.android.action.Action
import br.com.zup.beagle.android.cache.imagecomponent.ImageDownloader
import br.com.zup.beagle.android.context.Bind
import br.com.zup.beagle.android.context.ContextData
import br.com.zup.beagle.android.data.formatUrl
import br.com.zup.beagle.android.logger.BeagleMessageLogs
import br.com.zup.beagle.android.utils.CoroutineDispatchers
import br.com.zup.beagle.android.utils.dp
import br.com.zup.beagle.android.utils.handleEvent
import br.com.zup.beagle.android.utils.toAndroidColor
import br.com.zup.beagle.android.view.ViewFactory
import br.com.zup.beagle.android.view.custom.BeagleFlexView
import br.com.zup.beagle.android.widget.RootView
import br.com.zup.beagle.android.widget.WidgetView
import br.com.zup.beagle.annotation.RegisterWidget
import br.com.zup.beagle.core.Style
import br.com.zup.beagle.widget.core.Flex
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch

@RegisterWidget("bottomNavigationBar")
class BottomNavigationBar(
    val items: List<BottomNavigationItem>,
    val currentTab: Bind<Int>,
    val onTabSelection: List<Action>,
    val barTintColor: Bind<String>? = null,
    val tintColor: Bind<String>? = null
) : WidgetView() {

    private val imageDownloader = ImageDownloader()

    override fun buildView(rootView: RootView): View {
        val containerFlex = Style(flex = Flex(grow = 1.0))

        val container = ViewFactory.makeBeagleFlexView(rootView, containerFlex)
        val navigationBar = makeNavigationBar(rootView, container)
        container.addView(navigationBar)

        return container
    }

    private fun makeNavigationBar(
        rootView: RootView,
        container: BeagleFlexView
    ): BottomNavigationView {
        val context = rootView.getContext()

        val bottomNavigation = BottomNavigationView(context).apply {
            id = android.R.id.tabs
            ViewCompat.setElevation(this, 8.dp().toFloat())

            // TODO: Support bind
            val color = barTintColor?.value.toString().toAndroidColor()

            if (color != null) {
                setBackgroundColor(color)
            } else {
                setBackgroundResource(android.R.color.white)
            }
        }

        items.forEachIndexed { index, item ->

            val menuItem = bottomNavigation.menu.add(
                Menu.NONE, index, Menu.NONE, item.title
            )

            updateIcon(rootView, bottomNavigation, item, menuItem)
        }

        bottomNavigation.setOnNavigationItemSelectedListener { menuItem ->

            handleEvent(
                rootView,
                container,
                onTabSelection,
                ContextData(
                    "onTabSelection", value = mapOf(
                        "index" to menuItem.itemId
                    )
                ),
                analyticsValue = "onTabSelected"
            )

            true
        }

        updateItemColors(bottomNavigation)
        return bottomNavigation
    }

    private fun updateItemColors(
        bottomNavigationView: BottomNavigationView
    ) {
        val states = arrayOf(
            intArrayOf(android.R.attr.state_selected),
            intArrayOf(-android.R.attr.state_selected)
        )
        val colors = intArrayOf(
            tintColor?.value?.toString()?.toAndroidColor() ?: BLACK,
            GRAY
        )

        bottomNavigationView.itemIconTintList = ColorStateList(states, colors)
        bottomNavigationView.itemTextColor = ColorStateList(states, colors)
    }

    private fun updateIcon(
        rootView: RootView,
        bottomNavigationView: BottomNavigationView,
        bottomNavigationItem: BottomNavigationItem,
        menuItem: MenuItem
    ) {

        rootView.getLifecycleOwner().lifecycleScope.launch(CoroutineDispatchers.Main) {
            val stateListDrawable = StateListDrawable()

            val selectedDrawable = getDrawable(
                rootView,
                bottomNavigationView,
                bottomNavigationItem.selectedIconPath
            )

            stateListDrawable.addState(
                intArrayOf(android.R.attr.state_selected),
                selectedDrawable
            )

            val unselectedDrawable = getDrawable(
                rootView,
                bottomNavigationView,
                bottomNavigationItem.unselectedIconPath
            )

            stateListDrawable.addState(
                intArrayOf(-android.R.attr.state_selected),
                unselectedDrawable
            )

            menuItem.icon = stateListDrawable
        }
    }

    private suspend fun getDrawable(
        rootView: RootView,
        bottomNavigationView: BottomNavigationView,
        url: Bind<String>
    ): BitmapDrawable {
        // TODO: Support bind.
        val selectedUrl = url.value.toString()

        return BitmapDrawable(
            rootView.getContext().resources,
            getRemoteBitmap(selectedUrl, rootView)
        )
    }

    private suspend fun getRemoteBitmap(
        url: String?,
        rootView: RootView,
        width: Int = 25,
        height: Int = 25
    ): Bitmap? {
        try {
            val formattedUrl = url?.formatUrl()?.takeIf { it.isNotEmpty() } ?: url

            return if (formattedUrl != null) {
                imageDownloader.getRemoteImage(
                    formattedUrl, width, height, rootView.getContext()
                )

            } else {
                null
            }

        } catch (e: Exception) {
            BeagleMessageLogs.errorWhileTryingToDownloadImage(url ?: "", e)
            return null
        }
    }
}

data class BottomNavigationItem(
    val selectedIconPath: Bind<String>,
    val unselectedIconPath: Bind<String>,
    val title: String?
)