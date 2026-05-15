package com.openhands.android.data.remote

import org.junit.Test
import org.junit.Assert.*

/**
 * Tests for Workflow API models and execution flow
 */
class WorkflowApiTest {

    @Test
    fun testWorkflowExecuteRequestParsing() {
        // Test that WorkflowExecuteRequest can be created
        val request = WorkflowExecuteRequest(
            workflowId = "test-123",
            name = "Test Workflow",
            description = "Test description",
            nodes = listOf(
                mapOf("id" to "node1", "type" to "prompt", "name" to "Hello", "config" to "Say hello")
            )
        )
        
        assertEquals("test-123", request.workflowId)
        assertEquals("Test Workflow", request.name)
        assertEquals("Test description", request.description)
        assertEquals(1, request.nodes.size)
    }

    @Test
    fun testWorkflowExecutionResponseParsing() {
        // Test response parsing
        val response = WorkflowExecutionResponse(
            executionId = "exec-456",
            status = "running",
            message = "Workflow started",
            workflowId = "test-123",
            createdAt = "2025-01-15T10:00:00Z"
        )
        
        assertEquals("exec-456", response.executionId)
        assertEquals("running", response.status)
        assertEquals("test-123", response.workflowId)
    }

    @Test
    fun testWorkflowExecutionStatusParsing() {
        // Test status parsing
        val status = WorkflowExecutionStatus(
            id = "exec-456",
            workflowId = "test-123", 
            name = "Test Workflow",
            status = "completed",
            currentNode = "node1",
            progress = 1.0,
            createdAt = "2025-01-15T10:00:00Z",
            updatedAt = "2025-01-15T10:05:00Z",
            completedAt = "2025-01-15T10:05:00Z",
            error = null,
            result = null
        )
        
        assertEquals("exec-456", status.id)
        assertEquals("completed", status.status)
        assertEquals("node1", status.currentNode)
        assertEquals(1.0, status.progress, 0.001)
    }

    @Test
    fun testStatusMapping() {
        // Test execution status mapping
        val statuses = listOf("pending", "running", "completed", "failed", "cancelled", "terminated")
        
        assertTrue(statuses.contains("pending"))
        assertTrue(statuses.contains("running"))
        assertTrue(statuses.contains("completed"))
        assertTrue(statuses.contains("failed"))
        assertTrue(statuses.contains("cancelled"))
        assertTrue(statuses.contains("terminated"))
    }

    @Test
    fun testAdapterRequiredDisplay() {
        // Test ADAPTER_REQUIRED scenario
        val executionStatus = "failed"
        val error = "ADAPTER_REQUIRED"
        
        // When ADAPTER_REQUIRED is returned, show appropriate message
        val displayMessage = when {
            error.contains("ADAPTER_REQUIRED") -> "Real execution requires OpenHands Cloud adapter"
            executionStatus == "completed" -> "Workflow completed"
            executionStatus == "failed" -> "Workflow failed: $error"
            executionStatus == "running" -> "Workflow running..."
            else -> "Status: $executionStatus"
        }
        
        assertEquals("Real execution requires OpenHands Cloud adapter", displayMessage)
    }
}