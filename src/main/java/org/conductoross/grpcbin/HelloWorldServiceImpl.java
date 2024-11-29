package org.conductoross.grpcbin;

import complex.Complex;
import io.grpc.stub.StreamObserver;

import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class HelloWorldServiceImpl extends HelloWorldServiceGrpc.HelloWorldServiceImplBase {
    private static final Random random = new Random();
    private static final Semaphore semaphore = new Semaphore(5);// Limit to 5 concurrent requests

    @Override
    public void sayHello(HelloRequest request, StreamObserver<HelloResponse> responseObserver) {
        HelloResponse response = HelloResponse.newBuilder()
                .setMessage("Hello, " + request.getName())
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void complexRequest(Complex.ComplexMessage request, StreamObserver<Complex.ComplexMessage> responseObserver) {
        var response = Complex.ComplexMessage.newBuilder(request)
                .setName("response_value")
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void sayHelloWithDelay(HelloRequest request, StreamObserver<HelloResponse> responseObserver) {
        try {
            // Simulate a delay (e.g., 5 seconds)
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        String message = "Hello, " + request.getName();
        HelloResponse response = HelloResponse.newBuilder()
                .setMessage(message)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void sayHelloIntermittentFailures(HelloRequest request, StreamObserver<HelloResponse> responseObserver) {
        // Randomly simulate a failure
        if (random.nextBoolean()) {
            responseObserver.onError(new RuntimeException("Simulated failure"));
            return;
        }

        String message = "Hello, " + request.getName();
        HelloResponse response = HelloResponse.newBuilder()
                .setMessage(message)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void sayHelloServiceDown(org.conductoross.grpcbin.HelloRequest request,
                                    io.grpc.stub.StreamObserver<org.conductoross.grpcbin.HelloResponse> responseObserver) {
        responseObserver.onError(new RuntimeException("Service is down"));
    }

    @Override
    public void sayHelloOverloaded(org.conductoross.grpcbin.HelloRequest request,
                                   io.grpc.stub.StreamObserver<org.conductoross.grpcbin.HelloResponse> responseObserver) {
        try {
            // Acquire a permit before processing
            if (!semaphore.tryAcquire()) {
                responseObserver.onError(new RuntimeException("Service is overloaded"));
                return;
            }

            // Simulate some processing time
            String message = "Hello, " + request.getName();
            HelloResponse response = HelloResponse.newBuilder()
                    .setMessage(message)
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } finally {
            // Release the permit after processing
            semaphore.release();
        }
    }

    @Override
    public void complexRequestStream(Complex.ComplexMessage request, StreamObserver<Complex.ComplexMessage> responseObserver) {
        for (int i = 0; i < 100; i++) {
            var response = Complex.ComplexMessage.newBuilder(request)
                    .setId(i)
                    .setName("response_value")
                    .build();
            responseObserver.onNext(response);
        }
        responseObserver.onCompleted();
    }
}