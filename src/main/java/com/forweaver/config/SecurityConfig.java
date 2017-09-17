package com.forweaver.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.core.userdetails.UserDetailsService;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	@Qualifier("userDetailsService")
	UserDetailsService userDetailsService;

	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth)
			throws Exception {
		auth.userDetailsService(userDetailsService).passwordEncoder(
				passwordEncoder());
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.csrf().disable();
		
		http.httpBasic().and().authorizeRequests().antMatchers("/g/**","/s/**")
		.authenticated();
		
		http.authorizeRequests()
		.antMatchers("/resources/**", "/", "/*")
		.permitAll();
		
		http.authorizeRequests().antMatchers("/community/add",
				"/community/*/delete","/community/*/*/delete","/community/*/*/*/delete",
				"/community/*/update","/community/*/push","/community/*/*/update",
				"/community/*/add-repost","/community/*/*/add-reply","/community/*/edit").authenticated()
		.and().authorizeRequests().antMatchers("/community/**").permitAll();
		
		http.authorizeRequests().antMatchers("/code/add","/code/*/delete","/code/*/update",
				"/code/*/add-repost","/code/*/edit").authenticated()
		.and().authorizeRequests().antMatchers("/code/**").permitAll();
		
		http.authorizeRequests().antMatchers("/repository/*/*/delete","/repository/*/*/*/update",
				"/repository/*/*/weaver/*/add-weaver","/repository/*/*/weaver/*/join-ok",
				"/repository/*/*/weaver/*/join-cancel","/repository/*/*/weaver/*/delete",
				"/repository/*/*/join","/repository/*/*/community/add").authenticated()
		.and().authorizeRequests().antMatchers("/repository/**").permitAll();
		
		http.authorizeRequests().antMatchers("/code/add","/code/*/delete","/code/*/update",
				"/code/*/add-repost","/code/*/edit").authenticated()
		.and().authorizeRequests().antMatchers("/code/**").permitAll();
		
		http.authorizeRequests().antMatchers("/join","/check","/repassword**","/login").anonymous();
		
		http.authorizeRequests().antMatchers("/del","/edit").authenticated();
		
		http.formLogin().defaultSuccessUrl("/").loginPage("/login")
		.permitAll().and().logout().logoutUrl("/logout").permitAll();


	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		PasswordEncoder encoder = new ShaPasswordEncoder();
		return encoder;
	}

	@Bean
	@Override
	public AuthenticationManager authenticationManagerBean() throws Exception
	{
		return super.authenticationManagerBean();
	}

	@Bean(name="sessionRegistry")
	public SessionRegistry sessionRegistry() {
		return new SessionRegistryImpl();
	}

}