package com.github.dean535.permission.controller

import com.github.dean535.permission.config.WebConfig
import com.github.dean535.permission.entity.Branch
import com.github.dean535.permission.entity.Role
import com.github.dean535.permission.entity.User
import com.github.dean535.permission.service.BranchService
import com.github.dean535.error.GlobalExceptionHandler
import com.github.dean535.json.JsonReturnHandler
import com.github.dean535.permission.config.TestSecurityConfig
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.data.domain.PageImpl
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest
@AutoConfigureMockMvc
@ContextConfiguration(classes = [
    TestSecurityConfig::class,
    BranchController::class,
    WebConfig::class,
    JsonReturnHandler::class,
    GlobalExceptionHandler::class])
class BranchControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockkBean
    lateinit var branchService: BranchService

    @BeforeEach
    fun setup() {
        // given
        val role1 = Role(name = "admin").apply { this.id = 1 }
        val role2 = Role(name = "manager").apply { this.id = 2 }
        val user1 = User(login = "login1", address = "address1", email = "email1", notes = "notes1", active = true, role = role1).apply { this.id = 1 }
        val user2 = User(login = "login2", address = "address2", email = "email2", notes = "notes2", active = false, role = role2).apply { this.id = 2 }
        val branchA = Branch(name = "branchA", active = true, users = mutableListOf(user1)).apply { this.id = 1 }
        val branchB = Branch(name = "branchB", active = false, users = mutableListOf(user2)).apply { this.id = 2 }
        user1.branch = branchA
        user2.branch = branchB
        val mockedBranches = PageImpl(listOf(branchA, branchB))
        every { branchService.searchBySecurity(any(), any(), any(), any()) } returns mockedBranches
    }

    @Test
    fun `will not return embedded fields by default`() {
        // when
        val resultActions = mockMvc.perform(MockMvcRequestBuilders.get("/v1/branch"))
        // then
        resultActions
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.content[0].id", `is`(1)))
                .andExpect(jsonPath("$.content[1].id", `is`(2)))
                .andExpect(jsonPath("$.content[*].users").doesNotExist())
    }

    @Test
    fun `will return embedded`() {
        // when
        val resultActions = mockMvc.perform(MockMvcRequestBuilders.get("/v1/branch?embedded=users"))
        // then
        resultActions
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.content[0].id", `is`(1)))
                .andExpect(jsonPath("$.content[1].id", `is`(2)))
                .andExpect(jsonPath("$.content[*].users").exists())
    }

    @Test
    fun `will return 2 embedded`() {
        // when
        val resultActions = mockMvc.perform(MockMvcRequestBuilders.get("/v1/branch?embedded=users,users.role"))
        // then
        resultActions
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.content[0].id", `is`(1)))
                .andExpect(jsonPath("$.content[1].id", `is`(2)))
                .andExpect(jsonPath("$.content[*].users").exists())
                .andExpect(jsonPath("$.content[*].users[*].role").exists())
    }

    @Test
    fun `will return 3 embedded`() {
        // when
        val resultActions = mockMvc.perform(MockMvcRequestBuilders.get("/v1/branch?embedded=users,users.role,users.role.rolePermissions"))
        // then
        println(resultActions.andReturn().response.contentAsString)
        resultActions
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.content[0].id", `is`(1)))
                .andExpect(jsonPath("$.content[1].id", `is`(2)))
                .andExpect(jsonPath("$.content[*].users").exists())
                .andExpect(jsonPath("$.content[*].users[*].role").exists())
    }
}