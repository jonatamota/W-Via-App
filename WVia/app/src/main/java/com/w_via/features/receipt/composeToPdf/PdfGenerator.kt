/*
 * Copyright (C) 2025 Jaressen Kyle Salcedo
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
*/
package com.w_via.features.receipt.composeToPdf

//noinspection SuspiciousImport
import android.R
import android.app.Activity
import android.content.Context
import android.graphics.pdf.PdfDocument
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.view.ViewTreeObserver
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.io.OutputStream
import kotlin.coroutines.resume

class PdfGenerator(private val context: Context) {

    /**
     * Generates a PDF from the given Composables.
     *
     * @param pageSize Tamanho fixo da página. Quando **null**, o gerador mede o
     *                 Composable e usa as dimensões reais como tamanho da página
     *                 (wrap-content mode).
     * @param dpi      DPI usado no modo wrap-content (ignorado se [pageSize] for fornecido).
     * @param resolutionMultiplier Multiplicador da resolução interna no modo de tamanho
     *                 fixo. Aumenta a densidade de rasterização (texto continua vetorial;
     *                 bitmaps e camadas escalonadas ficam mais nítidos na impressão). O
     *                 canvas é reescalado para manter o tamanho físico da página.
     * @param maxWidthPx Largura máxima em pixels no modo wrap-content. Necessário para
     *                   que `fillMaxWidth()` funcione corretamente. Padrão: largura A4 a 72 dpi.
     * @param margin   Margem aplicada ao redor do conteúdo.
     */
    suspend fun generate(
        outputStream: OutputStream,
        pageSize: PdfPageSize? = PdfPageSize.A4(72),
        dpi: Int = 72,
        resolutionMultiplier: Float = 3f,
        maxWidthPx: Int = (8.27 * 72).toInt(), // largura A4 @ 72dpi como padrão
        margin: Dp = 0.dp,
        timeout: Long = 3000,
        pages: List<@Composable () -> Unit>
    ): Result<String> = withContext(Dispatchers.Main) {

        outputStream.use { out ->
            val pdfDocument = PdfDocument()
            val imageMonitor = PdfImageMonitor()

            val effectiveDpi = pageSize?.dpi ?: dpi
            // Em modo de tamanho fixo, aumenta a resolução interna de rasterização:
            // o texto permanece vetorial e bitmaps/camadas escalonadas ganham nitidez.
            val densityScale =
                (effectiveDpi / 72f) * (if (pageSize != null) resolutionMultiplier else 1f)
            val pdfDensity = object : Density {
                override val density: Float = densityScale
                override val fontScale: Float = 1f
            }

            val marginPx = (margin.value * (effectiveDpi / 160f)).toInt()

            try {
                pages.forEachIndexed { index, pageContent ->

                    val contentReady = CompletableDeferred<Unit>()
                    var viewAttached = false
                    var page: PdfDocument.Page? = null

                    val composeView = ComposeView(context = context)
                    val rootLayout =
                        (context as? Activity)?.window?.decorView?.findViewById<ViewGroup>(R.id.content)
                            ?: throw IllegalStateException("Context must be an Activity")

                    try {
                        // ── Modo tamanho fixo ──────────────────────────────────────────
                        if (pageSize != null) {
                            val contentWidth = pageSize.width - marginPx * 2
                            val contentHeight = pageSize.height - marginPx * 2
                            val contentWidthPx = (contentWidth * resolutionMultiplier).toInt()
                            val contentHeightPx = (contentHeight * resolutionMultiplier).toInt()

                            composeView.apply {
                                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
                                setContent {
                                    CompositionLocalProvider(
                                        LocalPdfImageMonitor provides imageMonitor,
                                        LocalDensity provides pdfDensity
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(contentWidth.dp, contentHeight.dp)
                                                .onGloballyPositioned {
                                                    if (!contentReady.isCompleted) contentReady.complete(Unit)
                                                }
                                        ) { pageContent() }
                                    }
                                }
                            }

                            composeView.alpha = 0f
                            rootLayout.addView(composeView, LayoutParams(contentWidthPx, contentHeightPx))

                            withTimeout(timeout) { contentReady.await() }
                            viewAttached = true

                            waitForNextFrame(composeView)
                            imageMonitor.waitForImages(composeView)

                            composeView.measure(
                                View.MeasureSpec.makeMeasureSpec(contentWidthPx, View.MeasureSpec.EXACTLY),
                                View.MeasureSpec.makeMeasureSpec(contentHeightPx, View.MeasureSpec.EXACTLY)
                            )
                            composeView.layout(0, 0, contentWidthPx, contentHeightPx)
                            waitForDrawReady(composeView)

                            val pageInfo = PdfDocument.PageInfo.Builder(
                                pageSize.width, pageSize.height, index + 1
                            ).create()
                            page = pdfDocument.startPage(pageInfo)

                            page.canvas.save()
                            page.canvas.translate(marginPx.toFloat(), marginPx.toFloat())
                            page.canvas.scale(1f / resolutionMultiplier, 1f / resolutionMultiplier)
                            composeView.draw(page.canvas)
                            page.canvas.restore()

                        // ── Modo wrap-content (adapta ao Composable) ───────────────────
                        } else {
                            composeView.apply {
                                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
                                setContent {
                                    CompositionLocalProvider(
                                        LocalPdfImageMonitor provides imageMonitor,
                                        LocalDensity provides pdfDensity
                                    ) {
                                        Box(
                                            modifier = Modifier.onGloballyPositioned {
                                                if (!contentReady.isCompleted) contentReady.complete(Unit)
                                            }
                                        ) { pageContent() }
                                    }
                                }
                            }

                            composeView.alpha = 0f
                            // AT_MOST na largura para que fillMaxWidth() funcione;
                            // UNSPECIFIED na altura para que o conteúdo declare o próprio tamanho
                            rootLayout.addView(
                                composeView,
                                LayoutParams(maxWidthPx, LayoutParams.WRAP_CONTENT)
                            )

                            withTimeout(timeout) { contentReady.await() }
                            viewAttached = true

                            waitForNextFrame(composeView)
                            imageMonitor.waitForImages(composeView)

                            composeView.measure(
                                View.MeasureSpec.makeMeasureSpec(maxWidthPx, View.MeasureSpec.AT_MOST),
                                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                            )

                            val measuredW = composeView.measuredWidth.coerceAtLeast(1)
                            val measuredH = composeView.measuredHeight.coerceAtLeast(1)

                            // Dimensões reais da página = conteúdo + margens
                            val pageW = measuredW + marginPx * 2
                            val pageH = measuredH + marginPx * 2

                            composeView.layout(0, 0, measuredW, measuredH)
                            waitForDrawReady(composeView)

                            val pageInfo = PdfDocument.PageInfo.Builder(pageW, pageH, index + 1).create()
                            page = pdfDocument.startPage(pageInfo)

                            page.canvas.save()
                            page.canvas.translate(marginPx.toFloat(), marginPx.toFloat())
                            composeView.draw(page.canvas)
                            page.canvas.restore()
                        }

                    } catch (e: Exception) {
                        e.printStackTrace()
                        return@withContext Result.failure(e)
                    } finally {
                        if (viewAttached) rootLayout.removeView(composeView)
                        page?.let { pdfDocument.finishPage(it) }
                    }
                }

                withContext(Dispatchers.IO) {
                    pdfDocument.writeTo(out)
                    out.flush()
                }

                return@withContext Result.success("Successfully generated ${pages.size} pages!")
            } catch (e: Exception) {
                e.printStackTrace()
                return@withContext Result.failure(e)
            } finally {
                pdfDocument.close()
            }
        }
    }

    @Deprecated("Obsolete")
    private fun Int.toDp(dpi: Int): Dp {
        // 160 is the baseline dpi of Android
        val scaleFactor = dpi / 160f
        return (this / scaleFactor).dp
    }

    private suspend fun waitForNextFrame(view: View) = suspendCancellableCoroutine { continuation ->
        view.post {
            continuation.resume(Unit)
        }
    }

    private suspend fun waitForDrawReady(view: View) = suspendCancellableCoroutine { continuation ->
        var resumed = false
        val observer = view.viewTreeObserver
        val listener = object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                if (!resumed) {
                    resumed = true
                    view.viewTreeObserver.removeOnPreDrawListener(this)
                    continuation.resume(Unit)
                }
                return true
            }
        }
        observer.addOnPreDrawListener(listener)

        // post to check if view is already ready
        view.post {
            if (!resumed && view.width > 0 && view.height > 0 && view.isAttachedToWindow) {
                resumed = true
                observer.removeOnPreDrawListener(listener)
                continuation.resume(Unit)
            }
        }
    }
}