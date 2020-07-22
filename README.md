# jsoncommunicator4java8

## Introduction

- This library suggests communicate method with JSON ([RFC8259](https://tools.ietf.org/html/rfc8259)).
- This library provides automatically parsing JSON <-> POJO(Plain Old Java Object).
- This library requires ([com.shimizukenta.jsonhub](https://github.com/kenta-shimizu/json4java8)) for parsing.

## About communicate method

1. Use `0x00` byte as delimiter. append delimiter after JSON bytes.

## How to use

### Create communicator instance and open, Client or Server

- Open server sample

```
JsonCommunicator<Pojo> server = JsonCommunicators.openServer(
    new InetSocketAddress("127.0.0.1", 10000),
    Pojo.class);
```

- Open client sample

```
JsonCommunicator<Pojo> client = JsonCommunicators.openClient(
    new InetSocketAddress("127.0.0.1", 10000),
    Pojo.class);
```

If you set `classOfT`, you can receive parsed POJO by `#addPojoReceivedListener`

### Send JSON or POJO

- Send JSON

```
String json = "{\"name\": \"John\"}";
client.send(json);
```

- Send POJO

```
Pojo pojo = new Pojo();
pojo.name = "John";

client.send(pojo);
```

### Receive JSON or POJO

- Add listener for receive JSON

```
client.addJsonReceivedListener((String json) -> {
    /* something ... */
});
```

- Add listener for receive parsed POJO

```
client.addPojoReceivedListener((Pojo pojo) -> {
    /* something ... */
});
```

see also [Examples](/src/examples/).
