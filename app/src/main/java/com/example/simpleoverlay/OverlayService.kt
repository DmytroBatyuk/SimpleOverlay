package com.example.simpleoverlay

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.widget.ImageView
import android.widget.FrameLayout
import android.opengl.ETC1.getWidth
import android.opengl.ETC1.getHeight
import android.R.attr.y
import android.R.attr.x
import android.opengl.ETC1.getHeight
import android.opengl.ETC1.getWidth
import android.view.*
import java.util.zip.Inflater
import android.view.WindowManager
import android.R.attr.y
import android.R.attr.x
import android.graphics.Point
import android.os.Build
import android.os.SystemClock
import android.util.Log
import android.view.Gravity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.marginBottom
import androidx.core.view.marginLeft
import androidx.core.view.marginRight
import androidx.core.view.marginTop


private const val DRAWABLE_RES_ID_ARG = "drawable_res_id"
private const val X_PERCENTAGE_ARG = "x_percentage"
private const val Y_PERCENTAGE_ARG = "y_percentage"
private const val TOP_PADDING_ARG = "top_padding"
private const val MATCH_PARENT_W_ARG = "match_parent_w"
private const val MATCH_PARENT_H_ARG = "match_parent_h"

private const val DEBUG = false
class OverlayService : Service() {

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }

    var drawableToViewMap = hashMapOf<Int, ViewGroup>()


    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.e("SERVICE", "onStartCommand")
        val drawableResId = intent.getIntExtra(DRAWABLE_RES_ID_ARG, R.drawable.ic_account_circle_black_24dp)
        if (!drawableToViewMap.contains(drawableResId)) {
            val xPercentage = intent.getIntExtra(X_PERCENTAGE_ARG, 0)
            val yPercentage = intent.getIntExtra(Y_PERCENTAGE_ARG, 0)
            val topPadding = intent.getIntExtra(TOP_PADDING_ARG, 0)
            val isMatchParentW = intent.getBooleanExtra(MATCH_PARENT_W_ARG, false)
            val isMatchParentH = intent.getBooleanExtra(MATCH_PARENT_H_ARG, false)
            val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val inflater = LayoutInflater.from(application)
            val view = inflater.inflate(R.layout.overlay_layout, null) as ViewGroup
            drawableToViewMap.put(drawableResId, view)
            val iv = view.findViewById<ImageView>(R.id.imageView)



            iv.setImageResource(drawableResId)
            val ivLayoutParams = iv.layoutParams as ConstraintLayout.LayoutParams
            iv.layoutParams = ivLayoutParams

            val params = WindowManager.LayoutParams(
                if (isMatchParentW) WindowManager.LayoutParams.MATCH_PARENT else WindowManager.LayoutParams.WRAP_CONTENT,
                if (isMatchParentH) WindowManager.LayoutParams.MATCH_PARENT else WindowManager.LayoutParams.WRAP_CONTENT,
                getType(),
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                        or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                        or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            )
            params.gravity = Gravity.TOP or Gravity.LEFT

            wm.addView(view, params)


            var xOffset = 0f
            var yOffset = 0f
            iv.setOnTouchListener(object : View.OnTouchListener {
                override fun onTouch(v: View, event: MotionEvent): Boolean {
                    return when (event.action) {
                        MotionEvent.ACTION_MOVE,
                        MotionEvent.ACTION_DOWN -> {
                            if (DEBUG) {
                                val point = Point()
                                wm.defaultDisplay?.getSize(point)
                                Log.e(
                                    "ON_CLICK",
                                    "screen=$point, e.point=(${event.x.toInt()}, ${event.y.toInt()}), " +
                                            "e.prec=(${event.xPrecision.toInt()}, ${event.yPrecision.toInt()}), " +
                                            "e.raw=(${event.rawX.toInt()}, ${event.rawY.toInt()}), " +
                                            "v.size=(${v.width}, ${v.height}), " +
                                            "v.margin=(${v.marginLeft}, ${v.marginTop}, ${v.marginRight}, ${v.marginBottom}), " +
                                            "v.padding=(${v.paddingLeft}, ${v.paddingTop}, ${v.paddingRight}, ${v.paddingBottom}), " +
                                            "view.padding=(${view.paddingLeft}, ${view.paddingTop}, ${view.paddingRight}, ${view.paddingBottom}), " +
                                            "view.padding=(${view.paddingLeft}, ${view.paddingTop}, ${view.paddingRight}, ${view.paddingBottom})"
                                )
                            }

                            xOffset = v.width / 2f
                            yOffset = v.height / 2f + topPadding
                            val x = event.rawX - xOffset
                            val y = event.rawY - yOffset
                            val params = WindowManager.LayoutParams(
                                if (isMatchParentW) WindowManager.LayoutParams.MATCH_PARENT else WindowManager.LayoutParams.WRAP_CONTENT,
                                if (isMatchParentH) WindowManager.LayoutParams.MATCH_PARENT else WindowManager.LayoutParams.WRAP_CONTENT,
                                x.toInt(), y.toInt(),
                                getType(),
                                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                                        WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH or
                                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                                PixelFormat.TRANSLUCENT
                            )
                            params.gravity = Gravity.TOP or Gravity.LEFT
                            wm.updateViewLayout(view, params)
                            true
                        }
                        else -> false
                    }
                }
            })

            val point = Point()
            wm.defaultDisplay?.getSize(point)
            if (DEBUG) {
                Log.e("INIT", "screen=$point")
            }

            val x = point.x * xPercentage / 100f
            val y = point.y * yPercentage / 100f
            iv.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    iv.viewTreeObserver.removeOnGlobalLayoutListener(this)

                    iv.dispatchTouchEvent(
                        MotionEvent.obtain(
                            SystemClock.uptimeMillis(),
                            SystemClock.uptimeMillis() + 100,
                            MotionEvent.ACTION_DOWN,
                            x,
                            y,
                            0
                        )
                    )
                }
            })

        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        drawableToViewMap.values.forEach {
            wm.removeView(it)
        }
    }

    private fun getType() = (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
    } else {
        WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
    })

    companion object {
        fun createIntent(
            context: Context,
            drawableResId: Int,
            xPercentage: Int,
            yPercentage: Int,
            topPadding: Int,
            isMatchParentW: Boolean,
            isMatchParentH: Boolean
        ) =
            Intent(context, OverlayService::class.java).apply {
                putExtra(DRAWABLE_RES_ID_ARG, drawableResId)
                putExtra(X_PERCENTAGE_ARG, xPercentage)
                putExtra(Y_PERCENTAGE_ARG, yPercentage)
                putExtra(TOP_PADDING_ARG, topPadding)
                putExtra(MATCH_PARENT_W_ARG, isMatchParentW)
                putExtra(MATCH_PARENT_H_ARG, isMatchParentH)
            }
    }
}
