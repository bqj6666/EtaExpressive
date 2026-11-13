package io.github.mangi.eta.ui.app

import android.os.SystemClock
import io.github.mangi.eta.core.AndroidAgentLogger

/**
 * 临时导航耗时探针：用来定位「点开设置页那一瞬间卡一下」到底花在哪一段。
 *
 * 只在 UI 线程打点，开销是几条日志；定位完成后整份删除。
 */
internal object NavPerfProbe {
    /** 跟随性能诊断开关：默认关闭，打开诊断后才会打点，避免平时刷日志。 */
    private val enabled: Boolean get() = io.github.mangi.eta.ui.components.StreamPerformanceDiagnostics.enabled

    private var anchor = 0L

    /** 记一次起点（例如点击导航那一刻），后续 mark 会同时输出相对它的耗时。 */
    fun anchor(label: String) {
        if (!enabled) return
        anchor = SystemClock.uptimeMillis()
        log(label)
    }

    fun mark(label: String) {
        if (!enabled) return
        log(label)
    }

    /** 包一段同步调用，输出它自己的耗时。 */
    fun measure(label: String, block: () -> Unit) {
        if (!enabled) {
            block()
            return
        }
        val start = SystemClock.uptimeMillis()
        try {
            block()
        } finally {
            log("$label took=${SystemClock.uptimeMillis() - start}ms")
        }
    }

    private fun log(label: String) {
        val now = SystemClock.uptimeMillis()
        val delta = if (anchor == 0L) 0L else now - anchor
        AndroidAgentLogger.info("navperf $label t=$now dt=${delta}ms")
    }
}
