package com.example.periodvibe.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator

/**
 * State holder for navigation state in Navigation 3.
 */
class NavigationState<T : NavKey>(
    val backStack: NavBackStack<T>
) {
    /**
     * The current top route in the back stack.
     */
    val topLevelRoute: T by derivedStateOf { backStack.last() }

    /**
     * Converts the navigation state into [NavEntry]s to be displayed by NavDisplay.
     */
    @Composable
    fun toEntries(
        entryProvider: (T) -> NavEntry<out T>
    ): List<NavEntry<T>> {
        @Suppress("UNCHECKED_CAST")
        return rememberDecoratedNavEntries(
            backStack = backStack,
            entryDecorators = listOf(rememberSaveableStateHolderNavEntryDecorator()),
            entryProvider = entryProvider as (T) -> NavEntry<T>
        )
    }
}

/**
 * Creates and remembers a [NavigationState].
 */
@Composable
fun <T : NavKey> rememberNavigationState(backStack: NavBackStack<T>): NavigationState<T> {
    return remember(backStack) { NavigationState(backStack) }
}

/**
 * Navigator class to handle navigation events and update the [NavigationState].
 */
class Navigator<T : NavKey>(
    private val state: NavigationState<T>
) {
    /**
     * Navigates to a new route, optionally popping up the back stack.
     */
    fun navigate(route: T, builder: (NavOptionsBuilder<T>.() -> Unit)? = null) {
        if (builder != null) {
            val options = NavOptionsBuilder<T>().apply(builder)
            
            val popUpToRoute = options.popUpToRoute
            val popUpToPredicate = options.popUpToPredicate
            
            if (popUpToRoute != null || popUpToPredicate != null) {
                val index = if (popUpToRoute != null) {
                    state.backStack.indexOf(popUpToRoute)
                } else {
                    state.backStack.indexOfLast { popUpToPredicate!!(it) }
                }
                
                if (index != -1) {
                    val removeCount = state.backStack.size - (if (options.inclusive) index else index + 1)
                    if (removeCount > 0) {
                        repeat(removeCount) {
                            state.backStack.removeLastOrNull()
                        }
                    }
                }
            }
        }
        state.backStack.add(route)
    }

    /**
     * Pops the current route from the back stack.
     */
    fun goBack() {
        if (state.backStack.size > 1) {
            state.backStack.removeLastOrNull()
        }
    }
}

/**
 * Builder for navigation options.
 */
class NavOptionsBuilder<T : NavKey> {
    var popUpToRoute: T? = null
    var popUpToPredicate: ((T) -> Boolean)? = null
    var inclusive: Boolean = false

    /**
     * Pops up to a specific route before navigating.
     */
    fun popUpTo(route: T, builder: (PopUpToBuilder.() -> Unit)? = null) {
        popUpToRoute = route
        builder?.let {
            val popUpToBuilder = PopUpToBuilder().apply(it)
            inclusive = popUpToBuilder.inclusive
        }
    }

    /**
     * Pops up to a route of a specific type before navigating.
     */
    inline fun <reified R : T> popUpTo(noinline builder: (PopUpToBuilder.() -> Unit)? = null) {
        popUpToPredicate = { it is R }
        builder?.let {
            val popUpToBuilder = PopUpToBuilder().apply(it)
            inclusive = popUpToBuilder.inclusive
        }
    }
}

/**
 * Builder for popUpTo options.
 */
class PopUpToBuilder {
    var inclusive: Boolean = false
}

/**
 * Creates and remembers a [Navigator].
 */
@Composable
fun <T : NavKey> rememberNavigator(state: NavigationState<T>): Navigator<T> {
    return remember(state) { Navigator(state) }
}
