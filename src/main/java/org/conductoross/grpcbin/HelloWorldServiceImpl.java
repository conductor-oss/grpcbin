package org.conductoross.grpcbin;

import complex.Complex;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class HelloWorldServiceImpl extends HelloWorldServiceGrpc.HelloWorldServiceImplBase {
    private static final Random random = new Random();
    private static final Semaphore semaphore = new Semaphore(5);// Limit to 5 concurrent requests
    // Track service cycle state
    private static final Map<String, ServiceState> serviceStates = new ConcurrentHashMap<>();

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
            // Generate a random number between 0 and 100
            int delayChance = random.nextInt(100) + 1;

            // Determine the delay based on the random number
            long delayInSeconds;
            if (delayChance <= 85) {
                delayInSeconds = 1;  // 85% chance for ~5 seconds delay
            } else if (delayChance <= 95) {
                delayInSeconds = 5; // 10% chance for ~10 seconds delay
            } else if (delayChance <= 99) {
                delayInSeconds = 10; // 4% chance for ~15 seconds delay
            } else {
                delayInSeconds = 20; // 1% chance for ~20 seconds delay
            }

            System.out.println("Delaying for " + delayInSeconds + " seconds...");
            TimeUnit.SECONDS.sleep(delayInSeconds);

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

            // Simulate some processing (delay) to emulate load
            int delay = new Random().nextInt(5) + 1;  // Random delay between 1 and 5 seconds
            TimeUnit.SECONDS.sleep(delay);

            // Simulate some processing time
            String message = "Hello, " + request.getName();
            HelloResponse response = HelloResponse.newBuilder()
                    .setMessage(message)
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            // Release the permit after processing
            semaphore.release();
        }
    }

    @Override
    public void sayHelloWithFailureTypes(org.conductoross.grpcbin.HelloRequest request,
                                         io.grpc.stub.StreamObserver<org.conductoross.grpcbin.HelloResponse> responseObserver) {
        Random random = new Random();

        // 10% chance of bad request (400)
        if (random.nextInt(100) < 10) {
            responseObserver.onError(new StatusRuntimeException(Status.INVALID_ARGUMENT.withDescription("Bad Request")));
            return;
        }

        // 20% chance of service unavailable (503)
        if (random.nextInt(100) < 20) {
            responseObserver.onError(new StatusRuntimeException(Status.UNAVAILABLE.withDescription("Service Unavailable")));
            return;
        }

        // 5% chance of rate limiting (429)
        if (random.nextInt(100) < 5) {
            responseObserver.onError(new StatusRuntimeException(Status.RESOURCE_EXHAUSTED.withDescription("Rate limit exceeded")));
            return;
        }

        // Normal response
        String message = "Hello, " + request.getName();
        HelloResponse response = HelloResponse.newBuilder()
                .setMessage(message)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();

    }

    @Override
    public void sayHelloWithExternalDependency(org.conductoross.grpcbin.HelloRequest request,
                                               io.grpc.stub.StreamObserver<org.conductoross.grpcbin.HelloResponse> responseObserver) {
        // Simulate external system delay (API, DB)
        int externalDelay = random.nextInt(10) + 1; // 1 to 10 seconds
        try {
            System.out.println("Simulating external API delay: " + externalDelay + " seconds");
            TimeUnit.SECONDS.sleep(externalDelay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Simulate a 10% chance of failure from the external system
        if (random.nextInt(100) < 10) {
            responseObserver.onError(new RuntimeException("External API failure"));
            return;
        }

        // Generate response based on the external system’s success
        String message = "Hello, " + request.getName() + ". Data retrieved successfully from external service!";

        // Build the response with dynamic content
        HelloResponse response = HelloResponse.newBuilder()
                .setMessage(message)
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

// Add these class members to your HelloWorldServiceImpl class

    // First, add this method to your HelloWorldServiceImpl class
    @Override
    public void sayHelloWithCyclicalDegradation(
            DegradationRequest request,
            StreamObserver<DegradationResponse> responseObserver) {

        try {
            // Get the parameters from the request
            int normalPeriod = request.getNormalPeriod();
            int degradationPeriod = request.getDegradationPeriod();
            int failurePeriod = request.getFailurePeriod();
            int recoveryPeriod = request.getRecoveryPeriod();
            int initialDelay = request.getInitialDelay();
            int degradationRate = request.getDegradationRate();
            int failureThreshold = request.getFailureThreshold();

            // Create a unique key for this configuration
            String configKey = String.format("cycle_%d_%d_%d_%d",
                    normalPeriod, degradationPeriod, failurePeriod, recoveryPeriod);

            // Get or create the service state
            ServiceState state = serviceStates.computeIfAbsent(configKey, k ->
                    new ServiceState(normalPeriod, degradationPeriod, failurePeriod, recoveryPeriod,
                            initialDelay, degradationRate, failureThreshold));

            // Get the current phase and calculated delay
            ServicePhase currentPhase = state.getCurrentPhase();
            int currentDelay = state.calculateCurrentDelay();
            long timeRemaining = state.getTimeRemainingInPhase() / 1000; // convert to seconds

            System.out.println("gRPC Service phase: " + currentPhase +
                    ", Delay: " + currentDelay + "ms, Time remaining: " + timeRemaining + "s");

            // If we're in the FAILING phase, fail the request
            if (currentPhase == ServicePhase.FAILING) {
                responseObserver.onError(
                        Status.INTERNAL
                                .withDescription("Service in FAILURE phase (" + timeRemaining +
                                        "s remaining until recovery begins)")
                                .asRuntimeException());
                return;
            }

            // Apply the calculated delay
            try {
                Thread.sleep(currentDelay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                responseObserver.onError(
                        Status.INTERNAL
                                .withDescription("Simulation was interrupted")
                                .asRuntimeException());
                return;
            }

            // Calculate cycle information
            long totalCycleDuration = (normalPeriod + degradationPeriod + failurePeriod + recoveryPeriod);
            long elapsedTimeSecs = (System.currentTimeMillis() - state.cycleStartTime) / 1000;
            long cycleNumber = (elapsedTimeSecs / totalCycleDuration) + 1;
            long timeInCurrentCycle = elapsedTimeSecs % totalCycleDuration;

            // Build and send response
            DegradationResponse response = DegradationResponse.newBuilder()
                    .setStatus("success")
                    .setResponseTime(currentDelay)
                    .setPhase(currentPhase.toString())
                    .setTimeRemainingInPhase(timeRemaining)
                    .setExpectedCircuitBreakerState(state.getExpectedCircuitBreakerState())
                    .setCycleNumber(cycleNumber)
                    .setTotalCycleDuration(totalCycleDuration)
                    .setTimeInCurrentCycle(timeInCurrentCycle)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription("Error processing degradation simulation: " + e.getMessage())
                            .asRuntimeException());
        }
    }

    // Add a reset method for gRPC too
    @Override
    public void resetSimulationState(
            ResetRequest request,
            StreamObserver<ResetResponse> responseObserver) {

        String configKey = request.getConfigKey().isEmpty() ? null : request.getConfigKey();
        Map<String, Object> result = new HashMap<>();

        if (configKey != null) {
            // Reset only a specific configuration
            serviceStates.remove(configKey);
            result.put("resetType", "specific");
            result.put("configKey", configKey);
        } else {
            // Reset all configurations
            int serviceStatesCount = serviceStates.size();
            serviceStates.clear();
            result.put("resetType", "all");
            result.put("serviceStatesCleared", serviceStatesCount);
        }

        result.put("status", "success");
        result.put("timestamp", System.currentTimeMillis());

        ResetResponse response = ResetResponse.newBuilder()
                .setStatus((String) result.get("status"))
                .setResetType((String) result.get("resetType"))
                .setTimestamp((long) result.get("timestamp"))
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    // Enum to track the current phase of the service
    public enum ServicePhase {
        NORMAL,
        DEGRADING,
        FAILING,
        RECOVERING
    }

    // Class to hold the service state - mostly copied from your CircuitBreakerService
    private static class ServiceState {
        private final int normalPeriod;
        private final int degradationPeriod;
        private final int failurePeriod;
        private final int recoveryPeriod;
        private final int initialDelay;
        private final int degradationRate;
        private final int failureThreshold;
        private final int totalCycleDuration;
        private long cycleStartTime;

        public ServiceState(int normalPeriod, int degradationPeriod, int failurePeriod, int recoveryPeriod,
                            int initialDelay, int degradationRate, int failureThreshold) {
            this.cycleStartTime = System.currentTimeMillis();
            this.normalPeriod = normalPeriod;
            this.degradationPeriod = degradationPeriod;
            this.failurePeriod = failurePeriod;
            this.recoveryPeriod = recoveryPeriod;
            this.initialDelay = initialDelay;
            this.degradationRate = degradationRate;
            this.failureThreshold = failureThreshold;
            this.totalCycleDuration = normalPeriod + degradationPeriod + failurePeriod + recoveryPeriod;
        }

        public ServicePhase getCurrentPhase() {
            long currentTime = System.currentTimeMillis();
            long elapsedTime = currentTime - cycleStartTime;

            // Check if we've completed a full cycle and need to reset
            if (elapsedTime >= totalCycleDuration * 1000L) {
                // Reset the cycle start time to begin a new cycle
                long cyclesCompleted = elapsedTime / (totalCycleDuration * 1000L);
                cycleStartTime += cyclesCompleted * (totalCycleDuration * 1000L);

                // Recalculate elapsed time after reset
                elapsedTime = currentTime - cycleStartTime;
            }

            if (elapsedTime < normalPeriod * 1000L) {
                return ServicePhase.NORMAL;
            } else if (elapsedTime < (normalPeriod + degradationPeriod) * 1000L) {
                return ServicePhase.DEGRADING;
            } else if (elapsedTime < (normalPeriod + degradationPeriod + failurePeriod) * 1000L) {
                return ServicePhase.FAILING;
            } else {
                return ServicePhase.RECOVERING;
            }
        }

        public long getTimeRemainingInPhase() {
            long currentTime = System.currentTimeMillis();
            long elapsedTime = currentTime - cycleStartTime;

            // Check if we've completed a full cycle and need to reset
            if (elapsedTime >= totalCycleDuration * 1000L) {
                // Reset the cycle start time to begin a new cycle
                long cyclesCompleted = elapsedTime / (totalCycleDuration * 1000L);
                cycleStartTime += cyclesCompleted * (totalCycleDuration * 1000L);

                // Recalculate elapsed time after reset
                elapsedTime = currentTime - cycleStartTime;
            }

            if (elapsedTime < normalPeriod * 1000L) {
                return normalPeriod * 1000L - elapsedTime;
            } else if (elapsedTime < (normalPeriod + degradationPeriod) * 1000L) {
                return (normalPeriod + degradationPeriod) * 1000L - elapsedTime;
            } else if (elapsedTime < (normalPeriod + degradationPeriod + failurePeriod) * 1000L) {
                return (normalPeriod + degradationPeriod + failurePeriod) * 1000L - elapsedTime;
            } else {
                return (normalPeriod + degradationPeriod + failurePeriod + recoveryPeriod) * 1000L - elapsedTime;
            }
        }

        public int calculateCurrentDelay() {
            ServicePhase phase = getCurrentPhase();
            long currentTime = System.currentTimeMillis();
            long elapsedTime = currentTime - cycleStartTime;

            // Check if we've completed a full cycle and need to reset
            if (elapsedTime >= totalCycleDuration * 1000L) {
                // Reset the cycle start time to begin a new cycle
                long cyclesCompleted = elapsedTime / (totalCycleDuration * 1000L);
                cycleStartTime += cyclesCompleted * (totalCycleDuration * 1000L);

                // Recalculate elapsed time after reset
                elapsedTime = currentTime - cycleStartTime;
            }

            switch (phase) {
                case NORMAL:
                    return initialDelay;

                case DEGRADING:
                    // Calculate how far we are through the degradation period (0.0 to 1.0)
                    double degradationProgress = (double) (elapsedTime - normalPeriod * 1000L) / (degradationPeriod * 1000L);
                    // Scale from initial delay to failure threshold
                    return (int) (initialDelay + degradationProgress * (failureThreshold - initialDelay));

                case FAILING:
                    // During failure phase, always use the threshold or higher
                    return failureThreshold;

                case RECOVERING:
                    // Calculate how far we are through the recovery period (0.0 to 1.0)
                    double recoveryProgress = (double) (elapsedTime - (normalPeriod + degradationPeriod + failurePeriod) * 1000L) / (recoveryPeriod * 1000L);
                    // Scale from failure threshold back to initial delay
                    return (int) (failureThreshold - recoveryProgress * (failureThreshold - initialDelay));

                default:
                    return initialDelay;
            }
        }

        public String getExpectedCircuitBreakerState() {
            ServicePhase phase = getCurrentPhase();

            switch (phase) {
                case NORMAL:
                    return "CLOSED";
                case DEGRADING:
                    return "CLOSED (approaching threshold)";
                case FAILING:
                    return "OPEN";
                case RECOVERING:
                    double recoveryProgress = (double) getTimeRemainingInPhase() / (recoveryPeriod * 1000L);
                    if (recoveryProgress > 0.7) {
                        return "OPEN";
                    } else if (recoveryProgress > 0.3) {
                        return "HALF-OPEN";
                    } else {
                        return "CLOSED";
                    }
                default:
                    return "CLOSED";
            }
        }
    }
}