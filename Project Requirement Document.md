
### Project Requirement
1. 專案概述

1.1 背景與需求

本系統為事件驅動架構，需要將事件從應用程式非同步發送到後端處理服務。系統需支援多種中介訊息系統（Kafka / RabbitMQ），以因應「主產品服務需求」與「客戶端環境限制」。

由於申報系列模組有許多功能都需要監聽各個事件來觸發某個行為，例如：

毒化物的列管化學品狀態 在判斷化學品的已申請、未申請、無需申請狀態時需要判斷：

化學品在該廠區是否有儲位

化學品的成分濃度是否符合法規管制區間

化學品的成分是否為毒化物、關注物質

優先管理化學品動態備查 在判斷一個化學品是否需要進行動態備查時需要判斷：

化學品的 H-Code

成分是否被法規列管

是否需要申報

1.2 Problem Statement

高耦合性: 不同模組間直接調用，造成緊密耦合

維護困難: 業務邏輯分散在各個模組中，難以統一管理

效能問題: 重複計算相同的業務規則

可靠性不足: 缺乏事件處理的錯誤處理和重試機制

環境適應性: 需支援 SaaS 雲端環境和客戶 On-Premise 部署

2. User Story

2.1 Primary User Stories

US001: 化學品狀態自動更新

As Q-Chem 系統管理者

I want 當化學品相關資料異動時，系統能自動觸發相關狀態更新

So that 我不需要手動重新計算每個化學品的申報狀態和合規性

US002: 法規合規即時監控

As 法規遵循負責人

I want 當法規條件變更或化學品資料更新時，系統能即時檢查合規狀態

So that 我能及時發現並處理不符合法規的情況

US003: H Card 自動重新生成

As 安全管理人員

I want 當化學品儲存位置、濃度或分類變更時，相關的 H Card 能自動更新

So that 危害辨識卡始終保持最新和準確的資訊

US005: 多環境支援

As 系統部署管理員

I want 系統能支援雲端 SaaS 和客戶 On-Premise 不同的訊息佇列環境

So that 可以根據不同部署環境選擇最適合的技術方案

3. Acceptance Criteria

3.1 核心功能驗收標準

AC001: 事件觸發機制

✅ 系統能偵測到化學品主記錄的異動（新增、修改、刪除）

✅ 系統能偵測到化學品成分的異動

✅ 系統能偵測到儲存位置的異動

✅ 系統能偵測到法規資料的異動

✅ 每個事件都包含完整的變更資訊（異動前後的值）

AC005: 多環境支援

✅ 支援 Kafka（SaaS 雲端環境）

✅ 支援 RabbitMQ（客戶 On-Premise 環境）

✅ 可透過設定檔切換訊息佇列類型

✅ 事件處理邏輯在不同環境下保持一致

4. 架構設計選擇

4.1 Kafka 與 RabbitMQ 可切換設計

選擇原因：

主產品 (SaaS 雲端環境) 採用 Kafka：

處理大量資料流（高併發、大吞吐量）

支援分區 (partition)、持久性 (log-based storage)

橫向擴展性 (scalability) 優勢

支援流式處理、順序性需求

客戶端專案 (On-Premise 環境) 採用 RabbitMQ：

客戶已有 RabbitMQ 部署經驗與環境限制

支援靈活的 Routing 與 Queue 策略

適合客製化需求，安裝部署相對輕量

實現方式：

使用 Spring Profiles + ConditionalOnProperty 控制「啟動 Kafka 或 RabbitMQ Listener」

設定檔 messaging.type 切換即可對應不同環境需求，開發無需修改代碼

4.2 整體架構流程

Controller 接收請求


進來的是一個 CustomEvent (或類似的 Event 物件)

根據 messaging.type (kafka or rabbitmq) 注入對應的 EventBus 實作


KafkaEventBus 或 RabbitMQEventBus

EventBus 負責發送訊息到對應的 Broker

KafkaEventListener / RabbitMQEventListener 各自作為 Consumer 監聽

Listener 接收到訊息後會呼叫 EventProcessor 處理業務邏輯

Processor 會去呼叫 Dispatcher

