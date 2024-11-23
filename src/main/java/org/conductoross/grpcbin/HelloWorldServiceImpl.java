package org.conductoross.grpcbin;

import complex.Complex;
import io.grpc.stub.StreamObserver;

public class HelloWorldServiceImpl extends HelloWorldServiceGrpc.HelloWorldServiceImplBase {
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