package com.lhf.feign.hystrix;

import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created on 2017/12/22.
 */
public class FeignHystrixProxyRegistrar implements ImportBeanDefinitionRegistrar
        , ResourceLoaderAware, BeanClassLoaderAware {

    private ResourceLoader resourceLoader;

    private ClassLoader classLoader;

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        ClassPathScanningCandidateComponentProvider scanner = getScanner();
        scanner.addIncludeFilter(new AnnotationTypeFilter(FeignHystrixProxy.class));
        Set<String> basePackages = getBasePackages(metadata);

        for(String basePackage : basePackages) {
            Set<BeanDefinition> candidateComponents = scanner.findCandidateComponents(basePackage);
            for (BeanDefinition candidateComponent : candidateComponents) {
                if (candidateComponent instanceof AnnotatedBeanDefinition) {
                    AnnotatedBeanDefinition beanDefinition = (AnnotatedBeanDefinition) candidateComponent;
                    AnnotationMetadata annotationMetadata = beanDefinition.getMetadata();
                    Assert.isTrue(
                        annotationMetadata.isAbstract(),
                        "@FeignHystrixProxy can only be specified on an abstract class"
                    );

                    Map<String, Object> attributes = annotationMetadata
                            .getAnnotationAttributes(FeignHystrixProxy.class.getCanonicalName());

                    registerFeignHystrixProxy(registry, beanDefinition.getBeanClassName(), attributes);
                }
            }
        }
    }

    private void registerFeignHystrixProxy(BeanDefinitionRegistry registry
            , String beanClassName, Map<String, Object> attributes) {
        BeanDefinitionBuilder definitionBuilder =
                BeanDefinitionBuilder.genericBeanDefinition(FeignHystrixProxyFactoryBean.class);
        definitionBuilder.addPropertyValue("type", beanClassName);
        definitionBuilder.addPropertyValue("fallbackTemplate", attributes.get("template"));

        registry.registerBeanDefinition(beanClassName, definitionBuilder.getBeanDefinition());
    }

    protected ClassPathScanningCandidateComponentProvider getScanner() {
        ClassPathScanningCandidateComponentProvider provider =
            new ClassPathScanningCandidateComponentProvider(false) {

                @Override
                protected boolean isCandidateComponent(
                        AnnotatedBeanDefinition beanDefinition) {
                    if (beanDefinition.getMetadata().isIndependent()) {
                        if (beanDefinition.getMetadata().isInterface()
                                && beanDefinition.getMetadata().getInterfaceNames().length == 1
                                && Annotation.class.getName().equals(beanDefinition.getMetadata().getInterfaceNames()[0]))
                        {
                            try {
                                Class<?> target = ClassUtils.forName(
                                    beanDefinition.getMetadata().getClassName(),
                                    FeignHystrixProxyRegistrar.this.classLoader);
                                return !target.isAnnotation();
                            }
                            catch (Exception ex) {
                                this.logger.error(
                                    "Could not load target class: "
                                        + beanDefinition.getMetadata().getClassName()
                                    , ex);
                            }
                        }
                        return true;
                    }
                    return false;
                }
            };

        provider.setResourceLoader(FeignHystrixProxyRegistrar.this.resourceLoader);

        return provider;
    }

    protected Set<String> getBasePackages(AnnotationMetadata importingClassMetadata) {
        Map<String, Object> attributes = importingClassMetadata
                .getAnnotationAttributes(EnableFeignHystrixProxy.class.getCanonicalName());

        Set<String> basePackages = new HashSet<>();

        for (String pkg : (String[]) attributes.get("value")) {
            if (StringUtils.hasText(pkg)) {
                basePackages.add(pkg);
            }
        }

        return basePackages;
    }
}
