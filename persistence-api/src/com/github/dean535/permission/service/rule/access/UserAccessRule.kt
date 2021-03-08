package com.github.dean535.persistence.service.rule.access

import com.github.dean535.persistence.entity.Permission
import com.github.dean535.persistence.entity.User
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

@Component
class UserAccessRule : AccessRule {
    override val ruleName: String
        get() = "belongTo"

    override fun exec(permission: Permission): Map<String, String> {
        val user = SecurityContextHolder.getContext().authentication.principal as User
        return mapOf("belongTo.id_eq" to user.id.toString())
    }
}