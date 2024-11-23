# Test server for all things gRPC

## Services
```protobuf
service HelloWorldService {
  rpc SayHello(helloworld.HelloRequest) returns (helloworld.HelloResponse) {}
  rpc ComplexRequest(complex.ComplexMessage) returns (complex.ComplexMessage) {}
  rpc ComplexRequestStream(complex.ComplexMessage) returns (stream complex.ComplexMessage) {}
}
```
## Run Locally

```shell
# Build
./gradlew build
java -jar build/libs/grpcbin-1.0.0-server.jar
```