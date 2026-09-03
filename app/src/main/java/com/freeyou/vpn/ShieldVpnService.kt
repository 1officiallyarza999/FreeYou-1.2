package com.freeyou.vpn

import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor
import android.util.Log
import com.freeyou.data.AdultBlockEngine
import com.freeyou.data.BlockRepo
import kotlinx.coroutines.*
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.nio.ByteBuffer

class ShieldVpnService : VpnService() {
    private var vpnInterface: ParcelFileDescriptor? = null
    private val vpnScope = CoroutineScope(Dispatchers.IO + Job())
    private var isRunning = false

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        if (action == "STOP") {
            stopVpn()
            return START_NOT_STICKY
        }
        startVpn()
        return START_STICKY
    }

    private fun startVpn() {
        if (isRunning) return
        isRunning = true
        try {
            val builder = Builder()
                .setSession("FreeYou Shield")
                .addAddress("10.0.0.2", 32)
                .addDnsServer("10.0.0.1")
                .addRoute("10.0.0.1", 32)
            
            vpnInterface = builder.establish()
            
            vpnScope.launch {
                runVpnLoop()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            isRunning = false
        }
    }

    private fun stopVpn() {
        isRunning = false
        vpnScope.cancel()
        vpnInterface?.close()
        vpnInterface = null
        stopSelf()
    }

    private suspend fun runVpnLoop() = withContext(Dispatchers.IO) {
        val pfd = vpnInterface ?: return@withContext
        val input = FileInputStream(pfd.fileDescriptor)
        val output = FileOutputStream(pfd.fileDescriptor)
        val buffer = ByteArray(32767)
        
        // We will forward non-blocked queries to Google DNS or Cloudflare
        val realDnsIp = InetAddress.getByName("8.8.8.8")
        val dnsSocket = DatagramSocket()

        while (isRunning && isActive) {
            try {
                val length = input.read(buffer)
                if (length > 0) {
                    handlePacket(buffer, length, output, dnsSocket, realDnsIp)
                }
            } catch (e: Exception) {
                // Ignore timeouts/stream closures
            }
        }
    }

    private fun handlePacket(
        packet: ByteArray,
        length: Int,
        output: FileOutputStream,
        dnsSocket: DatagramSocket,
        realDnsIp: InetAddress
    ) {
        val buffer = ByteBuffer.wrap(packet, 0, length)
        val versionAndIhl = buffer.get().toInt()
        val ihl = versionAndIhl and 0x0F
        val ipHeaderLength = ihl * 4
        
        if (buffer.capacity() < ipHeaderLength + 8) return // Too small
        
        buffer.position(9)
        val protocol = buffer.get().toInt()
        if (protocol != 17) return // Not UDP
        
        buffer.position(12)
        val srcIp = ByteArray(4)
        buffer.get(srcIp)
        val destIp = ByteArray(4)
        buffer.get(destIp)
        
        buffer.position(ipHeaderLength)
        val srcPort = buffer.short.toInt() and 0xFFFF
        val destPort = buffer.short.toInt() and 0xFFFF
        val udpLength = buffer.short.toInt() and 0xFFFF
        
        if (destPort == 53) {
            // It's a DNS query!
            val dnsPayloadLength = udpLength - 8
            if (dnsPayloadLength <= 0) return
            val dnsPayload = ByteArray(dnsPayloadLength)
            buffer.position(ipHeaderLength + 8)
            buffer.get(dnsPayload)
            
            val domain = DnsUtils.extractDomain(dnsPayload)
            
            val isAdult = AdultBlockEngine.isUrlOrHostBlocked(domain) != null
            val isCustomBlocked = BlockRepo.state.value.blocked.any { domain.contains(it) }
            val isStrict = BlockRepo.state.value.strict
            
            if (isAdult || isCustomBlocked) {
                // Blocked! Return dummy IP 0.0.0.0
                val dnsResponsePayload = DnsUtils.createDnsBlockResponse(dnsPayload)
                sendUdpResponse(output, destIp, srcIp, 53, srcPort, dnsResponsePayload)
            } else {
                // Allowed! Forward to real DNS, wait for response, and route back
                forwardDnsQuery(dnsPayload, output, destIp, srcIp, srcPort, dnsSocket, realDnsIp)
            }
        }
    }

    private fun forwardDnsQuery(
        dnsPayload: ByteArray,
        output: FileOutputStream,
        tunDestIp: ByteArray,
        tunSrcIp: ByteArray,
        tunSrcPort: Int,
        dnsSocket: DatagramSocket,
        realDnsIp: InetAddress
    ) {
        vpnScope.launch(Dispatchers.IO) {
            try {
                val outPacket = DatagramPacket(dnsPayload, dnsPayload.size, realDnsIp, 53)
                dnsSocket.send(outPacket)
                
                val inBuffer = ByteArray(1024)
                val inPacket = DatagramPacket(inBuffer, inBuffer.size)
                dnsSocket.soTimeout = 2000
                dnsSocket.receive(inPacket)
                
                val dnsResponsePayload = inBuffer.copyOfRange(0, inPacket.length)
                sendUdpResponse(output, tunDestIp, tunSrcIp, 53, tunSrcPort, dnsResponsePayload)
            } catch (e: Exception) {
                // Timeout or error forwarding DNS
            }
        }
    }

    private fun sendUdpResponse(
        output: FileOutputStream,
        srcIp: ByteArray,
        destIp: ByteArray,
        srcPort: Int,
        destPort: Int,
        udpPayload: ByteArray
    ) {
        val totalLength = 20 + 8 + udpPayload.size
        val responsePacket = ByteArray(totalLength)
        val buffer = ByteBuffer.wrap(responsePacket)
        
        // --- IPv4 Header ---
        buffer.put(0x45.toByte()) // Version(4) + IHL(5)
        buffer.put(0x00.toByte()) // TOS
        buffer.putShort(totalLength.toShort()) // Total Length
        buffer.putShort(0) // Identification
        buffer.putShort(0) // Flags/Fragment offset
        buffer.put(64.toByte()) // TTL
        buffer.put(17.toByte()) // Protocol (UDP)
        buffer.putShort(0) // Checksum (calculate later)
        buffer.put(srcIp)
        buffer.put(destIp)
        
        // Calculate IP Checksum
        val ipChecksum = calculateChecksum(responsePacket, 0, 20)
        buffer.putShort(10, ipChecksum.toShort())
        
        // --- UDP Header ---
        buffer.position(20)
        buffer.putShort(srcPort.toShort())
        buffer.putShort(destPort.toShort())
        buffer.putShort((8 + udpPayload.size).toShort())
        buffer.putShort(0) // UDP Checksum (0 = optional in IPv4)
        
        // --- UDP Payload ---
        buffer.put(udpPayload)
        
        try {
            output.write(responsePacket)
        } catch (e: Exception) {}
    }

    private fun calculateChecksum(data: ByteArray, offset: Int, length: Int): Int {
        var sum = 0
        var i = offset
        while (i < offset + length - 1) {
            val word = ((data[i].toInt() and 0xFF) shl 8) or (data[i + 1].toInt() and 0xFF)
            sum += word
            i += 2
        }
        if (length % 2 != 0) {
            sum += ((data[offset + length - 1].toInt() and 0xFF) shl 8)
        }
        while ((sum shr 16) > 0) {
            sum = (sum and 0xFFFF) + (sum shr 16)
        }
        return sum.inv() and 0xFFFF
    }

    override fun onDestroy() {
        super.onDestroy()
        stopVpn()
    }
}
