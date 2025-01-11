package com.ani.taku_backend;

import org.junit.jupiter.api.Test; // JUnit5
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.junit.jupiter.api.extension.ExtendWith;

@SpringBootTest(classes = TakuProjectApplication.class)
@ExtendWith(SpringExtension.class) // JUnit5
public class ContainerNameTest {

    @Autowired
    ApplicationContext context;

    @Test
    public void contextCheck() throws Exception {

        if (context != null) {
            String[] beans = context.getBeanDefinitionNames();
            for (String bean : beans) {
                System.out.println("bean: " + bean);
            }
            // 추가: cacheManager의 실제 클래스 이름 확인
            if (context.containsBean("cacheManager")) {
                Object cacheManagerBean = context.getBean("cacheManager");
                System.out.println(">>> cacheManager class = " + cacheManagerBean.getClass().getName());
            } else {
                System.out.println(">>> No cacheManager bean found!");
            }
        }
    }
}