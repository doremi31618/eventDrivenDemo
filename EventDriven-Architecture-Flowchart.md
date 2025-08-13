# Event-Driven Architecture Flowchart

## Real-World Scenario with Retry, Status Tracking & Event Isolation

This flowchart addresses the three key issues identified in the current implementation:
1. **Retry Mechanism** - Exponential backoff with DLQ
2. **Handler Status Tracking** - Detailed attempt logging  
3. **Event Isolation** - Concurrent processing to prevent blocking

```mermaid
flowchart TD
    %% Entry Points
    Client[Client Request] --> Controller[EventController]
    
    %% Event Publishing
    Controller --> |Create CustomEvent| EventBus{EventBus Factory}
    EventBus --> |kafka profile| KafkaBus[KafkaEventBus]
    EventBus --> |rabbitmq profile| RabbitBus[RabbitMQEventBus]
    
    %% Message Brokers
    KafkaBus --> |publish| KafkaTopic[Kafka Topic<br/>event-topic]
    RabbitBus --> |publish| RabbitQueue[RabbitMQ Queue<br/>eventQueue]
    
    %% Listeners with Concurrency
    KafkaTopic --> |@KafkaListener<br/>Concurrent Consumer| KafkaListener[KafkaEventListener<br/>Max 10 Concurrent]
    RabbitQueue --> |@RabbitListener<br/>Concurrent Consumer| RabbitListener[RabbitMQEventListener<br/>Max 10 Concurrent]
    
    %% Async Event Processing
    KafkaListener --> |Async Processing| AsyncProcessor[AsyncEventProcessor<br/>@Async ThreadPool]
    RabbitListener --> |Async Processing| AsyncProcessor
    
    %% Event Processing with Status Tracking
    AsyncProcessor --> CreateRecord[Create EventRecord<br/>Status: PROCESSING]
    CreateRecord --> SaveInitial[(Save to DB)]
    
    SaveInitial --> Validate{Validate Event}
    Validate --> |Invalid| RecordFailure[Update Status: INVALID]
    Validate --> |Valid| CheckIdempotent{Check Idempotency<br/>Duplicate Event?}
    
    CheckIdempotent --> |Duplicate| SkipProcessing[Skip Processing<br/>Status: DUPLICATE]
    CheckIdempotent --> |New Event| StartProcessing[Start Processing<br/>Create EventAttempt]
    
    %% Dispatcher with Timeout
    StartProcessing --> Dispatcher[EventDispatcher<br/>with Timeout Control]
    Dispatcher --> HandlerCheck{Handler Found?}
    
    HandlerCheck --> |No| NoHandlerError[No Handler Error<br/>Status: NO_HANDLER]
    HandlerCheck --> |Yes| ExecuteHandler[Execute Handler<br/>with Timeout]
    
    %% Handler Execution with Status Tracking
    ExecuteHandler --> HandlerResult{Handler Result}
    HandlerResult --> |Success| RecordSuccess[Status: SUCCESS<br/>Update EventAttempt]
    HandlerResult --> |Exception| RecordAttemptFailure[Record Attempt Failure<br/>Increment Retry Count]
    HandlerResult --> |Timeout| RecordTimeoutFailure[Record Timeout Failure<br/>Increment Retry Count]
    
    %% Retry Logic with Exponential Backoff
    RecordAttemptFailure --> CheckRetryCount{Retry Count < Max?<br/>Current: attempt/3}
    RecordTimeoutFailure --> CheckRetryCount
    
    CheckRetryCount --> |Yes| CalculateDelay[Calculate Delay<br/>2^attempt * 1000ms]
    CheckRetryCount --> |No| SendToDLQ[Send to DLQ<br/>Status: FAILED_MAX_RETRY]
    
    CalculateDelay --> ScheduleRetry[Schedule Retry<br/>@Scheduled Task]
    ScheduleRetry --> |After Delay| StartProcessing
    
    %% Dead Letter Queue Handling
    SendToDLQ --> DLQTopic[DLQ Topic/Queue<br/>event-dlq]
    DLQTopic --> ManualReview[Manual Review Required<br/>Admin Dashboard]
    
    %% Success Path
    RecordSuccess --> UpdateEventRecord[Update EventRecord<br/>Status: COMPLETED]
    UpdateEventRecord --> NotifyMonitoring[Send Metrics<br/>Success Counter++]
    
    %% Error Paths
    RecordFailure --> UpdateFailureRecord[Update EventRecord<br/>Status: FAILED]
    NoHandlerError --> UpdateNoHandlerRecord[Update EventRecord<br/>Status: NO_HANDLER]
    SkipProcessing --> UpdateSkipRecord[Update EventRecord<br/>Status: SKIPPED]
    
    %% Final Status Updates
    UpdateEventRecord --> EndSuccess[End: Success ✅]
    UpdateFailureRecord --> EndFailure[End: Failure ❌]
    UpdateNoHandlerRecord --> EndNoHandler[End: No Handler ⚠️]
    UpdateSkipRecord --> EndSkip[End: Skipped ⏭️]
    SendToDLQ --> EndDLQ[End: DLQ 💀]
    
    %% Database Models
    subgraph Database["Database Models"]
        EventRecord_DB[(EventRecord<br/>id, type, payload, status<br/>created_at, updated_at<br/>retry_count, final_status)]
        EventAttempt_DB[(EventAttempt<br/>id, event_id, attempt_number<br/>started_at, completed_at<br/>status, error_message<br/>handler_name, processing_time)]
        IdempotencyKey_DB[(IdempotencyKey<br/>event_id, processed_at<br/>checksum)]
    end
    
    SaveInitial -.-> EventRecord_DB
    StartProcessing -.-> EventAttempt_DB
    RecordSuccess -.-> EventAttempt_DB
    RecordAttemptFailure -.-> EventAttempt_DB
    CheckIdempotent -.-> IdempotencyKey_DB
    
    %% Monitoring & Alerting
    subgraph Monitoring["Monitoring & Alerting"]
        MetricSuccess[Success Rate Metric]
        MetricLatency[Processing Latency]
        MetricRetry[Retry Rate Metric]
        MetricDLQ[DLQ Alert]
        Dashboard[Admin Dashboard<br/>Real-time Status]
    end
    
    NotifyMonitoring -.-> MetricSuccess
    NotifyMonitoring -.-> MetricLatency
    RecordAttemptFailure -.-> MetricRetry
    SendToDLQ -.-> MetricDLQ
    ManualReview -.-> Dashboard
    
    %% Concurrent Handler Types
    subgraph Handlers["Event Handlers (Concurrent)"]
        EmailHandler[SendEmailEventHandler<br/>Async Processing]
        ReportHandler[GenerateReportEventHandler<br/>Async Processing]
        ChemicalHandler[ChemicalStatusHandler<br/>Async Processing]
        CustomHandler[CustomEventHandler<br/>Async Processing]
    end
    
    Dispatcher -.-> EmailHandler
    Dispatcher -.-> ReportHandler
    Dispatcher -.-> ChemicalHandler
    Dispatcher -.-> CustomHandler
    
    %% Configuration
    subgraph Config["Configuration"]
        RetryConfig[Retry Config<br/>max-attempts: 3<br/>initial-delay: 1s<br/>multiplier: 2]
        ConcurrencyConfig[Concurrency Config<br/>thread-pool-size: 10<br/>queue-capacity: 100]
        TimeoutConfig[Timeout Config<br/>handler-timeout: 30s<br/>processing-timeout: 60s]
    end
    
    %% Styles
    classDef successPath fill:#d4edda,stroke:#155724,color:#155724
    classDef errorPath fill:#f8d7da,stroke:#721c24,color:#721c24
    classDef processingPath fill:#fff3cd,stroke:#856404,color:#856404
    classDef dbNode fill:#e2e3e5,stroke:#41464b,color:#41464b
    classDef monitorNode fill:#cce5ff,stroke:#004085,color:#004085
    
    class EndSuccess successPath
    class EndFailure,EndDLQ errorPath
    class EndNoHandler,EndSkip processingPath
    class EventRecord_DB,EventAttempt_DB,IdempotencyKey_DB dbNode
    class MetricSuccess,MetricLatency,MetricRetry,MetricDLQ,Dashboard monitorNode
```

