package com.example.panicbutton

// We'll use a wrapper or just expect/actual for PDF generation
expect object PdfGenerator {
    fun generateHistoryPdf(history: List<HistoryEntry>, outputStream: Any)
}
