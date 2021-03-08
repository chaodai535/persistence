package com.github.dean535.persistence.entity

import com.github.b1412.api.entity.BaseEntity
import java.io.Serializable
import javax.persistence.Entity

@Entity
data class Rule(
    var name: String?
) : BaseEntity(), Serializable
