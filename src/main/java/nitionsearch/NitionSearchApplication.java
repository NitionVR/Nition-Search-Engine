package nitionsearch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class NitionSearchApplication {
    private static final Logger logger = LoggerFactory.getLogger(NitionSearchApplication.class);

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(NitionSearchApplication.class, args);

        // Log all beans that were configured
        logger.info("Beans loaded by Spring Boot:");
        String[] beanNames = context.getBeanDefinitionNames();
        for (String beanName : beanNames) {
            logger.info("Bean: {}", beanName);
        }
    }
}