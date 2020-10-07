package ir.piana.dev.pianareasty;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.hazelcast.spring.cache.HazelcastCacheManager;
import ir.piana.dev.pianareasty.sqlrest.ServiceProperties;
import ir.piana.dev.pianareasty.uploadrest.StorageProperties;
import ir.piana.dev.pianareasty.util.LowerCaseKeyDeserializer;
import ir.piana.dev.pianareasty.util.LowerCaseKeySerializer;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication(scanBasePackages = {
})
@EnableTransactionManagement
@EnableCaching
@EnableConfigurationProperties({
		ServiceProperties.class,
		StorageProperties.class
})
public class PianaReastyApplication {

	@Bean("jdbcObjectMapper")
	public ObjectMapper getJdbcObjectMapper() {
		ObjectMapper objectMapper = new ObjectMapper();
		SimpleModule module = new SimpleModule("LowerCaseKeyDeserializer",
				new Version(1,0,0,null));
		module.addKeyDeserializer(Object.class, new LowerCaseKeyDeserializer());
		module.addKeySerializer(Object.class, new LowerCaseKeySerializer());
		objectMapper.registerModule(module);
		return objectMapper;
	}

	@Bean("objectMapper")
	public ObjectMapper getObjectMapper() {
		ObjectMapper objectMapper = new ObjectMapper();
		return objectMapper;
	}

//	@Bean
//	public BCryptPasswordEncoder bCryptPasswordEncoder() {
//		BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
//		return bCryptPasswordEncoder;
//	}

	@Bean
	public CacheManager cacheManager() {
		return new HazelcastCacheManager();
	}

	@Bean
	CommandLineRunner init() {
		return (args) -> System.out.println("Start...");
	}

	public static void main(String[] args) {
		SpringApplication.run(PianaReastyApplication.class, args);
	}

}
