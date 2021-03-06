package com.github.dean535.security.handlers

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.dean535.error.ErrorDTO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.stereotype.Component
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class MyAccessDeniedHandler(
    @Autowired
    val objectMapper: ObjectMapper
) : AccessDeniedHandler {

    override fun handle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        accessDeniedException: AccessDeniedException
    ) {
        val msg = objectMapper.writeValueAsString(
            ErrorDTO(message = "No Permission to access: ${request.method} ${request.requestURI}")
        )

        response.status = HttpStatus.FORBIDDEN.value()
        response.writer.write(msg)
    }
}
