package com.huawei.ict.rag;

import com.huawei.ict.rag.infrastructure.code.CodeAssistantProperties;
import org.babyfish.jimmer.client.EnableImplicitApi;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableImplicitApi
@EnableConfigurationProperties(CodeAssistantProperties.class)
public class RagApplication {

	public static void main(String[] args) {
		SpringApplication.run(RagApplication.class, args);
	}

}
