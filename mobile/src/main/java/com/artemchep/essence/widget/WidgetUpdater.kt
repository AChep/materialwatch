package com.artemchep.essence.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Path
import android.view.LayoutInflater
import android.view.View
import android.widget.RemoteViews
import arrow.core.left
import com.artemchep.essence.R
import com.artemchep.essence.domain.exceptions.NoDataException
import com.artemchep.essence.domain.models.Theme
import com.artemchep.essence.domain.models.Visibility
import com.artemchep.essence.domain.models.currentTime
import com.artemchep.essence.ui.views.WatchFaceView
import com.artemchep.essence_common.databinding.WatchFaceBinding
import kotlin.math.min

object WidgetUpdater {
    fun updateClockWidget(context: Context) {
        val remoteViews = createRemoteViews(context)
        val componentName = ComponentName(context, WidgetProvider::class.java)
        AppWidgetManager
            .getInstance(context)
            .updateAppWidget(componentName, remoteViews)
    }

    private fun createRemoteViews(context: Context) = RemoteViews(
        context.packageName,
        R.layout.widget
    ).apply {
        val watchFaceWidth = 500
        val watchFaceHeight = 500
        val watchFaceViewBinding = WatchFaceBinding.inflate(LayoutInflater.from(context))
        with(watchFaceViewBinding.root) {
            // set the current state
            setTheme(Theme.BLACK)
            setTime(currentTime)
            setVisibility(Visibility())
            setWeather(NoDataException().left())
            // measure & layout the view
            val measuredWidth =
                View.MeasureSpec.makeMeasureSpec(watchFaceWidth, View.MeasureSpec.EXACTLY)
            val measuredHeight =
                View.MeasureSpec.makeMeasureSpec(watchFaceHeight, View.MeasureSpec.EXACTLY)
            measure(measuredWidth, measuredHeight)
            layout(0, 0, this.measuredWidth, this.measuredHeight)
        }
        val bitmap = Bitmap.createBitmap(
            watchFaceViewBinding.root.measuredWidth,
            watchFaceViewBinding.root.measuredHeight,
            Bitmap.Config.ARGB_8888,
        )
        val canvas = Canvas(bitmap)
        val path = Path().apply {
            addCircle(
                watchFaceViewBinding.root.measuredWidth / 2f,
                watchFaceViewBinding.root.measuredHeight / 2f,
                min(
                    watchFaceViewBinding.root.measuredWidth,
                    watchFaceViewBinding.root.measuredHeight,
                ) / 2f,
                Path.Direction.CW
            )
        }
        canvas.clipPath(path)
        watchFaceViewBinding.root.draw(canvas)
        // set the resulting image
        setImageViewBitmap(R.id.imageView, bitmap)
    }
}