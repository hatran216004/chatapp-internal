package com.example.librarymanagement;

import com.example.librarymanagement.service.inter.AwsService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class LibrarymanagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(LibrarymanagementApplication.class, args);
    }

    @Bean
    CommandLineRunner runnder(AwsService awsService) {
        return args -> {
//            awsService.putObject(
//                    "chatapp-internal-test",
//                    "images/users/foo",
//                    "Hello World".getBytes()
//            );
//
//            byte[] obj = awsService.getObject(
//                    "chatapp-internal-test",
//                    "images/users/foo"
//            );
//
//            System.out.println("cc: " + new String(obj));
        };
    }
}
