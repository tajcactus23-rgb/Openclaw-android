package com.openhands.android

import org.junit.Test
import org.junit.Assert.*
import com.openhands.android.presentation.ui.screens.NodeType

class AgentCanvasTest {

    @Test
    fun testNodeType_count() {
        // There should be 6 node types
        assertEquals(6, NodeType.entries.size)
    }

    @Test
    fun testNodeType_allHaveLabels() {
        NodeType.entries.forEach { type ->
            assertTrue(type.label.isNotBlank())
        }
    }

    @Test
    fun testNodeType_allHaveColors() {
        NodeType.entries.forEach { type ->
            assertTrue(type.color != 0L)
        }
    }

    @Test
    fun testNodeType_containsExpectedTypes() {
        val labels = NodeType.entries.map { it.label }
        
        assertTrue(labels.contains("Prompt"))
        assertTrue(labels.contains("File"))
        assertTrue(labels.contains("Skill"))
        assertTrue(labels.contains("Tool"))
        assertTrue(labels.contains("Model"))
        assertTrue(labels.contains("Output"))
    }
}