package com.github.dean535.permission.controller

import com.github.dean535.permission.controller.base.BaseBranchController
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/branch")
class BranchController : BaseBranchController()