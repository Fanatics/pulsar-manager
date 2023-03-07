/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.pulsar.manager.fanatics.security;

import org.apache.pulsar.manager.fanatics.saml.SamlUserDetailsService;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.extensions.saml2.config.SAMLConfigurer;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@EnableWebSecurity
@Order(10)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.csrf()
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse());

        // do not create session
        http.sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.NEVER)
                .invalidSessionUrl("/fpb-manager/okta");

        // disable cors
        http.authorizeRequests()
                .antMatchers("/ui/index.html").permitAll()
                .antMatchers("/static/**").permitAll()
                .antMatchers("/pulsar-manager/users/superuser").permitAll()
                .antMatchers("/pulsar-manager/csrf-token").permitAll()
                .antMatchers("/pulsar-manager/login").permitAll()
                .antMatchers("/saml/**").permitAll()
                .antMatchers("/favicon**").permitAll()
                .anyRequest().authenticated()
                .and()
                .apply(SAMLConfigurer.saml())
                .userDetailsService(new SamlUserDetailsService())
                    .serviceProvider()
                    .keyStore()
                    .storeFilePath("file:keystore.jks")
                    .password("secret")
                    .keyname("fpb-manager")
                    .keyPassword("secret")
                    .and()
                .protocol("https")
                .hostname(System.getenv("APP_DOMAIN"))
                .basePath("/")
                .and()
                    .identityProvider()
                    .metadataFilePath("file:okta_metadata.xml")
                    .and();
    }
}
