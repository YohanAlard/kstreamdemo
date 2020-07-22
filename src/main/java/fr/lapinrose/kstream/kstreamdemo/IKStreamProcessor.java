package fr.lapinrose.kstream.kstreamdemo;

import org.apache.kafka.streams.kstream.KStream;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;

public interface IKStreamProcessor {
    static String INPUT="input";
    static String OUTPUT="output";

    @Input(INPUT)
    KStream<String, String> inputStream();

    @Output(OUTPUT)

    KStream<String, String> outputstream();
}
