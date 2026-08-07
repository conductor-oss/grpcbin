package org.conductoross.grpcbin;

import io.grpc.stub.StreamObserver;

/**
 * Echo implementation of KitchensinkService.
 *
 * Every RPC reflects the request straight back to the caller, exercising all
 * proto3 syntax features (scalars, nested messages, enums, repeated, map,
 * oneof, well-known types, and recursive message references) over a real gRPC
 * transport without any application logic.
 */
public class KitchensinkServiceImpl extends KitchensinkServiceGrpc.KitchensinkServiceImplBase {

    @Override
    public void echoScalars(AllScalars request, StreamObserver<AllScalars> responseObserver) {
        responseObserver.onNext(request);
        responseObserver.onCompleted();
    }

    @Override
    public void echoNested(Outer request, StreamObserver<Outer> responseObserver) {
        responseObserver.onNext(request);
        responseObserver.onCompleted();
    }

    @Override
    public void echoRepeated(RepeatedFields request, StreamObserver<RepeatedFields> responseObserver) {
        responseObserver.onNext(request);
        responseObserver.onCompleted();
    }

    @Override
    public void echoMap(MapFields request, StreamObserver<MapFields> responseObserver) {
        responseObserver.onNext(request);
        responseObserver.onCompleted();
    }

    @Override
    public void echoOneof(OneofMessage request, StreamObserver<OneofMessage> responseObserver) {
        responseObserver.onNext(request);
        responseObserver.onCompleted();
    }

    @Override
    public void echoWkt(WellKnownTypes request, StreamObserver<WellKnownTypes> responseObserver) {
        responseObserver.onNext(request);
        responseObserver.onCompleted();
    }

    @Override
    public void echoTree(TreeNode request, StreamObserver<TreeNode> responseObserver) {
        responseObserver.onNext(request);
        responseObserver.onCompleted();
    }

    // NodeA → NodeB: map value and count from the request where possible.
    @Override
    public void echoRecursive(NodeA request, StreamObserver<NodeB> responseObserver) {
        NodeB response = NodeB.newBuilder()
                .setCount(request.getValue().length())
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
