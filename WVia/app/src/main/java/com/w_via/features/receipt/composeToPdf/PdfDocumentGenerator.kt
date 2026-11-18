package com.w_via.features.receipt.composeToPdf

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.compose.runtime.Composable
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

/**
 * Gerador de PDF genérico que salva o documento renderizado via [PdfGenerator]
 * no cache do aplicativo e devolve a Uri via FileProvider.
 *
 * Substitui os wrappers específicos por documento (recibo e declaração de óbito),
 * que apenas diferenciavam o nome do arquivo, o tamanho da página e o layout.
 */
class PdfDocumentGenerator(private val context: Context) {

    /**
     * Gera o PDF e retorna a Uri do arquivo gerado, ou null em caso de falha.
     *
     * @param fileName nome do arquivo (ex.: "recibo_123.pdf").
     * @param pageSize tamanho fixo da página; null usa o modo wrap-content do [PdfGenerator].
     * @param pages    layouts Compose, uma entrada por página.
     */
    suspend fun generate(
        fileName: String,
        pageSize: PdfPageSize? = PdfPageSize.A4(72),
        pages: List<@Composable () -> Unit>
    ): Uri? {
        return try {
            val file = File(context.cacheDir, fileName)

            FileOutputStream(file).use { outputStream ->
                val generator = PdfGenerator(context)
                val result = generator.generate(
                    outputStream = outputStream,
                    pageSize = pageSize,
                    pages = pages
                )

                if (result.isSuccess) {
                    FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.provider",
                        file
                    )
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

/**
 * Tamanho do PDF gerado, em KB, já formatado (ex.: "38 KB").
 *
 * A URI retornada pelo [PdfDocumentGenerator] é `content://` (FileProvider);
 * por isso o tamanho é lido via [ContentResolver]/[OpenableColumns.SIZE], com
 * fallback para o arquivo em [Context.cacheDir] quando o provedor não informar
 * o tamanho.
 */
fun pdfSizeKb(context: Context, uri: Uri, fileName: String): String {
    val bytes = runCatching {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val idx = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (idx >= 0 && !cursor.isNull(idx)) cursor.getLong(idx) else -1L
            } else {
                -1L
            }
        } ?: -1L
    }.getOrDefault(-1L)

    val size = if (bytes > 0) {
        bytes
    } else {
        runCatching { File(context.cacheDir, fileName).length() }.getOrDefault(0L)
    }
    return "%.0f KB".format(size / 1024.0)
}
