package com.monkopedia.otli

import com.monkopedia.otli.clang.ClangIndexConfig
import com.monkopedia.otli.clang.consumeAsFlow
import com.monkopedia.otli.clang.getClangService
import java.io.File
import kotlin.system.exitProcess
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking

fun main(args: Array<String>) {
    val compiler = OtliCompiler()
    println("Hello there")
    val testFile = File("test.kt")
    runBlocking {
        val service = getClangService()
        val index = service.index(
            ClangIndexConfig(
                "/home/jmonk/.espressif/tools/esp-clang/esp-18.1.2_20240912/esp-clang/bin/clang",
                listOf(
                    "/home/jmonk/esp_wifi_sample/espnow/cmake-build-debug/config",
                    "/home/jmonk/esp_wifi_sample/espnow/main",
                    "/opt/esp-idf/components/newlib/platform_include",
                    "/opt/esp-idf/components/freertos/config/include",
                    "/opt/esp-idf/components/freertos/config/include/freertos",
                    "/opt/esp-idf/components/freertos/config/xtensa/include",
                    "/opt/esp-idf/components/freertos/FreeRTOS-Kernel/include",
                    "/opt/esp-idf/components/freertos/FreeRTOS-Kernel/portable/xtensa/include",
                    "/opt/esp-idf/components/freertos/FreeRTOS-Kernel/portable/xtensa/include/freertos",
                    "/opt/esp-idf/components/freertos/esp_additions/include",
                    "/opt/esp-idf/components/esp_hw_support/include",
                    "/opt/esp-idf/components/esp_hw_support/include/soc",
                    "/opt/esp-idf/components/esp_hw_support/include/soc/esp32",
                    "/opt/esp-idf/components/esp_hw_support/dma/include",
                    "/opt/esp-idf/components/esp_hw_support/ldo/include",
                    "/opt/esp-idf/components/esp_hw_support/debug_probe/include",
                    "/opt/esp-idf/components/esp_hw_support/port/esp32/.",
                    "/opt/esp-idf/components/esp_hw_support/port/esp32/include",
                    "/opt/esp-idf/components/heap/include",
                    "/opt/esp-idf/components/heap/tlsf",
                    "/opt/esp-idf/components/log/include",
                    "/opt/esp-idf/components/soc/include",
                    "/opt/esp-idf/components/soc/esp32",
                    "/opt/esp-idf/components/soc/esp32/include",
                    "/opt/esp-idf/components/soc/esp32/register",
                    "/opt/esp-idf/components/hal/platform_port/include",
                    "/opt/esp-idf/components/hal/esp32/include",
                    "/opt/esp-idf/components/hal/include",
                    "/opt/esp-idf/components/esp_rom/include",
                    "/opt/esp-idf/components/esp_rom/esp32/include",
                    "/opt/esp-idf/components/esp_rom/esp32/include/esp32",
                    "/opt/esp-idf/components/esp_rom/esp32",
                    "/opt/esp-idf/components/esp_common/include",
                    "/opt/esp-idf/components/esp_system/include",
                    "/opt/esp-idf/components/esp_system/port/soc",
                    "/opt/esp-idf/components/esp_system/port/include/private",
                    "/opt/esp-idf/components/xtensa/esp32/include",
                    "/opt/esp-idf/components/xtensa/include",
                    "/opt/esp-idf/components/xtensa/deprecated_include",
                    "/opt/esp-idf/components/lwip/include",
                    "/opt/esp-idf/components/lwip/include/apps",
                    "/opt/esp-idf/components/lwip/include/apps/sntp",
                    "/opt/esp-idf/components/lwip/lwip/src/include",
                    "/opt/esp-idf/components/lwip/port/include",
                    "/opt/esp-idf/components/lwip/port/freertos/include",
                    "/opt/esp-idf/components/lwip/port/esp32xx/include",
                    "/opt/esp-idf/components/lwip/port/esp32xx/include/arch",
                    "/opt/esp-idf/components/lwip/port/esp32xx/include/sys",
                    "/opt/esp-idf/components/esp_driver_gpio/include",
                    "/opt/esp-idf/components/esp_pm/include",
                    "/opt/esp-idf/components/mbedtls/port/include",
                    "/opt/esp-idf/components/mbedtls/mbedtls/include",
                    "/opt/esp-idf/components/mbedtls/mbedtls/library",
                    "/opt/esp-idf/components/mbedtls/esp_crt_bundle/include",
                    "/opt/esp-idf/components/mbedtls/mbedtls/3rdparty/everest/include",
                    "/opt/esp-idf/components/mbedtls/mbedtls/3rdparty/p256-m",
                    "/opt/esp-idf/components/mbedtls/mbedtls/3rdparty/p256-m/p256-m",
                    "/opt/esp-idf/components/esp_app_format/include",
                    "/opt/esp-idf/components/esp_bootloader_format/include",
                    "/opt/esp-idf/components/app_update/include",
                    "/opt/esp-idf/components/bootloader_support/include",
                    "/opt/esp-idf/components/bootloader_support/bootloader_flash/include",
                    "/opt/esp-idf/components/esp_partition/include",
                    "/opt/esp-idf/components/efuse/include",
                    "/opt/esp-idf/components/efuse/esp32/include",
                    "/opt/esp-idf/components/esp_mm/include",
                    "/opt/esp-idf/components/spi_flash/include",
                    "/opt/esp-idf/components/esp_security/include",
                    "/opt/esp-idf/components/pthread/include",
                    "/opt/esp-idf/components/esp_timer/include",
                    "/opt/esp-idf/components/esp_driver_gptimer/include",
                    "/opt/esp-idf/components/esp_ringbuf/include",
                    "/opt/esp-idf/components/esp_driver_uart/include",
                    "/opt/esp-idf/components/vfs/include",
                    "/opt/esp-idf/components/app_trace/include",
                    "/opt/esp-idf/components/esp_event/include",
                    "/opt/esp-idf/components/nvs_flash/include",
                    "/opt/esp-idf/components/esp_driver_pcnt/include",
                    "/opt/esp-idf/components/esp_driver_spi/include",
                    "/opt/esp-idf/components/esp_driver_mcpwm/include",
                    "/opt/esp-idf/components/esp_driver_ana_cmpr/include",
                    "/opt/esp-idf/components/esp_driver_i2s/include",
                    "/opt/esp-idf/components/sdmmc/include",
                    "/opt/esp-idf/components/esp_driver_sdmmc/include",
                    "/opt/esp-idf/components/esp_driver_sdspi/include",
                    "/opt/esp-idf/components/esp_driver_sdio/include",
                    "/opt/esp-idf/components/esp_driver_dac/include",
                    "/opt/esp-idf/components/esp_driver_rmt/include",
                    "/opt/esp-idf/components/esp_driver_tsens/include",
                    "/opt/esp-idf/components/esp_driver_sdm/include",
                    "/opt/esp-idf/components/esp_driver_i2c/include",
                    "/opt/esp-idf/components/esp_driver_ledc/include",
                    "/opt/esp-idf/components/esp_driver_parlio/include",
                    "/opt/esp-idf/components/esp_driver_usb_serial_jtag/include",
                    "/opt/esp-idf/components/driver/deprecated",
                    "/opt/esp-idf/components/driver/i2c/include",
                    "/opt/esp-idf/components/driver/touch_sensor/include",
                    "/opt/esp-idf/components/driver/twai/include",
                    "/opt/esp-idf/components/driver/touch_sensor/esp32/include",
                    "/opt/esp-idf/components/esp_phy/include",
                    "/opt/esp-idf/components/esp_phy/esp32/include",
                    "/opt/esp-idf/components/esp_vfs_console/include",
                    "/opt/esp-idf/components/esp_netif/include",
                    "/opt/esp-idf/components/wpa_supplicant/include",
                    "/opt/esp-idf/components/wpa_supplicant/port/include",
                    "/opt/esp-idf/components/wpa_supplicant/esp_supplicant/include",
                    "/opt/esp-idf/components/esp_coex/include",
                    "/opt/esp-idf/components/esp_wifi/include",
                    "/opt/esp-idf/components/esp_wifi/include/local",
                    "/opt/esp-idf/components/esp_wifi/wifi_apps/include",
                    "/opt/esp-idf/components/esp_wifi/wifi_apps/nan_app/include",
                    "/opt/esp-idf/components/unity/include",
                    "/opt/esp-idf/components/unity/unity/src",
                    "/opt/esp-idf/components/cmock/CMock/src",
                    "/opt/esp-idf/components/console",
                    "/opt/esp-idf/components/http_parser",
                    "/opt/esp-idf/components/esp-tls",
                    "/opt/esp-idf/components/esp-tls/esp-tls-crypto",
                    "/opt/esp-idf/components/esp_adc/include",
                    "/opt/esp-idf/components/esp_adc/interface",
                    "/opt/esp-idf/components/esp_adc/esp32/include",
                    "/opt/esp-idf/components/esp_adc/deprecated/include",
                    "/opt/esp-idf/components/esp_driver_isp/include",
                    "/opt/esp-idf/components/esp_driver_cam/include",
                    "/opt/esp-idf/components/esp_driver_cam/interface",
                    "/opt/esp-idf/components/esp_driver_jpeg/include",
                    "/opt/esp-idf/components/esp_driver_ppa/include",
                    "/opt/esp-idf/components/esp_eth/include",
                    "/opt/esp-idf/components/esp_gdbstub/include",
                    "/opt/esp-idf/components/esp_hid/include",
                    "/opt/esp-idf/components/tcp_transport/include",
                    "/opt/esp-idf/components/esp_http_client/include",
                    "/opt/esp-idf/components/esp_http_server/include",
                    "/opt/esp-idf/components/esp_https_ota/include",
                    "/opt/esp-idf/components/esp_https_server/include",
                    "/opt/esp-idf/components/esp_psram/include",
                    "/opt/esp-idf/components/esp_lcd/include",
                    "/opt/esp-idf/components/esp_lcd/interface",
                    "/opt/esp-idf/components/protobuf-c/protobuf-c",
                    "/opt/esp-idf/components/protocomm/include/common",
                    "/opt/esp-idf/components/protocomm/include/security",
                    "/opt/esp-idf/components/protocomm/include/transports",
                    "/opt/esp-idf/components/protocomm/include/crypto/srp6a",
                    "/opt/esp-idf/components/protocomm/proto-c",
                    "/opt/esp-idf/components/esp_local_ctrl/include",
                    "/opt/esp-idf/components/espcoredump/include",
                    "/opt/esp-idf/components/espcoredump/include/port/xtensa",
                    "/opt/esp-idf/components/wear_levelling/include",
                    "/opt/esp-idf/components/fatfs/diskio",
                    "/opt/esp-idf/components/fatfs/src",
                    "/opt/esp-idf/components/fatfs/vfs",
                    "/opt/esp-idf/components/idf_test/include",
                    "/opt/esp-idf/components/idf_test/include/esp32",
                    "/opt/esp-idf/components/ieee802154/include",
                    "/opt/esp-idf/components/json/cJSON",
                    "/opt/esp-idf/components/mqtt/esp-mqtt/include",
                    "/opt/esp-idf/components/nvs_sec_provider/include",
                    "/opt/esp-idf/components/perfmon/include",
                    "/opt/esp-idf/components/rt/include",
                    "/opt/esp-idf/components/spiffs/include",
                    "/opt/esp-idf/components/wifi_provisioning/include"
                ),
                listOf("-D__XTENSA__", "-std=gnu17"),
                "/opt/esp-idf/components/esp_wifi/include/esp_wifi.h"
            )
        )
        val list = index.consumeAsFlow.toList()
        println("Parsed ${list.size} elements")
        println("${list.filter { it.file.endsWith("/esp_wifi.h") }.size} esp_wifi elements")
        service.close()
        exitProcess(0)
    }
//    testFile.writeText(
//        """
//        val x = 2
//        val y = 3
//        val z = x + y
//        """.trimIndent()
//    )
//    compiler.exec(
//        System.err,
//        "-kotlin-home",
//        "/usr/share/kotlin",
//        "-ir-output-dir",
//        "ir",
//        "-output-klib",
//        "-ir-output-name",
//        "test",
//        testFile.absolutePath,
//        "-libraries=otli-stdlib/build/otli-stdlib.klib"
//    )
//    compiler.exec(System.err, "-help")
}
