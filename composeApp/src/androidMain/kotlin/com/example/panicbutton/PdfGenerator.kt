package com.example.panicbutton

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

actual object PdfGenerator {
    actual fun generateHistoryPdf(history: List<HistoryEntry>, outputStream: Any) {
        val stream = outputStream as OutputStream
        val pdfDocument = PdfDocument()
        
        // Page Configuration (A4-ish: 595 x 842)
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas
        
        val titlePaint = Paint().apply {
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            color = Color.BLACK
        }
        val headerPaint = Paint().apply {
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            color = Color.DKGRAY
        }
        val textPaint = Paint().apply {
            textSize = 12f
            typeface = Typeface.DEFAULT
            color = Color.BLACK
        }
        val linePaint = Paint().apply {
            color = Color.LTGRAY
            strokeWidth = 1f
        }

        var y = 50f
        
        // Title
        canvas.drawText("Panic System History Report", 50f, y, titlePaint)
        y += 20f
        val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        canvas.drawText("Generated on: $dateStr", 50f, y, textPaint)
        y += 40f
        
        // Table Headers
        canvas.drawText("Patient Name", 50f, y, headerPaint)
        canvas.drawText("Time / Date", 250f, y, headerPaint)
        canvas.drawText("Response / Total", 450f, y, headerPaint)
        y += 10f
        canvas.drawLine(50f, y, 545f, y, linePaint)
        y += 25f

        // History Rows
        history.forEach { entry ->
            if (y > 780f) {
                pdfDocument.finishPage(page)
                val newPageInfo = PdfDocument.PageInfo.Builder(595, 842, pdfDocument.pages.size + 1).create()
                page = pdfDocument.startPage(newPageInfo)
                canvas = page.canvas
                y = 50f
                
                canvas.drawText("Patient Name", 50f, y, headerPaint)
                canvas.drawText("Time / Date", 250f, y, headerPaint)
                canvas.drawText("Response / Total", 450f, y, headerPaint)
                y += 10f
                canvas.drawLine(50f, y, 545f, y, linePaint)
                y += 25f
            }

            val isSummary = entry.patientName == "Daily Summary"
            if (isSummary) {
                textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textPaint.color = Color.BLUE
            } else {
                textPaint.typeface = Typeface.DEFAULT
                textPaint.color = Color.BLACK
            }

            canvas.drawText(entry.patientName, 50f, y, textPaint)
            canvas.drawText(entry.timestamp, 250f, y, textPaint)
            canvas.drawText(entry.responseTime, 450f, y, textPaint)
            
            y += 20f
        }

        pdfDocument.finishPage(page)
        
        try {
            pdfDocument.writeTo(stream)
        } finally {
            pdfDocument.close()
        }
    }
}
