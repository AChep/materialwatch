package com.artemchep.essence.ui.views.arc.impl

import android.content.Context
import android.graphics.Bitmap
import android.os.SystemClock
import android.renderscript.Allocation
import android.renderscript.RenderScript
import android.util.Log
import com.artemchep.essence.rs.ScriptC_arc
import com.artemchep.essence.ui.views.arc.ArcAlgorithm

/**
 * @author Artem Chepurnoy
 */
class RsArcAlgorithm(private val context: Context) : ArcAlgorithm {
    companion object {
        private const val TAG = "RsArcAlgorithm"
    }

    private var holder: Holder? = null

    override fun prepare(bitmap: Bitmap) {
        val target = Bitmap.createBitmap(bitmap.width, bitmap.height, bitmap.config)
        holder = Holder(context, target)
    }

    override fun bend(bitmap: Bitmap): Bitmap {
        val then = SystemClock.elapsedRealtime()

        val holder = holder!!
        val script = holder.arcScript
        script.set_inWidth(bitmap.width)
        script.set_inHeight(bitmap.height)
        val sourceAllocation = Allocation.createFromBitmap(
            holder.renderScript, bitmap,
            Allocation.MipmapControl.MIPMAP_NONE,
            Allocation.USAGE_SCRIPT
        )
        script.set_inImage(sourceAllocation)

//        val targetHeight = bitmap.width
//        val targetWidth = bitmap.height
//        val config = bitmap.config
        script.forEach_arc_quart_clockwise(holder.arcAllocation, holder.arcAllocation)
        holder.arcAllocation.copyTo(holder.bitmap)
        sourceAllocation.destroy()

        val now = SystemClock.elapsedRealtime()
        Log.i(TAG, "Spent ${now - then}ms to curve a quart")
        return holder.bitmap
    }

    override fun recycle() {
        holder?.recycle()
        holder = null
    }

    /**
     * @author Artem Chepurnoy
     */
    private class Holder(context: Context, val bitmap: Bitmap) {
        val renderScript = RenderScript.create(context)

        val arcScript = ScriptC_arc(renderScript)

        val arcAllocation = Allocation.createFromBitmap(
            renderScript, bitmap,
            Allocation.MipmapControl.MIPMAP_NONE,
            Allocation.USAGE_SCRIPT
        )

        fun recycle() {
            renderScript.destroy()
            bitmap.recycle()
        }
    }

}