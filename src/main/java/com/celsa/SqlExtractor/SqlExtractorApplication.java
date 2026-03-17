package com.celsa.SqlExtractor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SqlExtractorApplication {

	public static void main(String[] args) {
		// 1. Modificamos las reglas de seguridad de Java para permitir TLS 1.0
		java.security.Security.setProperty("jdk.tls.disabledAlgorithms", "SSLv3, RC4, DES, MD5withRSA");

		// 2. Arrancamos la aplicación
		SpringApplication.run(SqlExtractorApplication.class, args);
	}
}
 


