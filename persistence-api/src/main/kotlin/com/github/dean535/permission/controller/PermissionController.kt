package com.github.dean535.permission.controller

import com.github.dean535.cache.CacheClient
import com.github.dean535.permission.controller.base.BasePermissionController
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/permission")
class PermissionController(
    val cacheClient: CacheClient
) : BasePermissionController() 