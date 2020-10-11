package com.imoxion.board.beans;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.view.BeanNameViewResolver;
import org.springframework.web.servlet.view.json.MappingJacksonJsonView;

@Configuration
public class Config {
	@Bean
	public ViewResolver beanNameViewResolver() {
		BeanNameViewResolver resolver = new BeanNameViewResolver();
		return resolver;
	}

	@Bean
	public MappingJacksonJsonView jsonView() {
		return new MappingJacksonJsonView();
	}


}