Dispatcher 會依照 CustomEvent 的 Type 決定呼叫哪個 Handler 做事

EventProcessor 執行完後，紀錄處理結果到資料庫

5. 系統分層架構

層級

功能

負責角色

Controller

接收請求並觸發事件

只知道「我要發事件」，不關心用哪個 Message Queue

EventBus (Interface)

定義 send(event)

不管傳輸協議，專注「我要把事件送出去」

KafkaEventBus / RabbitMQEventBus

把事件傳送到 Kafka 或 RabbitMQ

負責具體實作傳送邏輯

Listener (Kafka/RabbitMQ)

被動監聽訊息，收到後觸發事件處理

Consumer，監聽 Topic / Queue

EventProcessor

負責業務邏輯處理

統一進入點，做資料前處理與分派

Dispatcher

根據事件 Type 派發給對應 Handler

決定叫哪個 Handler 去處理

Handler (多個實作類別)

真正執行業務處理

各自專注處理一種事件邏輯

Repository

負責資料庫存取 (CRUD)

保存處理結果或紀錄，與 DB 互動

6. 技術決策摘要

決策點

選擇

理由

SaaS 環境訊息中介系統

Kafka

高吞吐量、可橫向擴展、資料流式處理需求

On-Premise 環境訊息中介系統

RabbitMQ

部署簡單、彈性 Routing、客戶環境限制

訊息處理邏輯

抽象為 EventProcessor

降低重複代碼、提升可維護性

訊息傳遞模式

異步處理 (Kafka / MQ)

提升系統解耦與擴展性

事件處理狀態紀錄

資料庫記錄處理狀態

提供可觀測性、錯誤補償能力

7. 風險與對策

風險

對策

中介訊息傳遞失敗

DB 記錄處理狀態並設立監控告警，支援人工或自動補償

設定檔錯誤導致 Listener 啟動錯誤

CI/CD Pipeline 加入啟動 Profile 測試 (Profile-Based Integration Test)

DB 寫入負載過大

可擴展為異步批次入庫 / ElasticSearch 優化查詢

事件處理順序問題

Kafka 使用 partition key 保證順序；RabbitMQ 使用單一佇列處理

重複事件處理

實作冪等性檢查，避免重複處理同一事件

### Tech Spec

1. 背景與需求
本系統為事件驅動架構，需要將事件從應用程式非同步發送到後端處理服務。

系統需支援多種中介訊息系統（Kafka / RabbitMQ），以因應「主產品服務需求」與「客戶端環境限制」。

每筆事件皆需記錄處理狀態（成功/失敗）以確保處理可追蹤與補償機制。

2. 架構設計選擇
2.1 Kafka 與 RabbitMQ 可切換設計
選擇原因：

主產品 (核心服務) 採用 Kafka：

處理大量資料流（高併發、大吞吐量），Kafka 在分區 (partition)、持久性 (log-based storage) 及橫向擴展性 (scalability) 上具備優勢。

支援流式處理、訂單性需求更強。

客戶端專案 (On-Premise / 私有環境) 採用 RabbitMQ：

許多客戶已有 RabbitMQ 部署經驗與環境限制 (如無 Kafka 基礎設施)。

RabbitMQ 支援靈活的 Routing 與 Queue 策略，適合客製化需求。

實現方式：

使用 Spring Profiles + ConditionalOnProperty 控制「啟動 Kafka 或 RabbitMQ Listener」。

設定檔 messaging.type 切換即可對應不同環境需求，開發無需修改代碼。

infra.png
整體流程：
Controller 接收請求

進來的是一個 CustomEvent (或類似的 Event 物件)

根據 messaging.type (kafka or rabbitmq) 注入對應的 EventBus 實作

KafkaEventBus 或 RabbitMQEventBus

這層負責「決定用哪一種 Messaging 中介傳出去」

EventBus 負責發送訊息到對應的 Broker

KafkaEventBus.send() → 發送到 Kafka topic

RabbitMQEventBus.send() → 發送到 RabbitMQ queue

KafkaEventListener / RabbitMQEventListener 各自作為 Consumer 監聽

Kafka 監聽 Topic

RabbitMQ 監聽 Queue

