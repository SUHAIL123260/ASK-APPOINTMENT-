package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.AadhaarProfile
import com.example.data.AadhaarSetting
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.Locale

object PdfAadhaarGenerator {

    fun generateEaadhaarPdf(context: Context, profile: AadhaarProfile, setting: AadhaarSetting): File {
        val document = PdfDocument()
        val width = 595 // Standard A4 width in points
        val height = 842 // Standard A4 height in points
        val pageInfo = PdfDocument.PageInfo.Builder(width, height, 1).create()
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val titlePaint = Paint().apply {
            color = Color.parseColor("#0F3C6D") // AadhaarNavy
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }

        val subTitlePaint = Paint().apply {
            color = Color.parseColor("#D32F2F") // Red Demo warning
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }

        val headingPaint = Paint().apply {
            color = Color.parseColor("#1E5899")
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val bodyPaint = Paint().apply {
            color = Color.parseColor("#1A2433")
            textSize = 11f
            typeface = Typeface.DEFAULT
            isAntiAlias = true
        }

        val boldPaint = Paint().apply {
            color = Color.parseColor("#1A2433")
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val headerBgPaint = Paint().apply {
            color = Color.parseColor("#EBF1F6")
            style = Paint.Style.FILL
        }

        val borderPaint = Paint().apply {
            color = Color.parseColor("#0F3C6D")
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }

        val cardBgPaint = Paint().apply {
            color = Color.parseColor("#FFFFFF")
            style = Paint.Style.FILL
        }

        // 1. Draw Header Banner
        canvas.drawRect(0f, 0f, width.toFloat(), 100f, headerBgPaint)
        canvas.drawLine(0f, 100f, width.toFloat(), 100f, borderPaint)
        
        canvas.drawText("भारत सरकार / GOVERNMENT OF INDIA", (width / 2).toFloat(), 35f, titlePaint)
        canvas.drawText("Unique Identification Authority of India (UIDAI Demo)", (width / 2).toFloat(), 58f, Paint(titlePaint).apply { textSize = 14f; color = Color.parseColor("#333333") })
        canvas.drawText("⚠️ EDUCATIONAL & PRACTICE SIMULATOR ONLY • NOT AN OFFICIAL GOVT DOCUMENT ⚠️", (width / 2).toFloat(), 82f, subTitlePaint)

        // 2. Draw Simulated Aadhaar Letter Box
        var yPos = 130f
        canvas.drawText("Enrollment Details / नामांकन विवरण", 40f, yPos, headingPaint)
        yPos += 25f

        canvas.drawText("To,", 40f, yPos, bodyPaint)
        yPos += 18f
        canvas.drawText("${profile.nameEnglish} / ${profile.nameHindi}", 40f, yPos, boldPaint)
        yPos += 16f
        canvas.drawText("S/O: ${profile.fatherName}", 40f, yPos, bodyPaint)
        yPos += 16f
        
        val addrLines = profile.addressEnglish.chunked(60)
        addrLines.forEach { line ->
            canvas.drawText(line, 40f, yPos, bodyPaint)
            yPos += 16f
        }
        canvas.drawText("PIN Code: ${profile.pinCode}", 40f, yPos, boldPaint)
        yPos += 16f
        canvas.drawText("Mobile: ${profile.mobileNumber}", 40f, yPos, bodyPaint)

        // 3. Draw Cut-out Card Section (Bottom half)
        yPos = 420f
        val cardTop = yPos
        val cardLeft = 40f
        val cardRight = width - 40f
        val cardBottom = cardTop + 240f

        // Scissors dashed line above card
        val dashPaint = Paint().apply {
            color = Color.GRAY
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        canvas.drawLine(cardLeft, cardTop - 20f, cardRight, cardTop - 20f, dashPaint)
        canvas.drawText("--------------------------------- CUT HERE (SIMULATED e-AADHAAR CARD) ---------------------------------", (width / 2).toFloat(), cardTop - 25f, Paint(bodyPaint).apply { textAlign = Paint.Align.CENTER; textSize = 9f; color = Color.GRAY })

        // Outer Card Border
        canvas.drawRoundRect(RectF(cardLeft, cardTop, cardRight, cardBottom), 12f, 12f, cardBgPaint)
        canvas.drawRoundRect(RectF(cardLeft, cardTop, cardRight, cardBottom), 12f, 12f, borderPaint)

        // Card Header Bar
        val cardHeaderBg = Paint().apply { color = Color.parseColor("#0F3C6D") }
        canvas.drawRect(cardLeft, cardTop, cardRight, cardTop + 35f, cardHeaderBg)
        val whiteBold = Paint(boldPaint).apply { color = Color.WHITE; textAlign = Paint.Align.CENTER; textSize = 13f }
        canvas.drawText("भारत सरकार • GOVERNMENT OF INDIA", (width / 2).toFloat(), cardTop + 23f, whiteBold)

        // Card Body Content (Left side: details, Right side: QR Box simulation)
        val innerY = cardTop + 60f
        canvas.drawText("नाम / Name: ${profile.nameEnglish}", cardLeft + 20f, innerY, boldPaint)
        canvas.drawText("जन्म तिथि / DOB: ${profile.dob}", cardLeft + 20f, innerY + 22f, bodyPaint)
        canvas.drawText("लिंग / Gender: ${profile.gender} / ${profile.genderHindi}", cardLeft + 20f, innerY + 44f, bodyPaint)
        canvas.drawText("VID: ${profile.virtualId}", cardLeft + 20f, innerY + 66f, Paint(bodyPaint).apply { color = Color.DKGRAY })

        // QR Code Placeholder Box
        val qrBox = RectF(cardRight - 130f, innerY - 10f, cardRight - 20f, innerY + 100f)
        val qrPaint = Paint().apply { color = Color.parseColor("#EBF1F6"); style = Paint.Style.FILL }
        canvas.drawRect(qrBox, qrPaint)
        canvas.drawRect(qrBox, borderPaint)
        canvas.drawText("[ SIMULATED QR ]", qrBox.centerX(), qrBox.centerY(), Paint(bodyPaint).apply { textAlign = Paint.Align.CENTER; textSize = 9f })

        // Big Aadhaar Number at Bottom of Card
        val aadhaarNumPaint = Paint().apply {
            color = Color.parseColor("#D32F2F")
            textSize = 22f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText(profile.aadhaarNumber, (width / 2).toFloat(), cardBottom - 35f, aadhaarNumPaint)
        canvas.drawText("मेरा आधार, मेरी पहचान • DEMO SIMULATOR", (width / 2).toFloat(), cardBottom - 15f, Paint(boldPaint).apply { textAlign = Paint.Align.CENTER; textSize = 10f; color = Color.parseColor("#0F3C6D") })

        // 4. Footer Note & Password Instructions
        yPos = cardBottom + 40f
        val warningBox = RectF(40f, yPos, width - 40f, yPos + 65f)
        canvas.drawRoundRect(warningBox, 8f, 8f, Paint().apply { color = Color.parseColor("#FFF3E0") })
        
        val warnText = Paint(boldPaint).apply { color = Color.parseColor("#E65100"); textSize = 11f }
        canvas.drawText("🔐 e-Aadhaar Demo Password Instruction:", 55f, yPos + 22f, warnText)
        canvas.drawText("To open simulated PDF files, use the first 4 letters of name in CAPITAL + Birth Year.", 55f, yPos + 40f, bodyPaint)
        canvas.drawText("For this profile (${profile.nameEnglish}, DOB ${profile.dob}), the Demo Password is: ${profile.getPasswordHint()}", 55f, yPos + 55f, warnText)

        document.finishPage(page)

        val pdfDir = File(context.cacheDir, "eaadhaar_demo").apply { mkdirs() }
        val fileName = "eAadhaar_${profile.aadhaarNumber.replace(" ", "")}.pdf"
        val file = File(pdfDir, fileName)
        
        FileOutputStream(file).use { out ->
            document.writeTo(out)
        }
        document.close()
        return file
    }

    fun printPdf(context: Context, file: File, jobName: String) {
        try {
            val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
            if (printManager == null) {
                Toast.makeText(context, "Print service not available on this device", Toast.LENGTH_SHORT).show()
                return
            }
            val adapter = PdfPrintDocumentAdapter(file, jobName)
            printManager.print(jobName, adapter, null)
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to initiate print: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun sharePdf(context: Context, file: File, shareToWhatsAppOnly: Boolean = false) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "e-Aadhaar Demo Card - ${file.nameWithoutExtension}")
                putExtra(Intent.EXTRA_TEXT, "Here is the simulated e-Aadhaar Demo PDF generated from MeriPehchaan Simulator.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                if (shareToWhatsAppOnly) {
                    setPackage("com.whatsapp")
                }
            }
            val chooser = Intent.createChooser(intent, "Share e-Aadhaar Demo via")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            if (shareToWhatsAppOnly) {
                Toast.makeText(context, "WhatsApp not found. Opening standard share...", Toast.LENGTH_SHORT).show()
                sharePdf(context, file, false)
            } else {
                Toast.makeText(context, "Failed to share: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private class PdfPrintDocumentAdapter(
        private val file: File,
        private val jobName: String
    ) : PrintDocumentAdapter() {

        override fun onLayout(
            oldAttributes: PrintAttributes?,
            newAttributes: PrintAttributes?,
            cancellationSignal: CancellationSignal?,
            callback: LayoutResultCallback?,
            extras: Bundle?
        ) {
            if (cancellationSignal?.isCanceled == true) {
                callback?.onLayoutCancelled()
                return
            }
            val info = PrintDocumentInfo.Builder(jobName)
                .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                .setPageCount(1)
                .build()
            callback?.onLayoutFinished(info, true)
        }

        override fun onWrite(
            pages: Array<out PageRange>?,
            destination: ParcelFileDescriptor?,
            cancellationSignal: CancellationSignal?,
            callback: WriteResultCallback?
        ) {
            var input: FileInputStream? = null
            var output: FileOutputStream? = null
            try {
                input = FileInputStream(file)
                output = FileOutputStream(destination?.fileDescriptor)
                input.copyTo(output)
                callback?.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
            } catch (e: Exception) {
                callback?.onWriteFailed(e.message)
            } finally {
                try { input?.close() } catch (_: Exception) {}
                try { output?.close() } catch (_: Exception) {}
            }
        }
    }
}

