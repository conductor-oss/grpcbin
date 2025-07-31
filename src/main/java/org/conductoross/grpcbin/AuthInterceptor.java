package org.conductoross.grpcbin;

import io.grpc.*;

public class AuthInterceptor implements ServerInterceptor {
    
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String VALID_TOKEN = "test-bearer-token-123"; // For testing purposes
    
    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {
        
        String authHeader = headers.get(Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER));
        
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            call.close(Status.UNAUTHENTICATED.withDescription("Missing or invalid authorization header"), new Metadata());
            return new ServerCall.Listener<ReqT>() {};
        }
        
        String token = authHeader.substring(BEARER_PREFIX.length()).trim();
        
        if (!VALID_TOKEN.equals(token)) {
            call.close(Status.UNAUTHENTICATED.withDescription("Invalid bearer token"), new Metadata());
            return new ServerCall.Listener<ReqT>() {};
        }
        
        // Token is valid, proceed with the call
        System.out.println("✅ Valid token provided: " + token);
        return next.startCall(call, headers);
    }
}
