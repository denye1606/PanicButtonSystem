package com.example.panicbutton

actual object PdfGenerator {
    actual fun generateHistoryPdf(history: List<HistoryEntry>, outputStream: Any) {
        // iOS implementation would use UIGraphicsPDFRenderer
        // For now, this is a placeholder
    }
}
