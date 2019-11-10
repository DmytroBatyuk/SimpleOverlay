package com.example.simpleoverlay

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<View>(R.id.showOverlayButton).setOnClickListener {
            val overlays = arrayListOf(
//                OverlayInfo(
//                    R.drawable.rect, 0, 50
//                ),
                OverlayInfo(
                    R.drawable.peak_station, 0, 78, true
                ),
                OverlayInfo(
                    R.drawable.peak_mid_banner, 0, 50, true
                ),
                OverlayInfo(
                    R.drawable.peak_top_banner, 0, 0, true
                )
            )
            showOverlays(overlays)
        }

        findViewById<View>(R.id.hideOverlayButton).setOnClickListener {
            hideOverlay()
        }
    }


    private fun showOverlays(overlays: List<OverlayInfo>) {
        val rect = Rect()
        window.decorView.getWindowVisibleDisplayFrame(rect)
        val statusBarHeight = rect.top
        if (!isSystemAlertPermissionGranted(this@MainActivity)) {
            requestSystemAlertPermission(this@MainActivity, 1)
        } else {
            overlays.forEach {
                startService(
                    OverlayService.createIntent(
                        application,
                        it.drawableResId,
                        it.xPercentage,
                        it.yPercentage,
                        statusBarHeight,
                        it.isMatchParentW,
                        it.isMatchParentH
                    )
                )
            }
        }
    }

    private fun hideOverlay() {
        stopService(Intent(application, OverlayService::class.java))
    }

    private fun requestSystemAlertPermission(context: Activity, requestCode: Int) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return
        val packageName = context.packageName
        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
        context.startActivityForResult(intent, requestCode)
    }

    private fun isSystemAlertPermissionGranted(context: Context): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(context)
    }
}

private data class OverlayInfo(
    val drawableResId: Int,
    val xPercentage: Int,
    val yPercentage: Int,
    val isMatchParentW: Boolean = false,
    val isMatchParentH: Boolean = false
)