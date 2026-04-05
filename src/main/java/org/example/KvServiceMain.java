package org.example;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.example.grpc.KvGrpcService;
import org.example.repository.TarantoolKvRepository;

public class KvServiceMain {
    public static void main(String[] args) throws Exception {
        System.out.println("Connecting to Tarantool...");

        TarantoolKvRepository repo = new TarantoolKvRepository(
                "localhost", 3301, "admin", "password"
        );

        Server server = ServerBuilder
                .forPort(9090)
                .addService(new KvGrpcService(repo))
                .build()
                .start();

        System.out.println("gRPC server started on port 9090");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down...");
            server.shutdown();
            repo.close();
        }));

        server.awaitTermination();
    }
}