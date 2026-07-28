package com.hmall.common.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 修复 Springfox (Swagger) 与 Spring Boot Actuator 的冲突。
 * 仅当 springfox WebMvcRequestHandlerProvider 存在时生效。
 */
@Configuration
@ConditionalOnClass(name = "springfox.documentation.spring.web.WebMvcRequestHandlerProvider")
public class SwaggerActuatorFixConfig {

    @Bean
    public BeanPostProcessor springfoxHandlerProviderBeanPostProcessor() {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
                // 只处理 WebMvcRequestHandlerProvider
                if (bean.getClass().getName().equals(
                        "springfox.documentation.spring.web.WebMvcRequestHandlerProvider")) {
                    Field field = ReflectionUtils.findField(bean.getClass(), "handlerMappings");
                    if (field != null) {
                        field.setAccessible(true);
                        Object mappingsObj = ReflectionUtils.getField(field, bean);
                        if (mappingsObj instanceof List) {
                            List<?> mappings = (List<?>) mappingsObj;
                            List<?> filtered = mappings.stream()
                                    .filter(m -> {
                                        try {
                                            // 保留 patternParser 为 null 的（没有 PathPatternParser 的旧式 mapping）
                                            java.lang.reflect.Method m2 = m.getClass().getMethod("getPatternParser");
                                            return m2.invoke(m) == null;
                                        } catch (Exception e) {
                                            return true;
                                        }
                                    })
                                    .collect(Collectors.toList());
                            ReflectionUtils.setField(field, bean, filtered);
                        }
                    }
                }
                return bean;
            }
        };
    }
}
