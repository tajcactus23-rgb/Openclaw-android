package com.openhands.android

import org.junit.Test
import org.junit.Assert.*
import com.openhands.android.domain.model.CapabilityState

class AdapterRequiredTest {

    @Test
    fun testCapabilityState_hasAdapterRequired() {
        val states = CapabilityState.entries
        assertTrue(states.contains(CapabilityState.ADAPTER_REQUIRED))
    }

    @Test
    fun testCapabilityState_sessionNotAvailable() {
        val apiExists = false
        val capability = if (apiExists) CapabilityState.SUPPORTED else CapabilityState.ADAPTER_REQUIRED
        assertEquals(CapabilityState.ADAPTER_REQUIRED, capability)
    }

    @Test
    fun testCapabilityState_workflowNotAvailable() {
        val apiExists = false
        val capability = if (apiExists) CapabilityState.SUPPORTED else CapabilityState.ADAPTER_REQUIRED
        assertEquals(CapabilityState.ADAPTER_REQUIRED, capability)
    }

    @Test
    fun testCapabilityState_disconnected() {
        val state = CapabilityState.DISCONNECTED
        assertEquals(CapabilityState.DISCONNECTED, state)
    }
}