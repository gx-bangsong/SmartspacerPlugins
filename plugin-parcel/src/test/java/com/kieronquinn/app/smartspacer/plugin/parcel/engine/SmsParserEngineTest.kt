package com.kieronquinn.app.smartspacer.plugin.parcel.engine

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import kotlinx.coroutines.runBlocking
import com.kieronquinn.app.smartspacer.plugin.parcel.ParcelPlugin
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import com.kieronquinn.app.smartspacer.plugin.parcel.data.RuleDao
import com.kieronquinn.app.smartspacer.plugin.parcel.data.RuleItem
import kotlinx.coroutines.flow.flowOf
import org.mockito.Mockito.*

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class SmsParserEngineTest {

    private lateinit var engine: SmsParserEngine
    private val ruleDao = mock(RuleDao::class.java)

    @Before
    fun setUp() {
        stopKoin()
        startKoin {
            androidContext(RuntimeEnvironment.getApplication())
            modules(module {
                single { ruleDao }
            })
        }
        engine = SmsParserEngine(RuntimeEnvironment.getApplication())
    }

    @Test
    fun testCainiaoExtraction() = runBlocking {
        val rules = listOf(
            RuleItem("菜鸟驿站", 10, "菜鸟驿站,取件码", "(?:取件码|取件码为)\\s*[:：]?\\s*([A-Z0-9-]+)", "地址[:：]\\s*([^，。！\\s]+)", false)
        )
        `when`(ruleDao.getAllRules()).thenReturn(flowOf(rules))

        val sms = "【菜鸟驿站】您的中通快递取件码为 9-2-1004，请于21:00前到中海御景南门菜鸟驿站取件.地址：御景南门。"
        val result = engine.parse(sms)
        assertNotNull("Result should not be null", result)
        assertEquals("9-2-1004", result?.pickupCode)
        assertEquals("御景南门", result?.location)
    }

    @Test
    fun testFengchaoExtraction() = runBlocking {
        val rules = listOf(
            RuleItem("丰巢", 10, "丰巢,取件码", "(?:取件码|取件码为)\\s*[:：]?\\s*([0-9]{6,8})", "位于\\s*([^，。！\\s]+)", false)
        )
        `when`(ruleDao.getAllRules()).thenReturn(flowOf(rules))

        val sms = "【丰巢】您的取件码为 888888，请前往位于 香格里拉花园 的丰巢快递柜取件。"
        val result = engine.parse(sms)
        assertNotNull("Result should not be null", result)
        assertEquals("888888", result?.pickupCode)
        assertEquals("香格里拉花园", result?.location)
    }

    @Test
    fun testGenericExtraction() = runBlocking {
        val rules = listOf(
            RuleItem("通用快递", 1, "取件码", "(?:取件码|验证码|取货码)\\s*[:：]?\\s*([A-Z0-9-]+)", null, false)
        )
        `when`(ruleDao.getAllRules()).thenReturn(flowOf(rules))

        val sms = "您的取件码: 123456，请及时领取您的包裹。"
        val result = engine.parse(sms)
        assertNotNull("Result should not be null", result)
        assertEquals("123456", result?.pickupCode)
    }

    @Test
    fun testNonParcelSms() = runBlocking {
        val rules = listOf(
            RuleItem("通用快递", 1, "取件码", "(?:取件码|验证码|取货码)\\s*[:：]?\\s*([A-Z0-9-]+)", null, false)
        )
        `when`(ruleDao.getAllRules()).thenReturn(flowOf(rules))

        val sms = "验证码 123456，用于登录您的账户。"
        val result = engine.parse(sms)
        assertNull("Result should be null for non-parcel SMS", result)
    }
}
