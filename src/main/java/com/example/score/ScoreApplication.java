package com.example.score;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 启动类。运行：mvn -DskipTests package，然后 java -jar target/score-web.war（war + 内嵌 Tomcat）。
 */
@SpringBootApplication
public class ScoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(ScoreApplication.class, args);
    }
}
