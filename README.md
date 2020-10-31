# hazelcast-ftp
Pipeline batch source of FTP files for Hazelcast Jet.
```java
BatchSource<String> source = FtpSource.singleFile("/dir/file.txt", connectionParams());
p.readFrom(source).writeTo(sink);
        
BatchSource<TestType> source = FtpSource.fromDirectory("/csv", a -> true, connectionParams(), is -> new CsvInputStreamMapper<>(is, TestType.class));
p.readFrom(source).writeTo(sink);
```        