Listener 接收到訊息後會呼叫 EventProcessor 處理業務邏輯

例如：eventProcessor.processEvent(event);

Processor會去呼叫Dispatcher

Dispatcher會依照傳進來的CustomEvent的Type決定呼叫哪個Handler做事

EventProcessor 執行完後，紀錄處理結果到資料庫 (EventRecordRepository)

層級

功能

負責角色

Controller

接收請求並觸發事件

只知道「我要發事件」，不關心用哪個 Message Queue

EventBus (Interface)

定義 send(event)

不管傳輸協議，專注「我要把事件送出去」

KafkaEventBus / RabbitMQEventBus

把事件傳送到 Kafka 或 RabbitMQ

負責具體實作傳送邏輯

Listener (Kafka/RabbitMQ)

被動監聽訊息，收到後觸發事件處理

Consumer，監聽 Topic / Queue

EventProcessor

負責業務邏輯處理

統一進入點，做資料前處理與分派

Dispatcher

根據事件 Type 派發給對應 Handler

決定叫哪個 Handler 去處理

Handler (多個實作類別)

真正執行業務處理

各自專注處理一種事件邏輯

Repository

負責資料庫存取 (CRUD)

保存處理結果或紀錄，與 DB 互動

2.2 共用事件處理器 (EventProcessor)
Kafka / RabbitMQ 雖然底層實現不同，但實際處理事件的邏輯是一致的（如 Dispatcher 分發業務邏輯、DB 記錄狀態）。

將事件處理邏輯抽象為 EventProcessor，讓 Listener 僅負責「接收事件並轉交處理」。

效益：

降低代碼重複與維護成本。

方便統一加上錯誤處理、監控統計等功能。

2.3 異步訊息處理與可觀測性
訊息傳遞一律採用「異步模式 (Asynchronous Messaging)」，避免系統高併發場景下同步請求造成瓶頸。

事件處理狀態（成功/失敗）紀錄於資料庫中：

作為錯誤補償機制 (Retry/Manual Recovery)。

提供管理後台查詢與事件追蹤。

與監控/告警系統結合，做到可觀測性 (Observability)。

3. 技術決策摘要
決策點

選擇

理由

主產品訊息中介系統

Kafka

高吞吐量、可橫向擴展、資料流式處理需求

客戶端專案訊息中介系統

RabbitMQ

部署簡單、彈性 Routing、客戶環境限制

訊息處理邏輯

抽象為 EventProcessor

降低重複代碼、提升可維護性

訊息傳遞模式

異步處理 (Kafka / MQ)

提升系統解耦與擴展性

事件處理狀態紀錄

資料庫記錄處理狀態

提供可觀測性、錯誤補償能力

4. 擴展性與未來規劃
若未來需支援其他中介系統（如 AWS SQS、Azure Service Bus），僅需新增 Listener，Processor 可無痛複用。

EventProcessor 可進一步擴充如：

重試機制（自動補償, Kafka支援, RabbitMQ須自己維護邏輯）。

事件追蹤與監控指標。

可選同步回應（部分場景需求）。

5. 風險與對策
風險

對策

中介訊息傳遞失敗

DB 記錄處理狀態並設立監控告警，支援人工或自動補償

設定檔錯誤導致 Listener 啟動錯誤

CI/CD Pipeline 加入啟動 Profile 測試 (Profile-Based Integration Test)

DB 寫入負載過大

可擴展為異步批次入庫 / ElasticSearch 優化查詢

6. 決策結論
目前主產品以 Kafka 為核心架構處理高流量需求，客戶端專案則因應環境採用 RabbitMQ。

採用 Profile 切換 + 抽象處理邏輯的設計能降低維護負擔，且具備良好的擴展性與靈活性。

此架構能兼顧「業務需求」與「技術彈性」，為日後系統擴展與多樣化場景提供穩健基礎。

對 SaaS 平台來說（雲端環境）
事件量大、服務之間解耦 → Kafka 是業界標準。

事件要保留、支持事件重播 → Kafka 勝任。

對客戶 On-Prem 雙主機來說
客戶環境大多是自己控管，事件頻率與數據量不會像 SaaS 系統那麼大。

