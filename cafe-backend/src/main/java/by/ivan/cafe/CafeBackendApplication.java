package by.ivan.cafe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Точка входа в наше приложение.
 *
 * @SpringBootApplication — это «волшебная» аннотация, которая делает сразу три вещи:
 *   1) @Configuration        — говорит Spring: «здесь можно объявлять бины (наши объекты)»;
 *   2) @EnableAutoConfiguration — Spring сам настраивает всё, что нашёл в зависимостях
 *      (веб-сервер, базу данных, безопасность) — нам не нужно писать тонны конфигурации;
 *   3) @ComponentScan        — Spring обходит ВСЕ классы в пакете by.ivan.cafe и его
 *      подпакетах, находит аннотированные классы (@RestController, @Service, @Repository)
 *      и регистрирует их как «бины» — готовые объекты, которыми управляет Spring.
 *
 * Метод main — стандартная точка входа Java-программы. SpringApplication.run
 * поднимает встроенный веб-сервер (Tomcat) и запускает всё приложение.
 */
@SpringBootApplication
public class CafeBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(CafeBackendApplication.class, args);
    }
}