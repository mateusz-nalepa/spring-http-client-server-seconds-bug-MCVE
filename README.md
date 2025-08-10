# Current and expected behaviour

![Current and Expected behaviour.drawio.png](Current%20and%20Expected%20behaviour.drawio.png)

# Info
In tests, requests of batch is being sent. 

Request of batch == number of CPU

# Reproduce scenario

![Reproduce Scenario.drawio.png](Reproduce%20Scenario.drawio.png)

# How to reproduce bug

## Http Client Times - Observation starts too early

1. Run `RequestSenderApp`
2. Run `UndertowServerApp`
3. Run `MockExternalServiceApp`
4. Warmup
```shell
curl http://localhost:8080/warmup/undertow
```
5. Run simulation flow
```shell
curl http://localhost:8080/send-requests-default/undertow
```
6. Look for logs from `UndertowServerApp`
```text
First batch of requests:
END measuring client. Took: PT0s ✅

Second batch of requests:
END measuring client. Took: PT15s ❌ Should be 7s
```


## Http Server Times - Observation starts too late

1. Run `RequestSenderApp`
2. Run `VirtualApp`
3. Run `MockExternalServiceApp`
4. Warmup
```shell
curl http://localhost:8080/warmup/virtual
```
5. Run simulation flow
```shell
curl http://localhost:8080/send-requests-default/virtual
```
6. Look for logs from `VirtualApp`
```text
First batch of requests:
END measuring server. Took: PT10s ✅

Second batch of requests:
END measuring server. Took: PT17s ❌ Should be 25s
```
