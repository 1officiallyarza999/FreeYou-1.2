package com.freeyou.vpn

import java.nio.ByteBuffer

object DnsUtils {
    fun extractDomain(dnsPayload: ByteArray): String {
        try {
            val buffer = ByteBuffer.wrap(dnsPayload)
            buffer.position(12) // Skip DNS header (12 bytes)
            val domainBuilder = java.lang.StringBuilder()
            var len = buffer.get().toInt() and 0xFF
            while (len > 0) {
                val label = ByteArray(len)
                buffer.get(label)
                domainBuilder.append(String(label)).append(".")
                len = buffer.get().toInt() and 0xFF
            }
            if (domainBuilder.isNotEmpty()) {
                domainBuilder.deleteCharAt(domainBuilder.length - 1)
            }
            return domainBuilder.toString()
        } catch (e: Exception) {
            return ""
        }
    }

    fun createDnsBlockResponse(request: ByteArray): ByteArray {
        val response = ByteArray(request.size + 16)
        System.arraycopy(request, 0, response, 0, request.size)
        // Set QR=1 (response), AA=1, RA=1, RCODE=0
        response[2] = 0x84.toByte() // Flags MSB
        response[3] = 0x80.toByte() // Flags LSB
        // ANCOUNT = 1
        response[6] = 0x00
        response[7] = 0x01
        
        var offset = request.size
        // Name (Pointer to offset 12)
        response[offset++] = 0xC0.toByte()
        response[offset++] = 0x0C.toByte()
        // Type A (1)
        response[offset++] = 0x00
        response[offset++] = 0x01
        // Class IN (1)
        response[offset++] = 0x00
        response[offset++] = 0x01
        // TTL (300)
        response[offset++] = 0x00
        response[offset++] = 0x00
        response[offset++] = 0x01
        response[offset++] = 0x2C
        // RDLENGTH (4)
        response[offset++] = 0x00
        response[offset++] = 0x04
        // IP 0.0.0.0
        response[offset++] = 0x00
        response[offset++] = 0x00
        response[offset++] = 0x00
        response[offset++] = 0x00
        
        return response.copyOfRange(0, offset)
    }
}
