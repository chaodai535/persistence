package com.github.dean535.security.config

import com.github.dean535.security.LogoutSuccess
import com.github.dean535.security.MyAccessDecisionManager
import com.github.dean535.security.MyFilterSecurityInterceptor
import com.github.dean535.security.TokenAuthenticationFilter
import com.github.dean535.security.custom.CustomAuthenticationFilter
import com.github.dean535.security.custom.CustomUserDetailsAuthenticationProvider
import com.github.dean535.security.custom.CustomUserDetailsService
import com.github.dean535.security.handlers.AuthenticationFailureHandler
import com.github.dean535.security.handlers.AuthenticationSuccessHandler
import com.github.dean535.security.handlers.MyAccessDeniedHandler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.builders.WebSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.filter.CorsFilter
import java.util.*

@Configuration
@EnableWebSecurity
class SecurityConfig : WebSecurityConfigurerAdapter() {

    @Autowired
    private val myFilterSecurityInterceptor: MyFilterSecurityInterceptor? = null

    @Autowired
    private val myAccessDeniedHandler: MyAccessDeniedHandler? = null

    @Autowired
    private val myAccessDecisionManager: MyAccessDecisionManager? = null

    @Autowired
    private val logoutSuccess: LogoutSuccess? = null

    @Autowired
    private val authenticationSuccessHandler: AuthenticationSuccessHandler? = null

    @Autowired
    private val authenticationFailureHandler: AuthenticationFailureHandler? = null

    @Autowired
    lateinit var tokenAuthenticationFilter: TokenAuthenticationFilter

    @Autowired
    lateinit var userDetailsService: CustomUserDetailsService


//    @Bean
//    fun corsFilter(): CorsFilter {
//        val source = UrlBasedCorsConfigurationSource()
//        val config = CorsConfiguration()
//        //config.allowCredentials = true
//        config.addAllowedOrigin("*")
//        config.addAllowedHeader("*")
//        config.addAllowedMethod("*")
//        source.registerCorsConfiguration("/**", config)
//        return CorsFilter(source)
//    }


    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    fun authProvider(): AuthenticationProvider {
        return CustomUserDetailsAuthenticationProvider(passwordEncoder(), userDetailsService)
    }


    fun authenticationFilter(): CustomAuthenticationFilter {
        val authenticationFilter = CustomAuthenticationFilter()
        authenticationFilter.setAuthenticationManager(authenticationManagerBean())
        authenticationFilter.setAuthenticationSuccessHandler(authenticationSuccessHandler)
        authenticationFilter.setAuthenticationFailureHandler(authenticationFailureHandler)
        authenticationFilter.setRequiresAuthenticationRequestMatcher(AntPathRequestMatcher("/v1/login", "POST"))
        authenticationFilter.setAuthenticationManager(authenticationManagerBean())
        return authenticationFilter
    }

    @Autowired
    fun configureGlobal(auth: AuthenticationManagerBuilder) {
        auth.authenticationProvider(authProvider())
    }

    override fun configure(http: HttpSecurity) {
        http.cors()
        http.exceptionHandling().accessDeniedHandler(myAccessDeniedHandler)
        http.csrf().disable()
        http.headers().cacheControl().disable()
        http.headers().frameOptions().sameOrigin()
        http.authorizeRequests()
            .anyRequest()
            .authenticated()
            .accessDecisionManager(myAccessDecisionManager)

        http.addFilterBefore(authenticationFilter(), UsernamePasswordAuthenticationFilter::class.java)
            .addFilterBefore(tokenAuthenticationFilter, BasicAuthenticationFilter::class.java)
//            .addFilterBefore(myFilterSecurityInterceptor!!, FilterSecurityInterceptor::class.java)

    }
}