## Key Architectural Improvements

### 1. Retry Mechanism with Exponential Backoff
- **Max Retry Attempts**: 3 attempts per event
- **Exponential Backoff**: 1s → 2s → 4s delays
- **Dead Letter Queue**: Failed events after max retries
- **Scheduled Retry**: Using `@Scheduled` for delayed processing

### 2. Comprehensive Status Tracking
- **EventRecord**: Main event lifecycle status
- **EventAttempt**: Individual processing attempts with detailed logs
- **IdempotencyKey**: Prevent duplicate processing
- **Real-time Monitoring**: Success rates, latencies, retry counts

### 3. Event Isolation & Concurrent Processing
- **Async Processing**: `@Async` with dedicated thread pools
- **Concurrent Consumers**: Multiple Kafka/RabbitMQ consumers
- **Thread Pool Configuration**: Configurable concurrency limits
- **Timeout Controls**: Prevent hanging handlers

### 4. Real-World Scenarios Covered

#### Scenario A: High-Volume Chemical Status Updates
```
Chemical data change → 1000 events → Concurrent processing → Some fail → Auto retry → Success
```

#### Scenario B: Email Service Temporary Outage
```
Email event → Handler timeout → Retry with backoff → Service recovers → Success on retry 2
```

#### Scenario C: Database Connection Issues
```
Multiple events → DB connection lost → All fail → Retry after delay → Connection restored → Batch success
```

#### Scenario D: Invalid Event Data
```
Malformed event → Validation fails → Mark as INVALID → No retry → Alert admin
```

## Benefits of This Architecture

1. **Fault Tolerance**: Auto-recovery from transient failures
2. **Scalability**: Concurrent event processing prevents bottlenecks  
3. **Observability**: Detailed tracking and monitoring
4. **Maintainability**: Clear separation of concerns
5. **Flexibility**: Configurable retry policies and timeouts

## Implementation Priority

1. **Phase 1**: Add EventAttempt model and retry logic
2. **Phase 2**: Implement async processing with thread pools
3. **Phase 3**: Add comprehensive monitoring and DLQ handling
4. **Phase 4**: Implement idempotency checking and admin dashboard