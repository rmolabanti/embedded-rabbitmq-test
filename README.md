# embedded-rabbitmq-test

Issues faced 
- `brew install erlang` will install erlang 20 but Rabbitmq supports 19.3 only.
- By defalut erlang installed in `/usr/local/bin/erl`. Able to run `erl` commands from termical. But `org.zeroturnaround.exec.ProcessExecutor` not able to execute 'erl' commands. 
So replaced command in `ErlangShell.java` as following.
```	private static final String UNIX_ERL_COMMAND = "/usr/local/bin/erl";```

#### Test Case 
**EmbeddedRabbitMqTest#simpleConnection**
- Opens a RabbitMq connections and channel
- Sends simple message with basicPublish
- Checks received message from basicConsume




