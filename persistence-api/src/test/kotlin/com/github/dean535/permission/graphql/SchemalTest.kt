package com.github.dean535.permission.graphql


import com.github.dean535.permission.config.JpaConfig
import com.github.dean535.permission.entity.Branch
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import javax.persistence.EntityManager

@DataJpaTest(
        includeFilters = [ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = [GraphQLSchemaBuilder::class, JpaConfig::class]
        )]
)
class SchemalTest {

    @Autowired
    lateinit var schemaBuilder: GraphQLSchemaBuilder

    @Autowired
    lateinit var entityManager: EntityManager

    //@Test
    fun `schemaBuilder field definition`() {
        schemaBuilder.getQueryType()

        val entityType = entityManager.metamodel.entities.first { it.javaType == Branch::class.java }
        val fieldDefinition = schemaBuilder.getQueryFieldDefinition(entityType)
        println(fieldDefinition.arguments)
    }

}