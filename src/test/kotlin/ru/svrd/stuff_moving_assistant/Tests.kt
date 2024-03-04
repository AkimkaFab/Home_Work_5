package ru.svrd.stuff_moving_assistant

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.getForEntity
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.shaded.org.checkerframework.common.value.qual.EnsuresMinLenIf
import ru.svrd.stuff_moving_assistant.domain.moving_box.CreateMovingBoxDto
import ru.svrd.stuff_moving_assistant.domain.moving_box.MovingBox
import ru.svrd.stuff_moving_assistant.domain.moving_box.MovingBoxExtras
import ru.svrd.stuff_moving_assistant.domain.moving_session.CreateMovingSessionDto
import ru.svrd.stuff_moving_assistant.domain.moving_session.CreateMovingSessionResponse

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class Tests {

    @LocalServerPort
    var port: Int? = null

    @Autowired
    lateinit var testRestTemplate: TestRestTemplate

    companion object {

        @JvmStatic
        @Container
        private val postgresDb = PostgreSQLContainer("postgres:15-alpine")
            .withDatabaseName("stuff-moving-assistant")
        //    .apply { start() } // Пока не точно

        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.master.hikari.jdbc-url", postgresDb::getJdbcUrl)
            registry.add("spring.datasource.master.hikari.username", postgresDb::getUsername)
            registry.add("spring.datasource.master.hikari.password", postgresDb::getPassword)
        }
    }

    @Test
    fun createBoxTest() {
        val sessionBody = CreateMovingSessionDto("Create box test")
        val apiSession = testRestTemplate.postForEntity("http://localhost:$port/api/v1/moving/session",
            sessionBody, CreateMovingSessionResponse::class.java
        )

        val movingBoxBody = CreateMovingBoxDto("Test box")
        val movingBox = testRestTemplate.postForEntity("http://localhost:$port/api/v1/moving/session/${apiSession.body!!.id}/newBox",
            movingBoxBody, MovingBox::class.java
        )

        assertEquals(200, movingBox.statusCode.value())
        assertNotNull(movingBox.body!!.id)
        assertEquals("Test box", movingBox.body!!.title)
    }

    @Test
    fun createBoxAndGetItTest() {
        val sessionBody = CreateMovingSessionDto("Create box test")
        val apiSession = testRestTemplate.postForEntity(
            "http://localhost:$port/api/v1/moving/session",
            sessionBody, CreateMovingSessionResponse::class.java
        )

        val movingBoxBody1 = CreateMovingBoxDto("Test box 1")
        val movingBox1 = testRestTemplate.postForEntity(
            "http://localhost:$port/api/v1/moving/session/${apiSession.body!!.id}/newBox",
            movingBoxBody1, MovingBox::class.java
        )

//        val movingBoxBody2 = CreateMovingBoxDto("Test box 2")
//        val movingBox2 = testRestTemplate.postForEntity(
//            "http://localhost:$port/api/v1/moving/session/${apiSession.body!!.id}/newBox",
//            movingBoxBody2, MovingBox::class.java
//        )

        val getBox = testRestTemplate.getForEntity(
            "http://localhost:$port/api/v1/moving/session/${apiSession.body!!.id}/boxes",
            listOf(MovingBox::class.java)::class.java
        )

        assertEquals(200, getBox.statusCode.value())
        assertNotNull(getBox.body)
        //assertEquals(13,getBox.body)
        if (!(getBox.body.toString().contains("{id=2, sessionId=2, title=Test box 1"))) {
            assert(false)
        }
    }

    @Test
    fun createBoxAndAddItemsTest() {
        val sessionBody = CreateMovingSessionDto("Create box test")
        val apiSession = testRestTemplate.postForEntity(
            "http://localhost:$port/api/v1/moving/session",
            sessionBody, CreateMovingSessionResponse::class.java
        )

        val movingBoxBody = CreateMovingBoxDto("Test box")
        val movingBox = testRestTemplate.postForEntity(
            "http://localhost:$port/api/v1/moving/session/${apiSession.body!!.id}/newBox",
            movingBoxBody, MovingBox::class.java
        )

        val itemBoxBody = MovingBoxExtras(listOf("Item 1", "Item 2", "Item 3"))
        val boxWithItems = testRestTemplate.postForEntity(
            "http://localhost:$port/api/v1/moving/session/${apiSession.body!!.id}/box/${movingBox.body!!.id}/editItems",
            itemBoxBody, MovingBox::class.java
        )

        val getBox = testRestTemplate.getForEntity(
            "http://localhost:$port/api/v1/moving/session/${apiSession.body!!.id}/boxes",
            listOf(MovingBox::class.java)::class.java
        )

        assertEquals(200, getBox.statusCode.value())
        assertNotNull(getBox)
        if (!(getBox.toString().contains("extras={items=[Item 1, Item 2, Item 3]}}"))) {
            assert(false)
        }

    }

}