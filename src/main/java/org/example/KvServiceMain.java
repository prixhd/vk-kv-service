package org.example;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.example.grpc.KvGrpcService;
import org.example.repository.TarantoolKvRepository;

public class KvServiceMain {
    public static void main(String[] args) throws Exception {
        System.out.println("Подключение к Tarantool...");

        String host = System.getenv().getOrDefault("TARANTOOL_HOST", "localhost");
        int port = Integer.parseInt(System.getenv().getOrDefault("TARANTOOL_PORT", "3301"));
        String user = System.getenv().getOrDefault("TARANTOOL_USER", "admin");
        String password = System.getenv().getOrDefault("TARANTOOL_PASSWORD", "password");
        int grpcPort = Integer.parseInt(System.getenv().getOrDefault("GRPC_PORT", "9090"));

        TarantoolKvRepository repo = new TarantoolKvRepository(
                host, port, user, password
        );

        Server server = ServerBuilder
                .forPort(grpcPort)
                .addService(new KvGrpcService(repo))
                .build()
                .start();

        System.out.println("gRPC сервер запущен на порту: " + grpcPort);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Завершение работы");
            server.shutdown();
            repo.close();
        }));

        server.awaitTermination();
    }
}