他們只需要在雙主機上確保「事件執行一次、處理成功/失敗要有紀錄」。

RabbitMQ 天生支援「排隊、輪流派發、重試、死信佇列」，且比 Kafka 輕量，適合安裝在客戶的雙主機上做內部佇列處理。




### C4 Model 

⸻

C1: System Context Diagram（系統環境圖）

目的：顯示 Q-Chem 事件觸發系統與外部實體的互動。

主要元素：
	•	外部使用者
	•	系統管理員 / 安全管理員 / 法規遵循負責人
	•	前端管理介面
	•	外部系統
	•	Kafka / RabbitMQ（訊息中介）
	•	資料庫（MySQL / ElasticSearch）
	•	Q-Chem 事件觸發系統
	•	接收應用事件，發送到訊息中介，處理後回寫狀態，並提供可視化監控 API

[使用者] → [前端介面] → [Q-Chem 事件觸發系統] ↔ [資料庫]
                                             ↕
                                       [Kafka/RabbitMQ]


⸻

C2: Container Diagram（容器圖）

目的：顯示系統內主要「容器」（應用服務 / 儲存 / 外部系統）與資料流。

主要容器：
	1.	Frontend（Web）
	•	儀表板、事件檢視、告警通知
	•	呼叫 API 查詢事件狀態、觸發重播
	2.	Event Trigger Service（Spring Boot）
	•	Controller：接收觸發請求（應用系統事件）
	•	EventBus（KafkaEventBus / RabbitMQEventBus）：依設定傳送到中介
	•	Listener：從中介接收事件
	•	EventProcessor：共用業務處理邏輯
	•	Dispatcher：依事件類型選擇對應 Handler
	•	Handler：執行特定業務邏輯
	•	Repository：事件狀態 CRUD
	•	Logging & Monitoring Module：結構化日誌、Metrics
	3.	Database（MySQL / ElasticSearch）
	•	EventRecord（事件狀態、異動前後值）
	•	EventAttempt（嘗試紀錄）
	4.	Message Broker
	•	Kafka（SaaS 高吞吐）
	•	RabbitMQ（On-Prem 輕量）

⸻

C3: Component Diagram（元件圖）

目的：拆解 Event Trigger Service 內部組件與互動。

主要元件：
	•	Controller
	•	接收業務系統呼叫（REST API / Internal Call）
	•	封裝為 CustomEvent（含事件 ID、類型、payload、追蹤 ID）
	•	EventBus（Interface）
	•	KafkaEventBus：傳送至 Kafka topic
	•	RabbitMQEventBus：傳送至 RabbitMQ queue
	•	Listener
	•	KafkaEventListener / RabbitMQEventListener
	•	接收事件 → 呼叫 EventProcessor
	•	EventProcessor
	•	驗證事件
	•	呼叫 Dispatcher
	•	記錄處理狀態
	•	Dispatcher
	•	根據 event.type 選擇 Handler
	•	Handler（多個實作類）
	•	ChemicalStatusHandler
	•	RegulationCheckHandler
	•	HCardUpdateHandler
	•	Repository
	•	儲存事件紀錄（EventRecord）
	•	儲存嘗試紀錄（EventAttempt）
	•	Monitoring Module
	•	指標：吞吐、延遲、失敗率、DLQ 狀態
	•	整合告警

⸻

C4: Code / Class Diagram（核心代碼結構）

目的：展示關鍵類別與介面設計。

```Java
interface EventBus {
    void send(DomainEvent event);
    void sendAsync(DomainEvent event);
}

class KafkaEventBus implements EventBus { ... }
class RabbitMQEventBus implements EventBus { ... }

class EventProcessor {
    void processEvent(DomainEvent event) {
        // validate, dispatch, record status
    }
}

class Dispatcher {
    void dispatch(DomainEvent event) { ... }
}

interface Handler {
    void handle(DomainEvent event);
}

class ChemicalStatusHandler implements Handler { ... }
class RegulationCheckHandler implements Handler { ... }
class HCardUpdateHandler implements Handler { ... }

class EventRecordRepository { ... }
class EventAttemptRepository { ... }

```
⸻
