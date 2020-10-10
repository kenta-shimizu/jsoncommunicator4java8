# jsoncommunicator4java8

## Introduction

- This library proposes communicate method by JSON ([RFC8259](https://tools.ietf.org/html/rfc8259)).
- This library provides automatically parsing JSON <-> POJO(Plain Old Java Object).
- This library requires ([com.shimizukenta.jsonhub](https://github.com/kenta-shimizu/json4java8)) for parsing.

## About communicate method

1. Use `0x00` byte as delimiter. append delimiter after JSON bytes.

## How to use

### Create communicator instance and open, Client or Server

- Open server sample

```java
JsonCommunicator<Pojo> server = JsonCommunicators.openServer(
    new InetSocketAddress("127.0.0.1", 10000),
    Pojo.class);
```

- Open client sample

```java
JsonCommunicator<Pojo> client = JsonCommunicators.openClient(
    new InetSocketAddress("127.0.0.1", 10000),
    Pojo.class);
```

If you set `classOfT`, you can receive parsed POJO by `#addPojoReceivedListener`

### Send JSON or POJO

- Send JSON

```java
String json = "{\"name\": \"John\"}";
client.send(json);
```

- Send POJO

```java
Pojo pojo = new Pojo();
pojo.name = "John";

client.send(pojo);
```

### Receive JSON or POJO

- Add listener for receive JSON

```java
client.addJsonReceiveListener((String json) -> {
    /* something ... */
});
```

- Add listener for receive parsed POJO

```java
client.addPojoReceiveListener((Pojo pojo) -> {
    /* something ... */
});
```

see also [Examples](/src/examples/).
