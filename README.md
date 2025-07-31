# Test server for all things gRPC

## Services
```protobuf
service HelloWorldService {
  rpc SayHello(helloworld.HelloRequest) returns (helloworld.HelloResponse) {}
  rpc ComplexRequest(complex.ComplexMessage) returns (complex.ComplexMessage) {}
  rpc ComplexRequestStream(complex.ComplexMessage) returns (stream complex.ComplexMessage) {}
  rpc SayHelloWithCyclicalDegradation (DegradationRequest) returns (DegradationResponse) {}

}
```
## Run Locally

```shell
# Build
./gradlew build
java -jar build/libs/grpcbin-1.0.0-server.jar
```

- Port 50051 - No Authentication Required ✅
- Port 50052 - Bearer Token Required 🔐

## Compile protos to binary
Use the compiled protos to use with Orkes Conductor service orchestration
```shell
cd src/main/protos
protoc --proto_path=. --descriptor_set_out=compiled.bin *.proto
```