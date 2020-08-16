package udit.programmer.co.easypg.Common

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import com.google.android.material.snackbar.Snackbar
import kotlin.math.max
import kotlin.math.min

//This work is to mutable visibility of bottom navigation view on scrolling

class BottomNavigationView(context: Context, attributeSet: AttributeSet) :
    CoordinatorLayout.Behavior<View>(context, attributeSet) {

    override fun onStartNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: View,
        directTargetChild: View,
        target: View,
        axes: Int,
        type: Int
    ): Boolean {
        return axes == ViewCompat.SCROLL_AXIS_VERTICAL
    }

    override fun onNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: View,
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        type: Int,
        consumed: IntArray
    ) {
        super.onNestedScroll(
            coordinatorLayout,
            child,
            target,
            dxConsumed,
            dyConsumed,
            dxUnconsumed,
            dyUnconsumed,
            type,
            consumed
        )
        child.translationY =
            max(0f, min(child.height.toFloat(), child.translationY + dyConsumed))
    }

    override fun layoutDependsOn(
        parent: CoordinatorLayout,
        child: View,
        dependency: View
    ): Boolean {
        if (dependency is Snackbar.SnackbarLayout) updateSnackBar(child, dependency)
        return super.layoutDependsOn(parent, child, dependency)
    }

    private fun updateSnackBar(child: View?, dependency: Snackbar.SnackbarLayout) {
        if (dependency.layoutParams is CoordinatorLayout.LayoutParams) {
            val params = dependency.layoutParams as CoordinatorLayout.LayoutParams
            params.anchorId == child!!.id
            params.anchorGravity = Gravity.TOP
            params.gravity = Gravity.TOP
            dependency.layoutParams = params
        }
    }

}