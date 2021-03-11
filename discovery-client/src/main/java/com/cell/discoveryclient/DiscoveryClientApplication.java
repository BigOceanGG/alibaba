package com.cell.discoveryclient;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


@SpringBootApplication
public class DiscoveryClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(DiscoveryClientApplication.class, args);
    }

    @Slf4j
    @RestController
    static class TestController {

        @Autowired
        LoadBalancerClient loadBalancerClient;

        @Autowired
        RestTemplate restTemplate;

        @Autowired
        private WebClient.Builder webClientBuilder;

        @GetMapping("/test")
        public String test() {
            // 通过spring cloud common中的负载均衡接口选取服务提供节点实现接口调用
            ServiceInstance serviceInstance = loadBalancerClient.choose("discovery-server");
            String url = serviceInstance.getUri() + "/hello?name=" + "didi";
            RestTemplate restTemplate = new RestTemplate();
            String result = restTemplate.getForObject(url, String.class);
            return "Invoke : " + url + ", return : " + result;
        }

        @GetMapping("/testrest")
        public String testrest() {
            String result = restTemplate.getForObject("http://discovery-server/hello?name=rest", String.class);
            return "Return : " + result;
        }

        @GetMapping("/testweb")
        public Mono<String> testweb() {
            Mono<String> result = webClientBuilder.build()
                    .get()
                    .uri("http://discovery-server/hello?name=web")
                    .retrieve()
                    .bodyToMono(String.class);
            return result;
        }

        @Bean
        @LoadBalanced
        public RestTemplate restTemplate() {
            return new RestTemplate();
        }

        @Bean
        @LoadBalanced
        public WebClient.Builder loadBalancedWebClientBuilder() {
            return WebClient.builder();
        }


    }

}
