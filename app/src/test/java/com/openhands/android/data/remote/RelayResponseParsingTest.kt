package com.openhands.android.data.remote

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.*
import org.junit.Test

class RelayResponseParsingTest {

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    @Test
    fun testSessionResponseParsing() {
        val json = """[{"id": "s1", "name": "Test Session", "status": "running", "model": "gpt-4", "started_at": "2024-01-01T00:00:00Z"}]"""
        val type = Types.newParameterizedType(List::class.java, SessionResponse::class.java)
        val adapter = moshi.adapter<List<SessionResponse>>(type)
        val sessions = adapter.fromJson(json)
        assertNotNull(sessions)
        assertEquals(1, sessions!!.size)
        assertEquals("s1", sessions[0].id)
    }

    @Test
    fun testEmptySessionsList() {
        val json = "[]"
        val type = Types.newParameterizedType(List::class.java, SessionResponse::class.java)
        val adapter = moshi.adapter<List<SessionResponse>>(type)
        val sessions = adapter.fromJson(json)
        assertNotNull(sessions)
        assertTrue(sessions!!.isEmpty())
    }

    @Test
    fun testSkillResponseParsing() {
        val json = """[{"id": "skill1", "name": "Test Skill", "description": "A test", "trigger": "test", "content": "content", "source": "cloud"}]"""
        val type = Types.newParameterizedType(List::class.java, SkillResponse::class.java)
        val adapter = moshi.adapter<List<SkillResponse>>(type)
        val skills = adapter.fromJson(json)
        assertNotNull(skills)
        assertEquals(1, skills!!.size)
        assertEquals("cloud", skills[0].source)
    }

    @Test
    fun testMCPToolResponseParsing() {
        val json = """[{"id": "tool1", "name": "Bash", "description": "Commands", "server": "local", "enabled": true}]"""
        val type = Types.newParameterizedType(List::class.java, MCPToolResponse::class.java)
        val adapter = moshi.adapter<List<MCPToolResponse>>(type)
        val tools = adapter.fromJson(json)
        assertNotNull(tools)
        assertTrue(tools!![0].enabled)
    }

    @Test
    fun testCapabilityParsing() {
        val json = """[{"name": "sessions", "status": "available", "description": "List sessions", "backend": "openhands"}]"""
        val type = Types.newParameterizedType(List::class.java, CapabilityResponse::class.java)
        val adapter = moshi.adapter<List<CapabilityResponse>>(type)
        val caps = adapter.fromJson(json)
        assertNotNull(caps)
        assertEquals("available", caps!![0].status)
    }

    @Test
    fun testNoFakeSessions() {
        val json = "[]"
        val type = Types.newParameterizedType(List::class.java, SessionResponse::class.java)
        val adapter = moshi.adapter<List<SessionResponse>>(type)
        val sessions = adapter.fromJson(json)
        assertTrue(sessions!!.isEmpty())
    }

    @Test
    fun testNoFakeSkills() {
        val json = "[]"
        val type = Types.newParameterizedType(List::class.java, SkillResponse::class.java)
        val adapter = moshi.adapter<List<SkillResponse>>(type)
        val skills = adapter.fromJson(json)
        assertTrue(skills!!.isEmpty())
    }

    @Test
    fun testNoFakeTools() {
        val json = "[]"
        val type = Types.newParameterizedType(List::class.java, MCPToolResponse::class.java)
        val adapter = moshi.adapter<List<MCPToolResponse>>(type)
        val tools = adapter.fromJson(json)
        assertTrue(tools!!.isEmpty())
    }
}