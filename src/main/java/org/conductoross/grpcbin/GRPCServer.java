package org.conductoross.grpcbin;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.protobuf.services.ProtoReflectionService;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class GRPCServer {

    private Server noAuthServer;
    private Server authServer;

    private void start() throws IOException {
        // Server without authentication (port 50051)
        int noAuthPort = 50051;
        noAuthServer = ServerBuilder.forPort(noAuthPort)
                .addService(new HelloWorldServiceImpl())
                .addService(ProtoReflectionService.newInstance())
                .build()
                .start();
        System.out.println("✅ Server WITHOUT auth started on port " + noAuthPort);

        // Server with authentication (port 50052)
        int authPort = 50052;
        authServer = ServerBuilder.forPort(authPort)
                .addService(new HelloWorldServiceImpl())
                .addService(ProtoReflectionService.newInstance())
                .intercept(new AuthInterceptor()) // Add auth interceptor
                .build()
                .start();
        System.out.println("🔐 Server WITH auth started on port " + authPort + " (requires Bearer token: test-bearer-token-123)");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down gRPC servers");
            GRPCServer.this.stop();
        }));
    }

    private void stop() {
        try {
            if (noAuthServer != null) {
                noAuthServer.shutdown().awaitTermination(5, TimeUnit.SECONDS);
                System.out.println("No-auth server shut down");
            }
            if (authServer != null) {
                authServer.shutdown().awaitTermination(5, TimeUnit.SECONDS);
                System.out.println("Auth server shut down");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void blockUntilShutdown() throws InterruptedException {
        if (noAuthServer != null) {
            noAuthServer.awaitTermination();
        }
        if (authServer != null) {
            authServer.awaitTermination();
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        GRPCServer server = new GRPCServer();
        server.start();
        server.blockUntilShutdown();
    }
}
